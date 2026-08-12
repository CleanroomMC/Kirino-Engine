#version 430 core

in vec2 UV;
flat in vec4 UVRect;
flat in uint Color;
flat in uint Page;
flat in uint Hint;

flat in uvec4 ObfParam0;
flat in uvec4 ObfParam1;
flat in uvec4 ObfParam2;
flat in uvec4 ObfParam3;
flat in uvec4 ObfParam4;
flat in uvec4 ObfParam5;
flat in uvec4 ObfParam6;
flat in uvec4 ObfParam7;

uniform sampler2DArray atlas;
uniform uint tick;
uniform uint sdfSpread;

out vec4 FragColor;

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
    vec2 atlasSize = vec2(textureSize(atlas, 0).xy);
    return abs(UVRect.zw - UVRect.xy) * atlasSize;
}

float lineSegmentSDF(vec2 localUV, vec2 localA, vec2 localB, vec2 sizePx, float halfThicknessPx)
{
    sizePx = max(sizePx, vec2(1.0));

    vec2 p = localUV * sizePx;
    vec2 a = localA * sizePx;
    vec2 b = localB * sizePx;

    vec2 pa = p - a;
    vec2 ba = b - a;

    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 1e-6), 0.0, 1.0);
    float distancePx = length(pa - ba * h);
    float signedDistancePx = halfThicknessPx - distancePx;

    return clamp(0.5 + signedDistancePx * (0.5 / sdfSpread), 0.0, 1.0);
}

float lineSegmentSDF(vec2 localUV, vec2 localA, vec2 localB, float halfThicknessPx)
{
    return lineSegmentSDF(localUV, localA, localB, glyphTexelSize(), halfThicknessPx);
}

float sampleAtlas(vec2 atlasUV)
{
    bool zeroGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) == 0u); // 10...
    bool emptyGlyph = ((Hint & (1u << 31)) == 0u) && ((Hint & (1u << 30)) != 0u); // 01...
    bool failedGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) != 0u); // 11...

    float dist = 0.0;

    if (zeroGlyph || failedGlyph)
    {
        vec2 localUV = atlasToGlyphLocal(atlasUV);

        if (any(lessThan(localUV, vec2(0.0))) || any(greaterThan(localUV, vec2(1.0))))
        {
            dist = 0.0;
        }
        else
        {
            vec2 sizePx = 1.0 / max(fwidth(localUV), vec2(1e-6));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.2, 0.2), vec2(0.8, 0.2), sizePx, 1.25));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.2, 0.8), vec2(0.8, 0.8), sizePx, 1.25));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.2, 0.2), vec2(0.2, 0.8), sizePx, 1.25));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.8, 0.2), vec2(0.8, 0.8), sizePx, 1.25));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.2, 0.2), vec2(0.8, 0.8), sizePx, 1.25));
            dist = max(dist, lineSegmentSDF(localUV, vec2(0.2, 0.8), vec2(0.8, 0.2), sizePx, 1.25));
        }
    }
    else if (!emptyGlyph)
    {
        dist = texture(atlas, vec3(atlasUV, float(Page))).r;
    }

    return dist;
}

vec4 unpackARGB(uint c)
{
    return vec4(
        ((c >> 16u) & 255u) / 255.0,
        ((c >> 8u) & 255u) / 255.0,
        (c & 255u) / 255.0,
        ((c >> 24u) & 255u) / 255.0);
}

float sampleGlyphLocal(vec2 localUV)
{
    if (any(lessThan(localUV, vec2(0.0))) || any(greaterThan(localUV, vec2(1.0))))
    {
        return 0.0;
    }

    return sampleAtlas(glyphLocalToAtlas(localUV));
}

vec2 mirrorGlyphAcrossAxis(vec2 localUV, vec2 axis)
{
    vec2 sizePx = max(glyphTexelSize(), vec2(1.0));
    vec2 p = localUV * sizePx;
    vec2 center = sizePx * 0.5;
    axis = normalize(axis);
    vec2 relative = p - center;
    vec2 mirrored = center + 2.0 * axis * dot(relative, axis) - relative;
    return mirrored / sizePx;
}

const float BAND_COUNT = 5.0;
const float PRIMARY_SHIFT = 0.34;
const float SECONDARY_SHIFT = 0.14;
const float VERTICAL_SHIFT = 0.12;
const uint CELL_COUNT_X = 3u;
const uint CELL_COUNT_Y = 4u;
const float THRESHOLD_VARIATION = 0.09;

