package com.cleanroommc.kirino.engine.render.core.debug.gizmos;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.platform.ecs.struct.Block;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.core.resource.UploadStrategy;
import com.cleanroommc.kirino.engine.render.core.resource.builder.MeshTicketBuilder;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GizmosManager {

    private record BlockSurface(float x, float y, float z, int faceMask, int color) {
    }

    private record MeshletKey(int chunkPosX, int chunkPosY, int chunkPosZ) {
    }

    private record Meshlet(List<BlockSurface> blockSurfaces) {
    }

    private final GraphicResourceManager graphicResourceManager;

    private final Map<MeshletKey, List<Meshlet>> meshlets = new ConcurrentHashMap<>();

    /**
     * It doesn't care if one face of the block overlaps.
     * It only returns <code>true</code> when all faces (the whole <code>faceMask</code>)
     * of a block overlap.
     *
     * <p>Note: thread safety is guaranteed.</p>
     */
    private boolean meshletOverlaps(MeshletKey key, float x, float y, float z, int faceMask) {
        List<Meshlet> list = meshlets.get(key);
        if (list == null) {
            return false;
        }

        synchronized (list) {
            for (Meshlet meshlet : list) {
                for (BlockSurface blockSurface : meshlet.blockSurfaces) {
                    if (blockSurface.x == x
                            && blockSurface.y == y
                            && blockSurface.z == z
                            && blockSurface.faceMask == faceMask) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * <p>Note: thread safety is guaranteed.</p>
     */
    public void addMeshlet(int chunkPosX, int chunkPosY, int chunkPosZ, List<Integer> blocks) {
        MeshletKey key = new MeshletKey(chunkPosX, chunkPosY, chunkPosZ);
        List<Meshlet> list = meshlets.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));

        int xWorldOffset = chunkPosX * 16;
        int yWorldOffset = chunkPosY * 16;
        int zWorldOffset = chunkPosZ * 16;

        int hash = Arrays.hashCode(blocks.toArray(new Integer[0]));
        hash = hash * 31 + Objects.hash(xWorldOffset, yWorldOffset, zWorldOffset);

        Random random = new Random(hash);
        Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.5f);

        List<BlockSurface> blockSurfaceList = new ArrayList<>();
        for (Integer block : blocks) {
            int[] positionAndFaceMask = Block.decompress(block);
            float x = xWorldOffset + positionAndFaceMask[0];
            float y = yWorldOffset + positionAndFaceMask[1];
            float z = zWorldOffset + positionAndFaceMask[2];
            int faceMask = positionAndFaceMask[3];

            if (meshletOverlaps(key, x, y, z, faceMask)) {
                // duplicate; warn
                blockSurfaceList.add(new BlockSurface(x, y, z, faceMask, Color.RED.getRGB()));
            } else {
                blockSurfaceList.add(new BlockSurface(x, y, z, faceMask, color.getRGB()));
            }
        }

        synchronized (list) {
            list.add(new Meshlet(blockSurfaceList));
        }
    }

    public void clearMeshlets() {
        meshlets.clear();
    }

    public GizmosManager(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    public final static AttributeLayout ATTRIBUTE_LAYOUT;

    static {
        ATTRIBUTE_LAYOUT = new AttributeLayout();
        ATTRIBUTE_LAYOUT.push(new Stride(16)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true)));
    }

    private static final int FACE_X_POS = 0b100000;
    private static final int FACE_X_NEG = 0b010000;
    private static final int FACE_Y_POS = 0b001000;
    private static final int FACE_Y_NEG = 0b000100;
    private static final int FACE_Z_POS = 0b000010;
    private static final int FACE_Z_NEG = 0b000001;

    private void buildMeshlet(MeshTicketBuilder builder, Meshlet meshlet) {
        int faceCount = 0;
        for (BlockSurface blockSurface : meshlet.blockSurfaces) {
            faceCount += Integer.bitCount(blockSurface.faceMask);
        }

        ByteBuffer vboData = MemoryUtil.memAlloc(faceCount * 4 * ATTRIBUTE_LAYOUT.getFirstStride().getSize());
        ByteBuffer eboData = MemoryUtil.memAlloc(faceCount * 6 * Short.BYTES);

        int vertexBase = 0;
        for (BlockSurface blockSurface : meshlet.blockSurfaces) {
            vertexBase = buildBlockFaces(
                    vboData,
                    eboData,
                    blockSurface.x,
                    blockSurface.y,
                    blockSurface.z,
                    blockSurface.faceMask,
                    blockSurface.color,
                    vertexBase);
        }

        vboData.flip();
        eboData.flip();

        builder.build(vboData, eboData, ATTRIBUTE_LAYOUT, true, true);
    }

    private int buildBlockFaces(
            ByteBuffer vboData,
            ByteBuffer eboData,
            float x, float y, float z,
            int faceMask, int color,
            int vertexBase) {

        byte a = (byte) ((color >> 24) & 0xFF);
        byte r;
        byte g;
        byte b;
        Color color1 = new Color(color, true);

        if ((faceMask & FACE_X_POS) != 0) {
            float factor = 1.1f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_X_NEG) != 0) {
            float factor = 0.8f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Y_POS) != 0) {
            float factor = 1.2f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Y_NEG) != 0) {
            float factor = 0.6f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Z_POS) != 0) {
            float factor = 1.05f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Z_NEG) != 0) {
            float factor = 0.85f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 1)).putShort((short) (vertexBase + 2));
            eboData.putShort((short) vertexBase).putShort((short) (vertexBase + 2)).putShort((short) (vertexBase + 3));
            vertexBase += 4;
        }

        return vertexBase;
    }

    public List<HighLevelDC> getDrawCommands() {
        List<HighLevelDC> list = new ArrayList<>();

        for (Map.Entry<MeshletKey, List<Meshlet>> entry : meshlets.entrySet()) {
            for (Meshlet meshlet : entry.getValue()) {
                String id = "meshlet_" + System.identityHashCode(meshlet); // hashcode based on reference

                graphicResourceManager.requestMeshTicket(id, UploadStrategy.PERSISTENT, 20).ifPresent(builder -> {
                    buildMeshlet(builder, meshlet);
                    graphicResourceManager.submitMeshTicket(builder);
                });

                list.add(HighLevelDC.acquire().fillPassInternal(id, GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_SHORT));
            }
        }

        return list;
    }
}
