package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.mcttf.extract.AssetSource;
import com.google.common.base.Preconditions;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import org.jspecify.annotations.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

final class McResourceManagerAssetSource implements AssetSource {

    private final IResourceManager resourceManager;

    private boolean closed;

    public McResourceManagerAssetSource(@NonNull IResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager);

        this.resourceManager = resourceManager;
    }

    @Override
    public boolean exists(String path) {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        try (IResource ignored = resourceManager.getResource(McAssetPath.parse(path))) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    @NonNull
    public InputStream open(String path) throws IOException {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        IResource resource = resourceManager.getResource(McAssetPath.parse(path));

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

    @Override
    public void close() {
        // NO OP: IResourceManager is owned by Minecraft
        closed = true;
    }
}
