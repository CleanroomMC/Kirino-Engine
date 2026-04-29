#version 330 core

const vec4 pos[4] = vec4[](
    vec4(-1.0, -1.0, 0.0, 1.0),
    vec4(-1.0, 1.0, 0.0, 1.0),
    vec4(1.0, 1.0, 0.0, 1.0),
    vec4(1.0, -1.0, 0.0, 1.0));

void main()
{
    gl_Position = pos[gl_VertexID];
}
