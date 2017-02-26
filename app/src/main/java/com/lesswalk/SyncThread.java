package com.lesswalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

public class SyncThread
{
    private static final String TAG = "SyncThread";
    private static final String SHARED_PREFS_NAME = "lesswalk_shared_prefs";
    private final MainService mParent;
    private final Cloud mCloud;
    private Vector<CarusselContact> contacts;

    public SyncThread(MainService parent)
	{
        mParent = parent;
        mCloud = new AmazonCloud(mParent);
	}

	// TODO update databases
	// TODO update 
	
	public void start() 
	{
		// Start threads:
		Thread syncThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
                ArrayList<String> localContactsPhones = updateLocalContactsPhones();
			}
		});
		syncThread.start();
	}

    /*
     * Sync Module API Methods
     */

    /**
     * Get Locally stored phone numbers
     * @return
     */
    synchronized protected ArrayList<String> updateLocalContactsPhones() {
        ArrayList<String> uuidsList = new ArrayList<>();
        contacts = new Vector<>();
        SharedPreferences prefs = mParent.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        mParent.getContactManager().fillContactVector(contacts);
        for (CarusselContact c: contacts) {
            String originalPhone = c.getNumber();
            String localContact = prefs.getString(originalPhone, null);
            String userUuid;
            if (localContact == null){
                String[] phoneNumber = splitPhoneNumber(originalPhone);
                if (phoneNumber == null) continue;
                userUuid = mCloud.getUserUuid(
                        phoneNumber[MainService.PHONE_INDEX_COUNTRY]
                        , phoneNumber[MainService.PHONE_INDEX_MAIN]
                );
                if (userUuid == null || userUuid.equals("")) continue;
                localContact =
                        userUuid + "," +
                        phoneNumber[MainService.PHONE_INDEX_COUNTRY] + "," +
                        phoneNumber[MainService.PHONE_INDEX_MAIN];
                editor.putString(originalPhone, localContact);
            }else{
                String[] fields = localContact.split(",");
                String localUuid = fields[0];
                if (localUuid == null || localUuid.equals("")){
                    Log.e(TAG, "Bad local contact fields");
                    continue;
                }
                userUuid = mCloud.getUserUuid(
                        fields[2]
                        , fields[1]
                );
                if (userUuid == null || userUuid.equals("")){
                    Log.d(TAG, "The user {"+userUuid+","+fields[1]+","+fields[2]+"} has removed the profile");
                    editor.remove(originalPhone);
                }else if(!userUuid.equals(localUuid)) {
                    Log.d(TAG, "The user {"+userUuid+","+fields[1]+","+fields[2]+"} has changed the profile (used to be "+localUuid+")");
                    editor.remove(originalPhone);
                    String[] phoneNumber = splitPhoneNumber(originalPhone);
                    if (phoneNumber == null){
                        Log.e(TAG, "Bad phoneNumber - the splitPhoneNumber method has probably changed since last time");
                        continue;
                    }
                    localContact =
                            userUuid + "," +
                            phoneNumber[MainService.PHONE_INDEX_COUNTRY] + "," +
                            phoneNumber[MainService.PHONE_INDEX_MAIN];
                    editor.putString(originalPhone, localContact);
                }
            }
            uuidsList.add(userUuid);
        }
        editor.commit();
        return uuidsList;
    }

    private String[] splitPhoneNumber(String originalNumber) {
        String LOCAL_COUNTRY_CODE = "+972";
        String[] parts = new String[2];
        String input = originalNumber.replaceAll(" ", "");
        if (input.startsWith("0")){
            parts[MainService.PHONE_INDEX_COUNTRY] = LOCAL_COUNTRY_CODE;
            parts[MainService.PHONE_INDEX_MAIN] = input;
        }else {
            if (input.startsWith(LOCAL_COUNTRY_CODE)){
                parts[MainService.PHONE_INDEX_COUNTRY] = LOCAL_COUNTRY_CODE;
                parts[MainService.PHONE_INDEX_MAIN] = input.substring(
                        parts[MainService.PHONE_INDEX_COUNTRY].length()
                );
                if (!parts[MainService.PHONE_INDEX_MAIN].startsWith("0")){
                    parts[MainService.PHONE_INDEX_MAIN] = "0"+parts[MainService.PHONE_INDEX_MAIN];
                }
            }else return null;
        }
        return parts;
    }


}
