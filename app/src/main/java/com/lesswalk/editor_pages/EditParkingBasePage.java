package com.lesswalk.editor_pages;

import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.ParkingPageParametersBase;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.editor_pages.bases.EditAddressMapThumnail;
import com.lesswalk.editor_pages.bases.EditObjects2dManager;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectAddressCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectPhotoTipCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectTextTipCallback;
import com.lesswalk.editor_pages.objects3D.AddressObject3D;
import com.lesswalk.editor_pages.objects3D.EditorImageTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorTextTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorVideoTipObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Toast;

public abstract class EditParkingBasePage extends ParkingPageParametersBase
{
	private RectObject3D    addressArea        = null;
	private RectObject3D    tipsArea           = null;
	private RectObject3D    mapThumbnail       = null;
	private AddressObject3D addressText        = null;
	
	private EditorTextTipObject3D  textTip     = null;
	private EditorImageTipObject3D imageTip    = null;
	private EditorVideoTipObject3D videoTip    = null;
	
	public EditParkingBasePage(String title, Context context) 
	{
		super(title, context);
	}
	
	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
		
		RectF  mapThumbnailArea = new RectF(-0.5f, 0.1f, 0.5f, 0.4f);
		RectF  addressTextArea  = new RectF(-0.5f, -0.5f, 0.5f, 0.0f);
		
		addressArea = getAddressRectArea(drawableArea);
		
		if(mapThumbnail == null)
		{
			mapThumbnail = createAdressMapThumnail(addressArea.aspect()*mapThumbnailArea.height()/mapThumbnailArea.width());
			mapThumbnail.initObject
			(
				addressArea, 
				mapThumbnailArea.centerX(), 
				mapThumbnailArea.centerY(), 
				mapThumbnailArea.width(), 
				addressArea.aspect()*mapThumbnailArea.height()/mapThumbnailArea.width(), 
				1.0f
			);
			mapThumbnail.setOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					Toast.makeText(getContext(), "TODO will open googlemap!", Toast.LENGTH_SHORT).show();
				}
			});
			addressArea.addChild(mapThumbnail);
		}
		
		if(addressText == null)
		{
			addressText = new AddressObject3D();
			
			addressText.initObject
			(
				addressArea, 
				addressTextArea.centerX(), 
				addressTextArea.centerY(), 
				addressTextArea.width(), 
				addressArea.aspect()*addressTextArea.height()/addressTextArea.width(), 
				1.0f
			);
			
			addressText.setOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					((EditObjects2dManager) getContext()).getManualAddressText(new EditObjectAddressCallback()
					{
						@Override
						public void onReturn(String contry, String city, String street, String street_num) 
						{
							addressText.setAddress(contry, city, street, street_num);
						}
						
					}, addressText.getCountry(), addressText.getCity(), addressText.getStreet(), addressText.getStreet_num());
				}
			});
			
			addressArea.addChild(addressText);
		}
		
		tipsArea = getTipsArea(drawableArea);
		
		if(textTip == null)
		{
			RectF textTipRect = new RectF(-0.5f, -0.5f, -0.2f, 0.1f);
			
			textTip = new EditorTextTipObject3D(getContext());
			
			textTip.initObject
			(
				tipsArea, 
				textTipRect.centerX(), 
				textTipRect.centerY(), 
				textTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			textTip.setOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					((EditObjects2dManager) getContext()).getTipText(new EditObjectTextTipCallback()
					{
						@Override
						public void onReturn(String text) 
						{
							textTip.setTipText(text);
						}
						
					}, textTip.getTipText());
				}
			});
			
			tipsArea.addChild(textTip);
		}
		
		if(imageTip == null)
		{
			RectF imageTipRect = new RectF(-0.15f, -0.5f, 0.15f, 0.1f);
			
			imageTip = new EditorImageTipObject3D(getContext());
			
			imageTip.initObject
			(
				tipsArea, 
				imageTipRect.centerX(), 
				imageTipRect.centerY(), 
				imageTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			imageTip.setOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					((EditObjects2dManager) getContext()).getTipPhoto(new EditObjectPhotoTipCallback() 
					{
						@Override
						public void onReturn(Bitmap photo) 
						{
							imageTip.setImageTip(photo);
						}
					});
				}
			});
			
			tipsArea.addChild(imageTip);
		}
		
		if(videoTip == null)
		{
			RectF videoTipRect = new RectF( 0.2f, -0.5f, 0.5f, 0.1f);
			
			videoTip = new EditorVideoTipObject3D(getContext());
			
			videoTip.initObject
			(
				tipsArea, 
				videoTipRect.centerX(), 
				videoTipRect.centerY(), 
				videoTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			videoTip.setOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					Toast.makeText(getContext(), "videoTip", Toast.LENGTH_SHORT).show();
				}
			});
			
			tipsArea.addChild(videoTip);
		}
	}
	
	private RectObject3D createAdressMapThumnail(float aspect) 
	{
		EditAddressMapThumnail ans = new EditAddressMapThumnail();
		Bitmap work       = ImageObject3D.createBitmap(aspect);
		Bitmap original   = BitmapFactory.decodeResource(getContext().getResources(), getEmptyMapThumnailResourceId()).copy(Bitmap.Config.ARGB_8888, true);
		Bitmap smalled    = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas csmalled   = new Canvas(smalled);
		float  scale      = 0.8f;
		
		csmalled.drawBitmap
		(
			original,
			new Rect(0, 0, original.getWidth(), original.getHeight()), 
			new Rect
			(
				(int)(smalled.getWidth()*(1.0f-scale)), 
				(int)(smalled.getHeight()*(1.0f-scale)), 
				(int)(smalled.getWidth()*scale), 
				(int)(smalled.getHeight()*scale)
			), 
			null
		);
		
		ImageObject3D.CircleCut(smalled, Color.argb(255, 255, 176, 16));
		ImageObject3D.drawImageAtCenter(work, smalled, aspect);
		//ImageObject3D.fixIconByColor(startImage, Color.argb(255, 255, 176, 16));
		
		ans.generateTextureID(work);

		original.recycle();
		smalled.recycle();
		work.recycle();
		return ans;
	}
	
	@Override
	protected String getYouShouldNoticeTitle() 
	{
		return "Got Any Tips?";
	}

	protected abstract int getEmptyMapThumnailResourceId();
}
