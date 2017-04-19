package com.lesswalk.bases;

import android.text.Editable;

public interface ILesswalkService
{
	IContactManager getContactManager();

	String unzip(String path);

	boolean haveLocalNumber();

	int setLocalNumber(Editable text, ISetLocalNumberCallback iSetLocalNumberCallback);

	interface ISetLocalNumberCallback
	{
		void onSuccess();
		void onError(int errorID);
	}
}
