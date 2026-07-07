package com.cleanroommc.test.kirino.gl.ksml;

import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.utils.KsmlcUtils;
import com.cleanroommc.ksmlc.KSMLCompiler;
import com.cleanroommc.ksmlc.SourceFile;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GLTestExtension.class)
public class KsmlcTest {

    private static final Logger LOGGER = LogManager.getLogger();

    private void testSubmitToGL(String shaderSource, ShaderType shaderType) {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            int shaderID = GL20.glCreateShader(shaderType.glValue);
            GL20.glShaderSource(shaderID, shaderSource);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);
        });
    }

    @Test
    public void testSimpleCompile() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_simple.glsl"));
        SourceFile ksml = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_1.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        testSubmitToGL(shaderSource, ShaderType.VERTEX);
    }

    @Test
    public void testTwoKsmlImports() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_two_imports.glsl"));
        SourceFile ksml1 = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_1.ksml"));
        SourceFile ksml2 = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_2.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml1, ksml2}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        testSubmitToGL(shaderSource, ShaderType.VERTEX);
    }

    @Test
    public void testNamespace() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_namespace.glsl"));
        SourceFile ksml1 = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_2.ksml"));
        SourceFile ksml2 = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_3.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml1, ksml2}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        testSubmitToGL(shaderSource, ShaderType.VERTEX);
    }

    @Test
    public void testLineDirective() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_line_directive.glsl"));
        SourceFile ksml = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_4.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        assertTrue(shaderSource.contains("#line 11"));
        assertTrue(shaderSource.contains("#line 8"));

        if (GLTestExtension.isInitialized()) {
            testSubmitToGL(shaderSource, ShaderType.VERTEX);
        }
    }

    @Test
    public void testLineDirectiveBehavior() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_line_directive_behavior.glsl"));
        SourceFile ksml = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std_4.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            int shaderID = GL20.glCreateShader(ShaderType.VERTEX.glValue);
            GL20.glShaderSource(shaderID, shaderSource);
            GL20.glCompileShader(shaderID);

            assertEquals(GL11.GL_FALSE, GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS));

            String errorLog = GL20.glGetShaderInfoLog(shaderID, 1024);

            LOGGER.info("error log:\n{}", errorLog);
        });
    }
}
