package com.lesswalk.player_pages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.pagescarussel.CarusselPageInterface;

import java.io.File;

public class CarusselPlayerIndoorItem extends CarusselPageInterface 
{
	private static final String  INDOOR_NAME  = "indoor";

	private ImageObject3D INDOOR_ICON  = null;
	private ImageObject3D INDOOR_TITLE = null;
	private String        text_tip     = null;

	public CarusselPlayerIndoorItem(Context context) 
	{
		super(INDOOR_NAME, context);
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		RectObject3D tipsObj      = getTipsArea(drawableArea);
		RectObject3D textTipObj   = null;
		RectF        textTipsRect = new RectF(-0.5f, -0.5f, 0.5f, 0.5f);

		textTipObj = createTextTip(tipsObj.aspect()*textTipsRect.height()/textTipsRect.width(), text_tip);
		textTipObj.initObject
		(
				tipsObj,
				textTipsRect.centerX(),
				textTipsRect.centerY(),
				textTipsRect.width(),
				tipsObj.aspect()*textTipsRect.height()/textTipsRect.width(),
				USE_WEIGHT ? 1.0f:1.0f
		);
		tipsObj.addChild(textTipObj);

		drawableArea.addChild(getTitleObj(drawableArea));

		drawableArea.addChild(getYouShouldNoticeTitleObj(drawableArea));

		drawableArea.addChild(tipsObj);
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
		return "Indoor";
	}

	public void initIndoorItem(File objectsDir, String key, String value) 
	{
		// TODO Auto-generated method stub

		if(key.equals("tips"))
		{
			text_tip = value;
		}
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

}