float obfuscatedGlyphRaw(vec2 atlasUV)
{
    bool zeroGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) == 0u); // 10...
    bool emptyGlyph = ((Hint & (1u << 31)) == 0u) && ((Hint & (1u << 30)) != 0u); // 01...
    bool failedGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) != 0u); // 11...

    if (zeroGlyph || failedGlyph)
    {
        return sampleAtlas(atlasUV);
    }
    else if (emptyGlyph)
    {
        return 0.0;
    }

    vec2 localUV = atlasToGlyphLocal(atlasUV);

    if (any(lessThan(localUV, vec2(0.0))) || any(greaterThan(localUV, vec2(1.0))))
    {
        return 0.0;
    }

    uint baseSeed = ObfParam0.x;

    // 0 .. 4
    uint band = min(uint(floor(localUV.y * BAND_COUNT)), uint(BAND_COUNT) - 1u);

    float bandRand = 0.0;
    if (band == 0u)
    {
        bandRand = uintBitsToFloat(ObfParam0.y);
    }
    else if (band == 1u)
    {
        bandRand = uintBitsToFloat(ObfParam0.z);
    }
    else if (band == 2u)
    {
        bandRand = uintBitsToFloat(ObfParam0.w);
    }
    else if (band == 3u)
    {
        bandRand = uintBitsToFloat(ObfParam1.x);
    }
    else if (band == 4u)
    {
        bandRand = uintBitsToFloat(ObfParam1.y);
    }

    vec2 p0 = localUV;
    vec2 p1 = localUV;

    p0.x += (bandRand - 0.5) * PRIMARY_SHIFT;
    float mirrorRand = uintBitsToFloat(ObfParam2.w);
    if (mirrorRand > 0.5)
    {
        p1.x = 1.0 - p1.x;
    }

    float axisRand = uintBitsToFloat(ObfParam3.x);
    float angle = axisRand * 3.14159265359;
    vec2 axis = vec2(cos(angle), sin(angle));
    p0 = mirrorGlyphAcrossAxis(p0, axis);

    float secondBandRand = 0.0;
    if (band == 0u)
    {
        secondBandRand = uintBitsToFloat(ObfParam1.z);
    }
    else if (band == 1u)
    {
        secondBandRand = uintBitsToFloat(ObfParam1.w);
    }
    else if (band == 2u)
    {
        secondBandRand = uintBitsToFloat(ObfParam2.x);
    }
    else if (band == 3u)
    {
        secondBandRand = uintBitsToFloat(ObfParam2.y);
    }
    else if (band == 4u)
    {
        secondBandRand = uintBitsToFloat(ObfParam2.z);
    }

    p1.x += (secondBandRand - 0.5) * SECONDARY_SHIFT;

    float verticalRand = uintBitsToFloat(ObfParam3.y);
    p1.y += verticalRand * VERTICAL_SHIFT;

    float a = sampleGlyphLocal(p0);
    float b = sampleGlyphLocal(p1);

    float mode = uintBitsToFloat(ObfParam3.z);

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

    uint cellX = min(uint(floor(localUV.x * float(CELL_COUNT_X))), CELL_COUNT_X - 1u);
    uint cellY = min(uint(floor(localUV.y * float(CELL_COUNT_Y))), CELL_COUNT_Y - 1u);

    // 0 .. 11
    uint cellIndex = cellX + cellY * CELL_COUNT_X;

    float cellRand = 0.0;
    if (cellIndex == 0u)
    {
        cellRand = uintBitsToFloat(ObfParam3.w);
    }
    else if (cellIndex == 1u)
    {
        cellRand = uintBitsToFloat(ObfParam4.x);
    }
    else if (cellIndex == 2u)
    {
        cellRand = uintBitsToFloat(ObfParam4.y);
    }
    else if (cellIndex == 3u)
    {
        cellRand = uintBitsToFloat(ObfParam4.z);
    }
    else if (cellIndex == 4u)
    {
        cellRand = uintBitsToFloat(ObfParam4.w);
    }
    else if (cellIndex == 5u)
    {
        cellRand = uintBitsToFloat(ObfParam5.x);
    }
    else if (cellIndex == 6u)
    {
        cellRand = uintBitsToFloat(ObfParam5.y);
    }
    else if (cellIndex == 7u)
    {
        cellRand = uintBitsToFloat(ObfParam5.z);
    }
    else if (cellIndex == 8u)
    {
        cellRand = uintBitsToFloat(ObfParam5.w);
    }
    else if (cellIndex == 9u)
    {
        cellRand = uintBitsToFloat(ObfParam6.x);
    }
    else if (cellIndex == 10u)
    {
        cellRand = uintBitsToFloat(ObfParam6.y);
    }
    else if (cellIndex == 11u)
    {
        cellRand = uintBitsToFloat(ObfParam6.z);
    }

    float thresholdOffset = (cellRand - 0.5) * THRESHOLD_VARIATION;

    sdf -= thresholdOffset;

    float addStroke = uintBitsToFloat(ObfParam6.w);
    if (addStroke > 0.84)
    {
        float x0 = uintBitsToFloat(ObfParam7.x);
        float tilt = uintBitsToFloat(ObfParam7.y);
        vec2 start = vec2(x0 - tilt, 0.18);
        vec2 end = vec2(x0 + tilt, 0.82);
        float stroke = lineSegmentSDF(localUV, start, end, 1.25);
        sdf = max(sdf, stroke);
    }

    float addBar = uintBitsToFloat(ObfParam7.z);
    if (addBar > 0.74)
    {
        float y = uintBitsToFloat(ObfParam7.w);
        float bar = lineSegmentSDF(localUV, vec2(0.22, y), vec2(0.78, y), 1.25);
        sdf = max(sdf, bar);
    }

    return clamp(sdf, 0.0, 1.0);
}

