package com.lesswalk.bases;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;

public class ImageObject3D extends RectObject3D
{
	private static final int FRAME_PIXEL_W = 640;
	public ImageObject3D() 
	{
		super();
	}
	
	public ImageObject3D(String name) 
	{
		super(name);
	}
	
	public static void fixIconByColor(Bitmap icon, int argb) 
    {
        int iconPixs[] = new int[icon.getWidth() * icon.getHeight()];
        //
        icon.getPixels(iconPixs, 0, icon.getWidth(), 0, 0, icon.getWidth(), icon.getHeight());
        //
        for (int i = 0; i < iconPixs.length; i++)
        {
            if (iconPixs[i] == Color.BLACK)
            {
                iconPixs[i] = argb;
            }
            else {iconPixs[i] = Color.argb(0, 0, 0, 0);}
        }

        icon.setPixels(iconPixs, 0,icon.getWidth(), 0, 0, icon.getWidth(), icon.getHeight());
	}

	public static void CircleCut(Bitmap bit) 
	{
		CircleCut(bit, 0, false);
	}
	
	public static void CircleCut(Bitmap bit, int argb) 
	{
		CircleCut(bit, argb, true);
	}
	
	private static void CircleCut(Bitmap bit, int argb, boolean useColor)
	{
		Canvas c          = new Canvas(bit);
		Paint  p          = new Paint();
		int    iconPixs[] = new int[bit.getWidth()*bit.getHeight()];
		float  radius     = (bit.getWidth() <= bit.getHeight()) ? bit.getWidth()/2 : bit.getHeight()/2;
		float  dx         = 0.0f;
		float  dy         = 0.0f;
		float  strokeW    = 5.0f;
		//
		bit.getPixels(iconPixs, 0, bit.getWidth(), 0, 0, bit.getWidth(), bit.getHeight());
		
		for(int y = 0; y < bit.getHeight(); y++)
		{
			for(int x = 0; x < bit.getWidth(); x++)
			{
				dx = x - bit.getWidth()/2;
				dy = y - bit.getHeight()/2;
				//
				if(Math.sqrt(dx*dx + dy*dy) >= radius)
				{
					iconPixs[y*bit.getWidth() + x] = Color.TRANSPARENT;
				}
			}
		}
		
		bit.setPixels(iconPixs, 0,bit.getWidth(), 0, 0, bit.getWidth(), bit.getHeight());
		
		if(useColor)
		{
			p.setColor(argb);
		}
		else p.setColor(Color.BLACK);
		
		p.setStrokeWidth(strokeW);
		p.setStyle(Style.STROKE);
		c.drawCircle(bit.getWidth()/2, bit.getHeight()/2, radius-strokeW/2, p);
	}
	
	public static void drawImageAtCenter(Bitmap work, Bitmap bit, float aspect) 
	{
		Canvas cwork = new Canvas(work);
		if(aspect < 1.0f)
		{
			cwork.drawBitmap
			(
				bit, 
				new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
				new Rect(work.getWidth()/2 - work.getHeight()/2, 0, work.getWidth()/2 + work.getHeight()/2, work.getHeight()), 
				null
			);
		}
		else
		{
			cwork.drawBitmap
			(
				bit, 
				new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
				new Rect(0, work.getHeight()/2 - work.getWidth()/2, work.getWidth(), work.getHeight()/2 + work.getWidth()/2), 
				null
			);
		}
	}


	// TODO create one fixed function
	public static void drawTextToRect(String fullText, float addressTextSize, float  addressTextStart, Bitmap work, Canvas cwork, Paint pwork, float[] textOffset, float textOffsetStep)
	{
		String currentText      = "";
		String words[]          = fullText.split(" ");
		Rect   bounds           = new Rect();
		//
		pwork.setTextSize(addressTextSize);
		pwork.setTextSkewX(0.0f);
		//
		for (int i = 0; i < words.length;)
		{
			int start_i   = i;
			int end_i     = start_i;

			currentText = "";
			do
			{
				currentText += words[end_i] + " ";
				pwork.getTextBounds(currentText, 0, currentText.length(), bounds);
				if(bounds.width() < work.getWidth() - addressTextStart*2.0f) end_i++;
				else break;
			}
			while(end_i < words.length);

			Log.d("elazarkin1", "currentText=" + currentText);

			currentText   = "";
			for(int j = start_i; j < end_i; j++)
			{
				currentText += words[j] + " ";
			}

			pwork.getTextBounds(currentText, 0, currentText.length(), bounds);
			textOffset[0] += textOffsetStep;
			cwork.drawText(currentText, addressTextStart, textOffset[0], pwork);

			i = end_i;
		}
	}

	public static void drawTextToRect(String fullText, float addressTextSize, Bitmap work, Canvas cwork, Paint pwork, float[] textOffset)
	{
		String currentText      = "";
		String words[]          = fullText.split(" ");
		Rect   bounds           = new Rect();
		float  addressTextStart = work.getWidth()*0.1f;
		float  textStep         = addressTextSize/4.0f;
		//
		pwork.setTextSize(addressTextSize);
		pwork.setTextSkewX(0.0f);
		//
		for (int i = 0; i < words.length;) 
		{
			int start_i   = i;
			int end_i     = start_i;
			do
			{
				currentText += words[end_i] + " ";
				pwork.getTextBounds(currentText, 0, currentText.length(), bounds);
				if(bounds.width() < work.getWidth() - addressTextStart*2.0f) end_i++;
				else break;
			}
			while(end_i < words.length);
			
			Log.d("elazarkin1", "currentText=" + currentText);
			
			currentText   = "";
			for(int j = start_i; j < end_i; j++)
			{
				currentText += words[j] + " ";
			}
			
			pwork.getTextBounds(currentText, 0, currentText.length(), bounds);
			textOffset[0] += bounds.height() + textStep;
			cwork.drawText(currentText, addressTextStart, textOffset[0], pwork);
			
			i = end_i;
		}
	}
	
	public static Bitmap createBitmap(float aspect)
	{
		return Bitmap.createBitmap((int)(FRAME_PIXEL_W), (int) (FRAME_PIXEL_W*aspect), Config.ARGB_8888);
	}
}
