#version 430 core

layout(location = 0) in vec4 uv;
layout(location = 1) in vec4 rect;
layout(location = 2) in float size;
layout(location = 3) in int color;
layout(location = 4) in uint page;
layout(location = 5) in int hint;

uniform vec2 scaledRes;
uniform sampler2DArray atlas;
uniform uint tick;

out vec2 UV;
flat out vec4 UVRect;
flat out uint Color;
flat out uint Page;
flat out uint Hint;

flat out uvec4 ObfParam0; // baseSeed bandRandA0 bandRandA1 bandRandA2
flat out uvec4 ObfParam1; // bandRandA3 bandRandA4 bandRandB0 bandRandB1
flat out uvec4 ObfParam2; // bandRandB2 bandRandB3 bandRandB4 mirrorRand
flat out uvec4 ObfParam3; // axisRand verticalRand mode cellRand0
flat out uvec4 ObfParam4; // cellRand1 cellRand2 cellRand3 cellRand4
flat out uvec4 ObfParam5; // cellRand5 cellRand6 cellRand7 cellRand8
flat out uvec4 ObfParam6; // cellRand9 cellRand10 cellRand11 addStroke
flat out uvec4 ObfParam7; // strokeX0 strokeTilt addBar barY

const vec2 corners[4] = vec2[](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 0.0),
    vec2(1.0, 1.0));

