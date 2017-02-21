package com.lesswalk.bases;

import com.lesswalk.pagescarussel.CarusselPageInterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

public abstract class ParkingPageParametersBase extends CarusselPageInterface 
{
	private RectF titleRect                  = null;
	private RectF addressRect                = null;
	private RectF youShouldNotRect           = null;
	private RectF tipsRect                   = null;
	
	// TODO move and recalculate this into addressRect!
	private RectF mapAddrRect                = null;
	private RectF goButtonRect               = null;
	private RectF mapThumbnailRect           = null;
	private RectF arrivalTimeRect            = null;
	// end todo
	
	// TODO remove it and add info into tips area right way
	private RectF infoHelpViews[]            = null;
	// end todo

	
	
	// TODO protected2private
	protected RectObject3D  streetViewImage  = null;
	protected RectObject3D  street_view_back = null;
	protected RectObject3D  audioButton      = null;
	protected RectObject3D  videoButton      = null;
	// end todo

	private RectObject3D  title              = null;
	private RectObject3D  addressRectArea    = null;
	private RectObject3D  youShouldNotice    = null;
	private RectObject3D  tipsArea           = null;
	
	public ParkingPageParametersBase(String title, Context context) 
	{
		super(title, context);
		
		initRects();
	}
	
	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		title = createTitle(drawableArea.aspect()*titleRect.height()/titleRect.width());
		title.initObject
		(
			drawableArea, 
			titleRect.centerX(), 
			titleRect.centerY(), 
			titleRect.width(), 
			drawableArea.aspect()*titleRect.height()/titleRect.width(), 
			USE_WEIGHT ? 1.0f:1.0f
		);
		drawableArea.addChild(title);
		
		if((youShouldNotice = createTitleObj(getYouShouldNoticeTitle(), drawableArea.aspect()*youShouldNotRect.height()/youShouldNotRect.width())) != null)
		{
			youShouldNotice.initObject
			(
				drawableArea, 
				youShouldNotRect.centerX(), 
				youShouldNotRect.centerY(), 
				youShouldNotRect.width(), 
				drawableArea.aspect()*youShouldNotRect.height()/youShouldNotRect.width(), 
				USE_WEIGHT ? 1.0f:1.0f
			);
			drawableArea.addChild(youShouldNotice);
		}
	}

	protected abstract String getYouShouldNoticeTitle();

	private void initRects() 
	{
		float title_start         = 0.0f;
	    float title_h             = 0.0f;
	    float address_start       = 0.0f;
	    float address_h           = 0.0f;
	    float youShoudNoticeStart = 0.0f;
	    float youShoudNotice_h    = 0.0f;
	    float tips_start          = 0.0f;
	    float tips_h              = 0.0f;
	    
	    // TODO delete this after changes
	    float mapAddr_h           = 0.0f;
	    float noticeIconHeight    = 0.0f;
	    // end todo
	    
	    title_start         = 0.5f;
	    title_h             = 0.07f;
		titleRect           = new RectF(-0.5f, (title_start- title_h), 0.5f, title_start);
	    mapAddr_h           = 0.26f;
	    mapAddrRect         = new RectF(-0.34f, 0.5f - title_h - mapAddr_h, 0.26f, 0.5f - title_h);
	    goButtonRect        = new RectF(0.26f, 0.5f - title_h - mapAddr_h/2, 0.5f, 0.5f - title_h);
	    mapThumbnailRect    = new RectF(-0.5f, 0.5f - title_h - mapAddr_h/2, -0.34f, 0.5f - title_h);
	    arrivalTimeRect     = new RectF(-0.5f, 0.0f, 0.5f, 0.1f);
	    
	    address_start       = title_start - title_h;
	    address_h           = 0.6f;
	    addressRect         = new RectF(-0.5f, address_start - address_h, 0.5f, address_start);
	    
	    youShoudNoticeStart = address_start - address_h;
	    youShoudNotice_h    = title_h;
	    youShouldNotRect    = new RectF(-0.5f, youShoudNoticeStart - youShoudNotice_h, 0.5f, youShoudNoticeStart);
	    
	    tips_start          = youShoudNoticeStart - youShoudNotice_h;
	    tips_h              = tips_start - (-0.5f);
	    tipsRect            = new RectF(-0.5f, tips_start - tips_h, 0.5f, tips_start);
	    
	    noticeIconHeight    = Math.abs(-0.5f - youShouldNotRect.top)/2.0f;
	    
	    infoHelpViews = new RectF[]
    	{
			new RectF
		    ( 
				0.5f - noticeIconHeight, 
				youShouldNotRect.top - noticeIconHeight, 
				0.5f, 
				youShouldNotRect.top
			),
		    new RectF
		    ( 
				0.5f - noticeIconHeight, 
				youShouldNotRect.top - 2.0f*noticeIconHeight, 
				0.5f, 
				youShouldNotRect.top - noticeIconHeight
			)
    	};
	}
	
	protected ImageObject3D createTitleObj(String text, float aspect) 
	{
		ImageObject3D ret  = null;
		Bitmap titleBitmap = createTitleBitmap(text, aspect);
		//
		ret  = new ImageObject3D(text);
		ret.generateTextureID(titleBitmap);
		titleBitmap.recycle();
		
		return ret;
	}
	
	protected RectObject3D getAddressRectArea(RectObject3D drawableArea)
	{
		if(addressRectArea == null)
		{
			addressRectArea = new RectObject3D("addressRectArea");
			
			addressRectArea.initObject
			(
				drawableArea, 
				addressRect.centerX(), 
				addressRect.centerY(), 
				addressRect.width(), 
				drawableArea.aspect()*addressRect.height()/addressRect.width(), 
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			drawableArea.addChild(addressRectArea);
		}
		return addressRectArea;
	}
	
	protected RectObject3D getTipsArea(RectObject3D drawableArea) 
	{
		if(tipsArea == null)
		{
			tipsArea = new RectObject3D("tipsArea");
			
			tipsArea.initObject
			(
				drawableArea, 
				tipsRect.centerX(), 
				tipsRect.centerY(), 
				tipsRect.width(), 
				drawableArea.aspect()*tipsRect.height()/tipsRect.width(), 
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			drawableArea.addChild(tipsArea);
		}
		return tipsArea;
	}
	
	// TODO move it to player ParkingDesignPage
	protected RectF mapAddrRect() {return mapAddrRect;}
	protected RectF goButtonRect() {return goButtonRect;}
	protected RectF mapThumbnailRect() {return mapThumbnailRect;}
	protected RectF arrivalTimeRect() {return arrivalTimeRect;}
	protected RectF infoHelpViews(int index) {return infoHelpViews[index];}
	// end todo
	
	protected abstract RectObject3D createTitle(float aspect);
}
