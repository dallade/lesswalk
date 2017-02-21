//#extension GL_OES_EGL_image_external : require
precision mediump float;
//uniform samplerExternalOES texSampler2D;
uniform float isViewBackground;
uniform float color_scale;
uniform float alpha;
uniform vec4  backgroundColor;
varying vec2  tCoord;

void main()
{
    //vec4 test;
    
	//test = texture2D(texSampler2D, tCoord);
	
    //if(test.a < 0.6)
    //{
    //	if(isViewBackground < 0.5)
    //	{
    //		discard;
	//	}
    //	else
    //	{
    //		gl_FragColor = backgroundColor;
    //	}
    //}
    //else
    //{
    //    gl_FragColor = test*color_scale*alpha;
    //}
    
    gl_FragColor = vec4(0.5, 0.8, 0.3, 1.0);
}