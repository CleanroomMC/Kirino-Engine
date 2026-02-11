package com.cleanroommc.kirino.engine.render.platform.scene.fsm;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class MeshletGpuPipelineFSM {

    public enum State {
        INITIAL_WAIT,
        PREPARE_MESHLET_INPUT,
        COMPUTE_DISPATCH,
        IDLE, // if there's no meshlet changes after compute
    }

    private boolean isPullResultReady = false;
    private final FiniteStateMachine<MeshletGpuPipelineFSM.State, Integer> fsm;

    public MeshletGpuPipelineFSM() {
        fsm = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 0, 0)
                .initialState(State.INITIAL_WAIT)
                .addTransition(State.INITIAL_WAIT, 0, State.PREPARE_MESHLET_INPUT)
                .error((state, input) -> {
                    throw new RuntimeException(String.format(
                            "An error occurred inside MeshletGpuPipelineFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
                })
                .validate()
                .build();
    }

    public State getState() {
        return fsm.state();
    }

    private int initialWaitFrameCounter = 0;

    public void next() {
        if (fsm.state() == State.INITIAL_WAIT) {
            if (initialWaitFrameCounter++ >= KirinoCommonCore.KIRINO_CONFIG_HUB.getMeshletInitFrames()) {
                initialWaitFrameCounter = 0; // pass
            } else {
                return;
            }
        }

        // first time of finishing compute; results are ready to be drawn
        if (!isPullResultReady && fsm.state() == State.COMPUTE_DISPATCH) {
            isPullResultReady = true;
        }

        fsm.accept(0);
    }

    public void reset() {
        isPullResultReady = false;
        // next state is INITIAL_WAIT; reset counter
        initialWaitFrameCounter = 0;
        fsm.reset();
    }
}
