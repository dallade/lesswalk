package com.lesswalk.bases;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.lesswalk.R;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

public class RectObject3D extends BaseObject3D
{
    protected static final int   HANDLER_ATTR_VER_POS_INDEX     = 0;
    protected static final int   HANDLER_ATTR_TEX_COORD_INDEX   = 1;
    protected static final int   HANDLER_ATTR_SIZE              = 2;

    protected static final int   HANDLER_UNIF_PER_MAT_INDEX     = 0;
    protected static final int   HANDLER_UNIF_MV_MAT_INDEX      = 1;
    protected static final int   HANDLER_UNIF_TEXTURE_INDEX     = 2;
    protected static final int   HANDLER_UNIF_COLOR_SCALE       = 3;
    protected static final int   HANDLER_UNIF_ALPHA             = 4;
    protected static final int   HANDLER_VIEW_BACKGROUND        = 5;
    protected static final int   HANDLER_BACKGROUND_COLOR       = 6;
    protected static final int   HANDLER_UNIF_SIZE              = 7;
    
    private static int         program                          =-1;
    private static int         program_ext                      =-1;
    private static int         attrHandlers[]                   = null;
    private static int         unifHandlers[]                   = null;
    
    private static int         attrHandlers_ext[]               = null;
    private static int         unifHandlers_ext[]               = null;
    
    protected static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	
    private static final float[] baseFrameObject =
    {
        -0.5f, -0.5f, 0.0f,  // top left
         0.5f, -0.5f, 0.0f,  // bottom left
        -0.5f,  0.5f, 0.0f,  // bottom right
         0.5f,  0.5f, 0.0f  // top right
    };

    private static final float textureMap[] =
    {
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    };

	public interface OnClickedAction
	{
		void onClicked();
	}
	
	private OnClickedAction clickCallback = null;
    
	protected class Point3D
	{
		private float x = 0.0f;
		private float y = 0.0f;
		private float z = 0.0f;
		
		public Point3D(float x, float y, float z) 
		{
			init(x, y, z);
		}
		
		public Point3D(Point3D p) {this(p.x, p.y, p.z);}
		
		public Point3D(){this(0.0f, 0.0f, 0.0f);}

		void init(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		void init(Point3D p) {init(p.x, p.y, p.z);}
	}
	
	private int[] textID      = {-1};
    private float frame[]     = null;
    //
    private Vector<RectObject3D> childs = null;
    //
	private FloatBuffer vertexesBuffer   = null;
	private FloatBuffer textureMapBuffer = null;
	
	private RectObjectParams params = null;

	private float   objectAlpha      = 1.0f;
	private boolean isViewBackground = false;
	
	private String objectName = null;
	
	private float backgroundColor[] = null;
	
	private class RectObjectParams
	{
		RectObjectParams parent;

		float weight;
		float x0;
		float y0;
		float width;
		float aspect;
		
		public RectObjectParams(RectObjectParams parent, float x0, float y0, float w, float aspect, float weight) 
		{
			this.parent = parent;
			this.weight = weight;
			this.x0     = x0;
			this.y0     = y0;
			this.width  = w;
			this.aspect = aspect;
		}
		
	    public float x0() 
	    {
	    	return (parent != null ? parent.x0() + x0*parent.width() : x0);
		}
	    
	    public float y0() 
	    {
	    	
	    	return (parent != null ? parent.y0() + y0*parent.height() : y0);
		}
	    
		public float weight() {return (parent != null ? parent.weight() : 1.0f)*weight;}
	    
	    public float width()  {return (parent != null ? parent.width() : 1.0f)*width*weight;}
	    
	    public float height() {return width()*aspect();}
	    
//	    public float aspect()
//		{
//			return (parent != null ? parent.aspect():1.0f)*aspect;
//		}
	    public float aspect() {return aspect;}
	    
	}
	
    public RectObject3D()
    {
    	objectName = "RectObject3D";
    }
    
    public RectObject3D(String name) 
    {
    	objectName = new String("" + name);
	}
    
    public RectObject3D isOnObject(float x, float y)
    {
    	RectObject3D ret = null;
    	//
    	if(childs != null && childs.size() > 0)
    	{
    		for(RectObject3D o:childs)
    		{
    			if((ret=o.isOnObject(x, y)) != null) return ret;
    		}
    	}
    	
    	if(checkOnCurrentObject(x, y)) return this;
    	
    	return null;
    }

	public RectObject3D isOnClicked(float x, float y)
    {
    	RectObject3D ret = null;
    	//
    	if(childs != null && childs.size() > 0)
    	{
    		for(RectObject3D o:childs)
    		{
    			if((ret=o.isOnClicked(x, y)) != null) return ret;
    		}
    	}
    	
    	if(checkOnCurrentObject(x, y))
    	{
    		if(clickCallback != null)
    		{
    			clickCallback.onClicked();
    		}
    		return this;
    	}
    	
    	return null;
    }
	
