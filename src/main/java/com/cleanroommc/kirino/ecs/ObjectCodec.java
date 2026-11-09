package com.cleanroommc.kirino.ecs;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Objects;

final class ObjectCodec {
    private final SchemaLayout layout;
    private final SchemaLayout.Member[] members;
    private final AccessHandlePool.Access access;
    private final ObjectCodec[] nested;
    private final boolean[] references;

    public ObjectCodec(SchemaLayout layout) { this(layout, new Factory()); }

    private ObjectCodec(SchemaLayout layout, Factory factory) {
        this.layout = Objects.requireNonNull(layout);
        this.members = layout.members().elements();
        String[] fieldNames = new String[members.length];
        for (int i = 0; i < members.length; i++) {
            fieldNames[i] = members[i].name();
        }
        this.access = AccessHandlePool.prepare(layout.javaClass(), new MemberLayout(fieldNames));
        nested = new ObjectCodec[members.length];
        references = new boolean[members.length];
        for (int i = 0; i < members.length; i++) {
            SchemaLayout.Member member = members[i];
            Class<?> expected = member.nested() == null ? member.definition().scalarType.clazz : member.nested().javaClass();
            Class<?> actual = access.fieldType(i);
            if (actual != expected && MethodType.methodType(actual).unwrap().returnType() != expected) {
                throw new IllegalArgumentException("Field type mismatch in " + layout.name() + "." + member.name()
                    + ": expected " + expected.getName() + ", got " + actual.getName());
            }
            references[i] = !actual.isPrimitive();
            if (member.nested() != null) { nested[i] = factory.prepare(member.nested()); }
        }
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
        SchemaLayout.Column[] columns = layout.columns().elements();
        for (int i = 0; i < values.length; i++) {
            Object value = Objects.requireNonNull(values[i]);
            if (!columns[i].type().boxedClass.isInstance(value)) {
                throw new IllegalArgumentException("Invalid value for " + layout.name() + "." + String.join(".", columns[i].path().elements()));
            }
        }
        return read(new ArrayValues(values), 0);
    }

    public Object[] flatten(Object instance) {
        Object[] values = new Object[layout.unitCount()];
        flattenInto(instance, values, 0);
        return values;
    }

    public void flattenInto(Object instance, Object[] values, int offset) {
        Objects.checkFromIndexSize(offset, layout.unitCount(), values.length);
        validate(instance);
        writeAt(instance, new ArrayValues(values), offset, 0);
    }

    public void validate(Object instance) {
        Objects.requireNonNull(instance);
        if (!layout.javaClass().isInstance(instance)) {
            throw new IllegalArgumentException("Expected " + layout.javaClass().getName());
        }
        validateReferences(instance);
    }

    private void validateReferences(Object instance) {
        for (int i = 0; i < members.length; i++) {
            if (references[i]) {
                Object value = access.get(instance, i);
                if (value == null) {
                    throw new IllegalArgumentException("Null field " + layout.name() + "." + members[i].name());
                }
                if (nested[i] != null) { nested[i].validateReferences(value); }
            }
        }
    }

    public Object read(PrimitiveValues values, int row) { return readAt(values, 0, row); }

    public void write(Object instance, PrimitiveValues values, int row) {
        validate(instance);
        writeAt(instance, values, 0, row);
    }

    private Object readAt(PrimitiveValues values, int offset, int row) {
        Object instance = access.newInstance();
        for (int i = 0; i < members.length; i++) {
            SchemaLayout.Member member = members[i];
            int start = offset + member.firstOrdinal();
            if (nested[i] != null) {
                access.set(instance, i, nested[i].readAt(values, start, row));
            } else {
                switch (member.definition().scalarType) {
                    case BYTE -> access.setByte(instance, i, values.getByte(start, row));
                    case SHORT -> access.setShort(instance, i, values.getShort(start, row));
                    case INT -> access.setInt(instance, i, values.getInt(start, row));
                    case LONG -> access.setLong(instance, i, values.getLong(start, row));
                    case FLOAT -> access.setFloat(instance, i, values.getFloat(start, row));
                    case DOUBLE -> access.setDouble(instance, i, values.getDouble(start, row));
                    case BOOL -> access.setBool(instance, i, values.getBool(start, row));
                    default -> access.set(instance, i, ScalarCodec.create(member.definition().scalarType, values, start, row));
                }
            }
        }
        return instance;
    }

    private void writeAt(Object instance, PrimitiveValues values, int offset, int row) {
        for (int i = 0; i < members.length; i++) {
            SchemaLayout.Member member = members[i];
            int start = offset + member.firstOrdinal();
            if (nested[i] != null) {
                nested[i].writeAt(access.get(instance, i), values, start, row);
            } else {
                switch (member.definition().scalarType) {
                    case BYTE -> values.setByte(start, row, access.getByte(instance, i));
                    case SHORT -> values.setShort(start, row, access.getShort(instance, i));
                    case INT -> values.setInt(start, row, access.getInt(instance, i));
                    case LONG -> values.setLong(start, row, access.getLong(instance, i));
                    case FLOAT -> values.setFloat(start, row, access.getFloat(instance, i));
                    case DOUBLE -> values.setDouble(start, row, access.getDouble(instance, i));
                    case BOOL -> values.setBool(start, row, access.getBool(instance, i));
                    default -> ScalarCodec.write(member.definition().scalarType, access.get(instance, i), values, start, row);
                }
            }
        }
    }
}
