package com.cleanroommc.kirino.engine.render.core.resource;

import com.cleanroommc.kirino.engine.render.core.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.core.resource.payload.ResourcePayload;
import com.cleanroommc.kirino.engine.render.core.resource.payload.TexturePayload;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.ResourceReceipt;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.TextureReceipt;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class GResourceTicket<TP extends ResourcePayload<TP>, TR extends ResourceReceipt<TR>> {
    public final GResourceType type;
    public final UploadStrategy uploadStrategy;
    protected GResourceStatus status;
    protected final ResourcePayload<TP> payload;
    protected final ResourceReceipt<TR> receipt;

    private static final int DEFAULT_LIFE = 2;
    protected int defaultLife = DEFAULT_LIFE;
    protected int lifeFrame = defaultLife;

    private @Nullable Runnable expireCallback = null;

    protected final void setExpireCallback(@Nullable Runnable expireCallback) {
        this.expireCallback = expireCallback;
    }

    @NonNull
    protected final Optional<Runnable> getExpireCallback() {
        return Optional.ofNullable(expireCallback);
    }

    protected final void expire() {
        payload.release();
        getExpireCallback().ifPresent(Runnable::run);
        setExpireCallback(null);
    }

    @NonNull
    public final TR getReceipt() {
        return receipt.getReceipt();
    }

    public final boolean isResourceReady() {
        return status == GResourceStatus.GPU_READY;
    }

    public final boolean isExpired() {
        if (status == GResourceStatus.UPLOADING) {
            return false;
        } else {
            return lifeFrame <= 0 || status == GResourceStatus.TO_BE_DESTROYED;
        }
    }

    protected final void tickFrame() {
        if (status == GResourceStatus.GPU_READY) {
            lifeFrame--;
        }
    }

    protected final void keepAlive() {
        lifeFrame = defaultLife;
    }

    public final void setDefaultLife(int defaultLife) {
        this.defaultLife = defaultLife;
        keepAlive();
    }

    public GResourceTicket(@NonNull UploadStrategy uploadStrategy, @NonNull Class<TP> payloadClass, @NonNull Class<TR> receiptClass, @NonNull TP payload, @NonNull TR receipt) {
        Preconditions.checkNotNull(uploadStrategy);
        Preconditions.checkNotNull(payloadClass);
        Preconditions.checkNotNull(receiptClass);
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(payloadClass == MeshPayload.class || payloadClass == TexturePayload.class,
                "Argument payloadClass " + payloadClass.getName() + " is invalid. Must use a built-in payload class.");
        Preconditions.checkArgument(receiptClass == MeshReceipt.class || receiptClass == TextureReceipt.class,
                "Argument receiptClass " + receiptClass.getName() + " is invalid. Must use a built-in receipt class.");

        if (payloadClass == MeshPayload.class) {
            type = GResourceType.MESH;
        } else if (payloadClass == TexturePayload.class) {
            type = GResourceType.TEXTURE;
        } else {
            type = null; // impossible
        }

        GResourceType tempType;
        if (receiptClass == MeshReceipt.class) {
            tempType = GResourceType.MESH;
        } else if (receiptClass == TextureReceipt.class) {
            tempType = GResourceType.TEXTURE;
        } else {
            tempType = null; // impossible
        }

        Preconditions.checkArgument(type == tempType,
                "Arguments payloadClass and receiptClass refer to a different resource type.");

        status = GResourceStatus.CPU_ONLY;
        this.payload = payload;
        this.receipt = receipt;
        this.uploadStrategy = uploadStrategy;
    }
}
