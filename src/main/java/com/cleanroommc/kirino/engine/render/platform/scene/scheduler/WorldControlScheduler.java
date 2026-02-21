package com.cleanroommc.kirino.engine.render.platform.scene.scheduler;

import com.cleanroommc.kirino.engine.render.platform.scene.WorldControl;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.WorldControlFSM;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public final class WorldControlScheduler implements UpdateScheduler {

    public static class NewWorldHint {
        public boolean newWorld;
    }

    public final NewWorldHint newWorldHint = new NewWorldHint();

    private final WorldControlFSM worldFsm;
    private final WorldControl worldControl;

    public WorldControlScheduler(
            WorldControlFSM worldFsm,
            WorldControl worldControl) {

        this.worldFsm = worldFsm;
        this.worldControl = worldControl;
    }

    /**
     * @param payload Must be a non-null {@link NewWorldHint}
     */
    @Override
    public boolean update(@Nullable Object payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkArgument(payload instanceof NewWorldHint,
                "Payload must be an instance of \"WorldControlScheduler.NewWorldHint\".");

        // this flag will be modified inside "process NEW_WORLD_INITIAL_WAIT -> IDLE"
        NewWorldHint hint = (NewWorldHint) payload;
        hint.newWorld = false;

        //<editor-fold desc="process NEW_WORLD_REBUILD -> NEW_WORLD_INITIAL_WAIT">
        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_REBUILD) {
            worldControl.rebuildWorld();
            // finish this update immediately to consume ecs-entity side effects
            return true;
        }
        //</editor-fold>

        //<editor-fold desc="process NEW_WORLD_INITIAL_WAIT -> IDLE">
        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
            worldFsm.next(); // NEW_WORLD_INITIAL_WAIT or IDLE
            if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
                // wait; abort this update
                return true;
            } else {
                hint.newWorld = true; // continue running
            }
        }
        //</editor-fold>

        return false;
    }
}
