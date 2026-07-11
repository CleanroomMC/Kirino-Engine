package com.cleanroommc.kirino.ecs.component.scan.event;

import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ComponentScanningEvent extends Event {
    private final List<String> scanPackageNames = new ArrayList<>();

    public void register(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);

        scanPackageNames.add(packageName);
    }
}
