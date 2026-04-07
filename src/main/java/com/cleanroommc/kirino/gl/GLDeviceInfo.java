package com.cleanroommc.kirino.gl;

import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GLCapabilities;

public final class GLDeviceInfo {

    private int versionMajor;
    private int versionMinor;

    private String vendor;
    private String renderer;
    private String version;

    private int maxSsboBindings;
    private int maxSsboBlocksCompute;
    private int maxCombinedSsboBlocks;

    private int maxUboBindings;

    private int maxImageUnits;
    private int maxImageUniformsCompute;
    private int maxCombinedTextureUnits;

    private int maxComputeWorkGroupInvocations;
    private int maxComputeWorkGroupSizeX;
    private int maxComputeWorkGroupSizeY;
    private int maxComputeWorkGroupSizeZ;

    private int maxComputeWorkGroupCountX;
    private int maxComputeWorkGroupCountY;
    private int maxComputeWorkGroupCountZ;

    private int ssboAlignment;
    private int uboAlignment;

    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public String getVendor() {
        return vendor;
    }

    public String getRenderer() {
        return renderer;
    }

    public String getVersion() {
        return version;
    }

    public int getMaxSsboBindings() {
        return maxSsboBindings;
    }

    public int getMaxSsboBlocksCompute() {
        return maxSsboBlocksCompute;
    }

    public int getMaxCombinedSsboBlocks() {
        return maxCombinedSsboBlocks;
    }

    public int getMaxUboBindings() {
        return maxUboBindings;
    }

    public int getMaxImageUnits() {
        return maxImageUnits;
    }

    public int getMaxImageUniformsCompute() {
        return maxImageUniformsCompute;
    }

    public int getMaxCombinedTextureUnits() {
        return maxCombinedTextureUnits;
    }

    public int getMaxComputeWorkGroupInvocations() {
        return maxComputeWorkGroupInvocations;
    }

    public int getMaxComputeWorkGroupSizeX() {
        return maxComputeWorkGroupSizeX;
    }

    public int getMaxComputeWorkGroupSizeY() {
        return maxComputeWorkGroupSizeY;
    }

    public int getMaxComputeWorkGroupSizeZ() {
        return maxComputeWorkGroupSizeZ;
    }

    public int getMaxComputeWorkGroupCountX() {
        return maxComputeWorkGroupCountX;
    }

    public int getMaxComputeWorkGroupCountY() {
        return maxComputeWorkGroupCountY;
    }

    public int getMaxComputeWorkGroupCountZ() {
        return maxComputeWorkGroupCountZ;
    }

    public int getSsboAlignment() {
        return ssboAlignment;
    }

    public int getUboAlignment() {
        return uboAlignment;
    }

    public static GLDeviceInfo captureSnapshot() {
        GLCapabilities caps = org.lwjgl.opengl.GL.getCapabilities();

        GLDeviceInfo info = new GLDeviceInfo();

        String ver = GL11.glGetString(GL11.GL_VERSION);
        info.version = ver;
        info.vendor = GL11.glGetString(GL11.GL_VENDOR);
        info.renderer = GL11.glGetString(GL11.GL_RENDERER);

        parseVersion(ver, info);

        if (caps.OpenGL20) {
            info.maxCombinedTextureUnits = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        }

        if (caps.OpenGL31 || caps.GL_ARB_uniform_buffer_object) {
            info.maxUboBindings = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS);
            info.uboAlignment = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
        }

        if (caps.OpenGL42 || caps.GL_ARB_shader_image_load_store || caps.GL_EXT_shader_image_load_store) {
            info.maxImageUnits = GL11.glGetInteger(GL42.GL_MAX_IMAGE_UNITS);
        }

        if (caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object) {
            info.maxSsboBindings = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
            info.maxCombinedSsboBlocks = GL11.glGetInteger(GL43.GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS);
            info.maxSsboBlocksCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS);
            info.ssboAlignment = GL11.glGetInteger(GL43.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
        }

        if (caps.OpenGL43 || caps.GL_ARB_compute_shader) {
            info.maxImageUniformsCompute = GL11.glGetInteger(GL43.GL_MAX_COMPUTE_IMAGE_UNIFORMS);

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
            ========== GL Device Info ==========
            Vendor   : %s
            Renderer : %s
            Version  : %s (%d.%d)

            -------- Texture --------
            Max Combined Texture Units : %s

            -------- UBO --------
            Max UBO Bindings           : %s
            UBO Offset Alignment       : %s

            -------- Image Load/Store --------
            Max Image Units            : %s
            Max Compute Image Uniforms : %s

            -------- SSBO --------
            Max SSBO Bindings          : %s
            Max Combined SSBO Blocks   : %s
            Max Compute SSBO Blocks    : %s
            SSBO Offset Alignment      : %s

            -------- Compute --------
            Max Workgroup Invocations  : %s
            Max Workgroup Size         : %s x %s x %s
            Max Workgroup Count        : %s x %s x %s

            =====================================
            """.formatted(
                vendor,
                renderer,
                version,
                versionMajor,
                versionMinor,

                maxCombinedTextureUnits > 0 ? maxCombinedTextureUnits : na,

                maxUboBindings > 0 ? maxUboBindings : na,
                uboAlignment > 0 ? uboAlignment : na,

                maxImageUnits > 0 ? maxImageUnits : na,
                maxImageUniformsCompute > 0 ? maxImageUniformsCompute : na,

                maxSsboBindings > 0 ? maxSsboBindings : na,
                maxCombinedSsboBlocks > 0 ? maxCombinedSsboBlocks : na,
                maxSsboBlocksCompute > 0 ? maxSsboBlocksCompute : na,
                ssboAlignment > 0 ? ssboAlignment : na,

                maxComputeWorkGroupInvocations > 0 ? maxComputeWorkGroupInvocations : na,
                maxComputeWorkGroupSizeX > 0 ? maxComputeWorkGroupSizeX : na,
                maxComputeWorkGroupSizeY > 0 ? maxComputeWorkGroupSizeY : na,
                maxComputeWorkGroupSizeZ > 0 ? maxComputeWorkGroupSizeZ : na,
                maxComputeWorkGroupCountX > 0 ? maxComputeWorkGroupCountX : na,
                maxComputeWorkGroupCountY > 0 ? maxComputeWorkGroupCountY : na,
                maxComputeWorkGroupCountZ > 0 ? maxComputeWorkGroupCountZ : na);
    }
}
