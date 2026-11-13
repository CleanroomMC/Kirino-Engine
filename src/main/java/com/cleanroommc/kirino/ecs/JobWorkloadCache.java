package com.cleanroommc.kirino.ecs;

import java.util.concurrent.atomic.AtomicReference;

final class JobWorkloadCache {
    static final int MAX_RETAINED_ESTIMATES = 1 << 16;

    private final AtomicReference<JobWorkload> available = new AtomicReference<>(new JobWorkload());

    JobWorkload acquire() {
        JobWorkload workload = available.getAndSet(null);
        return workload != null ? workload : new JobWorkload();
    }

    void release(JobWorkload workload) {
        workload.reset(MAX_RETAINED_ESTIMATES);
        available.compareAndSet(null, workload);
    }

    void clear() {
        available.set(null);
    }
}
