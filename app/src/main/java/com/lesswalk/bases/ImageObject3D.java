package com.lesswalk.bases;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class ImageObject3D extends RectObject3D 
{
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
}
