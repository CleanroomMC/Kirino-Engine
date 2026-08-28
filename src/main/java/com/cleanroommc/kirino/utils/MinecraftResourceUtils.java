package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.Loader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;

public final class MinecraftResourceUtils {

    private MinecraftResourceUtils() {
    }

    /**
     * It returns the input stream for a file with the valid absolute path.
     */
    @NonNull
    private static InputStream getInputStream(@NonNull String absolutePath) {
        Preconditions.checkNotNull(absolutePath);

        File file = new File(absolutePath);

        Preconditions.checkState(file.exists() && file.isFile(),
                "File does not exist: %s", absolutePath);

        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * It finds the absolute path for resources inside the dev env.
     *
     * <p>Note: It's only able to find a subset of dev env resources:
     * <code>forge</code> and <code>kirino</code> resources including test data to be exact.</p>
     */
    @Nullable
    private static String findResource(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);
        Preconditions.checkState(rl.getNamespace().equals("forge") || rl.getNamespace().equals("kirino"),
                "Provided ResourceLocation \"%s\" must either be \"forge\" or \"kirino\" namespace.", rl.toString());

        File repo = findCleanroomRoot();
        Preconditions.checkNotNull(repo);

        File resources1 = new File(repo, "module/forge/src/main/resources/assets/forge");
        File resources2 = new File(repo, "module/kirino/src/main/resources/assets/kirino");
        File resources3 = new File(repo, "module/forge/src/test/resources/assets/forge");
        File resources4 = new File(repo, "module/kirino/src/test/resources/assets/kirino");
        Preconditions.checkState(resources1.exists() &&
                        resources1.isDirectory() &&
                        resources2.exists() &&
                        resources2.isDirectory() &&
                        resources3.exists() &&
                        resources3.isDirectory() &&
                        resources4.exists() &&
                        resources4.isDirectory(),
                "This is not the dev env.");

        String target = rl.getPath();

        if (target.startsWith("/")) {
            target = target.substring(1);
        }

        if (rl.getNamespace().equals("forge")) {
            File candidate1 = new File(resources1, target);
            if (candidate1.isFile()) {
                return candidate1.getAbsolutePath();
            }
        }

        if (rl.getNamespace().equals("kirino")) {
            File candidate2 = new File(resources2, target);
            if (candidate2.isFile()) {
                return candidate2.getAbsolutePath();
            }
        }

        if (rl.getNamespace().equals("forge")) {
            File candidate3 = new File(resources3, target);
            if (candidate3.isFile()) {
                return candidate3.getAbsolutePath();
            }
        }

        if (rl.getNamespace().equals("kirino")) {
            File candidate4 = new File(resources4, target);
            if (candidate4.isFile()) {
                return candidate4.getAbsolutePath();
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
            devEnv = resources1.exists() &&
                    resources1.isDirectory() &&
                    resources2.exists() &&
                    resources2.isDirectory() &&
                    resources3.exists() &&
                    resources3.isDirectory() &&
                    resources4.exists() &&
                    resources4.isDirectory();
            return devEnv;
        } catch (Exception e) {
            devEnv = false;
            return false;
        }
    }

    public enum NewLineType {
        BACK_SLASH_N,
        OS_DEPENDENT,
        NONE
    }

    @NonNull
    public static InputStream getInputStream(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        InputStream stream;

        // dev env runtime path
        if (isDevEnv() && (rl.getNamespace().equals("forge") || rl.getNamespace().equals("kirino"))) {
            String path = findResource(rl);
            Preconditions.checkNotNull(path,
                    "Provided ResourceLocation \"%s\" doesn't correspond to an actual file.", rl.toString());

            stream = getInputStream(path);

        // normal runtime path
        } else {
            FMLFolderResourcePack resourcePack = new FMLFolderResourcePack(Loader.instance().getIndexedModList().get(rl.getNamespace()));
            try {
                stream = resourcePack.getInputStream(rl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return stream;
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
