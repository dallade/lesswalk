package com.lesswalk.bases;

import android.text.Editable;

import com.lesswalk.database.AWS;

public interface ILesswalkService
{
	IContactManager getContactManager();

	String unzip(String path);

	boolean haveLocalNumber();

//	int setLocalNumber(String number, ISetLocalNumberCallback callback);
//	int setLocalNumber(String name, String number, ISetLocalNumberCallback callback);

	boolean checkIfUserExist(String number);

	int syncContactSignatures(String number, ISetLocalNumberCallback callback);

	String getLocalNumber();

    void downloadUserJsonIfNeed(String number);

    void sendVerificationSms(String number, String code);

    String getUserFirstName();

    String getUserLastName();

	void updateUserJson(String first, String last, String number);

    String uuidToPath(String uuid);

	String generateUUID();

    void deleteUserAccount(AWS.OnRequestListener onRequestListener);

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
