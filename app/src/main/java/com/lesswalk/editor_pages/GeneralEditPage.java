package com.lesswalk.editor_pages;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class GeneralEditPage extends EditParkingBasePage 
{
	private ImageObject3D GENERAL_ICON  = null;
	private ImageObject3D GENERAL_TITLE = null;
	
	public GeneralEditPage(String title, Context context) 
	{
		super(title, context);
	}

	@Override
	protected ImageObject3D getIcon() 
	{
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

	@Override
	protected String getPageTitle() 
	{
		return "Set your Address";
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
	}

	@Override
	protected RectObject3D createTitle(float aspect) 
	{
		if(GENERAL_TITLE == null)
		{
			GENERAL_TITLE = createTitleObj(getPageTitle(), aspect);
		}
		
		return GENERAL_TITLE;
	}

	@Override
	protected int getEmptyMapThumnailResourceId() 
	{
		return R.drawable.address_edit_card_icon;
	}
}
