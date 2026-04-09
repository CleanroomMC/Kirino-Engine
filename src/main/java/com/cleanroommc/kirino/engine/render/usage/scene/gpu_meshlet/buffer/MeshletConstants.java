package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer;

public class MeshletConstants {

    public final static int MESHLET_STRIDE_BYTES = 3616;
    // M * 32 block * 6 face * 4 vertex * 32 (vertex size) = 24576 M = 24 kb per meshlet
    public final static int WORST_CASE_MESHLET_VERTEX_BYTES = 24576;
    // M * 32 block * 6 face * 6 index * 4 (index size) = 4608 M = 4.5 kb per meshlet
    public final static int WORST_CASE_MESHLET_INDEX_BYTES = 4608;
    public final static int WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS = 16384;
}
