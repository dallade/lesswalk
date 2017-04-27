package com.lesswalk.contact_page.navigation_menu;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.lesswalk.bases.BaseInterRendererLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by elazarkin on 1/22/16.
 */
public class NavigationMenuRenderer extends BaseNavigationMenuRenderer implements GLSurfaceView.Renderer
{
    protected OnSurfaceChangedCallback onChangeCallback = null;
    
    private	boolean isVisable = true;

    public NavigationMenuRenderer()
    {
        super();
    }

    private float basicTransformMatrix[] = new float[16];

    public NavigationMenuRenderer(OnSurfaceChangedCallback _onChangeCallback)
    {
        super();
        onChangeCallback = _onChangeCallback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
    {
        onChangeCallback.onSurfaceCreated();
        // DO NOTHING
    }

    private void multiply(float mat[], int matOffset, float vec[], int vecOffset, float out[], int outOffset)
    {
        for(int i = 0; i < 3; i++)
        {
            out[outOffset + i] = mat[matOffset + i*3] * vec[vecOffset] + mat[matOffset + i*3 + 1] * vec[vecOffset +1] + mat[matOffset + i*3 + 2] * vec[vecOffset + 2];
        }
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h)
    {
        Log.d("elazarkin4", "onSurfaceChanged start");
        onChangeCallback.onSurfaceChanged(w, h);
        Log.d("elazarkin4", "onSurfaceChanged end");
    }

    @Override
    public void onDrawFrame(GL10 gl10)
    {
//        Log.d("elazarkin", "onDraw");
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        //GLES20.glClearColor(0.2f, 0.4f, 0.6f, 0.2f);

        if(isVisable)
        {
	        for(BaseInterRendererLayout l:layouts)
	        {
	            l.draw();
	        }
        }
        //
    }

    @Override
    public void moved(float lastX, float lastY, float x, float y)
    {
        for(BaseInterRendererLayout l:layouts)
        {
            if(l.cordinateIntoLayout(x, y))
            {
                l.movedAction(lastX, lastY, x, y);
            }
        }
    }

    @Override
    public void clicked(float x, float y)
    {
        for(BaseInterRendererLayout l:layouts)
        {
            if(l.cordinateIntoLayout(x, y))
            {
                l.clickedAction(x, y);
            }
        }
    }

	public void setOn()
	{
		isVisable = true;
	}

	public void setOff()
	{
		isVisable = false;
	}
}
