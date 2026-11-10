package com.cleanroommc.kirino.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobWorkloadCacheTest {
    @Test
    void overlappingAcquisitions_neverShareAWorkloadOrItsBuffer() {
        JobWorkloadCache cache = new JobWorkloadCache();
        JobWorkload first = cache.acquire();
        first.estimates = new int[]{11, 12};
        JobWorkload second = cache.acquire();
        second.estimates = new int[]{21, 22};

        assertNotSame(first, second);
        assertNotSame(first.estimates, second.estimates);
        cache.release(first);
        cache.release(second);
        JobWorkload retained = cache.acquire();
        JobWorkload overlapping = cache.acquire();
        assertSame(first, retained);
        assertNotSame(second, overlapping);
        assertNotSame(retained, overlapping);
        assertArrayEquals(new int[]{11, 12}, retained.estimates);
        cache.release(retained);
        cache.release(overlapping);
    }

    @Test
    void release_atRetentionLimit_keepsArrayAndResetsUniformMode() {
        JobWorkloadCache cache = new JobWorkloadCache();
        JobWorkload workload = cache.acquire();
        int[] buffer = new int[JobWorkloadCache.MAX_RETAINED_ESTIMATES];
        workload.estimates = buffer;
        workload.uniform = 42;

        cache.release(workload);

        JobWorkload reused = cache.acquire();
        assertSame(workload, reused);
        assertSame(buffer, reused.estimates);
        assertEquals(0, reused.uniform);
        cache.release(reused);
    }

    @Test
    void release_aboveRetentionLimit_dropsOversizedArray() {
        JobWorkloadCache cache = new JobWorkloadCache();
        JobWorkload workload = cache.acquire();
        workload.estimates = new int[JobWorkloadCache.MAX_RETAINED_ESTIMATES + 1];
        workload.uniform = 42;

        cache.release(workload);

        JobWorkload reused = cache.acquire();
        assertSame(workload, reused);
        assertEquals(0, reused.estimates.length);
        assertEquals(0, reused.uniform);
        cache.release(reused);
    }

    @Test
    void separateExecutors_haveIndependentRetentionSlots() {
        JobWorkloadCache first = new JobWorkloadCache();
        JobWorkloadCache second = new JobWorkloadCache();
        JobWorkload one = first.acquire();
        JobWorkload two = second.acquire();
        one.estimates = new int[]{1};
        two.estimates = new int[]{2};
        first.release(one);
        second.release(two);

        assertSame(one, first.acquire());
        assertSame(two, second.acquire());
        assertNotSame(one.estimates, two.estimates);
    }
}
