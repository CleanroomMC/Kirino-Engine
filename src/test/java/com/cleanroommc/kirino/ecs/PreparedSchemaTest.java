package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;

import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.ecs.component.scan.event.*;
import org.joml.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class PreparedSchemaTest {
    @Test
    void scanning_cyclicStruct_preservesFieldForCompilerValidation() throws Exception {
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        StructScanningEvent event = new StructScanningEvent();
        event.register("com.cleanroommc.kirino.ecs.schemafixtures");
        var plans = StructScanningHelper.scanStructClasses(event, fields);
        assertEquals(1, plans.size());
        var plan = plans.getFirst();
        assertEquals(List.of("self"), plan.memberLayout().fieldNames);
        structs.registerStructType(plan.structName(), Class.forName(plan.structClass()), plan.memberLayout(), plan.structDef());
        assertThrows(IllegalArgumentException.class, structs::lock);
    }

    @Test
    void renderSchemas_scannedComponentsAndExistingJob_keepTheirBindings() throws Exception {
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType scalar : ScalarType.values()) {
            fields.registerFieldType(scalar.name().toLowerCase(Locale.ROOT), scalar.clazz, new FieldDef(scalar));
        }
        StructScanningEvent structEvent = new StructScanningEvent();
        structEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.struct");
        for (var plan : StructScanningHelper.scanStructClasses(structEvent, fields)) {
            Class<?> clazz = Class.forName(plan.structClass());
            structs.registerStructType(plan.structName(), clazz, plan.memberLayout(), plan.structDef());
            fields.registerFieldType(plan.structName(), clazz, new FieldDef(plan.structName()));
        }
        structs.lock(); fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        ComponentScanningEvent componentEvent = new ComponentScanningEvent();
        componentEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.component");
        for (var plan : ComponentScanningHelper.scanComponentClasses(componentEvent, fields)) {
            components.registerComponent(plan.componentName(), Class.forName(plan.componentClass()).asSubclass(CleanComponent.class),
                plan.memberLayout(), plan.fieldTypeNames());
        }
        components.lock();
        Class<? extends CleanComponent> meshlet = com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent.class;
        SchemaLayout layout = components.getLayout(meshlet);
        assertSame(FlattenedScalarType.INT, layout.resolve("block31", "blockInfo", "zMinusFaceTexCoord3").type());
        assertSame(FlattenedScalarType.FLOAT, layout.resolve("normal", "z").type());
        var instance = new com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent();
        Object[] values = components.flattenComponent(instance);
        assertArrayEquals(values, components.flattenComponent(components.newComponent(components.getComponentName(meshlet), values)));
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(com.cleanroommc.kirino.engine.render.usage.task.job.ChunkMeshletGenJob.class);
        for (JobDataQuery query : jobs.getParallelJobDataQueries(com.cleanroommc.kirino.engine.render.usage.task.job.ChunkMeshletGenJob.class).keySet()) {
            assertNotNull(jobs.getParallelJobColumn(com.cleanroommc.kirino.engine.render.usage.task.job.ChunkMeshletGenJob.class, query));
        }
    }

    @Test
    void layout_mixedNestedTypes_preservesOrdinalsAndTypeIndexes() {
        StructRegistry structs = structs();
        SchemaLayout layout = structs.getLayout("root");
        assertEquals(7, layout.unitCount());
        assertSame(layout, structs.getLayout("root"));
        assertColumn(layout, new String[]{"id"}, FlattenedScalarType.INT, 0, 0);
        assertColumn(layout, new String[]{"leaf", "flag"}, FlattenedScalarType.BOOL, 1, 0);
        assertColumn(layout, new String[]{"leaf", "count"}, FlattenedScalarType.INT, 2, 1);
        assertColumn(layout, new String[]{"position", "x"}, FlattenedScalarType.FLOAT, 3, 0);
        assertColumn(layout, new String[]{"position", "z"}, FlattenedScalarType.FLOAT, 5, 2);
        assertColumn(layout, new String[]{"mass"}, FlattenedScalarType.FLOAT, 6, 3);
        assertEquals(1, layout.members().get(1).firstOrdinal());
        assertEquals(2, layout.members().get(1).unitCount());
        assertSame(structs.getLayout("leaf"), layout.members().get(1).nested());
        assertThrows(UnsupportedOperationException.class, () -> layout.columns().clear());
        assertThrows(UnsupportedOperationException.class, () -> layout.columns().getFirst().path().clear());
        assertThrows(IllegalArgumentException.class, () -> layout.resolve("leaf"));
        assertThrows(IllegalArgumentException.class, () -> layout.resolve("position"));
        assertThrows(IllegalArgumentException.class, () -> layout.resolve("id", "x"));
    }

    @Test
    void lock_forwardReferencesAndEmptyStructs_compileWholeGraph() {
        StructRegistry registry = new StructRegistry();
        registry.registerStructType("root", Root.class, new MemberLayout("leaf"), new StructDef(new FieldDef("leaf")));
        registry.registerStructType("leaf", Leaf.class, new MemberLayout("flag", "count"),
            new StructDef(new FieldDef(ScalarType.BOOL), new FieldDef(ScalarType.INT)));
        registry.registerStructType("empty", Empty.class, new MemberLayout(), new StructDef());
        registry.lock();
        assertEquals(2, registry.flattenedUnitCount("root"));
        assertEquals(0, registry.flattenedUnitCount("empty"));
        assertInstanceOf(Empty.class, registry.newStruct("empty"));
        assertArrayEquals(new Object[0], registry.flattenStruct(new Empty()));
    }

    @Test
    void lock_missingReferences_canBeFixedWithoutHalfLocking() {
        StructRegistry registry = new StructRegistry();
        registry.registerStructType("root", Root.class, new MemberLayout("leaf"), new StructDef(new FieldDef("leaf")));
        assertThrows(IllegalArgumentException.class, registry::lock);
        registry.registerStructType("leaf", Leaf.class, new MemberLayout("count"), new StructDef(new FieldDef(ScalarType.INT)));
        registry.lock();
        assertEquals(1, registry.flattenedUnitCount("root"));
    }

    @Test
    void lock_directAndIndirectCycles_reportsPathWithoutStackOverflow() {
        StructRegistry direct = new StructRegistry();
        direct.registerStructType("self", Root.class, new MemberLayout("leaf"), new StructDef(new FieldDef("self")));
        var selfError = assertThrows(IllegalArgumentException.class, direct::lock);
        assertTrue(selfError.getMessage().contains("self -> self"));
        StructRegistry indirect = new StructRegistry();
        indirect.registerStructType("a", Root.class, new MemberLayout("leaf"), new StructDef(new FieldDef("b")));
        indirect.registerStructType("b", Leaf.class, new MemberLayout("count"), new StructDef(new FieldDef("a")));
        var error = assertThrows(IllegalArgumentException.class, indirect::lock);
        assertTrue(error.getMessage().contains("a"));
        assertTrue(error.getMessage().contains("b"));
    }

    @Test
    void compilation_mismatchedNamesAndDuplicateNames_rejectsSchema() {
        StructRegistry mismatch = new StructRegistry();
        mismatch.registerStructType("leaf", Leaf.class, new MemberLayout(), new StructDef(new FieldDef(ScalarType.INT)));
        assertThrows(IllegalArgumentException.class, mismatch::lock);
        StructRegistry duplicates = new StructRegistry();
        duplicates.registerStructType("leaf", Leaf.class, new MemberLayout("count", "count"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef(ScalarType.INT)));
        assertThrows(IllegalArgumentException.class, duplicates::lock);
    }

    @Test
    void lock_invalidJavaMembersAndConstructors_failBeforeExecution() {
        for (String field : new String[]{"absent", "constant", "shared"}) {
            StructRegistry registry = new StructRegistry();
            registry.registerStructType("invalid", InvalidFields.class, new MemberLayout(field), new StructDef(new FieldDef(ScalarType.INT)));
            assertThrows(IllegalArgumentException.class, registry::lock, field);
        }
        StructRegistry wrongType = new StructRegistry();
        wrongType.registerStructType("leaf", Leaf.class, new MemberLayout("count"), new StructDef(new FieldDef(ScalarType.FLOAT)));
        assertThrows(IllegalArgumentException.class, wrongType::lock);
        StructRegistry noConstructor = new StructRegistry();
        noConstructor.registerStructType("invalid", NoDefaultConstructor.class, new MemberLayout(), new StructDef());
        assertThrows(IllegalArgumentException.class, noConstructor::lock);
    }

    @Test
    void codec_allScalarTypes_matchesExistingScalarOrder() {
        StructRegistry registry = new StructRegistry();
        ScalarType[] types = ScalarType.values();
        String[] names = {"b", "s", "i", "l", "f", "d", "bool", "v2", "v3", "v4", "m3", "m4"};
        FieldDef[] definitions = Arrays.stream(types).map(FieldDef::new).toArray(FieldDef[]::new);
        registry.registerStructType("all", AllTypes.class, new MemberLayout(names), new StructDef(definitions));
        registry.lock();
        Object[] values = new Object[41];
        values[0] = (byte) 1; values[1] = (short) 2; values[2] = 3; values[3] = 4L;
        values[4] = 5.5f; values[5] = 6.5d; values[6] = true;
        for (int i = 7; i < values.length; i++) { values[i] = (float) i; }
        Object instance = registry.newStruct("all", values);
        assertArrayEquals(values, registry.flattenStruct(instance));
        AllTypes all = (AllTypes) instance;
        assertEquals(new Vector4f(12, 13, 14, 15), all.v4);
        assertEquals(16f, all.m3.m00(), 0f);
        assertEquals(24f, all.m3.m22(), 0f);
        assertEquals(25f, all.m4.m00(), 0f);
        assertEquals(40f, all.m4.m33(), 0f);
    }

    @Test
    void codec_nestedRoundTripAndCallerBuffer_preservesValuesAndBounds() {
        StructRegistry structs = structs();
        structs.lock();
        Object[] values = {9, true, 17, 1f, -2f, 3f, 4f};
        Root root = (Root) structs.newStruct("root", values);
        assertArrayEquals(values, structs.flattenStruct(root));
        ObjectCodec codec = new ObjectCodec(structs.getLayout("root"));
        Object[] output = new Object[values.length + 2];
        output[0] = "left"; output[output.length - 1] = "right";
        codec.flattenInto(root, output, 1);
        assertArrayEquals(values, Arrays.copyOfRange(output, 1, output.length - 1));
        assertEquals("left", output[0]); assertEquals("right", output[output.length - 1]);
        assertThrows(IndexOutOfBoundsException.class, () -> codec.flattenInto(root, output, 3));
        assertThrows(IllegalArgumentException.class, () -> structs.newStruct("root", 1));
        Object[] wrong = values.clone(); wrong[0] = 1L;
        assertThrows(IllegalArgumentException.class, () -> structs.newStruct("root", wrong));
        root.leaf = null;
        assertThrows(IllegalArgumentException.class, () -> structs.flattenStruct(root));
    }

    @Test
    void codec_sharedNestedLayout_createsIndependentObjects() {
        StructRegistry structs = structs();
        structs.registerStructType("pair", Pair.class, new MemberLayout("first", "second"),
            new StructDef(new FieldDef("leaf"), new FieldDef("leaf")));
        structs.lock();
        Object[] values = {true, 11, false, 22};
        Pair pair = (Pair) structs.newStruct("pair", values);
        assertNotSame(pair.first, pair.second);
        assertEquals(11, pair.first.count);
        assertEquals(22, pair.second.count);
        assertArrayEquals(values, structs.flattenStruct(pair));
    }

    @Test
    void fieldRegistry_lock_validatesBindingsBeforeFreezing() {
        FieldRegistry wrongScalar = new FieldRegistry(new StructRegistry());
        wrongScalar.registerFieldType("int", float.class, new FieldDef(ScalarType.INT));
        assertThrows(IllegalArgumentException.class, wrongScalar::lock);
        StructRegistry structs = new StructRegistry();
        FieldRegistry forward = new FieldRegistry(structs);
        forward.registerFieldType("leaf", Leaf.class, new FieldDef("leaf"));
        assertThrows(IllegalArgumentException.class, forward::lock);
        structs.registerStructType("leaf", Leaf.class, new MemberLayout("count"), new StructDef(new FieldDef(ScalarType.INT)));
        structs.lock();
        forward.lock();
        assertArrayEquals(new Object[]{9}, forward.flattenField(forward.newField("leaf", 9)));
        assertThrows(IllegalStateException.class,
            () -> forward.registerFieldType("other", int.class, new FieldDef(ScalarType.INT)));
    }

    @Test
    void accessPool_failedRegistration_doesNotPublishOrReplaceAccess() {
        AccessHandlePool pool = new AccessHandlePool();
        assertThrows(IllegalArgumentException.class, () -> pool.register(Leaf.class, new MemberLayout("count", "missing")));
        assertFalse(pool.classRegistered(Leaf.class));
        pool.register(Leaf.class, new MemberLayout("count"));
        assertThrows(IllegalArgumentException.class, () -> pool.register(Leaf.class, new MemberLayout("missing")));
        Leaf leaf = (Leaf) pool.newClass(Leaf.class);
        pool.setFieldValue(Leaf.class, leaf, "count", 19);
        assertEquals(19, pool.getFieldValue(Leaf.class, leaf, "count"));
        assertThrows(IllegalStateException.class, () -> pool.setFieldValue(Leaf.class, leaf, "count", "wrong"));
        assertThrows(IllegalArgumentException.class, () -> pool.getFieldValue(Leaf.class, leaf, "missing"));
        pool.register(ThrowingConstructor.class, new MemberLayout());
        assertThrows(IllegalStateException.class, () -> pool.newClass(ThrowingConstructor.class));
    }

    @Test
    void componentRegistration_duplicateClass_keepsBothIndexesConsistent() {
        ComponentRegistry components = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        components.registerComponent("first", EmptyComponent.class, new MemberLayout());
        assertThrows(IllegalArgumentException.class, () -> components.registerComponent("second", EmptyComponent.class, new MemberLayout()));
        assertFalse(components.componentExists("second"));
        assertEquals("first", components.getComponentName(EmptyComponent.class));
        assertEquals(1, components.getComponentDataMap().size());
    }

    @Test
    void heapPool_preparedColumns_matchPathsAcrossGrowthAndMultipleComponents() {
        ComponentRegistry components = components();
        HeapPool pool = new HeapPool(components, List.of(OtherComponent.class, TestComponent.class), 1, 2, 1);
        TestComponent test = component(7);
        OtherComponent other = new OtherComponent(); other.value = 30f;
        pool.addEntity(10, new CleanComponent[]{other, test});
        SchemaLayout layout = components.getLayout(TestComponent.class);
        assertEquals(17, pool.getArray(TestComponent.class, layout.resolve("leaf", "count")).getInt(0));
        assertEquals(4f, pool.getArray(TestComponent.class, layout.resolve("mass")).getFloat(0), 0f);
        pool.getArray(TestComponent.class, layout.resolve("position", "y")).setFloat(0, 99f);
        assertEquals(99f, pool.getArray(TestComponent.class, "position", "y").getFloat(0), 0f);
        assertEquals(30f, pool.getArray(OtherComponent.class, "value").getFloat(0), 0f);
        pool.addEntity(11, new CleanComponent[]{other, component(8)});
        assertTrue(pool.getCurrentSize() > 1);
        assertEquals(8, pool.getArray(TestComponent.class, layout.resolve("id")).getInt(1));
        TestComponent roundTrip = (TestComponent) pool.getComponent(10, TestComponent.class);
        assertEquals(7, roundTrip.id);
        assertEquals(99f, roundTrip.position.y, 0f);
        assertThrows(IllegalArgumentException.class,
            () -> pool.getArray(TestComponent.class, components.getLayout(OtherComponent.class).resolve("value")));
        TestComponent replacement = component(55);
        pool.setComponent(10, replacement);
        assertEquals(55, pool.getArray(TestComponent.class, layout.resolve("id")).getInt(0));
    }

    @Test
    void jobRegistration_storesResolvedColumnAndPublishesOnlyOnSuccess() throws Exception {
        ComponentRegistry components = components();
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(TestJob.class);
        JobDataQuery query = TestJob.class.getField("counts").getAnnotation(JobDataQuery.class);
        SchemaLayout.Column column = jobs.getParallelJobColumn(TestJob.class, query);
        assertSame(components.getLayout(TestComponent.class).resolve("leaf", "count"), column);
        HeapPool pool = new HeapPool(components, List.of(TestComponent.class), 1, 1, 1);
        pool.addEntity(4, new CleanComponent[]{component(7)});
        TestJob job = (TestJob) jobs.getParallelJobInstantiator(TestJob.class).instantiate();
        jobs.getParallelJobDataQueries(TestJob.class).get(query).inject(job, pool.getArray(TestComponent.class, column));
        assertEquals(17, job.counts.getInt(0));
        assertThrows(RuntimeException.class, () -> jobs.registerParallelJob(InvalidJob.class));
        assertNull(jobs.getParallelJobInstantiator(InvalidJob.class));
        assertNull(jobs.getParallelJobDataQueries(InvalidJob.class));
    }

    @Test
    void frozenSchema_parallelReadsAndConversions_usePreparedData() throws Exception {
        StructRegistry structs = structs();
        structs.lock();
        SchemaLayout layout = structs.getLayout("root");
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> {
                    for (int j = 0; j < 100; j++) {
                        assertSame(layout, structs.getLayout("root"));
                        Object[] values = {j, true, 17, 1f, 2f, 3f, 4f};
                        assertArrayEquals(values, structs.flattenStruct(structs.newStruct("root", values)));
                    }
                    return null;
                });
            }
            for (Future<Void> future : executor.invokeAll(tasks)) { future.get(); }
        }
    }

    private static void assertColumn(SchemaLayout layout, String[] path, FlattenedScalarType type, int ordinal, int typeIndex) {
        SchemaLayout.Column column = layout.resolve(path);
        assertSame(type, column.type()); assertEquals(ordinal, column.ordinal()); assertEquals(typeIndex, column.typeIndex());
    }

    private static StructRegistry structs() {
        StructRegistry registry = new StructRegistry();
        registry.registerStructType("leaf", Leaf.class, new MemberLayout("flag", "count"),
            new StructDef(new FieldDef(ScalarType.BOOL), new FieldDef(ScalarType.INT)));
        registry.registerStructType("root", Root.class, new MemberLayout("id", "leaf", "position", "mass"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef("leaf"), new FieldDef(ScalarType.VEC3), new FieldDef(ScalarType.FLOAT)));
        return registry;
    }

    private static ComponentRegistry components() {
        StructRegistry structs = structs(); structs.lock();
        FieldRegistry fields = new FieldRegistry(structs);
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fields.registerFieldType("vec3", Vector3f.class, new FieldDef(ScalarType.VEC3));
        fields.registerFieldType("leaf", Leaf.class, new FieldDef("leaf")); fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("test", TestComponent.class, new MemberLayout("id", "leaf", "position", "mass"), "int", "leaf", "vec3", "float");
        components.registerComponent("other", OtherComponent.class, new MemberLayout("value"), "float");
        components.lock(); return components;
    }

    private static TestComponent component(int id) {
        TestComponent value = new TestComponent(); value.id = id;
        value.leaf = new Leaf(); value.leaf.flag = true; value.leaf.count = 17;
        value.position = new Vector3f(1, 2, 3); value.mass = 4; return value;
    }

    public static class Empty {}
    public static class Leaf { public boolean flag; public int count; }
    public static class Pair { public Leaf first; public Leaf second; }
    public static class Root { public int id; public Leaf leaf; public Vector3f position; public float mass; }
    public static class InvalidFields { public final int constant = 0; public static int shared; }
    public static class NoDefaultConstructor { public NoDefaultConstructor(int value) {} }
    public static class ThrowingConstructor { public ThrowingConstructor() { throw new IllegalStateException("expected"); } }
    public static class EmptyComponent implements CleanComponent {}
    public static class TestComponent extends Root implements CleanComponent {}
    public static class OtherComponent implements CleanComponent { public float value; }
    public static class AllTypes {
        public byte b; public short s; public int i; public long l; public float f; public double d; public boolean bool;
        public Vector2f v2; public Vector3f v3; public Vector4f v4; public Matrix3f m3; public Matrix4f m4;
    }
    public static class TestJob implements ParallelJob {
        @JobDataQuery(componentClass = TestComponent.class, fieldAccessChain = {"leaf", "count"})
        public PrimitiveArray counts;
        public void execute(com.cleanroommc.kirino.ecs.EntityCommands manager, int index, int id, int ordinal) {}
        public void query(com.cleanroommc.kirino.ecs.EntityQuery query) {}
        public int estimateWorkload(int index) { return 1; }
    }
    public static class InvalidJob extends TestJob {
        @JobDataQuery(componentClass = TestComponent.class, fieldAccessChain = {"absent"})
        public PrimitiveArray invalid;
    }
}
