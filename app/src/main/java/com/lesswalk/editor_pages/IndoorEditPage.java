package com.lesswalk.editor_pages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.json.CarruselJson;
import com.lesswalk.pagescarussel.CarusselPageInterface;

import java.io.File;

// TODO create IndoorBasePage!

public class IndoorEditPage extends CarusselPageInterface
{
	private final int STATE_HELP_INIT    = 0;
	private final int STATE_EDIT_PROCCES = 1;
	
	private ImageObject3D INDOOR_ICON  = null;
	private ImageObject3D INDOOR_TITLE = null;

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

	@Override
	protected RectObject3D createTitle(float aspect)
	{
		if(INDOOR_TITLE == null)
		{
			INDOOR_TITLE = createTitleObj(getPageTitle(), aspect);
		}

		return INDOOR_TITLE;
	}

	@Override
	protected String getYouShouldNoticeTitle()
	{
		return "You Should Notice";
	}

	@Override
	public void save(File dir, CarruselJson carruselJson)
	{

	}

}
