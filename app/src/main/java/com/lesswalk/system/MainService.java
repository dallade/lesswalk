package com.lesswalk.system;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.lesswalk.bases.ContactSignature;
import com.lesswalk.bases.IContactManager;
import com.lesswalk.bases.ILesswalkService;
import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.ZipManager;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class MainService extends Service implements ILesswalkService
{
	private static final String         TAG            = "lesswalk_MainService";
	private static       Semaphore      mutex          = new Semaphore(1);
	private              IBinder        mBinder        = new LocalBinder();
	private              ContactManager contactManager = null;
	private              SyncThread     syncThread     = null;

    @Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}
	
	public class LocalBinder extends Binder
	{
		public MainService getService()
		{
			return MainService.this;
		}
	}
	
	@Override
	public void onCreate() 
	{
		super.onCreate();

		Log.d(TAG, "MainService onStartCommand!");

		contactManager = new ContactManager(this);

		contactManager.startLoadContacts();

		syncThread = new SyncThread(this);

		syncThread.start();
	}

	private void onFinishLoadContacts()
	{
//		syncThread.start();
	}

	private class ContactManager implements IContactManager
	{
		private List<CarusselContact> contacts             = null;
		private ContentResolver       cr                   = null;

		protected ContactManager(Context context) 
		{
			cr = context.getContentResolver();
		}
		
		protected void startLoadContacts()
		{
			new HandlerThread("load contacts thread")
			{
				public void run() 
				{
					loadContacts();
				};
			}.start();
		}
		
		private Comparator<CarusselContact> StringComparator = new Comparator<CarusselContact>() 
    	{
			@Override
			public int compare(CarusselContact a, CarusselContact b) 
			{
				return a.getName().compareTo(b.getName());
			}
		};
		
		private void loadContacts()
	    {
	        
	        int columnIndexID     = 0;
	        int columnIndexName   = 0;
	        int columnIndexhasNum = 0;
	        int columnIndexNumber = 0;
	        Uri uri               = null;
	        Cursor cursor         = null;
	        
	        String ID             = null;
	        String phone_number   = null;
//	        String DISPLAY_NAME   = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?  ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;
//	        String PROJECTION[] =
//	        {
//	                ContactsContract.Contacts._ID,
//	                DISPLAY_NAME
//	        };
	        
	        String[] PROJECTION = 
        	{
    			ContactsContract.Contacts._ID, 
    			ContactsContract.Contacts.DISPLAY_NAME, 
    			ContactsContract.Contacts.HAS_PHONE_NUMBER,
			};
	        
	        if(contacts == null) contacts = new Vector<CarusselContact>();

	        cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, null, null, null);
	        
	        if(cursor == null)
	        {
	        	Log.d(TAG, "some problem - cursor = null");
	        	return;
	        }
	        else if(cursor.getCount() <= 0)
	        {
	        	Log.d(TAG, "some problem - cursor.getCount()=" + cursor.getCount());
	        	return;
	        }
	        else Log.d(TAG, "cursor is Ok - cursor.getCount()=" + cursor.getCount());
	        
	        cursor.moveToFirst();

	        while (cursor.moveToNext())
	        {
	            columnIndexID     = cursor.getColumnIndex(ContactsContract.Contacts._ID);
	            columnIndexName   = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
	            columnIndexhasNum = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
	            
	            if(!cursor.getString(columnIndexhasNum).equals("0"))
	            {
	            	ID = cursor.getString(columnIndexID);
		            uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(ID));
		            phone_number = getPhoneNumber(ID);
		            
		            if(phone_number != null && phone_number.length() > 0 && !phone_number.startsWith("*"))
		            {
			            try {mutex.acquire();} catch (InterruptedException e) {e.printStackTrace();}
			            
			            addContact
			            (
		            		contacts, ContactsContract.Contacts.openContactPhotoInputStream(cr, uri), 
							cursor.getString(columnIndexName), 
							phone_number
	            		);
			            
//			        	Log.d("lesswalk_MainService", String.format
//	        			(
//	    					"loaded contact %s %s  size = %d",
//	    					contacts.get(contacts.size()-1).getName(),
//	    					contacts.get(contacts.size()-1).getNumber(),
//	    					contacts.size()
//						));
			        	
						Collections.sort(contacts, StringComparator);
			        	// TODO order contacts
			        	mutex.release();
		            }
	            }
	        }

			onFinishLoadContacts();
	    }

		private void addContact(List<CarusselContact> contacts, InputStream photoIs, String name, String phone_number) 
		{
			/*
			 * check if already exists
			 * 
			 * TODO - switch to binary search
			 * 
			 * int start   = 0;
			 * int end     = contacts.size()-1;
			 * int current = 0; 
			 */
			
			for(CarusselContact c:contacts)
			{
				if(c.getName().compareTo(name) == 0) return;
			}
			
			contacts.add(new CarusselContact(photoIs, name, phone_number));
		}

		@Override
		public int getContactAmmount() 
		{
			return contacts == null ? 0:contacts.size();
		}

		@Override
		public void fillContactVector(Vector<CarusselContact> contacts) 
		{
			try {mutex.acquire();} catch (InterruptedException e) {e.printStackTrace();}
			
			contacts.removeAllElements();
			contacts.addAll(this.contacts);
			
			mutex.release();
		}

		@Override
		public void fillSignaturesByPhoneNumber(String phoneNumber, Vector<ContactSignature> signatures)
		{
			if (signatures == null) return;

			signatures.removeAllElements();

			// TODO use local databases data

			syncThread.fillSignaturesOfPhoneNumber(phoneNumber, signatures);
		}
	}

	// http://stackoverflow.com/questions/11218845/how-to-get-contacts-phone-number-in-android
	private String getPhoneNumber(String id) 
	{
	    String number = "";
	    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=" + id, null, null);

	    if(phones.getCount() > 0) 
	    {
	        while(phones.moveToNext()) 
	        {
	            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	        }
	        //Log.d(TAG, "number is" + number);
	    }
	    //else Log.d(TAG, "phones.getCount() = " + phones.getCount());

	    phones.close();

	    return number;
	}

	@Override
	public IContactManager getContactManager() 
	{
		return contactManager;
	}

	@Override
	public String unzip(String path)
	{
		File outDir = new File(getCacheDir(), "signature");

		outDir.mkdir();outDir.mkdirs();

		if(ZipManager.unzip(this, path, outDir.getPath()))
		{
			return outDir.getPath();
		}

		return null;
	}

	@Override
	public boolean haveLocalNumber()
	{
		return getLocalNumber() != null;
	}

	@Override
	public int setLocalNumber(String number, ISetLocalNumberCallback callback)
	{
		syncThread.addImportantTask(syncThread.new StoreLocalContactTask(number, callback));
		return 0;
	}

	@Override
	public String getLocalNumber()
	{
		return syncThread.getLocalNumber();
	}
}
