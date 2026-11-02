package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.reflect.ConstantExpressions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeRegistryTest {

    private static final String SCENE_SOURCE = """
            struct Light {
                vec3 position;
                float intensity;
            };
            struct Scene {
                Light lights[2];
                mat2x3 basis;
                uint flags;
            };
            uniform Scene scene;
            """;

    private static ShaderMeta analyze(String source) {
        ShaderMeta meta = new DefaultShaderAnalyzer().analyze(source, ShaderType.VERTEX);
        assertNotNull(meta, () -> "Could not analyze GLSL fixture:\n" + source);
        return meta;
    }

    private static TypeRegistry registry(ShaderMeta meta) {
        return new TypeRegistry(meta, new ConstantExpressions(meta));
    }

    private static TypeRegistry registry(String source) {
        return registry(analyze(source));
    }

    @Test
    void testScene() {
        TypeRegistry registry = registry(SCENE_SOURCE);
        StructType scene = registry.struct("Scene").orElseThrow();

        assertSame(scene, registry.types().get("Scene"));
        assertSame(scene, registry.type("Scene", StructType.class).orElseThrow());
        assertEquals(List.of("Light", "Scene"), new ArrayList<>(registry.structs().keySet()));
        assertEquals(List.of("lights", "basis", "flags"), scene.members().stream().map(StructMember::name).toList());
        assertTrue(registry.type("scene").isEmpty());
        assertTrue(registry.type("Scene", VectorType.class).isEmpty());
        assertTrue(registry.struct("float").isEmpty());
        assertTrue(registry.type("Missing").isEmpty());
        assertTrue(TypeRegistry.builtin("Missing").isEmpty());
        assertTrue(TypeRegistry.builtinName(scene).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "lights[1].position.x",
            "lights[1].position.r",
            "lights[1].position.s",
            "lights[1].position[0]"})
    void testSceneComponents(String path) {
        TypeRegistry registry = registry(SCENE_SOURCE);

        assertEquals(ScalarType.FLOAT, registry.fieldType("Scene", path).orElseThrow());
        assertEquals(4, registry.getFieldOrdinal("Scene", path));
        assertEquals(ScalarType.FLOAT, registry.fieldType("Scene", "lights[1]", "position", "x").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "missing",
            "lights[2]",
            "lights[0].missing",
            "lights[0].position.w",
            "basis[2]",
            "basis[0].w",
            "flags.x"})
    void testSceneMissingComponents(String path) {
        TypeRegistry registry = registry(SCENE_SOURCE);

        assertTrue(registry.fieldType("Scene", path).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("Scene", path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            ".lights",
            "lights.",
            "lights..position",
            "lights[-1]",
            "lights[]",
            "lights[0",
            "lights[0]position"})
    void testSceneBadPaths(String path) {
        TypeRegistry registry = registry(SCENE_SOURCE);

        assertThrows(IllegalArgumentException.class, () -> registry.fieldType("Scene", path));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("Scene", path));
    }

    @Test
    void testSceneFieldTypes() {
        TypeRegistry registry = registry(SCENE_SOURCE);
        StructType scene = registry.struct("Scene").orElseThrow();

        assertSame(scene, registry.fieldType("Scene").orElseThrow());
        assertInstanceOf(ArrayType.class, registry.fieldType(scene, "lights").orElseThrow());
        assertEquals(new VectorType(ScalarType.FLOAT, 3), registry.fieldType("Scene", "basis[1]").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("Scene"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("Scene", "lights[0].position"));
        assertThrows(IllegalArgumentException.class, () -> registry.flattenedUnitCount("Missing"));
    }

    @Test
    void testSceneTraversal() {
        TypeRegistry registry = registry(SCENE_SOURCE);
        List<String> paths = new ArrayList<>();
        List<GlslType> leaves = new ArrayList<>();
        registry.traverse("Scene", (path, type) -> {
            paths.add(path);
            leaves.add(type);
        });

        assertEquals(List.of(
                "lights[0].position.x",
                "lights[0].position.y",
                "lights[0].position.z",
                "lights[0].intensity",
                "lights[1].position.x",
                "lights[1].position.y",
                "lights[1].position.z",
                "lights[1].intensity",
                "basis[0].x",
                "basis[0].y",
                "basis[0].z",
                "basis[1].x",
                "basis[1].y",
                "basis[1].z",
                "flags"), paths);

        assertEquals(15, registry.flattenedUnitCount("Scene"));
        assertEquals(15, registry.flattenedUnitCount(registry.struct("Scene").orElseThrow()));
        for (int i = 0; i < paths.size(); i++) {
            GlslType expected = i == 14 ? ScalarType.UINT : ScalarType.FLOAT;
            assertEquals(expected, leaves.get(i), paths.get(i));
            assertEquals(expected, registry.fieldType("Scene", paths.get(i)).orElseThrow(), paths.get(i));
            assertEquals(i, registry.getFieldOrdinal("Scene", paths.get(i)), paths.get(i));
        }
    }
}
