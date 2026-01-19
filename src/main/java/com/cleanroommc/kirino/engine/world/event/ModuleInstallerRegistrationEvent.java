package com.cleanroommc.kirino.engine.world.event;

import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.world.type.WorldKind;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ModuleInstallerRegistrationEvent extends Event {
    private final List<ModuleInstaller<Headless>> headlessInstallers = new ArrayList<>();
    private final List<ModuleInstaller<Graphics>> graphicsInstallers = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <T extends WorldKind> void register(@NonNull Class<T> type, @NonNull ModuleInstaller<T> installer) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(installer);
        Preconditions.checkArgument(type == Headless.class || type == Graphics.class,
                "Argument \"type\" must be either \"Headless\" or \"Graphics\".");

        if (type == Headless.class) {
            headlessInstallers.add((ModuleInstaller<Headless>) installer);
            return;
        }

        if (type == Graphics.class) {
            graphicsInstallers.add((ModuleInstaller<Graphics>) installer);
            return;
        }
    }
}
