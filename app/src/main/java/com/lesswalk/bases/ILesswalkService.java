package com.lesswalk.bases;

import android.text.Editable;

public interface ILesswalkService
{
	IContactManager getContactManager();

	String unzip(String path);

	boolean haveLocalNumber();

	int setLocalNumber(String number, ISetLocalNumberCallback callback);

	String getLocalNumber();

	interface ISetLocalNumberCallback
	{
		void onSuccess();
		void onError(int errorID);
		void onProgress(String path);
		void notSuccessFinish();
	}

	public static int REGISTRATION_ERROR_STILL_NOT_REGISTRED = -1;
	public static int REGISTRATION_ERROR_FILE_SYSTEM         = -2;
	public static int REGISTRATION_ERROR_DOWNLOAD_SIGNATURES = -3;
	public static int CODE_ERROR_METADATA_NOT_CREATED        = -4;
	public static int CODE_ERROR_STRANGE_METADATA_ITEM       = -5;
}
