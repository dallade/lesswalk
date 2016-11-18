package com.lesswalk.pagescarussel;

import java.io.File;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by root on 3/22/16.
 */
public class CarusselParkingItem extends ParkingDesignPage
{
    private int ID = -1;

    private static ImageObject3D PARKING_ICON  = null;
    private static ImageObject3D PARKING_TITLE = null;
    
    public CarusselParkingItem(Context context, int _ID)
    {
        super("Parking", context);
        
        ID = _ID;
    }

    public void initStreetAddressItem(File objectsDir, String key, String value)
    {
        {
            Log.d("elazarkin", "CarusselParkingItem_" + ID + " initStreetAddressItem " + key + ":" + value);
        }
    }

    @Override
    protected String getPageTitle()
    {
        return "Parking";
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
	protected RectObject3D createTitle(float aspect) 
	{
		if(PARKING_TITLE == null)
		{
			PARKING_TITLE = createTitleObj(getPageTitle(), aspect);
		}
		
		return PARKING_TITLE;
	}
	
	@Override
	protected RectObject3D getArrivalTimeObj(float acpect) 
	{
		DriveTakeText3D ret = new DriveTakeText3D(acpect);
		ret.setDriveWalkText(-1, -1, -1, -1);
		return ret;
	}
	
	@Override
	public void destroy() 
	{
		if(PARKING_ICON != null)
		{
			PARKING_ICON.destroy();
			PARKING_ICON = null;
		}
		if(PARKING_TITLE != null)
		{
			PARKING_TITLE.destroy();
			PARKING_TITLE = null;
		}
		
		super.destroy();
	}
}
