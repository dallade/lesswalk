package com.lesswalk.editor_pages.bases;

import android.graphics.Bitmap;
import android.net.Uri;

import com.lesswalk.maps.MapData;

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

	public interface EditObjectVideoTipCallback
	{
		void onReturn(String videoUri);
	}


	public interface MapListener
	{
		void onResult(MapData mapData);

		MapData getMapData();
	}
}
