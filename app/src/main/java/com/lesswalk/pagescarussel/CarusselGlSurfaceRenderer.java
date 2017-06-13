package com.lesswalk.pagescarussel;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.lesswalk.bases.BaseRenderer;
import com.lesswalk.bases.RectObject3D;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CarusselGlSurfaceRenderer extends BaseRenderer implements GLSurfaceView.Renderer
{
	private static final float         SELF_ROTATION_SPEED  = 0.05f;
	private static final float         TOUCH_ROTATION_SPEED = 0.4f;
	private static final float         HVA                  = 15.0f;
	private              boolean       isVisiable           = false;
	private              GLSurfaceView parent               = null;

	@Override
    public String getRendererName()
    {
        return "CarusselGlSurfaceRenderer";
    }

	private Vector<CarusselPageInterface> container        = null;
	private int                           targetIndex      = -1;
	private ICarusselMainItem             carusselMainItem = null;
	private float                         perspectiveMat[] = null;
	//
	private boolean                       touched          = false;
	private Context                       context          = null;
	
//    private GLSurfaceView parent            = null;
    private int WIDTH  = 0;
	private int HEIGTH = 0;

    public CarusselGlSurfaceRenderer(Context context, GLSurfaceView parent)
    {
        this.context = context;
        this.parent  = parent;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig)
    {
		Log.d("elazarkin2", "onSurfaceCreated");
    	RectObject3D.init(context);
    }

	/*
	 * draw on all screen the half down (centered) area
	 * to make the look on carrussel from up
	 * 
	 *  __________
	 *  |        |
	 *  |  ____  |
	 *  |  |  |  |
	 *  |  |  |  |
	 *  ----------
	 *       ^
	 *       |
	 *      this
	 */
    
    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h)
    {
    	Log.d("elazarkin2", "onSurfaceChanged");
    	
    	WIDTH  = w;
    	HEIGTH = h;
    	
    	CarusselPageInterface.setModelView(w, h);
    	
//    	GLES20.glViewport(0, 0, w, h);

        if(perspectiveMat == null)
        {
            perspectiveMat = new float[16];
        }

        Matrix.perspectiveM(perspectiveMat, 0, hva_to_vva(HVA, w, h), 1.0f*w/h, 0.1f, 50.0f);
    }

    private float hva_to_vva(float hva, float w, float h)
    {
		return (float) Math.toDegrees(Math.atan(Math.tan(Math.toRadians(hva)*h/w)));
	}

	@Override
    public void onDrawFrame(GL10 gl10)
    {
		if(!isVisiable)
		{
			carusselMainItem.clearScreen();
			return;
		}

        if(carusselMainItem != null && container == null)
        {
            container = new Vector<CarusselPageInterface>();
            carusselMainItem.fillContainerByItems(container);
            Log.d("elazarkin2", "updateContainer: containerSize=" + container.size());
			if(container.size() >= 2) targetIndex = container.size()-1;
        }
        else if(carusselMainItem == null) return;

        carusselMainItem.clearScreen();

        carusselMainItem.setPerspectiveMatrix(perspectiveMat);
        
        if(container != null && container.size() > 0)
        {
        	drawCarrusselObj();
        }
    }

	public void setVisiable(boolean b)
	{
		isVisiable = b;
		parent.requestRender();
	}

	private class AngleManager implements Comparator<CarusselPageInterface> 
	{
		float currentAngleOffset = 0.0f;
		int   containerSize      = 0;

		private int checkCurrentAngleOffset(int size)
		{
			float step       = 360.0f/size;
			float current_i  = currentAngleOffset/step;
			int   wanted_i   = current_i >= 0 ? (int)(current_i + 0.5f) : (int)(current_i - 0.5f);

			return checkCurrentAngleOffset(size, wanted_i, SELF_ROTATION_SPEED);
		}
		//
		private int checkCurrentAngleOffset(int size, int wanted_i, float speed)
		{
			int   ret       = wanted_i;
			float step      = 360.0f / size;
			float current_i = currentAngleOffset / step;
			float diff1     = Math.abs(current_i - (current_i < wanted_i ? 0.0f:size) - wanted_i);
			float diff2     = Math.abs(current_i + (current_i < wanted_i ? size:0.0f) - wanted_i);
			//+ (current_i < wanted_i ? 1.0f:-1.0f)*size

			if(touched) {return -1;}

			if(diff1 <= speed || diff2 <= speed)
			{
				currentAngleOffset = step*wanted_i;
				ret =  -1;
			}
			else
			{
				currentAngleOffset += (diff1 > diff2 ? -1.0f : 1.0f)*speed*step;
			}

			while(currentAngleOffset >= 360.0f) currentAngleOffset -= 360.0f;
			while(currentAngleOffset < 0.0f) currentAngleOffset += 360.0f;

			return ret;
		}

	    @Override
	    public int compare(CarusselPageInterface p1, CarusselPageInterface p2) 
	    {
	    	float ang1 = Math.abs(calculateRotationAngle(p1.getIndex()));
	    	float ang2 = Math.abs(calculateRotationAngle(p2.getIndex()));
	    	//
	    	return ang1 > ang2 ? -1: ang1 < ang2 ? 1 : 0;
	    }
	    
		private float fix(float angle) 
		{
			float ret = angle;
			while(ret > 180.0f) ret -= 360.0f;
			while(ret <-180.0f) ret += 360.0f;
			return ret;
		}

		public float calculateRotationAngle(int index) 
		{
			return fix(index*360.0f/containerSize + angleManager.currentAngleOffset);
		}

		public void setCurrentContainerSize(int size) 
		{
			containerSize = size;
		}
	}
	
	private AngleManager angleManager = null;
	
    private void drawCarrusselObj() 
    {
    	try
		{
			float carruselR  = 0.0f;
			float viewOffset = 0.0f;

			float modelView[] = new float[16];
			float rotationM[] = new float[16];

			if (angleManager == null)
			{
				angleManager = new AngleManager();
			}
			//
			angleManager.setCurrentContainerSize(container.size());
			if (targetIndex >= 0)
			{
				targetIndex = angleManager.checkCurrentAngleOffset(container.size(), targetIndex, SELF_ROTATION_SPEED/8.0f);
			}
			else
			{
				angleManager.checkCurrentAngleOffset(container.size());
			}
	        //
	        if(container.size() > 2)
	        {
	            carruselR = (float) (CarusselPageInterface.page_width()/(2.0f*Math.tan(Math.PI/container.size())));
	        }
	        else
	        {
	            carruselR = 0.0f;
	        }
	
	        viewOffset = (float) (1.0f/Math.tan(HVA*Math.PI/360.0f));
	
	        Collections.sort(container, angleManager);
	        
	        for (int i = 0; i < container.size(); i++)
	        {
				float MIN_COLOR_SCALE = 0.4f;
				float color_scale     = 0.0f;
				//
	            float rotationAngle = angleManager.calculateRotationAngle(container.elementAt(i).getIndex());
	            
	            if(!container.elementAt(i).isInited())
	            {
	                container.elementAt(i).freeze();
	            }
	
	            while (rotationAngle <-180.0f) rotationAngle += 360.0f;
	            while (rotationAngle > 180.0f) rotationAngle -= 360.0f;
	
	            if(container.size() == 2)
	            {
	                if(Math.abs(rotationAngle) > 90.0f)continue;
	            }
	
	//            if(i != 3) continue;
	            //
	            color_scale = 1.0f - (Math.abs(rotationAngle)/180.0f)*(1.0f - MIN_COLOR_SCALE);
	            color_scale = (color_scale < MIN_COLOR_SCALE ? MIN_COLOR_SCALE : color_scale);
	
	            container.elementAt(i).setColorScale(color_scale);
	
	            Matrix.setIdentityM(modelView, 0);
	
	            container.elementAt(i).rotateIfNeed(modelView, rotationM);
	            modelView[14] += carruselR;
	            Matrix.setRotateM(rotationM, 0, rotationAngle, 0.0f, 1.0f, 0.0f);
	            Matrix.multiplyMM(modelView, 0, rotationM, 0, modelView, 0);
	            modelView[14] -= (viewOffset + carruselR);
	            
	            container.elementAt(i).setModelView(modelView);
	
	            container.elementAt(i).drawSelf();
	        }
    	} 
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
	}

    public void moved(float lastTouchedX, float lastTouchedY, float x, float y)
    {
        CarusselPageInterface frontPage  = null;
        
        if((frontPage=getCurrentPage()) != null)
        {
	        RectObject3D obj = frontPage.checkOnObject(x, y, WIDTH, HEIGTH);
	        
	        if(obj != null)
	        {
		        angleManager.currentAngleOffset += (x-lastTouchedX)*TOUCH_ROTATION_SPEED;
		        angleManager.currentAngleOffset = angleManager.currentAngleOffset < 0.0f ? angleManager.currentAngleOffset + 360.0f : angleManager.currentAngleOffset > 360.0f ? angleManager.currentAngleOffset - 360.0f : angleManager.currentAngleOffset;
		        touched     = true;
	        }
        }
    }

    public void clicked(float x, float y)
    {
    	CarusselPageInterface frontPage  = null;

        if((frontPage=getCurrentPage()) != null)
        {
	    	if(carusselRotationProcess(frontPage)) return;
	    	
	    	Log.d("elazarkin", "clicked - someObject=" + frontPage.checkOnObjectClickCommand(x, y, WIDTH, HEIGTH));
        }
    }

	public void touch_up()
    {
		if(container != null && container.size() > 0)
    	{
	        for(CarusselPageInterface i:container)
	        {
	            i.releaseClick();
	        }
    	}
        touched = false;
    }
	
	public void touched(float x, float y) 
	{
    	CarusselPageInterface frontPage  = null;
        
        if((frontPage=getCurrentPage()) != null)
        {
        	frontPage.checkOnObject(x, y, WIDTH, HEIGTH);
        }
	}
	
    private CarusselPageInterface getCurrentPage() 
    {
    	if(container != null && container.size() > 0)
    	{
	    	Collections.sort(container, angleManager);
	    	//
			return container.elementAt(container.size()-1);
    	}
    	return null;
	}

	private boolean carusselRotationProcess(CarusselPageInterface frontPage) 
    {
    	float rot_angle = 0.0f;
    	
    	rot_angle = angleManager.calculateRotationAngle(frontPage.getIndex());
    	
		return Math.abs(rot_angle) > SELF_ROTATION_SPEED;
	}

    public void removeAllItems()
    {
    	if(container != null && container.size() > 0)
    	{
	        for(CarusselPageInterface i:container)
	        {
	            i.destroy();
	        }
	        container.removeAllElements();
    	}
        //
        container        = null;
        carusselMainItem = null;
    }

    public void setCarusselMainItem(ICarusselMainItem item)
    {
        carusselMainItem = item;
    }
}