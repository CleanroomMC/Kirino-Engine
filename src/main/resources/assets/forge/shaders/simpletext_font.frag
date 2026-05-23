#version 430 core

in vec2 UV;

flat in uint Color;
flat in uint Page;
flat in uint Hint;

uniform sampler2D atlas;

out vec4 FragColor;

vec4 unpackARGB(uint c)
{
    return vec4(
        ((c >> 16u) & 255u) / 255.0,
        ((c >> 8u) & 255u) / 255.0,
        (c & 255u) / 255.0,
        ((c >> 24u) & 255u) / 255.0);
}

void main()
{
    ivec2 texSize = textureSize(atlas, 0);
    float dist = texture(atlas, UV).r;
    float w = fwidth(dist);
    w = max(w, 0.001);

    float edge = 0.5;
    float softness = 1.1;

    float alpha = smoothstep(edge - w * softness, edge + w * softness, dist);

    if (alpha <= 0.01) discard;

    vec4 color = unpackARGB(Color);
    FragColor = vec4(color.rgb, color.a * alpha);
}
