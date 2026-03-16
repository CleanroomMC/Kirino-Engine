package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.Loader;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.lang.reflect.Field;

public final class MinecraftResourceUtils {

    private static FMLFolderResourcePack resourcePackDevEnv = null;

    @SuppressWarnings("DataFlowIssue")
    private static FMLFolderResourcePack resourcePackDevEnvHack() {
        if (resourcePackDevEnv != null) {
            return resourcePackDevEnv;
        }

        FMLFolderResourcePack resourcePack = new FMLFolderResourcePack(Loader.instance().getIndexedModList().get("forge"));
        try {
            Field field = ReflectionUtils.findDeclaredField(AbstractResourcePack.class, "resourcePackFile", "field_110597_b");
            field.setAccessible(true);
            File current = (File) field.get(resourcePack);
            while (current != null && !current.getName().equals("projects")) {
                current = current.getParentFile();
            }
            File repo = current.getParentFile();
            File resources = new File(repo, "src/main/resources");
            field.set(resourcePack, resources);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        resourcePackDevEnv = resourcePack;

        return resourcePack;
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
            File resources = new File(repo, "src/main/resources");
            devEnv = resources.exists() && resources.isDirectory();
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
        try {
            FMLFolderResourcePack resourcePack = (isDevEnv() && rl.getNamespace().equals("forge")) ?
                    resourcePackDevEnvHack() : new FMLFolderResourcePack(Loader.instance().getIndexedModList().get(rl.getNamespace()));

            stream = resourcePack.getInputStream(rl);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
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
