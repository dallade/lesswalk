package com.lesswalk.bases;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.HandlerThread;

public class FlickerImageObject3D extends ImageObject3D 
{
	private boolean isAlive = false;

	public FlickerImageObject3D(String name) 
	{
		super(name);
		
		new HandlerThread("")
		{
			public void run()
			{
				float current_alpha = 1.0f;
				float alpha_step    = 0.2f;
				float curr_step     = alpha_step;
				long  sleep_time    = 100;
				//
				isAlive = true;
				//
				while(isAlive)
				{
					curr_step = current_alpha <= 0.3f ? alpha_step : current_alpha >= 1.0f ? -alpha_step : curr_step;
					current_alpha += curr_step;
					//
					updateObjectAlpha(current_alpha);
					try {sleep(sleep_time);} catch (Exception e) {}
				}
			}
		}.start();
	}

	public static void fixIconByColor(Bitmap icon, int argb) 
    {
        int iconPixs[] = new int[icon.getWidth() * icon.getWidth()];
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

	public void stop()
	{
		isAlive = false;
	}
}
