package com.lesswalk.editor_pages.objects3D;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

public class EditorImageTipObject3D extends RectObject3D 
{
	private boolean resetImageOnDrawSelf  = false;
	private Context context               = null;
	private Bitmap  image                 = null;
	
	// TODO add context to all RectObjects in the future
	public EditorImageTipObject3D(Context context)
	{
		this.context = context;
	}
	
	public void setText(String text) 
	{
		resetImageOnDrawSelf  = true;
	}
	
	@Override
	public void drawSelf() 
	{
		if(resetImageOnDrawSelf || getTextureID() < 0)
		{
			Bitmap work         = ImageObject3D.createBitmap(aspect());
			
			if(image == null)
			{
				Bitmap original     = BitmapFactory.decodeResource(context.getResources(), R.drawable.place_image_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
				Bitmap smalled      = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas csmalled     = new Canvas(smalled);
				float  scale        = 1.0f;
				
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
				ImageObject3D.CircleCut(smalled, Color.argb(255, 255, 176, 16));
				ImageObject3D.drawImageAtCenter(work, smalled, aspect());
				
				original.recycle();
				smalled.recycle();
			}
			else
			{
				ImageObject3D.drawImageAtCenter(work, image, aspect());
				ImageObject3D.CircleCut(work, Color.argb(255, 255, 176, 16));
			}

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

	public void setImageTip(Bitmap photo) 
	{
		image                 = photo;
		resetImageOnDrawSelf  = true;
	}
}
