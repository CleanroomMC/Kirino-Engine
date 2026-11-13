package com.cleanroommc.kirino.engine.render.usage;

import com.google.common.base.Preconditions;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32C;

final class SceneGpuFence {
    interface Driver {
        long insert();
        int poll(long fence);
        void delete(long fence);
        void finish();
    }

    private final Driver driver;
    private long fence;
    private boolean pending;
    private Thread owner;

    SceneGpuFence() {
        this(new Driver() {
            public long insert() { return GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0); }
            public int poll(long handle) { return GL32C.glClientWaitSync(handle, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, 0); }
            public void delete(long handle) { GL32C.glDeleteSync(handle); }
            public void finish() { GL11.glFinish(); }
        });
    }

    SceneGpuFence(Driver driver) { this.driver = driver; }

    void begin() {
        requireThread();
        Preconditions.checkState(!pending, "GPU retirement is already pending.");
        pending = true;
        fence = driver.insert();
        Preconditions.checkState(fence != 0, "Failed to create the scene retirement fence.");
    }

    boolean ready() {
        requireThread();
        if (!pending) {
            return true;
        }
        Preconditions.checkState(fence != 0, "The scene retirement fence is unavailable.");
        int status = driver.poll(fence);
        if (status == GL32C.GL_TIMEOUT_EXPIRED) {
            return false;
        }
        Preconditions.checkState(status == GL32C.GL_ALREADY_SIGNALED || status == GL32C.GL_CONDITION_SATISFIED,
            "Failed to wait for the scene retirement fence.");
        release();
        return true;
    }

    void await() {
        requireThread();
        if (pending) {
            driver.finish();
            release();
        }
    }

    private void requireThread() {
        if (owner == null) {
            owner = Thread.currentThread();
        }
        Preconditions.checkState(Thread.currentThread() == owner, "GPU retirement must run on its owning GL thread.");
    }

    private void release() {
        if (fence != 0) {
            driver.delete(fence);
            fence = 0;
        }
        pending = false;
    }
}
