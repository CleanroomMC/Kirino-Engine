#version 460 core

#define DRAW_RECT 1
#define DRAW_LINES 2
#define DRAW_BEZIER 3

#define FLAG_RADIUS 1
#define FLAG_BORDER 2
#define FLAG_SHADOW 4

struct DrawInfo
{
    int drawType;
    int flags;
    int payloadIndex;
    float depth;
};

struct RectPayload
{
    vec4 rect;
    vec2 shadow;
    int meshOffset;
    int vertexCount;
    uint color;
    float borderWidth;
    uint borderColor;
    float shadowBlur;
    uint shadowColor;
    float radius;
};

layout(std430, binding = 0) readonly buffer DrawInfos
{
    DrawInfo drawInfos[];
};

layout(std430, binding = 1) readonly buffer RectPayloads
{
    RectPayload rectPayloads[];
};

layout(std430, binding = 2) readonly buffer ArenaVerts
{
    vec2 arenaVerts[];
};

uniform vec2 scaledRes;
uniform bool useDepth;

out flat int DrawType;
out flat int Flags;
out flat vec4 Color;
out flat vec4 BorderColor;
out flat vec4 ShadowColor;
out flat vec4 Rect;
out vec2 LocalPos;
out flat float BorderWidth;
out flat float ShadowBlur;
out flat vec2 ShadowOffset;
out flat float Radius;
out flat float Pad;
out float RoundedRectDist;

vec4 unpackARGB(uint c)
{
    return vec4(
        ((c >> 16u) & 255u) / 255.0,
        ((c >> 8u) & 255u) / 255.0,
        (c & 255u) / 255.0,
        ((c >> 24u) & 255u) / 255.0);
}

vec2 res2Ndc(vec2 res, vec2 p)
{
    return vec2(
        p.x / res.x * 2.0 - 1.0,
        1.0 - p.y / res.y * 2.0);
}

vec2 rectVertex(vec4 rect, uint vertId, float pad)
{
    vec2 p0 = rect.xy - vec2(pad);
    vec2 p1 = rect.xy + rect.zw + vec2(pad);

    uint i = vertId % 6u;

    if (i == 0u) return vec2(p0.x, p0.y);
    if (i == 1u) return vec2(p1.x, p0.y);
    if (i == 2u) return vec2(p1.x, p1.y);
    if (i == 3u) return vec2(p1.x, p1.y);
    if (i == 4u) return vec2(p0.x, p1.y);

    return vec2(p0.x, p0.y);
}

vec2 roundedRectVertex1(RectPayload payload, uint vertId)
{
    int centerIndex = payload.meshOffset;
    int ringStart = payload.meshOffset + 1;
    int ringCount = max(payload.vertexCount - 1, 1);

    uint tri = vertId / 3u;
    uint lane = vertId % 3u;

    if (lane == 0u)
    {
        RoundedRectDist = 0.0;
        return arenaVerts[centerIndex];
    }

    int ringIndex = int(tri);
    if (lane == 2u)
    {
        ringIndex = (ringIndex + 1) % ringCount;
    }

    RoundedRectDist = 1.0;
    return arenaVerts[ringStart + ringIndex];
}

vec2 roundedRectVertex2(RectPayload payload, uint vertId, float pad)
{
    int centerIndex = payload.meshOffset;
    int ringStart = payload.meshOffset + 1;
    int ringCount = max(payload.vertexCount - 1, 1);

    uint innerFanVertCount = uint(ringCount) * 3u;
    uint outerRingVertCount = uint(ringCount) * 6u;

    if (vertId < innerFanVertCount)
    {
        uint tri = vertId / 3u;
        uint lane = vertId % 3u;

        if (lane == 0u)
        {
            RoundedRectDist = 0.0;
            return arenaVerts[centerIndex];
        }

        int ringIndex = int(tri);
        if (lane == 2u)
        {
            ringIndex = (ringIndex + 1) % ringCount;
        }

        RoundedRectDist = 1.0;
        return arenaVerts[ringStart + ringIndex];
    }

    uint local = vertId - innerFanVertCount;

    uint seg = local / 6u;
    uint lane = local % 6u;

    int i0 = int(seg);
    int i1 = (i0 + 1) % ringCount;

    bool useOuter;
    int ringIndex;

    if (lane == 0u)
    {
        ringIndex = i0;
        useOuter = false;
    }
    else if (lane == 1u)
    {
        ringIndex = i0;
        useOuter = true;
    }
    else if (lane == 2u)
    {
        ringIndex = i1;
        useOuter = true;
    }
    else if (lane == 3u)
    {
        ringIndex = i1;
        useOuter = true;
    }
    else if (lane == 4u)
    {
        ringIndex = i1;
        useOuter = false;
    }
    else
    {
        ringIndex = i0;
        useOuter = false;
    }

    vec2 pos = arenaVerts[ringStart + ringIndex];

    if (!useOuter || pad <= 0.001)
    {
        RoundedRectDist = 1.0;
        return pos;
    }

    vec2 innerMin = payload.rect.xy + vec2(payload.radius);
    vec2 innerMax = payload.rect.xy + payload.rect.zw - vec2(payload.radius);

    vec2 cornerCenter = vec2(
        (pos.x < (payload.rect.x + payload.rect.z * 0.5)) ? innerMin.x : innerMax.x,
        (pos.y < (payload.rect.y + payload.rect.w * 0.5)) ? innerMin.y : innerMax.y);

    vec2 outward = pos - cornerCenter;

    float len = length(outward);
    if (len > 0.001)
    {
        outward /= len;
    }
    else
    {
        int cornerVertCount = max(ringCount / 4, 1);
        int corner = clamp(ringIndex / cornerVertCount, 0, 3);
        outward = (corner == 0) ? vec2(-1.0, -1.0) :
                  (corner == 1) ? vec2( 1.0, -1.0) :
                  (corner == 2) ? vec2( 1.0,  1.0) : vec2(-1.0, 1.0);
        outward = normalize(outward);
    }

    RoundedRectDist = 2.0;
    return pos + outward * pad;
}

