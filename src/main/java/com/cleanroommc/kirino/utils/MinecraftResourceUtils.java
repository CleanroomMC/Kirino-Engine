package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.Loader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;

public final class MinecraftResourceUtils {

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

    @Nullable
    private static String findResource(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);
        Preconditions.checkState(rl.getNamespace().equals("forge"),
                "Provided ResourceLocation \"%s\" must have a forge namespace.", rl.toString());

        File repo;
        try {
            String path = System.getProperty("user.dir");
            File current = new File(path);
            while (current != null && !current.getName().equals("projects")) {
                current = current.getParentFile();
            }
            Preconditions.checkNotNull(current);

            repo = current.getParentFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Preconditions.checkNotNull(repo);

        File resources1 = new File(repo, "src/main/resources/assets/forge");
        File resources2 = new File(repo, "projects/kirino/src/main/resources/assets/forge");
        Preconditions.checkState(resources1.exists() && resources1.isDirectory() && resources2.exists() && resources2.isDirectory(),
                "This is not the dev env.");

        String target = rl.getPath();

        if (target.startsWith("/")) {
            target = target.substring(1);
        }

        File candidate1 = new File(resources1, target);
        if (candidate1.isFile()) {
            return candidate1.getAbsolutePath();
        }

        File candidate2 = new File(resources2, target);
        if (candidate2.isFile()) {
            return candidate2.getAbsolutePath();
        }

        return null;
    }

    private static Boolean devEnv = null;

    private static boolean isDevEnv() {
        if (devEnv != null) {
            return devEnv;
        }

        try {
            String path = System.getProperty("user.dir");
            File current = new File(path);
            while (current != null && !current.getName().equals("projects")) {
                current = current.getParentFile();
            }
            if (current == null) {
                devEnv = false;
                return false;
            }
            File repo = current.getParentFile();
            File resources1 = new File(repo, "src/main/resources");
            File resources2 = new File(repo, "projects/kirino/src/main/resources");
            devEnv = resources1.exists() && resources1.isDirectory() && resources2.exists() && resources2.isDirectory();
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
    public static String readText(@NonNull ResourceLocation rl, @NonNull NewLineType newLine) {
        Preconditions.checkNotNull(rl);
        Preconditions.checkNotNull(newLine);

        InputStream stream;
        if (isDevEnv() && rl.getNamespace().equals("forge")) {
            String path = findResource(rl);
            Preconditions.checkNotNull(path,
                    "Provided ResourceLocation \"%s\" doesn't correspond to an actual file.", rl.toString());

            stream = getInputStream(path);
        } else {
            FMLFolderResourcePack resourcePack = new FMLFolderResourcePack(Loader.instance().getIndexedModList().get(rl.getNamespace()));
            try {
                stream = resourcePack.getInputStream(rl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
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
