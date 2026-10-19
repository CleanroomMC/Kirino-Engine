package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class IntEnumStateMachine<I extends Enum<I>> implements FiniteStateMachine<Integer, I> {

    private final int lowerStateBound, upperStateBound;
    private final int[] transitionMap;
    private final OnEnterStateCallback<Integer, I>[] entryCallbacks;
    private final OnExitStateCallback<Integer, I>[] exitCallbacks;
    private final Rollback<Integer, I>[] rollbacks;
    private final ErrorCallback<Integer, I> error;
    private int state;
    private boolean processing;
    private final Deque<FSMBacklogPair<Integer, I>> backlog = new ArrayDeque<>();

    IntEnumStateMachine(
            int lowerStateBound,
            int upperStateBound,
            int @NonNull [] transitionMap,
            OnEnterStateCallback<Integer, I> @NonNull [] entryCallbacks,
            OnExitStateCallback<Integer, I> @NonNull [] exitCallbacks,
            Rollback<Integer, I> @NonNull [] rollbacks,
            @Nullable ErrorCallback<Integer, I> error,
            int initialState) {

        Preconditions.checkNotNull(transitionMap);
        Preconditions.checkNotNull(entryCallbacks);
        Preconditions.checkNotNull(exitCallbacks);
        Preconditions.checkNotNull(rollbacks);

        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.transitionMap = transitionMap.clone();
        this.entryCallbacks = entryCallbacks.clone();
        this.exitCallbacks = exitCallbacks.clone();
        this.rollbacks = rollbacks.clone();
        this.error = error;
        state = initialState;
    }

    @NonNull
    @Override
    public Integer state() {
        return state;
    }

    private int index(@NonNull I input, int state) {
        return ((upperStateBound - lowerStateBound + 1) * input.ordinal()) + (state - lowerStateBound);
    }

    @NonNull
    @Override
    public Optional<Integer> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        processing = true;

        try {
            int next = transitionMap[index(input, state)];
            if (next == -1) {
                if (error != null) {
                    error.error(state, input);
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
    public Optional<FSMBacklogPair<Integer, I>> backtrack() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (backlog.isEmpty()) {
            return Optional.empty();
        }

        processing = true;

        try {
            FSMBacklogPair<Integer, I> pair = backlog.peek();
            Rollback<Integer, I> rollback = rollbacks[index(pair.input(), pair.state())];
            if (rollback != null) {
                rollback.rollback(state, pair.input(), pair.state());
            }

            FSMBacklogPair<Integer, I> result = new FSMBacklogPair<>(state, pair.input());
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

    static class BuilderImpl<I extends Enum<I>> implements Builder<Integer, I> {

        private final int lowerStateBound, upperStateBound;
        private final I[] inputs;
        private final int[] transitionMap;
        private final OnEnterStateCallback<Integer, I>[] entryCallbacks;
        private final OnExitStateCallback<Integer, I>[] exitCallbacks;
        private final Rollback<Integer, I>[] rollbacks;
        private ErrorCallback<Integer, I> error;
        private Integer initialState;

        @SuppressWarnings("unchecked")
        BuilderImpl(int lowerStateBound, int upperStateBound, @NonNull Class<I> inputClass) {
            int stateCount = FSMPreconditions.rangeSize(lowerStateBound, upperStateBound);
            I[] inputs = FSMPreconditions.enumConstants(inputClass);
            int length = FSMPreconditions.transitionCount(stateCount, inputs.length);

            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.inputs = inputs;
            transitionMap = new int[length];

            for (int i = 0; i < length; i++) {
                transitionMap[i] = -1;
            }

            entryCallbacks = new OnEnterStateCallback[stateCount];
            exitCallbacks = new OnExitStateCallback[stateCount];
            rollbacks = new Rollback[length];
        }

        private int index(I input, int state) {
            return ((upperStateBound - lowerStateBound + 1) * input.ordinal()) + (state - lowerStateBound);
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, I> addTransition(
                @NonNull Integer state,
                @NonNull I input,
                @NonNull Integer nextState,
                @Nullable OnEnterStateCallback<Integer, I> onEnterStateCallback,
                @Nullable OnExitStateCallback<Integer, I> onExitStateCallback,
                @Nullable Rollback<Integer, I> rollbackCallback) {

            Preconditions.checkNotNull(state);
            Preconditions.checkNotNull(input);
            Preconditions.checkNotNull(nextState);
            Preconditions.checkArgument(state >= lowerStateBound && state <= upperStateBound,
                    "Argument \"state\"=%s must be in range [%s, %s].",
                    state, lowerStateBound, upperStateBound);
            Preconditions.checkArgument(nextState >= lowerStateBound && nextState <= upperStateBound,
                    "Argument \"nextState\"=%s must be in range [%s, %s].",
                    nextState, lowerStateBound, upperStateBound);

            int index = index(input, state);
            transitionMap[index] = nextState - lowerStateBound;
            if (onExitStateCallback != null) {
                exitCallbacks[state - lowerStateBound] = onExitStateCallback;
            }
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState - lowerStateBound] = onEnterStateCallback;
            }
            rollbacks[index] = rollbackCallback;

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, I> setEntryCallback(
                @NonNull Integer state,
                @Nullable OnEnterStateCallback<Integer, I> callback) {

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
        public Builder<Integer, I> setExitCallback(
                @NonNull Integer state,
                @Nullable OnExitStateCallback<Integer, I> callback) {

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
        public Builder<Integer, I> initialState(@NonNull Integer initialState) {
            Preconditions.checkNotNull(initialState, "Argument \"initialState\" must not be null.");
            Preconditions.checkArgument(initialState >= lowerStateBound && initialState <= upperStateBound,
                    "Argument \"state\"=%s must be in range [%s, %s].", initialState, lowerStateBound, upperStateBound);

            this.initialState = initialState;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, I> error(@NonNull ErrorCallback<Integer, I> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Argument \"errorCallback\" must not be null; omit error() to disable the failure callback.");

            error = errorCallback;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<Integer, I> validate() {
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
                    for (I input : inputs) {
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
        public FiniteStateMachine<Integer, I> build() {
            Preconditions.checkState(initialState != null,
                    "The initial state must be set before the FSM is built.");
            Preconditions.checkArgument(initialState >= lowerStateBound && initialState <= upperStateBound,
                    "Argument \"initialState\"=%s must be in range [%s, %s].",
                    initialState, lowerStateBound, upperStateBound);

            return new IntEnumStateMachine<>(
                    lowerStateBound,
                    upperStateBound,
                    transitionMap,
                    entryCallbacks,
                    exitCallbacks,
                    rollbacks,
                    error,
                    initialState);
        }
    }
}
