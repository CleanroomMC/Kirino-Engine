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

float directionalShadow(vec2 uv, vec2 texel, float outerEdge, float w)
{
    const vec2 shadowDir = normalize(vec2(1.0, 1.0));
    const int shadowSteps = 6;

    float shadow = 0.0;

    for (int i = 0; i <= shadowSteps; i++) {
        vec2 shadowUV = uv - shadowDir * texel * float(i);
        float dist = texture(atlas, shadowUV).r;
        float mask = smoothstep(outerEdge - w, outerEdge + w, dist);
        shadow = max(shadow, mask);
    }

    return shadow;
}

void main()
{
    float boldness = 0.0;
    bool enableOutline = true;
    bool enableShadow = true;

    vec2 texel = 1.0 / vec2(textureSize(atlas, 0));

    float edge = 0.5;
    float outlineThickness = enableOutline ? 0.05 : 0.0;

    vec4 color = unpackARGB(Color);
    vec4 outlineColor = vec4(0.0, 0.0, 0.0, color.a);
    vec4 shadowColor = vec4(0.0, 0.0, 0.0, color.a * 0.75);

    float dist = texture(atlas, UV).r;
    float w = max(fwidth(dist), 0.001);

    float fillEdge = edge - boldness;
    float outerEdge = fillEdge - outlineThickness;

    float fill = smoothstep(fillEdge - w, fillEdge + w, dist);
    float outer = enableOutline ? smoothstep(outerEdge - w, outerEdge + w, dist) : fill;
    float stroke = enableOutline ? max(outer - fill, 0.0) : 0.0;

    float shadow = enableShadow ? directionalShadow(UV, texel, outerEdge, w) : 0.0;

    float fillAlpha = fill * color.a;
    float outlineAlpha = stroke * outlineColor.a;

    vec3 mainPremulRgb = color.rgb * fillAlpha + outlineColor.rgb * outlineAlpha * (1.0 - fillAlpha);
    float mainAlpha = fillAlpha + outlineAlpha * (1.0 - fillAlpha);
    float shadowAlpha = shadow * shadowColor.a;
    vec3 premulRgb = mainPremulRgb + shadowColor.rgb * shadowAlpha * (1.0 - mainAlpha);

    float alpha = mainAlpha + shadowAlpha * (1.0 - mainAlpha);
    vec3 rgb = alpha > 0.0 ? premulRgb / alpha : vec3(0.0);

    FragColor = vec4(rgb, alpha);
}
