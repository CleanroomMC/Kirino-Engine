package com.cleanroommc.kirino.ui.font;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;

final class McAssetPath {

    private static final String ASSETS_PREFIX = "assets/";

    private McAssetPath() {
    }

    @NonNull
    static ResourceLocation parse(@NonNull String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkArgument(path.startsWith(ASSETS_PREFIX),
                "Expected asset path starting with \"%s\": %s",
                ASSETS_PREFIX,
                path);

        String relative = path.substring(ASSETS_PREFIX.length());
        int separator = relative.indexOf('/');

        Preconditions.checkArgument(separator > 0 && separator < relative.length() - 1,
                "Invalid asset path: %s",
                path);

        return new ResourceLocation(relative.substring(0, separator), relative.substring(separator + 1));
    }
}
