package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class EnumIntStateMachine<S extends Enum<S>> implements FiniteStateMachine<S, Integer> {

    private final int lowerInputBound, upperInputBound;
    private final S[] states;
    private final int[] transitionMap;
    private final OnEnterStateCallback<S, Integer>[] stateEnterCallbackMap;
    private final OnExitStateCallback<S, Integer>[] stateExitCallbackMap;
    private final Rollback<S, Integer>[] rollbacks;
    private final ErrorCallback<S, Integer> error;
    private int state;
    private boolean processing;
    private final Deque<FSMBacklogPair<S, Integer>> backlog = new ArrayDeque<>();

    EnumIntStateMachine(
            int lowerInputBound,
            int upperInputBound,
            @NonNull S[] states,
            int @NonNull [] transitionMap,
            OnEnterStateCallback<S, Integer> @NonNull [] stateEnterCallbackMap,
            OnExitStateCallback<S, Integer> @NonNull [] stateExitCallbackMap,
            Rollback<S, Integer> @NonNull [] rollbacks,
            @Nullable ErrorCallback<S, Integer> error,
            @NonNull S initialState) {

        Preconditions.checkNotNull(states);
        Preconditions.checkNotNull(transitionMap);
        Preconditions.checkNotNull(stateEnterCallbackMap);
        Preconditions.checkNotNull(stateExitCallbackMap);
        Preconditions.checkNotNull(rollbacks);
        Preconditions.checkNotNull(initialState);

        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.states = states;
        this.transitionMap = transitionMap.clone();
        this.stateEnterCallbackMap = stateEnterCallbackMap.clone();
        this.stateExitCallbackMap = stateExitCallbackMap.clone();
        this.rollbacks = rollbacks.clone();
        this.error = error;
        state = initialState.ordinal();
    }

    @NonNull
    @Override
    public S state() {
        return states[state];
    }

    private int index(int input, int state) {
        return (states.length * (input - lowerInputBound)) + state;
    }

    @NonNull
    @Override
    public Optional<S> accept(@NonNull Integer input) {
        Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
        Preconditions.checkArgument(!(input < lowerInputBound || input > upperInputBound),
                "Argument \"input\"=%s must be in range [%s, %s].", input, lowerInputBound, upperInputBound);
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

            if (stateExitCallbackMap[state] != null) {
                stateExitCallbackMap[state].transition(states[state], input, states[next]);
            }
            if (stateEnterCallbackMap[next] != null) {
                stateEnterCallbackMap[next].transition(states[state], input, states[next]);
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
    public Optional<FSMBacklogPair<S, Integer>> backtrack() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (backlog.isEmpty()) {
            return Optional.empty();
        }

        processing = true;

        try {
            FSMBacklogPair<S, Integer> pair = backlog.peek();
            Rollback<S, Integer> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
            if (rollback != null) {
                rollback.rollback(states[state], pair.input(), pair.state());
            }

            FSMBacklogPair<S, Integer> result = new FSMBacklogPair<>(states[state], pair.input());
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

    static class BuilderImpl<S extends Enum<S>> implements Builder<S, Integer> {

        private final int lowerInputBound, upperInputBound;
        private final int[] transitionMap;
        private final S[] states;
        private final OnEnterStateCallback<S, Integer>[] stateEnterCallbackMap;
        private final OnExitStateCallback<S, Integer>[] stateExitCallbackMap;
        private final Rollback<S, Integer>[] rollbacks;
        private ErrorCallback<S, Integer> error;
        private S initialState;

        @SuppressWarnings("unchecked")
        BuilderImpl(@NonNull Class<S> stateClass, int lowerInputBound, int upperInputBound) {
            int inputCount = FSMPreconditions.rangeSize(lowerInputBound, upperInputBound);
            S[] states = FSMPreconditions.enumConstants(stateClass);

            Preconditions.checkArgument(states.length > 0, "The state enum must not be empty.");

            int size = FSMPreconditions.transitionCount(states.length, inputCount);

            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            this.states = states;
            transitionMap = new int[size];

            for (int i = 0; i < size; i++) {
                transitionMap[i] = -1;
            }

            stateEnterCallbackMap = new OnEnterStateCallback[states.length];
            stateExitCallbackMap = new OnExitStateCallback[states.length];
            rollbacks = new Rollback[size];
        }

        private int index(int input, S state) {
            return (states.length * (input - lowerInputBound)) + state.ordinal();
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> addTransition(
                @NonNull S state,
                @NonNull Integer input,
                @NonNull S nextState,
                @Nullable OnEnterStateCallback<S, Integer> onEnterStateCallback,
                @Nullable OnExitStateCallback<S, Integer> onExitStateCallback,
                @Nullable Rollback<S, Integer> rollbackCallback) {

            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");
            Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
            Preconditions.checkNotNull(nextState, "Argument \"nextState\" must not be null.");
            Preconditions.checkArgument(!(input < lowerInputBound || input > upperInputBound),
                    "Argument \"input\"=%s must be in range [%s, %s].", input, lowerInputBound, upperInputBound);

            final int idx = index(input, state);
            transitionMap[idx] = nextState.ordinal();
            if (onEnterStateCallback != null) {
                stateEnterCallbackMap[nextState.ordinal()] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                stateExitCallbackMap[state.ordinal()] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> setEntryCallback(
                @NonNull S state,
                @Nullable OnEnterStateCallback<S, Integer> callback) {

            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            stateEnterCallbackMap[state.ordinal()] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> setExitCallback(
                @NonNull S state,
                @Nullable OnExitStateCallback<S, Integer> callback) {

            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            stateExitCallbackMap[state.ordinal()] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> initialState(@NonNull S initialState) {
            Preconditions.checkNotNull(initialState, "Argument \"initialState\" must not be null.");

            this.initialState = initialState;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> error(@NonNull ErrorCallback<S, Integer> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Argument \"errorCallback\" must not be null; omit error() to disable the failure callback.");

            error = errorCallback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, Integer> validate() {
            Preconditions.checkState(initialState != null,
                    "The initial state must be set before the FSM is validated.");

            BitSet reachable = new BitSet(states.length);
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);

            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (!reachable.get(state.ordinal())) {
                    reachable.set(state.ordinal());
                    int inputCount = transitionMap.length / states.length;
                    for (int inputIndex = 0; inputIndex < inputCount; inputIndex++) {
                        int input = lowerInputBound + inputIndex;
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
        public FiniteStateMachine<S, Integer> build() {
            Preconditions.checkState(initialState != null, "The initial state must be set before the FSM is built.");

            return new EnumIntStateMachine<>(
                    lowerInputBound,
                    upperInputBound,
                    states,
                    transitionMap,
                    stateEnterCallbackMap,
                    stateExitCallbackMap,
                    rollbacks,
                    error,
                    initialState);
        }
    }
}
