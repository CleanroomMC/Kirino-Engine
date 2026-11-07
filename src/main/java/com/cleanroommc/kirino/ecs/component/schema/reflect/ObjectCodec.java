package com.cleanroommc.kirino.ecs.component.schema.reflect;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.meta.SchemaLayout;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.joml.*;

import java.lang.invoke.MethodType;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class ObjectCodec {
    private final SchemaLayout layout;
    private final AccessHandlePool.Access access;
    private final ObjectCodec[] nested;

    public ObjectCodec(SchemaLayout layout) {
        this(layout, new Factory());
    }

    private ObjectCodec(SchemaLayout layout, Factory factory) {
        this.layout = Objects.requireNonNull(layout);



        this.access = AccessHandlePool.prepare(layout.javaClass(),
            new MemberLayout(layout.members().stream().map(SchemaLayout.Member::name).toList()));
        ObjectArrayList<ObjectCodec> children = new ObjectArrayList<>();
        for (int i = 0; i < layout.members().size(); i++) {
            SchemaLayout.Member member = layout.members().get(i);
            Class<?> expected = member.nested() == null ? member.definition().scalarType.clazz : member.nested().javaClass();
            Class<?> actual = access.fieldType(i);
            if (actual != expected && MethodType.methodType(actual).unwrap().returnType() != expected) {
                throw new IllegalArgumentException("Field type mismatch in " + layout.name() + "." + member.name()
                    + ": expected " + expected.getName() + ", got " + actual.getName());
            }
            if (member.nested() != null) {
                children.add(factory.prepare(member.nested()));
            }
        }
        nested = children.toArray(ObjectCodec[]::new);
    }

    public static final class Factory {
        private final Map<SchemaLayout, ObjectCodec> codecs = new Reference2ObjectOpenHashMap<>();

        public ObjectCodec prepare(SchemaLayout layout) {
            ObjectCodec codec = codecs.get(layout);
            if (codec == null) {
                codec = new ObjectCodec(layout, this);
                codecs.put(layout, codec);
            }
            return codec;
        }
    }

    public Object create(Object... values) {
        Objects.requireNonNull(values);
        if (values.length != layout.unitCount()) {
            throw new IllegalArgumentException("Expected " + layout.unitCount() + " values for " + layout.name() + ", got " + values.length);
        }
        for (int i = 0; i < values.length; i++) {
            Object value = Objects.requireNonNull(values[i]);
            boolean valid = switch (layout.columns().get(i).type()) {
                case BYTE -> value instanceof Byte;
                case SHORT -> value instanceof Short;
                case INT -> value instanceof Integer;
                case LONG -> value instanceof Long;
                case FLOAT -> value instanceof Float;
                case DOUBLE -> value instanceof Double;
                case BOOL -> value instanceof Boolean;
            };
            if (!valid) {
                throw new IllegalArgumentException("Invalid value for " + layout.name() + "." + String.join(".", layout.columns().get(i).path()));
            }
        }
        return createAt(values, 0);
    }

    private Object createAt(Object[] values, int offset) {
        Object instance = access.newInstance();
        int child = 0;
        for (int i = 0; i < layout.members().size(); i++) {
            SchemaLayout.Member member = layout.members().get(i);
            int start = offset + member.firstOrdinal();
            Object value = member.nested() == null ? scalar(member.definition().scalarType, values, start)
                : nested[child++].createAt(values, start);
            access.set(instance, i, value);
        }
        return instance;
    }

    public Object[] flatten(Object instance) {
        Object[] values = new Object[layout.unitCount()];
        flattenInto(instance, values, 0);
        return values;
    }

    public void flattenInto(Object instance, Object[] values, int offset) {
        Objects.requireNonNull(instance);
        Objects.checkFromIndexSize(offset, layout.unitCount(), values.length);
        if (!layout.javaClass().isInstance(instance)) {
            throw new IllegalArgumentException("Expected " + layout.javaClass().getName());
        }
        flattenAt(instance, values, offset);
    }

    private void flattenAt(Object instance, Object[] values, int offset) {
        int child = 0;
        for (int i = 0; i < layout.members().size(); i++) {
            SchemaLayout.Member member = layout.members().get(i);
            Object value = access.get(instance, i);
            if (value == null) {
                throw new IllegalArgumentException("Null field " + layout.name() + "." + member.name());
            }
            int start = offset + member.firstOrdinal();
            if (member.nested() == null) {
                flattenScalar(member.definition().scalarType, value, values, start);
            } else {
                nested[child++].flattenAt(value, values, start);
            }
        }
    }

    private static Object scalar(ScalarType type, Object[] values, int start) {
        return switch (type) {
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOL -> values[start];
            case VEC2 -> new Vector2f((float) values[start], (float) values[start + 1]);
            case VEC3 -> new Vector3f((float) values[start], (float) values[start + 1], (float) values[start + 2]);
            case VEC4 -> new Vector4f((float) values[start], (float) values[start + 1], (float) values[start + 2], (float) values[start + 3]);
            case MAT3 -> new Matrix3f((float) values[start], (float) values[start + 1], (float) values[start + 2],
                (float) values[start + 3], (float) values[start + 4], (float) values[start + 5],
                (float) values[start + 6], (float) values[start + 7], (float) values[start + 8]);
            case MAT4 -> new Matrix4f((float) values[start], (float) values[start + 1], (float) values[start + 2], (float) values[start + 3],
                (float) values[start + 4], (float) values[start + 5], (float) values[start + 6], (float) values[start + 7],
                (float) values[start + 8], (float) values[start + 9], (float) values[start + 10], (float) values[start + 11],
                (float) values[start + 12], (float) values[start + 13], (float) values[start + 14], (float) values[start + 15]);
        };
    }

    private static void flattenScalar(ScalarType type, Object value, Object[] values, int start) {
        switch (type) {
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOL -> values[start] = value;
            case VEC2 -> {
                Vector2f vector = (Vector2f) value;
                values[start] = vector.x;
                values[start + 1] = vector.y;
            }
            case VEC3 -> {
                Vector3f vector = (Vector3f) value;
                values[start] = vector.x;
                values[start + 1] = vector.y;
                values[start + 2] = vector.z;
            }
            case VEC4 -> {
                Vector4f vector = (Vector4f) value;
                values[start] = vector.x;
                values[start + 1] = vector.y;
                values[start + 2] = vector.z;
                values[start + 3] = vector.w;
            }
            case MAT3 -> {
                Matrix3f matrix = (Matrix3f) value;
                for (int column = 0; column < 3; column++) {
                    for (int row = 0; row < 3; row++) {
                        values[start + column * 3 + row] = matrix.get(column, row);
                    }
                }
            }
            case MAT4 -> {
                Matrix4f matrix = (Matrix4f) value;
                for (int column = 0; column < 4; column++) {
                    for (int row = 0; row < 4; row++) {
                        values[start + column * 4 + row] = matrix.get(column, row);
                    }
                }
            }
        }
    }
}
