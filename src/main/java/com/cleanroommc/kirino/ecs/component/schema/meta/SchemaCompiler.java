package com.cleanroommc.kirino.ecs.component.schema.meta;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;
import java.util.function.Function;

public final class SchemaCompiler {
    private SchemaCompiler() {
    }

    public static SchemaLayout compile(String name, Class<?> javaClass, List<FieldDef> definitions,
                                       MemberLayout memberLayout, Function<String, SchemaLayout> structs) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(javaClass);
        Objects.requireNonNull(structs);
        if (definitions.size() != memberLayout.fieldNames.size()) {
            throw new IllegalArgumentException("Field names and definitions have different lengths in " + name);
        }
        List<SchemaLayout.Member> members = new ObjectArrayList<>();
        List<SchemaLayout.Column> columns = new ObjectArrayList<>();
        Map<String, MutableNode> paths = new Object2ObjectOpenHashMap<>();
        int[] counts = new int[FlattenedScalarType.values().length];
        for (int i = 0; i < definitions.size(); i++) {
            String fieldName = memberLayout.fieldNames.get(i);
            if (paths.putIfAbsent(fieldName, new MutableNode()) != null) {
                throw new IllegalArgumentException("Duplicate field " + fieldName + " in " + name);
            }
            FieldDef definition = definitions.get(i);
            SchemaLayout nested = definition.fieldKind == FieldKind.STRUCT ? structs.apply(definition.structTypeName) : null;
            int first = columns.size();
            if (nested != null) {
                for (SchemaLayout.Column column : nested.columns()) {
                    addColumn(ImmutableList.<String>builder().add(fieldName).addAll(column.path()).build(),
                        column.type(), columns, counts, paths);
                }
            } else if (definition.fieldKind == FieldKind.SCALAR) {
                ScalarType scalar = definition.scalarType;
                List<FlattenedScalarType> types = FlattenedScalarType.flatten(scalar);
                if (scalar.isSingular()) {
                    addColumn(ImmutableList.of(fieldName), types.getFirst(), columns, counts, paths);
                } else {
                    String[] fieldsByOrdinal = new String[types.size()];
                    for (String field : scalar.fieldNames) {
                        fieldsByOrdinal[scalar.ordinalOffsetOfField(field)] = field;
                    }
                    for (int j = 0; j < fieldsByOrdinal.length; j++) {
                        addColumn(ImmutableList.of(fieldName, fieldsByOrdinal[j]), types.get(j), columns, counts, paths);
                    }
                }
            } else {
                throw new IllegalArgumentException("Unresolved struct " + definition.structTypeName + " in " + name);
            }
            members.add(new SchemaLayout.Member(fieldName, definition, first, columns.size() - first, nested));
        }
        return new SchemaLayout(name, javaClass, ImmutableList.copyOf(members), ImmutableList.copyOf(columns), freeze(paths));
    }

    private static void addColumn(ImmutableList<String> path, FlattenedScalarType type,
                                  List<SchemaLayout.Column> columns, int[] counts, Map<String, MutableNode> paths) {
        SchemaLayout.Column column = new SchemaLayout.Column(path, type, columns.size(), counts[type.ordinal()]++);
        columns.add(column);
        MutableNode node = null;
        Map<String, MutableNode> children = paths;
        for (String segment : path) {
            node = children.computeIfAbsent(segment, key -> new MutableNode());
            children = node.children;
        }
        node.column = column;
    }

    private static Map<String, SchemaLayout.PathNode> freeze(Map<String, MutableNode> nodes) {
        Map<String, SchemaLayout.PathNode> result = new Object2ObjectLinkedOpenHashMap<>();
        nodes.forEach((name, node) -> result.put(name,
            new SchemaLayout.PathNode(freeze(node.children), node.column))
        );
        return result;
    }

    private static final class MutableNode {
        final Map<String, MutableNode> children = new Object2ObjectLinkedOpenHashMap<>();
        SchemaLayout.Column column;
    }
}
