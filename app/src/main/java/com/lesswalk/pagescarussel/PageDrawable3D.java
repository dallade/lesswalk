package com.lesswalk.pagescarussel;

import android.util.Log;

import com.lesswalk.bases.RectObject3D;

public class PageDrawable3D extends RectObject3D 
{
	private RectObject3D back_obj             = null;
	private RectObject3D back_obj_backup      = null;
	private int          backgroundID         = -1;
	private int          backgroundIDBackup   = -1;
	private float        currentRotationAngle = 0.0f;
	
	
	public PageDrawable3D(String name) 
	{
		super(name);
	}

	
	// TODO change ID to RECT_3D_OBJECT ! ! !
	
	protected void setBackgroundID(int id)
	{
		backgroundID = id;
	}
	
	protected void removeBackgrounds()
	{
		backgroundID = -1;

		if(back_obj != null)
		{
			back_obj.release();
			back_obj = null;
		}
	}

	public int getBackgroundID() 
	{
		return backgroundID;
	}
	
	public Object getBackgroundObj() 
	{
		return back_obj;
	}

	public float currentRotationAngle() 
	{
		return currentRotationAngle;
	}

	public void updateRotationAngle(float angle) 
	{
		currentRotationAngle = angle;
	}
	
	@Override
	public int getTextureID() 
	{
		if(currentRotationAngle >= 90.0f)
		{
			// reverse rotation
			if(backgroundID < 0) {return backgroundIDBackup;}
			
			return backgroundID;
		}
		else 
		{
			return -1;
		}
	}
	
	@Override
	public void drawSelf() 
	{
		if(currentRotationAngle < 90.0f)
		{
			backgroundIDBackup = -1;
			back_obj_backup = null;
			super.drawSelf();
		}
		else
		{
			if(back_obj != null)
			{
				back_obj.drawSelf();
			}
			else if(back_obj_backup != null)
			{
				back_obj_backup.drawSelf();
			}
			else
			{
				super.drawCurrent();
			}
		}
	}
	
	@Override
	public RectObject3D isOnClicked(float x, float y) 
	{
		if(currentRotationAngle <= 0.0f)
		{
			return super.isOnClicked(x, y);
		}
		else if(currentRotationAngle >= 180.0f)
		{
			if(back_obj != null)
			{
				setBackgroundObjBackup(back_obj);
			}
			else if(getBackgroundID() > 0)
			{
				setBackgroundIDBackup(getBackgroundID());
			}
			removeBackgrounds();
			return this;
		}
		
		return null;
	}

	private void setBackgroundObjBackup(RectObject3D back_obj) 
	{
		back_obj_backup = back_obj;
	}


	private void setBackgroundIDBackup(int backgroundID) 
	{
		backgroundIDBackup = backgroundID;
	}


	public void setBackgroundObject(RectObject3D obj) 
	{
		Log.d("elazarkin", "PageDrawable - setBackgroundObject: " + obj);
		back_obj = obj;
		back_obj.prepare();
	}
}
