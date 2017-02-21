package com.lesswalk.editor_pages;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.pagescarussel.CarusselPageInterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class IndoorEditPage extends CarusselPageInterface 
{
	private static final int STATE_HELP_INIT    = 0;
	private static final int STATE_EDIT_PROCCES = 1;
	
	private static ImageObject3D INDOOR_ICON  = null;
	
	public IndoorEditPage(String title, Context context) 
	{
		super(title, context);
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		return "Indoor Edit";
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) {
		// TODO Auto-generated method stub
		
	}

}
