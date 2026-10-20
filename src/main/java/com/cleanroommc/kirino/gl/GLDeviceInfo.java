package com.cleanroommc.kirino.gl;

import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GLCapabilities;

public final class GLDeviceInfo {

    private int versionMajor;
    private int versionMinor;

    public boolean isVersionAtLeast(int major, int minor) {
        return versionMajor > major || (versionMajor == major && versionMinor >= minor);
    }

    private String vendor;
    private String renderer;
    private String version;

    private int maxTextureSize;
    private int max3dTextureSize;
    private int maxCubeMapTextureSize;
    private int maxArrayTextureLayers;
    private int maxRectangleTextureSize;
    private int maxTextureBufferSize;
    private int textureBufferAlignment;
    private float maxTextureLodBias;
    private float maxTextureAnisotropy;
    private int maxTextureUnitsVertex;
    private int maxTextureUnitsFragment;
    private int maxTextureUnitsGeometry;
    private int maxTextureUnitsTessControl;
    private int maxTextureUnitsTessEvaluation;
    private int maxTextureUnitsCompute;
    private int maxCombinedTextureUnits;

    private int maxColorAttachments;
    private int maxDrawBuffers;
    private int maxRenderbufferSize;
    private int maxSamples;
    private int maxColorTextureSamples;
    private int maxDepthTextureSamples;
    private int maxIntegerSamples;
    private int maxFramebufferWidth;
    private int maxFramebufferHeight;
    private int maxFramebufferLayers;
    private int maxFramebufferSamples;
    private int maxViewportWidth;
    private int maxViewportHeight;
    private int maxViewports;

    private int maxVertexAttribs;
    private int maxVertexAttribBindings;
    private int maxVertexAttribRelativeOffset;
    private int maxVertexAttribStride;

    private int maxUniformComponentsVertex;
    private int maxUniformComponentsFragment;
    private int maxUniformComponentsGeometry;
    private int maxUniformComponentsTessControl;
    private int maxUniformComponentsTessEvaluation;
    private int maxUniformComponentsCompute;
    private int maxVaryingComponents;
    private int maxUniformLocations;
    private int maxGeometryOutputVertices;
    private int maxGeometryTotalOutputComponents;
    private int maxPatchVertices;
    private int maxTessGenLevel;

    private int maxUboBindings;
    private int maxUboBlockSize;
    private int maxCombinedUboBlocks;
    private int maxUboBlocksVertex;
    private int maxUboBlocksFragment;
    private int maxUboBlocksGeometry;
    private int maxUboBlocksTessControl;
    private int maxUboBlocksTessEvaluation;
    private int maxUboBlocksCompute;
    private int uboAlignment;

    private int maxImageUnits;
    private int maxImageSamples;
    private int maxCombinedImageUniforms;
    private int maxCombinedImageUnitsAndFragmentOutputs;
    private int maxImageUniformsVertex;
    private int maxImageUniformsFragment;
    private int maxImageUniformsGeometry;
    private int maxImageUniformsTessControl;
    private int maxImageUniformsTessEvaluation;
    private int maxImageUniformsCompute;

    private int maxSsboBindings;
    private long maxSsboBlockSize;
    private int maxCombinedSsboBlocks;
    private int maxSsboBlocksVertex;
    private int maxSsboBlocksFragment;
    private int maxSsboBlocksGeometry;
    private int maxSsboBlocksTessControl;
    private int maxSsboBlocksTessEvaluation;
    private int maxSsboBlocksCompute;
    private int ssboAlignment;

    private int maxComputeSharedMemorySize;
    private int maxComputeWorkGroupInvocations;
    private int maxComputeWorkGroupSizeX;
    private int maxComputeWorkGroupSizeY;
    private int maxComputeWorkGroupSizeZ;
    private int maxComputeWorkGroupCountX;
    private int maxComputeWorkGroupCountY;
    private int maxComputeWorkGroupCountZ;

    private int minMapBufferAlignment;

    /**
     * <code>GL_VERSION</code>: Major OpenGL version number parsed from the version string.
     */
    public int getVersionMajor() {
        return versionMajor;
    }

    /**
     * <code>GL_VERSION</code>: Minor OpenGL version number parsed from the version string.
     */
    public int getVersionMinor() {
        return versionMinor;
    }

    /**
     * <code>GL_VENDOR</code>: OpenGL implementation vendor name.
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * <code>GL_RENDERER</code>: Renderer name reported by the driver.
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * <code>GL_VERSION</code>: Full OpenGL version string reported by the driver.
     */
    public String getVersion() {
        return version;
    }

    /**
     * <code>GL_MAX_TEXTURE_SIZE</code>: Maximum width or height of a 1D or 2D texture, in texels.
     */
    public int getMaxTextureSize() {
        return maxTextureSize;
    }

    /**
     * <code>GL_MAX_3D_TEXTURE_SIZE</code>: Maximum width, height, or depth of a 3D texture, in texels.
     */
    public int getMax3dTextureSize() {
        return max3dTextureSize;
    }

    /**
     * <code>GL_MAX_CUBE_MAP_TEXTURE_SIZE</code>: Maximum width or height of a cubemap face, in texels.
     */
    public int getMaxCubeMapTextureSize() {
        return maxCubeMapTextureSize;
    }

    /**
     * <code>GL_MAX_ARRAY_TEXTURE_LAYERS</code>: Maximum number of layers in a 1D or 2D array texture.
     */
    public int getMaxArrayTextureLayers() {
        return maxArrayTextureLayers;
    }

    /**
     * <code>GL_MAX_RECTANGLE_TEXTURE_SIZE</code>: Maximum width or height of a rectangle texture, in texels.
     */
    public int getMaxRectangleTextureSize() {
        return maxRectangleTextureSize;
    }

    /**
     * <code>GL_MAX_TEXTURE_BUFFER_SIZE</code>: Maximum number of addressable texels in a buffer texture.
     */
    public int getMaxTextureBufferSize() {
        return maxTextureBufferSize;
    }

    /**
     * <code>GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT</code>: Required byte alignment of buffer texture range offsets.
     */
    public int getTextureBufferAlignment() {
        return textureBufferAlignment;
    }

    /**
     * <code>GL_MAX_TEXTURE_LOD_BIAS</code>: Maximum supported magnitude of texture LOD bias.
     */
    public float getMaxTextureLodBias() {
        return maxTextureLodBias;
    }

