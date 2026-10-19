package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class IntRangeStateMachine implements FiniteStateMachine<Integer, Integer> {

    private final int lowerStateBound, upperStateBound;
    private final int lowerInputBound, upperInputBound;
    private final int[] transitionMap;
    private final OnEnterStateCallback<Integer, Integer>[] entryCallbacks;
    private final OnExitStateCallback<Integer, Integer>[] exitCallbacks;
    private final Rollback<Integer, Integer>[] rollbackCallbacks;
    private final ErrorCallback<Integer, Integer> errorCallback;
    private int state;
    private boolean processing;
    private final Deque<FSMBacklogPair<Integer, Integer>> backlog = new ArrayDeque<>();

    IntRangeStateMachine(
            int lowerStateBound,
            int upperStateBound,
            int lowerInputBound,
            int upperInputBound,
            int @NonNull [] transitionMap,
            OnEnterStateCallback<Integer, Integer> @NonNull [] entryCallbacks,
            OnExitStateCallback<Integer, Integer> @NonNull [] exitCallbacks,
            Rollback<Integer, Integer> @NonNull [] rollbackCallbacks,
            @Nullable ErrorCallback<Integer, Integer> errorCallback,
            int initialState) {

        Preconditions.checkNotNull(transitionMap);
        Preconditions.checkNotNull(entryCallbacks);
        Preconditions.checkNotNull(exitCallbacks);
        Preconditions.checkNotNull(rollbackCallbacks);

        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.transitionMap = transitionMap.clone();
        this.entryCallbacks = entryCallbacks.clone();
        this.exitCallbacks = exitCallbacks.clone();
        this.rollbackCallbacks = rollbackCallbacks.clone();
        this.errorCallback = errorCallback;
        state = initialState;
    }

    @NonNull
    @Override
    public Integer state() {
        return state;
    }

    private int index(int input, int state) {
        return ((input - lowerInputBound) * (upperStateBound - lowerStateBound + 1)) + (state - lowerStateBound);
    }

    @NonNull
    @Override
    public Optional<Integer> accept(@NonNull Integer input) {
        Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
        Preconditions.checkArgument(!(input < lowerInputBound || input > upperInputBound),
                "Argument \"input\"=%s must be in range [%s, %s].",
                input, lowerInputBound, upperInputBound);
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        processing = true;

        try {
            int next = transitionMap[index(input, state)];
            if (next == -1) {
                if (errorCallback != null) {
                    errorCallback.error(state, input);
                }

                return Optional.empty();
            }

            int nextState = next + lowerStateBound;
            if (exitCallbacks[state - lowerStateBound] != null) {
                exitCallbacks[state - lowerStateBound].transition(state, input, nextState);
            }
            if (entryCallbacks[next] != null) {
                entryCallbacks[next].transition(state, input, nextState);
            }

            backlog.push(new FSMBacklogPair<>(state, input));
            state = nextState;

            return Optional.of(state);
        } finally {
            processing = false;
        }
    }

    @NonNull
    @Override
    public Optional<FSMBacklogPair<Integer, Integer>> backtrack() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (backlog.isEmpty()) {
            return Optional.empty();
        }

        processing = true;

        try {
            FSMBacklogPair<Integer, Integer> pair = backlog.peek();
            Rollback<Integer, Integer> rollback = rollbackCallbacks[index(pair.input(), pair.state())];
            if (rollback != null) {
                rollback.rollback(state, pair.input(), pair.state());
            }

            FSMBacklogPair<Integer, Integer> result = new FSMBacklogPair<>(state, pair.input());
            backlog.pop();
            state = pair.state();

            return Optional.of(result);
        } finally {
            processing = false;
        }
    }

    @Override
    public void reset() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (!backlog.isEmpty()) {
            state = backlog.pollLast().state();
            backlog.clear();
        }
    }

    static class BuilderImpl implements Builder<Integer, Integer> {

        private final int lowerStateBound, upperStateBound;
        private final int lowerInputBound, upperInputBound;
        private final int[] transitionMap;
        private final OnEnterStateCallback<Integer, Integer>[] entryCallbacks;
        private final OnExitStateCallback<Integer, Integer>[] exitCallbacks;
        private final Rollback<Integer, Integer>[] rollbacks;
        private ErrorCallback<Integer, Integer> error;
        private Integer initialState;

        @SuppressWarnings("unchecked")
        BuilderImpl(int lowerStateBound, int upperStateBound, int lowerInputBound, int upperInputBound) {
            int stateCount = FSMPreconditions.rangeSize(lowerStateBound, upperStateBound);
            int inputCount = FSMPreconditions.rangeSize(lowerInputBound, upperInputBound);
            int size = FSMPreconditions.transitionCount(stateCount, inputCount);

            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            transitionMap = new int[size];

            for (int i = 0; i < size; i++) {
                transitionMap[i] = -1;
            }

            entryCallbacks = new OnEnterStateCallback[stateCount];
            exitCallbacks = new OnExitStateCallback[stateCount];
            rollbacks = new Rollback[size];
        }

        private int index(int input, int state) {
            return ((input - lowerInputBound) * (upperStateBound - lowerStateBound + 1)) + (state - lowerStateBound);
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> addTransition(
                @NonNull Integer state,
                @NonNull Integer input,
                @NonNull Integer nextState,
                @Nullable OnEnterStateCallback<Integer, Integer> onEnterStateCallback,
                @Nullable OnExitStateCallback<Integer, Integer> onExitStateCallback,
                @Nullable Rollback<Integer, Integer> rollbackCallback) {

            Preconditions.checkNotNull(state);
            Preconditions.checkNotNull(input);
            Preconditions.checkNotNull(nextState);
            Preconditions.checkArgument(state >= lowerStateBound && state <= upperStateBound,
                    "Argument \"state\"=%s must be in range [%s, %s].",
                    state, lowerStateBound, upperStateBound);
            Preconditions.checkArgument(nextState >= lowerStateBound && nextState <= upperStateBound,
                    "Argument \"nextState\"=%s must be in range [%s, %s].",
                    nextState, lowerStateBound, upperStateBound);
            Preconditions.checkArgument(input >= lowerInputBound && input <= upperInputBound,
                    "Argument \"input\"=%s must be in range [%s, %s].",
                    input, lowerInputBound, upperInputBound);

            int idx = index(input, state);
            transitionMap[idx] = nextState - lowerStateBound;
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState - lowerStateBound] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                exitCallbacks[state - lowerStateBound] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> setEntryCallback(
                @NonNull Integer state,
                @Nullable OnEnterStateCallback<Integer, Integer> callback) {

            Preconditions.checkNotNull(state);
            Preconditions.checkArgument(!(state < lowerStateBound || state > upperStateBound),
                    "Argument \"state\"=%s must be in range [%s, %s].",
                    state, lowerStateBound, upperStateBound);

            entryCallbacks[state - lowerStateBound] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> setExitCallback(
                @NonNull Integer state,
                @Nullable OnExitStateCallback<Integer, Integer> callback) {

            Preconditions.checkNotNull(state);
            Preconditions.checkArgument(!(state < lowerStateBound || state > upperStateBound),
                    "Argument \"state\"=%s must be in range [%s, %s].",
                    state, lowerStateBound, upperStateBound);

            exitCallbacks[state - lowerStateBound] = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> initialState(@NonNull Integer initialState) {
            Preconditions.checkNotNull(initialState);
            Preconditions.checkArgument(!(initialState < lowerStateBound || initialState > upperStateBound),
                    "Argument \"state\"=%s must be in range [%s, %s].",
                    initialState, lowerStateBound, upperStateBound);

            this.initialState = initialState;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> error(@NonNull ErrorCallback<Integer, Integer> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Argument \"errorCallback\" must not be null; omit error() to disable the failure callback.");

            error = errorCallback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, Integer> validate() {
            Preconditions.checkState(initialState != null,
                    "The initial state must be set before the FSM is validated.");
            Preconditions.checkArgument(initialState >= lowerStateBound && initialState <= upperStateBound,
                    "Argument \"initialState\"=%s must be in range [%s, %s].",
                    initialState, lowerStateBound, upperStateBound);

            final int size = upperStateBound - lowerStateBound + 1;
            BitSet reachable = new BitSet(size);
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(initialState);

            while (!stack.isEmpty()) {
                int state = stack.pop();
                int stateIndex = state - lowerStateBound;
                if (!reachable.get(stateIndex)) {
                    reachable.set(stateIndex);
                    int inputCount = transitionMap.length / size;
                    for (int inputIndex = 0; inputIndex < inputCount; inputIndex++) {
                        int input = lowerInputBound + inputIndex;
                        int next = transitionMap[index(input, state)];
                        if (next != -1) {
                            int nextState = next + lowerStateBound;
                            if (nextState != state) {
                                stack.push(nextState);
                            }
                        }
                    }
                }
            }

            Preconditions.checkState(reachable.cardinality() == size,
                    "Some states are not reachable from the initial state.");

            return this;
        }

        @NonNull
        @Override
        public FiniteStateMachine<Integer, Integer> build() {
            Preconditions.checkState(initialState != null,
                    "The initial state must be set before the FSM is built.");
            Preconditions.checkArgument(initialState >= lowerStateBound && initialState <= upperStateBound,
                    "Argument \"initialState\"=%s must be in range [%s, %s].",
                    initialState, lowerStateBound, upperStateBound);

            return new IntRangeStateMachine(
                    lowerStateBound,
                    upperStateBound,
                    lowerInputBound,
                    upperInputBound,
                    transitionMap,
                    entryCallbacks,
                    exitCallbacks,
                    rollbacks,
                    error,
                    initialState);
        }
    }
}
