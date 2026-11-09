package com.cleanroommc.kirino.ecs.api.system;

import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * It helps {@link SystemExeFlowGraph} to guide the execution flow by providing the future handles.
 *
 */
public final class ExecutionContainer {
    ExecutionContainer() {
    }

    private final List<JobScheduler.ExecutionHandle> handles = new ObjectArrayList<>();

    private final List<CompletableFuture<Void>> futures = new ObjectArrayList<>();

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
