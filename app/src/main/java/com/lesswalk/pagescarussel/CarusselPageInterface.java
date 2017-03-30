package com.lesswalk.pagescarussel;

import java.io.File;

import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by root on 3/12/16.
 */
public abstract class CarusselPageInterface extends RectObject3D
{
	protected static final boolean USE_WEIGHT = true;
	
	private static final float   PAGE_Y_START    = 0.06f;
	private static final float   ARC_RADIUS      = PAGE_Y_START*1.0f;
	
    private static final float   FRAME_WEIGHT    = USE_WEIGHT ? 0.9f:1.0f;
    private static final float   FRAME_ASPECT    = 1.6f;
    private static final float   FRAME_W_RADIUS  = 0.45f;
    private static final int     FRAME_PIXEL_W   = 1024;
    
    private static final float   FRAME_X0        = 0.0f;
    private static final float   FRAME_Y0        =-0.9f*FRAME_ASPECT+FRAME_W_RADIUS*FRAME_ASPECT;
    
    private static final float   VIEW_W_WEIGHT   = 0.5f;
    private static final float   VIEW_H_WEIGHT   = 1.0f;
    
    private static final float   STEP_ROTATION   = 4.0f;
    
    private static MediaPlayer   mediaPlayer     = null;
    
    protected class ImageItem
    {
        String imagePath = null;
        String direction = "0";
        String latitude  = "0.0f";
        String longitude = "0.0f";
        String source    = null;
        float  zoom      = 0.0f;
    }

    protected class MapAddressItem
    {
        final String itemsMap[] = {"City", "Country", "CountryCode", "Street"};
        final int CITY         = 0;
        final int COUNTRY      = 1;
        final int COUNTRY_CODE = 2;
        final int STREET       = 3;
        final int SIZE         = 4;
        
        String    items[]      = null;

        MapAddressItem()
        {
            items = new String[SIZE];
            for(int i = 0; i < SIZE; i++) items[i] = "";
        }
    }

	private static float WIDTH  = -1;
	private static float HEIGTH = -1;
    
    private ImageItem      image      = null;
    private MapAddressItem mapAddress = null;
    
    private String  title    = null;
    private boolean isInited = false;
    private Context context  = null;

	private int   index = -1;
	
	private PageDrawable3D  drawableArea = null;

	private File videoFile = null;

	private static boolean mediaPlayerBusy = false;

    public CarusselPageInterface(String title, Context context)
    {
    	super(title + "_page");
    	//
        this.title   = title + "";
        this.context = context;
        initObject(null, FRAME_X0, FRAME_Y0, page_width(), page_aspect(), FRAME_WEIGHT);
    }

    //
    public boolean isInited() {return isInited;}
    //
	public void setIndex(int index) {this.index = index;}
	//
	public int getIndex() {return index;}
	//
	public Context getContext() {return context;}
	//
    protected void freeze()
    {
    	Log.d("elazarkin", String.format("freeze() getIndex()=%d getTextureID()=%d", getIndex(), getTextureID()));
    	
        if(getTextureID() < 0)
        {
        	ImageObject3D   icon         = null;
        	createBasicPage();
        	//
        	icon = getIcon();
        	icon.initObject
        	(
    			this, 
    			0.0f, 
    			0.5f - ARC_RADIUS,
    			ARC_RADIUS*2*aspect(), 
    			1.0f,
    			USE_WEIGHT ? 0.9f:1.0f
			);
        	addChild(icon);
        	//
        	drawableArea = new PageDrawable3D("drawableArea");
//        	if(isBackgroundOn())
//        	{
//	        	Bitmap test  = Bitmap.createBitmap(32, 32, Config.ARGB_8888);
//	        	Canvas ctest = new Canvas(test);
//	        	ctest.drawColor(Color.argb(180, 0, 0, 255));
//	        	drawableArea.generateTextureID(test);
//	        	test.recycle();
//        	}
        	drawableArea.initObject(this, 0.0f, -ARC_RADIUS, 1.0f, (1.0f - ARC_RADIUS*2)*aspect(), USE_WEIGHT ? 0.95f:1.0f);
        	
        	addChild(drawableArea);
        	//
        	addChilds(drawableArea);
        }
        isInited = true;
    }
    
