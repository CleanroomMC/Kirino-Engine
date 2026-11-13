package com.cleanroommc.kirino.ecs;

import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

public final class EntityAllocationProfile {
    private static final ThreadMXBean ALLOCATIONS = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);
    private static final String[] PHASES = {"enqueueDestroy", "flushDestroy", "enqueueCreate", "flushCreate",
        "enqueueAdd", "flushAdd", "enqueueRemove", "flushRemove"};

    public static void main(String[] args) throws Exception {
        String schema = args.length == 0 ? "small" : args[0];
        int count = args.length < 2 ? 25000 : Integer.parseInt(args[1]);
        if (count < 2 || !ALLOCATIONS.isThreadAllocatedMemorySupported()) {
            throw new IllegalArgumentException("At least two rows and allocation counters are required");
        }
        ALLOCATIONS.setThreadAllocatedMemoryEnabled(true);
        EntityLoadFixture fixture = new EntityLoadFixture(schema);
        EntityManager manager = fixture.manager();
        manager.reserveCapacity(List.of(fixture.type), count);
        CleanEntityHandle[] handles = fixture.populate(manager, count);
        manager.flush();
        EntityLoadFixture.Marker marker = new EntityLoadFixture.Marker();
        long[] allocated = new long[PHASES.length];
        for (int iteration = 0; iteration < 32; iteration++) {
            run(manager, handles, fixture, marker, allocated);
        }
        java.util.Arrays.fill(allocated, 0);
        int iterations = 16;
        for (int iteration = 0; iteration < iterations; iteration++) {
            run(manager, handles, fixture, marker, allocated);
        }
        for (int i = 0; i < PHASES.length; i++) {
            int entities = i < 4 ? count / 2 : count;
            System.out.printf(Locale.ROOT, "%s: %.3f B/batch, %.3f B/entity%n",
                PHASES[i], allocated[i] / (double) iterations, allocated[i] / (double) (iterations * entities));
        }
        ArchetypeDataPool pool = manager.startQuery(manager.newQuery().with(fixture.type)
            .without(EntityLoadFixture.Marker.class)).getFirst();
        var values = pool.getArray(fixture.type, fixture.idField);
        for (CleanEntityHandle handle : handles) {
            if (!handle.isAlive() || handle.contains(EntityLoadFixture.Marker.class)
                || values.getInt(pool.getEntityRow(handle.index)) != 713) {
                throw new IllegalStateException("Profiling changed entity state");
            }
        }
    }

    private static void run(EntityManager manager, CleanEntityHandle[] handles, EntityLoadFixture fixture,
                            EntityLoadFixture.Marker marker, long[] allocated) {
        long before = bytes();
        for (int i = 0; i < handles.length / 2; i++) {
            if (!handles[i].tryDestroy()) throw new IllegalStateException("Destroy rejected");
        }
        allocated[0] += bytes() - before;
        before = bytes();
        manager.flush();
        allocated[1] += bytes() - before;
        before = bytes();
        for (int i = 0; i < handles.length / 2; i++) handles[i] = manager.createEntity(fixture.payload);
        allocated[2] += bytes() - before;
        before = bytes();
        manager.flush();
        allocated[3] += bytes() - before;
        before = bytes();
        for (CleanEntityHandle handle : handles) {
            if (!handle.tryAddComponent(marker)) throw new IllegalStateException("ADD rejected");
        }
        allocated[4] += bytes() - before;
        before = bytes();
        manager.flush();
        allocated[5] += bytes() - before;
        before = bytes();
        for (CleanEntityHandle handle : handles) {
            if (!handle.tryRemoveComponent(EntityLoadFixture.Marker.class)) throw new IllegalStateException("REMOVE rejected");
        }
        allocated[6] += bytes() - before;
        before = bytes();
        manager.flush();
        allocated[7] += bytes() - before;
    }

    private static long bytes() {
        return ALLOCATIONS.getThreadAllocatedBytes(Thread.currentThread().threadId());
    }
}
