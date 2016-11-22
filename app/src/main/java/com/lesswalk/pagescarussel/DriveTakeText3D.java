package com.lesswalk.pagescarussel;

import com.lesswalk.bases.RectObject3D;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.Log;

public class DriveTakeText3D extends RectObject3D 
{
	private static final int    WIDTH_RESOLUTION   = 1024;
	private static final String DRIVE_FORMAT       = "%3.1f ~%d mins";
	private static final String WALK_FORMAT        = "+%dmin";
	private static final String WALK_ADDITION_TEXT = "LW Time";
	
	private String driveTime = null;
	private String walkTime = null;
	
	private Bitmap work  = null;
	private Canvas cwork = null;
	private Paint  pwork = null;
	
	private float asspect = 1.0f;
	private boolean resetImageInDrawSelf = false;
	
	public DriveTakeText3D(float asspect) 
	{
		this.asspect = asspect;
	}
	
	public void setDriveWalkText(int driveDistMETRE, int driveTimeMINUTES, int walkDistMETRE, int walkTimeMINUTES)
	{
		if(driveDistMETRE >= 0 && driveTimeMINUTES >= 0)
		{
			driveTime = String.format(DRIVE_FORMAT, driveDistMETRE/1000.0f, driveTimeMINUTES);
		}
		else driveTime = "unknow";
		
		if(walkTimeMINUTES >= 0)
		{
			walkTime  = String.format(WALK_FORMAT, walkTimeMINUTES);
		}
		else walkTime = "unknow";
		
		//Log.d("elazarkin", "driveTime=" + driveTime + " walkTime=" + walkTime);
		//
		init();
	}

	@Override
	public void drawSelf() 
	{
		if(resetImageInDrawSelf)
		{
			setImage(getTextureID(), work);
			resetImageInDrawSelf = false;
		}
		super.drawSelf();
	}

	private void init() 
	{
		if(work == null)
		{
			work = Bitmap.createBitmap(WIDTH_RESOLUTION, (int) (WIDTH_RESOLUTION*asspect + 0.5f), Config.ARGB_8888);
			cwork = new Canvas(work);
			pwork = new Paint();
		}
		
		cwork.drawColor(Color.argb(0, 0, 0, 0), Mode.CLEAR);
		
		drawBackground(Color.argb(255, 255, 176, 16));
		
		if(getTextureID() < 0)
		{
			generateTextureID(work);
		}
		else resetImageInDrawSelf = true;
	}

	private void drawBackground(int argb) 
	{
		float radius        = work.getHeight()/2.0f;
		float textPadding   = work.getWidth()/20.0f;
		Rect  driveBound    = new Rect();
		Rect  walkBound     = new Rect();
		Rect  addTextBound  = new Rect();
		
		pwork.setColor(argb);
		pwork.setStyle(Style.FILL);
		
		cwork.drawCircle(radius, radius, radius, pwork);
		cwork.drawCircle(work.getWidth() - radius, radius, radius, pwork);
		
		cwork.drawRect(radius, 0, work.getWidth() - radius, radius*2.0f, pwork);
		
		pwork.setTextSize(radius*0.8f);
		
		pwork.getTextBounds(driveTime, 0, driveTime.length(), driveBound);
		pwork.getTextBounds(walkTime, 0, walkTime.length(), walkBound);
		pwork.getTextBounds(WALK_ADDITION_TEXT, 0, WALK_ADDITION_TEXT.length(), addTextBound);
		
		pwork.setColor(Color.BLACK);
		cwork.drawText(driveTime, radius, work.getHeight()/2 + driveBound.height()/2, pwork);
		cwork.drawText(walkTime, work.getWidth() - radius - addTextBound.width() - walkBound.width() - textPadding, work.getHeight()/2 + walkBound.height()/2, pwork);
		pwork.setColor(Color.WHITE);
		cwork.drawText(WALK_ADDITION_TEXT, work.getWidth() - radius - addTextBound.width(), work.getHeight()/2 + addTextBound.height()/2, pwork);
	}
}