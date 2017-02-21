package com.lesswalk.editor_pages.bases;

import android.graphics.Bitmap;

public class EditManagerCallbacks 
{
	public interface EditObjectAddressCallback
	{
		void onReturn(String contry, String city, String street, String street_num);
	}
	
	public interface EditObjectTextTipCallback
	{
		void onReturn(String text);
	}
	
	public interface EditObjectPhotoTipCallback
	{
		void onReturn(Bitmap photo);
	}
}
