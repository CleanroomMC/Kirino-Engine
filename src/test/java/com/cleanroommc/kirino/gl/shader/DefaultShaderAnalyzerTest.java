package com.cleanroommc.kirino.gl.shader;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultShaderAnalyzerTest {

    private static ShaderMeta analyze(String source) {
        ShaderMeta meta = new DefaultShaderAnalyzer().analyze(source, ShaderType.VERTEX);
        assertNotNull(meta);
        return meta;
    }

    private static String sourceAt(String source, ShaderMeta.SourceSpan span) {
        String[] lines = source.split("\\r\\n|\\r|\\n", -1);
        if (span.startLine() == span.endLine()) {
            return lines[span.startLine() - 1].substring(span.startColumn() - 1, span.endColumn());
        }
        StringBuilder result = new StringBuilder(lines[span.startLine() - 1].substring(span.startColumn() - 1));
        for (int line = span.startLine(); line < span.endLine() - 1; line++) {
            result.append('\n').append(lines[line]);
        }
        return result.append('\n').append(lines[span.endLine() - 1], 0, span.endColumn()).toString();
    }

    @Test
    void test1() {
        String source = """
                uniform layout(std140);
                uniform A { float a; };
                layout(column_major) uniform layout(row_major);
                uniform B { mat2 b; };
                buffer layout(std430);
                in layout(local_size_x = 8);
                layout(triangle_strip) out layout(max_vertices = 3);
                """;
        ShaderMeta meta = analyze(source);

        assertEquals(7, meta.topLevelDeclarations.size());
        assertSame(meta.globalLayouts.get(0), meta.topLevelDeclarations.get(0));
        assertSame(meta.interfaceBlocks.get(0), meta.topLevelDeclarations.get(1));
        assertSame(meta.globalLayouts.get(1), meta.topLevelDeclarations.get(2));
        assertSame(meta.interfaceBlocks.get(1), meta.topLevelDeclarations.get(3));
        assertEquals(List.of("uniform", "uniform", "buffer", "in", "out"),
                meta.globalLayouts.stream().map(ShaderMeta.GlobalLayoutDeclaration::storage).toList());
        assertEquals(List.of("column_major", "row_major"),
                meta.globalLayouts.get(1).layouts().stream().map(ShaderMeta.Layout::name).toList());
        assertEquals("layout(column_major) uniform layout(row_major);", sourceAt(source, meta.globalLayouts.get(1).span()));
        assertEquals("row_major", sourceAt(source, meta.globalLayouts.get(1).layouts().get(1).span()));
    }

    @Test
    void test2() {
        String source = """
                const struct S { float x; } s = S(1.0);
                struct T { float x; }[2] a[3], b;
                struct { vec3 pos; }[4] lights;
                uniform S value;
                """;
        ShaderMeta meta = analyze(source);

        assertEquals(3, meta.structs.size());

        ShaderMeta.StructDeclaration s = meta.structs.getFirst();
        assertEquals("S", s.name());
        assertEquals(List.of("const"), s.qualifiers());
        assertEquals("S(1.0)", s.declarators().getFirst().initializer());
        assertEquals("s = S(1.0)", sourceAt(source, s.declarators().getFirst().span()));
        assertEquals(source.lines().findFirst().orElseThrow(), sourceAt(source, s.span()));

        ShaderMeta.StructDeclaration t = meta.structs.get(1);
        assertEquals("2", t.arrayDimensions().getFirst().expression());
        assertEquals("[2]", sourceAt(source, t.arrayDimensions().getFirst().span()));
        assertEquals("3", t.declarators().get(0).arrayDimensions().getFirst().expression());
        assertTrue(t.declarators().get(1).arrayDimensions().isEmpty());

        assertNull(meta.structs.get(2).name());
        assertEquals("4", meta.structs.get(2).arrayDimensions().getFirst().expression());
        assertEquals("S", meta.uniforms.getFirst().type().name());
        assertSame(s, meta.topLevelDeclarations.getFirst());
        assertSame(meta.uniforms.getFirst(), meta.topLevelDeclarations.getLast());
    }

    @Test
    void test3() {
        String source = """
                // comment
                layout(std430, binding = 2) buffer B {
                    layout(offset = 16) vec4[2] a[3], b;
                    float tail[];
                } instances[4]; // comment
                layout(location = 0) out vec4 color;
                uniform struct { float x; }[2] values[3];
                """;
        ShaderMeta meta = analyze(source);

        ShaderMeta.InterfaceBlock block = meta.interfaceBlocks.getFirst();
        assertEquals(new ShaderMeta.SourceSpan(2, 1, 5, 15), block.span());
        assertTrue(sourceAt(source, block.span()).endsWith("} instances[4];"));
        assertEquals("binding = 2", sourceAt(source, block.layouts().get(1).span()));
        assertEquals("instances[4]", sourceAt(source, block.instance().span()));
        assertEquals("[4]", sourceAt(source, block.instance().arrayDimensions().getFirst().span()));

        ShaderMeta.Member member = block.members().getFirst();
        assertEquals("layout(offset = 16) vec4[2] a[3], b;", sourceAt(source, member.span()));
        assertEquals("offset = 16", sourceAt(source, member.layouts().getFirst().span()));
        assertEquals("vec4[2]", sourceAt(source, member.type().span()));
        assertEquals("[2]", sourceAt(source, member.type().arrayDimensions().getFirst().span()));
        assertEquals("a[3]", sourceAt(source, member.declarators().getFirst().span()));
        assertEquals("b", sourceAt(source, member.declarators().getLast().span()));

        ShaderMeta.ArrayDimension unsized = block.members().getLast().declarators().getFirst().arrayDimensions().getFirst();
        assertNull(unsized.expression());
        assertEquals("[]", sourceAt(source, unsized.span()));

        assertEquals("layout(location = 0) out vec4 color;", sourceAt(source, meta.inputOutputs.getFirst().span()));

        ShaderMeta.UniformDeclaration uniform = meta.uniforms.getFirst();
        assertEquals("uniform struct { float x; }[2] values[3];", sourceAt(source, uniform.span()));
        assertEquals("struct { float x; }[2]", sourceAt(source, uniform.type().span()));

        ShaderMeta.StructDeclaration inline = uniform.type().inlineStruct();
        assertNotNull(inline);
        assertNull(inline.name());
        assertEquals("struct { float x; }", sourceAt(source, inline.span()));
        assertTrue(inline.arrayDimensions().isEmpty());
        assertTrue(inline.declarators().isEmpty());
        assertEquals("float x;", sourceAt(source, inline.members().getFirst().span()));
    }

    @Test
    void test4() {
        String source = "#version 460\nvoid main() {}\nuniform float x; /* comment */\n";

        ShaderMeta.UniformDeclaration uniform = analyze(source).uniforms.getFirst();
        assertEquals(new ShaderMeta.SourceSpan(3, 1, 3, 16), uniform.span());
        assertEquals("uniform float x;", sourceAt(source, uniform.span()));
        assertEquals("float", sourceAt(source, uniform.type().span()));
        assertEquals("x", sourceAt(source, uniform.declarators().getFirst().span()));
    }

    @Test
    void test5() {
        String source = """
                const int COUNT = 2, TOTAL = COUNT * 2;
                shared vec4 tile[COUNT];
                float scratch;
                vec4 helper(vec4 value) {
                    const int LOCAL_ONLY = 3;
                    return value;
                }
                uniform vec4 values[TOTAL];
                """;
        ShaderMeta meta = analyze(source);

        assertEquals(3, meta.variables.size());
        assertEquals(List.of("const"), meta.variables.get(0).qualifiers());
        assertEquals(List.of("COUNT", "TOTAL"), meta.variables.get(0).declarators().stream()
                .map(ShaderMeta.Declarator::name).toList());
        assertEquals("COUNT*2", meta.variables.get(0).declarators().get(1).initializer());
        assertEquals("shared", meta.variables.get(1).storage());
        assertEquals("COUNT", meta.variables.get(1).declarators().getFirst().arrayDimensions().getFirst().expression());
        assertNull(meta.variables.get(2).storage());
        assertEquals(4, meta.topLevelDeclarations.size());
        assertSame(meta.variables.getFirst(), meta.topLevelDeclarations.getFirst());
        assertSame(meta.uniforms.getFirst(), meta.topLevelDeclarations.getLast());
        assertEquals("TOTAL", meta.uniforms.getFirst().declarators().getFirst().arrayDimensions().getFirst().expression());
        assertEquals("shared vec4 tile[COUNT];", sourceAt(source, meta.variables.get(1).span()));
    }

    @Test
    void test6() {
        String source = """
                precision mediump float;
                precision highp sampler2D;
                invariant gl_Position;
                precise first, second;
                uniform vec4 tint;
                """;
        ShaderMeta meta = analyze(source);

        assertEquals(2, meta.precisions.size());
        assertEquals("mediump", meta.precisions.getFirst().precision());
        assertEquals("sampler2D", meta.precisions.getLast().type().name());
        assertEquals("precision highp sampler2D;", sourceAt(source, meta.precisions.getLast().span()));
        assertEquals(2, meta.qualifiers.size());
        assertEquals(List.of("invariant"), meta.qualifiers.getFirst().qualifiers());
        assertEquals("gl_Position", meta.qualifiers.getFirst().declarators().getFirst().name());
        assertEquals(List.of("first", "second"), meta.qualifiers.getLast().declarators().stream()
                .map(ShaderMeta.Declarator::name).toList());
        assertEquals(5, meta.topLevelDeclarations.size());
    }

    @Test
    void test7() {
        String source = """
                subroutine vec4 Shade(const in vec3 normal, out vec4 values[2]);
                subroutine vec4 Other(vec3, out vec4[2]);
                subroutine void NoArgs(void);
                layout(index = 3) subroutine(Shade, Other) vec4 shade(const in vec3 normal, out vec4 values[2]) {
                    struct LocalOnly { float ignored; };
                    if (true) { values[0] = vec4(normal, 1.0); }
                    return values[0];
                }
                subroutine uniform Shade selected[2];
                uniform float after;
                """;
        ShaderMeta meta = analyze(source);

        assertEquals(3, meta.subroutineTypes.size());
        ShaderMeta.SubroutineTypeDeclaration shade = meta.subroutineTypes.getFirst();
        assertEquals("Shade", shade.name());
        assertEquals(List.of("const", "in"), shade.parameters().getFirst().qualifiers());
        assertEquals("normal", shade.parameters().getFirst().name());
        assertEquals("2", shade.parameters().getLast().arrayDimensions().getFirst().expression());
        assertNull(meta.subroutineTypes.get(1).parameters().getFirst().name());
        assertEquals("2", meta.subroutineTypes.get(1).parameters().getLast().type().arrayDimensions().getFirst().expression());
        assertTrue(meta.subroutineTypes.getLast().parameters().isEmpty());
        assertEquals(1, meta.subroutineFunctions.size());
        ShaderMeta.SubroutineFunctionDeclaration function = meta.subroutineFunctions.getFirst();
        assertEquals("shade", function.name());
        assertEquals(List.of("Shade", "Other"), function.compatibleTypes());
        assertEquals("3", function.layouts().getFirst().expression());
        assertTrue(function.definition());
        assertTrue(sourceAt(source, function.span()).endsWith("}"));
        assertTrue(meta.structs.isEmpty());
        assertEquals(2, meta.uniforms.size());
        assertEquals(List.of("subroutine"), meta.uniforms.getFirst().qualifiers());
        assertEquals("selected", meta.uniforms.getFirst().declarators().getFirst().name());
        assertEquals("after", meta.uniforms.getLast().declarators().getFirst().name());
        assertEquals(6, meta.topLevelDeclarations.size());
    }

    @Test
    void test8() {
        ShaderMeta meta = analyze("subroutine vec4 Shade(); subroutine(Shade) vec4 missingBody();");

        assertTrue(meta.subroutineTypes.getFirst().parameters().isEmpty());
        assertFalse(meta.subroutineFunctions.getFirst().definition());
    }

    @Test
    void test9() {
        ShaderAnalyzer analyzer = new DefaultShaderAnalyzer();

        assertNull(analyzer.analyze("precision highp;", ShaderType.VERTEX));
        assertNull(analyzer.analyze("const int COUNT = ;", ShaderType.VERTEX));
        assertNull(analyzer.analyze("subroutine vec4 Shade(", ShaderType.VERTEX));
        assertNull(analyzer.analyze("subroutine(Shade) vec4 broken() { return vec4(1.0);", ShaderType.VERTEX));
    }
}
