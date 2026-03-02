package com.cleanroommc.test.kirino.gl.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class GLTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger LOGGER = LogManager.getLogger("JUnit GL Test");
    private static final ReentrantLock GLOBAL_GL_LOCK = new ReentrantLock(true);

    private static long window;
    private static ExecutorService glThread = null;

    private void initGL() {
        glThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GL Test Thread");
            t.setDaemon(true);
            return t;
        });

        submit(() -> {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!GLFW.glfwInit()) {
                throw new IllegalStateException("\"GLFW.glfwInit\" failed.");
            }

            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

            window = GLFW.glfwCreateWindow(
                    1, 1,
                    "gl-tests",
                    MemoryUtil.NULL,
                    MemoryUtil.NULL);

            if (window == MemoryUtil.NULL) {
                throw new IllegalStateException("\"GLFW.glfwCreateWindow\" failed.");
            }

            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();

            GLFW.glfwSwapInterval(0);

            LOGGER.info("GL context initialized.");

            String rawGLVersion = GL11.glGetString(GL11.GL_VERSION);
            int majorGLVersion = -1;
            int minorGLVersion = -1;

            if (rawGLVersion != null) {
                String[] parts = rawGLVersion.split("\\s+")[0].split("\\.");
                if (parts.length >= 2) {
                    try {
                        majorGLVersion = Integer.parseInt(parts[0]);
                        minorGLVersion = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                rawGLVersion = "";
            }

            LOGGER.info("OpenGL version: {}", rawGLVersion);

            if (rawGLVersion.isEmpty() || majorGLVersion == -1 || minorGLVersion == -1) {
                throw new RuntimeException("Failed to parse the OpenGL version.");
            }

            LOGGER.info("Parsed OpenGL version: {}.{}", majorGLVersion, minorGLVersion);
        }).join();
    }

    private void destroyGL() {
        if (glThread == null) {
            return;
        }

        submit(() -> {
            if (window != MemoryUtil.NULL) {
                GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
                GL.setCapabilities(null);
                GLFW.glfwDestroyWindow(window);
                window = MemoryUtil.NULL;
            }
            GLFW.glfwTerminate();
            LOGGER.info("GL context destroyed.");
        }).join();

        glThread.shutdown();
        try {
            if (!glThread.awaitTermination(2, TimeUnit.SECONDS)) {
                glThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            glThread.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("GL thread shutdown.");
        }
    }

    @Override
    public void beforeAll(@NonNull ExtensionContext context) throws Exception {
        GLOBAL_GL_LOCK.lock();
        try {
            initGL();
        } catch (Throwable t) {
            GLOBAL_GL_LOCK.unlock();
            throw t;
        }
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) throws Exception {
        try {
            destroyGL();
        } finally {
            GLOBAL_GL_LOCK.unlock();
        }
    }

    public static CompletableFuture<Void> submit(Runnable r) {
        Executor executor = glThread;
        if (executor == null) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("GLTestExtension not initialized yet."));
            return f;
        }
        return CompletableFuture.runAsync(r, executor);
    }

    public static <T> CompletableFuture<T> submit(Callable<T> c) {
        Executor executor = glThread;
        if (executor == null) {
            CompletableFuture<T> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("GLTestExtension not initialized yet."));
            return f;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return c.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }
}
