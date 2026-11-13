#version 460 compatibility

uniform int KirinoEngine_UI_transformMode;
uniform mat4 KirinoEngine_UI_postNdcTransform;

vec4 kirinoApplyUiTransform(vec4 uiNdc)
{
    if (KirinoEngine_UI_transformMode == 0) return uiNdc;
    vec4 mapped = KirinoEngine_UI_postNdcTransform * uiNdc;
    if (KirinoEngine_UI_transformMode == 1) return gl_ModelViewProjectionMatrix * mapped;
    return mapped;
}

uniform float posX;
uniform float posY;
uniform float width;
uniform float height;

out vec2 UV;

void main()
{
    if (gl_VertexID == 0)
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX, posY, 0.0, 1.0));
        UV = vec2(0.0, 0.0);
    }
    else if (gl_VertexID == 1)
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX, posY + height, 0.0, 1.0));
        UV = vec2(0.0, 1.0);
    }
    else if (gl_VertexID == 2)
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX + width, posY + height, 0.0, 1.0));
        UV = vec2(1.0, 1.0);
    }
    else if (gl_VertexID == 3)
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX, posY, 0.0, 1.0));
        UV = vec2(0.0, 0.0);
    }
    else if (gl_VertexID == 4)
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX + width, posY + height, 0.0, 1.0));
        UV = vec2(1.0, 1.0);
    }
    else
    {
        gl_Position = kirinoApplyUiTransform(vec4(posX + width, posY, 0.0, 1.0));
        UV = vec2(1.0, 0.0);
    }
}