vec2 res2Ndc(vec2 res, vec2 p)
{
    return vec2(
        p.x / res.x * 2.0 - 1.0,
        1.0 - p.y / res.y * 2.0);
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

uint glyphSeed()
{
    vec2 atlasSize = vec2(textureSize(atlas, 0).xy);

    uvec2 p = uvec2(floor(UVRect.xy * atlasSize + 0.5));
    uvec2 q = uvec2(floor(UVRect.zw * atlasSize + 0.5));

    uint seed = hashUint(Page);
    seed = hashUint(seed ^ hashUint(p.x));
    seed = hashUint(seed ^ hashUint(p.y));
    seed = hashUint(seed ^ hashUint(q.x));
    seed = hashUint(seed ^ hashUint(q.y));

    return seed;
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

void initObfParams(bool enableObfuscated)
{
    ObfParam0 = uvec4(0u);
    ObfParam1 = uvec4(0u);
    ObfParam2 = uvec4(0u);
    ObfParam3 = uvec4(0u);
    ObfParam4 = uvec4(0u);
    ObfParam5 = uvec4(0u);
    ObfParam6 = uvec4(0u);
    ObfParam7 = uvec4(0u);

    if (!enableObfuscated) return;

    uint baseSeed = hashUint(glyphSeed() ^ hashUint(tick * SALT_TICK));

    float bandRandA0 = hash01(baseSeed ^ hashUint(0u * SALT_BAND_A));
    float bandRandA1 = hash01(baseSeed ^ hashUint(1u * SALT_BAND_A));
    float bandRandA2 = hash01(baseSeed ^ hashUint(2u * SALT_BAND_A));
    float bandRandA3 = hash01(baseSeed ^ hashUint(3u * SALT_BAND_A));
    float bandRandA4 = hash01(baseSeed ^ hashUint(4u * SALT_BAND_A));

    float bandRandB0 = hash01(baseSeed ^ hashUint(0u * SALT_BAND_B));
    float bandRandB1 = hash01(baseSeed ^ hashUint(1u * SALT_BAND_B));
    float bandRandB2 = hash01(baseSeed ^ hashUint(2u * SALT_BAND_B));
    float bandRandB3 = hash01(baseSeed ^ hashUint(3u * SALT_BAND_B));
    float bandRandB4 = hash01(baseSeed ^ hashUint(4u * SALT_BAND_B));

    float mirrorRand = hash01(baseSeed ^ SALT_MIRROR);

    float axisRand = hash01(baseSeed ^ SALT_MIRROR_AXIS);

    float verticalRand = hash01(baseSeed ^ SALT_VERTICAL) - 0.5;

    float mode = hash01(baseSeed ^ SALT_MODE);

    float cellRand0 = hash01(baseSeed ^ hashUint(0u * SALT_CELL));
    float cellRand1 = hash01(baseSeed ^ hashUint(1u * SALT_CELL));
    float cellRand2 = hash01(baseSeed ^ hashUint(2u * SALT_CELL));
    float cellRand3 = hash01(baseSeed ^ hashUint(3u * SALT_CELL));
    float cellRand4 = hash01(baseSeed ^ hashUint(4u * SALT_CELL));
    float cellRand5 = hash01(baseSeed ^ hashUint(5u * SALT_CELL));
    float cellRand6 = hash01(baseSeed ^ hashUint(6u * SALT_CELL));
    float cellRand7 = hash01(baseSeed ^ hashUint(7u * SALT_CELL));
    float cellRand8 = hash01(baseSeed ^ hashUint(8u * SALT_CELL));
    float cellRand9 = hash01(baseSeed ^ hashUint(9u * SALT_CELL));
    float cellRand10 = hash01(baseSeed ^ hashUint(10u * SALT_CELL));
    float cellRand11 = hash01(baseSeed ^ hashUint(11u * SALT_CELL));

    float addStroke = hash01(baseSeed ^ SALT_STROKE_ENABLE);
    float strokeX0 = 0.18 + hash01(baseSeed ^ SALT_STROKE_X) * 0.64;
    float strokeTilt = (hash01(baseSeed ^ SALT_STROKE_TILT) - 0.5) * 0.40;

    float addBar = hash01(baseSeed ^ SALT_BAR_ENABLE);
    float barY = 0.25 + hash01(baseSeed ^ SALT_BAR_Y) * 0.50;

    ObfParam0.x = baseSeed;
    ObfParam0.y = floatBitsToUint(bandRandA0);
    ObfParam0.z = floatBitsToUint(bandRandA1);
    ObfParam0.w = floatBitsToUint(bandRandA2);

    ObfParam1.x = floatBitsToUint(bandRandA3);
    ObfParam1.y = floatBitsToUint(bandRandA4);
    ObfParam1.z = floatBitsToUint(bandRandB0);
    ObfParam1.w = floatBitsToUint(bandRandB1);

    ObfParam2.x = floatBitsToUint(bandRandB2);
    ObfParam2.y = floatBitsToUint(bandRandB3);
    ObfParam2.z = floatBitsToUint(bandRandB4);
    ObfParam2.w = floatBitsToUint(mirrorRand);

    ObfParam3.x = floatBitsToUint(axisRand);
    ObfParam3.y = floatBitsToUint(verticalRand);
    ObfParam3.z = floatBitsToUint(mode);
    ObfParam3.w = floatBitsToUint(cellRand0);

    ObfParam4.x = floatBitsToUint(cellRand1);
    ObfParam4.y = floatBitsToUint(cellRand2);
    ObfParam4.z = floatBitsToUint(cellRand3);
    ObfParam4.w = floatBitsToUint(cellRand4);

    ObfParam5.x = floatBitsToUint(cellRand5);
    ObfParam5.y = floatBitsToUint(cellRand6);
    ObfParam5.z = floatBitsToUint(cellRand7);
    ObfParam5.w = floatBitsToUint(cellRand8);

    ObfParam6.x = floatBitsToUint(cellRand9);
    ObfParam6.y = floatBitsToUint(cellRand10);
    ObfParam6.z = floatBitsToUint(cellRand11);
    ObfParam6.w = floatBitsToUint(addStroke);

    ObfParam7.x = floatBitsToUint(strokeX0);
    ObfParam7.y = floatBitsToUint(strokeTilt);
    ObfParam7.z = floatBitsToUint(addBar);
    ObfParam7.w = floatBitsToUint(barY);
}

void main()
{
    Color = uint(color);
    Page = page;
    Hint = uint(hint);

    bool enableObfuscated = true;

    vec2 corner = corners[gl_VertexID];
    vec2 pos = rect.xy + corner * rect.zw;

    // italic
    float italicScope = 0.22;
    pos.x += (1.0 - corner.y) * rect.w * italicScope;

    bool zeroGlyph = (Hint & (1u << 31)) != 0u;
    bool emptyGlyph = (Hint & (1u << 30)) != 0u;
    bool failedGlyph = (Hint & (1u << 29)) != 0u;

    if (zeroGlyph || emptyGlyph || failedGlyph)
    {
        UVRect = vec4(0.0, 0.0, 1.0, 1.0);
        UV = mix(UVRect.xy, UVRect.zw, corner);
    }
    else
    {
        UV = mix(uv.xy, uv.zw, corner);
        UVRect = uv;
    }

    initObfParams(enableObfuscated);

    gl_Position = vec4(res2Ndc(scaledRes, pos), 0.0, 1.0);
}