    protected abstract void addChilds(RectObject3D drawableArea);

	private void createBasicPage() 
    {
    	Bitmap page         = Bitmap.createBitmap(FRAME_PIXEL_W, (int) (FRAME_PIXEL_W*FRAME_ASPECT), Bitmap.Config.ARGB_8888);
        Canvas cpage        = new Canvas(page);
        Paint  ppage        = new Paint();
        
        cpage.drawColor(Color.argb(0, 0, 0, 0));

        ppage.setColor(Color.argb(255, 255, 247, 229));
        cpage.drawRect(0.0f, page.getHeight()*ARC_RADIUS, page.getWidth(), page.getHeight(), ppage);
        //
        // TODO delete this line
        if(isBackgroundOn()) ppage.setColor(Color.argb(255, 255, 255, 255));
        //
        cpage.drawCircle(page.getWidth()/2, page.getHeight()*ARC_RADIUS, page.getHeight()*ARC_RADIUS, ppage);
        
        generateTextureID(page);
        
        page.recycle();
	}
	
	protected Bitmap createTitleBitmap(String title, float aspect) 
	{
		Bitmap work             = ImageObject3D.createBitmap(aspect);
		Canvas cwork            = new Canvas(work);
		Paint  pwork            = new Paint();
		float  titleTextSize    = work.getHeight()*0.7f;
		float  startOfLine      = 0.05f*work.getWidth();
		float  lineDistFromText = 0.02f*work.getWidth();
		Rect   titleBounds      = new Rect();
		
		if(title != null)
		{
			//
			pwork.setStrokeWidth(work.getHeight()/20.0f);
			pwork.setTextSize(titleTextSize);
			pwork.setFakeBoldText(true);
			pwork.setTextSkewX(-0.2f);
			pwork.getTextBounds(title, 0, title.length(), titleBounds);
			//
			// TODO delete this line
			if(isBackgroundOn())cwork.drawColor(Color.argb(255, 128, 64, 192));
			//
			cwork.drawLine
			(
				startOfLine, 
				work.getHeight()/2,
				work.getWidth()/2  - titleBounds.width() / 2 - lineDistFromText,
				work.getHeight()/2, 
				pwork
			);
	
			cwork.drawLine
			(
				work.getWidth() - startOfLine, 
				work.getHeight()/2,
				work.getWidth()/2 + titleBounds.width()/2 + lineDistFromText,
				work.getHeight()/2, 
				pwork
			);
	
			cwork.drawText(title, work.getWidth()/2 - titleBounds.width()/2, work.getHeight()/2 + titleBounds.height()/3, pwork);
		}
		//
		return work;
	}

	protected Bitmap getTextTipImage(String text_tip, float aspect)
	{
		Bitmap work       = ImageObject3D.createBitmap(aspect);
		Canvas cwork      = new Canvas(work);
		Paint  pwork      = new Paint();
		int    lineCount  = 5;
		float  lineSpaceH = work.getHeight() / lineCount;

		pwork.setColor(Color.GRAY);
		pwork.setStrokeWidth(3.0f);

		for(int i = 0; i < lineCount; i++)
		{
			cwork.drawLine(0, lineSpaceH*(i+1)-1, work.getWidth(), lineSpaceH*(i+1)-2, pwork);
		}

		if(text_tip != null)
		{
			ImageObject3D.drawTextToRect(text_tip, lineSpaceH*4/5, 0.0f, work, cwork, pwork, new float[]{-lineSpaceH*0.1f}, lineSpaceH);
		}

		return work;
	}
	
