#version 430 core

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
    int indices[];
};

uniform vec3 worldOffset;
uniform mat4 viewRot;
uniform mat4 projection;

out vec2 TexCoord;

void main()
{
    uint vid = uint(indices[gl_VertexID]);
    Vertex v = vertices[vid];

    gl_Position = projection * viewRot * vec4(v.pos - worldOffset, 1.0);
    TexCoord = v.texCoord;
}
