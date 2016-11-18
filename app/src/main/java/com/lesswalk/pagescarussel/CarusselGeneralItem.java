package com.lesswalk.pagescarussel;

import java.io.File;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * Created by root on 3/22/16.
 */
public class CarusselGeneralItem extends ParkingDesignPage
{
	private static final String  GENERAL_NAME  = "General";
	private static ImageObject3D GENERAL_ICON  = null;
	private static ImageObject3D GENERAL_TITLE = null;
	//
    private class IndoorAddress
    {
    	String entrance = null;
    	String floor    = null;
    	
    	@Override
    	public String toString() 
    	{
    		String text = "";
    		
    		if(floor != null)
    		{
    			text += "Floor " + floor;
    			if(entrance != null) text += ", ";
    		}
    		
    		if(entrance != null)
    		{
    			text += "Ent. " + entrance;
    		}
    		return text;
    	}
    }
    
    private IndoorAddress indoorAddress = null;
    
    public CarusselGeneralItem(Context context)
    {
        super(GENERAL_NAME, context);
    }
    
    @Override
    protected ImageObject3D getIcon()
    {
    	// TODO check if it not mistake to use static Icon
        if(GENERAL_ICON == null)
        {
        	Bitmap  icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.form_address_icon_2x).copy(Bitmap.Config.ARGB_8888, true);
        	ImageObject3D.fixIconByColor(icon, Color.argb(255, 255, 176, 16));
        	//
            GENERAL_ICON = new ImageObject3D("Icon");
            GENERAL_ICON.generateTextureID(icon);
            //
            icon.recycle();
        }
    	
        return GENERAL_ICON; 
    }

	protected RectObject3D createTitle(float aspect) 
	{
		if(GENERAL_TITLE == null)
		{
			Bitmap titleBitmap = createTitleBitmap(getPageTitle(), aspect);
			//
			GENERAL_TITLE  = new ImageObject3D("title");
			GENERAL_TITLE.generateTextureID(titleBitmap);
			titleBitmap.recycle();
		}
		
		return GENERAL_TITLE;
	}
	
    @Override
    protected String getPageTitle()
    {
        return "Address";
    }

	public void initIndoorAddressItem(File objectsDir, String key, String value) 
	{
		if(indoorAddress == null) indoorAddress = new IndoorAddress();
		//
		if(key.equals("entrance")) indoorAddress.entrance = value;
		else if(key.equals("floor")) indoorAddress.floor = value;
	}
	
	@Override
	public void destroy() 
	{
		if(GENERAL_ICON != null)
		{
			GENERAL_ICON.destroy();
			GENERAL_ICON = null;
		}
		if(GENERAL_TITLE != null)
		{
			GENERAL_TITLE.destroy();
			GENERAL_TITLE = null;
		}
		super.destroy();
	}

	@Override
	protected RectObject3D getArrivalTimeObj(float aspect) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
