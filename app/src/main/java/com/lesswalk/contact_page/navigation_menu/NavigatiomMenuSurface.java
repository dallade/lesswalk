package com.lesswalk.contact_page.navigation_menu;

import com.lesswalk.bases.BaseInterRendererLayout;
import com.lesswalk.bases.IContactManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by elazarkin on 2/21/16.
 */
public class NavigatiomMenuSurface extends GLSurfaceView
{
    private NavigationMenuRenderer  navigationMenuR  = null;
//    private Vector<CarusselContact> contacts         = null;

    private float   lastTouchedX = 0.0f;
    private float   lastTouchedY = 0.0f;
    private boolean alreadyAdded = false;
    
//    private NavigationIconLayout    navigationIconLayout    = null;
    private NavigationContactLayout navigationContactLayout = null;
    
    public NavigatiomMenuSurface(Context context)
    {
        super(context);
        
//        navigationIconLayout    = new NavigationIconLayout(getContext());
        navigationContactLayout = new NavigationContactLayout(getContext());
    }

    private void setContactNavigationMenu(int w, int h)
    {
    	Log.d("lesswalk","setContactNavigationMenu " + w + "x" + h);
    	
    	navigationContactLayout.setWhParams(w, h, new BaseInterRendererLayout.RendererLayoutParams(0.0f, 0.0f, 1.0f, 1.0f));
    	navigationContactLayout.init(getContext());
        navigationMenuR.addLayoutItem(navigationContactLayout);
    }
    
    private static final float MAX_MOVED_DIST_TO_CLICK = 10.0f;
    private float moved_dist = 0.0f;
    private long  touch_time = 0L;

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean ret   = false;

        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.d("elazarkin", "touched");
            lastTouchedX = event.getX();
            lastTouchedY = event.getY();
            moved_dist   = 0.0f;
            touch_time   = System.currentTimeMillis();
            ret = true;
        }
        else if(event.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d("elazarkin", "up");
            if(moved_dist < MAX_MOVED_DIST_TO_CLICK && System.currentTimeMillis() - touch_time < 200L)
            {
                navigationMenuR.clicked(lastTouchedX, lastTouchedY);
            }
            ret = false;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
        	float diff_x = event.getX() - lastTouchedX;
        	float diff_y = event.getY() - lastTouchedY;
            navigationMenuR.moved(lastTouchedX, lastTouchedY, event.getX(), event.getY());
            
            moved_dist += (float) Math.sqrt(diff_x*diff_x + diff_y*diff_y);
            
    		lastTouchedX = event.getX();
            lastTouchedY = event.getY();
//            Log.d("elazarkin", "moved");
            ret   = true;
        }

        requestRender();
        return ret;
    }

    public void initiation()
    {
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setEGLContextClientVersion(2);

        setRenderer(navigationMenuR = new NavigationMenuRenderer(new NavigationMenuRenderer.OnSurfaceChangedCallback()
        {
            @Override
            public void onSurfaceChanged(int w, int h)
            {
                if(!alreadyAdded)
                {
                    setContactNavigationMenu(w, h);
                    alreadyAdded = true;
                }
            }
        }));

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void setContactManager(IContactManager manager)
    {
		navigationContactLayout.setContactManager(manager);
    }

	public void setContactFilter(String text)
	{
		navigationContactLayout.setContactFilter(text);
	}

	public void setOn()
	{
		if(navigationMenuR != null)
		{
			setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			navigationMenuR.setOn();
		}
	}

	public void setOff()
	{
		if(navigationMenuR != null)
		{
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			navigationMenuR.setOff();
			requestRender();
		}
	}
}
