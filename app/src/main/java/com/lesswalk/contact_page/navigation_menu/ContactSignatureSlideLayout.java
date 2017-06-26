package com.lesswalk.contact_page.navigation_menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.lesswalk.R;
import com.lesswalk.json.AssetsJson;
import com.lesswalk.json.CarruselJson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;

public class ContactSignatureSlideLayout extends View 
{
	public interface IContactSignatureSliderCallback
	{
		void onSignatureClicked(String path);
	}

	private class SignatureArea
	{
		String path      = "";
		float x0 = 0, y0 = 0, x1 = 0, y1 = 0;
	}

	private static final int   ICON_RESOLUTION          = 256;
	private static final long  MAX_TOUCHED_TIME_PRESSED = 200;
	private static final long  MAX_MOVED_DIST           = 10;
	private static final float RETURN_VELOCATION        = 3000.0f;

	private static AssetsJson assets  = null;
	private static Bitmap     icons[] = null;
	private static int        NO_TYPE = 0;

	private        CarruselJson                    faked            = null;
	private        IContactSignatureSliderCallback callback         = null;
	private        Vector<CarruselJson>            container        = null;
	private        Vector<CarruselJson>            workContainer    = null;
	private        SignatureArea                   areas[]          = null;
	private        HandlerThread                   returnAnimation  = null;
	private        int                             icons_in_row     = 5;
	private        boolean                         editable         = false;
	private        Bitmap                          screen           = null;
	private        Canvas                          cscreen          = null;
	private        float                           offset           = 0.0f;
	private        float                           last_touched_x   = 0.0f;
	private        float                           last_touched_y   = 0.0f;
	private        float                           moved_dist       = 0.0f;
	private        float                           last_speed       = 0.0f;
	private        long                            last_time        = 0L;
	private        long                            touched_time     = 0L;
	private        boolean                         touched          = false;
	private        int                             animation_target = 0;
	private        boolean                         needRedraw       = false;
	private        boolean                         animationAlive   = false;
	private        int                             randomIndex      = 0;
	
	public ContactSignatureSlideLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		container     = new Vector<CarruselJson>();
		workContainer = new Vector<CarruselJson>();
		areas         = new SignatureArea[icons_in_row*2];
		
		for(int i = 0; i < areas.length; i++) areas[i] = new SignatureArea();
		
		faked         = new CarruselJson();
		
		for(int i = 0; i < icons_in_row; i++)
		{
			workContainer.add(faked);
		}

