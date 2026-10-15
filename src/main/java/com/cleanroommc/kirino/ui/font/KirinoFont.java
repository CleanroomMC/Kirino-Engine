package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.cleanroom.cache.Cache;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Fonts served from the Cleanroom CDN and kept in the global cache.
 */
public enum KirinoFont {

    SOURCE_HAN_SANS_HW(
            "source_han_sans/source_han_sans_hw_vf.ttf",
            "9382849fce0197cfc792b528ac2f9ed26a1034084936f14ad67a4c7bdb19411e");

    private static final String CDN = "https://cdn.cleanroommc.com/kirino/fonts/";

    private final String path;
    private final String sha256;

    KirinoFont(String path, String sha256) {
        this.path = path;
        this.sha256 = sha256;
    }

    /**
     * Blocks while the font is downloaded on first use.
     */
    @NonNull
    public Path fetch() throws IOException {
        return Cache.global()
                .namespace("kirino")
                .fromUrl(URI.create(CDN + path))
                .expectSha256(sha256)
                .get("fonts/" + path);
    }
}
