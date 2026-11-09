package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class SchemaLayout {
    private final String name;
    private final Class<?> javaClass;
    private final ObjectArrayView<Member> members;
    private final ObjectArrayView<Column> columns;
    private final ImmutableMap<String, PathNode> paths;

    SchemaLayout(String name, Class<?> javaClass, List<Member> members,
                 List<Column> columns, Map<String, PathNode> paths) {
        this.name = name;
        this.javaClass = javaClass;
        this.members = ObjectArrayView.copyOf(members, Member[]::new);
        this.columns = ObjectArrayView.copyOf(columns, Column[]::new);
        this.paths = ImmutableMap.copyOf(paths);
    }

    public String name() {
        return name;
    }

    public Class<?> javaClass() {
        return javaClass;
    }

    public ObjectArrayView<Member> members() {
        return members;
    }

    public ObjectArrayView<Column> columns() {
        return columns;
    }

    public int unitCount() {
        return columns.size();
    }

    public Column resolve(String... path) {
        Objects.requireNonNull(path);
        if (path.length == 0) {
            throw new IllegalArgumentException("Field path must not be empty for " + name);
        }
        PathNode node = null;
        Map<String, PathNode> children = paths;
        for (String segment : path) {
            Objects.requireNonNull(segment);
        }
        for (String segment : path) {
            node = children.get(segment);
            if (node == null) {
                throw new IllegalArgumentException("Invalid field path " + String.join(".", path) + " in " + name);
            }
            children = node.children;
        }
        if (node.column == null) {
            throw new IllegalArgumentException("Field path must reach a primitive field: " + String.join(".", path) + " in " + name);
        }
        return node.column;
    }

    public record Member(String name, FieldDef definition, int firstOrdinal, int unitCount, SchemaLayout nested) {
    }

    public record Column(ObjectArrayView<String> path, FlattenedScalarType type, int ordinal, int typeIndex) {
        public Column {
            Objects.requireNonNull(path);
            Objects.requireNonNull(type);
            if (path.isEmpty() || ordinal < 0 || typeIndex < 0) {
                throw new IllegalArgumentException("Invalid column metadata");
            }
        }
    }

    static final class PathNode {
        final ImmutableMap<String, PathNode> children;
        final Column column;

        PathNode(Map<String, PathNode> children, Column column) {
            this.children = ImmutableMap.copyOf(children);
            this.column = column;
        }
    }
}
