package com.lesswalk.bases;

import com.lesswalk.MainService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public abstract class BaseActivity extends Activity
{
	private static final String           TAG          = "lesswalkBaseActivity";
	private static       Intent           mainActivity = null;
	private              ILesswalkService mainServer   = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		if(mainActivity == null)
		{
			mainActivity = new Intent(getIntent());
		}

		Log.d("lesswalk", "BaseActivity - onCreate");
		connectToService();
	}

	private boolean isMyServiceRunning(Context context)
	{
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	    	//Log.d(TAG, "" + service.service.getClassName());
	        if ("com.lesswallk".equals(service.service.getClassName())) 
	        {
	            return true;
	        }
	    }
	    return false;
	}

	private boolean ifIsSameActivity(Intent i1, Intent i2)
	{
		return i1.getComponent().getClassName().equals(i2.getComponent().getClassName());
	}

	private boolean ifIsMainActivity(Intent i)
	{
		return ifIsSameActivity(mainActivity, i);
	}

	@Override
	public void onBackPressed()
	{
		if(!ifIsMainActivity(getIntent()))
		{
			finish();

			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		connectToService();
	}
	
	private void connectToService() 
	{
		Log.d(TAG, "connectToService() mainServer=" + mainServer);
		if(mainServer == null)
		{
			if(!isMyServiceRunning(this))
			{
				startService(new Intent(BaseActivity.this, MainService.class));
				android.util.Log.d(TAG, "BaseActivity: MainService.class started! ");
			}
			bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
		}
	}


	@Override
	protected void onPause()
	{
		//if(!ifIsMainActivity(getIntent())) finish();

		try
		{
			unbindService(mConnection);
		}
		catch (Exception e){}

		super.onPause();
	}

	private ServiceConnection mConnection = new ServiceConnection() 
	{
		@Override
		public void onServiceDisconnected(ComponentName name) 
		{
			mainServer = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) 
		{
			Log.d(TAG, String.format("service=%s getService=%s", service, ((MainService.LocalBinder) service).getService()));
			mainServer = (ILesswalkService) ((MainService.LocalBinder) service).getService();
			Log.d(TAG, "mainServer = " + mainServer);
			mainServiceConnected();
		}
	};
	
	protected ILesswalkService getService() 
	{
		return mainServer;
	}
	
	protected abstract void mainServiceConnected();
}
