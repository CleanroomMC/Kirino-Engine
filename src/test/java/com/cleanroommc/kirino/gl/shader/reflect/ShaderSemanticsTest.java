package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.reflect.execution.ComputeExecution;
import com.cleanroommc.kirino.gl.shader.reflect.execution.VertexExecution;
import com.cleanroommc.kirino.gl.shader.reflect.layout.ImageFormat;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.Interpolation;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.MemoryQualifier;
import com.cleanroommc.kirino.gl.shader.reflect.resource.Block;
import com.cleanroommc.kirino.gl.shader.reflect.resource.StorageType;
import com.cleanroommc.kirino.gl.shader.reflect.resource.Uniform;
import com.cleanroommc.kirino.gl.shader.reflect.resource.Variable;
import com.cleanroommc.kirino.gl.shader.reflect.type.ArrayType;
import com.cleanroommc.kirino.gl.shader.reflect.type.AtomicCounterType;
import com.cleanroommc.kirino.gl.shader.reflect.type.ImageType;
import com.cleanroommc.kirino.gl.shader.reflect.type.ScalarType;
import com.cleanroommc.kirino.gl.shader.reflect.type.VectorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShaderSemanticsTest {

    private static ShaderSemantics semantics(String source, ShaderType stage) {
        ShaderMeta meta = new DefaultShaderAnalyzer().analyze(source, stage);
        assertNotNull(meta, "The GLSL fixture must parse successfully.");
        return new ShaderSemantics(meta);
    }

    @Test
    void testUniformAndInOut() {
        ShaderSemantics semantics = semantics("""
                uniform float exposure = 1.0;
                layout(location = 0) in vec3 position;
                layout(location = 1) flat out vec2 texCoord;
                """, ShaderType.VERTEX);

        assertEquals(VertexExecution.INSTANCE, semantics.execution);
        assertEquals(1, semantics.uniforms.size());
        assertEquals(1, semantics.inputs.size());
        assertEquals(1, semantics.outputs.size());

        Uniform exposure = semantics.uniforms.get("exposure");
        assertNotNull(exposure);
        assertEquals(StorageType.DEFAULT_UNIFORM, exposure.storageType());
        assertEquals(ScalarType.FLOAT, exposure.type());
        assertEquals("1.0", exposure.initializer());

        Variable position = semantics.inputs.get("position");
        assertNotNull(position);
        assertEquals(StorageType.STAGE_INPUT, position.storageType());
        assertEquals(new VectorType(ScalarType.FLOAT, 3), position.type());
        assertNotNull(position.layout().location());
        assertEquals(0, position.layout().location().requireValue());

        Variable texCoord = semantics.outputs.get("texCoord");
        assertNotNull(texCoord);
        assertEquals(StorageType.STAGE_OUTPUT, texCoord.storageType());
        assertNotNull(texCoord.layout().location());
        assertEquals(1, texCoord.layout().location().requireValue());
        assertEquals(Interpolation.FLAT, texCoord.qualifiers().interpolation());
    }

    @Test
    void testUniformImage() {
        ShaderSemantics semantics = semantics("""
                layout(rgba32f, binding = 2) readonly uniform image2D sourceImage;
                """, ShaderType.COMPUTE);

        Uniform image = semantics.uniforms.get("sourceImage");
        assertNotNull(image);
        assertInstanceOf(ImageType.class, image.type());
        assertEquals(ImageFormat.RGBA32F, image.layout().imageFormat());
        assertNotNull(image.layout().binding());
        assertEquals(2, image.layout().binding().requireValue());
        assertTrue(image.qualifiers().memory().contains(MemoryQualifier.READONLY));
    }

    @Test
    void testConstAndWorkgroup() {
        ShaderSemantics semantics = semantics("""
                const int COUNT = 4;
                layout(local_size_x = COUNT * 2, local_size_y = 2) in;
                shared vec4 tile[COUNT];
                """, ShaderType.COMPUTE);

        ComputeExecution execution = assertInstanceOf(ComputeExecution.class, semantics.execution);
        assertEquals(8, execution.localSizeX().requireValue());
        assertEquals(2, execution.localSizeY().requireValue());
        assertEquals(1, execution.localSizeZ().requireValue());
        assertEquals(16, execution.invocationsPerWorkgroup().orElseThrow());

        Variable count = semantics.variables.get("COUNT");
        assertNotNull(count);
        assertEquals(StorageType.CONSTANT, count.storageType());

        Variable tile = semantics.variables.get("tile");
        assertNotNull(tile);
        assertEquals(StorageType.SHARED, tile.storageType());
        assertTrue(tile.qualifiers().memory().contains(MemoryQualifier.COHERENT));

        ArrayType array = assertInstanceOf(ArrayType.class, tile.type());
        assertEquals(4, array.extent().length().orElseThrow());
        assertEquals(new VectorType(ScalarType.FLOAT, 4), array.elementType());
    }

    @Test
    void testUboSsbo() {
        ShaderSemantics semantics = semantics("""
                layout(std140, binding = 0) uniform Camera {
                    mat4 viewProjection;
                    vec4 eye;
                };
                layout(std430, binding = 1) buffer Particles {
                    vec4 positions[];
                };
                """, ShaderType.VERTEX);

        Block camera = semantics.uniformBlocks.get("Camera");
        assertNotNull(camera);
        assertEquals(StorageType.UNIFORM_BLOCK, camera.storageType());
        assertNotNull(camera.layout().binding());
        assertEquals(0, camera.layout().binding().requireValue());
        var cameraLayout = camera.computedLayout().orElseThrow();
        assertEquals(64, cameraLayout.member("eye").orElseThrow().offset());
        assertEquals(80, cameraLayout.size());

        Block particles = semantics.storageBlocks.get("Particles");
        assertNotNull(particles);
        assertEquals(StorageType.SHADER_STORAGE_BLOCK, particles.storageType());
        assertNotNull(particles.layout().binding());
        assertEquals(1, particles.layout().binding().requireValue());
        ArrayType positions = assertInstanceOf(ArrayType.class, particles.member("positions").orElseThrow().type());
        assertTrue(positions.extent().runtimeSized());
        var particlesLayout = particles.computedLayout().orElseThrow();
        assertTrue(particlesLayout.runtimeSized());
        assertEquals(48, particlesLayout.requiredSize(3));
    }

    @Test
    void testStd430Vec3Alignment() {
        ShaderSemantics semantics = semantics("""
                layout(std430, binding = 0) buffer Data {
                    float prefix;
                    vec3 position;
                    float weight;
                    vec3 normals[2];
                    float tail;
                };
                """, ShaderType.COMPUTE);

        Block data = semantics.storageBlocks.get("Data");
        assertNotNull(data);
        var layout = data.computedLayout().orElseThrow();
        var position = layout.member("position").orElseThrow();
        var normals = layout.member("normals").orElseThrow();

        assertEquals(0, layout.member("prefix").orElseThrow().offset());
        assertEquals(16, position.offset());
        assertEquals(16, position.layout().alignment());
        assertEquals(12, position.layout().size());
        assertEquals(28, layout.member("weight").orElseThrow().offset());
        assertEquals(32, normals.offset());
        assertEquals(16, normals.layout().arrayStride());
        assertEquals(48, layout.member("normals[1]").orElseThrow().offset());
        assertEquals(64, layout.member("tail").orElseThrow().offset());

        assertEquals(16, layout.alignment());
        assertEquals(80, layout.size());
    }

    @Test
    void testAtomicCounters() {
        ShaderSemantics semantics = semantics("""
                layout(binding = 0) uniform atomic_uint counters[4], afterArray;
                layout(binding = 0) uniform atomic_uint grid[2][3];
                layout(binding = 0) uniform atomic_uint afterGrid;
                layout(binding = 1) uniform atomic_uint independent;
                """, ShaderType.FRAGMENT);

        Uniform counters = semantics.uniforms.get("counters");
        assertNotNull(counters);
        assertEquals(StorageType.ATOMIC_COUNTER, counters.storageType());
        ArrayType array = assertInstanceOf(ArrayType.class, counters.type());
        assertEquals(4, array.extent().length().orElseThrow());
        assertEquals(AtomicCounterType.INSTANCE, array.elementType());

        Uniform grid = semantics.uniforms.get("grid");
        assertNotNull(grid);
        ArrayType outer = assertInstanceOf(ArrayType.class, grid.type());
        ArrayType inner = assertInstanceOf(ArrayType.class, outer.elementType());
        assertEquals(2, outer.extent().length().orElseThrow());
        assertEquals(3, inner.extent().length().orElseThrow());
        assertEquals(AtomicCounterType.INSTANCE, inner.elementType());

        String[] names = {"counters", "afterArray", "grid", "afterGrid", "independent"};
        long[] expectedOffsets = {0, 16, 20, 44, 0};
        for (int i = 0; i < names.length; i++) {
            Uniform uniform = semantics.uniforms.get(names[i]);
            assertNotNull(uniform);
            assertNotNull(uniform.layout().offset());
            assertEquals(expectedOffsets[i], uniform.layout().offset().requireValue(), names[i]);
        }

        Uniform afterArray = semantics.uniforms.get("afterArray");
        assertNotNull(afterArray);
        assertEquals(AtomicCounterType.INSTANCE, afterArray.type());

        Uniform afterGrid = semantics.uniforms.get("afterGrid");
        assertNotNull(afterGrid);
        assertEquals(AtomicCounterType.INSTANCE, afterGrid.type());
    }

    @Test
    void testBadAtomicCounterOffsets() {
        assertThrows(IllegalArgumentException.class, () -> semantics("""
                layout(binding = 0, offset = 2) uniform atomic_uint counter;
                """, ShaderType.FRAGMENT));
    }
}
