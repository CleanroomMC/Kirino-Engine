package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class TableFiniteStateMachine<S, I> implements FiniteStateMachine<S, I> {

    private final Table<I, S, S> transitionMap;
    private final Map<S, OnEnterStateCallback<S, I>> entryCallbacks;
    private final Map<S, OnExitStateCallback<S, I>> exitCallbacks;
    private final Table<I, S, Rollback<S, I>> rollbackTable;
    private final ErrorCallback<S, I> errorCallback;
    private S state;
    private boolean processing;
    private final Deque<FSMBacklogPair<S, I>> stack = new ArrayDeque<>();

    TableFiniteStateMachine(
            @NonNull Table<I, S, S> transitionMap,
            @NonNull Map<S, OnEnterStateCallback<S, I>> entryCallbacks,
            @NonNull Map<S, OnExitStateCallback<S, I>> exitCallbacks,
            @NonNull Table<I, S, Rollback<S, I>> rollbackTable,
            @Nullable ErrorCallback<S, I> errorCallback,
            @NonNull S initialState) {

        Preconditions.checkNotNull(transitionMap);
        Preconditions.checkNotNull(entryCallbacks);
        Preconditions.checkNotNull(exitCallbacks);
        Preconditions.checkNotNull(rollbackTable);
        Preconditions.checkNotNull(initialState);

        this.transitionMap = ImmutableTable.copyOf(transitionMap);
        this.entryCallbacks = ImmutableMap.copyOf(entryCallbacks);
        this.exitCallbacks = ImmutableMap.copyOf(exitCallbacks);
        this.rollbackTable = ImmutableTable.copyOf(rollbackTable);
        this.errorCallback = errorCallback;
        state = initialState;
    }

    @NonNull
    @Override
    public Optional<S> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
        Preconditions.checkArgument(transitionMap.containsRow(input),
                "Argument \"input\"=%s must be a configured input.", input);
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        processing = true;

        try {
            S newState = transitionMap.get(input, state);
            if (newState == null) {
                if (errorCallback != null) {
                    errorCallback.error(state, input);
                }

                return Optional.empty();
            }

            if (exitCallbacks.containsKey(state)) {
                exitCallbacks.get(state).transition(state, input, newState);
            }
            if (entryCallbacks.containsKey(newState)) {
                entryCallbacks.get(newState).transition(state, input, newState);
            }

            stack.push(new FSMBacklogPair<>(state, input));
            state = newState;

            return Optional.of(state);
        } finally {
            processing = false;
        }
    }

    @NonNull
    @Override
    public S state() {
        return state;
    }

    @NonNull
    @Override
    public Optional<FSMBacklogPair<S, I>> backtrack() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (stack.isEmpty()) {
            return Optional.empty();
        }

        processing = true;

        try {
            FSMBacklogPair<S, I> entry = stack.peek();
            Rollback<S, I> rollback = rollbackTable.get(entry.input(), entry.state());
            if (rollback != null) {
                rollback.rollback(state, entry.input(), entry.state());
            }

            FSMBacklogPair<S, I> result = new FSMBacklogPair<>(state, entry.input());
            stack.pop();
            state = entry.state();

            return Optional.of(result);
        } finally {
            processing = false;
        }
    }

    @Override
    public void reset() {
        Preconditions.checkState(!processing, "FSM callbacks must not modify the same FSM.");

        if (!stack.isEmpty()) {
            state = stack.pollLast().state();
            stack.clear();
        }
    }

    static class BuilderImpl<S, I> implements Builder<S, I> {

        private final Table<I, S, S> transitionMap = HashBasedTable.create();
        private final Map<S, OnEnterStateCallback<S, I>> entryCallbacks = new HashMap<>();
        private final Map<S, OnExitStateCallback<S, I>> exitCallbacks = new HashMap<>();
        private final Table<I, S, Rollback<S, I>> rollbackTable = HashBasedTable.create();
        private S initialState = null;
        private ErrorCallback<S, I> error = null;

        BuilderImpl() {
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> addTransition(
                @NonNull S currentState,
                @NonNull I input,
                @NonNull S nextState,
                @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                @Nullable Rollback<S, I> rollback) {

            Preconditions.checkNotNull(currentState, "Argument \"currentState\" must not be null.");
            Preconditions.checkNotNull(input, "Argument \"input\" must not be null.");
            Preconditions.checkNotNull(nextState, "Argument \"nextState\" must not be null.");

            transitionMap.put(input, currentState, nextState);
            if (onEnterStateCallback != null) {
                entryCallbacks.put(nextState, onEnterStateCallback);
            }
            if (onExitStateCallback != null) {
                exitCallbacks.put(currentState, onExitStateCallback);
            }
            if (rollback == null) {
                rollbackTable.remove(input, currentState);
            } else {
                rollbackTable.put(input, currentState, rollback);
            }

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            if (callback == null) {
                entryCallbacks.remove(state);
            } else {
                entryCallbacks.put(state, callback);
            }

            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        @Override
        public Builder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Argument \"state\" must not be null.");

            if (callback == null) {
                exitCallbacks.remove(state);
            } else {
                exitCallbacks.put(state, callback);
            }

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

            Set<S> allStates = new HashSet<>();
            allStates.add(initialState);
            allStates.addAll(transitionMap.columnKeySet());
            allStates.addAll(transitionMap.values());
            allStates.addAll(entryCallbacks.keySet());
            allStates.addAll(exitCallbacks.keySet());

            Set<S> reachable = new HashSet<>();
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);

            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (reachable.add(state)) {
                    for (S nextState : transitionMap.column(state).values()) {
                        stack.push(nextState);
                    }
                }
            }

            Preconditions.checkState(reachable.containsAll(allStates),
                    "Some states are not reachable from the initial state.");

            return this;
        }

        @NonNull
        @Override
        public FiniteStateMachine<S, I> build() {
            Preconditions.checkState(initialState != null, "The initial state must be set before the FSM is built.");

            return new TableFiniteStateMachine<>(
                    transitionMap,
                    entryCallbacks,
                    exitCallbacks,
                    rollbackTable,
                    error,
                    initialState);
        }
    }
}
