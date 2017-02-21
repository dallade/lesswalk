package com.lesswalk.bases;

import android.util.Log;

/**
 * Created by root on 1/28/16.
 */
public abstract class BaseInterRendererLayout extends BaseRenderer
{
    protected int w = 0, h = 0;
    protected RendererLayoutParams params = null;

    public BaseInterRendererLayout(int _w, int _h, RendererLayoutParams _params)
    {
    	setWhParams(_w, _h, _params);
    }

    public BaseInterRendererLayout(){}
    
	public void setWhParams(int _w, int _h, RendererLayoutParams _params) 
    {
    	w = _w;
    	h = _h;
    	params = _params; 
	}

	public abstract void draw();

    public boolean cordinateIntoLayout(float x, float y)
    {
        if(x < params.getStartWeightX()*w) return false;
        if(x > params.getEndWeightX()*w) return false;
        if(y > h - params.getStartWeightY()*h) return false;
        if(y < h - params.getEndWeightY()*h) return false;

        return true;
    }
    
	protected boolean onObject(float x, float y, float[] object) 
	{
		float real_w = 0.0f, real_h = 0.0f;
		float fix_x = 0.0f, fix_y = 0.0f;
		if(params == null || w == 0 || h == 0) return false;
		
//		Log.d("lesswalk_click", String.format
//		(
//			"y=%3.1f ys=%3.1f ye=%3.1f h=%d", 
//			y, 
//			params.getStartWeightY()*h,
//			params.getEndWeightY()*h,
//			h
//		));
		
		real_w = w*(params.getEndWeightX()-params.getStartWeightX());
		real_h = h*(params.getEndWeightY()-params.getStartWeightY());
		
		fix_x = 2.0f*(x-params.getStartWeightX()*w)/real_w - 1.0f;
		fix_y = 2.0f*(h+params.getStartWeightY()*h-y)/real_h - 1.0f;
		
		Log.d("lesswalk_click", String.format
		(
			"fix(%3.1f, %3.1f) rect(%3.1f, %3.1f):(%3.1f, %3.1f)", 
			fix_x, fix_y,
			object[0], object[1], object[2], object[3]
		));
		
		if(fix_x > object[0] && fix_y > object[1] && fix_x < object[2] && fix_y < object[3])
		{
			return true;
		}
		
		return false;
	}

    public abstract void movedAction(float lastX, float lastY, float x, float y);

    public abstract void clickedAction(float x, float y);

    protected float yStepToRendererYStep(float s)
    {
        return 2.0f*s/h;
    }

    public static class RendererLayoutParams
    {
        private float startWeightX = 0.0f;
        private float startWeightY = 0.0f;
        private float endWeightX   = 1.0f;
        private float endWeightY   = 1.0f;
        //
        public RendererLayoutParams(float _startWeightX, float _startWeigthY, float _endWeigthX, float _endWeightY)
        {
            startWeightX = _startWeightX;
            startWeightY = _startWeigthY;
            endWeightX   = _endWeigthX;
            endWeightY   = _endWeightY;
        }

        public float getStartWeightX()
        {
            return startWeightX;
        }

        public float getStartWeightY()
        {
            return startWeightY;
        }

        public float getEndWeightX()
        {
            return endWeightX;
        }

        public float getEndWeightY()
        {
            return endWeightY;
        }
    }
}
