package com.lesswalk.editor_pages.bases;

import com.lesswalk.views.MyCameraView.CameraViewBitmapCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;

public class ImageView extends View 
{
	private Bitmap bit  = null;
	
	public ImageView(Context context) 
	{
		super(context);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) 
	{
		if(bit != null)
		{
			canvas.save();
			canvas.rotate(90, getWidth()/2, getHeight()/2);
			canvas.drawBitmap
			(
				bit, 
				new Rect(0, 0, bit.getWidth(), bit.getHeight()), 
				new Rect(0, 0, getWidth(), getHeight()), 
				null
			);
			canvas.restore();
		}
		else canvas.drawColor(Color.GRAY);
	}

	public CameraViewBitmapCallback getFrameCallback() 
	{
		return new CameraViewBitmapCallback() 
		{
			@Override
			public void onFrame(Bitmap bit) 
			{
				ImageView.this.bit = bit;
				((Activity)getContext()).runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						ImageView.this.invalidate();
					}
				});
			}
		};
	}

	public Bitmap getBitmap() 
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		return Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
	}
}