// pad2 >= pad1 must be satisfied
vec2 roundedRectVertex3(RectPayload payload, uint vertId, float pad1, float pad2)
{
    int centerIndex = payload.meshOffset;
    int ringStart = payload.meshOffset + 1;
    int ringCount = max(payload.vertexCount - 1, 1);

    uint innerFanVertCount = uint(ringCount) * 3u;
    uint ring1VertCount = uint(ringCount) * 6u;
    uint ring2VertCount = uint(ringCount) * 6u;

    if (vertId < innerFanVertCount)
    {
        uint tri = vertId / 3u;
        uint lane = vertId % 3u;

        if (lane == 0u)
        {
            RoundedRectDist = 0.0;
            return arenaVerts[centerIndex];
        }

        int ringIndex = int(tri);

        if (lane == 2u)
        {
            ringIndex = (ringIndex + 1) % ringCount;
        }

        RoundedRectDist = 1.0;
        return arenaVerts[ringStart + ringIndex];
    }

    if (vertId < innerFanVertCount + ring1VertCount)
    {
        uint local = vertId - innerFanVertCount;

        uint seg = local / 6u;
        uint lane = local % 6u;

        int i0 = int(seg);
        int i1 = (i0 + 1) % ringCount;

        bool useOuter;
        int ringIndex;

        if (lane == 0u)
        {
            ringIndex = i0;
            useOuter = false;
        }
        else if (lane == 1u)
        {
            ringIndex = i0;
            useOuter = true;
        }
        else if (lane == 2u)
        {
            ringIndex = i1;
            useOuter = true;
        }
        else if (lane == 3u)
        {
            ringIndex = i1;
            useOuter = true;
        }
        else if (lane == 4u)
        {
            ringIndex = i1;
            useOuter = false;
        }
        else
        {
            ringIndex = i0;
            useOuter = false;
        }

        vec2 pos = arenaVerts[ringStart + ringIndex];

        if (!useOuter || pad1 <= 0.001)
        {
            RoundedRectDist = 1.0;
            return pos;
        }

        vec2 innerMin = payload.rect.xy + vec2(payload.radius);
        vec2 innerMax = payload.rect.xy + payload.rect.zw - vec2(payload.radius);

        vec2 cornerCenter = vec2(
            (pos.x < (payload.rect.x + payload.rect.z * 0.5)) ? innerMin.x : innerMax.x,
            (pos.y < (payload.rect.y + payload.rect.w * 0.5)) ? innerMin.y : innerMax.y);

        vec2 outward = pos - cornerCenter;

        float len = length(outward);
        if (len > 0.001)
        {
            outward /= len;
        }
        else
        {
            int cornerVertCount = max(ringCount / 4, 1);
            int corner = clamp(ringIndex / cornerVertCount, 0, 3);
            outward = (corner == 0) ? vec2(-1.0, -1.0) :
                      (corner == 1) ? vec2( 1.0, -1.0) :
                      (corner == 2) ? vec2( 1.0,  1.0) : vec2(-1.0, 1.0);
            outward = normalize(outward);
        }

        RoundedRectDist = 2.0;
        return pos + outward * pad1;
    }

    uint local = vertId - innerFanVertCount - ring1VertCount;

    uint seg = local / 6u;
    uint lane = local % 6u;

    int i0 = int(seg);
    int i1 = (i0 + 1) % ringCount;

    bool useOuter;
    int ringIndex;

    if (lane == 0u)
    {
        ringIndex = i0;
        useOuter = false;
    }
    else if (lane == 1u)
    {
        ringIndex = i0;
        useOuter = true;
    }
    else if (lane == 2u)
    {
        ringIndex = i1;
        useOuter = true;
    }
    else if (lane == 3u)
    {
        ringIndex = i1;
        useOuter = true;
    }
    else if (lane == 4u)
    {
        ringIndex = i1;
        useOuter = false;
    }
    else
    {
        ringIndex = i0;
        useOuter = false;
    }

    vec2 pos = arenaVerts[ringStart + ringIndex];

    vec2 innerMin = payload.rect.xy + vec2(payload.radius);
    vec2 innerMax = payload.rect.xy + payload.rect.zw - vec2(payload.radius);

    vec2 cornerCenter = vec2(
        (pos.x < (payload.rect.x + payload.rect.z * 0.5)) ? innerMin.x : innerMax.x,
        (pos.y < (payload.rect.y + payload.rect.w * 0.5)) ? innerMin.y : innerMax.y);

    vec2 outward = pos - cornerCenter;

    float len = length(outward);
    if (len > 0.001)
    {
        outward /= len;
    }
    else
    {
        int cornerVertCount = max(ringCount / 4, 1);
        int corner = clamp(ringIndex / cornerVertCount, 0, 3);
        outward = (corner == 0) ? vec2(-1.0, -1.0) :
                  (corner == 1) ? vec2( 1.0, -1.0) :
                  (corner == 2) ? vec2( 1.0,  1.0) : vec2(-1.0, 1.0);
        outward = normalize(outward);
    }

    if (!useOuter || abs(pad2 - pad1) <= 0.001)
    {
        RoundedRectDist = 2.0;
        return pos + outward * pad1;
    }

    RoundedRectDist = 3.0;
    return pos + outward * pad2;
}

