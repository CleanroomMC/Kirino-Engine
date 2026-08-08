#version 430 core

layout(location = 0) in vec4 uv;
layout(location = 1) in vec4 rect;
layout(location = 2) in float size;
layout(location = 3) in int color;
layout(location = 4) in uint page;
layout(location = 5) in int hint;

uniform vec2 scaledRes;

out vec2 UV;
flat out vec4 UVRect;
flat out uint Color;
flat out uint Page;
flat out uint Hint;

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

void main()
{
    Color = uint(color);
    Page = page;
    Hint = uint(hint);

    vec2 corner = corners[gl_VertexID];
    vec2 pos = rect.xy + corner * rect.zw;

    // italic
    float italicScope = 0.22;
    pos.x += (1.0 - corner.y) * rect.w * italicScope;

    bool zeroGlyph = (Hint & (1u << 31)) != 0u;
    bool failedGlyph = (Hint & (1u << 29)) != 0u;

    if (zeroGlyph || failedGlyph)
    {
        UVRect = vec4(0.0, 0.0, 1.0, 1.0);
        UV = mix(UVRect.xy, UVRect.zw, corner);
    }
    else
    {
        UV = mix(uv.xy, uv.zw, corner);
        UVRect = uv;
    }

    gl_Position = vec4(res2Ndc(scaledRes, pos), 0.0, 1.0);
}
