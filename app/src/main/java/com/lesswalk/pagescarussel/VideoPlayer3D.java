package com.lesswalk.pagescarussel;

import com.lesswalk.bases.RectObject3D;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

public class VideoPlayer3D extends RectObject3D 
{
	private MediaPlayer    mediaPlayer     = null; 
	private SurfaceTexture surfaceTexture  = null;
	
	public VideoPlayer3D(String videoSource) 
	{
		super("VideoPlayerButton");
		
		mediaPlayer = new MediaPlayer();
		try 
		{
			mediaPlayer.setDataSource(videoSource);
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() 
			{
				@Override
				public void onPrepared(MediaPlayer mp) 
				{
					Log.d("elazarkin", "mediaPlayer onPrepared");
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() 
			{
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) 
				{
					Log.d("elazarkin", "error(" + what + ") extra(" + extra + ")");
					return false;
				}
			});
		} 
		catch (Exception e) 
		{
			Log.d("elazarkin", "" + e.getMessage());
		} 
	}	
	
	private OnFrameAvailableListener frameAvailableListener = new OnFrameAvailableListener() 
	{
		@Override
		public void onFrameAvailable(SurfaceTexture surfaceTexture) 
		{
			Log.d("elazarkin", "mediaPlayer.onFrameAvailable()!");
		}
	};
	
	public void setSurfaceTexture() 
	{
		if(getTextureID() < 0)
		{
			//GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
			//checkGlError("Texture bind_1");
//			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			//checkGlError("Texture bind_1.1");
			//GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
			//checkGlError("Texture bind_1.2");
			generateTextureID();
//			checkGlError("Texture bind_1.3");
			
			GLES20.glEnable(GL_TEXTURE_EXTERNAL_OES);
//			checkGlError("Texture bind_1.4");
			GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, getTextureID());
			//checkGlError("Texture bind_2");
			GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			//checkGlError("Texture bind_2.1");
			GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//			checkGlError("Texture bind_2.2");
//			GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
//			checkGlError("Texture bind_3");
//			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//			GLES20.glEnable(GLES20.GL_TEXTURE0);
//			GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, getTextureID());
//			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
//            //
//            checkGLError("glBindTexture(" + textures[0] + ")");
//            //
		}
		
		if(surfaceTexture == null)
		{
			//surfaceTexture = new SurfaceTexture(getTextureID());
			surfaceTexture = new SurfaceTexture(getTextureID());
		}
		
		surfaceTexture.setOnFrameAvailableListener(frameAvailableListener);
		mediaPlayer.setSurface(new Surface(surfaceTexture));
		mediaPlayer.setLooping(true);
	}
	
	@Override
	public void drawCurrent() 
	{
		float temp[] = new float[16];
		if(surfaceTexture == null) return;
		//
		surfaceTexture.updateTexImage();
		surfaceTexture.getTransformMatrix(temp);
		drawCurrent(getTextureID());
	}
	


	@Override
	public void release() 
	{
		mediaPlayer.stop();
	}
	
	@Override
	public void prepare() 
	{
		mediaPlayer.start();
		Log.d("elazarkin", "mediaPlayer.start()!");
	}
	
	@Override
	protected boolean isExternal() 
	{
		return true;
	} 
}
