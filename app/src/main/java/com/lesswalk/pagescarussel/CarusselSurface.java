package com.lesswalk.pagescarussel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by root on 3/11/16.
 */
public class CarusselSurface extends GLSurfaceView
{
	private static final int MIN_MOVED_COUNTER = 7;

    private CarusselGlSurfaceRenderer renderer       = null;
    private Context                   context        = null;
    private float                     lastTouchedX   = 0.0f;
    private float                     lastTouchedY   = 0.0f;
    private int                       movedCounter[] = {0};

    public CarusselSurface(Context context)
    {
        super(context);
        this.context = context;
    }

    public void initiation()
    {
//    	setZOrderMediaOverlay(true);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setEGLContextClientVersion(2);

        setRenderer(renderer = new CarusselGlSurfaceRenderer(context, this));

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean ret   = false;

        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            ret = true;
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            if(!moved(movedCounter))
            {
                renderer.clicked(lastTouchedX, lastTouchedY);
            }
            
            renderer.touch_up();
            
            movedCounter[0] = 0;
            
            ret = false;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
        	if(moved(movedCounter))
        	{
        		renderer.moved(lastTouchedX, lastTouchedY, event.getX(), event.getY());
        	}
        	else
        	{
        		renderer.touched(event.getX(), event.getY());
        	}
        	//
            ret   = true;
        }
        
        if(ret)
        {
            lastTouchedX = event.getX();
            lastTouchedY = event.getY();
        }

        return ret;
    }

    private boolean moved(int movedCounter[]) 
    {
    	movedCounter[0]++;
    	//
		return movedCounter[0] >= MIN_MOVED_COUNTER;
	}

	public void removeCarusselItems()
    {
        renderer.removeAllItems();
    }

    public void addCarusselMainItem(ICarusselMainItem item)
    {
        renderer.setCarusselMainItem(item);
    }

    public void setVisiable(boolean b)
    {
        if(renderer != null) renderer.setVisiable(b);
    }
}
