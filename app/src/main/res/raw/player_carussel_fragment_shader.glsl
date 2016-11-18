precision mediump float;
uniform sampler2D texSampler2D;
uniform float color_scale;
uniform float alpha;
uniform float isViewBackground;
uniform vec4  backgroundColor;
varying vec2 tCoord;

void main()
{
    vec4 test = texture2D(texSampler2D, tCoord);
    if(test.a < 0.6)
    {
    	if(isViewBackground < 0.5)
    	{
    		discard;
		}
    	else
    	{
    		gl_FragColor = backgroundColor;
    	}
    }
    else
    {
        gl_FragColor = test*color_scale*alpha;
    }
}