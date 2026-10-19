package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Finite State Machine
 *
 * <p>Instances and builders are not thread-safe. Callbacks may inspect the current state,
 * but must not call {@link #accept(Object)}, {@link #backtrack()}, {@link #reset()} or
 * {@link #rewind()} on the same machine.</p>
 *
 * @param <S> State type
 * @param <I> Input type
 * @implSpec Has to store a state transition table, transition callbacks, current state and a backlog.
 * @author Eerie
 */
public interface FiniteStateMachine<S, I> {

    /**
     * State getter
     *
     * @return Current State
     */
    @NonNull
    S state();

    /**
     * Accepts input.
     * If it fails call the corresponding {@link ErrorCallback error callback} and return an empty optional.
     * State and history are committed after exit and entry callbacks succeed; callback exceptions propagate
     * without changing either. External side effects of callbacks are not automatically undone.
     *
     * @param input Input to the state machine
     * @return Next state the FSM transitions to
     * @implSpec If state transition is successful, execute the {@link OnEnterStateCallback}
     *           and {@link OnExitStateCallback}.
     * @throws IllegalArgumentException If the input is outside the configured range, enum or table input set
     * @throws IllegalStateException If called recursively from a callback on this machine
     */
    @NonNull
    Optional<S> accept(@NonNull I input);

    /**
     * Backtrack the state machine to its previous state
     *
     * @return The current state (the one that is backtracked from) and input leading towards it.
     * @implSpec The state machine stores previous inputs and states for backtracking.
     *           Execute the rollback callback before committing the backtrack; if it throws, state and history are unchanged.
     */
    @NonNull
    Optional<FSMBacklogPair<S, I>> backtrack();

    /**
     * Sets the state to the initial state and clears the backlog without running callbacks.
     */
    void reset();

    /**
     * A quick way to undo all the actions taken by the entire state machine
     */
    default void rewind() {
        while (backtrack().isPresent()) {
            // backtrack performs each rollback
        }
    }

    @FunctionalInterface
    interface Rollback<S, I> {
        /**
         * Undo the changes made by the {@link OnEnterStateCallback} and {@link OnExitStateCallback}
         *
         * @param prevState State from which the function backtracks
         * @param input The input that caused the backtracked transition
         * @param currState The state that the FSM backtracks to
         */
        void rollback(@NonNull S prevState, @NonNull I input, @NonNull S currState);
    }

    @FunctionalInterface
    interface ErrorCallback<S, I> {
        /**
         * Transition failure callback
         *
         * @param state The current state
         * @param input The input that caused the failure
         */
        void error(@NonNull S state, @NonNull I input);
    }

    @FunctionalInterface
    interface OnEnterStateCallback<S, I> {
        /**
         * Called while transitioning to a state
         *
         * @param prevState The state the FSM is transitioning from
         * @param input The input causing the transition
         * @param nextState The state the input is transitioning towards
         * @apiNote Executed after {@link OnExitStateCallback}
         */
        void transition(@NonNull S prevState, @NonNull I input, @NonNull S nextState);
    }

    @FunctionalInterface
    interface OnExitStateCallback<S, I> {
        /**
         * Called while exiting a state
         *
         * @param currState The state the FSM is transitioning from
         * @param input The input causing the transition
         * @param nextState The state the input is transitioning towards
         * @apiNote Executed before {@link OnEnterStateCallback}
         */
        void transition(@NonNull S currState, @NonNull I input, @NonNull S nextState);
    }

    interface Builder<S, I> {
        /**
         * Adds a possible transition to the FSM
         * <p>Adding the same state/input pair again replaces its destination and rollback callback.</p>
         *
         * @param state                From
         * @param input                The input causing the transition
         * @param nextState            To
         * @param onEnterStateCallback Executed during transition to nextState.
         *                             If <code>null</code> is passed to this parameter, nothing will change.
         *                             A non-null value overrides the {@link OnEnterStateCallback}
         *                             for the destination state. <b>This callback is stored per state</b>
         * @param onExitStateCallback  Executed during transition from state.
         *                             If <code>null</code> is passed to this parameter, nothing will change.
         *                             A non-null value overrides the {@link OnExitStateCallback}
         *                             for the source state. <b>This callback is stored per state</b>
         * @param rollbackCallback     Executed during {@link FiniteStateMachine#backtrack()}.
         *                             It overrides the {@link Rollback} regardless of nullability.
         *                             <b>This callback is stored per transition,
         *                             it is different for each state and input combination.</b>
         * @return The builder
         * @throws IllegalArgumentException If a state or input is outside the configured enum or integer range
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                @Nullable Rollback<S, I> rollbackCallback);

        /**
         * Sets the entry callback for a state
         *
         * @param state    The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         *                 {@link Builder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @return The builder
         * @throws IllegalArgumentException If the state is outside the configured enum or integer range
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback);

        /**
         * Sets the exit callback for a state
         *
         * @param state    The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         *                 {@link Builder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @return The builder
         * @throws IllegalArgumentException If the state is outside the configured enum or integer range
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback);

        /**
         * Sets the initial state, that the FSM will start in. <br/>
         * <b>MUST BE CALLED BEFORE {@link Builder#build()}!!!</b>
         *
         * @param initialState The initial state
         * @return The builder
         * @throws IllegalArgumentException If the state is outside the configured enum or integer range
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> initialState(@NonNull S initialState);

        /**
         * Sets the error callback which will be executed if and only if a transition fails
         * due to an input that leads to a non-existent route. An exception will be thrown if
         * the input itself is invalid.
         *
         * @param errorCallback The error callback
         * @return The builder
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> error(@NonNull ErrorCallback<S, I> errorCallback);

        /**
         * @implNote Throw an exception if any configured state cannot be reached from the initial state.
         * @implSpec Use DFS or another graph traversal algorithm
         */
        @CanIgnoreReturnValue
        @NonNull
        Builder<S, I> validate();

        /**
         * Builds an independent snapshot of the configured transitions and callbacks. <br/>
         * <b>{@link Builder#initialState(Object)} must be called before this!!!</b>
         *
         * @return The FSM
         */
        @NonNull
        FiniteStateMachine<S, I> build();

        // defaults
        @CanIgnoreReturnValue
        @NonNull
        default Builder<S, I> addTransition(@NonNull S state, @NonNull I input, @NonNull S nextState) {
            return addTransition(state, input, nextState, null, null, null);
        }

        @CanIgnoreReturnValue
        @NonNull
        default Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @NonNull OnEnterStateCallback<S, I> onEnterStateCallback,
                @NonNull OnExitStateCallback<S, I> onExitStateCallback) {

            Preconditions.checkNotNull(onEnterStateCallback);
            Preconditions.checkNotNull(onExitStateCallback);

            return addTransition(state, input, nextState, onEnterStateCallback, onExitStateCallback, null);
        }

        @CanIgnoreReturnValue
        @NonNull
        default Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @NonNull OnEnterStateCallback<S, I> onEnterStateCallback) {

            Preconditions.checkNotNull(onEnterStateCallback);

            return addTransition(state, input, nextState, onEnterStateCallback, null, null);
        }

        @CanIgnoreReturnValue
        @NonNull
        default Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @NonNull OnExitStateCallback<S, I> onExitStateCallback) {

            Preconditions.checkNotNull(onExitStateCallback);

            return addTransition(state, input, nextState, null, onExitStateCallback, null);
        }

        @CanIgnoreReturnValue
        @NonNull
        default Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @NonNull Rollback<S, I> rollbackCallback) {

            Preconditions.checkNotNull(rollbackCallback);

            return addTransition(state, input, nextState, null, null, rollbackCallback);
        }
    }

    class BuilderImpl {

        @NonNull
        public static <S extends Enum<S>, I extends Enum<I>> Builder<S, I> enumStateMachine(
                @NonNull Class<S> stateClass,
                @NonNull Class<I> inputClass) {

            return new EnumStateMachine.BuilderImpl<>(stateClass, inputClass);
        }

        @NonNull
        public static <S, I> Builder<S, I> tableStateMachine() {
            return new TableFiniteStateMachine.BuilderImpl<>();
        }

        @NonNull
        public static Builder<Integer, Integer> intRangeStateMachine(
                int lowerStateBoundInclusive,
                int upperStateBoundInclusive,
                int lowerInputBoundInclusive,
                int upperInputBoundInclusive) {

            return new IntRangeStateMachine.BuilderImpl(
                    lowerStateBoundInclusive,
                    upperStateBoundInclusive,
                    lowerInputBoundInclusive,
                    upperInputBoundInclusive);
        }

        @NonNull
        public static <S extends Enum<S>> Builder<S, Integer> enumIntStateMachine(
                @NonNull Class<S> stateClass,
                int lowerInputBoundInclusive,
                int upperInputBoundInclusive) {

            return new EnumIntStateMachine.BuilderImpl<>(
                    stateClass,
                    lowerInputBoundInclusive,
                    upperInputBoundInclusive);
        }

        @NonNull
        public static <I extends Enum<I>> Builder<Integer, I> enumIntStateMachine(
                int lowerStateBoundInclusive,
                int upperStateBoundInclusive,
                @NonNull Class<I> inputClass) {

            return new IntEnumStateMachine.BuilderImpl<>(
                    lowerStateBoundInclusive,
                    upperStateBoundInclusive,
                    inputClass);
        }
    }

    record FSMBacklogPair<S, I>(@NonNull S state, @NonNull I input) {
        public FSMBacklogPair {
            Preconditions.checkNotNull(state);
            Preconditions.checkNotNull(input);
        }
    }
}
