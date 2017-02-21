package com.lesswalk.editor_pages;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class ParkingEditPage extends EditParkingBasePage
{
    private static ImageObject3D PARKING_ICON  = null;
    private static ImageObject3D PARKING_TITLE = null;

	public ParkingEditPage(String title, Context context) 
	{
		super(title, context);
	}

	@Override
	protected ImageObject3D getIcon() 
	{
    	// TODO check if it not mistake to use static Icon
        if(PARKING_ICON == null)
        {
        	Bitmap  icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.parking_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
        	ImageObject3D.fixIconByColor(icon, Color.argb(255, 255, 176, 16));
        	//
            PARKING_ICON = new ImageObject3D("PARKING_ICON");
            PARKING_ICON.generateTextureID(icon);
            //
            icon.recycle();
        }
    	
        return PARKING_ICON;
	}

	@Override
	protected String getPageTitle() 
	{
		return "Set Parking Splot";
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
	}
	
	@Override
	protected RectObject3D createTitle(float aspect) 
	{
		if(PARKING_TITLE == null)
		{
			PARKING_TITLE = createTitleObj(getPageTitle(), aspect);
		}
		
		return PARKING_TITLE;
	}

	@Override
	protected int getEmptyMapThumnailResourceId() 
	{
		return R.drawable.parking_address_edit_card_icon_1x;
	}

}
