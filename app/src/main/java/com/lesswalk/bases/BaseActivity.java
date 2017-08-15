package com.lesswalk.bases;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.lesswalk.system.MainService;
import com.lesswalk.RegistrationActivity;

public abstract class BaseActivity extends FragmentActivity
{
	protected static final String PROP_NAVIGATION_APP_PACKAGE = "navigation_app_package";

	public static final String INTENT_EXTRA_NAME_CONTENT_DIR    = "content_dir";
	public static final String INTENT_EXTRA_NAME_SIGNATURE_UUID = "signature_uuid";
	public static final String INTENT_EXTRA_NAME_SPOT_NAME      = "spot_name";
	public static final String INTENT_EXTRA_NAME_ICON_UUID      = "icon_uuid";
	public static final String INTENT_EXTRA_NAME_TITLE          = "title";

	private static final String           TAG           = "lesswalkBaseActivity";
    private static       Intent           mainActivity  = null;
	private              ILesswalkService mainServer    = null;
	private              boolean          permissionsOK = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		if(mainActivity == null)
		{
			mainActivity = new Intent(getIntent());
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			requestPermissions(new String[]
					{
							Manifest.permission.INTERNET,
							Manifest.permission.READ_CONTACTS,
							Manifest.permission.CAMERA,
							Manifest.permission.READ_EXTERNAL_STORAGE,
							Manifest.permission.WRITE_EXTERNAL_STORAGE
					}, 1);
		}
		else
		{
			permissionsOK = true;
		}

		Log.d("lesswalk", "BaseActivity - onCreate");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		Log.d("elazarkin9", "onRequestPermissionsResult");

		switch (requestCode)
		{
			case 1:
			{
				for(int i = 0; i < permissions.length; i++)
				{
					// If request is cancelled, the result arrays are empty.
					if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
					{
						Log.d("elazarkin9", "onRequestPermissionsResult permission" + permissions[i]+ " is oK");
					}
					else
					{
						Log.d("elazarkin9", "problem with permission");
						Toast.makeText(this, "application must have all permissions!", Toast.LENGTH_LONG).show();
						finish();
					}
				}

				if(!permissionsOK)
				{
					permissionsOK = true;
					connectToService();
				}
			}
		}
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

		if(permissionsOK)
		{
			connectToService();
		}
	}
	
	private void connectToService() 
	{
		Log.d(TAG, "connectToService() mainServer=" + mainServer);
		if(!isMyServiceRunning(this))
		{
			startService(new Intent(BaseActivity.this, MainService.class));
			android.util.Log.d(TAG, "BaseActivity: MainService.class started! ");
		}
		bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	protected void onPause()
	{
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
			Log.d(TAG, "mainServer = " + mainServer + " is registration - " + (BaseActivity.this instanceof RegistrationActivity));

			if(!(BaseActivity.this instanceof RegistrationActivity) && !mainServer.checkLogin())
			{
				Log.d("elazarkin9", "BaseActivity.this instanceof RegistrationActivity is " + (BaseActivity.this instanceof RegistrationActivity) + " mainServer.checkLogin() is " + mainServer.checkLogin());
				startActivity(new Intent(BaseActivity.this, RegistrationActivity.class));
			}
			else
			{
				Log.d("elazarkin9", "BaseActivity.this instanceof RegistrationActivity is " + (BaseActivity.this instanceof RegistrationActivity) + " mainServer.checkLogin() is " + mainServer.checkLogin());
				mainServiceConnected();
			}
		}
	};
	
	public ILesswalkService getService()
	{
		return mainServer;
	}
	
	protected abstract void mainServiceConnected();
}
