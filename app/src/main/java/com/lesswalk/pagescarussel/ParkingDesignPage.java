package com.lesswalk.pagescarussel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import com.lesswalk.R;
import com.lesswalk.bases.FlickerImageObject3D;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.HandlerThread;
import android.util.Log;

public abstract class ParkingDesignPage extends CarusselPageInterface 
{
	private RectObject3D  title              = null;
	private RectObject3D  mapAddress         = null;
	private RectObject3D  goButton           = null;
	private RectObject3D  map_thumbnail_obj  = null;
	private RectObject3D  map_thumbnail_back = null;
	private RectObject3D  arrivalTime        = null;
	private RectObject3D  youShouldNotice    = null;
	private RectObject3D  streetViewImage    = null;
	private RectObject3D  street_view_back   = null;
	private RectObject3D  audioButton        = null;
	private RectObject3D  videoButton        = null;
	private VideoPlayer3D videoPlayer        = null;
	
	private File map_thumbnail = null;
	private File audio_path    = null;
	
	private AlertDialog openWazeDialog  = null;

	public ParkingDesignPage(String title, Context context) 
	{
		super(title, context);
	}
	
    public void initItem(File objectsDir, String key, String value)
    {
		if(key.equals("map_thumbnail"))
		{
			map_thumbnail = new File(objectsDir, value);
		}
		else if(key.equals("audio"))
		{
			audio_path = new File(objectsDir, value);
		}
    }

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
	    float title_h           = 0.07f;

		RectF titleRect = new RectF(-0.5f, (0.5f - title_h), 0.5f, 0.5f);
	    float mapAddr_h         = 0.26f;
	    RectF mapAddrRect       = new RectF(-0.34f, 0.5f - title_h - mapAddr_h, 0.26f, 0.5f - title_h);
	    RectF goButtonRect      = new RectF(0.26f, 0.5f - title_h - mapAddr_h/2, 0.5f, 0.5f - title_h);
	    RectF mapThumbnailRect  = new RectF(-0.5f, 0.5f - title_h - mapAddr_h/2, -0.34f, 0.5f - title_h);
	    RectF arrivalTimeRect   = new RectF(-0.5f, 0.0f, 0.5f, 0.1f);
	    RectF youShouldNotRect  = new RectF(-0.5f, -0.1f - title_h, 0.5f, -0.1f);
	    float noticeIconHeight  = Math.abs(-0.5f - youShouldNotRect.top)/2.0f;
	    
	    RectF infoHelpViews[] =
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
	    
	    int infoHelpViewsCounter = 0;
		//
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
		//
		mapAddress = createMapAddress(drawableArea.aspect()*mapAddrRect.height()/mapAddrRect.width());
		mapAddress.initObject
		(
			drawableArea, 
			mapAddrRect.centerX(), 
			mapAddrRect.centerY(), 
			mapAddrRect.width(), 
			drawableArea.aspect()*mapAddrRect.height()/mapAddrRect.width(), 
			USE_WEIGHT ? 1.0f:1.0f
		);
		drawableArea.addChild(mapAddress);
//		//
		goButton = createGoButton(drawableArea.aspect()*goButtonRect.height()/goButtonRect.width());
		goButton.initObject
		(
			drawableArea, 
			goButtonRect.centerX(), 
			goButtonRect.centerY(), 
			goButtonRect.width(), 
			drawableArea.aspect()*goButtonRect.height()/goButtonRect.width(),
			USE_WEIGHT ? 0.8f:1.0f
		);
		
		goButton.addOnClickCallback(new OnClickedAction() 
		{
			@Override
			public void onClicked() 
			{
				if(openWazeDialog == null)
				{
					createOpenWazeDialog();
				}
				openWazeDialog.show();
			}
		});
		
