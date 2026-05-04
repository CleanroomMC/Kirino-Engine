#version 330 core

uniform sampler2D atlas;

in vec2 UV;

out vec4 FragColor;

void main()
{
    ivec2 texSize = textureSize(atlas, 0);
    float dist = texture(atlas, UV).r;
    float w = fwidth(UV.x) * texSize.x;
    w = clamp(w, 0.001, 0.02);

    float edge = 0.5;
    float softness = 1.1;

    float alpha = 1.0 - smoothstep(edge - w * softness, edge + w * softness, dist);

    // outline
//    float outline = 1.0 - smoothstep(edge - w * softness, edge + w * softness, dist);
//    float border = 1.0 - smoothstep(edge + 0.05 - w * softness, edge + 0.05 + w * softness, dist);
//    float alpha = border - outline;

    alpha = pow(alpha, 1.0 / 2.2);

    FragColor = vec4(1.0, 1.0, 1.0, alpha);
}