    /**
     * <code>GL_MAX_TEXTURE_MAX_ANISOTROPY</code>: Maximum supported texture anisotropy factor.
     */
    public float getMaxTextureAnisotropy() {
        return maxTextureAnisotropy;
    }

    /**
     * <code>GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a vertex shader.
     */
    public int getMaxTextureUnitsVertex() {
        return maxTextureUnitsVertex;
    }

    /**
     * <code>GL_MAX_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a fragment shader.
     */
    public int getMaxTextureUnitsFragment() {
        return maxTextureUnitsFragment;
    }

    /**
     * <code>GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a geometry shader.
     */
    public int getMaxTextureUnitsGeometry() {
        return maxTextureUnitsGeometry;
    }

    /**
     * <code>GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a tessellation control shader.
     */
    public int getMaxTextureUnitsTessControl() {
        return maxTextureUnitsTessControl;
    }

    /**
     * <code>GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a tessellation evaluation shader.
     */
    public int getMaxTextureUnitsTessEvaluation() {
        return maxTextureUnitsTessEvaluation;
    }

    /**
     * <code>GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS</code>: Maximum number of texture image units accessible to a compute shader.
     */
    public int getMaxTextureUnitsCompute() {
        return maxTextureUnitsCompute;
    }

    /**
     * <code>GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS</code>: Maximum combined texture image-unit usage across all shader stages.
     * A unit accessed by multiple stages counts separately for each stage.
     */
    public int getMaxCombinedTextureUnits() {
        return maxCombinedTextureUnits;
    }

    /**
     * <code>GL_MAX_COLOR_ATTACHMENTS</code>: Maximum number of color attachment slots in a framebuffer.
     */
    public int getMaxColorAttachments() {
        return maxColorAttachments;
    }

    /**
     * <code>GL_MAX_DRAW_BUFFERS</code>: Maximum number of draw buffers a fragment shader can write to.
     */
    public int getMaxDrawBuffers() {
        return maxDrawBuffers;
    }

    /**
     * <code>GL_MAX_RENDERBUFFER_SIZE</code>: Maximum width or height of a renderbuffer, in pixels.
     */
    public int getMaxRenderbufferSize() {
        return maxRenderbufferSize;
    }

    /**
     * <code>GL_MAX_SAMPLES</code>: General sample-count limit for multisample renderbuffers.
     * Integer formats also use <code>GL_MAX_INTEGER_SAMPLES</code>; query format-specific sample counts separately.
     */
    public int getMaxSamples() {
        return maxSamples;
    }

    /**
     * <code>GL_MAX_COLOR_TEXTURE_SAMPLES</code>: General sample-count limit for color multisample textures.
     * Integer formats also use <code>GL_MAX_INTEGER_SAMPLES</code>; query format-specific sample counts separately.
     */
    public int getMaxColorTextureSamples() {
        return maxColorTextureSamples;
    }

    /**
     * <code>GL_MAX_DEPTH_TEXTURE_SAMPLES</code>: General sample-count limit for depth/stencil multisample textures.
     * Query format-specific sample counts separately.
     */
    public int getMaxDepthTextureSamples() {
        return maxDepthTextureSamples;
    }

    /**
     * <code>GL_MAX_INTEGER_SAMPLES</code>: General sample-count limit for signed and unsigned integer multisample textures and renderbuffers.
     * Query format-specific sample counts separately.
     */
    public int getMaxIntegerSamples() {
        return maxIntegerSamples;
    }

    /**
     * <code>GL_MAX_FRAMEBUFFER_WIDTH</code>: Maximum framebuffer object width, in pixels.
     */
    public int getMaxFramebufferWidth() {
        return maxFramebufferWidth;
    }

    /**
     * <code>GL_MAX_FRAMEBUFFER_HEIGHT</code>: Maximum framebuffer object height, in pixels.
     */
    public int getMaxFramebufferHeight() {
        return maxFramebufferHeight;
    }

    /**
     * <code>GL_MAX_FRAMEBUFFER_LAYERS</code>: Maximum layer count of a layered framebuffer object.
     */
    public int getMaxFramebufferLayers() {
        return maxFramebufferLayers;
    }

    /**
     * <code>GL_MAX_FRAMEBUFFER_SAMPLES</code>: Maximum sample count for a framebuffer object, including its attachments.
     */
    public int getMaxFramebufferSamples() {
        return maxFramebufferSamples;
    }

    /**
     * <code>GL_MAX_VIEWPORT_DIMS</code> (element 0): Maximum viewport width, in pixels.
     */
    public int getMaxViewportWidth() {
        return maxViewportWidth;
    }

    /**
     * <code>GL_MAX_VIEWPORT_DIMS</code> (element 1): Maximum viewport height, in pixels.
     */
    public int getMaxViewportHeight() {
        return maxViewportHeight;
    }

    /**
     * <code>GL_MAX_VIEWPORTS</code>: Maximum number of simultaneous viewports.
     */
    public int getMaxViewports() {
        return maxViewports;
    }

    /**
     * <code>GL_MAX_VERTEX_ATTRIBS</code>: Maximum number of generic vertex attribute slots.
     */
    public int getMaxVertexAttribs() {
        return maxVertexAttribs;
    }

    /**
     * <code>GL_MAX_VERTEX_ATTRIB_BINDINGS</code>: Maximum number of vertex buffer binding slots in a vertex array.
     */
    public int getMaxVertexAttribBindings() {
        return maxVertexAttribBindings;
    }

    /**
     * <code>GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET</code>: Maximum attribute byte offset relative to its vertex buffer binding.
     */
    public int getMaxVertexAttribRelativeOffset() {
        return maxVertexAttribRelativeOffset;
    }

    /**
     * <code>GL_MAX_VERTEX_ATTRIB_STRIDE</code>: Maximum vertex buffer stride, in bytes.
     */
    public int getMaxVertexAttribStride() {
        return maxVertexAttribStride;
    }

    /**
     * <code>GL_MAX_VERTEX_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a vertex shader, in components.
     */
    public int getMaxUniformComponentsVertex() {
        return maxUniformComponentsVertex;
    }

    /**
     * <code>GL_MAX_FRAGMENT_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a fragment shader, in components.
     */
    public int getMaxUniformComponentsFragment() {
        return maxUniformComponentsFragment;
    }

    /**
     * <code>GL_MAX_GEOMETRY_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a geometry shader, in components.
     */
    public int getMaxUniformComponentsGeometry() {
        return maxUniformComponentsGeometry;
    }

