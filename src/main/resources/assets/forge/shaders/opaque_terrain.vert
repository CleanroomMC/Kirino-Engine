#version 430 core

struct Vertex
{
    vec3 pos;
    vec2 texCoord;
};

struct MeshletRange
{
    uint firstIndex;
    uint indexCount;
    uint vertexCount;
};

layout(std430, binding = 1) readonly buffer OutputVertices
{
    Vertex vertices[];
};

layout(std430, binding = 2) readonly buffer OutputIndices
{
    int indices[];
};

layout(std430, binding = 4) buffer MeshletRanges
{
    MeshletRange ranges[];
};

uniform vec3 worldOffset;
uniform mat4 viewRot;
uniform mat4 projection;

out vec2 TexCoord;

//// debug
//bool resolveIndex(uint globalIndex, out uint indexSlot)
//{
//    uint accumulated = 0u;
//
//    for (uint m = 0u; m < 16384; ++m)
//    {
//        uint count = ranges[m].indexCount;
//
//        if (globalIndex < accumulated + count)
//        {
//            uint localIndex = globalIndex - accumulated;
//            indexSlot = ranges[m].firstIndex + localIndex;
//            return true;
//        }
//
//        accumulated += count;
//    }
//
//    indexSlot = 0u;
//    return false;
//}

void main()
{
//    uint indexSlot;
//    bool found = resolveIndex(gl_VertexID, indexSlot);
//    if (!found)
//    {
//        gl_Position = vec4(0.0);
//        TexCoord = vec2(0.0);
//        return;
//    }

    if (uint(gl_VertexID) >= ranges[3].indexCount) return;

    uint indexSlot = 3456u + uint(gl_VertexID);
    uint vid = uint(indices[indexSlot]);
    Vertex v = vertices[vid];

    gl_Position = projection * viewRot * vec4(v.pos - worldOffset, 1.0);
    TexCoord = v.texCoord;
}
