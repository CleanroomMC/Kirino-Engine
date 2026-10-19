package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class EnumStateMachine<S extends Enum<S>, I extends Enum<I>> implements FiniteStateMachine<S, I> {

    private final S[] states;
    private final int[] transitionMap;
    private final OnEnterStateCallback<S, I>[] entryCallbacks;
    private final OnExitStateCallback<S, I>[] exitCallbacks;
    private final Rollback<S, I>[] rollbacks;
    private final ErrorCallback<S, I> error;
    private int state;
    private boolean processing;
    private final Deque<FSMBacklogPair<S, I>> backlog = new ArrayDeque<>();

    EnumStateMachine(
            @NonNull S initialState,
            @NonNull S[] states,
            int @NonNull [] transitionMap,
            OnEnterStateCallback<S, I> @NonNull [] entryCallbacks,
            OnExitStateCallback<S, I> @NonNull [] exitCallbacks,
            Rollback<S, I> @NonNull [] rollbacks,
            @Nullable ErrorCallback<S, I> error) {

        Preconditions.checkNotNull(initialState);
        Preconditions.checkNotNull(states);
        Preconditions.checkNotNull(transitionMap);
        Preconditions.checkNotNull(entryCallbacks);
        Preconditions.checkNotNull(exitCallbacks);
        Preconditions.checkNotNull(rollbacks);

        this.transitionMap = transitionMap.clone();
        this.entryCallbacks = entryCallbacks.clone();
        this.exitCallbacks = exitCallbacks.clone();
        this.rollbacks = rollbacks.clone();
        this.error = error;
        this.states = states;
        state = initialState.ordinal();
    }

    @NonNull
    @Override
    public S state() {
        return states[state];
    }

    private int index(I input, int state) {
        return (states.length * input.ordinal()) + state;
    }

    @NonNull
    @Override
    public Optional<S> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        processing = true;

        try {
            int next = transitionMap[index(input, state)];
            if (next == -1) {
                if (error != null) {
                    error.error(states[state], input);
                }

                return Optional.empty();
            }

            if (exitCallbacks[state] != null) {
                exitCallbacks[state].transition(states[state], input, states[next]);
            }
            if (entryCallbacks[next] != null) {
                entryCallbacks[next].transition(states[state], input, states[next]);
            }

            backlog.push(new FSMBacklogPair<>(states[state], input));
            state = next;

            return Optional.of(states[state]);
        } finally {
            processing = false;
        }
    }

    @NonNull
    @Override
    public Optional<FSMBacklogPair<S, I>> backtrack() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (backlog.isEmpty()) {
            return Optional.empty();
        }

        processing = true;

        try {
            FSMBacklogPair<S, I> pair = backlog.peek();
            Rollback<S, I> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
            if (rollback != null) {
                rollback.rollback(states[state], pair.input(), pair.state());
            }

            FSMBacklogPair<S, I> result = new FSMBacklogPair<>(states[state], pair.input());
            backlog.pop();
            state = pair.state().ordinal();

            return Optional.of(result);
        } finally {
            processing = false;
        }
    }

    @Override
    public void reset() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (!backlog.isEmpty()) {
            state = backlog.pollLast().state().ordinal();
            backlog.clear();
        }
    }

    static class BuilderImpl<S extends Enum<S>, I extends Enum<I>> implements Builder<S, I> {

        private final S[] states;
        private final I[] inputs; // only for validation
        private final int[] transitionMap;
        private final OnEnterStateCallback<S, I>[] entryCallbacks;
        private final OnExitStateCallback<S, I>[] exitCallbacks;
        private final Rollback<S, I>[] rollbacks;
        private ErrorCallback<S, I> error;
        private S initialState;

        @SuppressWarnings("unchecked")
        BuilderImpl(@NonNull Class<S> stateClass, @NonNull Class<I> inputClass) {
            S[] states = FSMPreconditions.enumConstants(stateClass);
            I[] inputs = FSMPreconditions.enumConstants(inputClass);

            Preconditions.checkArgument(states.length > 0, "The state enum must not be empty.");

            int length = FSMPreconditions.transitionCount(states.length, inputs.length);

            this.states = states;
            this.inputs = inputs;
            transitionMap = new int[length];

            for (int i = 0; i < length; i++) {
                transitionMap[i] = -1;
            }

            entryCallbacks = new OnEnterStateCallback[states.length];
            exitCallbacks = new OnExitStateCallback[states.length];
            rollbacks = new Rollback[length];
        }

        private int index(I input, S state) {
            return (states.length * input.ordinal()) + state.ordinal();
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> addTransition(
                @NonNull S state,
                @NonNull I input,
                @NonNull S nextState,
                @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                @Nullable Rollback<S, I> rollbackCallback) {

            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");
            Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
            Preconditions.checkNotNull(nextState, "Argument \"nextState\" must not be null.");

            int idx = index(input, state);
            transitionMap[idx] = nextState.ordinal();
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState.ordinal()] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                exitCallbacks[state.ordinal()] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            entryCallbacks[state.ordinal()] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            exitCallbacks[state.ordinal()] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> initialState(@NonNull S initialState) {
            Preconditions.checkNotNull(initialState, "Argument \"initialState\" must not be null.");

            this.initialState = initialState;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> error(@NonNull ErrorCallback<S, I> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Argument \"errorCallback\" must not be null; omit error() to disable the failure callback.");

            error = errorCallback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> validate() {
            Preconditions.checkState(initialState != null,
                    "The initial state must be set before the FSM is validated.");

            BitSet reachable = new BitSet(states.length);
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);

            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (!reachable.get(state.ordinal())) {
                    reachable.set(state.ordinal());
                    for (I input : inputs) {
                        int next = transitionMap[index(input, state)];
                        if (!(next == -1 || next == state.ordinal())) {
                            stack.push(states[next]);
                        }
                    }
                }
            }

            Preconditions.checkState(reachable.cardinality() == states.length,
                    "Some states are not reachable from the initial state.");

            return this;
        }

        @NonNull
        @Override
        public FiniteStateMachine<S, I> build() {
            Preconditions.checkState(initialState != null, "The initial state must be set before the FSM is built.");

            return new EnumStateMachine<>(
                    initialState,
                    states,
                    transitionMap,
                    entryCallbacks,
                    exitCallbacks,
                    rollbacks,
                    error);
        }
    }
}
