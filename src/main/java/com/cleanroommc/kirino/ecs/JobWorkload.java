package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.job.ParallelJob;

import java.util.Arrays;

final class JobWorkload {
    private static final int[] NO_ESTIMATES = new int[0];

    int uniform;
    int[] estimates = NO_ESTIMATES;

    void reset(int maximumRetainedEstimates) {
        uniform = 0;
        if (estimates.length > maximumRetainedEstimates) {
            estimates = NO_ESTIMATES;
        }
    }

    long prepare(ParallelJob job, PoolRows rows) {
        int liveCount = 0;
        long total = 0;
        int common = 0;
        int rowLimit = rows.limit;
        int[] nextFree = rows.nextFree;
        for (int row = 0; row < rowLimit; row++) {
            if (nextFree[row] != 0) {
                continue;
            }

            int estimate = estimate(job, row);
            if (estimate != common) {
                if (liveCount == 0) {
                    common = estimate;
                } else {
                    uniform = 0;
                    return total + prepareMixed(job, rows, row, liveCount, common, estimate);
                }
            }
            liveCount++;
            total += estimate;
        }
        uniform = common;
        return total;
    }

    private long prepareMixed(ParallelJob job, PoolRows rows, int firstRow, int liveCount,
                              int common, int firstEstimate) {
        int rowLimit = rows.limit;
        int[] nextFree = rows.nextFree;
        if (estimates.length < rowLimit) {
            int required = liveCount;
            for (int row = firstRow; row < rowLimit; row++) {
                if (nextFree[row] == 0) {
                    required++;
                }
            }
            if (estimates.length < required) {
                estimates = new int[required];
            }
        }
        Arrays.fill(estimates, 0, liveCount, common);
        estimates[liveCount++] = firstEstimate;
        long total = firstEstimate;
        int[] values = estimates;
        for (int row = firstRow + 1; row < rowLimit; row++) {
            if (nextFree[row] != 0) {
                continue;
            }

            int estimate = estimate(job, row);
            values[liveCount++] = estimate;
            total += estimate;
        }
        return total;
    }

    private static int estimate(ParallelJob job, int row) {
        int estimate = job.estimateWorkload(row);
        if (estimate < 1) {
            throw new IllegalStateException("The estimated workload at index=" + row
                + " must be greater than or equal to 1. (Parallel job class: " + job.getClass().getName() + ").");
        }

        return estimate;
    }
}
