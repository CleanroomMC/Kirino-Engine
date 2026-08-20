#version 430 core

in vec2 UV;
flat in vec4 UVRect;
flat in uint Color;
flat in uint Page;
flat in uint Hint;

uniform sampler2D atlas;
uniform uint tick;
uniform uint sdfSpread;

out vec4 FragColor;

vec4 unpackARGB(uint c)
{
    return vec4(
        ((c >> 16u) & 255u) / 255.0,
        ((c >> 8u) & 255u) / 255.0,
        (c & 255u) / 255.0,
        ((c >> 24u) & 255u) / 255.0);
}

uint hashUint(uint x)
{
    x ^= x >> 16u;
    x *= 0x7feb352du;
    x ^= x >> 15u;
    x *= 0x846ca68bu;
    x ^= x >> 16u;
    return x;
}

float hash01(uint x)
{
    return float(hashUint(x)) * (1.0 / 4294967295.0);
}

const uint SALT_TICK = 0x9e3779b9u;
const uint SALT_BAND_A = 0x85ebca6bu;
const uint SALT_BAND_B = 0x27d4eb2du;
const uint SALT_MIRROR_AXIS = 0x6d2b79f5u;
const uint SALT_MIRROR = 0xa511e9b3u;
const uint SALT_VERTICAL = 0x165667b1u;
const uint SALT_MODE = 0xd3a2646cu;
const uint SALT_CELL = 0xc2b2ae35u;
const uint SALT_STROKE_ENABLE = 0xb5297a4du;
const uint SALT_STROKE_X = 0x1b56c4e9u;
const uint SALT_STROKE_TILT = 0x94d049bbu;
const uint SALT_BAR_ENABLE = 0x3c6ef372u;
const uint SALT_BAR_Y = 0xda3e39cbu;

vec2 atlasToGlyphLocal(vec2 atlasUV)
{
    return (atlasUV - UVRect.xy) / (UVRect.zw - UVRect.xy);
}

vec2 glyphLocalToAtlas(vec2 localUV)
{
    return mix(UVRect.xy, UVRect.zw, localUV);
}

vec2 glyphTexelSize()
{
    vec2 atlasSize = vec2(textureSize(atlas, 0));
    return abs(UVRect.zw - UVRect.xy) * atlasSize;
}

uint glyphSeed()
{
    vec2 atlasSize = vec2(textureSize(atlas, 0));

    uvec2 p = uvec2(floor(UVRect.xy * atlasSize + 0.5));
    uvec2 q = uvec2(floor(UVRect.zw * atlasSize + 0.5));

    uint seed = hashUint(p.x);
    seed = hashUint(seed ^ hashUint(p.y));
    seed = hashUint(seed ^ hashUint(q.x));
    seed = hashUint(seed ^ hashUint(q.y));

    return seed;
}

float sampleGlyphLocal(vec2 localUV)
{
    if (any(lessThan(localUV, vec2(0.0))) || any(greaterThan(localUV, vec2(1.0))))
    {
        return 0.0;
    }

    return texture(atlas, glyphLocalToAtlas(localUV)).r;
}

float lineSegmentSDF(vec2 localP, vec2 localA, vec2 localB, float halfThicknessPx)
{
    vec2 sizePx = max(glyphTexelSize(), vec2(1.0));

    vec2 p = localP * sizePx;
    vec2 a = localA * sizePx;
    vec2 b = localB * sizePx;

    vec2 pa = p - a;
    vec2 ba = b - a;

    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 1e-6), 0.0, 1.0);
    float distancePx = length(pa - ba * h);
    float signedDistancePx = halfThicknessPx - distancePx;

    return clamp(0.5 + signedDistancePx * (0.5 / sdfSpread), 0.0, 1.0);
}

vec2 mirrorGlyphAcrossAxis(vec2 localP, vec2 axis)
{
    vec2 sizePx = max(glyphTexelSize(), vec2(1.0));
    vec2 p = localP * sizePx;
    vec2 center = sizePx * 0.5;
    axis = normalize(axis);
    vec2 relative = p - center;
    vec2 mirrored = center + 2.0 * axis * dot(relative, axis) - relative;
    return mirrored / sizePx;
}

