package com.lesswalk.bases;

import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.json.CarruselJson;

import java.util.Vector;

public interface IContactManager 
{
	int getContactAmmount();
	
	void fillContactVector(Vector<CarusselContact> contacts);

	void fillSignaturesByPhoneNumber(String phoneNumber, Vector<CarruselJson> signatures);
}
