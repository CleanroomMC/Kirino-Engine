package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class GuiRenderer {

    // stride=?
    // int type
    //
    private final int drawInfoCapacity;
    private final ByteBuffer drawInfo;
    private final SSBOView drawInfoView;

    // stride=16
    // int count: actual vert count (= TRIANGLES mode vert count. != vert count from the input stream.
    //     every type uses diff mode. normalize in vert shader)
    // int instanceCount = 1
    // int first = 0
    // int baseInstance: drawInfo SSBO index
    private final int idbCapacity;
    private final ByteBuffer idb;
    private final IDBView idbView;

    GuiRenderer(int drawInfoCapacity, int idbCapacity) {
        this.drawInfoCapacity = drawInfoCapacity;
        this.idbCapacity = idbCapacity;
        drawInfo = BufferUtils.createByteBuffer(drawInfoCapacity);
        idb = BufferUtils.createByteBuffer(idbCapacity);
        drawInfoView = new SSBOView(new GLBuffer());
        idbView = new IDBView(new GLBuffer());
    }

    public void render(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        int count = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            Preconditions.checkState((flags & SG_GuiOp.FLAG_COMPILED) != 0,
                    "Command (pos=%s, op=%s) must be compiled already.", pos, op);

            if (op == SG_GuiOp.DRAW_RECT) {

            } else if (op == SG_GuiOp.DRAW_LINES) {

            } else if (op == SG_GuiOp.DRAW_BEZIER) {

            } else if (op == SG_GuiOp.PUSH_CLIP) {
                if (count > 0) {
                    count = 0;
                    flush();
                }
                pushClip();
            } else if (op == SG_GuiOp.POP_CLIP) {
                if (count > 0) {
                    count = 0;
                    flush();
                }
                popClip();
            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            count++;
            pos += size;
        }

        if (count > 0) {
            count = 0;
            flush();
        }
    }

    private void pushClip() {

    }

    private void popClip() {

    }

    private void flush() {

    }
}
