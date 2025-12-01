package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.system.graph.SystemExeGraph;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import org.jspecify.annotations.NonNull;

public abstract class CleanSystem {
    protected final ExecutionContainer execution = new ExecutionContainer();

    /**
     * This update is guaranteed to be synchronized, but you can start async tasks here.
     *
     * <br>
     * <hr>
     * <p><b><i>Read this part if you utilized {@link SystemExeGraph} to coordinate the execution.</i></b></p>
     * <br>
     * <p><code>execution.updateExecutions()</code> must be called during this update so we can keep track of the async tasks started here.</p>
     * <code>execution.updateExecutions()</code> assigns the async futures to this update;
     * <code>execution.noExecutions()</code> will be executed by {@link SystemExeGraph} before the update by default, which assumes that you didn't start any async task.
     * <p>{@link SystemExeGraph} relies on {@link ExecutionContainer} to work properly.</p>
     *
     * <br>
     * <hr>
     * <p><b><i>Read this part if you didn't utilize {@link SystemExeGraph}.</i></b></p>
     * <br>
     * If you instantiated a system manually and guide the execution by yourself,
     * then you don't have to worry about {@link ExecutionContainer}.
     * You don't have to call any of <code>execution.updateExecutions()</code> OR <code>execution.noExecutions()</code>,
     * but you're not forced to NOT call them either.
     */
    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
}