float obfuscatedGlyph(vec2 atlasUV)
{
    vec2 localUV = atlasToGlyphLocal(atlasUV);

    if (any(lessThan(localUV, vec2(0.0))) || any(greaterThan(localUV, vec2(1.0))))
    {
        return 0.0;
    }

    uint baseSeed = hashUint(glyphSeed() ^ hashUint(tick * SALT_TICK));

    const float BAND_COUNT = 5.0;
    const float PRIMARY_SHIFT = 0.34;
    const float SECONDARY_SHIFT = 0.14;
    const float VERTICAL_SHIFT = 0.12;

    uint band = min(uint(floor(localUV.y * BAND_COUNT)), uint(BAND_COUNT) - 1u);
    float bandRand = hash01(baseSeed ^ hashUint(band * SALT_BAND_A));

    vec2 p0 = localUV;
    vec2 p1 = localUV;

    p0.x += (bandRand - 0.5) * PRIMARY_SHIFT;
    float mirrorRand = hash01(baseSeed ^ SALT_MIRROR);
    if (mirrorRand > 0.5)
    {
        p1.x = 1.0 - p1.x;
    }

    float axisRand = hash01(baseSeed ^ SALT_MIRROR_AXIS);
    float angle = axisRand * 3.14159265359;
    vec2 axis = vec2(cos(angle), sin(angle));
    p0 = mirrorGlyphAcrossAxis(p0, axis);

    float secondBandRand = hash01(baseSeed ^ hashUint(band * SALT_BAND_B));
    p1.x += (secondBandRand - 0.5) * SECONDARY_SHIFT;

    float verticalRand = hash01(baseSeed ^ SALT_VERTICAL) - 0.5;
    p1.y += verticalRand * VERTICAL_SHIFT;

    float a = sampleGlyphLocal(p0);
    float b = sampleGlyphLocal(p1);

    float mode = hash01(baseSeed ^ SALT_MODE);

    float sdf;

    if (mode < 0.45)
    {
        sdf = a;
    }
    else if (mode < 0.82)
    {
        sdf = max(a, b);
    }
    else
    {
        sdf = min(a, b);
    }

    const uint CELL_COUNT_X = 3u;
    const uint CELL_COUNT_Y = 4u;

    uint cellX = min(uint(floor(localUV.x * float(CELL_COUNT_X))), CELL_COUNT_X - 1u);
    uint cellY = min(uint(floor(localUV.y * float(CELL_COUNT_Y))), CELL_COUNT_Y - 1u);

    uint cellIndex = cellX + cellY * CELL_COUNT_X;
    float cellRand = hash01(baseSeed ^ hashUint(cellIndex * SALT_CELL));

    const float THRESHOLD_VARIATION = 0.09;

    float thresholdOffset = (cellRand - 0.5) * THRESHOLD_VARIATION;

    sdf -= thresholdOffset;

    float addStroke = hash01(baseSeed ^ SALT_STROKE_ENABLE);
    if (addStroke > 0.84)
    {
        float x0 = 0.18 + hash01(baseSeed ^ SALT_STROKE_X) * 0.64;
        float tilt = (hash01(baseSeed ^ SALT_STROKE_TILT) - 0.5) * 0.40;
        vec2 start = vec2(x0 - tilt, 0.18);
        vec2 end = vec2(x0 + tilt, 0.82);
        float stroke = lineSegmentSDF(localUV, start, end, 1.25);
        sdf = max(sdf, stroke);
    }

    float addBar = hash01(baseSeed ^ SALT_BAR_ENABLE);
    if (addBar > 0.74)
    {
        float y = 0.25 + hash01(baseSeed ^ SALT_BAR_Y) * 0.50;
        float bar = lineSegmentSDF(localUV, vec2(0.22, y), vec2(0.78, y), 1.25);
        sdf = max(sdf, bar);
    }

    return clamp(sdf, 0.0, 1.0);
}

float directionalShadow(bool enableObfuscated, vec2 uv, vec2 texel, float outerEdge, float w)
{
    const vec2 SHADOW_DIR = normalize(vec2(1.0, 1.0));
    const int SHADOW_STEPS = 6;

    float shadow = 0.0;

    for (int i = 0; i <= SHADOW_STEPS; i++)
    {
        vec2 shadowUV = uv - SHADOW_DIR * texel * float(i);
        float dist = enableObfuscated ? obfuscatedGlyph(shadowUV) : texture(atlas, shadowUV).r;
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
    bool enableObfuscated = false;

    vec2 texel = 1.0 / vec2(textureSize(atlas, 0));
    float dist = enableObfuscated ? obfuscatedGlyph(UV) : texture(atlas, UV).r;
    float w = max(fwidth(dist), 0.001);

    float edge = 0.5;
    float outlineThickness = enableOutline ? 0.05 : 0.0;

    vec4 color = unpackARGB(Color);
    vec4 outlineColor = vec4(0.0, 0.0, 0.0, color.a);
    vec4 shadowColor = vec4(0.0, 0.0, 0.0, color.a * 0.75);

    float fillEdge = edge - boldness;
    float outerEdge = fillEdge - outlineThickness;

    float fill = smoothstep(fillEdge - w, fillEdge + w, dist);
    float outer = enableOutline ? smoothstep(outerEdge - w, outerEdge + w, dist) : fill;
    float stroke = enableOutline ? max(outer - fill, 0.0) : 0.0;

    float shadow = enableShadow ? directionalShadow(enableObfuscated, UV, texel, outerEdge, w) : 0.0;

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
