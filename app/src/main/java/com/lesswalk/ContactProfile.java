package com.lesswalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.contact_page.navigation_menu.ContactSignatureSlideLayout;
import com.lesswalk.editor_pages.bases.ImageView;

import java.util.Vector;

public class ContactProfile extends BaseActivity 
{
	private TextView                    contact_profile_name_tv = null;
	private ContactSignatureSlideLayout contactSignatureLayout  = null;
	private ContactSignatureSlideLayout userSignatureLayout     = null;
	private ImageView                   sendMessage             = null;
	private ImageView                   callNumber              = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_profile);
		
		contactSignatureLayout  = (ContactSignatureSlideLayout) findViewById(R.id.contact_profile_visit_slider);
		userSignatureLayout     = (ContactSignatureSlideLayout) findViewById(R.id.contact_profile_invite_slider);
		contact_profile_name_tv = (TextView) findViewById(R.id.contact_profile_name_tv);
		
		contact_profile_name_tv.setText(getIntent().getExtras().getString("contact_name", "No Name"));
	}
	@Override
	protected void mainServiceConnected() 
	{
		Vector<ContactSignature> signatures = new Vector<ContactSignature>();
		
		getService().getContactManager().fillSignaturesByPhoneNumber
		(
			getIntent().getExtras().getString("phone_number", "No Number"), 
			signatures
		);

        Log.d("elazarkin", "signatures_size=" + signatures.size());

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
