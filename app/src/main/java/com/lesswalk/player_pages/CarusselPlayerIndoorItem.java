package com.lesswalk.player_pages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.pagescarussel.CarusselPageInterface;

import java.io.File;

public class CarusselPlayerIndoorItem extends CarusselPageInterface 
{
	private static final String  INDOOR_NAME  = "indoor";

	private class IndoorAddress
	{
		String entrance  = null;
		String floor     = null;
		String apartment = null;

		@Override
		public String toString()
		{
			String text = "";

			if(floor != null)
			{
				text += "Floor " + floor;
			}

			if(entrance != null)
			{
				text += (text.length() > 0 ? " , ": "") + "Entrance " + entrance;
			}

			if(apartment != null)
			{
				text += (text.length() > 0 ? " , ": "") + "Apartment " + apartment;
			}

			return text;
		}
	}

	private ImageObject3D INDOOR_ICON  = null;
	private ImageObject3D INDOOR_TITLE = null;
	private String        text_tip     = null;

	private IndoorAddress indoorAddress = null;

	public void initIndoorAddressItem(File objectsDir, String key, String value)
	{
		if(key.equals("entrance")) indoorAddress.entrance = "" + value;
		else if(key.equals("floor")) indoorAddress.floor = "" + value;
		else if(key.equals("apt")) indoorAddress.apartment = "" + value;
	}

	public CarusselPlayerIndoorItem(Context context) 
	{
		super(INDOOR_NAME, context);

		indoorAddress = new IndoorAddress();
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		RectObject3D addressArea  = getAddressRectArea(drawableArea);
		RectObject3D tipsObj      = getTipsArea(drawableArea);
		RectObject3D textTipObj   = null;
		RectF        textTipsRect = new RectF(-0.5f, -0.5f, 0.25f, 0.5f);

		addIndoorAddress(addressArea, drawableArea.aspect(), indoorAddress.entrance, indoorAddress.floor, indoorAddress.apartment);

		drawableArea.addChild(addressArea);

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

	private void addIndoorAddress(RectObject3D addressArea, float parentAspect, String entrance, String floor, String apartment)
	{
		RectF entArea   = new RectF(-0.5f,  0.1f, 0.0f, 0.4f);
		RectF floorArea = new RectF(-0.5f, -0.4f, -0.1f, -0.1f);
		RectF apartArea = new RectF(-0.1f, -0.4f, 0.5f, -0.1f);

		_addIndoorAddress(addressArea, parentAspect, entArea, entrance, "Entrance");
		_addIndoorAddress(addressArea, parentAspect, floorArea, floor, "Floor");
		_addIndoorAddress(addressArea, parentAspect, apartArea, apartment, "Apartment");
	}

	private void _addIndoorAddress(RectObject3D addressArea, float parentAspect, RectF area, String _text, String title)
	{
		Bitmap       work           = ImageObject3D.createBitmap(addressArea.aspect() * area.height() / area.width());
		Canvas       cwork          = new Canvas(work);
		Paint        pwork          = new Paint();
		Bitmap       num            = Bitmap.createBitmap(work.getHeight(), work.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas       cnum           = new Canvas(num);
		Paint        pnum           = new Paint();
		int          textSize       = 8;
		int          TEXT_SIZE_STEP = 2;
		Rect         textBound      = new Rect();
		int          circleColor    = (_text == null ? Color.LTGRAY : Color.DKGRAY);
		String       text           = (_text == null ? "?" : _text);
		RectObject3D obj            = new RectObject3D("indoor_address_" + title);

		do
		{
			textSize += TEXT_SIZE_STEP;
			pnum.setTextSize(textSize);
			pnum.getTextBounds(text, 0, text.length(), textBound);
		}
		while (textBound.height() < num.getHeight()*0.6f && textBound.width() < num.getWidth()*0.6f);

		Log.d("elazarkin8", "indoor_address: textSize = " + textSize + " text = " + text);

		pnum.setColor(circleColor);
		pnum.setStyle(Paint.Style.FILL);
		cnum.drawCircle(num.getWidth()/2, num.getHeight()/2, num.getWidth()/2, pnum);
		pnum.setColor(Color.WHITE);
		pnum.setTextSize(textSize);
		//cnum.drawText(text, num.getWidth()/2 - textSize/2, num.getHeight()/2 - textSize/2, pnum);
		if(text.equals("1"))
		{
			cnum.drawText(text, num.getWidth() / 2 - textBound.width(), num.getHeight() / 2 + textBound.height() / 2, pnum);
		}
		else
		{
			cnum.drawText(text, num.getWidth() / 2 - textBound.width() / 2, num.getHeight() / 2 + textBound.height() / 2, pnum);
		}

		cwork.drawBitmap(num, 0, 0, null);

		pwork.setTextSize(textSize*0.5f);
		pwork.setColor(circleColor);
		pwork.getTextBounds(title, 0, title.length(), textBound);

		cwork.drawText(title, num.getWidth() + 10, work.getHeight()/2 + textBound.height()/2, pwork);

		obj.initObject(addressArea, area.centerX(), area.centerY(), area.width(), addressArea.aspect() * area.height() / area.width(), USE_WEIGHT ? 1.0f:1.0f);
		obj.generateTextureID(work);

		addressArea.addChild(obj);

		num.recycle();
		work.recycle();
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