    /**
     * <code>GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a tessellation control shader, in components.
     */
    public int getMaxUniformComponentsTessControl() {
        return maxUniformComponentsTessControl;
    }

    /**
     * <code>GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a tessellation evaluation shader, in components.
     */
    public int getMaxUniformComponentsTessEvaluation() {
        return maxUniformComponentsTessEvaluation;
    }

    /**
     * <code>GL_MAX_COMPUTE_UNIFORM_COMPONENTS</code>: Maximum default-block uniform storage for a compute shader, in components.
     */
    public int getMaxUniformComponentsCompute() {
        return maxUniformComponentsCompute;
    }

    /**
     * <code>GL_MAX_VARYING_FLOATS</code>: Component limit for vertex outputs or fragment inputs in a program
     * containing only vertex and fragment shaders, excluding <code>gl_Position</code>.
     * This constant aliases <code>GL_MAX_VARYING_COMPONENTS</code>.
     */
    public int getMaxVaryingComponents() {
        return maxVaryingComponents;
    }

    /**
     * <code>GL_MAX_UNIFORM_LOCATIONS</code>: Maximum number of default-block uniform locations in a program.
     */
    public int getMaxUniformLocations() {
        return maxUniformLocations;
    }

    /**
     * <code>GL_MAX_GEOMETRY_OUTPUT_VERTICES</code>: Maximum number of vertices emitted by one geometry shader invocation.
     */
    public int getMaxGeometryOutputVertices() {
        return maxGeometryOutputVertices;
    }

    /**
     * <code>GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS</code>: Maximum total output-component budget per geometry shader invocation.
     * The declared maximum output vertex count multiplied by the active per-vertex output component count must fit this budget.
     */
    public int getMaxGeometryTotalOutputComponents() {
        return maxGeometryTotalOutputComponents;
    }

    /**
     * <code>GL_MAX_PATCH_VERTICES</code>: Maximum number of vertices in an input patch or a tessellation control shader output patch.
     */
    public int getMaxPatchVertices() {
        return maxPatchVertices;
    }

    /**
     * <code>GL_MAX_TESS_GEN_LEVEL</code>: Maximum supported tessellation subdivision level.
     */
    public int getMaxTessGenLevel() {
        return maxTessGenLevel;
    }

    /**
     * <code>GL_MAX_UNIFORM_BUFFER_BINDINGS</code>: Maximum number of uniform buffer binding slots.
     */
    public int getMaxUboBindings() {
        return maxUboBindings;
    }

    /**
     * <code>GL_MAX_UNIFORM_BLOCK_SIZE</code>: Maximum size of one uniform block, in bytes.
     */
    public int getMaxUboBlockSize() {
        return maxUboBlockSize;
    }

    /**
     * <code>GL_MAX_COMBINED_UNIFORM_BLOCKS</code>: Maximum combined active uniform-block usage across all shader stages.
     * A block referenced by multiple stages counts separately for each stage.
     */
    public int getMaxCombinedUboBlocks() {
        return maxCombinedUboBlocks;
    }

    /**
     * <code>GL_MAX_VERTEX_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a vertex shader.
     */
    public int getMaxUboBlocksVertex() {
        return maxUboBlocksVertex;
    }

    /**
     * <code>GL_MAX_FRAGMENT_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a fragment shader.
     */
    public int getMaxUboBlocksFragment() {
        return maxUboBlocksFragment;
    }

    /**
     * <code>GL_MAX_GEOMETRY_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a geometry shader.
     */
    public int getMaxUboBlocksGeometry() {
        return maxUboBlocksGeometry;
    }

    /**
     * <code>GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a tessellation control shader.
     */
    public int getMaxUboBlocksTessControl() {
        return maxUboBlocksTessControl;
    }

    /**
     * <code>GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a tessellation evaluation shader.
     */
    public int getMaxUboBlocksTessEvaluation() {
        return maxUboBlocksTessEvaluation;
    }

    /**
     * <code>GL_MAX_COMPUTE_UNIFORM_BLOCKS</code>: Maximum number of uniform blocks accessed by a compute shader.
     */
    public int getMaxUboBlocksCompute() {
        return maxUboBlocksCompute;
    }

    /**
     * <code>GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT</code>: Required byte alignment of uniform buffer range offsets.
     */
    public int getUboAlignment() {
        return uboAlignment;
    }

    /**
     * <code>GL_MAX_IMAGE_UNITS</code>: Maximum number of image load/store binding slots.
     */
    public int getMaxImageUnits() {
        return maxImageUnits;
    }

    /**
     * <code>GL_MAX_IMAGE_SAMPLES</code>: Maximum sample count of a texture level supported for image loads, stores, and atomics.
     */
    public int getMaxImageSamples() {
        return maxImageSamples;
    }

    /**
     * <code>GL_MAX_COMBINED_IMAGE_UNIFORMS</code>: Maximum combined active image-uniform usage across all shader stages.
     * An image uniform accessed by multiple stages counts separately for each stage.
     */
    public int getMaxCombinedImageUniforms() {
        return maxCombinedImageUniforms;
    }

    /**
     * <code>GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS</code>: Maximum sum of active image uniforms
     * across all shaders, active shader storage blocks when supported, and active fragment shader outputs.
     * With shader storage buffer support, this constant aliases <code>GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES</code>.
     */
    public int getMaxCombinedImageUnitsAndFragmentOutputs() {
        return maxCombinedImageUnitsAndFragmentOutputs;
    }

    /**
     * <code>GL_MAX_VERTEX_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a vertex shader.
     */
    public int getMaxImageUniformsVertex() {
        return maxImageUniformsVertex;
    }

    /**
     * <code>GL_MAX_FRAGMENT_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a fragment shader.
     */
    public int getMaxImageUniformsFragment() {
        return maxImageUniformsFragment;
    }

    /**
     * <code>GL_MAX_GEOMETRY_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a geometry shader.
     */
    public int getMaxImageUniformsGeometry() {
        return maxImageUniformsGeometry;
    }

    /**
     * <code>GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a tessellation control shader.
     */
    public int getMaxImageUniformsTessControl() {
        return maxImageUniformsTessControl;
    }

    /**
     * <code>GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a tessellation evaluation shader.
     */
    public int getMaxImageUniformsTessEvaluation() {
        return maxImageUniformsTessEvaluation;
    }

    /**
     * <code>GL_MAX_COMPUTE_IMAGE_UNIFORMS</code>: Maximum number of active image uniforms in a compute shader.
     */
    public int getMaxImageUniformsCompute() {
        return maxImageUniformsCompute;
    }

