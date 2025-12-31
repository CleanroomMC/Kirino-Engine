#version 330 core

in vec2 TexCoord;

out vec4 FragColor;

uniform sampler2D tex;

void main(void)
{
    vec4 baseColor = texture(tex, TexCoord);
    FragColor = baseColor;
}
