package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.ISystemExeFlowGraph;
import org.jspecify.annotations.NonNull;

/**
 * It's recommended to utilize the implementations of {@link ISystemExeFlowGraph} to guide the execution of systems in {@link CleanWorld}.
 *
 * @see CleanWorld
 */
public abstract class CleanSystem {
    protected final ExecutionContainer execution = new ExecutionContainer();

    /**
     * <p><b><i>Read this part if you utilized {@link ISystemExeFlowGraph} in a {@link CleanWorld} to coordinate the execution of this system:</i></b></p>
     * <br>
     * <p><code>execution.updateExecutions()</code> must be called during this update so we can keep track of the async tasks started here.</p>
     * <code>execution.updateExecutions()</code> assigns the async futures to this update;
     * <code>execution.noExecutions()</code> clears all futures, which assumes that you didn't start any async tasks.
     * You're supposed to call <code>execution.updateExecutions()</code> if you started any async tasks and <code>execution.noExecutions()</code> will be
     * called automatically by the {@link ISystemExeFlowGraph} before the update.
     * <p>{@link ISystemExeFlowGraph} relies on {@link ExecutionContainer} to work properly.</p>
     *
     * <br>
     * <hr>
     * <p><b><i>Read this part if you didn't utilize {@link ISystemExeFlowGraph}:</i></b></p>
     * <br>
     * If you instantiated a system manually and guide the execution by yourself,
     * then you don't have to worry about {@link ExecutionContainer}.
     * You don't have to call any of <code>execution.updateExecutions()</code> OR <code>execution.noExecutions()</code>,
     * and you're not forced to NOT call them either.
     */
    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
}