    /**
     * <code>GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS</code>: Maximum number of shader storage buffer binding slots.
     */
    public int getMaxSsboBindings() {
        return maxSsboBindings;
    }

    /**
     * <code>GL_MAX_SHADER_STORAGE_BLOCK_SIZE</code>: Maximum size of one shader storage block, in bytes.
     */
    public long getMaxSsboBlockSize() {
        return maxSsboBlockSize;
    }

    /**
     * <code>GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS</code>: Maximum combined active shader-storage-block usage across all shader stages.
     * A block referenced by multiple stages counts separately for each stage.
     */
    public int getMaxCombinedSsboBlocks() {
        return maxCombinedSsboBlocks;
    }

    /**
     * <code>GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a vertex shader.
     */
    public int getMaxSsboBlocksVertex() {
        return maxSsboBlocksVertex;
    }

    /**
     * <code>GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a fragment shader.
     */
    public int getMaxSsboBlocksFragment() {
        return maxSsboBlocksFragment;
    }

    /**
     * <code>GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a geometry shader.
     */
    public int getMaxSsboBlocksGeometry() {
        return maxSsboBlocksGeometry;
    }

    /**
     * <code>GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a tessellation control shader.
     */
    public int getMaxSsboBlocksTessControl() {
        return maxSsboBlocksTessControl;
    }

    /**
     * <code>GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a tessellation evaluation shader.
     */
    public int getMaxSsboBlocksTessEvaluation() {
        return maxSsboBlocksTessEvaluation;
    }

    /**
     * <code>GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS</code>: Maximum number of shader storage blocks accessed by a compute shader.
     */
    public int getMaxSsboBlocksCompute() {
        return maxSsboBlocksCompute;
    }

    /**
     * <code>GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT</code>: Required byte alignment of shader storage buffer range offsets.
     */
    public int getSsboAlignment() {
        return ssboAlignment;
    }

