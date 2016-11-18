package com.lesswalk.bases;

/**
 * Created by root on 1/28/16.
 */
public abstract class BaseInterRendererLayout extends BaseRenderer
{
    protected int w = 0, h = 0;
    protected RendererLayoutParams params = null;

    public BaseInterRendererLayout(int _w, int _h, RendererLayoutParams _params)
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
