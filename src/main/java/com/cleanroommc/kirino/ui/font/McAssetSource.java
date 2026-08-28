package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.mcttf.extract.AssetSource;
import com.google.common.base.Preconditions;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

final class McAssetSource implements AssetSource {

    private static final String ASSETS_PREFIX = "assets/";

    private final IResourceManager resourceManager;

    private boolean closed;

    public McAssetSource(@NonNull IResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager);

        this.resourceManager = resourceManager;
    }

    @Override
    public boolean exists(String path) {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        try (IResource ignored = resourceManager.getResource(toResourceLocation(path))) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    @NonNull
    public InputStream open(String path) throws IOException {
        Preconditions.checkState(!closed, "Asset source has been closed.");

        IResource resource = resourceManager.getResource(toResourceLocation(path));

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

    @NonNull
    private static ResourceLocation toResourceLocation(@NonNull String path) {
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

        String namespace = relative.substring(0, separator);
        String resourcePath = relative.substring(separator + 1);

        return new ResourceLocation(namespace, resourcePath);
    }
}
