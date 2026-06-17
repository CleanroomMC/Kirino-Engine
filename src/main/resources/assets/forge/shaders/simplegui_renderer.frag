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

float directionalShadow(vec2 shadowOffset, vec4 rect, vec2 localPos, float roundedRectDist, float shadowBlur)
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

    float blur = 1.0 - clamp(shadowBlur, 0.0, 1.0);

    float directional = dot(radialDir, shadowDir);
    float t = clamp(directional * 0.5 + 0.5, 0.0, 1.0);
    t = pow(t, mix(1.0, 5.0, blur));
    float directionalFactor = mix(0.0, 1.0, t);
    float shadowT = clamp(roundedRectDist - 2.0, 0.0, 1.0);
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

        if (hasRadius)
        {
            float aaWidth = fwidth(RoundedRectDist);
            aaWidth = max(aaWidth, 0.001);

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
                if (RoundedRectDist < 1.0 - aaWidth)
                {
                    FragColor = Color;
                }
                else if (RoundedRectDist < 1.0)
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos, RoundedRectDist, ShadowBlur);
                    float a = shadow.a;
                    shadow = mix(shadow, Color, 1.0 - a);
                    shadow.a = a;
                    float t = smoothstep(1.0 - aaWidth, 1.0, RoundedRectDist);
                    FragColor = mix(Color, shadow, t);
                }
                else
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos, RoundedRectDist, ShadowBlur);
                    if (RoundedRectDist > 2.0 - aaWidth)
                    {
                        vec4 outsideColor = shadow;
                        outsideColor.a = 0.0;
                        float t = smoothstep(2.0 - aaWidth, 2.0, RoundedRectDist);
                        FragColor = mix(shadow, outsideColor, t);
                    }
                    else
                    {
                        FragColor = shadow;
                    }
                }
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
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos, RoundedRectDist, ShadowBlur);
                    float a = shadow.a;
                    shadow = mix(shadow, BorderColor, 1.0 - a);
                    shadow.a = a;
                    float t = smoothstep(2.0 - aaWidth, 2.0, RoundedRectDist);
                    FragColor = mix(BorderColor, shadow, t);
                }
                else
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos, RoundedRectDist, ShadowBlur);
                    if (RoundedRectDist > 3.0 - aaWidth)
                    {
                        vec4 outsideColor = shadow;
                        outsideColor.a = 0.0;
                        float t = smoothstep(3.0 - aaWidth, 3.0, RoundedRectDist);
                        FragColor = mix(shadow, outsideColor, t);
                    }
                    else
                    {
                        FragColor = shadow;
                    }
                }
            }
        }
        else
        {
            if (!hasBorder && !hasShadow)
            {
                FragColor = Color;
            }
            else if (hasBorder && !hasShadow)
            {
                bool insideFill = (LocalPos.x >= 0.0 && LocalPos.y >= 0.0 && LocalPos.x <= Rect.z && LocalPos.y <= Rect.w);
                bool insideBorder = (LocalPos.x >= -BorderWidth && LocalPos.y >= -BorderWidth && LocalPos.x <= Rect.z + BorderWidth && LocalPos.y <= Rect.w + BorderWidth);

                if (insideFill)
                {
                    FragColor = Color;
                }
                else if (insideBorder)
                {
                    FragColor = BorderColor;
                }
                else
                {
                    discard;
                }
            }
            else if (!hasBorder && hasShadow)
            {
                bool insideFill = (LocalPos.x >= 0.0 && LocalPos.y >= 0.0 && LocalPos.x <= Rect.z && LocalPos.y <= Rect.w);
                vec2 shadowLocal = LocalPos - ShadowOffset;
                bool insideShadow = (shadowLocal.x >= -ShadowBlur && shadowLocal.y >= -ShadowBlur && shadowLocal.x <= Rect.z + ShadowBlur && shadowLocal.y <= Rect.w + ShadowBlur);

                if (insideFill)
                {
                    FragColor = Color;
                }
                else if (insideShadow)
                {
                    FragColor = ShadowColor;
                }
                else
                {
                    discard;
                }
            }
            else if (hasBorder && hasShadow)
            {
                bool insideFill = (LocalPos.x >= 0.0 && LocalPos.y >= 0.0 && LocalPos.x <= Rect.z && LocalPos.y <= Rect.w);
                bool insideBorder = (LocalPos.x >= -BorderWidth && LocalPos.y >= -BorderWidth && LocalPos.x <= Rect.z + BorderWidth && LocalPos.y <= Rect.w + BorderWidth);
                vec2 shadowLocal = LocalPos - ShadowOffset;
                bool insideShadow = (shadowLocal.x >= -(ShadowBlur + BorderWidth) && shadowLocal.y >= -(ShadowBlur + BorderWidth) && shadowLocal.x <= Rect.z + ShadowBlur + BorderWidth && shadowLocal.y <= Rect.w + ShadowBlur + BorderWidth);

                if (insideFill)
                {
                    FragColor = Color;
                }
                else if (insideBorder)
                {
                    FragColor = BorderColor;
                }
                else if (insideShadow)
                {
                    FragColor = ShadowColor;
                }
                else
                {
                    discard;
                }
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