	public void setOnClickCallback(OnClickedAction callback)
	{
		this.clickCallback = callback;
	}
	
	private boolean checkOnCurrentObject(float x, float y)
	{
		if(objectName.contains("Parking_page"))
		{
			Log.d("elazarkin16", String.format
			(
					"%s:[(%3.2f, %3.2f)->(%3.2f, %3.2f)]       clicked(%3.2f, %3.2f)",
					objectName,
					x0() - width() / 2, y0() - height() / 2,
					x0() + width() / 2, y0() + height() / 2,
					x, y
			));
		}

    	return
	    	(
	    		getTextureID() >= 0
	    		&&
				x > x0() - width()/2 
				&& 
				x < x0() + width()/2
				&&
				y > y0() - height()/2
				&&
				y < y0() + height()/2
			);
	}
	
	public void releseClick() 
	{
    	if(childs != null && childs.size() > 0)
    	{
    		for(RectObject3D o:childs)
    		{
    			o.releseClick();
    		}
    	}
	}
    
    public float x0() {return params == null ? 0.0f:params.x0();}
    //
    public float y0() {return params == null ? 0.0f:params.y0();}
    
    public float weight() {return params == null ? 1.0f:params.weight();}
    
    public float width() {return params == null ? 1.0f:params.width();}
    
    public float height() {return params == null ? 1.0f:params.height();}
    
    public float aspect() {return params == null ? 1.0f:params.aspect();}
    
    private float fix_x(float x) 
    {
    	return x0() + x*width();
	}

    private float fix_y(float y) 
	{
		return y0() + y*height();
	}
	
    public float fix_size(float size)
	{
		return size*weight();
	}
	
	public void initObject(RectObject3D parent, float x0, float y0, float w, float aspect, float weight)
    {
        if(frame == null) {frame = new float[baseFrameObject.length];}
        //
    	params = new RectObjectParams(parent == null ? null:parent.params, x0, y0, w, aspect, weight);
        //
        for(int i = 0; i < frame.length; i += 3)
        {
        	frame[i+0] = fix_x(baseFrameObject[i + 0]);
        	frame[i+1] = fix_y(baseFrameObject[i + 1]);
        	frame[i+2] =       baseFrameObject[i + 2];
        }
    }
    
    public void addChild(RectObject3D obj) 
	{
		if(childs == null) childs = new Vector<RectObject3D>();
		//
		childs.add(obj);
	}


    protected Buffer getVertexesBuffer() 
	{
		if(vertexesBuffer == null)
		{
			vertexesBuffer = loadFloatBuffer(frame);
		}
		return vertexesBuffer;
	}
    
    protected Buffer getTextureMapBuffer() 
	{
		if(textureMapBuffer == null)
		{
			textureMapBuffer = loadFloatBuffer(textureMap);
		}
		return textureMapBuffer;
	}
    
	public void drawSelf() 
	{
		if(getTextureID() >= 0)
		{
			drawCurrent();
		}
	        
        if(childs != null && childs.size() > 0)
        {
        	for(RectObject3D o:childs)
        	{
        		o.drawSelf();
        	}
        }
	}
	
	public void updateObjectAlpha(float alpha) 
	{
		objectAlpha = alpha;
	};
	
	protected void generateTextureID()
	{
		GLES20.glGenTextures(1, textID, 0);
	}
    
    public void generateTextureID(Bitmap image) 
    {
    	generateTextureID();
    	//
    	setImage(getTextureID(), image);
	}
    
    protected static void setImage(int id, Bitmap bit)
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bit, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        
//      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
//      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
//      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//      GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }
    
    public void destroy()
    {
    	if(childs != null && childs.size() > 0)
    	{
    		for(RectObject3D o:childs) o.destroy();
    	}
    	
    	if(textID != null && textID[0] >= 0)
    	{
    		GLES20.glDeleteTextures(textID.length, textID, 0);
    	}
    }
    
    public int getTextureID() { return textID[0];}
    
    @Override
    public String toString() {return objectName + "_RectObject3D";}
    
    protected void drawCurrent(int textureID) 
	{
    	int _program = isExternal() ? RectObject3D.program_ext : RectObject3D.program;
    	int attrs[]  = isExternal() ? RectObject3D.attrHandlers_ext : RectObject3D.attrHandlers;
    	int unifs[]  = isExternal() ? RectObject3D.unifHandlers_ext : RectObject3D.unifHandlers;
    	
    	int target   = isExternal() ? GL_TEXTURE_EXTERNAL_OES:GLES20.GL_TEXTURE_2D;
    	
    	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    	GLES20.glUseProgram(_program);
		GLES20.glUniform1f(unifs[HANDLER_UNIF_ALPHA], objectAlpha);
		GLES20.glUniform1f(unifs[HANDLER_VIEW_BACKGROUND], isViewBackground ? 1.0f:0.0f);
		
		if(backgroundColor == null)
		{
			backgroundColor = new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random(), 1.0f};
		}
		
