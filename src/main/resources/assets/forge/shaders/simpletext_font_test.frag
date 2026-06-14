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
    vec2 texel = vec2(1.0, 1.0) / textureSize(atlas, 0);
    float d0 = texture(atlas, UV).r;
    float d1 = texture(atlas, UV + vec2(1, 0) * texel).r;
    float d2 = texture(atlas, UV - vec2(1, 0) * texel).r;
    float d3 = texture(atlas, UV + vec2(0, 1) * texel).r;
    float d4 = texture(atlas, UV - vec2(0, 1) * texel).r;

    float dist = (d0 * 4 + d1 + d2 + d3 + d4) / 8.0;

    float w = fwidth(dist);
    w = max(w, 0.001);

    float edge = 0.5;
    float softness = 1.0;

    float alpha = smoothstep(edge - w * softness, edge + w * softness, dist);

    vec4 color = unpackARGB(Color);
    FragColor = vec4(color.rgb, color.a * alpha);
}
