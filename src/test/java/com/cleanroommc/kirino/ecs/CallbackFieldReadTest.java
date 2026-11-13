package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CallbackFieldReadTest {
    @Test
    void directRead_supportsRegisteredWrapperFieldsWithoutConstructingAComponent() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("counted", Counted.class, new MemberLayout("value"), "int");
        components.lock();
        EntityManager manager = new EntityManager(components);
        Counted input = new Counted();
        input.value = 713;
        int createdBefore = Counted.constructions;
        var entity = manager.createEntity(context -> {
            assertEquals(713, context.getInt(Counted.class, "value"));
            assertEquals(createdBefore, Counted.constructions);
        }, null, input);
        manager.flush();
        entity.tryDestroy();
        manager.flush();
        assertEquals(createdBefore, Counted.constructions);
    }

    public static class Counted implements CleanComponent {
        static int constructions;
        public Integer value;
        public Counted() { constructions++; }
    }
}
