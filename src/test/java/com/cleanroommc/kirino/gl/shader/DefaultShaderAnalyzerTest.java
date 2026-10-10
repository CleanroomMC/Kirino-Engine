package com.cleanroommc.kirino.gl.shader;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultShaderAnalyzerTest {

    private static ShaderMeta analyze(String source) {
        ShaderMeta meta = new DefaultShaderAnalyzer().analyze(source);
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
}
