package com.lesswalk.pagescarussel;

import java.io.IOException;

import com.lesswalk.bases.RectObject3D;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

public class VideoPlayer3D extends RectObject3D 
{
	private MediaPlayer    mediaPlayer     = null; 
	private SurfaceTexture surfaceTexture  = null;
	private int            textures[]      = {-1};
	
	public VideoPlayer3D(String videoSource) 
	{
		super("VideoPlayerButton");
		
		mediaPlayer = new MediaPlayer();
		try 
		{
			mediaPlayer.setDataSource(videoSource);
			mediaPlayer.prepare();
		} 
		catch (IllegalArgumentException e) {e.printStackTrace();} 
		catch (SecurityException e) {e.printStackTrace();} 
		catch (IllegalStateException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
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
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glGenTextures(1, textures, 0);
		
		surfaceTexture = new SurfaceTexture(textures[0]);
		surfaceTexture.setOnFrameAvailableListener(frameAvailableListener);
		mediaPlayer.setSurface(new Surface(surfaceTexture));
		mediaPlayer.setLooping(true);
	}
	
	@Override
	public void drawCurrent() 
	{
		if(surfaceTexture == null) return;
		surfaceTexture.updateTexImage();
		//
		drawCurrent(textures[0]);
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
}
