package com.lesswalk;

import java.util.Vector;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.contact_page.navigation_menu.ContactSignatureSlideLayout;

public class ContactProfile extends BaseActivity 
{
	private TextView                    contact_profile_name_tv = null;
	private ContactSignatureSlideLayout contactSignatureLayout  = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_profile);
		
		contactSignatureLayout = (ContactSignatureSlideLayout) findViewById(R.id.contact_profile_visit_slider);
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
		
		for(ContactSignature c:signatures)
		{
			contactSignatureLayout.addContactSignature(c);
            Log.d("elazarkin", "add signature" + c.getSignutarePath());
        }
	}
}
