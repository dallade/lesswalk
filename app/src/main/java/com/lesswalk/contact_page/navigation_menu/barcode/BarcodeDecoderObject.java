package com.lesswalk.contact_page.navigation_menu.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.android.AmbientLightManager;
import com.google.zxing.android.ViewfinderResultPointCallback;
import com.google.zxing.android.ViewfinderView;
import com.google.zxing.android.camera.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.lesswalk.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class BarcodeDecoderObject
{
	private static final String TAG = "lwBarcodeDecoderObject";
	
	public interface BarcodeDetectorCallback
	{
		void onBarcodeDetection(String text);
	}
	
	private enum State
	{
		PREVIEW, SUCCESS, DONE
	}
	
	private Context 		parent 			= null;
	private ViewfinderView 	viewfinderView 	= null;
	private SurfaceView		surfaceView		= null;
	
    private CameraManager           	cameraManager   	= null;
    private Result 						savedResultToShow	= null;
    private CaptureActivityHandler  	handler				= null;
    private boolean						hasSurface			= false;
    private SurfaceCallback				surfaceCallback		= null;
    private BarcodeDetectorCallback		callback			= null;
    
    private AmbientLightManager 		ambientLightManager	= null;
	
	public BarcodeDecoderObject(Context _parent, ViewfinderView _viewfinderView, SurfaceView _surfaceView)
	{
		parent				= _parent;
		viewfinderView		= _viewfinderView;
		surfaceView			= _surfaceView;
		surfaceCallback		= new SurfaceCallback();
		ambientLightManager = new AmbientLightManager(_parent);
	}
	
	public void setCallback(BarcodeDetectorCallback _callback)
	{
		callback = _callback;
	}
	
	public void resume()
	{
    	SurfaceHolder 	surfaceHolder	= null;
    	
        cameraManager = new CameraManager(parent);
        viewfinderView.setCameraManager(cameraManager);
        
        ambientLightManager.start(cameraManager);
		
		surfaceHolder = surfaceView.getHolder();
		
		if (hasSurface)
		{
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		}
		else
		{
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(surfaceCallback);
		}
	}
	
	private void initCamera(SurfaceHolder surfaceHolder)
	{
		if (surfaceHolder == null)
		{
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen())
		{
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try
		{
			cameraManager.openDriver(surfaceHolder);
			
			//cameraManager.setTorch(false);// FLASH!
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null)
			{
				//handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet);
				handler = new CaptureActivityHandler(this, null, null, null);
			}
			decodeOrStoreSavedBitmap(null, null);
		}
		catch (IOException ioe)
		{
			Log.w(TAG, ioe);
//			displayFrameworkBugMessageAndExit();
		}
		catch (RuntimeException e)
		{
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
//			displayFrameworkBugMessageAndExit();
		}
	}
	
	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result)
	{
		// Bitmap isn't used yet -- will be used soon
		if (handler == null)
		{
			savedResultToShow = result;
		}
		else
		{
			if (result != null)
			{
				savedResultToShow = result;
			}
			if (savedResultToShow != null)
			{
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}
	
	private class SurfaceCallback implements SurfaceHolder.Callback
	{
		public SurfaceCallback(){}

		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			if (holder == null)
			{
				Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
			}
			if (!hasSurface)
			{
				hasSurface = true;
				initCamera(holder);
			}
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			hasSurface = false;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 *
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor)
	{
//		inactivityTimer.onActivity();
//		lastResult = rawResult;
//		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		boolean fromLiveScan = barcode != null;
		if (fromLiveScan)
		{
//			historyManager.addHistoryItem(rawResult, resultHandler);
//			// Then not from history, so beep/vibrate and we have an image to
//			// draw on
//			beepManager.playBeepSoundAndVibrate();
			drawResultPoints(barcode, scaleFactor, rawResult);
		}
		
		if(rawResult != null)
		{
			if(callback != null)
			{
				callback.onBarcodeDetection(rawResult.getText());
			}
			Log.d(TAG, "handleDecode barcode => " + rawResult.getText());
		}
	}
	
	private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult)
	{
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0)
		{
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(parent.getResources().getColor(R.color.result_points));
			if (points.length == 2)
			{
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			}
			else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13))
			{
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			}
			else
			{
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points)
				{
					if (point != null)
					{
						canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
					}
				}
			}
		}
	}
	
	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor)
	{
		if (a != null && b != null)
		{
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}
	
	private static class CaptureActivityHandler extends Handler
	{
		private State			state;
		private DecodeThread 	decodeThread	= null;
		private BarcodeDecoderObject	parent          = null;

		public CaptureActivityHandler
		(
			BarcodeDecoderObject             	_parent,
			Collection<BarcodeFormat> 	decodeFormats, 
			Map<DecodeHintType, ?> 		baseHints, 
			String 						characterSet 
		)
		{
			parent = _parent;
			decodeThread = parent.new DecodeThread(parent, decodeFormats, baseHints, characterSet, new ViewfinderResultPointCallback(parent.viewfinderView));
			
			decodeThread.start();
			state = State.SUCCESS;

			// Start ourselves capturing previews and decoding.
			parent.cameraManager.startPreview();
			
			restartPreviewAndDecode();
		}

		@Override
		public void handleMessage(Message message)
		{
			Log.d(TAG, "handleMessage");
			
			switch (message.what)
			{
				case R.id.restart_preview:
					Log.d(TAG, "handleMessage restart_preview");
					restartPreviewAndDecode();
					break;
				case R.id.decode_succeeded:
					Log.d(TAG, "handleMessage decode_succeeded");
					state = State.SUCCESS;
					Bundle bundle = message.getData();
					Bitmap barcode = null;
					float scaleFactor = 1.0f;
					if (bundle != null)
					{
						byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
						if (compressedBitmap != null)
						{
							barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
							// Mutable copy:
							barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
						}
						scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
					}
					parent.handleDecode((Result) message.obj, barcode, scaleFactor);
					break;
				case R.id.decode_failed:
					Log.d(TAG, "handleMessage decode_failed");
					// We're decoding as fast as possible, so when one decode fails,
					// start another.
					state = State.PREVIEW;
					parent.cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
					break;
				case R.id.return_scan_result:
					Log.d(TAG, "handleMessage return_scan_result");
//					activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
//					activity.finish();
					break;
				case R.id.launch_product_query:
					String url = (String) message.obj;
					Log.d(TAG, "handleMessage launch_product_query url=" + url);
		
				default: Log.d(TAG, "handleMessage ??? message.what=" + message.what);
			}
		}

		public void quitSynchronously()
		{
			state = State.DONE;
			parent.cameraManager.stopPreview();
			Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
			quit.sendToTarget();
			try
			{
				// Wait at most half a second; should be enough time, and onPause()
				// will timeout quickly
				decodeThread.join(500L);
			}
			catch (InterruptedException e)
			{
				// continue
			}

			// Be absolutely sure we don't send any queued up messages
			removeMessages(R.id.decode_succeeded);
			removeMessages(R.id.decode_failed);
		}

		private void restartPreviewAndDecode()
		{
			if (state == State.SUCCESS)
			{
				state = State.PREVIEW;
				parent.cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
				parent.viewfinderView.drawViewfinder();
			}
		}
	}
	
	class DecodeThread extends Thread
	{
		public static final String BARCODE_BITMAP        = "barcode_bitmap";
		public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

		private Map<DecodeHintType, Object>	hints;
		private Handler						handler;
		private CountDownLatch 				handlerInitLatch;
		private BarcodeDecoderObject				parent = null;

		DecodeThread
		(
			BarcodeDecoderObject				_parent,
			Collection<BarcodeFormat> 	decodeFormats, 
			Map<DecodeHintType, ?>		baseHints, 
			String						characterSet, 
			ResultPointCallback			resultPointCallback
		)
		{
			parent              = _parent;
			handlerInitLatch	= new CountDownLatch(1);

			hints = new EnumMap<>(DecodeHintType.class);
			if (baseHints != null)
			{
				hints.putAll(baseHints);
			}

			// The prefs can't change while the thread is running, so pick them up
			// once here.
			if (decodeFormats == null || decodeFormats.isEmpty())
			{
				decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
				
				decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
				decodeFormats.addAll(EnumSet.of(BarcodeFormat.DATA_MATRIX));
				
//					decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
//					decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
//					decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
//					decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
//					decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS);
//					decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS);
			}
			hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

			if (characterSet != null)
			{
				hints.put(DecodeHintType.CHARACTER_SET, characterSet);
			}
			hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
			Log.i(TAG, "Hints: " + hints);
		}

		Handler getHandler()
		{
			try
			{
				handlerInitLatch.await();
			}
			catch (InterruptedException ie)
			{
				// continue?
			}
			return handler;
		}

		@Override
		public void run()
		{
			Looper.prepare();
			handler = new DecodeHandler(parent, hints);
			handlerInitLatch.countDown();
			Looper.loop();
		}
	}
	
	private static class DecodeHandler extends Handler
	{
		private MultiFormatReader 		multiFormatReader	= null;
		private boolean					running       		= true;
		private CameraManager			cameraManager 		= null;
		private BarcodeDecoderObject	parent              = null;

		public DecodeHandler(BarcodeDecoderObject _parent, Map<DecodeHintType, Object> hints)
		{
			parent = _parent;
			multiFormatReader 	= new MultiFormatReader();
			
			multiFormatReader.setHints(hints);
			
			cameraManager = _parent.cameraManager;
		}

		@Override
		public void handleMessage(Message message)
		{
			if (!running)
			{
				return;
			}
			
			switch (message.what)
			{
				case R.id.decode:
					decode((byte[]) message.obj, message.arg1, message.arg2);
					break;
				case R.id.quit:
					running = false;
					Looper.myLooper().quit();
					break;
			}
		}

		/**
		 * Decode the data within the viewfinder rectangle, and time how long it
		 * took. For efficiency, reuse the same reader objects from one decode to
		 * the next.
		 *
		 * @param data
		 *            The YUV preview frame.
		 * @param width
		 *            The width of the preview frame.
		 * @param height
		 *            The height of the preview frame.
		 */
		private void decode(byte[] data, int width, int height)
		{
			long start = System.currentTimeMillis();
			Result rawResult = null;
			PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data, width, height);
			
			if (source != null)
			{
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				try
				{
					rawResult = multiFormatReader.decodeWithState(bitmap);
				}
				catch (ReaderException re)
				{
					// continue
				}
				finally
				{
					multiFormatReader.reset();
				}
			}

			if (rawResult != null)
			{
				// Don't log the barcode contents for security.
				long end = System.currentTimeMillis();
				Log.d(TAG, "Found barcode in " + (end - start) + " ms");
				if (parent.handler != null)
				{
					Message message = Message.obtain(parent.handler, R.id.decode_succeeded, rawResult);
					Bundle bundle = new Bundle();
					bundleThumbnail(source, bundle);
					message.setData(bundle);
					message.sendToTarget();
				}
			}
			else
			{
				if (parent.handler != null)
				{
					Message message = Message.obtain(parent.handler, R.id.decode_failed);
					message.sendToTarget();
					
				}
			}
		}

		private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle)
		{
			int[] pixels = source.renderThumbnail();
			int width = source.getThumbnailWidth();
			int height = source.getThumbnailHeight();
			Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
			bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
			bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
		}
	}
	
	public void onPause()
	{
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		
		if(cameraManager != null) cameraManager.closeDriver();
		
		if (!hasSurface && surfaceView != null)
		{
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(surfaceCallback);
		}
		
		if(ambientLightManager != null) ambientLightManager.stop();
	}

	public void setOff()
	{
		if(viewfinderView != null) viewfinderView.setVisibility(View.INVISIBLE);
		if(surfaceView != null) surfaceView.setVisibility(View.INVISIBLE);
	}

	public void setOn()
	{
		if(surfaceView != null) surfaceView.setVisibility(View.VISIBLE);
		if(viewfinderView != null) viewfinderView.setVisibility(View.VISIBLE);
	}
}
