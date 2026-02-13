package com.cleanroommc.kirino.engine.render.platform.scene.fsm;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class MeshletGpuPipelineFSM {

    public enum State {
        INITIAL_WAIT,
        ARMED, // transitional state. last check before the computation
        COMPUTABLE, // when able to compute OR computing
        IDLE
    }

    private boolean isPullResultReady = false;
    private final FiniteStateMachine<MeshletGpuPipelineFSM.State, Integer> fsm;

    public MeshletGpuPipelineFSM() {
        fsm = FiniteStateMachine.BuilderImpl.enumIntStateMachine(State.class, 0, 0)
                .initialState(State.INITIAL_WAIT)
                .addTransition(State.INITIAL_WAIT, 0, State.IDLE)
                .addTransition(State.ARMED, 0, State.COMPUTABLE)
                .addTransition(State.COMPUTABLE, 0, State.IDLE)
                .addTransition(State.IDLE, 0, State.ARMED)
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
        if (!isPullResultReady && fsm.state() == State.COMPUTABLE) {
            isPullResultReady = true;
        }

        State oldState = fsm.state();

        fsm.accept(0);

        if (oldState != State.INITIAL_WAIT && fsm.state() == State.IDLE) {
            // clear backlog
            fsm.reset();
            fsm.accept(0);
        }
    }

    public void reset() {
        isPullResultReady = false;
        // next state is INITIAL_WAIT; reset counter
        initialWaitFrameCounter = 0;
        fsm.reset();
    }
}
