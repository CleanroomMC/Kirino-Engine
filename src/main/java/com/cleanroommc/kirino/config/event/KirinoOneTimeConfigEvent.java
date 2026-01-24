package com.cleanroommc.kirino.config.event;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.config.KirinoConfigHub;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;

public class KirinoOneTimeConfigEvent extends Event {

    public KirinoConfigHub.@NonNull RequiresRestart getOneTimeConfig() {
        return MethodHolder.getConfig(KirinoCommonCore.KIRINO_CONFIG_HUB);
    }

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getFieldGetter(KirinoConfigHub.class, "requiresRestart", KirinoConfigHub.RequiresRestart.class));

            Preconditions.checkNotNull(DELEGATE.configGetter);
        }

        static KirinoConfigHub.RequiresRestart getConfig(KirinoConfigHub configHub) {
            KirinoConfigHub.RequiresRestart result;
            try {
                result = (KirinoConfigHub.RequiresRestart) DELEGATE.configGetter.invokeExact(configHub);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(MethodHandle configGetter) {
        }
    }
}
