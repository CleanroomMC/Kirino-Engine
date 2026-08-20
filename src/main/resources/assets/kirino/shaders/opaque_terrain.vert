#version 430 core

// 32
struct Vertex
{
    vec3 pos;
    vec2 texCoord;
};

layout(std430, binding = 1) readonly buffer OutputVertices
{
    Vertex vertices[];
};

layout(std430, binding = 2) readonly buffer OutputIndices
{
    uint indices[];
};

layout(std430, binding = 5) readonly buffer DrawIndices
{
    uint drawIndices[];
};

uniform vec3 worldOffset;
uniform mat4 viewRot;
uniform mat4 projection;

out vec2 TexCoord;

#ifdef KIRINO_DEBUG
layout(std430, binding = 13) buffer TempBuffer
{
    vec4 temp[];
};
#endif

void main()
{
    uint actualIndex = drawIndices[uint(gl_VertexID)];
    uint vertexIndex = indices[actualIndex];
    Vertex v = vertices[vertexIndex];

    gl_Position = projection * viewRot * vec4(v.pos - worldOffset, 1.0);
    TexCoord = v.texCoord;

#ifdef KIRINO_DEBUG
    uint index = uint(gl_VertexID);
    if (index >= 1024) return;
    temp[index * 2].xyz = v.pos;
    temp[index * 2].w = actualIndex;
    temp[index * 2 + 1].x = vertexIndex;
#endif
}
