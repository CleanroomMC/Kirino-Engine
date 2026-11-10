package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler.ExecutionHandle;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

final class LegacyJobExecutor {
    private final LegacyJobRegistry jobRegistry;

    LegacyJobExecutor(LegacyJobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public ExecutionHandle executeParallelJob(com.cleanroommc.kirino.ecs.api.EntityManager manager,
                                              Class<? extends ParallelJob> clazz,
                                              @Nullable Map<String, Object> externalData,
                                              Executor executor) {
        if (!(manager instanceof EntityManager entityManager)) {
            throw new IllegalArgumentException("Jobs require the built-in ECS entity manager implementation");
        }
        Map<JobDataQuery, JobDataInjector> parallelJobDataQueries = jobRegistry.getParallelJobDataQueries(clazz);
        Map<String, JobDataInjector> parallelJobExternalDataQueries = jobRegistry.getParallelJobExternalDataQueries(clazz);
        JobInstantiator instantiator = jobRegistry.getParallelJobInstantiator(clazz);
        if (parallelJobDataQueries == null || parallelJobExternalDataQueries == null || instantiator == null) {
            throw new IllegalStateException("Parallel job class " + clazz.getName() + " isn't registered.");
        }

        if (!parallelJobExternalDataQueries.isEmpty()) {
            if (externalData == null) {
                throw new IllegalArgumentException("Argument \"externalData\" must not be null since there are "
                    + parallelJobExternalDataQueries.size() + " external data queries.");
            }
            for (String key : parallelJobExternalDataQueries.keySet()) {
                Preconditions.checkArgument(externalData.containsKey(key),
                    "Missing the entry \"%s\" from \"externalData\".", key);
            }
        }

        EntityQuery query = entityManager.newQuery();
        ((ParallelJob) instantiator.instantiate()).query(query);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        EntityManager.ExecutionScope execution = entityManager.beginExecution(query);
        try {
            EntityCommands commands = execution.commands();
            ArchetypeDataPool[] archetypes = execution.pools();
            int threadOrdinal = 0;
            for (ArchetypeDataPool archetype : archetypes) {
                ParallelJob job = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                PoolRows rows = archetype.getRows();
                int rowLimit = rows.limit;
                int[] nextFree = rows.nextFree;

                int workload = 0;
                for (int i = 0; i < rowLimit; i++) {
                    if (nextFree[i] != 0) {
                        continue;
                    }

                    int jobWorkload = job.estimateWorkload(i);
                    if (jobWorkload < 1) {
                        throw new IllegalStateException("The estimated workload at index=" + i
                            + " must be greater than or equal to 1. (Parallel job class: " + clazz.getName() + ")");
                    }

                    workload += jobWorkload;
                }

                // future count
                int targetWorkloadPerThread = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
                int futureCount = Math.ceilDivExact(workload, targetWorkloadPerThread);
                if (futureCount > Runtime.getRuntime().availableProcessors()) {
                    futureCount = Runtime.getRuntime().availableProcessors();
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
                    int accumulated = 0;
                    int startIndex = 0;

                    for (int i = 0; i < rowLimit; i++) {
                        if (nextFree[i] != 0) {
                            continue;
                        }

                        accumulated += job.estimateWorkload(i);

                        if (accumulated >= targetWorkloadPerThread) {
                            final int endIndexExclusive = i + 1;
                            final int finalThreadOrdinal = threadOrdinal;
                            final int finalStartIndex = startIndex;

                            final ParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                            futures.add(CompletableFuture.runAsync(() -> {
                                for (int j = finalStartIndex; j < endIndexExclusive; j++) {
                                    if (nextFree[j] != 0) {
                                        continue;
                                    }

                                    jobPerThread.execute(commands, j, archetype.getEntityID(j), finalThreadOrdinal);
                                }
                            }, executor));

                            threadOrdinal++;
                            startIndex = endIndexExclusive;
                            accumulated = 0;
                        }
                    }

                    boolean hasRemainingWork = false;
                    for (int j = startIndex; j < rowLimit; j++) {
                        if (nextFree[j] == 0) {
                            hasRemainingWork = true;
                            break;
                        }
                    }

                    if (hasRemainingWork) {
                        final int finalThreadOrdinal = threadOrdinal;
                        final int finalStartIndex = startIndex;

                        final ParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                        futures.add(CompletableFuture.runAsync(() -> {
                            for (int j = finalStartIndex; j < rowLimit; j++) {
                                if (nextFree[j] != 0) {
                                    continue;
                                }

                                jobPerThread.execute(commands, j, archetype.getEntityID(j), finalThreadOrdinal);
                            }
                        }, executor));

                        threadOrdinal++;
                    }
                }
            }

            return new ExecutionHandle(finishExecution(futures, execution), threadOrdinal, !futures.isEmpty());
        } catch (Throwable failure) {
            finishExecution(futures, execution);
            Throwables.throwIfUnchecked(failure);
            throw new CompletionException(failure);
        }
    }

    private static CompletableFuture<Void> finishExecution(List<CompletableFuture<Void>> futures,
                                                           EntityManager.ExecutionScope execution) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((unused, failure) -> {
            Throwable resultFailure = failure;
            try {
                execution.close();
            } catch (Throwable releaseFailure) {
                if (resultFailure == null) {
                    resultFailure = releaseFailure;
                } else if (resultFailure != releaseFailure) {
                    resultFailure.addSuppressed(releaseFailure);
                }
            }
            if (resultFailure == null) {
                result.complete(null);
            } else {
                result.completeExceptionally(resultFailure);
            }
        });
        return result;
    }

    private ParallelJob newParallelJob(
        JobInstantiator instantiator,
        Map<JobDataQuery, JobDataInjector> parallelJobDataQueries,
        Map<String, JobDataInjector> parallelJobExternalDataQueries,
        ArchetypeDataPool archetype,
        @Nullable Map<String, Object> externalData) {

        ParallelJob job = (ParallelJob) instantiator.instantiate();

        // data injection
        for (Map.Entry<JobDataQuery, JobDataInjector> entry : parallelJobDataQueries.entrySet()) {
            SchemaLayout.Column column = jobRegistry.getParallelJobColumn(job.getClass(), entry.getKey());
            Class<? extends CleanComponent> component = entry.getKey().componentClass().asSubclass(CleanComponent.class);
            PrimitiveArray array = column == null ? archetype.getArray(component, entry.getKey().fieldAccessChain())
                : archetype.getArray(component, column);
            entry.getValue().inject(job, array);
        }
        if (externalData != null) {
            for (Map.Entry<String, JobDataInjector> entry : parallelJobExternalDataQueries.entrySet()) {
                entry.getValue().inject(job, externalData.get(entry.getKey()));
            }
        }

        return job;
    }
}
