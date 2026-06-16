#version 430 core

layout(location = 0) in vec4 uv;
layout(location = 1) in vec4 rect;
layout(location = 2) in float size;
layout(location = 3) in int color;
layout(location = 4) in uint page;
layout(location = 5) in int hint;

uniform vec2 scaledRes;

out vec2 UV;

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
    vec2 corner = corners[gl_VertexID];

    vec2 pos = rect.xy + corner * rect.zw;

    UV = mix(uv.xy, uv.zw, corner);

    Color = uint(color);
    Page = page;
    Hint = uint(hint);

    gl_Position = vec4(res2Ndc(scaledRes, pos), 0.0, 1.0);
}
