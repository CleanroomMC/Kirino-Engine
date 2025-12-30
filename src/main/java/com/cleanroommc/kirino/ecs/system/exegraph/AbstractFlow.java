package com.cleanroommc.kirino.ecs.system.exegraph;

import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractFlow implements ISystemExeFlowGraph {

    public static final class BarrierNode {
        final String id;
        final List<Transition> outgoing = new ArrayList<>();
        final List<Transition> incoming = new ArrayList<>();

        @Nullable Runnable callback;
        int inDegree = 0;

        BarrierNode(String id, @Nullable Runnable callback) {
            this.id = id;
            this.callback = callback;
        }
    }

    public static final class Transition {
        final @Nullable CleanSystem system; // null = dummy
        final BarrierNode from;
        final BarrierNode to;

        Transition(@Nullable CleanSystem system, @NonNull BarrierNode from, @NonNull BarrierNode to) {
            this.system = system;
            this.from = from;
            this.to = to;
        }
    }

    private final @Nullable Runnable finishCallback;
    private final CleanWorld world;
    private final List<BarrierNode> topo;
    private volatile boolean executing = false;
    private final ReentrantLock lock = new ReentrantLock();

    protected AbstractFlow(@NonNull CleanWorld world, @NonNull List<@NonNull BarrierNode> topo, @Nullable Runnable finishCallback) {
        this.finishCallback = finishCallback;
        this.world = world;
        this.topo = topo;
    }

    @Override
    public void execute() {
        if (!lock.tryLock()) {
            throw new IllegalStateException("Must not execute while executing!");
        }

        executing = true;
        try {
            for (BarrierNode node : topo) {
                for (Transition edge : node.incoming) {
                    if (edge.system != null) {
                        ISystemExeFlowGraph.joinSystem(edge.system);
                    }
                }

                if (node.callback != null) {
                    node.callback.run();
                }

                for (Transition edge : node.outgoing) {
                    if (edge.system != null) {
                        ISystemExeFlowGraph.executeSystem(world, edge.system);
                    }
                }
            }
        } finally {
            try {
                if (finishCallback != null) {
                    finishCallback.run();
                }
            } finally {
                executing = false;
                lock.unlock();
            }
        }
    }

    @Override
    public boolean isExecuting() {
        return executing;
    }
}
