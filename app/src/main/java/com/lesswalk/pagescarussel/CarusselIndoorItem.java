package com.lesswalk.pagescarussel;

import java.io.File;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class CarusselIndoorItem extends CarusselPageInterface 
{
	private static final String  INDOOR_NAME  = "indoor";
	
	private static ImageObject3D INDOOR_ICON  = null;
	
	
	public CarusselIndoorItem(Context context) 
	{
		super(INDOOR_NAME, context);
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		
	}

	@Override
	protected ImageObject3D getIcon() 
	{
        if(INDOOR_ICON == null)
        {
        	Bitmap  icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.form_indoor_icon_2x).copy(Bitmap.Config.ARGB_8888, true);
        	ImageObject3D.fixIconByColor(icon, Color.argb(255, 255, 176, 16));
        	//
        	INDOOR_ICON = new ImageObject3D("INDOOR_ICON");
        	INDOOR_ICON.generateTextureID(icon);
            //
            icon.recycle();
        }
    	
        return INDOOR_ICON;
	}

	@Override
	protected String getPageTitle() 
	{
		return null;
	}

	public void initIndoorItem(File objectsDir, String key, String value) 
	{
		// TODO Auto-generated method stub
	}

}
