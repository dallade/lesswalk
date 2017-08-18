package com.lesswalk.editor_pages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.ParkingPageParametersBase;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.editor_pages.bases.EditAddressMapThumnail;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectAddressCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectPhotoTipCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectTextTipCallback;
import com.lesswalk.editor_pages.bases.EditObjects2dManager;
import com.lesswalk.editor_pages.objects3D.AddressObject3D;
import com.lesswalk.editor_pages.objects3D.EditorImageTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorTextTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorVideoTipObject3D;
import com.lesswalk.maps.MapData;

public abstract class EditParkingBasePage extends ParkingPageParametersBase
{
    private static final String                 TAG            = EditParkingBasePage.class.getSimpleName();
    private              RectObject3D           addressArea    = null;
    private              RectObject3D           tipsArea       = null;
    private              RectObject3D           mapThumbnail   = null;
    private              AddressObject3D        addressObject  = null;
    private              EditorTextTipObject3D  textTipObject  = null;
    private              EditorImageTipObject3D imageTipObject = null;
    private              EditorVideoTipObject3D videoTipObject = null;
    private MapData                          mMapData;
    private EditManagerCallbacks.MapListener mMapListener;

    public EditParkingBasePage(String title, Context context)
	{
		super(title, context);
	}

	protected AddressObject3D getAddressObject()
	{
		return addressObject;
	}

	protected EditorTextTipObject3D getTextTipObject()
	{
		return textTipObject;
	}

	protected EditorImageTipObject3D getImageTipObject()
	{
		return imageTipObject;
	}

	protected EditorVideoTipObject3D getVideoTipObject()
	{
		return videoTipObject;
	}
	
	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
		
		RectF  mapThumbnailArea = new RectF(-0.5f, 0.1f, 0.5f, 0.4f);
		RectF  addressTextArea  = new RectF(-0.5f, -0.5f, 0.5f, 0.0f);
//
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
					//TODO elad
                    mMapData = new MapData();
					if (addressObject!=null && addressObject.toString().length()>4){
						mMapData.inAddress = addressObject.toString();
					}
                    mMapListener = new EditManagerCallbacks.MapListener(){

                        @Override
                        public void onResult(MapData mapData) {
                            mMapData = mapData;
                            final String text = String.format("pos: %s", mMapData.latLng.toString());
                            Log.d(TAG, text);
                            Toast.makeText(getContext().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public MapData getMapData() {
                            return mMapData;
                        }
                    };
                    ((EditObjects2dManager) getContext()).openMapForResult(mMapListener);
                }
			});
			addressArea.addChild(mapThumbnail);
		}
//
		if(addressObject == null)
		{
			addressObject = new AddressObject3D();

			addressObject.initObject
			(
				addressArea,
				addressTextArea.centerX(),
				addressTextArea.centerY(),
				addressTextArea.width(),
				addressArea.aspect()*addressTextArea.height()/addressTextArea.width(),
				1.0f
			);

			addressObject.setOnClickCallback(new OnClickedAction()
			{
				@Override
				public void onClicked()
				{
					// TODO // FIXME: 04/07/17 use MapAddress Object
					((EditObjects2dManager) getContext()).getManualAddressText(new EditObjectAddressCallback()
					{
						@Override
						public void onReturn(String country, String city, String street, String street_num)
						{
							addressObject.setAddress(country, city, street, street_num);
						}

					}, addressObject.getCountry(), addressObject.getCity(), addressObject.getStreet(), "0");
				}
			});

			addressArea.addChild(addressObject);
		}

		tipsArea = getTipsArea(drawableArea);
		
		if(textTipObject == null)
		{
			RectF textTipRect = new RectF(-0.5f, -0.5f, -0.2f, 0.1f);
			
			textTipObject = new EditorTextTipObject3D(getContext());
			
			textTipObject.initObject
			(
				tipsArea, 
				textTipRect.centerX(), 
				textTipRect.centerY(), 
				textTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			textTipObject.setOnClickCallback(new OnClickedAction()
			{
				@Override
				public void onClicked() 
				{
					((EditObjects2dManager) getContext()).getTipText(new EditObjectTextTipCallback()
					{
						@Override
						public void onReturn(String text) 
						{
							textTipObject.setTipText(text);
						}
						
					}, textTipObject.getTipText());
				}
			});
			
			tipsArea.addChild(textTipObject);
		}
		
		if(imageTipObject == null)
		{
			RectF imageTipRect = new RectF(-0.15f, -0.5f, 0.15f, 0.1f);
			
			imageTipObject = new EditorImageTipObject3D(getContext());
			
			imageTipObject.initObject
			(
				tipsArea, 
				imageTipRect.centerX(), 
				imageTipRect.centerY(), 
				imageTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			imageTipObject.setOnClickCallback(new OnClickedAction()
			{
				@Override
				public void onClicked() 
				{
					((EditObjects2dManager) getContext()).getTipPhoto(new EditObjectPhotoTipCallback() 
					{
						@Override
						public void onReturn(Bitmap photo) 
						{
							imageTipObject.setImageTip(photo);
						}
					});
				}
			});
			
			tipsArea.addChild(imageTipObject);
		}
		
		if(videoTipObject == null)
		{
			RectF videoTipRect = new RectF( 0.2f, -0.5f, 0.5f, 0.1f);
			
			videoTipObject = new EditorVideoTipObject3D(getContext());
			
			videoTipObject.initObject
			(
				tipsArea, 
				videoTipRect.centerX(), 
				videoTipRect.centerY(), 
				videoTipRect.width(), 
				aspect(), 
				1.0f
			);
			
			videoTipObject.setOnClickCallback(new OnClickedAction()
			{
				@Override
				public void onClicked() 
				{
					Toast.makeText(getContext(), "videoTipObject", Toast.LENGTH_SHORT).show();
				}
			});
			
			tipsArea.addChild(videoTipObject);
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
