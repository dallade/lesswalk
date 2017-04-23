package com.lesswalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.google.zxing.android.ViewfinderView;
import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.bases.ContactSignature.SignatureType;
import com.lesswalk.contact_page.navigation_menu.ContactSignatureSlideLayout;
import com.lesswalk.contact_page.navigation_menu.NavigatiomMenuSurface;
import com.lesswalk.contact_page.navigation_menu.barcode.BarcodeDecoderObject;
import com.lesswalk.contact_page.navigation_menu.barcode.BarcodeDecoderObject.BarcodeDetectorCallback;

import java.util.Vector;

public class MainActivity extends BaseActivity 
{
	public static final String TAG = "lesswalkMainActivity";

    public void goToLoginPage(View view)//TODO remove later
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private enum MODE {CONTACT_CARUSSEL_MODE, QR_DETECTOR_MODE};

	private NavigatiomMenuSurface       navigationMenuGL = null;
	private SearchView                  searchFilter     = null;
	private BarcodeDecoderObject        barcodeObject    = null;
	private ContactSignatureSlideLayout signatureSlider  = null;
	private ImageButton                 qrcodeButton     = null;
	private MODE                        currentMode      = MODE.CONTACT_CARUSSEL_MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy);

		barcodeObject	= new BarcodeDecoderObject
		(
			this,
			(ViewfinderView) findViewById(R.id.contact_act_barcodefinder),  
			(SurfaceView) findViewById(R.id.contact_act_preview_view)
		);
        
        barcodeObject.setCallback(barcodeDetectorCallback);
        
        searchFilter    = (SearchView) findViewById(R.id.contact_search_filter);
        signatureSlider = (ContactSignatureSlideLayout) findViewById(R.id.contact_act_signatures_slider);

		signatureSlider.setCallback(new ContactSignatureSlideLayout.IContactSignatureSliderCallback()
		{
			@Override
			public void onSignatureClicked(String path)
			{
				if(path == null || path.length() <= 0 || path.equals("null"))
				{
					startActivity(new Intent(MainActivity.this, EditorActivity.class));
				}
				else
				{
					String dirPath = null;
					if(path != null && (dirPath=getService().unzip(path)) != null)
					{
						Intent i = new Intent(MainActivity.this, PlayerActivity.class);
						//
						i.putExtra("content_dir", dirPath);
						startActivity(i);
					}
				}
			}
		});

		qrcodeButton	= (ImageButton) findViewById(R.id.contact_act_qrcode_bt);
        
        qrcodeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setMode(MODE.QR_DETECTOR_MODE);
			}
		});
        
        searchFilter.setOnQueryTextListener(new OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String text)
			{
				if(navigationMenuGL != null)
				{
					navigationMenuGL.setContactFilter(text);
				}
				return false;
			}
		});
    }
    
    private BarcodeDetectorCallback barcodeDetectorCallback = new BarcodeDetectorCallback()
	{
		@Override
		public void onBarcodeDetection(String text)
		{
			if(text.startsWith("lesswalk://"))
			{
				int ret = signatureSlider.addContactSignature(new ContactSignature("", SignatureType.PARK, text));
				// TODO move this to MainService
				if(ret == -1)
				{
					Toast.makeText(MainActivity.this, "This signature already existed!", Toast.LENGTH_SHORT).show();
				}
			}
			else Toast.makeText(MainActivity.this, "Not Lesswalk signature: " + text, Toast.LENGTH_SHORT).show();
			setMode(MODE.CONTACT_CARUSSEL_MODE);
		}
	};
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	setMode(MODE.CONTACT_CARUSSEL_MODE);

		if(signatureSlider != null) signatureSlider.resume();
    }
    
	private void setMode(final MODE mode)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				switch (mode)
				{
					case CONTACT_CARUSSEL_MODE:
					{
						if(barcodeObject != null)
						{
							barcodeObject.setOff();
							barcodeObject.onPause();
						}
						if(navigationMenuGL != null) navigationMenuGL.setOn();
						if(qrcodeButton != null)
						{
							qrcodeButton.setVisibility(View.VISIBLE);
							qrcodeButton.bringToFront();
						}
						currentMode = mode;
						break;
					}
					case QR_DETECTOR_MODE:
					{
						if(barcodeObject != null)
						{
							barcodeObject.resume();
							barcodeObject.setOn();
						}
						if(navigationMenuGL != null) navigationMenuGL.setOff();
						if(qrcodeButton != null) qrcodeButton.setVisibility(View.INVISIBLE);
						currentMode = mode;
						break;
					}
					default: break;
				}
			}
		});
	}

	@Override
	protected void mainServiceConnected() 
	{
		Vector<ContactSignature> signatures = new Vector<ContactSignature>();
		RelativeLayout navigationSurfaceScreen = (RelativeLayout) findViewById(R.id.navigation_surface_screen);
		
        navigationMenuGL        = new NavigatiomMenuSurface(this);
        navigationMenuGL.initiation();
		navigationMenuGL.setContactManager(getService().getContactManager());
		navigationSurfaceScreen.addView(navigationMenuGL);

		getService().getContactManager().fillSignaturesByPhoneNumber(getService().getLocalNumber(), signatures);

		for(ContactSignature cs:signatures)
		{
			signatureSlider.addContactSignature(cs);
		}

		setMode(currentMode);
	}
	
	@Override
	protected void onPause()
	{
		signatureSlider.pause();
		barcodeObject.onPause();
		super.onPause();
	}
	
	@Override
	public void onBackPressed()
	{
		String filter = null;

		if(currentMode == MODE.QR_DETECTOR_MODE)
		{
			setMode(MODE.CONTACT_CARUSSEL_MODE);
			return;
		}
		else if((filter=navigationMenuGL.getContactFilter()) != null && filter.length() > 0)
		{
			navigationMenuGL.setContactFilter("");
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					searchFilter.setQuery("", true);
				}
			});

			return;
		}

		super.onBackPressed();
	}
}
