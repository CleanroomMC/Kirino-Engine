package com.cleanroommc.kirino.ecs;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class FieldDefTest {
    @Test
    void hashCode_allScalarTypes_matchesPreviousFormula() {
        for (ScalarType type : ScalarType.values()) {
            FieldDef field = new FieldDef(type);
            assertEquals(Objects.hash(FieldKind.SCALAR, type, null), field.hashCode());
            assertEquals(field.hashCode(), new FieldDef(type).hashCode());
        }
    }

    @Test
    void hashCode_structNames_matchesPreviousFormulaIncludingOverflow() {
        for (String name : new String[]{"", "AABB", "BlockInfo", "Aa", "BB", "кириллица", "x".repeat(4096)}) {
            FieldDef field = new FieldDef(name);
            assertEquals(Objects.hash(FieldKind.STRUCT, null, name), field.hashCode());
            assertEquals(field.hashCode(), new FieldDef(new String(name)).hashCode());
        }
    }

    @Test
    void equals_sameDescription_preservesReferenceEquality() {
        FieldDef first = new FieldDef("AABB");
        FieldDef second = new FieldDef("AABB");
        assertEquals(first, first);
        assertNotEquals(first, second);
        Map<FieldDef, Integer> fields = new HashMap<>();
        fields.put(first, 1);
        fields.put(second, 2);
        assertEquals(2, fields.size());
        assertEquals(1, fields.get(first));
        assertEquals(2, fields.get(second));
    }

    @Test
    void constructors_nullArguments_remainRejected() {
        assertThrows(NullPointerException.class, () -> new FieldDef((ScalarType) null));
        assertThrows(NullPointerException.class, () -> new FieldDef((String) null));
    }
}
