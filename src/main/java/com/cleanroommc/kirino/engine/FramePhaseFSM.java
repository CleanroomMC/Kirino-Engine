package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class FramePhaseFSM {

    private final FiniteStateMachine<FramePhase, Integer> fsm;

    FramePhaseFSM() {
        fsm = FiniteStateMachine.BuilderImpl.enumIntStateMachine(FramePhase.class, 0, 0)
                .initialState(FramePhase.PREPARE)
                .addTransition(FramePhase.PREPARE, 0, FramePhase.PRE_UPDATE)
                .addTransition(FramePhase.PRE_UPDATE, 0, FramePhase.UPDATE)
                .addTransition(FramePhase.UPDATE, 0, FramePhase.RENDER_OPAQUE)
                .addTransition(FramePhase.RENDER_OPAQUE, 0, FramePhase.RENDER_TRANSPARENT)
                .addTransition(FramePhase.RENDER_TRANSPARENT, 0, FramePhase.POST_UPDATE)
                .addTransition(FramePhase.POST_UPDATE, 0, FramePhase.RENDER_OVERLAY)
                .error((state, input) -> {
                    throw new RuntimeException(String.format(
                            "An error occurred inside FramePhaseFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
                })
                .validate()
                .build();
    }

    public FramePhase getState() {
        return fsm.state();
    }

    public void next() {
        if (fsm.state() == FramePhase.RENDER_OVERLAY) {
            fsm.reset(); // clear backlog
        } else {
            fsm.accept(0);
        }
    }
}
