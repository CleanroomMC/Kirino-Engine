package com.cleanroommc.kirino.engine.render.core.pipeline.draw;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.IDrawCommand;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.core.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.MeshReceipt;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class DrawQueue {
    /**
     * The queue we use for commands.
     */
    private Deque<IDrawCommand> deque = new ArrayDeque<>();

    /**
     * Part of the double-buffered queue optimization.
     */
    private Deque<IDrawCommand> deque2 = new ArrayDeque<>();

    public DrawQueue() {
    }

    public void enqueue(IDrawCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public IDrawCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }

    //<editor-fold desc="compile">
    /**
     * Compiles everything into {@link LowLevelDC}s.
     * After calling this method, every element in this draw queue is guaranteed to be a {@link LowLevelDC}.
     * High-level commands are converted in-place. Low-level commands remain unchanged.
     *
     * @param graphicResourceManager The graphic resource manager
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue compile(GraphicResourceManager graphicResourceManager) {
        // deque2 should be empty to begin with
        if (!deque2.isEmpty()) {
            deque2.clear();
        }

        IDrawCommand item;
        while ((item = deque.pollFirst()) != null) {
            if (item instanceof LowLevelDC) {
                deque2.offerLast(item);
            } else if (item instanceof HighLevelDC highLevelDC) {
                Optional<GResourceTicket<MeshPayload, MeshReceipt>> optional = graphicResourceManager.getMeshTicket(highLevelDC.meshTicketID);
                if (optional.isEmpty()) {
                    continue;
                }
                if (!optional.get().isResourceReady() || optional.get().isExpired()) {
                    continue;
                }

                MeshReceipt meshReceipt = optional.get().getReceipt();

                int elementSize = 0;
                if (highLevelDC.elementType == GL11.GL_UNSIGNED_BYTE) {
                    elementSize = 1;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_SHORT) {
                    elementSize = 2;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_INT) {
                    elementSize = 4;
                }

                deque2.offerLast(LowLevelDC.acquire().fillMultiElementIndirectUnit(
                        meshReceipt.vao,
                        highLevelDC.mode,
                        highLevelDC.elementType,
                        meshReceipt.eboLength / elementSize,
                        1,
                        meshReceipt.eboOffset / elementSize,
                        meshReceipt.baseVertex,
                        0));

                highLevelDC.recycle();
            }
        }

        // deque is now empty and deque2 is filled; swap
        Deque<IDrawCommand> swap = deque2;
        deque2 = deque;
        deque = swap;
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="simplify">
    private final Map<VAOKey, List<LowLevelDC>> groupedCommands = new HashMap<>();
    private final List<LowLevelDC> mdiUnits = new ArrayList<>();

    /**
     * Only used by {@link #simplify(IndirectDrawBufferGenerator)}.
     */
    record VAOKey(int vao, int mode, int elementType) {
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>Every element in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It combines and simplifies {@link LowLevelDC}s, especially combines commands into <code>MULTI_ELEMENTS_INDIRECT</code> commands.
     * Usually it's called after {@link #compile(GraphicResourceManager)} which compiles everything into {@link LowLevelDC}s.
     *
     * @param idbGenerator The indirect draw buffer manager
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue simplify(IndirectDrawBufferGenerator idbGenerator) {
        // deque2 should be empty to begin with
        if (!deque2.isEmpty()) {
            deque2.clear();
        }

        // clear groupedCommands
        for (List<LowLevelDC> list : groupedCommands.values()) {
            list.clear();
        }

        IDrawCommand item;
        while ((item = deque.pollFirst()) != null) {
            LowLevelDC lowLevelDC = (LowLevelDC) item;
            VAOKey key = new VAOKey(lowLevelDC.vao, lowLevelDC.mode, lowLevelDC.elementType);
            groupedCommands.computeIfAbsent(key, k -> new ArrayList<>()).add(lowLevelDC);
        }

        // deque is now empty

        idbGenerator.reset();

        for (Map.Entry<VAOKey, List<LowLevelDC>> entry : groupedCommands.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            mdiUnits.clear();
            for (LowLevelDC lowLevelDC : entry.getValue()) {
                if (lowLevelDC.type == LowLevelDC.DrawType.MULTI_ELEMENTS_INDIRECT_UNIT) {
                    mdiUnits.add(lowLevelDC);
                } else {
                    deque2.offerLast(lowLevelDC);
                }
            }

            // combine units and upload to idb
            if (mdiUnits.isEmpty()) {
                continue;
            }

            int start = 0;
            while (start < mdiUnits.size()) {
                int end = Math.min(start + KirinoCommonCore.KIRINO_CONFIG_HUB.getMaxMultiDrawIndirectUnitCount(), mdiUnits.size());
                List<LowLevelDC> chunk = mdiUnits.subList(start, end);
                deque2.offerLast(idbGenerator.generate(
                        chunk,
                        entry.getKey().vao,
                        entry.getKey().mode,
                        entry.getKey().elementType));
                start = end;
            }
        }

        // deque is empty and deque2 is filled; swap
        Deque<IDrawCommand> swap = deque2;
        deque2 = deque;
        deque = swap;
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="sort">
    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>Every element in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It sorts all {@link LowLevelDC}s, which is the final stage of command processing.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue sort() {

        return this;
    }
    //</editor-fold>
}
