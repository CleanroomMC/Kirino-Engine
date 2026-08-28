package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.mcttf.extract.AssetSource;
import com.google.common.base.Preconditions;
import net.minecraft.client.resources.IResourcePack;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;

final class McResourcePackAssetSource implements AssetSource {

    private final IResourcePack resourcePack;

    private boolean closed;

    public McResourcePackAssetSource(@NonNull IResourcePack resourcePack) {
        Preconditions.checkNotNull(resourcePack);

        this.resourcePack = resourcePack;
    }

    @Override
    public boolean exists(String path) {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        return resourcePack.resourceExists(McAssetPath.parse(path));
    }

    @Override
    @NonNull
    public InputStream open(String path) throws IOException {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        return resourcePack.getInputStream(McAssetPath.parse(path));
    }

    @Override
    public void close() {
        // NO OP: IResourcePack is externally owned
        closed = true;
    }
}