	protected Bitmap createMapAddresBitmap(float aspect, String additionText) 
	{
		Bitmap work   = ImageObject3D.createBitmap(aspect);
		Canvas cwork  = new Canvas(work);
		Paint  pwork  = new Paint();
		
		if(mapAddress != null)
		{
//			String addressText[]    = { mapAddress.items[mapAddress.STREET], mapAddress.items[mapAddress.CITY] };
			String fullText         = mapAddress.items[mapAddress.STREET] + ", " + mapAddress.items[mapAddress.CITY];
			float  addressTextSize  = work.getHeight()*0.15f;
			float  textOffset[]     = {0.0f};
			//
			// TODO delete this line
			if(isBackgroundOn()) cwork.drawColor(Color.WHITE);
			//
			textOffset[0] += work.getHeight()*0.02f;
			
			if(additionText != null) ImageObject3D.drawTextToRect(additionText, addressTextSize, work, cwork, pwork, textOffset);
			//
			ImageObject3D.drawTextToRect(fullText, addressTextSize, work, cwork, pwork, textOffset);
		}
		else
		{
			cwork.drawColor(Color.GREEN);
		}
		//
		return work;
	}
	
	protected String getMapAddressString()
	{
		return mapAddress != null ? (mapAddress.items[mapAddress.STREET] + ", " + mapAddress.items[mapAddress.CITY]):"No know address!";
	}

	public void initImageItem(File objectsDir, String key, String value)
    {
        if(key != null && value != null)
        {
            if(image == null) image = new ImageItem();

            if(key.equals("name"))
            {
            	image.imagePath = new File(objectsDir, value).getAbsolutePath();
                Log.d("elazarkin", "image filePath: " + image.imagePath );
            }
            else if(key.equals("latitude"))
            {
            	image.latitude = value;
            	Log.d("elazarkin", "image latitude: " + image.latitude );
            }
            else if(key.equals("longitude"))
            {
            	image.longitude = value;
            	Log.d("elazarkin", "image longitude: " + image.longitude );
            }
        }
    }
	
	protected String getLatitude() 
	{
		return image == null ? "" : image.latitude == null ? "":image.latitude;
	}
	
	protected String getLongitude()
	{
		return image == null ? "" : image.longitude == null ? "":image.longitude;
	}
	
	protected String getImagePath()
	{
		return image == null ? null : image.imagePath == null ? null : image.imagePath;
	}
	
	protected File getVideoFile() 
	{
		return videoFile;
	}
	
    public void initMapAddressItem(File objectsDir, String key, String value)
    {
        if(key != null && value != null)
        {
            int itemIndex = -1;

            if(mapAddress == null) mapAddress = new MapAddressItem();

            for(int i = 0; i < mapAddress.itemsMap.length; i++)
            {
                if(key.equals(mapAddress.itemsMap[i]))
                {
                    itemIndex = i;
                    break;
                }
            }
            //
            if(itemIndex >= 0)
            {
                mapAddress.items[itemIndex]= new String(value);
                Log.d("elazarkin", "initMapAddressItem " + this.title + " " + mapAddress.itemsMap[itemIndex] + " " + mapAddress.items[itemIndex]);
            }
        }
    }
    
    public void initVideoItem(File objectsDir, String key, String value) 
	{
    	if(key != null && value != null)
    	{
    		if(key.equals("name"))
    		{
    			videoFile = new File(objectsDir, value);
    		}
    	}
	}
    
	public static float page_aspect() {return FRAME_ASPECT;};
	public static float page_width()  {return FRAME_W_RADIUS*2;}
	public static float page_heigth() {return page_width()*FRAME_ASPECT;}

    protected abstract ImageObject3D getIcon();
    protected abstract String getPageTitle();

	public static void setModelView(int w, int h) 
	{
        float small    = 0.5f;
        float normal   = 1.0f;
        float big      = normal*normal/small;
        float w_start  = (VIEW_W_WEIGHT - big + normal)*w;
        float h_start  = (VIEW_H_WEIGHT - big + normal)*h;
        float w_size   = big*w;
        float h_size   = big*h;
        //
        WIDTH  = w;
        HEIGTH = h;
        //
        
    	GLES20.glViewport((int)w_start, (int)h_start, (int)w_size, (int)h_size);
	}
	
