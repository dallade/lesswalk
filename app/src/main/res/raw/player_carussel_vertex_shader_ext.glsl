attribute vec4 aPosition;
attribute vec2 textureCoord;

uniform   mat4 u_pMatrix;
uniform   mat4 u_mvMatrix;

varying   vec2 tCoord;

void main()
{
    tCoord = textureCoord;
    gl_Position = u_pMatrix*u_mvMatrix*aPosition;
}