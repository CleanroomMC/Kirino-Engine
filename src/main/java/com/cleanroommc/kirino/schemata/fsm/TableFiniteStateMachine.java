package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.kirino.schemata.immutable.KirinoImmutableMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

final class TableFiniteStateMachine<S, I> implements FiniteStateMachine<S, I> {

    private final Table<I, S, S> transitionMap;
    private final Map<S, OnEnterStateCallback<S, I>> entryCallbacks;
    private final Map<S, OnExitStateCallback<S, I>> exitCallbacks;
    private final Table<I, S, Rollback<S, I>> rollbackTable;
    private final ErrorCallback<S, I> errorCallback;
    private S state;
    private final Deque<FSMBacklogPair<S, I>> stack = new ArrayDeque<>();

    TableFiniteStateMachine(@NonNull Table<I, S, S> transitionMap,
                            @NonNull Map<S, OnEnterStateCallback<S, I>> entryCallbacks,
                            @NonNull Map<S, OnExitStateCallback<S, I>> exitCallbacks,
                            @NonNull Table<I, S, Rollback<S, I>> rollbackTable,
                            @Nullable ErrorCallback<S, I> errorCallback,
                            @NonNull S initialState) {
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbackTable = rollbackTable;
        this.errorCallback = errorCallback;
        state = initialState;
    }

