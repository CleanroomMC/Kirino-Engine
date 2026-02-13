package com.cleanroommc.kirino.ecs.job.event;

import com.cleanroommc.kirino.ecs.job.ParallelJob;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class JobRegistrationEvent extends Event {
    private final List<Class<? extends ParallelJob>> parallelJobClasses = new ArrayList<>();

    public void register(@NonNull Class<? extends ParallelJob> parallelJobClass) {
        parallelJobClasses.add(parallelJobClass);
    }
}
