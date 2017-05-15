package com.lesswalk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.contact_page.navigation_menu.ContactSignatureSlideLayout;
import com.lesswalk.views.RoundedButtonWithText;

import java.util.Vector;

public class ContactProfile extends BaseActivity
{
	private RelativeLayout              screen                  = null;
	private TextView                    contact_profile_name_tv = null;
	private ContactSignatureSlideLayout contactSignatureLayout  = null;
	private ContactSignatureSlideLayout userSignatureLayout     = null;
	private RoundedButtonWithText       sendMessage             = null;
	private RoundedButtonWithText       callNumber              = null;
	private LinearLayout                shareMenu               = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_profile);

		screen = (RelativeLayout) findViewById(R.id.contact_profile_screen);

		contactSignatureLayout = (ContactSignatureSlideLayout) findViewById(R.id.contact_profile_visit_slider);
		userSignatureLayout = (ContactSignatureSlideLayout) findViewById(R.id.contact_profile_invite_slider);
		contact_profile_name_tv = (TextView) findViewById(R.id.contact_profile_name_tv);

		contact_profile_name_tv.setText(getIntent().getExtras().getString("contact_name", "No Name"));

		sendMessage = (RoundedButtonWithText) findViewById(R.id.contact_profile_send_message);
		callNumber = (RoundedButtonWithText) findViewById(R.id.contact_profile_call_number);

		sendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", getIntent().getExtras().getString("phone_number", "No Number"), null)));
			}
		});

		setShareMenu();

		callNumber.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getIntent().getExtras().getString("phone_number", "No Number")));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				{
					if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    {
						Toast.makeText(ContactProfile.this, "you need add permission of CALL_PHONE first", Toast.LENGTH_LONG).show();
                        return;
                    }
				}
				startActivity(intent);
			}
		});
	}

	private void setShareMenu()
	{
		Button inviteBt = null;
		Button shareBt  = null;
		Button editBt   = null;
		Button cancelBt = null;

		shareMenu = (LinearLayout) ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.share_contact_menu, null);
		shareMenu.setVisibility(View.GONE);

		screen.addView(shareMenu, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

		inviteBt = (Button) shareMenu.findViewById(R.id.contact_profile_menu_invite);
		shareBt = (Button) shareMenu.findViewById(R.id.contact_profile_menu_share);
		editBt = (Button) shareMenu.findViewById(R.id.contact_profile_menu_edit);
		cancelBt = (Button) shareMenu.findViewById(R.id.contact_profile_menu_cancel);

		inviteBt.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				// TODO check if number existed, if not - send sms with request download app. Anyway add task of invite
				//https://play.google.com/store/apps/details?id=com.lesswalk
				//https://itones.apple.com/app/lesswalk/id1025069015?mt=8
			}
		});

		cancelBt.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				shareMenu.setVisibility(View.GONE);
			}
		});
	}

	@Override
	protected void mainServiceConnected() 
	{
		setContactSignatures();
		setUserSignatures();
	}

	private void setUserSignatures()
	{
		Vector<ContactSignature> signatures = new Vector<ContactSignature>();

		getService().getContactManager().fillSignaturesByPhoneNumber(getService().getLocalNumber(), signatures);

		userSignatureLayout.setCallback(new ContactSignatureSlideLayout.IContactSignatureSliderCallback()
		{
			@Override
			public void onSignatureClicked(String path)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						shareMenu.setVisibility(View.VISIBLE);
						shareMenu.bringToFront();
					}
				});
			}
		});

		for(ContactSignature c:signatures)
		{
			userSignatureLayout.addContactSignature(c);
			Log.d("elazarkin", "add signature" + c.getSignutarePath());
		}
	}

	private void setContactSignatures()
	{
		Vector<ContactSignature> signatures = new Vector<ContactSignature>();

		getService().getContactManager().fillSignaturesByPhoneNumber
		(
				getIntent().getExtras().getString("phone_number", "No Number"),
				signatures
		);

		contactSignatureLayout.setCallback(new ContactSignatureSlideLayout.IContactSignatureSliderCallback()
		{
			@Override
			public void onSignatureClicked(String path)
			{
				String dirPath = null;
				if(path != null && (dirPath=getService().unzip(path)) != null)
				{
					Intent i = new Intent(ContactProfile.this, PlayerActivity.class);
					//
					i.putExtra("content_dir", dirPath);
					startActivity(i);
				}
			}
		});

		for(ContactSignature c:signatures)
		{
			contactSignatureLayout.addContactSignature(c);
			Log.d("elazarkin", "add signature" + c.getSignutarePath());
		}
	}

	@Override
	protected void onPause()
	{
		if(contactSignatureLayout != null)
		{
			contactSignatureLayout.pause();
		}

		if(userSignatureLayout != null)
		{
			userSignatureLayout.pause();
		}

		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if(contactSignatureLayout != null)
		{
			contactSignatureLayout.resume();
		}

		if(userSignatureLayout != null)
		{
			userSignatureLayout.resume();
		}

	}
}
