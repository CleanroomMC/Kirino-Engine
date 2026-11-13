package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Locale;
import java.util.Random;

final class EntityLoadFixture {
    final ComponentRegistry registry;
    final Class<? extends CleanComponent> type;
    final CleanComponent[] payload;
    final String idField;

    EntityLoadFixture(String schema) throws ClassNotFoundException {
        CleanroomEnvironment.setSide(Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType scalar : ScalarType.values()) {
            fields.registerFieldType(scalar.name().toLowerCase(Locale.ROOT), scalar.clazz, new FieldDef(scalar));
        }
        if (schema.equals("meshlet")) {
            StructScanningEvent event = new StructScanningEvent();
            event.register("com.cleanroommc.kirino.engine.render.usage.ecs.struct");
            for (var plan : StructScanningHelper.scanStructClasses(event, fields)) {
                Class<?> type = Class.forName(plan.structClass());
                structs.registerStructType(plan.structName(), type, plan.memberLayout(), plan.structDef());
                fields.registerFieldType(plan.structName(), type, new FieldDef(plan.structName()));
            }
        }
        structs.lock();
        fields.lock();
        registry = new ComponentRegistry(fields);
        registry.registerComponent("small", Small.class, new MemberLayout("id", "mass", "seed", "flag"),
            "int", "float", "long", "bool");
        registry.registerComponent("allocation_marker", Marker.class, new MemberLayout());
        if (schema.equals("meshlet")) {
            ComponentScanningEvent event = new ComponentScanningEvent();
            event.register("com.cleanroommc.kirino.engine.render.usage.ecs.component");
            for (var plan : ComponentScanningHelper.scanComponentClasses(event, fields)) {
                registry.registerComponent(plan.componentName(), Class.forName(plan.componentClass()).asSubclass(CleanComponent.class),
                    plan.memberLayout(), plan.fieldTypeNames());
            }
            MeshletComponent meshlet = new MeshletComponent();
            meshlet.meshletId = 713;
            payload = new CleanComponent[]{meshlet};
            type = MeshletComponent.class;
            idField = "meshletId";
        } else {
            Small small = new Small();
            small.id = 713;
            small.mass = 2.5f;
            small.seed = Long.MAX_VALUE;
            small.flag = true;
            payload = new CleanComponent[]{small};
            type = Small.class;
            idField = "id";
        }
        registry.lock();
    }

    EntityManager manager() { return new EntityManager(registry); }

    CleanEntityHandle[] populate(EntityManager manager, int rows) {
        CleanEntityHandle[] handles = new CleanEntityHandle[rows];
        for (int i = 0; i < rows; i++) {
            handles[i] = manager.createEntity(payload);
        }
        return handles;
    }

    void verify(EntityManager manager, CleanEntityHandle[] handles) {
        int live = 0;
        for (ArchetypeDataPool pool : manager.startQuery(manager.newQuery().with(type))) {
            var values = pool.getArray(type, idField);
            PoolRows rows = pool.getRows();
            for (int row = 0; row < rows.limit; row++) {
                if (rows.nextFree[row] == 0) {
                    int entity = pool.getEntityID(row);
                    if (!handles[entity].isAlive() || values.getInt(row) != 713) {
                        throw new IllegalStateException("Bulk data or identity mismatch");
                    }
                    live++;
                }
            }
        }
        if (live != handles.length) {
            throw new IllegalStateException("Incorrect live count: " + live);
        }
    }

    static int[] order(int rows, String order) {
        int[] ids = new int[rows];
        for (int i = 0; i < rows; i++) {
            ids[i] = order.equals("reverse") ? rows - i - 1 : i;
        }
        if (order.equals("shuffled")) {
            Random random = new Random(0x4b4952494e4fL);
            for (int i = rows - 1; i > 0; i--) {
                int other = random.nextInt(i + 1);
                int value = ids[i];
                ids[i] = ids[other];
                ids[other] = value;
            }
        }
        return ids;
    }

    public static class Small implements CleanComponent {
        public int id;
        public float mass;
        public long seed;
        public boolean flag;
    }

    public static class Marker implements CleanComponent {
    }
}