void sort2(inout float a, inout float b)
{
    if (a > b)
    {
        float t = a;
        a = b;
        b = t;
    }
}

float median5(float a, float b, float c, float d, float e)
{
    sort2(a, b);
    sort2(d, e);
    sort2(c, e);
    sort2(c, d);
    sort2(a, d);
    sort2(a, c);
    sort2(b, e);
    sort2(b, d);
    sort2(b, c);

    return c;
}

float obfuscatedGlyph(vec2 atlasUV)
{
    bool zeroGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) == 0u); // 10...
    bool emptyGlyph = ((Hint & (1u << 31)) == 0u) && ((Hint & (1u << 30)) != 0u); // 01...
    bool failedGlyph = ((Hint & (1u << 31)) != 0u) && ((Hint & (1u << 30)) != 0u); // 11...

    if (zeroGlyph || emptyGlyph || failedGlyph)
    {
        return obfuscatedGlyphRaw(atlasUV);
    }

    vec2 texel = 1.0 / vec2(textureSize(atlas, 0).xy);

    vec2 dx = vec2(texel.x, 0.0);
    vec2 dy = vec2(0.0, texel.y);

    float c = obfuscatedGlyphRaw(atlasUV);
    float l = obfuscatedGlyphRaw(atlasUV - dx);
    float r = obfuscatedGlyphRaw(atlasUV + dx);
    float u = obfuscatedGlyphRaw(atlasUV - dy);
    float d = obfuscatedGlyphRaw(atlasUV + dy);

    float m = median5(c, l, r, u, d);
    float cleaned = min(c, m);

    return mix(cleaned, max(cleaned, m), 0.2);
}

float obfuscatedMask(vec2 atlasUV, float edge, float w)
{
    float dist = obfuscatedGlyphRaw(atlasUV);
    return smoothstep(edge - w, edge + w, dist);
}

float obfuscatedShadowMask(vec2 atlasUV, float edge, float w)
{
    vec2 texel = 1.0 / vec2(textureSize(atlas, 0).xy);

    vec2 dx = vec2(texel.x, 0.0);
    vec2 dy = vec2(0.0, texel.y);

    float c = obfuscatedMask(atlasUV, edge, w);
    float l = obfuscatedMask(atlasUV - dx, edge, w);
    float r = obfuscatedMask(atlasUV + dx, edge, w);
    float u = obfuscatedMask(atlasUV - dy, edge, w);
    float d = obfuscatedMask(atlasUV + dy, edge, w);

    float support = (c + l + r + u + d) * 0.2;

    return c * smoothstep(0.35, 0.65, support);
}

float directionalShadow(bool enableObfuscated, vec2 atlasUV, vec2 texel, float fillEdge, float w)
{
    const vec2 SHADOW_DIR = normalize(vec2(1.0, 1.0));
    const int SHADOW_STEPS = 6;

    float shadow = 0.0;

    for (int i = 1; i <= SHADOW_STEPS; i++)
    {
        vec2 shadowUV = atlasUV - SHADOW_DIR * texel * float(i);

        float mask;
        if (enableObfuscated)
        {
            mask = obfuscatedShadowMask(shadowUV, fillEdge, w);
        }
        else
        {
            float dist = sampleAtlas(shadowUV);
            mask = smoothstep(fillEdge - w, fillEdge + w, dist);
        }

        float t = float(i - 1) / float(max(SHADOW_STEPS - 1, 1));
        float weight = mix(1.0, 0.55, pow(t, 1.5));
        shadow = max(shadow, mask * weight);
    }

    return shadow;
}

void main()
{
    float boldness = 0.0;
    bool enableOutline = true;
    bool enableShadow = true;
    bool enableObfuscated = true;

    vec2 texel = 1.0 / vec2(textureSize(atlas, 0).xy);
    float dist = enableObfuscated ? obfuscatedGlyph(UV) : sampleAtlas(UV);
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

    float shadow = enableShadow ? directionalShadow(enableObfuscated, UV, texel, fillEdge, w) : 0.0;

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
