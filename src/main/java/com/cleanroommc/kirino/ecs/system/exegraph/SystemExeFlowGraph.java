package com.cleanroommc.kirino.ecs.system.exegraph;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.system.ExecutionContainer;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * It guides the execution flow of ecs-systems.
 * You're supposed to instantiate an instance in {@link CleanWorld} to help you manage the system execution.
 *
 * @implNote The flow graphs should be immutable once constructed, and
 *           the builder pattern ({@link Builder}) should be utilized to help constructing the flow graphs
 */
public interface SystemExeFlowGraph {
    String START_NODE = "__START__";
    String END_NODE = "__END__";

    /**
     * This method is blocking.
     * Must not execute again before finishing the last execution.
     * Thread safety is guaranteed.
     *
     * @implNote Guarantee the thread safety and check the precondition (directly throw if needed) that the last execution is finished.
     *           Must call {@link #executeSystem(CleanWorld, CleanSystem)} and {@link #joinSystem(CleanSystem)} on every transition edge
     *           to actually generate and join the completable futures from a system
     * @see #isExecuting()
     */
    void execute();

    /**
     * Async version of {@link #execute()}.
     */
    default CompletableFuture<Void> executeAsync(Executor executor) {
        return CompletableFuture.runAsync(this::execute, executor);
    }

    /**
     * @return Whether the execution is ongoing
     * @implNote The flag should be volatile or guarded by the synchronization
     */
    boolean isExecuting();

    /**
     * The builder of an execution flow graph.
     * The execution graph must be a DAG and everything must start from {@link #START_NODE} and ends in {@link #END_NODE}.
     */
    interface Builder<TFlowGraph extends SystemExeFlowGraph> {
        /**
         * <p>Must not input {@link #START_NODE} or {@link #END_NODE}.</p>
         * <p>Inputting the same <code>nodeID</code> again overrides the old node.</p>
         * <br>
         *
         * {@link #START_NODE} and {@link #END_NODE} are built-in node IDs.
         * Moreover, everything must start from {@link #START_NODE} and end in {@link #END_NODE}.
         *
         * @implNote Check the precondition that <code>nodeID</code> isn't {@link #START_NODE} or {@link #END_NODE}
         * @param nodeID The node ID
         */
        @NonNull
        Builder<TFlowGraph> addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback);

        /**
         * A dummy version of {@link #addTransition(CleanSystem, String, String)}.
         * No system will be executed but you can still have callbacks.
         */
        @NonNull
        Builder<TFlowGraph> addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * Must not input the nodes that don't exist. {@link #START_NODE} and {@link #END_NODE} are allowed as built-in nodes.
         *
         * @implNote Check the precondition that <code>fromNodeID</code> and <code>toNodeID</code> are valid
         * @param task The ecs-system instance
         * @param fromNodeID From
         * @param toNodeID To
         */
        @NonNull
        Builder<TFlowGraph> addTransition(@NonNull CleanSystem task, @NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * Set the callback of the {@link #START_NODE}.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @NonNull
        Builder<TFlowGraph> setStartNodeCallback(@Nullable Runnable callback);

        /**
         * Set the callback of the {@link #END_NODE}.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @NonNull
        Builder<TFlowGraph> setEndNodeCallback(@Nullable Runnable callback);

        /**
         * Set the callback of the method <code>{@link SystemExeFlowGraph#execute()} / {@link SystemExeFlowGraph#executeAsync(Executor)}</code>.
         * It's different from {@link #setEndNodeCallback(Runnable)} since the end node callback will be ran
         * before the finish callback.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @NonNull
        Builder<TFlowGraph> setFinishCallback(@Nullable Runnable callback);

        /**
         * @implNote Check the precondition that the execution graph is a DAG and
         *           everything starts from {@link #START_NODE} and ends in {@link #END_NODE}
         * @return The building result
         */
        @NonNull
        TFlowGraph build();
    }

    class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldGetter(CleanSystem.class, "execution", ExecutionContainer.class),
                    ReflectionUtils.getFieldGetter(ExecutionContainer.class, "handles", List.class),
                    ReflectionUtils.getFieldGetter(ExecutionContainer.class, "futures", List.class),
                    ReflectionUtils.getFieldGetter(CleanWorld.class, "entityManager", EntityManager.class),
                    ReflectionUtils.getFieldGetter(CleanWorld.class, "jobScheduler", JobScheduler.class));

            Preconditions.checkNotNull(DELEGATE.executionContainerGetter);
            Preconditions.checkNotNull(DELEGATE.handlesGetter);
            Preconditions.checkNotNull(DELEGATE.futuresGetter);
            Preconditions.checkNotNull(DELEGATE.entityManagerGetter);
            Preconditions.checkNotNull(DELEGATE.jobSchedulerGetter);
        }

        static ExecutionContainer getExecutionContainer(CleanSystem cleanSystem) {
            ExecutionContainer result;
            try {
                result = (ExecutionContainer) DELEGATE.executionContainerGetter.invokeExact(cleanSystem);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        static List<JobScheduler.ExecutionHandle> getHandles(ExecutionContainer executionContainer) {
            List<JobScheduler.ExecutionHandle> result;
            try {
                result = (List<JobScheduler.ExecutionHandle>) DELEGATE.handlesGetter.invokeExact(executionContainer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        static List<CompletableFuture<Void>> getFutures(ExecutionContainer executionContainer) {
            List<CompletableFuture<Void>> result;
            try {
                result = (List<CompletableFuture<Void>>) DELEGATE.futuresGetter.invokeExact(executionContainer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static EntityManager getEntityManager(CleanWorld cleanWorld) {
            EntityManager result;
            try {
                result = (EntityManager) DELEGATE.entityManagerGetter.invokeExact(cleanWorld);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static JobScheduler getJobScheduler(CleanWorld cleanWorld) {
            JobScheduler result;
            try {
                result = (JobScheduler) DELEGATE.jobSchedulerGetter.invokeExact(cleanWorld);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
                MethodHandle executionContainerGetter,
                MethodHandle handlesGetter,
                MethodHandle futuresGetter,
                MethodHandle entityManagerGetter,
                MethodHandle jobSchedulerGetter) {
        }
    }

    static void joinSystem(CleanSystem system) {
        ExecutionContainer executionContainer = MethodHolder.getExecutionContainer(system);
        List<JobScheduler.ExecutionHandle> handles = MethodHolder.getHandles(executionContainer);
        List<CompletableFuture<Void>> futures = MethodHolder.getFutures(executionContainer);

        List<CompletableFuture<Void>> allFutures = new ArrayList<>();
        for (JobScheduler.ExecutionHandle handle : handles) {
            if (handle.async()) {
                allFutures.add(handle.future());
            }
        }
        allFutures.addAll(futures);

        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
    }

    static void executeSystem(CleanWorld world, CleanSystem system) {
        EntityManager entityManager = MethodHolder.getEntityManager(world);
        JobScheduler jobScheduler = MethodHolder.getJobScheduler(world);
        ExecutionContainer executionContainer = MethodHolder.getExecutionContainer(system);

        executionContainer.noExecutions();
        system.update(entityManager, jobScheduler);
    }
}