		drawableArea.addChild(goButton);
//		//
		if(map_thumbnail != null && (map_thumbnail_obj=createMapThumnail(drawableArea.aspect()*mapThumbnailRect.height()/mapThumbnailRect.width())) != null)
		{
			map_thumbnail_obj.initObject
			(
				drawableArea, 
				mapThumbnailRect.centerX(), 
				mapThumbnailRect.centerY(), 
				mapThumbnailRect.width(), 
				drawableArea.aspect()*mapThumbnailRect.height()/mapThumbnailRect.width(),
				USE_WEIGHT ? 0.8f:1.0f
			);
			
			map_thumbnail_obj.addOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					setBackgroundID(map_thumbnail_back.getTextureID());
				}
			});
			drawableArea.addChild(map_thumbnail_obj);
		}
//		
		if((arrivalTime = getArrivalTimeObj(drawableArea.aspect()*arrivalTimeRect.height()/arrivalTimeRect.width())) != null)
		{
			arrivalTime.initObject
			(
				drawableArea, 
				arrivalTimeRect.centerX(), 
				arrivalTimeRect.centerY(), 
				arrivalTimeRect.width(), 
				drawableArea.aspect()*arrivalTimeRect.height()/arrivalTimeRect.width(), 
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			/*
			 * TEST
			 */
			
			new HandlerThread("")
			{
				public void run() 
				{
					Date date = new Date();
					
					while(true)
					{
						date.setTime(System.currentTimeMillis());
						if(arrivalTime != null)
						{
							((DriveTakeText3D)arrivalTime).setDriveWalkText(date.getSeconds(), date.getSeconds()+1, date.getSeconds()+2, date.getSeconds()+3);
						}
						else break;
						//
						try {sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
					}
				};
			}.start();
			
			drawableArea.addChild(arrivalTime);
		}
//		
		if((youShouldNotice = createTitleObj("You Should Notice", drawableArea.aspect()*youShouldNotRect.height()/youShouldNotRect.width())) != null)
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
//		
		if
		(
			getImagePath() != null 
			&& 
			(streetViewImage=createStreetViewImage(drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width())) != null
		)
		{
			streetViewImage.initObject
			(
				drawableArea, 
				infoHelpViews[infoHelpViewsCounter].centerX(), 
				infoHelpViews[infoHelpViewsCounter].centerY(), 
				infoHelpViews[infoHelpViewsCounter].width(), 
				drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width(),
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			infoHelpViewsCounter++;
			
			streetViewImage.addOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					setBackgroundID(street_view_back.getTextureID());
				}
			});
			drawableArea.addChild(streetViewImage);
		}
				
		if
		(
			getVideoFile() != null
			&& 
			(videoButton=createVideoButton(drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width())) != null
		)
		{
			videoButton.initObject
			(
				drawableArea, 
				infoHelpViews[infoHelpViewsCounter].centerX(), 
				infoHelpViews[infoHelpViewsCounter].centerY(), 
				infoHelpViews[infoHelpViewsCounter].width(), 
				drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width(),
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			infoHelpViewsCounter++;
			
			videoPlayer = new VideoPlayer3D(getVideoFile().getAbsolutePath());
			videoPlayer.setSurfaceTexture();
			videoPlayer.initObject(drawableArea, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
			
			videoButton.addOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					// TODO Auto-generated method stub
//					Toast.makeText(getContext(), "will play Video !", Toast.LENGTH_LONG).show();
					setBackgroundObject(videoPlayer);
				}
			});
			
			drawableArea.addChild(videoButton);
		}
		
		if
		(
			audio_path != null
			&&
			infoHelpViewsCounter < 2
			&& 
			(audioButton = createAudioButton(drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width())) != null
		)
		{
			audioButton.initObject
			(
				drawableArea, 
				infoHelpViews[infoHelpViewsCounter].centerX(), 
				infoHelpViews[infoHelpViewsCounter].centerY(), 
				infoHelpViews[infoHelpViewsCounter].width(), 
				drawableArea.aspect()*infoHelpViews[infoHelpViewsCounter].height()/infoHelpViews[infoHelpViewsCounter].width(),
				USE_WEIGHT ? 1.0f:1.0f
			);
			
			infoHelpViewsCounter++;
			
			audioButton.addOnClickCallback(new OnClickedAction() 
			{
				@Override
				public void onClicked() 
				{
					playAudio(audio_path);
//					Toast.makeText(getContext(), "will play sound !", Toast.LENGTH_LONG).show();
				}
			});
			
			drawableArea.addChild(audioButton);
		}
		// TODO Auto-generated method stub
	}
	
	private RectObject3D createVideoButton(float aspect) 
	{
		RectObject3D ans      = new ImageObject3D("VideoButton");
		Bitmap       bit      = null;
		Bitmap       work     = null;
		
		bit = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.play_video_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
		
		work = createBitmap(aspect);
		
//		ImageObject3D.fixIconByColor(bit, Color.argb(255, 255, 176, 16));
		ImageObject3D.CircleCut(bit, Color.argb(255, 255, 176, 16));
		
		drawImageAtCenter(work, bit, aspect);
		
		ans.generateTextureID(work);
		work.recycle();
		bit.recycle();
		
		return ans;
	}

	private RectObject3D createAudioButton(float aspect) 
	{
		RectObject3D ans      = new ImageObject3D("AudioButton");
		Bitmap       bit      = null;
		Bitmap       work     = null;
		bit = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.place_audio_tips_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
		
		work = createBitmap(aspect);
		
		ImageObject3D.fixIconByColor(bit, Color.argb(255, 255, 176, 16));
		ImageObject3D.CircleCut(bit, Color.argb(255, 255, 176, 16));
		
		drawImageAtCenter(work, bit, aspect);
		
		ans.generateTextureID(work);
		work.recycle();
		bit.recycle();
		return ans;
	}

	private RectObject3D createStreetViewImage(float aspect) 
	{
		RectObject3D ans      = new ImageObject3D("StreetViewImage");
		Bitmap       work     = null;
		Bitmap       bit      = null;
		Bitmap       backBit  = null;
		Matrix       matrix   = new Matrix();
		
		street_view_back = new ImageObject3D("MapThumnail4Background");
		
		try 
		{ 
			bit = BitmapFactory.decodeStream(new FileInputStream(getImagePath())).copy(Bitmap.Config.ARGB_8888, true);
			//
			matrix.preScale(-1.0f, 1.0f);
			backBit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
			street_view_back.generateTextureID(backBit);
			
			ImageObject3D.CircleCut(bit, Color.argb(255, 255, 176, 16));
			
			work = createBitmap(aspect);
			drawImageAtCenter(work, bit, aspect);
			ans.generateTextureID(work);
			
			bit.recycle();
			backBit.recycle();
		} 
		catch (FileNotFoundException e) 
		{
			ans = null;
		}
		
		return ans;
	}

	private RectObject3D createMapAddress(float aspect) 
	{
		RectObject3D mapAddress    = new ImageObject3D("mapAddress");
		Bitmap       mapAddressBit = createMapAddresBitmap(aspect, null);
		
		mapAddress.generateTextureID(mapAddressBit);
		
		mapAddressBit.recycle();
		
		return mapAddress;
	}
	
	private void createOpenWazeDialog() 
	{
		AlertDialog.Builder builder = null;
		
        builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setPositiveButton("Open Waze", new OnClickListener() 
        {
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				try 
				{
//					String url = "waze:?ll=37.44469,-122.15971&navigate=yes";
					String url = String.format("waze://?ll=%s,%s&navigate=yes", getLatitude(), getLongitude());
					Log.d("elazarkin", "url = " + url);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					getContext().startActivity(intent);
				} 
				catch (ActivityNotFoundException ex) 
				{
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
					getContext().startActivity(intent);
				}
			}
		});
        
        builder.setNegativeButton("Cancel", null);
        builder.setTitle("Drive to destination?");
        builder.setMessage(getMapAddressString());
        openWazeDialog = builder.create();
	}

	private RectObject3D createMapThumnail(float aspect) 
	{
		RectObject3D ans      = new ImageObject3D("MapThumnail");
		Bitmap       work     = null;
		Bitmap       bit      = null;
		Bitmap       backBit  = null;
		Matrix       matrix   = new Matrix();
		
		map_thumbnail_back = new ImageObject3D("MapThumnail4Background");
		
		try 
		{
			bit = BitmapFactory.decodeStream(new FileInputStream(map_thumbnail)).copy(Bitmap.Config.ARGB_8888, true);
			//
			matrix.preScale(-1.0f, 1.0f);
			backBit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
			map_thumbnail_back.generateTextureID(backBit);
			
			work  = createBitmap(aspect);
			ImageObject3D.CircleCut(bit, Color.argb(255, 255, 176, 16));
			drawImageAtCenter(work, bit, aspect);
			ans.generateTextureID(work);
			
			bit.recycle();
			backBit.recycle();
		} 
		catch (FileNotFoundException e) 
		{
			ans = null;
		}
		
		return ans;
	}

	private void drawImageAtCenter(Bitmap work, Bitmap bit, float aspect) 
	{
		Canvas cwork = new Canvas(work);
		if(aspect < 1.0f)
		{
			cwork.drawBitmap
			(
				bit, 
				new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
				new Rect(work.getWidth()/2 - work.getHeight()/2, 0, work.getWidth()/2 + work.getHeight()/2, work.getHeight()), 
				null
			);
		}
		else
		{
			cwork.drawBitmap
			(
				bit, 
				new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
				new Rect(0, work.getHeight()/2 - work.getWidth()/2, work.getWidth(), work.getHeight()/2 + work.getWidth()/2), 
				null
			);
		}
	}

	private RectObject3D createGoButton(float aspect) 
	{
		float internalWieght = 1.6f;
		FlickerImageObject3D goButton = new FlickerImageObject3D("goButton");
		//
		Bitmap car = BitmapFactory.decodeResource
		(
			getContext().getResources(), 
			R.drawable.drive_icon_3x
		).copy(Bitmap.Config.ARGB_8888, true);
		//
		Bitmap work = Bitmap.createBitmap
		(
			(int)(internalWieght*car.getHeight()/aspect), 
			(int)(internalWieght*car.getHeight()), 
			Bitmap.Config.ARGB_8888
		);
		
		Canvas cwork   = new Canvas(work);
		Paint pwork    = new Paint();
		float textSize = work.getHeight()/3.0f;
		
		//
		ImageObject3D.CircleCut(car);
		cwork.drawColor(Color.argb(0, 0, 0, 0));
		//
		// TODO delete this line
		if(isBackgroundOn()) cwork.drawColor(Color.argb(255, 0, 255, 0));
		//
		cwork.drawBitmap(car, 0, work.getHeight()/2 - car.getHeight()/2, null);
		pwork.setTextSize(textSize);
		cwork.drawText("GO!", car.getWidth() + 10, work.getHeight()/2 + textSize/2, pwork);
		
		if(!isBackgroundOn())
		ImageObject3D.fixIconByColor(work, Color.argb(255, 255, 100, 0));
		goButton.generateTextureID(work);
		car.recycle();
		work.recycle();
		return goButton;
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
	
	@Override
	public void destroy() 
	{
		/*
		 * non group objects
		 */
		if(map_thumbnail_back != null)
		{
			map_thumbnail_back.destroy();
		}
		
		if(street_view_back != null)
		{
			street_view_back.destroy();
		}
		
		super.destroy();
	}
	
	protected abstract RectObject3D createTitle(float aspect);
	protected abstract RectObject3D getArrivalTimeObj(float aspect);
}
