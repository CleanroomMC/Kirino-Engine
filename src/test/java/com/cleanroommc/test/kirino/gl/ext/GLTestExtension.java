package com.cleanroommc.test.kirino.gl.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.Window;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.opentest4j.TestAbortedException;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class GLTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger LOGGER = LogManager.getLogger("JUnit GL Test");
    private static final ReentrantLock GLOBAL_GL_LOCK = new ReentrantLock(true);

    private static Window window;
    private static ExecutorService glThread = null;

    public static Logger logger() {
        return LOGGER;
    }

    private static int versionMajor = -1;
    private static int versionMinor = -1;

    public static int getVersionMajor() {
        return versionMajor;
    }

    public static int getVersionMinor() {
        return versionMinor;
    }

    public static void assumeGL46() {
        Assumptions.assumeTrue(versionMajor == 4 && versionMinor == 6);
    }

    private static boolean initialized = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void assumeInitialized() {
        Assumptions.assumeTrue(initialized);
    }

    private void initGL() {
        versionMajor = -1;
        versionMinor = -1;

        glThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GL Test Thread");
            t.setDaemon(true);
            return t;
        });

        submit(() -> {
            window = SDL.createWindow()
                    .title("gl-tests")
                    .size(1, 1)
                    .hidden()
                    .vsync(false)
                    .openGL()
                    .build();

            LOGGER.info("GL context initialized.");

            String rawGLVersion = GL11.glGetString(GL11.GL_VERSION);

            if (rawGLVersion != null) {
                String[] parts = rawGLVersion.split("\\s+")[0].split("\\.");
                if (parts.length >= 2) {
                    try {
                        versionMajor = Integer.parseInt(parts[0]);
                        versionMinor = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                rawGLVersion = "";
            }

            LOGGER.info("OpenGL version: {}", rawGLVersion);

            if (rawGLVersion.isEmpty() || versionMajor == -1 || versionMinor == -1) {
                throw new RuntimeException("Failed to parse the OpenGL version.");
            }

            LOGGER.info("Parsed OpenGL version: {}.{}", versionMajor, versionMinor);
        }).join();
    }

    private void destroyGL() {
        if (glThread == null) {
            return;
        }

        submit(() -> {
            if (window != null) {
                window.releaseContext();
                GL.setCapabilities(null);
                window.close();
                window = null;
            }
            SDL.shutdown();
            LOGGER.info("GL context destroyed.");
        }).join();

        versionMajor = -1;
        versionMinor = -1;

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
            initialized = false;
            initGL();
            initialized = true;
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize GL test environment.", t);
        }
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) throws Exception {
        try {
            destroyGL();
            initialized = false;
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
        return CompletableFuture.runAsync(() -> {
            try {
                r.run();
            } catch (TestAbortedException ignore) {
            } catch (Throwable e) {
                throw new CompletionException(e);
            }
        }, executor);
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
            } catch (TestAbortedException ignore) {
                return null;
            } catch (Throwable e) {
                throw new CompletionException(e);
            }
        }, executor);
    }
}
