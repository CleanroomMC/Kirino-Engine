package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.schema.meta.SchemaLayout;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
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

    public ExecutionHandle executeParallelJob(EntityManager entityManager, Class<? extends ParallelJob> clazz, @Nullable Map<String, Object> externalData, Executor executor) {
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
        List<ArchetypeDataPool> archetypes = entityManager.startQuery(query);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        int threadOrdinal = 0;
        for (ArchetypeDataPool archetype : archetypes) {
            ParallelJob job = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

            ArrayRange arrayRange = archetype.getArrayRange();

            int workload = 0;
            for (int i = arrayRange.start; i < arrayRange.end; i++) {
                if (arrayRange.deprecatedIndexes.contains(i)) {
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
                for (int i = arrayRange.start; i < arrayRange.end; i++) {
                    if (arrayRange.deprecatedIndexes.contains(i)) {
                        continue;
                    }

                    Optional<Integer> entityID = archetype.getEntityID(i);

                    job.execute(entityManager, i, entityID.orElseThrow(IllegalStateException::new), threadOrdinal);
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

                        final ParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                        futures.add(CompletableFuture.runAsync(() -> {
                            for (int j = finalStartIndex; j < endIndexExclusive; j++) {
                                if (arrayRange.deprecatedIndexes.contains(j)) {
                                    continue;
                                }

                                Optional<Integer> entityID = archetype.getEntityID(j);

                                jobPerThread.execute(entityManager, j, entityID.orElseThrow(IllegalStateException::new), finalThreadOrdinal);
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

                    final ParallelJob jobPerThread = newParallelJob(instantiator, parallelJobDataQueries, parallelJobExternalDataQueries, archetype, externalData);

                    futures.add(CompletableFuture.runAsync(() -> {
                        for (int j = finalStartIndex; j < arrayRange.end; j++) {
                            if (arrayRange.deprecatedIndexes.contains(j)) {
                                continue;
                            }

                            Optional<Integer> entityID = archetype.getEntityID(j);

                            jobPerThread.execute(entityManager, j, entityID.orElseThrow(IllegalStateException::new), finalThreadOrdinal);
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
