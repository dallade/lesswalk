package com.lesswalk.bases;

import android.text.Editable;

public interface ILesswalkService
{
	IContactManager getContactManager();

	String unzip(String path);

	boolean haveLocalNumber();

	int setLocalNumberAndName(String number, String firstName, String lastName, ISetLocalNumberCallback callback);

	String getLocalNumber();

	interface ISetLocalNumberCallback
	{
		void onSuccess();
		void onError(int errorID);
	}

	public static int REGISTRATION_ERROR_STILL_NOT_REGISTRED = -1;
	public static int REGISTRATION_ERROR_FILE_SYSTEM         = -2;
	public static int REGISTRATION_ERROR_DOWNLOAD_SIGNATURES = -3;
	public static int REGISTRATION_ERROR_SMS_STATE_NOT_READY = -4;
}