		randomIndex = (int) (Math.random()*65536);
	}

	public void setCallback(IContactSignatureSliderCallback callback)
	{
		this.callback = callback;
	}

	public void removeAllChilds()
	{
		container.removeAllElements();
	}

	public int addContactSignature(CarruselJson signature)
	{
		if(alreadyContainSignature(container, signature))
		{
			return -1;
		}
		
		workContainer.removeAllElements();
		
		container.add(signature);
		
		workContainer.addAll(container);
		
		if(editable && workContainer.size()%icons_in_row == 0)
		{
			workContainer.add(faked);
		}
		
		while(workContainer.size()%icons_in_row != 0)
		{
			workContainer.add(faked);
		}
		
		needRedraw = true;
		
		return 0;
	}
	
	private boolean alreadyContainSignature(Vector<CarruselJson> container, CarruselJson signature)
	{
		for(CarruselJson c:container)
		{
			if(c.getKey().equals(signature.getKey()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		int into_areas_counter = 0;
		int icon_area_size     = 0;
		int icon_size          = 0;
		
		float startx = 0;
		float endx   = 0;
		
		float fixed_offset = 0.0f;
		float all_size     = 0.0f;

		if(assets == null)
		{
			try
			{
				File assetsDir = new File(getContext().getFilesDir(), "assets");

				if(assetsDir.exists())
				{
					assets = AssetsJson.createFromFile(new File(assetsDir, "content.json"));
					icons = new Bitmap[assets.getImages().length + 1];

					for (int i = 0; i < assets.getImages().length; i++)
					{
						icons[i] = BitmapFactory.decodeStream(new FileInputStream(new File(assetsDir, assets.getImages()[i].getName())));
					}

					icons[(NO_TYPE = icons.length - 1)] = BitmapFactory.decodeResource(getResources(), R.drawable.dashed_border_2x);
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}

		if(icons == null) return;
		
		if(screen == null || screen.getWidth() != getWidth() || screen.getHeight() != getHeight())
		{
			if(screen != null) screen.recycle();
			
			screen  = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			cscreen = new Canvas(screen);
		}
		
		cscreen.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		
		icon_size      = screen.getHeight()*2/3;
		icon_area_size = screen.getWidth()/icons_in_row;
		
		if(icon_size > icon_area_size) icon_size = icon_area_size;
		
		/*
		 * offset can be between 0 to w*(workContainer/icons_in_row)
		 * 
		 *  we try to draw the object twice on offset and on -w*(workContainer/icons_in_row) + offset
		 */
		
		all_size		= screen.getWidth()*workContainer.size()/icons_in_row;
		fixed_offset	= offset;
		
		while(fixed_offset < 0.0f) fixed_offset += all_size;
		while(fixed_offset >= all_size) fixed_offset -= all_size;
		
		for(int i = 0; i < workContainer.size(); i++)
		{
			startx = i*icon_area_size + fixed_offset;
			if(startx < screen.getWidth())
			{
				//Log.d("aaaa", into_areas_counter + ": startx=" + startx + " w=" + screen.getWidth() + " allScreen = " + all_size + " offset=" + fixed_offset);
				initArea(areas[into_areas_counter], icon_area_size, startx, icon_size, workContainer.elementAt(i).getKey());
				drawIcon(cscreen, areas[into_areas_counter], workContainer.elementAt(i).getIcon());
				into_areas_counter++;
			}
			
			endx = -all_size + (i+1)*icon_area_size + fixed_offset;
			if(endx > 0 && endx < screen.getWidth() + icon_area_size)
			{
				//Log.d("aaaa", into_areas_counter + ": endx=" + endx + " w=" + screen.getWidth() + " allScreen = " + all_size + " offset=" + fixed_offset);
				initArea(areas[into_areas_counter], icon_area_size, endx - icon_area_size, icon_size, workContainer.elementAt(i).getKey());
				drawIcon(cscreen, areas[into_areas_counter], workContainer.elementAt(i).getIcon());
				into_areas_counter++;
			}
		}
		
		while(into_areas_counter < areas.length)
		{
			areas[into_areas_counter++].path = null;
		}
		
		canvas.drawBitmap(screen, 0, 0, null);
	}

	private void drawIcon(Canvas c, SignatureArea area, String icon)
	{
		Bitmap bit   = null;
		int    index = icon != null ? typeToIconType(icon):NO_TYPE;

		bit = icons[index];
		c.drawBitmap
		(
			bit, 
			new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
			new Rect((int)area.x0, (int)area.y0, (int)area.x1, (int)area.y1), 
			null
		);
	}
	
	private int typeToIconType(String type)
	{
		for (int i = 0; i < assets.getImages().length; i++)
		{
			if(type.contains(assets.getImages()[i].getKey())) return i;
		}

		return NO_TYPE;
	}

	private void initArea(SignatureArea signatureArea, float icon_area_size, float startx, int icon_size, String path) 
	{
		signatureArea.x0   = startx + (icon_area_size - icon_size)/2.0f;
		signatureArea.x1   = signatureArea.x0 + icon_size;
		signatureArea.y0   = 0;
		signatureArea.y1   = icon_size;
		signatureArea.path = path;
	}
	
	class ReturnAnimationThread extends HandlerThread
	{
		public ReturnAnimationThread()
		{
			super("slider return thread");
		}

		@Override
		public void run() 
		{
			long  debug_t   = System.currentTimeMillis();
			long  last_t    = System.currentTimeMillis();
			long  curr_t    = 0L;
			float curr_step = 0.0f;
			float curr_dist = 0.0f;

			animationAlive = true;
			
			while(animationAlive)
			{
				curr_t    = System.currentTimeMillis();
				
				if(!touched)
				{
					curr_step = RETURN_VELOCATION*(curr_t - last_t)/1000.0f;
					curr_dist = offset - animation_target;
					
					if(Math.abs(curr_step) > Math.abs(curr_dist))
					{
						curr_step = Math.abs(curr_dist);
					}
	
					if(curr_step != 0.0f)
					{
						needRedraw = true;
						offset += curr_step*(offset < animation_target ? 1.0f:-1.0f);
					}
				}
				
				if(needRedraw)
				{
					((Activity)getContext()).runOnUiThread(new Runnable()
					{
						@Override
						public void run() 
						{
							ContactSignatureSlideLayout.this.invalidate();
						}
					});
				}
				
				last_t = curr_t;
				if(curr_t - debug_t > 1000)
				{
//					Log.d("elazarkin", "thread_" + randomIndex + ": still alive");
					debug_t = curr_t;
				}
				try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		long    curr_t  = System.currentTimeMillis();
		boolean outside = false;
		boolean ret     = false;
		int     action  = event.getAction();
		int     x       = (int)(event.getX() + 0.5f);
		int     y       = (int)(event.getY() + 0.5f);


		if(screen == null) return false;
		//
		if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) 
		{
			outside = true;
		}
		//
		if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || outside)
		{
			if
			(
				action == MotionEvent.ACTION_UP 
				&& 
				curr_t - touched_time < MAX_TOUCHED_TIME_PRESSED
				&&
				moved_dist < MAX_MOVED_DIST
			)
			{
				int index = 0;
				for(; index < areas.length; index++)
				{
					if(intoArea(areas[index], last_touched_x, last_touched_y))
					{
//						Toast.makeText(getContext(), "will open carussel path=" + areas[index].path, Toast.LENGTH_SHORT).show();
						if(callback != null) callback.onSignatureClicked(areas[index].path);
					}
				}
				
				startReturnAnimation(0);
			}
			else
			{
				startReturnAnimation(last_speed > 0.4f ? 1 : last_speed < -0.4f ? -1 : 0);
			}
			touched_time = 0L;
			touched      = false;
		}
		else if(action == MotionEvent.ACTION_MOVE)
		{
			float diff_x = event.getX() - last_touched_x;
			float diff_y = event.getY() - last_touched_y;
			
			float all_layouts_size = screen.getWidth()*workContainer.size()/icons_in_row;
			
			offset += diff_x;
			
			if(offset < 0.0f) offset += all_layouts_size;
			
			if(offset >= all_layouts_size) offset -= all_layouts_size;
			
			moved_dist += Math.sqrt(diff_x*diff_x + diff_y*diff_y);
			
			last_speed = diff_x/(curr_t - last_time);
		}
		else
		{
			if(action == MotionEvent.ACTION_DOWN)
			{
				touched_time = curr_t;
				last_time    = curr_t;
				moved_dist   = 0.0f;
				touched      = true;
			}
			ret     = true;
		}
		//
		last_touched_x = event.getX();
		last_touched_y = event.getY();
		last_time      = curr_t;
		//
//		invalidate();
		return ret;
	}

	private boolean intoArea(SignatureArea area, float x, float y) 
	{
		return x > area.x0 && x < area.x1 && y > area.y0 && y < area.y1;
	}

	private void startReturnAnimation(int i) 
	{
		if(i == 0)
		{
			animation_target    = (int)(offset/screen.getWidth() + 0.5f)*screen.getWidth();
		}
		else if(i < 0)
		{
			animation_target    = (int)(offset/screen.getWidth())*screen.getWidth();
		}
		else animation_target    = (int)(offset/screen.getWidth() + 1.0f)*screen.getWidth();
	}

	public void pause()
	{
		try
		{
			animationAlive = false;
			returnAnimation.join();
			returnAnimation.quit();
		}
		catch (Exception e)
		{
			Log.e("elazarkin", "error: " + e.getMessage());
			e.printStackTrace();
		}
	}

    public void resume()
    {
		(returnAnimation = new ReturnAnimationThread()).start();
    }
}
