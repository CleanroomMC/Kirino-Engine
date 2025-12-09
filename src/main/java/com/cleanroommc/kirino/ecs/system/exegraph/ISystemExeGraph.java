package com.cleanroommc.kirino.ecs.system.exegraph;

import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.system.ExecutionContainer;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISystemExeGraph {
    /**
     * Must not execute again before finishing the last execution.
     *
     * @implNote Check the precondition that the last execution is finished
     * @see #isExecuting()
     */
    void execute();

    /**
     * @return Whether the execution is ongoing
     */
    boolean isExecuting();

    interface IBuilder {
        /**
         * <p>Must not input <code>START</code> or <code>END</code>.</p>
         * <p>Inputting the same <code>nodeID</code> again overrides the old node.</p>
         * <br>
         *
         * <code>START</code> and <code>END</code> are built-in node IDs.
         * Moreover, <code>START</code> and <code>END</code> are not barrier nodes.
         * They will not force the execution to be synchronized.
         *
         * @implNote Check the precondition that <code>nodeID</code> isn't <code>START</code> or <code>END</code>
         * @param nodeID The node ID
         */
        @NonNull
        IBuilder addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback);

        @NonNull
        IBuilder addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * Must not input the nodes that don't exist. <code>START</code> and <code>END</code> are allowed as built-in nodes.
         *
         * @implNote Check the precondition that <code>fromNodeID</code> and <code>toNodeID</code> are valid
         * @param task The ecs-system instance
         * @param fromNodeID From
         * @param toNodeID To
         */
        @NonNull
        IBuilder addTransition(@NonNull CleanSystem task, @NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * A <code>null</code> callback clears the callback.
         */
        @NonNull
        IBuilder setStartCallback(@Nullable Runnable callback);

        /**
         * A <code>null</code> callback clears the callback.
         */
        @NonNull
        IBuilder setEndCallback(@Nullable Runnable callback);

        /**
         * @implNote Check the precondition that the execution graph is a DAG and
         *           everything starts from the <code>START</code> node and ends in the <code>END</code> node
         * @return The building result
         */
        @NonNull
        ISystemExeGraph build();
    }

    class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldGetter(CleanSystem.class, "execution", ExecutionContainer.class),
                    ReflectionUtils.getFieldGetter(ExecutionContainer.class, "handles", List.class),
                    ReflectionUtils.getFieldGetter(ExecutionContainer.class, "futures", List.class));

            Preconditions.checkNotNull(DELEGATE.executionContainerGetter);
            Preconditions.checkNotNull(DELEGATE.handlesGetter);
            Preconditions.checkNotNull(DELEGATE.futuresGetter);
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
        static List<CompletableFuture<?>> getFutures(ExecutionContainer executionContainer) {
            List<CompletableFuture<?>> result;
            try {
                result = (List<CompletableFuture<?>>) DELEGATE.futuresGetter.invokeExact(executionContainer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
                MethodHandle executionContainerGetter,
                MethodHandle handlesGetter,
                MethodHandle futuresGetter) {
        }
    }
}
