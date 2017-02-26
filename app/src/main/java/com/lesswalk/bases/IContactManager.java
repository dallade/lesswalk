package com.lesswalk.bases;

import java.util.List;
import java.util.Vector;

import com.lesswalk.contact_page.navigation_menu.CarusselContact;

public interface IContactManager 
{
	int getContactAmmount();
	
	void fillContactVector(Vector<CarusselContact> contacts);

	void fillSignaturesByPhoneNumber(String phoneNumber, Vector<ContactSignature> signatures);
}
