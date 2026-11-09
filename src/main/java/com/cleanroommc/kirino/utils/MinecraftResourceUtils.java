package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.util.List;

public final class MinecraftResourceUtils {

    private MinecraftResourceUtils() {
    }

    /**
     * It returns the input stream for a {@link ResourceLocation} when the
     * resource manager is ready.
     */
    @NonNull
    private static InputStream getInputStreamFromResourceManager(@NonNull ResourceLocation rl) throws IOException {
        Preconditions.checkState(!beforeResourceManagerInitialization,
                "Minecraft resource manager isn't ready.");
        Preconditions.checkNotNull(rl);

        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        Preconditions.checkNotNull(resourceManager);

        IResource resource = resourceManager.getResource(new net.minecraft.util.ResourceLocation(rl.namespace(), rl.path()));

        final InputStream input;

        try {
            input = resource.getInputStream();
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable closeError) {
                t.addSuppressed(closeError);
            }
            throw t;
        }

        return new FilterInputStream(input) {

            private boolean streamClosed;

            @Override
            public void close() throws IOException {
                if (streamClosed) {
                    return;
                }

                streamClosed = true;

                IOException failure = null;

                try {
                    super.close();
                } catch (IOException e) {
                    failure = e;
                }

                try {
                    resource.close();
                } catch (IOException e) {
                    if (failure != null) {
                        failure.addSuppressed(e);
                    } else {
                        failure = e;
                    }
                }

                if (failure != null) {
                    throw failure;
                }
            }
        };
    }

    /**
     * It returns the input stream for a file with the valid absolute path.
     */
    @NonNull
    private static InputStream getInputStreamFromPath(@NonNull String absolutePath) throws IOException {
        Preconditions.checkNotNull(absolutePath);

        File file = new File(absolutePath);

        Preconditions.checkState(file.exists() && file.isFile(),
                "File does not exist: %s", absolutePath);

        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * It finds the absolute path for resources inside the dev env.
     *
     * <p>Note: Searches the forge, kirino and cleanroom namespaces across all three modules.</p>
     */
    @Nullable
    private static String findResourceDevEnv(@NonNull String namespace, @NonNull String path) {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(path);
        Preconditions.checkState(namespace.equals("forge") || namespace.equals("kirino") || namespace.equals("cleanroom"),
                "Provided namespace \"%s\" must be \"forge\", \"kirino\" or \"cleanroom\".", namespace);

        File repo = findCleanroomRoot();
        Preconditions.checkNotNull(repo);

        String target = path;

        if (target.startsWith("/")) {
            target = target.substring(1);
        }

        for (String sourceSet : List.of("main", "test")) {
            for (String module : List.of("forge", "kirino", "cleanroom")) {
                File candidate = new File(repo, "module/" + module + "/src/" + sourceSet + "/resources/assets/" + namespace + "/" + target);
                if (candidate.isFile()) {
                    return candidate.getAbsolutePath();
                }
            }
        }

        return null;
    }

    private static Boolean devEnv = null;

    @Nullable
    private static File findCleanroomRoot() {
        File current = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (current != null) {
            if (new File(current, "module/forge/src/main/resources").isDirectory()
                    && new File(current, "module/kirino/src/main/resources").isDirectory()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    /**
     * @return Whether it is the Cleanroom dev env (not Cleanroom mod template but Cleanroom itself)
     */
    private static boolean isDevEnv() {
        if (devEnv != null) {
            return devEnv;
        }

        try {
            File repo = findCleanroomRoot();
            if (repo == null) {
                devEnv = false;
                return false;
            }
            File resources1 = new File(repo, "module/forge/src/main/resources");
            File resources2 = new File(repo, "module/kirino/src/main/resources");
            File resources3 = new File(repo, "module/forge/src/test/resources");
            File resources4 = new File(repo, "module/kirino/src/test/resources");
            File resources5 = new File(repo, "module/cleanroom/src/main/resources");
            File resources6 = new File(repo, "module/cleanroom/src/test/resources");
            devEnv = resources1.exists() &&
                    resources1.isDirectory() &&
                    resources2.exists() &&
                    resources2.isDirectory() &&
                    resources3.exists() &&
                    resources3.isDirectory() &&
                    resources4.exists() &&
                    resources4.isDirectory() &&
                    resources5.exists() &&
                    resources5.isDirectory() &&
                    resources6.exists() &&
                    resources6.isDirectory();
            return devEnv;
        } catch (Exception e) {
            devEnv = false;
            return false;
        }
    }

    private static boolean beforeResourceManagerInitialization = true;

    /**
     * <p>Note: Must not be called by clients!</p>
     */
    public static void resourceManagerInitializationFinished() {
        beforeResourceManagerInitialization = false;
    }

    public enum NewLineType {
        BACK_SLASH_N,
        OS_DEPENDENT,
        NONE
    }

    /**
     * Minecraft agnostic.
     */
    @NonNull
    public static InputStream getEarlyInputStream(net.minecraft.util.@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        return getEarlyInputStream(new ResourceLocation(rl.getNamespace(), rl.getPath()));
    }

    /**
     * Minecraft agnostic.
     */
    @NonNull
    public static InputStream getEarlyInputStream(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        String namespace = rl.namespace();
        String path = rl.path();

        if (isDevEnv() && (namespace.equals("forge") || namespace.equals("kirino") || namespace.equals("cleanroom"))) {
            String absolutePath = findResourceDevEnv(namespace, path);
            Preconditions.checkNotNull(absolutePath,
                    "Resource \"%s:%s\" doesn't correspond to an actual file.", namespace, path);

            try {
                return getInputStreamFromPath(absolutePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String target = path.startsWith("/") ? path.substring(1) : path;
        String resource = "/assets/" + namespace + "/" + target;
        InputStream stream = MinecraftResourceUtils.class.getResourceAsStream(resource);
        return Preconditions.checkNotNull(stream,
                "Resource \"%s:%s\" doesn't correspond to an actual classpath resource \"%s\".",
                namespace, path, resource);
    }

    @NonNull
    public static InputStream getInputStream(net.minecraft.util.@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        return getInputStream(new ResourceLocation(rl.getNamespace(), rl.getPath()));
    }

    @NonNull
    public static InputStream getInputStream(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        InputStream stream;

        if (beforeResourceManagerInitialization || (isDevEnv() && (rl.namespace().equals("forge") || rl.namespace().equals("kirino") || rl.namespace().equals("cleanroom")))) {
            stream = getEarlyInputStream(rl);

        // normal path
        } else {
            try {
                stream = getInputStreamFromResourceManager(rl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return stream;
    }

    @NonNull
    public static String readText(net.minecraft.util.@NonNull ResourceLocation rl, @NonNull NewLineType newLine) {
        Preconditions.checkNotNull(rl);

        return readText(new ResourceLocation(rl.getNamespace(), rl.getPath()), newLine);
    }

    @NonNull
    public static String readText(@NonNull ResourceLocation rl, @NonNull NewLineType newLine) {
        Preconditions.checkNotNull(rl);
        Preconditions.checkNotNull(newLine);

        try (InputStream stream = getInputStream(rl)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                switch (newLine) {
                    case BACK_SLASH_N -> builder.append('\n');
                    case OS_DEPENDENT -> builder.append(System.lineSeparator());
                }
            }
            reader.close();
            return builder.toString();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
