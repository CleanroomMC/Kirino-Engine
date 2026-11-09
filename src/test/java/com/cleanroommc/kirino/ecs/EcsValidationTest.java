package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobScheduler;

import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.config.KirinoConfig;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EcsValidationTest {
    @TempDir
    Path temporaryDirectory;
    private static final String LONG_FIELD = "field".repeat(30);

    @Test
    void fieldOrdinals_validLongPathsAndInvalidPaths_preserveResolutionAndErrors() {
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("leaf", Leaf.class, new MemberLayout(LONG_FIELD),
            new StructDef(new FieldDef(ScalarType.INT)));
        structs.registerStructType("nested", Nested.class, new MemberLayout("leaf"),
            new StructDef(new FieldDef("leaf")));
        assertEquals(0, structs.getFieldOrdinal("nested", "leaf", LONG_FIELD));
        assertThrows(IllegalArgumentException.class, () -> structs.getFieldOrdinal("nested"));
        var error = assertThrows(IllegalArgumentException.class,
            () -> structs.getFieldOrdinal("leaf", LONG_FIELD + "missing"));
        assertTrue(error.getMessage().contains(LONG_FIELD + "missing"));
        assertThrows(NullPointerException.class, () -> structs.getFieldOrdinal("nested", new String[]{null}));

        FieldRegistry fields = new FieldRegistry(structs);
        fields.registerFieldType("integer", int.class, new FieldDef(ScalarType.INT));
        fields.registerFieldType("vector", Vector3f.class, new FieldDef(ScalarType.VEC3));
        fields.registerFieldType("leafField", Leaf.class, new FieldDef("leaf"));
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("test", TestComponent.class, new MemberLayout("id", "position", "leaf"),
            "integer", "vector", "leafField");
        assertEquals(0, components.getFieldOrdinal("test", "id"));
        assertEquals(3, components.getFieldOrdinal("test", "position", "z"));
        assertEquals(4, components.getFieldOrdinal("test", "leaf", LONG_FIELD));
        assertThrows(IllegalArgumentException.class, () -> components.getFieldOrdinal("test"));
        assertThrows(IllegalArgumentException.class, () -> components.getFieldOrdinal("missing", "id"));
        assertThrows(IllegalArgumentException.class, () -> components.getFieldOrdinal("test", "absent"));
        assertThrows(IllegalArgumentException.class, () -> components.getFieldOrdinal("test", "id", "x"));
        assertThrows(IllegalArgumentException.class, () -> components.getFieldOrdinal("test", "position", "invalid"));
    }

    @Test
    void componentAndFieldConversions_validAndMissingTypes_preserveBehavior() {
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        fields.registerFieldType("integer", int.class, new FieldDef(ScalarType.INT));
        fields.registerFieldType("vector", Vector3f.class, new FieldDef(ScalarType.VEC3));
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("test", TestComponent.class, new MemberLayout("id", "position"), "integer", "vector");
        fields.lock();
        components.lock();
        Object[] values = {9, 1.5f, -2f, 3.25f};
        TestComponent component = assertInstanceOf(TestComponent.class, components.newComponent("test", values));
        assertArrayEquals(values, components.flattenComponent(component));
        assertEquals(42, fields.newField("integer", 42));
        assertArrayEquals(new Object[]{42}, fields.flattenField(42));
        assertNull(fields.newField("missing", 42));
        assertNull(components.newComponent("missing", 42));
        assertThrows(IllegalArgumentException.class, () -> fields.flattenField("unregistered"));
        assertThrows(IllegalArgumentException.class, () -> components.flattenComponent(new MissingComponent()));
        assertThrows(NullPointerException.class, () -> fields.newField("integer", new Object[]{null}));
        assertThrows(NullPointerException.class, () -> components.newComponent("test", new Object[]{null}));
    }

    @Test
    void scalarConversions_allTypes_preserveRoundTripAndTypeValidation() {
        Object[][] values = {
            {(byte) 1}, {(short) 2}, {3}, {4L}, {1.5f}, {2.5d}, {true},
            {1f, 2f}, {1f, 2f, 3f}, {1f, 2f, 3f, 4f},
            {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f},
            {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f}
        };
        ScalarType[] types = ScalarType.values();
        assertEquals(types.length, values.length);
        for (int i = 0; i < types.length; i++) {
            ScalarType type = types[i];
            Object value = type.newScalar(values[i]);
            assertNotNull(value, type.name());
            assertArrayEquals(values[i], type.flattenScalar(value), type.name());
            assertEquals(values[i].length, FlattenedScalarType.flattenedUnitCount(type));
            assertEquals(values[i].length, FlattenedScalarType.flatten(type).size());
            assertThrows(IllegalArgumentException.class, () -> type.flattenScalar("wrong"));
            assertEquals(-1, type.ordinalOffsetOfField("missing"));
            assertThrows(NullPointerException.class, () -> type.ordinalOffsetOfField(null));
        }
        assertThrows(NullPointerException.class, () -> FlattenedScalarType.flatten(null));
        assertThrows(NullPointerException.class, () -> FlattenedScalarType.flattenedUnitCount(null));
    }

    @Test
    void entityAccess_validAndInvalidIndexes_preserveBoundsChecks() {
        EntityManager entities = new EntityManager(emptyComponentRegistry());
        assertThrows(IndexOutOfBoundsException.class, () -> entities.getLatestGeneration(0));
        assertThrows(IndexOutOfBoundsException.class, () -> entities.getComponentTypes(-1));
        var handle = entities.createEntity(new MissingComponent());
        assertTrue(handle.valid());
        assertEquals(0, entities.getLatestGeneration(handle.index));
        assertEquals(List.of(MissingComponent.class), entities.getComponentTypes(handle.index));
        assertThrows(IndexOutOfBoundsException.class, () -> entities.getLatestGeneration(handle.index + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> entities.getComponentTypes(handle.index + 1));
        assertTrue(handle.tryDestroy());
        assertFalse(handle.valid());
    }

    @Test
    void scheduler_validWorkAndInvalidInputs_preserveExecutionAndErrors() throws Exception {
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunchHome = Launch.minecraftHome;
        try {
            homeField.set(null, temporaryDirectory.toFile());
            Launch.minecraftHome = temporaryDirectory.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunchHome;
        }
        ComponentRegistry components = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        EntityManager entities = new EntityManager(components);
        assertEquals(0, entities.createEntity().index);
        entities.flush();
        JobRegistry registry = new JobRegistry(components) {
            @Override
            public Map<JobDataQuery, JobDataInjector> getParallelJobDataQueries(Class<? extends ParallelJob> clazz) {
                return Map.of();
            }
            @Override
            public Map<String, JobDataInjector> getParallelJobExternalDataQueries(Class<? extends ParallelJob> clazz) {
                return clazz == ExternalJob.class ? Map.of("required", (owner, value) -> {}) : Map.of();
            }
            @Override
            public JobInstantiator getParallelJobInstantiator(Class<? extends ParallelJob> clazz) {
                return clazz == InvalidJob.class ? InvalidJob::new : TestJob::new;
            }
        };
        JobScheduler scheduler = CleanECSRuntime.newJobScheduler(registry);
        var handle = scheduler.executeParallelJob(entities, TestJob.class, null, Runnable::run);
        handle.future().join();
        assertFalse(handle.async());
        assertThrows(IllegalStateException.class,
            () -> scheduler.executeParallelJob(entities, InvalidJob.class, null, Runnable::run));
        assertThrows(IllegalArgumentException.class,
            () -> scheduler.executeParallelJob(entities, ExternalJob.class, null, Runnable::run));
        assertThrows(IllegalArgumentException.class,
            () -> scheduler.executeParallelJob(entities, ExternalJob.class, Map.of(), Runnable::run));
    }

    public static class Leaf {
        public int value;
    }
    public static class Nested {
        public Leaf leaf;
    }
    public static class TestComponent implements CleanComponent {
        public int id;
        public Vector3f position;
        public Leaf leaf;
    }
    public static class MissingComponent implements CleanComponent {}
    private static ComponentRegistry emptyComponentRegistry() {
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        registry.registerComponent("missing", MissingComponent.class, new MemberLayout());
        registry.lock();
        return registry;
    }
    public static class TestJob implements ParallelJob {
        public void execute(EntityCommands entities, int index, int entityID, int ordinal) {
            assertEquals(0, entityID);
        }
        public void query(EntityQuery query) {}
        public int estimateWorkload(int index) { return 1; }
    }
    public static class InvalidJob extends TestJob {
        @Override
        public int estimateWorkload(int index) { return 0; }
    }
    public static class ExternalJob extends TestJob {}
}
