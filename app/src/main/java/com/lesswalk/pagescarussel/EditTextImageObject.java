package com.lesswalk.pagescarussel;

import com.lesswalk.bases.ImageObject3D;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

public class EditTextImageObject extends ImageObject3D 
{
	private static final int    WIDTH_RESOLUTION   = 640;
	
	private String  text      			 = null;
	private String  emptyText 			 = null;
	private boolean resetImageOnDrawSelf = false;

	public EditTextImageObject(String emptyText) 
	{
		super("EditTextImageObject");
		this.emptyText = emptyText;
	}

	public void setText(String text) 
	{
		this.text = text;
		//
		resetImageOnDrawSelf  = true;
	}
	
	protected void setRefreshFlag() 
	{
		resetImageOnDrawSelf  = true;
	}
	
	protected String getText() 
	{
		return text == null ? new String(""+emptyText) : new String(text);
	}
	
	@Override
	public void drawSelf() 
	{
		if(resetImageOnDrawSelf || getTextureID() < 0)
		{
			Bitmap              work         =   ImageObject3D.createBitmap(aspect());//Bitmap.createBitmap(WIDTH_RESOLUTION, (int) (WIDTH_RESOLUTION*aspect() + 0.5f), Config.ARGB_8888);;
			Canvas              cwork        =   new Canvas(work);
			Paint               pwork        =   new Paint();
			float               textOffset[] = {0.0f};
			
			pwork.setColor(Color.argb(255, 255, 176, 16));
			
			textOffset[0] += work.getHeight()*0.12f;
			
			ImageObject3D.drawTextToRect(getText(), work.getHeight()*0.20f, work, cwork, pwork, textOffset);
			
			if(getTextureID() < 0)
			{
				generateTextureID(work);
			}
			else setImage(getTextureID(), work);
			
			work.recycle();
			resetImageOnDrawSelf = false;
		}
		super.drawSelf();
	}
}
