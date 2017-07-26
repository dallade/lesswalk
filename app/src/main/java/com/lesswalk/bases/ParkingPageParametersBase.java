package com.lesswalk.bases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.lesswalk.pagescarussel.CarusselPageInterface;

public abstract class ParkingPageParametersBase extends CarusselPageInterface 
{
	// TODO move and recalculate this into addressRect!
	private RectF mapAddrRect      = null;
	private RectF goButtonRect     = null;
	private RectF mapThumbnailRect = null;
	private RectF arrivalTimeRect  = null;
	// end todo
	
	// TODO remove it and add info into tips area right way
	private RectF textTipsRect    = null;
	private RectF infoHelpViews[] = null;
	// end todo

	// TODO protected2private
	protected RectObject3D streetViewImage  = null;
	protected RectObject3D street_view_back = null;
	protected RectObject3D audioButton      = null;
	protected RectObject3D videoButton      = null;
	protected RectObject3D textTipObj       = null;

	// end todo

	public ParkingPageParametersBase(String title, Context context)
	{
		super(title, context);

		initRects();
	}

	@Override
	protected void addChilds(RectObject3D drawableArea)
	{
		drawableArea.addChild(getAddressRectArea(drawableArea));

		drawableArea.addChild(getTitleObj(drawableArea));

		drawableArea.addChild(getYouShouldNoticeTitleObj(drawableArea));

		drawableArea.addChild(getTipsArea(drawableArea));
	}

	private void initRects()
	{
	    // TODO delete this after changes
	    float mapAddr_h           = 0.0f;
	    float noticeIconHeight    = 0.0f;
	    // end todo

		// TODO create function that create internal rects into exitst rect

	    mapAddr_h           = 0.26f;
	    mapAddrRect         = new RectF(-0.34f, 0.5f - getTitleRect().height() - mapAddr_h, 0.26f, 0.5f - getTitleRect().height());
	    goButtonRect        = new RectF(0.26f, 0.5f - getTitleRect().height() - mapAddr_h/2, 0.5f, 0.5f - getTitleRect().height());
	    mapThumbnailRect    = new RectF(-0.5f, 0.5f - getTitleRect().height() - mapAddr_h/2, -0.34f, 0.5f - getTitleRect().height());
	    arrivalTimeRect     = new RectF(-0.5f, 0.0f, 0.5f, 0.1f);
	    
	    noticeIconHeight    = Math.abs(-0.5f - getYouShouldNoticeRect().top)/2.0f;
	    
	    infoHelpViews = new RectF[]
    	{
			new RectF
		    ( 
				0.5f - noticeIconHeight,
				getYouShouldNoticeRect().top - noticeIconHeight,
				0.5f,
				getYouShouldNoticeRect().top
			),
		    new RectF
		    ( 
				0.5f - noticeIconHeight,
				getYouShouldNoticeRect().top - 2.0f*noticeIconHeight,
				0.5f,
				getYouShouldNoticeRect().top - noticeIconHeight
			)
    	};

		textTipsRect = new RectF
		(
			-0.5f,
			getYouShouldNoticeRect().top - 2.0f*noticeIconHeight,
			infoHelpViews[0].left - 0.05f,
			getYouShouldNoticeRect().top
		);
	}
	
	// TODO move it to player ParkingDesignPage
	protected RectF mapAddrRect() {return mapAddrRect;}
	protected RectF goButtonRect() {return goButtonRect;}
	protected RectF mapThumbnailRect() {return mapThumbnailRect;}
	protected RectF arrivalTimeRect() {return arrivalTimeRect;}
	protected RectF infoHelpViews(int index) {return infoHelpViews[index];}
	protected RectF textTipsRect() {return textTipsRect;}
	// end todo
}
