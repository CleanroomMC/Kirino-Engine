#version 460 core

#define DRAW_RECT 1
#define DRAW_LINES 2
#define DRAW_BEZIER 3

#define FLAG_RADIUS 1
#define FLAG_BORDER 2
#define FLAG_SHADOW 4

in flat int DrawType;
in flat int Flags;
in flat vec4 Color;
in flat vec4 BorderColor;
in flat vec4 ShadowColor;
in flat vec4 Rect;
in vec2 LocalPos;
in flat float BorderWidth;
in flat float ShadowBlur;
in flat vec2 ShadowOffset;
in flat float Radius;
in flat float Pad;
in float RoundedRectDist;

out vec4 FragColor;

float directionalShadow(vec2 shadowOffset, vec4 rect, vec2 localPos)
{
    vec2 shadowDir = normalize(shadowOffset);
    vec2 center = rect.zw * 0.5;
    vec2 fromCenter = localPos - center;

    float len = length(fromCenter);

    vec2 radialDir;
    if (len > 0.001)
    {
        radialDir = fromCenter / len;
    }
    else
    {
        radialDir = shadowDir;
    }

    float directional = dot(radialDir, shadowDir);
    float t = clamp(directional * 0.5 + 0.5, 0.0, 1.0);
    t = t * t * t;
    float directionalFactor = mix(0.0, 1.0, t);
    float shadowT = clamp(RoundedRectDist - 2.0, 0.0, 1.0);
    float shadowAlpha = (1.0 - smoothstep(0.0, 1.0, shadowT)) * directionalFactor;

    return shadowAlpha;
}

void main()
{
    if (DrawType == DRAW_RECT)
    {
        bool hasRadius = (Flags & FLAG_RADIUS) != 0;
        bool hasBorder = (Flags & FLAG_BORDER) != 0;
        bool hasShadow = (Flags & FLAG_SHADOW) != 0;

        FragColor = Color;

        if (hasRadius)
        {
            float aaWidth = 0.08;
            if (!hasBorder && !hasShadow)
            {
                if (RoundedRectDist < 1.0 - aaWidth)
                {
                    FragColor = Color;
                }
                else
                {
                    vec4 outsideColor = Color;
                    outsideColor.a = 0.0;
                    float t = smoothstep(1.0 - aaWidth, 1.0, RoundedRectDist);
                    FragColor = mix(Color, outsideColor, t);
                }
            }
            else if (hasBorder && !hasShadow)
            {
                if (RoundedRectDist < 1.0 - aaWidth)
                {
                    FragColor = Color;
                }
                else if (RoundedRectDist < 1.0 + aaWidth)
                {
                    float t = smoothstep(1.0 - aaWidth, 1.0 + aaWidth, RoundedRectDist);
                    FragColor = mix(Color, BorderColor, t);
                }
                else if (RoundedRectDist < 2.0 - aaWidth)
                {
                    FragColor = BorderColor;
                }
                else
                {
                    vec4 outsideColor = BorderColor;
                    outsideColor.a = 0.0;
                    float t = smoothstep(2.0 - aaWidth, 2.0, RoundedRectDist);
                    FragColor = mix(BorderColor, outsideColor, t);
                }
            }
            else if (!hasBorder && hasShadow)
            {

            }
            else if (hasBorder && hasShadow)
            {
                if (RoundedRectDist < 1.0 - aaWidth)
                {
                    FragColor = Color;
                }
                else if (RoundedRectDist < 1.0 + aaWidth)
                {
                    float t = smoothstep(1.0 - aaWidth, 1.0 + aaWidth, RoundedRectDist);
                    FragColor = mix(Color, BorderColor, t);
                }
                else if (RoundedRectDist < 2.0 - aaWidth)
                {
                    FragColor = BorderColor;
                }
                else if (RoundedRectDist < 2.0)
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos);
                    if (shadow.a <= 0.01)
                    {
                        shadow = BorderColor;
                        shadow.a = 0.0;
                    }
                    float t = smoothstep(2.0 - aaWidth, 2.0, RoundedRectDist);
                    FragColor = mix(BorderColor, shadow, t);
                }
                else
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos);
                    FragColor = shadow;
                }
            }
        }
        else
        {
            if (!hasBorder && !hasShadow)
            {

            }
            else if (hasBorder && !hasShadow)
            {

            }
            else if (!hasBorder && hasShadow)
            {

            }
            else if (hasBorder && hasShadow)
            {

            }
        }
    }
    else if (DrawType == DRAW_LINES)
    {

    }
    else if (DrawType == DRAW_BEZIER)
    {

    }
    else
    {
        discard;
    }
}
