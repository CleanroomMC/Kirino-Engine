package com.cleanroommc.kirino.engine.render.usage.task;

public final class RenderJobDataKeys {
    public static final String STORAGE = "kirino.render.storage";
    public static final String CAMERA = "kirino.render.camera";
    public static final String MAX_LOD_COUNTER = "kirino.render.max_lod_counter";
    public static final String LOD = "kirino.render.lod";
    public static final String WORLD = "kirino.render.world";
    public static final String CHUNK_PROVIDER = "kirino.render.chunk_provider";
    public static final String TEMP_BUFFERS = "kirino.render.temp_buffers";
    public static final String BLOCK_MESH_GENERATOR = "kirino.render.block_mesh_generator";
    public static final String MESHLET_CREATE_CALLBACK = "kirino.render.meshlet_create_callback";
    public static final String MESHLET_DESTROY_CALLBACK = "kirino.render.meshlet_destroy_callback";
    public static final String GIZMOS_MANAGER = "kirino.render.gizmos_manager";
    public static final String MESHLET_GPU_WRITER = "kirino.render.meshlet_gpu_writer";
    public static final String CHUNK_DELTA = "kirino.render.chunk_delta";

    private RenderJobDataKeys() {}
}
