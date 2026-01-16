package com.cleanroommc.kirino.engine.render.platform.scene.fsm;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

public class TerrainCpuPipelineFSM {

    public enum State {
        IDLE,
        CHUNK_PRIORITIZATION_TASK,
        MESHLET_GEN_TASK,
        MESHLET_DESTROY_TASK
    }

    private final FiniteStateMachine<State, Integer> fsm;

    public TerrainCpuPipelineFSM() {
        fsm = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 0, 2)
                .initialState(State.IDLE)
                .addTransition(State.IDLE, 1, State.CHUNK_PRIORITIZATION_TASK)
                .addTransition(State.CHUNK_PRIORITIZATION_TASK, 0, State.MESHLET_GEN_TASK)
                .addTransition(State.MESHLET_GEN_TASK, 0, State.IDLE)
                .addTransition(State.IDLE, 2, State.MESHLET_DESTROY_TASK)
                .addTransition(State.MESHLET_DESTROY_TASK, 0, State.IDLE)
                .error((state, input) -> {
                    throw new RuntimeException(String.format(
                            "An error occurred inside TerrainFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
                })
                .validate()
                .build();
    }

    public State getState() {
        return fsm.state();
    }

    public void prioritizeChunks() {
        fsm.accept(1);
    }

    public void destroyMeshlets() {
        fsm.accept(2);
    }

    private int meshletGenCounter = 0;
    private int meshletGenTarget = 0;

    public void setMeshletGenCounter(int target) {
        meshletGenCounter = 0;
        meshletGenTarget = target;
    }

    public int getMeshletGenCounter() {
        return meshletGenCounter;
    }

    public void next() {
        if (fsm.state() == State.MESHLET_GEN_TASK) {
            if (++meshletGenCounter >= meshletGenTarget) {
                fsm.accept(0); // pass
            }
            return;
        }

        fsm.accept(0);

        if (fsm.state() == State.IDLE) {
            fsm.reset(); // clear backlog
        }
    }
}
