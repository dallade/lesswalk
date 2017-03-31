package com.lesswalk.editor_pages.objects3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

public class EditorTextTipObject3D extends RectObject3D 
{
	private String  tip_text              = null;
	private boolean resetImageOnDrawSelf  = false;
	private Context context               = null;
	
	// TODO add context to all RectObjects in the future
	public EditorTextTipObject3D(Context context)
	{
		this.context = context;
	}
	
	public void setTipText(String text) 
	{
		tip_text = text;
		//
		resetImageOnDrawSelf  = true;
	}
	
	@Override
	public void drawSelf() 
	{
		if(resetImageOnDrawSelf || getTextureID() < 0)
		{
			Bitmap work         = ImageObject3D.createBitmap(aspect());
			Canvas cwork        = new Canvas(work);
			Bitmap original     = BitmapFactory.decodeResource(context.getResources(), R.drawable.place_tips_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
			Bitmap smalled      = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas csmalled     = new Canvas(smalled);
			float  scale        = 0.9f;
//			float  text_start[] = {0.7f};
			
			csmalled.drawBitmap
			(
				original,
				new Rect(0, 0, original.getWidth(), original.getHeight()), 
				new Rect
				(
					(int)(smalled.getWidth()*(1.0f-scale)), 
					(int)(smalled.getHeight()*(1.0f-scale)), 
					(int)(smalled.getWidth()*scale), 
					(int)(smalled.getHeight()*scale)
				), 
				null
			);

//			ImageObject3D.fixIconByColor(smalled, Color.argb(255, 255, 176, 16));
			ImageObject3D.CircleCut(smalled, (tip_text == null || tip_text.length() <= 0 ? Color.argb(255, 255, 176, 16):Color.argb(255, 230, 216, 36)));

			ImageObject3D.drawImageAtCenter(work, smalled, aspect());
//			cwork.drawBitmap
//			(
//				smalled, 
//				new Rect(0, 0, smalled.getWidth(), smalled.getHeight()), 
//				new Rect
//				(
//					(int)(work.getWidth()*(1.0f - text_start[0])/2.0f), 
//					0, 
//					(int)(work.getWidth()*text_start[0]), 
//					(int)(work.getHeight()*text_start[0])
//				), 
//				null
//			);
			
			if(getTextureID() < 0)
			{
				generateTextureID(work);
			}
			else setImage(getTextureID(), work);
			
			work.recycle();
			original.recycle();
			smalled.recycle();
			
			resetImageOnDrawSelf = false;
		}
		super.drawSelf();
	}

	public String getTipText() 
	{
		return tip_text == null || tip_text.length() <= 0 ? "":tip_text;
	}
}
