package com.cleanroommc.kirino.engine.render.platform.scene.fsm;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class WorldControlFSM {

    public enum State {
        NO_WORLD,
        NEW_WORLD_REBUILD,
        NEW_WORLD_INITIAL_WAIT,
        IDLE
    }

    private final FiniteStateMachine<State, Integer> fsm;

    public WorldControlFSM() {
        fsm = FiniteStateMachine.Builder.enumIntStateMachine(WorldControlFSM.State.class, 0, 0)
                .initialState(State.NO_WORLD)
                .addTransition(State.NO_WORLD, 0, State.NEW_WORLD_REBUILD)
                .addTransition(State.NEW_WORLD_REBUILD, 0, State.NEW_WORLD_INITIAL_WAIT)
                .addTransition(State.NEW_WORLD_INITIAL_WAIT, 0, State.IDLE)
                .error((state, input) -> {
                    throw new RuntimeException(String.format(
                            "An error occurred inside WorldControlFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
                })
                .validate()
                .build();
    }

    public State getState() {
        return fsm.state();
    }

    private int newWorldFrameCounter = 0;

    public void next() {
        if (fsm.state() == State.NEW_WORLD_INITIAL_WAIT) {
            if (newWorldFrameCounter++ >= KirinoCore.KIRINO_CONFIG_HUB.worldInitFrames) {
                newWorldFrameCounter = 0; // pass
            } else {
                return;
            }
        }

        if (fsm.state() == State.NEW_WORLD_REBUILD) {
            // next state is NEW_WORLD_INITIAL_WAIT; reset counter
            newWorldFrameCounter = 0;
        }

        fsm.accept(0);
    }

    public void reset() {
        fsm.reset();
    }
}
