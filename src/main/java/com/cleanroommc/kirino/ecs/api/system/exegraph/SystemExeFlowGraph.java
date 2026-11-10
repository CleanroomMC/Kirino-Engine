package com.cleanroommc.kirino.ecs.api.system.exegraph;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * It guides the execution flow of ecs-systems.
 * You're supposed to instantiate an instance in {@link CleanWorld} to help you manage the system execution.
 *
 * @implNote The flow graphs should be immutable once constructed, and
 * the builder pattern ({@link Builder}) should be utilized to help constructing the flow graphs
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
     * @see #isExecuting()
     */
    void execute();

    /**
     * Async version of {@link #execute()}.
     */
    @NonNull
    CompletableFuture<Void> executeAsync(Executor executor);

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
         * <p>
         * {@link #START_NODE} and {@link #END_NODE} are built-in node IDs.
         * Moreover, everything must start from {@link #START_NODE} and end in {@link #END_NODE}.
         *
         * @param nodeID The node ID
         * @implNote Check the precondition that <code>nodeID</code> isn't {@link #START_NODE} or {@link #END_NODE}
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback);

        /**
         * A dummy version of {@link #addTransition(CleanSystem, String, String)}.
         * No system will be executed but you can still have callbacks.
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * Must not input the nodes that don't exist. {@link #START_NODE} and {@link #END_NODE} are allowed as built-in nodes.
         *
         * @param task       The ecs-system instance
         * @param fromNodeID From
         * @param toNodeID   To
         * @implNote Check the precondition that <code>fromNodeID</code> and <code>toNodeID</code> are valid
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> addTransition(@NonNull CleanSystem task, @NonNull String fromNodeID, @NonNull String toNodeID);

        /**
         * Set the callback of the {@link #START_NODE}.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> setStartNodeCallback(@Nullable Runnable callback);

        /**
         * Set the callback of the {@link #END_NODE}.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> setEndNodeCallback(@Nullable Runnable callback);

        /**
         * Set the callback of the method <code>{@link SystemExeFlowGraph#execute()} / {@link SystemExeFlowGraph#executeAsync(Executor)}</code>.
         * It's different from {@link #setEndNodeCallback(Runnable)} since the end node callback will be ran
         * before the finish callback.
         *
         * <p>A <code>null</code> callback clears the current callback.</p>
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> setFinishCallback(@Nullable Runnable callback);

        @CanIgnoreReturnValue
        @NonNull
        Builder<TFlowGraph> setFailureCallback(@Nullable Consumer<Throwable> callback);

        /**
         * @return The building result
         * @implNote Check the precondition that the execution graph is a DAG and
         * everything starts from {@link #START_NODE} and ends in {@link #END_NODE}
         */
        @NonNull
        TFlowGraph build();
    }

}