	protected void playAudio(File audioPath) 
	{
		if(mediaPlayer == null)
		{
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}
		
		if(mediaPlayerBusy)
		{
			mediaPlayer.reset();
			mediaPlayer.setOnCompletionListener(null);
		}
		
		try 
		{
			mediaPlayerBusy = true;
			mediaPlayer.setDataSource(audioPath.getAbsolutePath());
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() 
			{
				@Override
				public void onPrepared(MediaPlayer arg0) 
				{
					mediaPlayer.start();
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				@Override
				public void onCompletion(MediaPlayer arg0) 
				{
					mediaPlayerBusy = false;
					mediaPlayer.reset();
					Log.d("elazarkin", "onCompletion(MediaPlayer arg0)");
				}
			});
		} 
		catch (Exception e) {e.printStackTrace();}
	}
	
    private static boolean isBackgroundOn = false;

	public static boolean isBackgroundOn() {return isBackgroundOn;}

	public RectObject3D checkOnObjectClickCommand(float x, float y, int w, int h) 
	{
		return super.isOnClicked(fix_x(x, w), fix_y(y, h));
	}
	
	public RectObject3D checkOnObject(float x, float y, int w, int h) 
	{
		return super.isOnObject(fix_x(x, w), fix_y(y, h));
	}
	
	private float fix_y(float y, int h) 
	{
		return -(y/h - 0.0f)*page_heigth();
	}

	private float fix_x(float x, int w) 
	{
		return (2.0f*x/w - 1.0f)*VIEW_W_WEIGHT;
	}
	
	public void releaseClick() 
	{
		super.releseClick();
	}
	
	@Override
	public void destroy() 
	{
		if(mediaPlayer != null)
		{
			mediaPlayer.reset();
			mediaPlayer = null;
		}

		isInited = false;
		super.destroy();
	}

	public void rotateIfNeed(float[] modelView, float[] rotationM) 
	{
		if(drawableArea.getBackgroundID() >= 0 || drawableArea.getBackgroundObj() != null)
		{
			if(drawableArea.currentRotationAngle() < 180.0f - STEP_ROTATION)
			{
				drawableArea.updateRotationAngle(drawableArea.currentRotationAngle() + STEP_ROTATION);
			}
			else
			{
				drawableArea.updateRotationAngle(180.0f);
			}
		}
		else if(drawableArea.getBackgroundID() < 0 && drawableArea.getBackgroundObj() == null)
		{
			if(drawableArea.currentRotationAngle() > STEP_ROTATION)
			{
				drawableArea.updateRotationAngle(drawableArea.currentRotationAngle() - STEP_ROTATION);
			}
			else
			{
				drawableArea.updateRotationAngle(0.0f);
			}
		}
		
		Matrix.setRotateM(rotationM, 0, drawableArea.currentRotationAngle(), 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(modelView, 0, rotationM, 0, modelView, 0);
		
		return;
	}
	
	protected void setBackgroundID(int id)
	{
		drawableArea.setBackgroundID(id);
	}
	
	protected void setBackgroundObject(RectObject3D videoPlayer) 
	{
		drawableArea.setBackgroundObject(videoPlayer);
	}
	
	protected void removeBackgroundID()
	{
		drawableArea.setBackgroundID(-1);
	}

	public void setColorScale(float color_scale) 
	{
		GLES20.glUniform1f(getUnifHandler(HANDLER_UNIF_COLOR_SCALE), color_scale);
	}

	public void setModelView(float[] modelView) 
	{
		GLES20.glUniformMatrix4fv(getUnifHandler(HANDLER_UNIF_MV_MAT_INDEX), 1, false, modelView, 0);
	}
}