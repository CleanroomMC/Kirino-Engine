#version 460 core

#define DRAW_RECT 1
#define DRAW_LINES 2
#define DRAW_BEZIER 3

#define FLAG_RADIUS 1
#define FLAG_BORDER 2
#define FLAG_SHADOW 4

#define FLAG_COLOR1 1
#define FLAG_COLOR2 2

in flat int DrawType;
in flat int Flags;
in flat vec4 Color0;
in flat vec4 Color1;
in flat vec4 Color2;
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
in float LineDist;
in float LineProgress;
in flat float TotalLineLength;

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
                    FragColor = Color0;
                }
                else
                {
                    vec4 outsideColor = Color0;
                    outsideColor.a = 0.0;
                    float t = smoothstep(1.0 - aaWidth, 1.0, RoundedRectDist);
                    FragColor = mix(Color0, outsideColor, t);
                }
            }
            else if (hasBorder && !hasShadow)
            {
                if (RoundedRectDist < 1.0 - aaWidth)
                {
                    FragColor = Color0;
                }
                else if (RoundedRectDist < 1.0 + aaWidth)
                {
                    float t = smoothstep(1.0 - aaWidth, 1.0 + aaWidth, RoundedRectDist);
                    FragColor = mix(Color0, BorderColor, t);
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
                    FragColor = Color0;
                }
                else if (RoundedRectDist < 1.0)
                {
                    vec4 shadow = ShadowColor;
                    shadow.a *= directionalShadow(ShadowOffset, Rect, LocalPos, RoundedRectDist, ShadowBlur);
                    float a = shadow.a;
                    shadow = mix(shadow, Color0, 1.0 - a);
                    shadow.a = a;
                    float t = smoothstep(1.0 - aaWidth, 1.0, RoundedRectDist);
                    FragColor = mix(Color0, shadow, t);
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
                    FragColor = Color0;
                }
                else if (RoundedRectDist < 1.0 + aaWidth)
                {
                    float t = smoothstep(1.0 - aaWidth, 1.0 + aaWidth, RoundedRectDist);
                    FragColor = mix(Color0, BorderColor, t);
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
                FragColor = Color0;
            }
            else if (hasBorder && !hasShadow)
            {
                bool insideFill = (LocalPos.x >= 0.0 && LocalPos.y >= 0.0 && LocalPos.x <= Rect.z && LocalPos.y <= Rect.w);
                bool insideBorder = (LocalPos.x >= -BorderWidth && LocalPos.y >= -BorderWidth && LocalPos.x <= Rect.z + BorderWidth && LocalPos.y <= Rect.w + BorderWidth);

                if (insideFill)
                {
                    FragColor = Color0;
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
                    FragColor = Color0;
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
                    FragColor = Color0;
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
        float aaWidth = fwidth(LineDist);
        aaWidth = max(aaWidth, 0.01);

        vec4 targetColor = Color0;

        bool hasColor1 = (Flags & FLAG_COLOR1) != 0;
        bool hasColor2 = (Flags & FLAG_COLOR2) != 0;

        if (hasColor1 && hasColor2)
        {
            float _half = TotalLineLength / 2.0;
            if (LineProgress < _half)
            {
                targetColor = mix(Color0, Color1, LineProgress / _half);
            }
            else
            {
                targetColor = mix(Color1, Color2, (LineProgress - _half) / _half);
            }
        }
        else if (hasColor1)
        {
            targetColor = mix(Color0, Color1, LineProgress / TotalLineLength);
        }

        if (LineDist < 1.0 + aaWidth)
        {
            float perc = clamp((max(1.0, LineDist) - 1.0) / aaWidth, 0.0, 1.0);
            perc = 1.0 - perc;
            vec4 outsideColor = targetColor;
            outsideColor.a = 0.0;
            float t = smoothstep(0.0, 1.0, perc);
            FragColor = mix(targetColor, outsideColor, t);
        }
        else if (LineDist < 2.0 - aaWidth)
        {
            FragColor = targetColor;
        }
        else
        {
            vec4 outsideColor = targetColor;
            outsideColor.a = 0.0;
            float t = smoothstep(2.0 - aaWidth, 2.0, LineDist);
            FragColor = mix(targetColor, outsideColor, t);
        }
    }
    else if (DrawType == DRAW_BEZIER)
    {

    }
    else
    {
        discard;
    }
}
