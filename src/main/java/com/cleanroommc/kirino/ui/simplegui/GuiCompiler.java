package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class GuiCompiler {

    private final TransientArena arena;

    GuiCompiler(TransientArena arena) {
        this.arena = arena;
    }

    public void compile(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        int layer = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            // |---header---|---used---|---tail---|---other---|---padding---|
            // tail = layer + depth
            if (op != SG_GuiOp.PUSH_CLIP && op != SG_GuiOp.POP_CLIP) {
                view.putInt(pos + used, layer++);
            }

            if (op == SG_GuiOp.DRAW_RECT && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileRect(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.DRAW_LINES && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileLines(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.DRAW_BEZIER && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileBezier(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.PUSH_CLIP && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compilePushClip(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.POP_CLIP && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compilePopClip(view, pos, flags, used, size);
            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            pos += size;
        }

        pos = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            if (op != SG_GuiOp.PUSH_CLIP && op != SG_GuiOp.POP_CLIP) {
                int _layer = view.getInt(pos + used);
                float depth = (float) (((double) _layer) / ((double) layer));
                view.putFloat(pos + used + 4, depth);
            }

            pos += size;
        }
    }

    //<editor-fold desc="compile">
    private void compileRect(ByteBuffer buffer, int pos, int flags, int used, int size) {
        int posPointer = pos + SG_CmdHeader.HEADER_SIZE;

        float x = buffer.getFloat(posPointer); posPointer += 4;
        float y = buffer.getFloat(posPointer); posPointer += 4;
        float width = buffer.getFloat(posPointer); posPointer += 4;
        float height = buffer.getFloat(posPointer); posPointer += 4;
        int color = buffer.getInt(posPointer); posPointer += 4;

        float radius = 0f;
        int cornerType = 0;

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            radius = buffer.getFloat(posPointer); posPointer += 4;
            cornerType = buffer.getInt(posPointer); posPointer += 4;
        }

        boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;

        if (!needsMesh) {
            buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
            return;
        }

        int[] out = new int[2];
        buildRoundedRectMesh(x, y, width, height, radius, cornerType, out);
        int meshOffset = out[0];
        int vertexCount = out[1]; // fan vert count

        Preconditions.checkState(used + SG_CmdHeader.TAIL_SIZE + 8 <= size,
                "No reserved space for in-place rewrite (used=%s, want=%s, size=%s).",
                used, used + SG_CmdHeader.TAIL_SIZE + 8, size);

        int outputPos = used + SG_CmdHeader.TAIL_SIZE;

        buffer.putInt(outputPos, meshOffset);
        buffer.putInt(outputPos + 4, vertexCount);
        buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
    }

    private void compileLines(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compileBezier(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compilePushClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compilePopClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }
    //</editor-fold>

    //<editor-fold desc="mesh building">
    private static final int[] CURSOR = {0};

    private void buildRoundedRectMesh(
            float x,
            float y,
            float width,
            float height,
            float radius,
            int cornerType,
            int[] out) {

        Preconditions.checkArgument(out.length == 2, "Length of \"out\" must be 2.");
        Preconditions.checkArgument(
                cornerType == 0 ||
                        cornerType == 1 ||
                        cornerType == 2 ||
                        cornerType == 3 ||
                        cornerType == 4 ||
                        cornerType == 5,
                "Argument \"cornerType\"=%s must be either 0, 1, 2, 3, 4, 5.", cornerType);

        boolean circle = cornerType == 0 || cornerType == 1;

        CURSOR[0] = 0;

        float tlx = x + radius;
        float tly = y + radius;

        float trx = x + width - radius;
        float try_ = y + radius;

        float brx = x + width - radius;
        float bry = y + height - radius;

        float blx = x + radius;
        float bly = y + height - radius;

        if (circle) {
            // 0: 5 vertices
            // 1: 10 vertices
            int cornerVertCount = cornerType == 0 ? 5 : 10;
            int size = cornerVertCount * 4 * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 16);
            ByteBuffer view = arena.view();

            out[0] = offset;
            out[1] = cornerVertCount * 4 + 1; // plus center

            // put center at first
            putVertex(
                    view,
                    offset,
                    CURSOR[0]++,
                    (tlx + trx) / 2f,
                    (tly + bly) / 2f);

            emitArc(
                    view, offset, CURSOR,
                    tlx, tly,
                    radius,
                    (float) Math.PI,
                    (float) Math.PI * 0.5f,
                    cornerVertCount);

            emitArc(
                    view, offset, CURSOR,
                    trx, try_,
                    radius,
                    (float) Math.PI * 0.5f,
                    0f,
                    cornerVertCount);

            emitArc(
                    view, offset, CURSOR,
                    brx, bry,
                    radius,
                    0f,
                    -(float) Math.PI * 0.5f,
                    cornerVertCount);

            emitArc(
                    view, offset, CURSOR,
                    blx, bly,
                    radius,
                    -(float) Math.PI * 0.5f,
                    -(float) Math.PI,
                    cornerVertCount);
        } else {
            // 2: n=4, 8 vertices
            // 3: n=4, 16 vertices
            // 4: n=5, 8 vertices
            // 5: n=5, 16 vertices
            int cornerVertCount = (cornerType == 2 || cornerType == 4) ? 8 : 16;
            int size = cornerVertCount * 4 * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 16);
            ByteBuffer view = arena.view();

            out[0] = offset;
            out[1] = cornerVertCount * 4 + 1; // plus center

            float superellipseN = (cornerType == 2 || cornerType == 4) ? 4f : 5f;

            // put center at first
            putVertex(
                    view,
                    offset,
                    CURSOR[0]++,
                    (tlx + trx) / 2f,
                    (tly + bly) / 2f);

            emitSuperellipseCorner(
                    view, offset, CURSOR,
                    tlx, tly,
                    radius,
                    (float) Math.PI,
                    (float) Math.PI * 0.5f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, CURSOR,
                    trx, try_,
                    radius,
                    (float) Math.PI * 0.5f,
                    0f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, CURSOR,
                    brx, bry,
                    radius,
                    0f,
                    -(float) Math.PI * 0.5f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, CURSOR,
                    blx, bly,
                    radius,
                    -(float) Math.PI * 0.5f,
                    -(float) Math.PI,
                    superellipseN,
                    cornerVertCount);
        }
    }

    private static void putVertex(
            ByteBuffer buffer,
            int baseOffset,
            int index,
            float x,
            float y) {

        int pos = baseOffset + index * 8;

        buffer.putFloat(pos, x);
        buffer.putFloat(pos + 4, y);
    }

    private static void emitArc(
            ByteBuffer buffer,
            int baseOffset,
            int[] cursor,
            float cx,
            float cy,
            float radius,
            float startAngle,
            float endAngle,
            int count) {

        for (int i = 0; i < count; i++) {
            float t = (float) i / (count - 1);
            float a = startAngle + (endAngle - startAngle) * t;
            float px = cx + (float) Math.cos(a) * radius;
            float py = cy - (float) Math.sin(a) * radius;

            putVertex(
                    buffer,
                    baseOffset,
                    cursor[0]++,
                    px,
                    py);
        }
    }

    private static void emitSuperellipseCorner(
            ByteBuffer buffer,
            int baseOffset,
            int[] cursor,
            float cx,
            float cy,
            float radius,
            float startAngle,
            float endAngle,
            float n,
            int count) {

        float power = 2f / n;

        for (int i = 0; i < count; i++) {
            float t = (float) i / (count - 1);
            float a = startAngle + (endAngle - startAngle) * t;

            float c = (float) Math.cos(a);
            float s = (float) Math.sin(a);

            float sx = signedPow(c, power);
            float sy = signedPow(s, power);

            float px = cx + sx * radius;
            float py = cy - sy * radius;

            putVertex(
                    buffer,
                    baseOffset,
                    cursor[0]++,
                    px,
                    py);
        }
    }

    private static float signedPow(float v, float power) {
        if (v == 0f) {
            return 0f;
        }

        return Math.copySign((float) Math.pow(Math.abs(v), power), v);
    }
    //</editor-fold>
}
