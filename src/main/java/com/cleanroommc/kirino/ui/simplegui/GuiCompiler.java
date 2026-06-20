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

            // |-----------used-----------|
            // |------------------------------size-----------------------------|
            // |---header---|---payload---|---tail---|---other---|---padding---|

            // tail = layer + depth
            if (op != SG_GuiOp.PUSH_CLIP && op != SG_GuiOp.POP_CLIP) {
                view.putInt(pos + used, layer++);
            }

            if (op == SG_GuiOp.DRAW_RECT && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileRect(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.DRAW_LINES && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileLines(view, pos, flags, used, size);
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
        int meshOffset = out[0]; // unit: vertex
        int vertexCount = out[1]; // fan vert count

        Preconditions.checkState(used + SG_CmdHeader.TAIL_SIZE + 8 <= size,
                "No reserved space for in-place rewrite (used=%s, want=%s, size=%s).",
                used, used + SG_CmdHeader.TAIL_SIZE + 8, size);

        int outputPos = pos + used + SG_CmdHeader.TAIL_SIZE;

        buffer.putInt(outputPos, meshOffset);
        buffer.putInt(outputPos + 4, vertexCount);

        buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
    }

    private void compileLines(ByteBuffer buffer, int pos, int flags, int used, int size) {
        int offset = 4;
        if ((flags & SG_GuiOp.FLAG_COLOR1) != 0) {
            offset += 4;
        }
        if ((flags & SG_GuiOp.FLAG_COLOR2) != 0) {
            offset += 4;
        }

        int vertexNumPos = pos + SG_CmdHeader.HEADER_SIZE + offset;
        int lineWidthPos = vertexNumPos + 4;
        int vertexNum = buffer.getInt(vertexNumPos);
        float lineWidth = buffer.getFloat(lineWidthPos);

        int formsLoopPos = pos + SG_CmdHeader.HEADER_SIZE + offset + 8 + (vertexNum * 2) * 4;
        boolean formsLoop = (buffer.get(formsLoopPos) != 0);

        float[] vertices = new float[vertexNum * 2];
        int verticesStartPos = pos + SG_CmdHeader.HEADER_SIZE + offset + 8;
        for (int i = 0; i < vertexNum; i++) {
            vertices[i * 2] = buffer.getFloat(verticesStartPos + i * 8);
            vertices[i * 2 + 1] = buffer.getFloat(verticesStartPos + i * 8 + 4);
        }

        int[] out = new int[2];
        buildLinesMesh(vertices, vertexNum, lineWidth, formsLoop, out);
        int meshOffset0 = out[0]; // unit: vertex
        int meshOffset1 = out[1]; // unit: vertex

        Preconditions.checkState(used + SG_CmdHeader.TAIL_SIZE + 8 <= size,
                "No reserved space for in-place rewrite (used=%s, want=%s, size=%s).",
                used, used + SG_CmdHeader.TAIL_SIZE + 8, size);

        int outputPos = pos + used + SG_CmdHeader.TAIL_SIZE;

        buffer.putInt(outputPos, meshOffset0);
        buffer.putInt(outputPos + 4, meshOffset1);

        buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
    }

    private void compilePushClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compilePopClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }
    //</editor-fold>

    //<editor-fold desc="mesh building utils">
    private static final float EPSILON = 1E-6f;

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

    private static float signedPow(float v, float power) {
        if (v == 0f) {
            return 0f;
        }

        return Math.copySign((float) Math.pow(Math.abs(v), power), v);
    }

    private static float nearZeroKeepSign(float value) {
        if (Math.abs(value) >= EPSILON) {
            return value;
        }

        return value < 0f ? -EPSILON : EPSILON;
    }

    private static float cross(float x1, float y1, float x2, float y2) {
        return x1 * y2 - y1 * x2;
    }
    //</editor-fold>

    //<editor-fold desc="rounded rect mesh building">
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
                        cornerType == 5 ||
                        cornerType == 6,
                "Argument \"cornerType\"=%s must be either 0, 1, 2, 3, 4, 5, 6.", cornerType);

        boolean circle = cornerType == 0 || cornerType == 1;
        boolean superellipse = cornerType == 2 ||
                cornerType == 3 ||
                cornerType == 4 ||
                cornerType == 5;

        int[] cursor = {0};

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
            int size = (cornerVertCount * 4 + 1) * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 8);
            ByteBuffer view = arena.view();

            out[0] = offset / 8; // unit: vertex
            out[1] = cornerVertCount * 4 + 1; // plus center

            // put center at first
            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    (tlx + trx) / 2f,
                    (tly + bly) / 2f);

            emitArc(
                    view, offset, cursor,
                    tlx, tly,
                    radius,
                    (float) Math.PI,
                    (float) Math.PI * 0.5f,
                    cornerVertCount);

            emitArc(
                    view, offset, cursor,
                    trx, try_,
                    radius,
                    (float) Math.PI * 0.5f,
                    0f,
                    cornerVertCount);

            emitArc(
                    view, offset, cursor,
                    brx, bry,
                    radius,
                    0f,
                    -(float) Math.PI * 0.5f,
                    cornerVertCount);

            emitArc(
                    view, offset, cursor,
                    blx, bly,
                    radius,
                    -(float) Math.PI * 0.5f,
                    -(float) Math.PI,
                    cornerVertCount);
        } else if (superellipse) {
            // 2: n=4, 8 vertices
            // 3: n=4, 16 vertices
            // 4: n=5, 8 vertices
            // 5: n=5, 16 vertices
            int cornerVertCount = (cornerType == 2 || cornerType == 4) ? 8 : 16;
            int size = (cornerVertCount * 4 + 1) * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 8);
            ByteBuffer view = arena.view();

            out[0] = offset / 8; // unit: vertex
            out[1] = cornerVertCount * 4 + 1; // plus center

            float superellipseN = (cornerType == 2 || cornerType == 4) ? 4f : 5f;

            // put center at first
            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    (tlx + trx) / 2f,
                    (tly + bly) / 2f);

            emitSuperellipseCorner(
                    view, offset, cursor,
                    tlx, tly,
                    radius,
                    (float) Math.PI,
                    (float) Math.PI * 0.5f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, cursor,
                    trx, try_,
                    radius,
                    (float) Math.PI * 0.5f,
                    0f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, cursor,
                    brx, bry,
                    radius,
                    0f,
                    -(float) Math.PI * 0.5f,
                    superellipseN,
                    cornerVertCount);

            emitSuperellipseCorner(
                    view, offset, cursor,
                    blx, bly,
                    radius,
                    -(float) Math.PI * 0.5f,
                    -(float) Math.PI,
                    superellipseN,
                    cornerVertCount);
        } else {
            // 6: 12 vertices in total (square)
            int size = 13 * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 8);
            ByteBuffer view = arena.view();

            out[0] = offset / 8; // unit: vertex
            out[1] = 13; // plus center

            // put center at first
            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    (tlx + trx) / 2f,
                    (tly + bly) / 2f);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x,
                    y);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    tlx,
                    y);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    trx,
                    y);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x + width,
                    y);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x + width,
                    try_);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x + width,
                    bry);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x + width,
                    y + height);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    brx,
                    y + height);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    blx,
                    y + height);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x,
                    y + height);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x,
                    bly);

            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    x,
                    tly);
        }
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
    //</editor-fold>

    //<editor-fold desc="lines mesh building">
    private void buildLinesMesh(
            float[] vertices,
            int vertexNum,
            float lineWidth,
            boolean formsLoop,
            int[] out) {

        Preconditions.checkArgument(out.length == 2, "Length of \"out\" must be 2.");

        int[] cursor = {0};

        int lineNum = vertexNum - 1;
        int vertexCount = (formsLoop ? lineNum + 1 : lineNum) * 6;

        // store length of every line seg (compact storing)
        int lineSegLenSlotCount = ((formsLoop ? lineNum + 1 : lineNum) % 2 == 0) ?
                (formsLoop ? lineNum + 1 : lineNum) / 2 : ((formsLoop ? lineNum + 1 : lineNum) / 2) + 1;
        lineSegLenSlotCount += 1; // first slot for the total length

        int size = (vertexCount + lineSegLenSlotCount) * 8;
        int offset = arena.alloc(size, 8);
        ByteBuffer view = arena.view();

        out[0] = offset / 8; // unit: vertex
        out[1] = out[0] + vertexCount; // unit: vertex

        float[] lineNormalX = new float[vertexNum];
        float[] lineNormalY = new float[vertexNum];

        for (int i = 0; i < vertexNum - 1; i++) {
            float x1 = vertices[i * 2];
            float y1 = vertices[i * 2 + 1];
            float x2 = vertices[(i + 1) * 2];
            float y2 = vertices[(i + 1) * 2 + 1];

            float dxN = -(y2 - y1);
            float dyN = x2 - x1;
            float len = (float) Math.sqrt(dxN * dxN + dyN * dyN);

            lineNormalX[i] = dxN / nearZeroKeepSign(len);
            lineNormalY[i] = dyN / nearZeroKeepSign(len);
        }

        if (formsLoop) {
            float x1 = vertices[(vertexNum - 1) * 2];
            float y1 = vertices[(vertexNum - 1) * 2 + 1];
            float x2 = vertices[0];
            float y2 = vertices[1];

            float dxN = -(y2 - y1);
            float dyN = x2 - x1;
            float len = (float) Math.sqrt(dxN * dxN + dyN * dyN);

            lineNormalX[vertexNum - 1] = dxN / nearZeroKeepSign(len);
            lineNormalY[vertexNum - 1] = dyN / nearZeroKeepSign(len);
        } else {
            lineNormalX[vertexNum - 1] = lineNormalX[vertexNum - 2];
            lineNormalY[vertexNum - 1] = lineNormalY[vertexNum - 2];
        }

        float[] vertexNormalX = new float[vertexNum * 2];
        float[] vertexNormalY = new float[vertexNum * 2];

        float[] outNormal = new float[2];
        for (int i = 0; i < vertexNum; i++) {
            float normalX1;
            float normalY1;
            float normalX2;
            float normalY2;

            if (i == 0 || i == vertexNum - 1) {
                normalX1 = lineNormalX[i] * lineWidth / 2f;
                normalY1 = lineNormalY[i] * lineWidth / 2f;
                normalX2 = -normalX1;
                normalY2 = -normalY1;
            } else {
                getJoinNormal(vertices, lineNormalX, lineNormalY, i, lineWidth, 1f, outNormal);
                normalX1 = outNormal[0];
                normalY1 = outNormal[1];

                getJoinNormal(vertices, lineNormalX, lineNormalY, i, lineWidth, -1f, outNormal);
                normalX2 = outNormal[0];
                normalY2 = outNormal[1];
            }

            vertexNormalX[i * 2] = normalX1;
            vertexNormalY[i * 2] = normalY1;
            vertexNormalX[i * 2 + 1] = normalX2;
            vertexNormalY[i * 2 + 1] = normalY2;
        }

        if (formsLoop) {
            fixLoopJoinNormal(vertices, vertexNum, lineNormalX, lineNormalY, lineWidth, vertexNormalX, vertexNormalY);
        }

        for (int i = 0; i < lineNum; i++) {
            int i1 = i * 2;
            int i2 = i * 2 + 1;
            int i3 = i * 2 + 2;
            int i4 = i * 2 + 3;

            emitTriangle(view, offset, cursor, vertices, vertexNormalX, vertexNormalY, i1, i3, i2);
            emitTriangle(view, offset, cursor, vertices, vertexNormalX, vertexNormalY, i3, i4, i2);
        }

        if (formsLoop) {
            int i1 = 0;
            int i2 = 1;
            int i3 = vertexNum * 2 - 2;
            int i4 = vertexNum * 2 - 1;

            emitTriangle(view, offset, cursor, vertices, vertexNormalX, vertexNormalY, i1, i3, i2);
            emitTriangle(view, offset, cursor, vertices, vertexNormalX, vertexNormalY, i3, i4, i2);
        }

        float[] lineSegLen = new float[formsLoop ? lineNum + 1 : lineNum];
        for (int i = 0; i < vertexNum - 1; i++) {
            float x1 = vertices[i * 2];
            float y1 = vertices[i * 2 + 1];
            float x2 = vertices[(i + 1) * 2];
            float y2 = vertices[(i + 1) * 2 + 1];

            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lineSegLen[i] = len;
        }

        if (formsLoop) {
            float x1 = vertices[(vertexNum - 1) * 2];
            float y1 = vertices[(vertexNum - 1) * 2 + 1];
            float x2 = vertices[0];
            float y2 = vertices[1];

            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lineSegLen[lineSegLen.length - 1] = len;
        }

        float totalLength = 0f;
        for (int i = 0; i < lineSegLen.length; i++) {
            totalLength += lineSegLen[i];
            if (i > 0) {
                lineSegLen[i] += lineSegLen[i - 1];
            }
        }

        putVertex(
                view,
                offset,
                cursor[0]++,
                totalLength,
                0f);

        for (int i = 0; i < lineSegLenSlotCount - 1; i++) {
            putVertex(
                    view,
                    offset,
                    cursor[0]++,
                    lineSegLen[i * 2],
                    (lineSegLen.length >= i * 2 + 2) ? lineSegLen[i * 2 + 1] : 0f);
        }
    }

    private static void fixLoopJoinNormal(
            float[] vertices,
            int vertexNum,
            float[] lineNormalX,
            float[] lineNormalY,
            float lineWidth,
            float[] vertexNormalX,
            float[] vertexNormalY) {

        float[] outNormal = new float[2];

        getLoopJoinNormal(vertices, vertexNum, lineNormalX, lineNormalY, lineWidth, 0, 1f, outNormal);
        vertexNormalX[0] = outNormal[0];
        vertexNormalY[0] = outNormal[1];

        getLoopJoinNormal(vertices, vertexNum, lineNormalX, lineNormalY, lineWidth, 0, -1f, outNormal);
        vertexNormalX[1] = outNormal[0];
        vertexNormalY[1] = outNormal[1];

        int lastVertexIndex = vertexNum - 1;
        int lastBase = lastVertexIndex * 2;

        getLoopJoinNormal(vertices, vertexNum, lineNormalX, lineNormalY, lineWidth, lastVertexIndex, 1f, outNormal);
        vertexNormalX[lastBase] = outNormal[0];
        vertexNormalY[lastBase] = outNormal[1];

        getLoopJoinNormal(vertices, vertexNum, lineNormalX, lineNormalY, lineWidth, lastVertexIndex, -1f, outNormal);
        vertexNormalX[lastBase + 1] = outNormal[0];
        vertexNormalY[lastBase + 1] = outNormal[1];
    }

    private static void getLoopJoinNormal(
            float[] vertices,
            int vertexNum,
            float[] lineNormalX,
            float[] lineNormalY,
            float lineWidth,
            int i,
            float side,
            float[] out) {

        float halfWidth = lineWidth / 2f;

        int prevIndex;
        int nextIndex;
        int prevLineIndex;
        int nextLineIndex;

        if (i == 0) {
            prevIndex = vertexNum - 1;
            nextIndex = 1;
            prevLineIndex = vertexNum - 1;
            nextLineIndex = 0;
        } else {
            prevIndex = vertexNum - 2;
            nextIndex = 0;
            prevLineIndex = vertexNum - 2;
            nextLineIndex = vertexNum - 1;
        }

        float x0 = vertices[prevIndex * 2];
        float y0 = vertices[prevIndex * 2 + 1];
        float x1 = vertices[i * 2];
        float y1 = vertices[i * 2 + 1];
        float x2 = vertices[nextIndex * 2];
        float y2 = vertices[nextIndex * 2 + 1];

        float n0x = lineNormalX[prevLineIndex] * halfWidth * side;
        float n0y = lineNormalY[prevLineIndex] * halfWidth * side;
        float n1x = lineNormalX[nextLineIndex] * halfWidth * side;
        float n1y = lineNormalY[nextLineIndex] * halfWidth * side;

        float ax = x0 + n0x;
        float ay = y0 + n0y;
        float bx = x1 + n0x;
        float by = y1 + n0y;

        float cx = x1 + n1x;
        float cy = y1 + n1y;
        float dx = x2 + n1x;
        float dy = y2 + n1y;

        float rx = bx - ax;
        float ry = by - ay;
        float sx = dx - cx;
        float sy = dy - cy;

        float denom = cross(rx, ry, sx, sy);

        if (Math.abs(denom) < EPSILON) {
            float nx = lineNormalX[prevLineIndex] + lineNormalX[nextLineIndex];
            float ny = lineNormalY[prevLineIndex] + lineNormalY[nextLineIndex];

            float len = (float) Math.sqrt(nx * nx + ny * ny);

            if (Math.abs(len) < EPSILON) {
                nx = lineNormalX[nextLineIndex];
                ny = lineNormalY[nextLineIndex];
                len = (float) Math.sqrt(nx * nx + ny * ny);
            }

            out[0] = nx / nearZeroKeepSign(len) * halfWidth * side;
            out[1] = ny / nearZeroKeepSign(len) * halfWidth * side;
            return;
        }

        float qpx = cx - ax;
        float qpy = cy - ay;

        float t = cross(qpx, qpy, sx, sy) / denom;

        float intersectX = ax + rx * t;
        float intersectY = ay + ry * t;

        out[0] = intersectX - x1;
        out[1] = intersectY - y1;
    }

    private static void getJoinNormal(
            float[] vertices,
            float[] lineNormalX,
            float[] lineNormalY,
            int i,
            float lineWidth,
            float side,
            float[] out) {

        float halfWidth = lineWidth / 2f;

        float x0 = vertices[(i - 1) * 2];
        float y0 = vertices[(i - 1) * 2 + 1];
        float x1 = vertices[i * 2];
        float y1 = vertices[i * 2 + 1];
        float x2 = vertices[(i + 1) * 2];
        float y2 = vertices[(i + 1) * 2 + 1];

        float n0x = lineNormalX[i - 1] * halfWidth * side;
        float n0y = lineNormalY[i - 1] * halfWidth * side;
        float n1x = lineNormalX[i] * halfWidth * side;
        float n1y = lineNormalY[i] * halfWidth * side;

        float ax = x0 + n0x;
        float ay = y0 + n0y;
        float bx = x1 + n0x;
        float by = y1 + n0y;

        float cx = x1 + n1x;
        float cy = y1 + n1y;
        float dx = x2 + n1x;
        float dy = y2 + n1y;

        float rx = bx - ax;
        float ry = by - ay;
        float sx = dx - cx;
        float sy = dy - cy;

        float denom = cross(rx, ry, sx, sy);

        if (Math.abs(denom) < EPSILON) {
            float nx = lineNormalX[i - 1] + lineNormalX[i];
            float ny = lineNormalY[i - 1] + lineNormalY[i];

            float len = (float) Math.sqrt(nx * nx + ny * ny);

            if (Math.abs(len) < EPSILON) {
                nx = lineNormalX[i];
                ny = lineNormalY[i];
                len = (float) Math.sqrt(nx * nx + ny * ny);
            }

            out[0] = nx / nearZeroKeepSign(len) * halfWidth * side;
            out[1] = ny / nearZeroKeepSign(len) * halfWidth * side;
            return;
        }

        float qpx = cx - ax;
        float qpy = cy - ay;

        float t = cross(qpx, qpy, sx, sy) / denom;

        float intersectX = ax + rx * t;
        float intersectY = ay + ry * t;

        out[0] = intersectX - x1;
        out[1] = intersectY - y1;
    }

    private static float getExpandedVertX(float[] vertices, float[] vertexNormalX, int index) {
        int vertexIndex = index / 2;
        return vertices[vertexIndex * 2] + vertexNormalX[index];
    }

    private static float getExpandedVertY(float[] vertices, float[] vertexNormalY, int index) {
        int vertexIndex = index / 2;
        return vertices[vertexIndex * 2 + 1] + vertexNormalY[index];
    }

    private static void emitTriangle(
            ByteBuffer buffer,
            int baseOffset,
            int[] cursor,
            float[] vertices,
            float[] vertexNormalX,
            float[] vertexNormalY,
            int i1,
            int i2,
            int i3) {

        float x1 = getExpandedVertX(vertices, vertexNormalX, i1);
        float y1 = getExpandedVertY(vertices, vertexNormalY, i1);
        float x2 = getExpandedVertX(vertices, vertexNormalX, i2);
        float y2 = getExpandedVertY(vertices, vertexNormalY, i2);
        float x3 = getExpandedVertX(vertices, vertexNormalX, i3);
        float y3 = getExpandedVertY(vertices, vertexNormalY, i3);

        putVertex(buffer, baseOffset, cursor[0]++, x1, y1);
        putVertex(buffer, baseOffset, cursor[0]++, x2, y2);
        putVertex(buffer, baseOffset, cursor[0]++, x3, y3);
    }
    //</editor-fold>
}
