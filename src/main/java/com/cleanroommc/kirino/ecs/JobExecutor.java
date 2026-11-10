package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler.ExecutionHandle;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.google.common.base.Throwables;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

final class JobExecutor {
    private final JobRegistry jobRegistry;
    private final EntityManager owner;
    private final JobWorkloadCache workloads = new JobWorkloadCache();

    JobExecutor(JobRegistry jobRegistry) {
        this(jobRegistry, null);
    }

    JobExecutor(JobRegistry jobRegistry, EntityManager owner) {
        this.jobRegistry = jobRegistry;
        this.owner = owner;
        if (owner != null) {
            owner.lifecycle.setClearScratch(workloads::clear);
        }
    }

    public ExecutionHandle executeParallelJob(com.cleanroommc.kirino.ecs.api.EntityManager manager,
                                              Class<? extends ParallelJob> clazz,
                                              @Nullable Map<String, Object> externalData,
                                              Executor executor) {
        if (!(manager instanceof EntityManager entityManager)) {
            throw new IllegalArgumentException("Jobs require the built-in ECS entity manager implementation");
        }
        if (owner != null && owner != entityManager) {
            throw new IllegalArgumentException("The scheduler and manager belong to different runtimes.");
        }
        RuntimeLifecycle.Lease lease = entityManager.lifecycle.acquire(entityManager.lifecycle.epoch());
        RuntimeLifecycle.Lease previous = lease.enter();
        Throwable failure = null;
        try {
            return executeAccepted(entityManager, clazz, externalData, executor, lease);
        } catch (Throwable executionFailure) {
            failure = executionFailure;
            ExecutionFailures.rethrow(executionFailure);
            throw new AssertionError();
        } finally {
            lease.leave(previous);
            lease.prepared(failure);
        }
    }

    private ExecutionHandle executeAccepted(EntityManager entityManager, Class<? extends ParallelJob> clazz,
                                            Map<String, Object> externalData, Executor executor,
                                            RuntimeLifecycle.Lease lease) {
        JobDefinition definition = jobRegistry.getDefinition(clazz);
        if (definition == null) {
            throw new IllegalStateException("Parallel job class " + clazz.getName() + " isn't registered.");
        }

        Object[] externalValues = definition.resolveExternalData(externalData);

        EntityQuery query = entityManager.newQuery();
        ((ParallelJob) definition.instantiator.instantiate()).query(query);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        EntityManager.ExecutionScope execution = entityManager.beginExecution(query);
        JobWorkload preparation = null;
        try {
            EntityCommands commands = execution.commands();
            ArchetypeDataPool[] archetypes = execution.pools();
            if (archetypes.length != 0) {
                preparation = workloads.acquire();
            }
            int threadOrdinal = 0;
            for (ArchetypeDataPool archetype : archetypes) {
                PrimitiveArray[] columns = definition.resolveColumns(archetype);
                ParallelJob job = definition.instantiate(columns, externalValues);

                PoolRows rows = archetype.getRows();
                int rowLimit = rows.limit;
                int[] nextFree = rows.nextFree;

                long workload = preparation.prepare(job, rows);

                // future count
                long targetWorkloadPerThread = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
                long requestedCount = Math.ceilDivExact(workload, targetWorkloadPerThread);
                int futureCount = (int) Math.min(requestedCount, Runtime.getRuntime().availableProcessors());
                if (requestedCount > futureCount) {
                    targetWorkloadPerThread = workload / futureCount;
                }

                if (futureCount <= 1) {
                    // run synchronously
                    for (int i = 0; i < rowLimit; i++) {
                        if (nextFree[i] != 0) {
                            continue;
                        }

                        job.execute(commands, i, archetype.getEntityID(i), threadOrdinal);
                    }
                    threadOrdinal++;
                } else {
                    // run asynchronously
                    int uniform = preparation.uniform;
                    int[] estimates = preparation.estimates;
                    int liveIndex = 0;
                    long accumulated = 0;
                    int startIndex = 0;

                    for (int i = 0; i < rowLimit; i++) {
                        if (nextFree[i] != 0) {
                            continue;
                        }

                        accumulated += uniform != 0 ? uniform : estimates[liveIndex];
                        liveIndex++;

                        if (accumulated >= targetWorkloadPerThread) {
                            final int endIndexExclusive = i + 1;
                            final int finalThreadOrdinal = threadOrdinal;
                            final int finalStartIndex = startIndex;

                            final ParallelJob jobPerThread = definition.instantiate(columns, externalValues);

                            futures.add(CompletableFuture.runAsync(() -> {
                                RuntimeLifecycle.Lease previous = lease.enter();
                                try {
                                    for (int j = finalStartIndex; j < endIndexExclusive; j++) {
                                        if (nextFree[j] != 0) {
                                            continue;
                                        }

                                        jobPerThread.execute(commands, j, archetype.getEntityID(j), finalThreadOrdinal);
                                    }
                                } finally {
                                    lease.leave(previous);
                                }
                            }, executor));

                            threadOrdinal++;
                            startIndex = endIndexExclusive;
                            accumulated = 0;
                        }
                    }

                    if (accumulated != 0) {
                        final int finalThreadOrdinal = threadOrdinal;
                        final int finalStartIndex = startIndex;

                        final ParallelJob jobPerThread = definition.instantiate(columns, externalValues);

                        futures.add(CompletableFuture.runAsync(() -> {
                            RuntimeLifecycle.Lease previous = lease.enter();
                            try {
                                for (int j = finalStartIndex; j < rowLimit; j++) {
                                    if (nextFree[j] != 0) {
                                        continue;
                                    }

                                    jobPerThread.execute(commands, j, archetype.getEntityID(j), finalThreadOrdinal);
                                }
                            } finally {
                                lease.leave(previous);
                            }
                        }, executor));

                        threadOrdinal++;
                    }
                }
            }

            CompletableFuture<Void> completion = finishExecution(futures, execution, null);
            lease.follow(completion);
            SystemExecution.track(lease.completion);
            return new ExecutionHandle(new ObservedCompletion(lease.completion), threadOrdinal, !futures.isEmpty());
        } catch (Throwable failure) {
            CompletableFuture<Void> completion = finishExecution(futures, execution, failure);
            lease.follow(completion);
            SystemExecution.track(lease.completion);
            Throwables.throwIfUnchecked(failure);
            throw new CompletionException(failure);
        } finally {
            if (preparation != null) {
                workloads.release(preparation);
            }
        }
    }

    private static CompletableFuture<Void> finishExecution(List<CompletableFuture<Void>> futures,
                                                           EntityManager.ExecutionScope execution, Throwable initialFailure) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((unused, failure) -> {
            Throwable resultFailure = initialFailure;
            for (CompletableFuture<Void> future : futures) {
                try {
                    future.join();
                } catch (Throwable taskFailure) {
                    resultFailure = ExecutionFailures.merge(resultFailure, taskFailure);
                }
            }
            try {
                execution.close();
            } catch (Throwable releaseFailure) {
                resultFailure = ExecutionFailures.merge(resultFailure, releaseFailure);
            }
            if (resultFailure == null) {
                result.complete(null);
            } else {
                result.completeExceptionally(resultFailure);
            }
        });
        return result;
    }
}
