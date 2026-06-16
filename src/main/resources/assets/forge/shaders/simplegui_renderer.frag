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
in float BorderDist;

out vec4 FragColor;

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
            if (!hasBorder && !hasShadow)
            {

            }
            else if (hasBorder && !hasShadow)
            {
                if (BorderDist >= 0.0 && BorderDist <= 1.0)
                {
                    FragColor = Color;
                }
                if (BorderDist > 1.0 && BorderDist <= 2.0)
                {
                    FragColor = BorderColor;
                }
            }
            else if (!hasBorder && hasShadow)
            {

            }
            else if (hasBorder && hasShadow)
            {

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
