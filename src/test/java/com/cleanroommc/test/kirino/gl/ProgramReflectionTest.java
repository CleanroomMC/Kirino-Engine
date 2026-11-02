package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.shader.reflect.ProgramReflection;
import com.cleanroommc.kirino.gl.shader.reflect.type.ScalarType;
import com.cleanroommc.kirino.gl.shader.reflect.type.VectorType;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GLTestExtension.class)
public class ProgramReflectionTest {

    private static final String SHADER_SRC = """
            #version 460
            layout(local_size_x = 1) in;
            layout(location = 0) uniform float scale;
            layout(std430, binding = 3) buffer Result {
                vec4 value;
            };

            void main() {
                value = vec4(scale);
            }
            """;

    @Test
    void testUniformAndSsbo() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.assumeGL46();
        GLTestExtension.submit(() -> {
            int shaderID = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
            int programID = GL20.glCreateProgram();
            GL20.glShaderSource(shaderID, SHADER_SRC);
            GL20.glCompileShader(shaderID);
            assertEquals(GL11.GL_TRUE, GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS), GL20.glGetShaderInfoLog(shaderID));

            GL20.glAttachShader(programID, shaderID);
            GL20.glLinkProgram(programID);
            assertEquals(GL11.GL_TRUE, GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS), GL20.glGetProgramInfoLog(programID));

            ProgramReflection reflection = new ProgramReflection(programID, ImmutableList.of());
            assertEquals(1, reflection.uniforms().size());
            var scale = reflection.uniform("scale").orElseThrow();
            assertEquals(ScalarType.FLOAT, scale.type());
            assertEquals(0, scale.location());
            assertTrue(reflection.uniformBlocks().isEmpty());

            assertEquals(1, reflection.storageBlocks().size());
            var result = reflection.storageBlock("Result").orElseThrow();
            assertEquals(3, result.binding());
            assertEquals(16, result.dataSize());
            var value = result.member("value").orElseThrow();
            assertEquals(new VectorType(ScalarType.FLOAT, 4), value.type());
            assertEquals(0, value.offset());

            GL43.glShaderStorageBlockBinding(programID, result.index(), 4);
            assertEquals(4, reflection.storageBlock("Result").orElseThrow().binding());
            assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
        }).join();
    }
}
