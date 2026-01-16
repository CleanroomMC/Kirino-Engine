package com.cleanroommc.kirino.engine.render.platform.scene.fsm;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class MeshletGpuPipelineFSM {

    public enum State {
        INITIAL_WAIT,
        PREPARE_FIRST_MESHLET_INPUT
    }

    private final FiniteStateMachine<MeshletGpuPipelineFSM.State, Integer> fsm;

    public MeshletGpuPipelineFSM() {
        fsm = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 0, 0)
                .initialState(State.INITIAL_WAIT)
                .addTransition(State.INITIAL_WAIT, 0, State.PREPARE_FIRST_MESHLET_INPUT)
                .error((state, input) -> {
                    throw new RuntimeException(String.format(
                            "An error occurred inside MeshletComputeFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
                })
                .validate()
                .build();
    }

    public State getState() {
        return fsm.state();
    }
}
