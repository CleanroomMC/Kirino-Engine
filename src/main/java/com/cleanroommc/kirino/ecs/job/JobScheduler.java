package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JobScheduler {
    private final JobRegistry jobRegistry;

    public JobScheduler(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public record ExecutionHandle(@NonNull CompletableFuture<Void> future, int totalThreadCount, boolean async) {
    }

    public ExecutionHandle executeParallelJob(EntityManager entityManager, Class<? extends IParallelJob> clazz, @Nullable Map<String, Object> externalData, Executor executor) {
        Map<JobDataQuery, IJobDataInjector> parallelJobDataQueries = jobRegistry.getParallelJobDataQueries(clazz);
        Map<String, IJobDataInjector> parallelJobExternalDataQueries = jobRegistry.getParallelJobExternalDataQueries(clazz);
        IJobInstantiator instantiator = jobRegistry.getParallelJobInstantiator(clazz);
        if (parallelJobDataQueries == null || parallelJobExternalDataQueries == null || instantiator == null) {
            throw new IllegalStateException("Parallel job class " + clazz.getName() + " isn't registered.");
        }

        if (!parallelJobExternalDataQueries.isEmpty()) {
            Preconditions.checkArgument(externalData != null,
                    "Argument \"externalData\" must not be null since there are %d external data queries.", parallelJobExternalDataQueries.size());
            for (String key : parallelJobExternalDataQueries.keySet()) {
                Preconditions.checkArgument(externalData.containsKey(key),
                        "Missing the entry \"%s\" from \"externalData\".", key);
            }
        }

        EntityQuery query = entityManager.newQuery();
        ((IParallelJob) instantiator.instantiate()).query(query);
        List<ArchetypeDataPool> archetypes = entityManager.startQuery(query);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        int threadOrdinal = 0;
        for (ArchetypeDataPool archetype : archetypes) {
            IParallelJob job = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

            ArrayRange arrayRange = archetype.getArrayRange();

            int workload = 0;
            for (int i = arrayRange.start; i < arrayRange.end; i++) {
                if (arrayRange.deprecatedIndexes.contains(i)) {
                    continue;
                }

                int jobWorkload = job.estimateWorkload(i);
                Preconditions.checkState(jobWorkload >= 1,
                        "The estimated workload at index=%d must be greater than or equal to 1. (Parallel job class: %s)",
                        i, clazz.getName());

                workload += jobWorkload;
            }

            // future count
            int targetWorkloadPerThread = KirinoCore.KIRINO_CONFIG_HUB.targetWorkloadPerThread;
            int futureCount = Math.ceilDivExact(workload, targetWorkloadPerThread);
            if (futureCount > Runtime.getRuntime().availableProcessors()) {
                futureCount = Runtime.getRuntime().availableProcessors();
                targetWorkloadPerThread = workload / futureCount;
            }

            if (futureCount <= 1) {
                // run synchronously
                for (int i = arrayRange.start; i < arrayRange.end; i++) {
                    if (arrayRange.deprecatedIndexes.contains(i)) {
                        continue;
                    }

                    Optional<Integer> entityID = archetype.getEntityID(i);
                    Preconditions.checkState(entityID.isPresent());

                    job.execute(entityManager, i, entityID.get(), threadOrdinal);
                }
                threadOrdinal++;
            } else {
                // run asynchronously
                int accumulated = 0;
                int startIndex = arrayRange.start;

                for (int i = arrayRange.start; i < arrayRange.end; i++) {
                    if (arrayRange.deprecatedIndexes.contains(i)) {
                        continue;
                    }

                    accumulated += job.estimateWorkload(i);

                    if (accumulated >= targetWorkloadPerThread) {
                        final int endIndexExclusive = i + 1;
                        final int finalThreadOrdinal = threadOrdinal;
                        final int finalStartIndex = startIndex;

                        final IParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                        futures.add(CompletableFuture.runAsync(() -> {
                            for (int j = finalStartIndex; j < endIndexExclusive; j++) {
                                if (arrayRange.deprecatedIndexes.contains(j)) {
                                    continue;
                                }

                                Optional<Integer> entityID = archetype.getEntityID(j);
                                Preconditions.checkState(entityID.isPresent());

                                jobPerThread.execute(entityManager, j, entityID.get(), finalThreadOrdinal);
                            }
                        }, executor));

                        threadOrdinal++;
                        startIndex = endIndexExclusive;
                        accumulated = 0;
                    }
                }

                boolean hasRemainingWork = false;
                for (int j = startIndex; j < arrayRange.end; j++) {
                    if (!arrayRange.deprecatedIndexes.contains(j)) {
                        hasRemainingWork = true;
                        break;
                    }
                }

                if (hasRemainingWork) {
                    final int finalThreadOrdinal = threadOrdinal;
                    final int finalStartIndex = startIndex;

                    final IParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                    futures.add(CompletableFuture.runAsync(() -> {
                        for (int j = finalStartIndex; j < arrayRange.end; j++) {
                            if (arrayRange.deprecatedIndexes.contains(j)) {
                                continue;
                            }

                            Optional<Integer> entityID = archetype.getEntityID(j);
                            Preconditions.checkState(entityID.isPresent());

                            jobPerThread.execute(entityManager, j, entityID.get(), finalThreadOrdinal);
                        }
                    }, executor));

                    threadOrdinal++;
                }
            }
        }

        if (futures.isEmpty()) {
            return new ExecutionHandle(CompletableFuture.completedFuture(null), threadOrdinal, false);
        } else if (futures.size() == 1) {
            return new ExecutionHandle(futures.getFirst(), threadOrdinal, true);
        } else {
            return new ExecutionHandle(CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])), threadOrdinal, true);
        }
    }

    private IParallelJob newParallelJob(
            IJobInstantiator instantiator,
            Map<JobDataQuery, IJobDataInjector> parallelJobDataQueries,
            Map<String, IJobDataInjector> parallelJobExternalDataQueries,
            ArchetypeDataPool archetype,
            @Nullable Map<String, Object> externalData) {

        IParallelJob job = (IParallelJob) instantiator.instantiate();

        // data injection
        for (Map.Entry<JobDataQuery, IJobDataInjector> entry : parallelJobDataQueries.entrySet()) {
            IPrimitiveArray array = archetype.getArray(entry.getKey().componentClass().asSubclass(ICleanComponent.class), entry.getKey().fieldAccessChain());
            entry.getValue().inject(job, array);
        }
        if (externalData != null) {
            for (Map.Entry<String, IJobDataInjector> entry : parallelJobExternalDataQueries.entrySet()) {
                entry.getValue().inject(job, externalData.get(entry.getKey()));
            }
        }

        return job;
    }
}
