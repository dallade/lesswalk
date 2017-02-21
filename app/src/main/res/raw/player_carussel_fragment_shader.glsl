//#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform sampler2D texSampler2D;
//uniform samplerExternalOES extTexture2D;
//uniform sampler2D extTexture2D;
//uniform float isExternal;
uniform float isViewBackground;
uniform float color_scale;
uniform float alpha;
uniform vec4  backgroundColor;
varying vec2  tCoord;

void main()
{
    vec4 test;
    
    //if(isExternal < 0.5)
    //{
    //	test = texture2D(texSampler2D, tCoord);
	//}
	//else
	//{
	//	test = texture2D(extTexture2D, tCoord);
	//}
	
	test = texture2D(texSampler2D, tCoord);
	
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