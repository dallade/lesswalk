package com.lesswalk.views;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyCameraView extends SurfaceView implements SurfaceHolder.Callback 
{
	private SurfaceHolder mHolder = null;
	private Camera        cam     = null;
	
	private CameraViewBitmapCallback callback = null;
	
	private CustomOutputStream       os       = new CustomOutputStream();
//	private CustomInputStream        is       = new CustomInputStream();
	
	public interface CameraViewBitmapCallback
	{
		void onFrame(Bitmap bit);
	}
	//public static int sizex=640,sizey=480;
	@SuppressWarnings("deprecation")
	public MyCameraView(Context context) 
	{
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//mHolder.setFixedSize(400, 400);
	}
	
	private static Size size;
	
	public void surfaceCreated(SurfaceHolder holder) 
	{
		Log.d("elazarkin", "camera surfaceCreated");
		cam = Camera.open(0);
		try 
		{
			cam.setPreviewDisplay(holder);
//			cam.setDisplayOrientation(90);
			
		} 
		catch (IOException exception) 
		{
			cam.release();
			cam = null;			
		}
		Camera.Parameters pcam=cam.getParameters();
		pcam.setPreviewSize(640, 480);
		size = pcam.getPreviewSize();
		cam.setParameters(pcam);
		cam.startPreview();
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	{
		Log.d("elazarkin", "camera surfaceChanged");
		cam.startPreview();
		cam.setPreviewCallback(new PreviewCallback() 
		{
			public void onPreviewFrame(byte[] data, Camera camera) 
			{
				if(callback != null)
				{
					Bitmap   bit = null;
					YuvImage yuv = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
					os.reset();
					yuv.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, os);
					Log.d("elazarkin", "onPreviewFrame os.bufferSize="+os.bufferSize);
					bit = BitmapFactory.decodeByteArray(os.buffer, 0, os.bufferSize);
					callback.onFrame(bit);
				}
//				
			}
		});
	}
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		Log.d("elazarkin", "camera surfaceDestroyed");
		cam.setPreviewCallback(null);
		cam.stopPreview();
		cam.release();
		cam = null;
	}
	
	private class CustomOutputStream extends OutputStream
	{
		private byte buffer[]   = null;
		private int  bufferSize = 0;
		
		public CustomOutputStream() 
		{
			buffer     = new byte[1024*1024*4];
			bufferSize = 0;
		}
		
		public void reset()
		{
			bufferSize = 0;
		}
		
		@Override
		public void write(byte[] src) throws IOException 
		{
			if(bufferSize + src.length < buffer.length)
			{
				System.arraycopy(src, 0, buffer, bufferSize, src.length);
				bufferSize += src.length;
			}
		}
		
		@Override
		public void write(int oneByte) throws IOException 
		{
			buffer[bufferSize++] = (byte) oneByte;
		}
	}
	
//	private class CustomInputStream extends InputStream
//	{
//		private byte buffer[]    = null;
//		private int  bufferSize  = 0;
//		private int  bufferIndex = 0;
//		
//		public CustomInputStream() 
//		{
//			buffer     = new byte[1024*1024*4];
//			bufferSize = 0;
//		}
//		
//		public void setBuffer(byte[] buffer, int size)
//		{
//			System.arraycopy(buffer, 0, this.buffer, 0, size);
//			bufferSize = size;
//		}
//
//		@Override
//		public int read() throws IOException 
//		{
//			return bufferIndex < bufferSize ? buffer[bufferIndex++]:-1;
//		}
//	}
	public void setFrameCallback(CameraViewBitmapCallback callback) 
	{
		this.callback = callback;
	}
}