    @NonNull
    @Override
    public Optional<S> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Provided \"input\" can't be null.");
        Preconditions.checkArgument(transitionMap.containsRow(input), "%s is not a valid input.", input);

        if (!transitionMap.contains(input, state)) {
            if (errorCallback != null) {
                errorCallback.error(state, input);
            }
            return Optional.empty();
        }

        S newState = transitionMap.get(input, state);
        if (newState != null) {
            stack.push(new FSMBacklogPair<S, I>(state, input));
            if (exitCallbacks.containsKey(state)) {
                exitCallbacks.get(state).transition(state, input, newState);
            }
            if (entryCallbacks.containsKey(newState)) {
                entryCallbacks.get(newState).transition(state, input, newState);
            }
            state = newState;
        } else if (errorCallback != null) {
            errorCallback.error(state, input);
            return Optional.empty();
        }

        return Optional.of(state);
    }

    @NonNull
    @Override
    public S state() {
        return state;
    }

    @NonNull
    @Override
    public Optional<FSMBacklogPair<S, I>> backtrack() {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        FSMBacklogPair<S, I> entry = stack.pop();
        Rollback<S, I> rollback = rollbackTable.get(entry.input(), entry.state());
        if (rollback != null) {
            rollback.rollback(state, entry.input(), entry.state());
        }

        FSMBacklogPair<S, I> result = new FSMBacklogPair<>(state, entry.input());
        state = entry.state();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!stack.isEmpty()) {
            state = stack.pollLast().state();
            stack.clear();
        }
    }

    static class BuilderImpl<S, I> implements Builder<S, I> {
        private final Class<S> stateClass;
        private final Class<OnEnterStateCallback<S, I>> entryCallbackClass;
        private final Class<OnExitStateCallback<S, I>> exitCallbackClass;
        private final ImmutableTable.Builder<I, S, S> builder = ImmutableTable.builder();
        private KirinoImmutableMap.Builder<S, OnEnterStateCallback<S, I>> entryCallbackMapBuilder = null;
        private KirinoImmutableMap.Builder<S, OnExitStateCallback<S, I>> exitCallbackMapBuilder = null;
        private final ImmutableTable.Builder<I, S, Rollback<S, I>> rollbackTable = ImmutableTable.builder();
        private S initialState = null;
        private ErrorCallback<S, I> error = null;

        BuilderImpl(Class<S> stateClass, @Nullable Class<OnEnterStateCallback<S, I>> entryCallbackClass, @Nullable Class<OnExitStateCallback<S, I>> exitCallbackClass) {
            if (entryCallbackClass != null) {
                this.entryCallbackMapBuilder = new KirinoImmutableMap.Builder<>(stateClass, entryCallbackClass);
            }
            if (exitCallbackClass != null) {
                this.exitCallbackMapBuilder = new KirinoImmutableMap.Builder<>(stateClass, exitCallbackClass);
            }

            this.stateClass = stateClass;
            this.entryCallbackClass = entryCallbackClass;
            this.exitCallbackClass = exitCallbackClass;
        }

        @NonNull
        @Override
        public Builder<S, I> addTransition(@NonNull S currentState, @NonNull I input, @NonNull S nextState,
                                           @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                                           @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                                           FiniteStateMachine.@Nullable Rollback<S, I> rollback) {
            Preconditions.checkNotNull(currentState, "Parameter \"currentState\" can't be null.");
            Preconditions.checkNotNull(input, "Parameter \"input\" can't be null.");
            Preconditions.checkNotNull(nextState, "Parameter \"nextState\" can't be null.");

            builder.put(input, currentState, nextState);
            if (onEnterStateCallback != null && entryCallbackMapBuilder != null) {
                try {
                    entryCallbackMapBuilder.insert(nextState, onEnterStateCallback);
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (onExitStateCallback != null && exitCallbackMapBuilder != null) {
                try {
                    exitCallbackMapBuilder.insert(nextState, onExitStateCallback);
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (rollback != null) {
                rollbackTable.put(input, currentState, rollback);
            }

            return this;
        }

        /**
         * Sets the entry callback for a state. It's not recommended to delete a callback by passing null because
         * it violates the immutable pattern and is performance heavy.
         *
         * @param state    The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         *                 {@link Builder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @return The builder
         */
        @NonNull
        @Override
        public Builder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Provided \"state\" can't be null.");

            if (entryCallbackMapBuilder == null) {
                return this;
            }

            if (callback == null) {
                KirinoImmutableMap.Builder<S, OnEnterStateCallback<S, I>> newBuilder = new KirinoImmutableMap.Builder<>(stateClass, entryCallbackClass);
                KirinoImmutableMap<S, OnEnterStateCallback<S, I>> map = entryCallbackMapBuilder.build();
                for (Map.Entry<S, OnEnterStateCallback<S, I>> entry : map.entrySet()) {
                    if (entry.getKey() != state) {
                        newBuilder.insert(entry.getKey(), entry.getValue());
                    }
                }
                entryCallbackMapBuilder = newBuilder;
            } else {
                entryCallbackMapBuilder.insert(state, callback);
            }
            return this;
        }

        /**
         * Sets the exit callback for a state. It's not recommended to delete a callback by passing null because
         * it violates the immutable pattern and is performance heavy.
         *
         * @param state    The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         *                 {@link Builder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @return The builder
         */
        @NonNull
        @Override
        public Builder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Provided \"state\" can't be null.");

            if (exitCallbackMapBuilder == null) {
                return this;
            }

            if (callback == null) {
                KirinoImmutableMap.Builder<S, OnExitStateCallback<S, I>> newBuilder = new KirinoImmutableMap.Builder(stateClass, exitCallbackClass);
                KirinoImmutableMap<S, OnExitStateCallback<S, I>> map = exitCallbackMapBuilder.build();
                for (Map.Entry<S, OnExitStateCallback<S, I>> entry : map.entrySet()) {
                    if (entry.getKey() != state) {
                        newBuilder.insert(entry.getKey(), entry.getValue());
                    }
                }
                exitCallbackMapBuilder = newBuilder;
            } else {
                exitCallbackMapBuilder.insert(state, callback);
            }
            return this;
        }

        @NonNull
        @Override
        public Builder<S, I> initialState(@NonNull S initialState) {
            Preconditions.checkNotNull(initialState, "Provided \"initialState\" can't be null.");

            this.initialState = initialState;
            return this;
        }

        @NonNull
        @Override
        public Builder<S, I> error(@NonNull ErrorCallback<S, I> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Provided \"errorCallback\" can't be null, if you don't want to use a failure callback don't call this function.");

            error = errorCallback;
            return this;
        }

        @NonNull
        @Override
        public Builder<S, I> validate() {
            Table<I, S, S> table = builder.build();
            Set<S> reachable = new HashSet<>();
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);
            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (!reachable.contains(state)) {
                    reachable.add(state);
                    for (I input : table.rowKeySet()) {
                        if (table.contains(input, state)) {
                            stack.push(table.get(input, state));
                        }
                    }
                }
            }

            Preconditions.checkState(reachable.size() == table.columnKeySet().size(),
                    "Some state not reachable.");

            return this;
        }

        @NonNull
        @Override
        public FiniteStateMachine<S, I> build() {
            Preconditions.checkNotNull(initialState, "The Initial State must be set before the FSM is built.");

            return new TableFiniteStateMachine<>(builder.buildOrThrow(),
                    entryCallbackMapBuilder != null ? entryCallbackMapBuilder.build() : Map.of(),
                    exitCallbackMapBuilder != null ? exitCallbackMapBuilder.build() : Map.of(),
                    rollbackTable.buildOrThrow(), error, initialState);
        }
    }
}