void initOut(int drawType, int flags)
{
    DrawType = drawType;
    Flags = flags;
    Color = vec4(0.0);
    BorderColor = vec4(0.0);
    ShadowColor = vec4(0.0);
    Rect = vec4(0.0);
    LocalPos = vec2(0.0);
    BorderWidth = 0.0;
    ShadowBlur = 0.0;
    ShadowOffset = vec2(0.0);
    Radius = 0.0;
    Pad = 0.0;
    RoundedRectDist = 0.0;
}

void main()
{
    DrawInfo info = drawInfos[gl_BaseInstance];
    initOut(info.drawType, info.flags);

    vec2 pos;

    if (info.drawType == DRAW_RECT)
    {
        RectPayload payload = rectPayloads[info.payloadIndex];

        bool hasRadius = (info.flags & FLAG_RADIUS) != 0;
        bool hasBorder = (info.flags & FLAG_BORDER) != 0;
        bool hasShadow = (info.flags & FLAG_SHADOW) != 0;

        if (hasBorder)
        {
            BorderColor = unpackARGB(payload.borderColor);
            BorderWidth = payload.borderWidth;
        }

        if (hasShadow)
        {
            ShadowColor = unpackARGB(payload.shadowColor);
            ShadowBlur = payload.shadowBlur;
            ShadowOffset = payload.shadow;
        }

        if (hasRadius)
        {
            if (!hasBorder && !hasShadow)
            {
                pos = roundedRectVertex1(payload, uint(gl_VertexID));
            }
            else if (hasBorder && !hasShadow)
            {
                float pad = abs(payload.borderWidth);
                pos = roundedRectVertex2(payload, uint(gl_VertexID), pad);
            }
            else if (!hasBorder && hasShadow)
            {
                float pad = max(abs(payload.shadow.x), abs(payload.shadow.y));
                pos = roundedRectVertex2(payload, uint(gl_VertexID), pad);
            }
            else if (hasBorder && hasShadow)
            {
                float pad1 = abs(payload.borderWidth);
                float pad2 = max(abs(payload.shadow.x), abs(payload.shadow.y)) + pad1;
                pad2 = pad2 <= pad1 ? pad1 + 0.1 : pad2;
                pos = roundedRectVertex3(payload, uint(gl_VertexID), pad1, pad2);
            }
        }
        else
        {
            float pad = 0.0;
            if (hasBorder)
            {
                pad += abs(payload.borderWidth);
            }
            if (hasShadow)
            {
                pad += max(abs(payload.shadow.x), abs(payload.shadow.y));
            }
            pos = rectVertex(payload.rect, uint(gl_VertexID), pad);
        }

        Rect = payload.rect;
        LocalPos = pos - payload.rect.xy;
        Radius = payload.radius;
        Color = unpackARGB(payload.color);
    }
    else if (info.drawType == DRAW_LINES)
    {

    }
    else if (info.drawType == DRAW_BEZIER)
    {

    }

    float z = info.depth * 2.0 - 1.0;
    gl_Position = vec4(res2Ndc(scaledRes, pos), useDepth ? z : 0.0, 1.0);
}
