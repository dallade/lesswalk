precision mediump float;
uniform sampler2D texSampler2D;
varying vec2  tCoord;

void main()
{
    gl_FragColor = texture2D(texSampler2D, tCoord);
}