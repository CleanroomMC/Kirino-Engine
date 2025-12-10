package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.system.exegraph.ISystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * It helps {@link ISystemExeFlowGraph} to guide the execution flow by providing the future handles.
 *
 * @see ISystemExeFlowGraph#joinSystem(CleanSystem)
 */
public class ExecutionContainer {
    ExecutionContainer() {
    }

    /**
     * Accessed via reflection in {@link ISystemExeFlowGraph.MethodHolder}.
     */
    private final List<JobScheduler.ExecutionHandle> handles = new ArrayList<>();

    /**
     * Accessed via reflection in {@link ISystemExeFlowGraph.MethodHolder}.
     */
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

    public void noExecutions() {
        updateExecutions(null, (JobScheduler.ExecutionHandle[]) null);
    }

    public void updateExecutions(JobScheduler.ExecutionHandle @NonNull ... handles) {
        updateExecutions(null, handles);
    }

    public void updateExecutions(@NonNull CompletableFuture<Void> @Nullable [] futures, JobScheduler.ExecutionHandle @Nullable ... handles) {
        this.handles.clear();
        this.futures.clear();
        if (futures != null) {
            this.futures.addAll(Arrays.asList(futures));
        }
        if (handles != null) {
            this.handles.addAll(Arrays.asList(handles));
        }
    }
}
