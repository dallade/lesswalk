attribute vec2   vPosition;
uniform   mat3   u_FixMat;
varying   vec2   v_TexCoordinate;
attribute vec2   a_TexCoordinate;
void main() 
{
    vec3 result;
    float x, y, n;
    //
    result = u_FixMat*vec3(vPosition[0], vPosition[1], 1.0);
    n      = result[2];
    x      = result[0]/n;
    y      = result[1]/n;

    v_TexCoordinate = a_TexCoordinate;
    gl_Position     = vec4(x, y, 1.0, 1.0);
//    gl_Position     = vec4(vPosition[0], vPosition[1], 1.0, 1.0);
}