		GLES20.glUniform4f(unifs[HANDLER_BACKGROUND_COLOR], backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
		
        GLES20.glEnableVertexAttribArray(attrs[HANDLER_ATTR_VER_POS_INDEX]);
        GLES20.glVertexAttribPointer(attrs[HANDLER_ATTR_VER_POS_INDEX], 3, GLES20.GL_FLOAT, false, 0, getVertexesBuffer());
        GLES20.glEnableVertexAttribArray(attrs[HANDLER_ATTR_TEX_COORD_INDEX]);
        GLES20.glVertexAttribPointer(attrs[HANDLER_ATTR_TEX_COORD_INDEX], 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE / 8, getTextureMapBuffer());
        GLES20.glEnable(target);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(unifs[HANDLER_UNIF_TEXTURE_INDEX], 0);
        GLES20.glBindTexture(target, textureID);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}
    
	protected void drawCurrent() 
	{
		drawCurrent(getTextureID());
	}

	public static void init(Context context) 
	{
		//if(program < 0)
		{
	        program = createProgram
	        (
	            loadShaderFromGlslFile(context.getResources(), R.raw.player_carussel_vertex_shader),
	            loadShaderFromGlslFile(context.getResources(), R.raw.player_carussel_fragment_shader)
	        );
	        
	        attrHandlers     = new int[HANDLER_ATTR_SIZE];
	        unifHandlers     = new int[HANDLER_UNIF_SIZE];
	        checkGlError("create program check");
	        Log.d("elazarkin", "program=" + program);
	        loadHandlers(attrHandlers, unifHandlers, program);
		}
		
		//if(program_ext < 0)
		{
	        
	        program_ext = createProgram
	        (
	            loadShaderFromGlslFile(context.getResources(), R.raw.player_carussel_vertex_shader_ext),
	            loadShaderFromGlslFile(context.getResources(), R.raw.player_carussel_fragment_shader_ext)
	        );
	        checkGlError("create program check");
	        attrHandlers_ext = new int[HANDLER_ATTR_SIZE];
	        unifHandlers_ext = new int[HANDLER_UNIF_SIZE];
	        Log.d("elazarkin", "program_ext=" + program_ext);
	        loadHandlers(attrHandlers_ext, unifHandlers_ext, program_ext);
		}
	}
	
    protected static void loadHandlers(int atts[], int unifs[], int _program)
    {
        String attrNames[] = new String[HANDLER_ATTR_SIZE];
        String unifNames[] = new String[HANDLER_UNIF_SIZE];

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(_program);

        attrNames[HANDLER_ATTR_VER_POS_INDEX]   = "aPosition";
        attrNames[HANDLER_ATTR_TEX_COORD_INDEX] = "textureCoord";

        for (int i = 0; i < attrNames.length; i++)
        {
            atts[i] = GLES20.glGetAttribLocation(_program, attrNames[i]);
            Log.d("elazarkin", "attr: " + attrNames[i] + " = " + atts[i]);
        }

        unifNames[HANDLER_UNIF_PER_MAT_INDEX]      = "u_pMatrix";
        unifNames[HANDLER_UNIF_MV_MAT_INDEX]       = "u_mvMatrix";
        unifNames[HANDLER_UNIF_TEXTURE_INDEX]      = "texSampler2D";
        unifNames[HANDLER_UNIF_COLOR_SCALE]        = "color_scale";
        unifNames[HANDLER_UNIF_ALPHA]              = "alpha";
        unifNames[HANDLER_VIEW_BACKGROUND]         = "isViewBackground";
        unifNames[HANDLER_BACKGROUND_COLOR]        = "backgroundColor";

        for (int i = 0; i < unifNames.length; i++)
        {
            unifs[i] = GLES20.glGetUniformLocation(_program, unifNames[i]);
            Log.d("elazarkin", "unif: " + unifNames[i] + " = " + unifs[i]);
        }
    }

	public static int getPerspectiveMatHandler() 
	{
		return unifHandlers[HANDLER_UNIF_PER_MAT_INDEX];
	}

	public static void useProgram()
	{
		GLES20.glUseProgram(program);
	}

	public static void clear() 
	{
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDepthMask(true);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
	}
	
	public int getUnifHandler(int HAND_ID) 
	{
		return unifHandlers[HAND_ID];
	}

	public static void setPerspectiveMatrix(float[] perspectiveMat) 
	{
		GLES20.glUniformMatrix4fv(RectObject3D.getPerspectiveMatHandler(), 1, false, perspectiveMat, 0);
	}

	public void prepare() {}
	public void release() {}
	
    public static void checkGlError(String title)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        {
            Log.e("elazarkin4", "glError " + title + ": " + error);
        }
    }
    
    protected boolean isExternal()
    {
    	return false;
    }
}