    /**
     * <code>GL_MAX_COMPUTE_SHARED_MEMORY_SIZE</code>: Maximum total storage for shared variables in one compute program, in bytes.
     * Each workgroup has its own instance of this shared storage.
     */
    public int getMaxComputeSharedMemorySize() {
        return maxComputeSharedMemorySize;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS</code>: Maximum total number of invocations in one compute workgroup.
     */
    public int getMaxComputeWorkGroupInvocations() {
        return maxComputeWorkGroupInvocations;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_SIZE</code> (index 0): Maximum number of invocations along the X axis of a compute workgroup.
     */
    public int getMaxComputeWorkGroupSizeX() {
        return maxComputeWorkGroupSizeX;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_SIZE</code> (index 1): Maximum number of invocations along the Y axis of a compute workgroup.
     */
    public int getMaxComputeWorkGroupSizeY() {
        return maxComputeWorkGroupSizeY;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_SIZE</code> (index 2): Maximum number of invocations along the Z axis of a compute workgroup.
     */
    public int getMaxComputeWorkGroupSizeZ() {
        return maxComputeWorkGroupSizeZ;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_COUNT</code> (index 0): Maximum number of compute workgroups dispatched along the X axis in one dispatch.
     */
    public int getMaxComputeWorkGroupCountX() {
        return maxComputeWorkGroupCountX;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_COUNT</code> (index 1): Maximum number of compute workgroups dispatched along the Y axis in one dispatch.
     */
    public int getMaxComputeWorkGroupCountY() {
        return maxComputeWorkGroupCountY;
    }

    /**
     * <code>GL_MAX_COMPUTE_WORK_GROUP_COUNT</code> (index 2): Maximum number of compute workgroups dispatched along the Z axis in one dispatch.
     */
    public int getMaxComputeWorkGroupCountZ() {
        return maxComputeWorkGroupCountZ;
    }

    /**
     * <code>GL_MIN_MAP_BUFFER_ALIGNMENT</code>: Minimum guaranteed byte alignment of mapped buffer memory.
     * For a range mapping, the returned pointer minus the mapped byte offset is aligned to this value.
     */
    public int getMinMapBufferAlignment() {
        return minMapBufferAlignment;
    }

    /**
     * <p>Note: Must capture the snapshot on the thread that owns a GL context.</p>
     */
    @NonNull
    public static GLDeviceInfo captureSnapshot() {
        GLCapabilities caps = org.lwjgl.opengl.GL.getCapabilities();

        GLDeviceInfo info = new GLDeviceInfo();

        String ver = GL11.glGetString(GL11.GL_VERSION);
        info.version = ver;
        info.vendor = GL11.glGetString(GL11.GL_VENDOR);
        info.renderer = GL11.glGetString(GL11.GL_RENDERER);

        parseVersion(ver, info);

        boolean geometry = caps.OpenGL32 || caps.GL_ARB_geometry_shader4 || caps.GL_EXT_geometry_shader4;
        boolean tessellation = caps.OpenGL40 || caps.GL_ARB_tessellation_shader;
        boolean compute = caps.OpenGL43 || caps.GL_ARB_compute_shader;

        String legacyExtensions = "";
        if (!caps.OpenGL14) {
            String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
            if (extensions != null) {
                legacyExtensions = " " + extensions + " ";
            }
        }

        info.maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);

        int[] viewportDims = new int[2];
        GL11.glGetIntegerv(GL11.GL_MAX_VIEWPORT_DIMS, viewportDims);
        info.maxViewportWidth = viewportDims[0];
        info.maxViewportHeight = viewportDims[1];

        if (caps.OpenGL12 || legacyExtensions.contains(" GL_EXT_texture3D ")) {
            info.max3dTextureSize = GL11.glGetInteger(GL12.GL_MAX_3D_TEXTURE_SIZE);
        }

        if (caps.OpenGL13 || caps.GL_ARB_texture_cube_map || legacyExtensions.contains(" GL_EXT_texture_cube_map ")) {
            info.maxCubeMapTextureSize = GL11.glGetInteger(GL13.GL_MAX_CUBE_MAP_TEXTURE_SIZE);
        }

        if (caps.OpenGL14 || legacyExtensions.contains(" GL_EXT_texture_lod_bias ")) {
            info.maxTextureLodBias = GL11.glGetFloat(GL14.GL_MAX_TEXTURE_LOD_BIAS);
        }

        if (caps.OpenGL30 || caps.GL_EXT_texture_array) {
            info.maxArrayTextureLayers = GL11.glGetInteger(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS);
        }

        if (caps.OpenGL31 || caps.GL_ARB_texture_rectangle) {
            info.maxRectangleTextureSize = GL11.glGetInteger(GL31.GL_MAX_RECTANGLE_TEXTURE_SIZE);
        }

        if (caps.OpenGL31 || caps.GL_ARB_texture_buffer_object || caps.GL_EXT_texture_buffer_object) {
            info.maxTextureBufferSize = GL11.glGetInteger(GL31.GL_MAX_TEXTURE_BUFFER_SIZE);
        }

        if (caps.OpenGL43 || caps.GL_ARB_texture_buffer_range) {
            info.textureBufferAlignment = GL11.glGetInteger(GL43.GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
        }

        if (caps.OpenGL46 || caps.GL_ARB_texture_filter_anisotropic || caps.GL_EXT_texture_filter_anisotropic) {
            info.maxTextureAnisotropy = GL11.glGetFloat(GL46.GL_MAX_TEXTURE_MAX_ANISOTROPY);
        }

        if (caps.OpenGL20 || caps.GL_ARB_vertex_shader || caps.GL_ARB_vertex_program) {
            info.maxVertexAttribs = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
        }

        if (caps.OpenGL20 || caps.GL_ARB_vertex_shader) {
            info.maxTextureUnitsVertex = GL11.glGetInteger(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
            info.maxCombinedTextureUnits = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
            info.maxUniformComponentsVertex = GL11.glGetInteger(GL20.GL_MAX_VERTEX_UNIFORM_COMPONENTS);
            info.maxVaryingComponents = GL11.glGetInteger(GL20.GL_MAX_VARYING_FLOATS);
        }

        if (caps.OpenGL20 || caps.GL_ARB_fragment_shader || caps.GL_ARB_fragment_program || caps.GL_ARB_vertex_shader) {
            info.maxTextureUnitsFragment = GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS);
        }

        if (caps.OpenGL20 || caps.GL_ARB_fragment_shader) {
            info.maxUniformComponentsFragment = GL11.glGetInteger(GL20.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS);
        }

        if (caps.OpenGL20 || caps.GL_ARB_draw_buffers) {
            info.maxDrawBuffers = GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS);
        }

        if (caps.OpenGL30 || caps.GL_ARB_framebuffer_object || caps.GL_EXT_framebuffer_object) {
            info.maxColorAttachments = GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS);
            info.maxRenderbufferSize = GL11.glGetInteger(GL30.GL_MAX_RENDERBUFFER_SIZE);
        }

        if (caps.OpenGL30 || caps.GL_ARB_framebuffer_object || caps.GL_EXT_framebuffer_multisample) {
            info.maxSamples = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
        }

        if (caps.OpenGL32 || caps.GL_ARB_texture_multisample) {
            info.maxColorTextureSamples = GL11.glGetInteger(GL32.GL_MAX_COLOR_TEXTURE_SAMPLES);
            info.maxDepthTextureSamples = GL11.glGetInteger(GL32.GL_MAX_DEPTH_TEXTURE_SAMPLES);
            info.maxIntegerSamples = GL11.glGetInteger(GL32.GL_MAX_INTEGER_SAMPLES);
        }

        if (caps.OpenGL43 || caps.GL_ARB_framebuffer_no_attachments) {
            info.maxFramebufferWidth = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_WIDTH);
            info.maxFramebufferHeight = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_HEIGHT);
            info.maxFramebufferLayers = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_LAYERS);
            info.maxFramebufferSamples = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_SAMPLES);
        }

        if (caps.OpenGL41 || caps.GL_ARB_viewport_array) {
            info.maxViewports = GL11.glGetInteger(GL41.GL_MAX_VIEWPORTS);
        }

        if (caps.OpenGL43 || caps.GL_ARB_vertex_attrib_binding) {
            info.maxVertexAttribBindings = GL11.glGetInteger(GL43.GL_MAX_VERTEX_ATTRIB_BINDINGS);
            info.maxVertexAttribRelativeOffset = GL11.glGetInteger(GL43.GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET);
        }

        if (caps.OpenGL44) {
            info.maxVertexAttribStride = GL11.glGetInteger(GL44.GL_MAX_VERTEX_ATTRIB_STRIDE);
        }

        if (caps.OpenGL43 || caps.GL_ARB_explicit_uniform_location) {
            info.maxUniformLocations = GL11.glGetInteger(GL43.GL_MAX_UNIFORM_LOCATIONS);
        }

        if (geometry) {
            info.maxTextureUnitsGeometry = GL11.glGetInteger(GL32.GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS);
            info.maxUniformComponentsGeometry = GL11.glGetInteger(GL32.GL_MAX_GEOMETRY_UNIFORM_COMPONENTS);
            info.maxGeometryOutputVertices = GL11.glGetInteger(GL32.GL_MAX_GEOMETRY_OUTPUT_VERTICES);
            info.maxGeometryTotalOutputComponents = GL11.glGetInteger(GL32.GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS);
        }

        if (tessellation) {
            info.maxTextureUnitsTessControl = GL11.glGetInteger(GL40.GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS);
            info.maxTextureUnitsTessEvaluation = GL11.glGetInteger(GL40.GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS);
            info.maxUniformComponentsTessControl = GL11.glGetInteger(GL40.GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS);
            info.maxUniformComponentsTessEvaluation = GL11.glGetInteger(GL40.GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS);
            info.maxPatchVertices = GL11.glGetInteger(GL40.GL_MAX_PATCH_VERTICES);
            info.maxTessGenLevel = GL11.glGetInteger(GL40.GL_MAX_TESS_GEN_LEVEL);
        }

        if (caps.OpenGL31 || caps.GL_ARB_uniform_buffer_object) {
            info.maxUboBindings = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS);
            info.maxUboBlockSize = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BLOCK_SIZE);
            info.maxCombinedUboBlocks = GL11.glGetInteger(GL31.GL_MAX_COMBINED_UNIFORM_BLOCKS);
            info.maxUboBlocksVertex = GL11.glGetInteger(GL31.GL_MAX_VERTEX_UNIFORM_BLOCKS);
            info.maxUboBlocksFragment = GL11.glGetInteger(GL31.GL_MAX_FRAGMENT_UNIFORM_BLOCKS);
            info.uboAlignment = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
            if (caps.OpenGL32 || caps.GL_ARB_geometry_shader4) {
                info.maxUboBlocksGeometry = GL11.glGetInteger(GL31.GL_MAX_GEOMETRY_UNIFORM_BLOCKS);
            }
            if (tessellation) {
                info.maxUboBlocksTessControl = GL11.glGetInteger(GL40.GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS);
                info.maxUboBlocksTessEvaluation = GL11.glGetInteger(GL40.GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS);
            }
            if (compute) {
                info.maxUboBlocksCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_UNIFORM_BLOCKS);
            }
        }

        if (caps.OpenGL42 || caps.GL_ARB_shader_image_load_store || caps.GL_EXT_shader_image_load_store) {
            info.maxImageUnits = GL11.glGetInteger(GL42.GL_MAX_IMAGE_UNITS);
            info.maxImageSamples = GL11.glGetInteger(GL42.GL_MAX_IMAGE_SAMPLES);
        }

        if (caps.OpenGL42 || caps.GL_ARB_shader_image_load_store || caps.GL_EXT_shader_image_load_store || caps.GL_ARB_shader_storage_buffer_object) {
            info.maxCombinedImageUnitsAndFragmentOutputs = GL11.glGetInteger(GL42.GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS);
        }

        if (caps.OpenGL42 || caps.GL_ARB_shader_image_load_store) {
            info.maxCombinedImageUniforms = GL11.glGetInteger(GL42.GL_MAX_COMBINED_IMAGE_UNIFORMS);
            info.maxImageUniformsVertex = GL11.glGetInteger(GL42.GL_MAX_VERTEX_IMAGE_UNIFORMS);
            info.maxImageUniformsFragment = GL11.glGetInteger(GL42.GL_MAX_FRAGMENT_IMAGE_UNIFORMS);
            if (geometry) {
                info.maxImageUniformsGeometry = GL11.glGetInteger(GL42.GL_MAX_GEOMETRY_IMAGE_UNIFORMS);
            }
            if (tessellation) {
                info.maxImageUniformsTessControl = GL11.glGetInteger(GL42.GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS);
                info.maxImageUniformsTessEvaluation = GL11.glGetInteger(GL42.GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS);
            }
        }

        if (caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object) {
            info.maxSsboBindings = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
            info.maxSsboBlockSize = GL32.glGetInteger64(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
            info.maxCombinedSsboBlocks = GL11.glGetInteger(GL43.GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS);
            info.maxSsboBlocksVertex = GL11.glGetInteger(GL43.GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
            info.maxSsboBlocksFragment = GL11.glGetInteger(GL43.GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS);
            info.ssboAlignment = GL11.glGetInteger(GL43.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
            if (geometry) {
                info.maxSsboBlocksGeometry = GL11.glGetInteger(GL43.GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS);
            }
            if (tessellation) {
                info.maxSsboBlocksTessControl = GL11.glGetInteger(GL43.GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS);
                info.maxSsboBlocksTessEvaluation = GL11.glGetInteger(GL43.GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS);
            }
            if (compute) {
                info.maxSsboBlocksCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS);
            }
        }

        if (compute) {
            info.maxTextureUnitsCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS);
            info.maxUniformComponentsCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_UNIFORM_COMPONENTS);
            info.maxImageUniformsCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_IMAGE_UNIFORMS);
            info.maxComputeSharedMemorySize = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_SHARED_MEMORY_SIZE);
            info.maxComputeWorkGroupInvocations = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS);

            int[] temp = new int[1];
            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, temp);
            info.maxComputeWorkGroupSizeX = temp[0];
            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, temp);
            info.maxComputeWorkGroupSizeY = temp[0];
            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, temp);
            info.maxComputeWorkGroupSizeZ = temp[0];

            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, temp);
            info.maxComputeWorkGroupCountX = temp[0];
            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, temp);
            info.maxComputeWorkGroupCountY = temp[0];
            GL30C.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, temp);
            info.maxComputeWorkGroupCountZ = temp[0];
        }

        if (caps.OpenGL42 || caps.GL_ARB_map_buffer_alignment) {
            info.minMapBufferAlignment = GL11.glGetInteger(GL42.GL_MIN_MAP_BUFFER_ALIGNMENT);
        }

        return info;
    }

    private static void parseVersion(String version, GLDeviceInfo info) {
        int majorGLVersion = -1;
        int minorGLVersion = -1;

        if (version != null) {
            String[] parts = version.split("\\s+")[0].split("\\.");
            if (parts.length >= 2) {
                try {
                    majorGLVersion = Integer.parseInt(parts[0]);
                    minorGLVersion = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        } else {
            throw new RuntimeException("Failed to parse the OpenGL version.");
        }

        if (majorGLVersion == -1 || minorGLVersion == -1) {
            throw new RuntimeException("Failed to parse the OpenGL version.");
        }

        info.versionMajor = majorGLVersion;
        info.versionMinor = minorGLVersion;
    }

    @Override
    public String toString() {
        String na = "N/A";
        return """
            ==================== GL Device Info ====================
            (vendor) Vendor                                                              : %s
            (renderer) Renderer                                                          : %s
            (version) Version                                                            : %s
            (versionMajor) Major Version                                                 : %d
            (versionMinor) Minor Version                                                 : %d

            ---------------- Texture ----------------
            (maxTextureSize) Max Texture Size                                            : %s
            (max3dTextureSize) Max 3D Texture Size                                       : %s
            (maxCubeMapTextureSize) Max Cube Map Texture Size                            : %s
            (maxArrayTextureLayers) Max Array Texture Layers                             : %s
            (maxRectangleTextureSize) Max Rectangle Texture Size                         : %s
            (maxTextureBufferSize) Max Texture Buffer Texels                             : %s
            (textureBufferAlignment) Texture Buffer Offset Alignment                     : %s
            (maxTextureLodBias) Max Texture LOD Bias                                     : %s
            (maxTextureAnisotropy) Max Texture Anisotropy                                : %s
            (maxTextureUnitsVertex) Max Vertex Texture Units                             : %s
            (maxTextureUnitsFragment) Max Fragment Texture Units                         : %s
            (maxTextureUnitsGeometry) Max Geometry Texture Units                         : %s
            (maxTextureUnitsTessControl) Max TessControl Texture Units                   : %s
            (maxTextureUnitsTessEvaluation) Max TessEvaluation Texture Units             : %s
            (maxTextureUnitsCompute) Max Compute Texture Units                           : %s
            (maxCombinedTextureUnits) Max Combined Texture Units                         : %s

            ---------------- Framebuffer / Multisampling ----------------
            (maxColorAttachments) Max Color Attachments                                  : %s
            (maxDrawBuffers) Max Draw Buffers                                            : %s
            (maxRenderbufferSize) Max Renderbuffer Size                                  : %s
            (maxSamples) Max Renderbuffer Samples                                        : %s
            (maxColorTextureSamples) Max Color Texture Samples                           : %s
            (maxDepthTextureSamples) Max Depth Texture Samples                           : %s
            (maxIntegerSamples) Max Integer Samples                                      : %s
            (maxFramebufferWidth) Max Framebuffer Width                                  : %s
            (maxFramebufferHeight) Max Framebuffer Height                                : %s
            (maxFramebufferLayers) Max Framebuffer Layers                                : %s
            (maxFramebufferSamples) Max Framebuffer Samples                              : %s
            (maxViewportWidth) Max Viewport Width                                        : %s
            (maxViewportHeight) Max Viewport Height                                      : %s
            (maxViewports) Max Viewports                                                 : %s

            ---------------- Vertex Input ----------------
            (maxVertexAttribs) Max Vertex Attributes                                     : %s
            (maxVertexAttribBindings) Max Vertex Attribute Bindings                      : %s
            (maxVertexAttribRelativeOffset) Max Vertex Relative Offset                   : %s
            (maxVertexAttribStride) Max Vertex Attribute Stride                          : %s

            ---------------- Shader ----------------
            (maxUniformComponentsVertex) Max Vertex Uniform Components                   : %s
            (maxUniformComponentsFragment) Max Fragment Uniform Components               : %s
            (maxUniformComponentsGeometry) Max Geometry Uniform Components               : %s
            (maxUniformComponentsTessControl) Max TessControl Uniform Components         : %s
            (maxUniformComponentsTessEvaluation) Max TessEvaluation Uniform Components   : %s
            (maxUniformComponentsCompute) Max Compute Uniform Components                 : %s
            (maxVaryingComponents) Max Varying Components                                : %s
            (maxUniformLocations) Max Uniform Locations                                  : %s
            (maxGeometryOutputVertices) Max Geometry Output Vertices                     : %s
            (maxGeometryTotalOutputComponents) Max Geometry Total Output Components      : %s
            (maxPatchVertices) Max Patch Vertices                                        : %s
            (maxTessGenLevel) Max Tessellation Generation Level                          : %s

            ---------------- UBO ----------------
            (maxUboBindings) Max UBO Bindings                                            : %s
            (maxUboBlockSize) Max UBO Block Size (bytes)                                 : %s
            (maxCombinedUboBlocks) Max Combined UBO Blocks                               : %s
            (maxUboBlocksVertex) Max Vertex UBO Blocks                                   : %s
            (maxUboBlocksFragment) Max Fragment UBO Blocks                               : %s
            (maxUboBlocksGeometry) Max Geometry UBO Blocks                               : %s
            (maxUboBlocksTessControl) Max TessControl UBO Blocks                         : %s
            (maxUboBlocksTessEvaluation) Max TessEvaluation UBO Blocks                   : %s
            (maxUboBlocksCompute) Max Compute UBO Blocks                                 : %s
            (uboAlignment) UBO Offset Alignment                                          : %s

            ---------------- Image Load/Store ----------------
            (maxImageUnits) Max Image Units                                              : %s
            (maxImageSamples) Max Image Samples                                          : %s
            (maxCombinedImageUniforms) Max Combined Image Uniforms                       : %s
            (maxCombinedImageUnitsAndFragmentOutputs) Max Image Units + Fragment Outputs : %s
            (maxImageUniformsVertex) Max Vertex Image Uniforms                           : %s
            (maxImageUniformsFragment) Max Fragment Image Uniforms                       : %s
            (maxImageUniformsGeometry) Max Geometry Image Uniforms                       : %s
            (maxImageUniformsTessControl) Max TessControl Image Uniforms                 : %s
            (maxImageUniformsTessEvaluation) Max TessEvaluation Image Uniforms           : %s
            (maxImageUniformsCompute) Max Compute Image Uniforms                         : %s

            ---------------- SSBO ----------------
            (maxSsboBindings) Max SSBO Bindings                                          : %s
            (maxSsboBlockSize) Max SSBO Block Size (bytes)                               : %s
            (maxCombinedSsboBlocks) Max Combined SSBO Blocks                             : %s
            (maxSsboBlocksVertex) Max Vertex SSBO Blocks                                 : %s
            (maxSsboBlocksFragment) Max Fragment SSBO Blocks                             : %s
            (maxSsboBlocksGeometry) Max Geometry SSBO Blocks                             : %s
            (maxSsboBlocksTessControl) Max TessControl SSBO Blocks                       : %s
            (maxSsboBlocksTessEvaluation) Max TessEvaluation SSBO Blocks                 : %s
            (maxSsboBlocksCompute) Max Compute SSBO Blocks                               : %s
            (ssboAlignment) SSBO Offset Alignment                                        : %s

            ---------------- Compute ----------------
            (maxComputeSharedMemorySize) Max Shared Memory Size (bytes)                  : %s
            (maxComputeWorkGroupInvocations) Max Workgroup Invocations                   : %s
            (maxComputeWorkGroupSizeX) Max Workgroup Size X                              : %s
            (maxComputeWorkGroupSizeY) Max Workgroup Size Y                              : %s
            (maxComputeWorkGroupSizeZ) Max Workgroup Size Z                              : %s
            (maxComputeWorkGroupCountX) Max Workgroup Count X                            : %s
            (maxComputeWorkGroupCountY) Max Workgroup Count Y                            : %s
            (maxComputeWorkGroupCountZ) Max Workgroup Count Z                            : %s

            ---------------- Buffer Mapping ----------------
            (minMapBufferAlignment) Min Map Buffer Alignment                             : %s

            ========================================================
            """.formatted(
                vendor,
                renderer,
                version,
                versionMajor,
                versionMinor,

                maxTextureSize > 0 ? maxTextureSize : na,
                max3dTextureSize > 0 ? max3dTextureSize : na,
                maxCubeMapTextureSize > 0 ? maxCubeMapTextureSize : na,
                maxArrayTextureLayers > 0 ? maxArrayTextureLayers : na,
                maxRectangleTextureSize > 0 ? maxRectangleTextureSize : na,
                maxTextureBufferSize > 0 ? maxTextureBufferSize : na,
                textureBufferAlignment > 0 ? textureBufferAlignment : na,
                maxTextureLodBias > 0 ? maxTextureLodBias : na,
                maxTextureAnisotropy > 0 ? maxTextureAnisotropy : na,
                maxTextureUnitsVertex > 0 ? maxTextureUnitsVertex : na,
                maxTextureUnitsFragment > 0 ? maxTextureUnitsFragment : na,
                maxTextureUnitsGeometry > 0 ? maxTextureUnitsGeometry : na,
                maxTextureUnitsTessControl > 0 ? maxTextureUnitsTessControl : na,
                maxTextureUnitsTessEvaluation > 0 ? maxTextureUnitsTessEvaluation : na,
                maxTextureUnitsCompute > 0 ? maxTextureUnitsCompute : na,
                maxCombinedTextureUnits > 0 ? maxCombinedTextureUnits : na,

                maxColorAttachments > 0 ? maxColorAttachments : na,
                maxDrawBuffers > 0 ? maxDrawBuffers : na,
                maxRenderbufferSize > 0 ? maxRenderbufferSize : na,
                maxSamples > 0 ? maxSamples : na,
                maxColorTextureSamples > 0 ? maxColorTextureSamples : na,
                maxDepthTextureSamples > 0 ? maxDepthTextureSamples : na,
                maxIntegerSamples > 0 ? maxIntegerSamples : na,
                maxFramebufferWidth > 0 ? maxFramebufferWidth : na,
                maxFramebufferHeight > 0 ? maxFramebufferHeight : na,
                maxFramebufferLayers > 0 ? maxFramebufferLayers : na,
                maxFramebufferSamples > 0 ? maxFramebufferSamples : na,
                maxViewportWidth > 0 ? maxViewportWidth : na,
                maxViewportHeight > 0 ? maxViewportHeight : na,
                maxViewports > 0 ? maxViewports : na,

                maxVertexAttribs > 0 ? maxVertexAttribs : na,
                maxVertexAttribBindings > 0 ? maxVertexAttribBindings : na,
                maxVertexAttribRelativeOffset > 0 ? maxVertexAttribRelativeOffset : na,
                maxVertexAttribStride > 0 ? maxVertexAttribStride : na,

                maxUniformComponentsVertex > 0 ? maxUniformComponentsVertex : na,
                maxUniformComponentsFragment > 0 ? maxUniformComponentsFragment : na,
                maxUniformComponentsGeometry > 0 ? maxUniformComponentsGeometry : na,
                maxUniformComponentsTessControl > 0 ? maxUniformComponentsTessControl : na,
                maxUniformComponentsTessEvaluation > 0 ? maxUniformComponentsTessEvaluation : na,
                maxUniformComponentsCompute > 0 ? maxUniformComponentsCompute : na,
                maxVaryingComponents > 0 ? maxVaryingComponents : na,
                maxUniformLocations > 0 ? maxUniformLocations : na,
                maxGeometryOutputVertices > 0 ? maxGeometryOutputVertices : na,
                maxGeometryTotalOutputComponents > 0 ? maxGeometryTotalOutputComponents : na,
                maxPatchVertices > 0 ? maxPatchVertices : na,
                maxTessGenLevel > 0 ? maxTessGenLevel : na,

                maxUboBindings > 0 ? maxUboBindings : na,
                maxUboBlockSize > 0 ? maxUboBlockSize : na,
                maxCombinedUboBlocks > 0 ? maxCombinedUboBlocks : na,
                maxUboBlocksVertex > 0 ? maxUboBlocksVertex : na,
                maxUboBlocksFragment > 0 ? maxUboBlocksFragment : na,
                maxUboBlocksGeometry > 0 ? maxUboBlocksGeometry : na,
                maxUboBlocksTessControl > 0 ? maxUboBlocksTessControl : na,
                maxUboBlocksTessEvaluation > 0 ? maxUboBlocksTessEvaluation : na,
                maxUboBlocksCompute > 0 ? maxUboBlocksCompute : na,
                uboAlignment > 0 ? uboAlignment : na,

                maxImageUnits > 0 ? maxImageUnits : na,
                maxImageSamples > 0 ? maxImageSamples : na,
                maxCombinedImageUniforms > 0 ? maxCombinedImageUniforms : na,
                maxCombinedImageUnitsAndFragmentOutputs > 0 ? maxCombinedImageUnitsAndFragmentOutputs : na,
                maxImageUniformsVertex > 0 ? maxImageUniformsVertex : na,
                maxImageUniformsFragment > 0 ? maxImageUniformsFragment : na,
                maxImageUniformsGeometry > 0 ? maxImageUniformsGeometry : na,
                maxImageUniformsTessControl > 0 ? maxImageUniformsTessControl : na,
                maxImageUniformsTessEvaluation > 0 ? maxImageUniformsTessEvaluation : na,
                maxImageUniformsCompute > 0 ? maxImageUniformsCompute : na,

                maxSsboBindings > 0 ? maxSsboBindings : na,
                maxSsboBlockSize > 0 ? maxSsboBlockSize : na,
                maxCombinedSsboBlocks > 0 ? maxCombinedSsboBlocks : na,
                maxSsboBlocksVertex > 0 ? maxSsboBlocksVertex : na,
                maxSsboBlocksFragment > 0 ? maxSsboBlocksFragment : na,
                maxSsboBlocksGeometry > 0 ? maxSsboBlocksGeometry : na,
                maxSsboBlocksTessControl > 0 ? maxSsboBlocksTessControl : na,
                maxSsboBlocksTessEvaluation > 0 ? maxSsboBlocksTessEvaluation : na,
                maxSsboBlocksCompute > 0 ? maxSsboBlocksCompute : na,
                ssboAlignment > 0 ? ssboAlignment : na,

                maxComputeSharedMemorySize > 0 ? maxComputeSharedMemorySize : na,
                maxComputeWorkGroupInvocations > 0 ? maxComputeWorkGroupInvocations : na,
                maxComputeWorkGroupSizeX > 0 ? maxComputeWorkGroupSizeX : na,
                maxComputeWorkGroupSizeY > 0 ? maxComputeWorkGroupSizeY : na,
                maxComputeWorkGroupSizeZ > 0 ? maxComputeWorkGroupSizeZ : na,
                maxComputeWorkGroupCountX > 0 ? maxComputeWorkGroupCountX : na,
                maxComputeWorkGroupCountY > 0 ? maxComputeWorkGroupCountY : na,
                maxComputeWorkGroupCountZ > 0 ? maxComputeWorkGroupCountZ : na,

                minMapBufferAlignment > 0 ? minMapBufferAlignment : na);
    }
}
