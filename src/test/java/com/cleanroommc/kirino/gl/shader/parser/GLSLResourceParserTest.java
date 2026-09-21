package com.cleanroommc.kirino.gl.shader.parser;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GLSLResourceParserTest {

    private static ASTTranslationUnit parse(String source) throws ParseException {
        return new GLSLResourceParser(new StringReader(source)).TranslationUnit();
    }

    private static Object value(Node node) {
        return ((SimpleNode) node).jjtGetValue();
    }

    private static void assertMember(ASTMemberDeclaration member, String type, String name) {
        assertEquals(type, value(onlyDirectChild(member, ASTTypeSpecifier.class)));
        assertEquals(name, value(onlyDescendant(member, ASTDeclarator.class)));
    }

    private static void assertLayout(Node root, String expectedName, String expectedExpression) {
        for (ASTLayoutQualifierItem item : descendants(root, ASTLayoutQualifierItem.class)) {
            GLSLResourceParser.LayoutQualifierValue qualifier = (GLSLResourceParser.LayoutQualifierValue) value(item);
            if (expectedName.equals(qualifier.name())) {
                assertEquals(expectedExpression, qualifier.expression());
                return;
            }
        }

        fail("Missing layout qualifier '" + expectedName + "'.");
    }

    private static <T extends Node> T onlyDirectChild(Node root, Class<T> type) {
        List<T> matches = directChildren(root, type);
        assertEquals(1, matches.size(), "Expected exactly one direct " + type.getSimpleName());
        return matches.getFirst();
    }

    private static <T extends Node> T onlyDescendant(Node root, Class<T> type) {
        List<T> matches = descendants(root, type);
        assertEquals(1, matches.size(), "Expected exactly one descendant " + type.getSimpleName());
        return matches.getFirst();
    }

    private static <T extends Node> List<T> directChildren(Node root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            Node child = root.jjtGetChild(i);
            if (type.isInstance(child)) {
                matches.add(type.cast(child));
            }
        }
        return matches;
    }

    private static <T extends Node> List<T> descendants(Node root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        collectDescendants(root, type, matches);
        return matches;
    }

    private static <T extends Node> void collectDescendants(Node root, Class<T> type, List<T> matches) {
        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            Node child = root.jjtGetChild(i);
            if (type.isInstance(child)) {
                matches.add(type.cast(child));
            }
            collectDescendants(child, type, matches);
        }
    }

    @Test
    public void test1() throws Exception {
        ASTTranslationUnit root = parse(
                        """
                        layout(binding = 3) uniform sampler2D albedo;
                        uniform highp vec4 tint = vec4(1.0);
                        layout(binding = BASE_BINDING + 2) uniform image2D images[4];
                        uniform vec4 first, second[2], third = vec4(1.0, 0.5, 0.25, 1.0);
                        """);

        List<ASTUniformDeclaration> uniforms = directChildren(root, ASTUniformDeclaration.class);
        assertEquals(4, uniforms.size());

        ASTUniformDeclaration albedo = uniforms.getFirst();
        assertEquals("uniform", value(onlyDirectChild(albedo, ASTStorageQualifier.class)));
        assertEquals("sampler2D", value(onlyDirectChild(albedo, ASTTypeSpecifier.class)));
        assertEquals("albedo", value(onlyDescendant(albedo, ASTDeclarator.class)));
        assertLayout(albedo, "binding", "3");

        ASTUniformDeclaration tint = uniforms.get(1);
        assertEquals("vec4", value(onlyDirectChild(tint, ASTTypeSpecifier.class)));
        assertEquals("highp", value(onlyDirectChild(tint, ASTQualifier.class)));
        assertEquals("vec4(1.0)", value(onlyDescendant(tint, ASTInitializer.class)));

        ASTUniformDeclaration images = uniforms.get(2);
        assertLayout(images, "binding", "BASE_BINDING+2");
        assertEquals("4", value(onlyDescendant(images, ASTArrayDimension.class)));

        List<ASTDeclarator> declarators = descendants(uniforms.get(3), ASTDeclarator.class);
        assertEquals(3, declarators.size());
        assertEquals("first", value(declarators.get(0)));
        assertEquals("second", value(declarators.get(1)));
        assertEquals("third", value(declarators.get(2)));
        assertEquals("2", value(onlyDescendant(declarators.get(1), ASTArrayDimension.class)));
        assertEquals("vec4(1.0,0.5,0.25,1.0)", value(onlyDescendant(declarators.get(2), ASTInitializer.class)));
    }

    @Test
    public void test2() throws Exception {
        ASTTranslationUnit root = parse(
                """
                        struct Material {
                            vec4 baseColor;
                            mat3 tangentFrame;
                            float weights[4];
                        };
                        uniform Material material;
                        """);

        assertEquals(2, root.jjtGetNumChildren());

        ASTStructDeclaration declaration = onlyDirectChild(root, ASTStructDeclaration.class);
        ASTStructSpecifier specifier = onlyDirectChild(declaration, ASTStructSpecifier.class);
        assertEquals("Material", value(onlyDirectChild(specifier, ASTStructName.class)));

        List<ASTMemberDeclaration> members = directChildren(specifier, ASTMemberDeclaration.class);
        assertEquals(3, members.size());
        assertMember(members.get(0), "vec4", "baseColor");
        assertMember(members.get(1), "mat3", "tangentFrame");
        assertMember(members.get(2), "float", "weights");
        assertEquals("4", value(onlyDescendant(members.get(2), ASTArrayDimension.class)));

        ASTUniformDeclaration uniform = onlyDirectChild(root, ASTUniformDeclaration.class);
        assertEquals("Material", value(onlyDirectChild(uniform, ASTTypeSpecifier.class)));
        assertEquals("material", value(onlyDescendant(uniform, ASTDeclarator.class)));
    }

    @Test
    public void test3() throws Exception {
        ASTTranslationUnit root = parse(
                """
                        layout(std140, binding = CAMERA_BINDING, row_major)
                        uniform CameraBlock {
                            mat4 view;
                            mat4 projection;
                            vec4 frustumPlanes[6];
                        } camera;
                        """);

        ASTInterfaceBlockDeclaration block = onlyDirectChild(root, ASTInterfaceBlockDeclaration.class);
        assertEquals("uniform", value(onlyDirectChild(block, ASTStorageQualifier.class)));
        assertEquals("CameraBlock", value(onlyDirectChild(block, ASTBlockName.class)));
        assertEquals("camera", value(onlyDirectChild(block, ASTInstanceDeclarator.class)));

        assertLayout(block, "std140", null);
        assertLayout(block, "binding", "CAMERA_BINDING");
        assertLayout(block, "row_major", null);

        List<ASTMemberDeclaration> members = directChildren(block, ASTMemberDeclaration.class);
        assertEquals(3, members.size());
        assertMember(members.get(0), "mat4", "view");
        assertMember(members.get(1), "mat4", "projection");
        assertMember(members.get(2), "vec4", "frustumPlanes");
        assertEquals("6", value(onlyDescendant(members.get(2), ASTArrayDimension.class)));
    }

    @Test
    public void test4() throws Exception {
        ASTTranslationUnit root = parse(
                """
                        layout(std430, binding = 5) readonly buffer ParticleBuffer {
                            uint count;
                            Particle particles[];
                        };
                        """);

        ASTInterfaceBlockDeclaration block = onlyDirectChild(root, ASTInterfaceBlockDeclaration.class);
        assertEquals("buffer", value(onlyDirectChild(block, ASTStorageQualifier.class)));
        assertEquals("ParticleBuffer", value(onlyDirectChild(block, ASTBlockName.class)));
        assertTrue(directChildren(block, ASTInstanceDeclarator.class).isEmpty());
        assertEquals("readonly", value(onlyDirectChild(block, ASTQualifier.class)));
        assertLayout(block, "std430", null);
        assertLayout(block, "binding", "5");

        List<ASTMemberDeclaration> members = directChildren(block, ASTMemberDeclaration.class);
        assertEquals(2, members.size());
        assertMember(members.get(0), "uint", "count");
        assertMember(members.get(1), "Particle", "particles");
        assertNull(value(onlyDescendant(members.get(1), ASTArrayDimension.class)));
    }

    @Test
    public void test5() throws Exception {
        ASTTranslationUnit root = parse(
                """
                        #version 450 core
                        #define TEXTURE_BINDING 7
                        layout(location = 0) in vec3 position;
                        layout(location = 0) out vec4 fragmentColor;
                        const int SAMPLE_COUNT = 4;
                        void helper() {
                            struct LocalOnly { int ignored; };
                            LocalOnly value;
                        }
                        layout(binding = TEXTURE_BINDING) uniform sampler2D colorTexture;
                        void main() {
                            fragmentColor = texture(colorTexture, vec2(0.0));
                        }
                        """);

        assertEquals(1, root.jjtGetNumChildren());
        assertInstanceOf(ASTUniformDeclaration.class, root.jjtGetChild(0));
        assertTrue(descendants(root, ASTStructDeclaration.class).isEmpty());
        assertLayout(root, "binding", "TEXTURE_BINDING");
        assertEquals("colorTexture", value(onlyDescendant(root, ASTDeclarator.class)));
    }

    @Test
    public void test6() throws Exception {
        ASTTranslationUnit root = parse(
                """
                        #version 450
                        
                        layout(binding = 1) uniform sampler2D textureMap;
                        """);

        ASTUniformDeclaration uniform = onlyDirectChild(root, ASTUniformDeclaration.class);
        assertEquals(3, uniform.jjtGetFirstToken().beginLine);
        assertEquals("layout", uniform.jjtGetFirstToken().image);
        assertEquals(";", uniform.jjtGetLastToken().image);
        assertSame(uniform.jjtGetFirstToken(), onlyDirectChild(uniform, ASTLayoutQualifier.class).jjtGetFirstToken());
    }

    @Test
    public void test7() {
        ParseException exception = assertThrows(ParseException.class,
                () -> parse("layout(binding = 0) uniform sampler2D missingSemicolon"));
        assertFalse(exception.getMessage().isEmpty());
    }
}
