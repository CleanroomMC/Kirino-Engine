package com.cleanroommc.kirino.ecs.job.event;

import com.cleanroommc.kirino.ecs.job.IParallelJob;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class JobRegistrationEvent extends Event {
    private final List<Class<? extends IParallelJob>> parallelJobClasses = new ArrayList<>();

    public void register(@NonNull Class<? extends IParallelJob> parallelJobClass) {
        parallelJobClasses.add(parallelJobClass);
    }
}
