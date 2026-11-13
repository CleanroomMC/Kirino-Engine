package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer;

public final class MeshletBufferCapacity {
    private MeshletBufferCapacity() {
    }

    public static int maximumBytes(long deviceLimit) {
        if (deviceLimit < 4) {
            throw new IllegalArgumentException("Invalid GPU buffer limit: " + deviceLimit);
        }
        return (int) (Math.min(deviceLimit, Integer.MAX_VALUE - 8L) & ~3L);
    }

    public static long requiredBytes(int meshletCount, int stride, int padding) {
        if (meshletCount < 0 || stride <= 0 || padding < 0) {
            throw new IllegalArgumentException("Invalid meshlet buffer layout or count");
        }
        return (long) meshletCount * stride + padding;
    }

    public static int checkedBytes(String name, int meshletCount, int stride, int padding, int maximum) {
        long required = requiredBytes(meshletCount, stride, padding);
        if (required > maximum) {
            throw new IllegalStateException(name + " capacity exceeded: meshlets=" + meshletCount
                + ", requiredBytes=" + required + ", maxBytes=" + maximum);
        }
        return (int) required;
    }
}
