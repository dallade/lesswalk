package com.lesswalk.editor_pages.bases;

import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectAddressCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectPhotoTipCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectTextTipCallback;

public interface EditObjects2dManager 
{
	void getManualAddressText(EditObjectAddressCallback callback, String country, String city, String street, String street_num);

	void getTipText(EditObjectTextTipCallback callback, String tip);
	
	void getTipPhoto(EditObjectPhotoTipCallback callback);
}
