package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.reflect.type.*;
import org.lwjgl.opengl.*;

final class GlTypes {

    private GlTypes() {
    }

    static GlslType resolve(int token, int arraySize, String name) {
        return resolve(token, arraySize, name, false);
    }

    static GlslType resolve(int token, int arraySize, String name, boolean bufferVariable) {
        GlslType type = basic(token);
        if (arraySize > 1 || arraySize == 0 || name.endsWith("[0]")) {
            type = new ArrayType(type, arraySize == 0
                    ? bufferVariable ? ArrayExtent.RUNTIME_SIZED : ArrayExtent.UNSIZED
                    : ArrayExtent.sized(new IntegerExpression(Integer.toString(arraySize), (long) arraySize)));
        }
        return type;
    }

    static GlslType basic(int token) {
        String spelling = switch (token) {
            case GL20.GL_BOOL -> "bool";
            case GL20.GL_BOOL_VEC2 -> "bvec2";
            case GL20.GL_BOOL_VEC3 -> "bvec3";
            case GL20.GL_BOOL_VEC4 -> "bvec4";
            case GL11.GL_INT -> "int";
            case GL20.GL_INT_VEC2 -> "ivec2";
            case GL20.GL_INT_VEC3 -> "ivec3";
            case GL20.GL_INT_VEC4 -> "ivec4";
            case GL11.GL_UNSIGNED_INT -> "uint";
            case GL30.GL_UNSIGNED_INT_VEC2 -> "uvec2";
            case GL30.GL_UNSIGNED_INT_VEC3 -> "uvec3";
            case GL30.GL_UNSIGNED_INT_VEC4 -> "uvec4";
            case GL11.GL_FLOAT -> "float";
            case GL20.GL_FLOAT_VEC2 -> "vec2";
            case GL20.GL_FLOAT_VEC3 -> "vec3";
            case GL20.GL_FLOAT_VEC4 -> "vec4";
            case GL11.GL_DOUBLE -> "double";
            case GL40.GL_DOUBLE_VEC2 -> "dvec2";
            case GL40.GL_DOUBLE_VEC3 -> "dvec3";
            case GL40.GL_DOUBLE_VEC4 -> "dvec4";
            case GL20.GL_FLOAT_MAT2 -> "mat2";
            case GL21.GL_FLOAT_MAT2x3 -> "mat2x3";
            case GL21.GL_FLOAT_MAT2x4 -> "mat2x4";
            case GL21.GL_FLOAT_MAT3x2 -> "mat3x2";
            case GL20.GL_FLOAT_MAT3 -> "mat3";
            case GL21.GL_FLOAT_MAT3x4 -> "mat3x4";
            case GL21.GL_FLOAT_MAT4x2 -> "mat4x2";
            case GL21.GL_FLOAT_MAT4x3 -> "mat4x3";
            case GL20.GL_FLOAT_MAT4 -> "mat4";
            case GL40.GL_DOUBLE_MAT2 -> "dmat2";
            case GL40.GL_DOUBLE_MAT2x3 -> "dmat2x3";
            case GL40.GL_DOUBLE_MAT2x4 -> "dmat2x4";
            case GL40.GL_DOUBLE_MAT3x2 -> "dmat3x2";
            case GL40.GL_DOUBLE_MAT3 -> "dmat3";
            case GL40.GL_DOUBLE_MAT3x4 -> "dmat3x4";
            case GL40.GL_DOUBLE_MAT4x2 -> "dmat4x2";
            case GL40.GL_DOUBLE_MAT4x3 -> "dmat4x3";
            case GL40.GL_DOUBLE_MAT4 -> "dmat4";
            case GL20.GL_SAMPLER_1D -> "sampler1D";
            case GL42.GL_IMAGE_1D -> "image1D";
            case GL30.GL_INT_SAMPLER_1D -> "isampler1D";
            case GL42.GL_INT_IMAGE_1D -> "iimage1D";
            case GL30.GL_UNSIGNED_INT_SAMPLER_1D -> "usampler1D";
            case GL42.GL_UNSIGNED_INT_IMAGE_1D -> "uimage1D";
            case GL20.GL_SAMPLER_1D_SHADOW -> "sampler1DShadow";
            case GL20.GL_SAMPLER_2D -> "sampler2D";
            case GL42.GL_IMAGE_2D -> "image2D";
            case GL30.GL_INT_SAMPLER_2D -> "isampler2D";
            case GL42.GL_INT_IMAGE_2D -> "iimage2D";
            case GL30.GL_UNSIGNED_INT_SAMPLER_2D -> "usampler2D";
            case GL42.GL_UNSIGNED_INT_IMAGE_2D -> "uimage2D";
            case GL20.GL_SAMPLER_2D_SHADOW -> "sampler2DShadow";
            case GL20.GL_SAMPLER_3D -> "sampler3D";
            case GL42.GL_IMAGE_3D -> "image3D";
            case GL30.GL_INT_SAMPLER_3D -> "isampler3D";
            case GL42.GL_INT_IMAGE_3D -> "iimage3D";
            case GL30.GL_UNSIGNED_INT_SAMPLER_3D -> "usampler3D";
            case GL42.GL_UNSIGNED_INT_IMAGE_3D -> "uimage3D";
            case GL20.GL_SAMPLER_CUBE -> "samplerCube";
            case GL42.GL_IMAGE_CUBE -> "imageCube";
            case GL30.GL_INT_SAMPLER_CUBE -> "isamplerCube";
            case GL42.GL_INT_IMAGE_CUBE -> "iimageCube";
            case GL30.GL_UNSIGNED_INT_SAMPLER_CUBE -> "usamplerCube";
            case GL42.GL_UNSIGNED_INT_IMAGE_CUBE -> "uimageCube";
            case GL30.GL_SAMPLER_CUBE_SHADOW -> "samplerCubeShadow";
            case GL31.GL_SAMPLER_2D_RECT -> "sampler2DRect";
            case GL42.GL_IMAGE_2D_RECT -> "image2DRect";
            case GL31.GL_INT_SAMPLER_2D_RECT -> "isampler2DRect";
            case GL42.GL_INT_IMAGE_2D_RECT -> "iimage2DRect";
            case GL31.GL_UNSIGNED_INT_SAMPLER_2D_RECT -> "usampler2DRect";
            case GL42.GL_UNSIGNED_INT_IMAGE_2D_RECT -> "uimage2DRect";
            case GL31.GL_SAMPLER_2D_RECT_SHADOW -> "sampler2DRectShadow";
            case GL30.GL_SAMPLER_1D_ARRAY -> "sampler1DArray";
            case GL42.GL_IMAGE_1D_ARRAY -> "image1DArray";
            case GL30.GL_INT_SAMPLER_1D_ARRAY -> "isampler1DArray";
            case GL42.GL_INT_IMAGE_1D_ARRAY -> "iimage1DArray";
            case GL30.GL_UNSIGNED_INT_SAMPLER_1D_ARRAY -> "usampler1DArray";
            case GL42.GL_UNSIGNED_INT_IMAGE_1D_ARRAY -> "uimage1DArray";
            case GL30.GL_SAMPLER_1D_ARRAY_SHADOW -> "sampler1DArrayShadow";
            case GL30.GL_SAMPLER_2D_ARRAY -> "sampler2DArray";
            case GL42.GL_IMAGE_2D_ARRAY -> "image2DArray";
            case GL30.GL_INT_SAMPLER_2D_ARRAY -> "isampler2DArray";
            case GL42.GL_INT_IMAGE_2D_ARRAY -> "iimage2DArray";
            case GL30.GL_UNSIGNED_INT_SAMPLER_2D_ARRAY -> "usampler2DArray";
            case GL42.GL_UNSIGNED_INT_IMAGE_2D_ARRAY -> "uimage2DArray";
            case GL30.GL_SAMPLER_2D_ARRAY_SHADOW -> "sampler2DArrayShadow";
            case GL40.GL_SAMPLER_CUBE_MAP_ARRAY -> "samplerCubeArray";
            case GL42.GL_IMAGE_CUBE_MAP_ARRAY -> "imageCubeArray";
            case GL40.GL_INT_SAMPLER_CUBE_MAP_ARRAY -> "isamplerCubeArray";
            case GL42.GL_INT_IMAGE_CUBE_MAP_ARRAY -> "iimageCubeArray";
            case GL40.GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY -> "usamplerCubeArray";
            case GL42.GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY -> "uimageCubeArray";
            case GL40.GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW -> "samplerCubeArrayShadow";
            case GL31.GL_SAMPLER_BUFFER -> "samplerBuffer";
            case GL42.GL_IMAGE_BUFFER -> "imageBuffer";
            case GL31.GL_INT_SAMPLER_BUFFER -> "isamplerBuffer";
            case GL42.GL_INT_IMAGE_BUFFER -> "iimageBuffer";
            case GL31.GL_UNSIGNED_INT_SAMPLER_BUFFER -> "usamplerBuffer";
            case GL42.GL_UNSIGNED_INT_IMAGE_BUFFER -> "uimageBuffer";
            case GL32.GL_SAMPLER_2D_MULTISAMPLE -> "sampler2DMS";
            case GL42.GL_IMAGE_2D_MULTISAMPLE -> "image2DMS";
            case GL32.GL_INT_SAMPLER_2D_MULTISAMPLE -> "isampler2DMS";
            case GL42.GL_INT_IMAGE_2D_MULTISAMPLE -> "iimage2DMS";
            case GL32.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE -> "usampler2DMS";
            case GL42.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE -> "uimage2DMS";
            case GL32.GL_SAMPLER_2D_MULTISAMPLE_ARRAY -> "sampler2DMSArray";
            case GL42.GL_IMAGE_2D_MULTISAMPLE_ARRAY -> "image2DMSArray";
            case GL32.GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY -> "isampler2DMSArray";
            case GL42.GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY -> "iimage2DMSArray";
            case GL32.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY -> "usampler2DMSArray";
            case GL42.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY -> "uimage2DMSArray";
            case GL42.GL_UNSIGNED_INT_ATOMIC_COUNTER -> "atomic_uint";
            default -> null;
        };
        return spelling == null
                ? new UnresolvedType("GL type 0x" + Integer.toHexString(token))
                : TypeRegistry.builtin(spelling).orElseThrow();
    }
}
