package com.lesswalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;
import com.lesswalk.utils.PhoneUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SyncThread
{
    private static final String TAG = "SyncThread";
    private static final String SHARED_PREFS_NAME = "lesswalk_shared_prefs";
    private MainService mParent = null;
    private Cloud mCloud = null;
    private Vector<CarusselContact> contacts;
    private DataBaseUpdater dataBaseUpdater = null;

    private boolean isAlive = false;

    public SyncThread(MainService parent)
    {
        mParent = parent;
        mCloud = new AmazonCloud(mParent);
    }

    public void start()
    {
        if(!isAlive)
        {
            isAlive = true;
            (dataBaseUpdater = new DataBaseUpdater(this, mParent)).start();
        }
    }

    public void stop()
    {
        isAlive = false;
        try
        {
            dataBaseUpdater.join();
            dataBaseUpdater = null;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This thread will update data (files and database) by user existed contacts
     *
     * first (by loop of exist numbers) the thread check differences about the number on Amazon cloud and local
     * and update it if need
     *
     * databases:
     *  1 - number, uuid, signatures_uuids (sign1,sign2...)
     *  2 - signature_uuid - type
     *
     * files of signatures will be as zip files
     */

    private class DataBaseUpdater extends Thread
    {
        private MainService service = null;
        private SyncThread parent = null;

        public DataBaseUpdater(SyncThread _parent, MainService _service)
        {
            parent  = _parent;
            service = _service;
        }
        @Override
        public void run()
        {
            long MIN_LOOP_TIME = 3*60*1000;

            Vector<CarusselContact> contacts = new Vector<CarusselContact>();

            while(isAlive)
            {
                long t0 = System.currentTimeMillis();
                long tdiff = 0L;
                long t2sleep = 0L;

                contacts.removeAllElements();
                service.getContactManager().fillContactVector(contacts);

                for(CarusselContact c:contacts)
                {
                    updateContactIfNeed(c);
                }

                tdiff = System.currentTimeMillis() - t0;
                t2sleep = MIN_LOOP_TIME - tdiff;

                if(t2sleep >= 100)
                {
                    try {sleep(t2sleep);} catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }

        private void updateContactIfNeed(CarusselContact c)
        {
            String       number[]      = PhoneUtils.splitPhoneNumber(c.getNumber());
            String       userUuid      = null;
            List<String> sinaturesList = null;

            if (number == null) return;

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null) return;

            sinaturesList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " uuid:" + userUuid);
        }
    }

    /*
     * Sync Module API Methods
     */

    /**
     * Get Locally stored phone numbers
     *
     * @return
     */
//    synchronized protected ArrayList<String> updateLocalContactsPhones()
//    {
//        ArrayList<String> uuidsList = new ArrayList<>();
//        contacts = new Vector<>();
//        SharedPreferences prefs = mParent.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        mParent.getContactManager().fillContactVector(contacts);
//        for (CarusselContact c : contacts)
//        {
//            String originalPhone = c.getNumber();
//            String localContact = prefs.getString(originalPhone, null);
//            String userUuid;
//            if (localContact == null)
//            {
//                String[] phoneNumber = splitPhoneNumber(originalPhone);
//                if (phoneNumber == null) continue;
//                userUuid = mCloud.getUserUuid(
//                        phoneNumber[MainService.PHONE_INDEX_COUNTRY]
//                        , phoneNumber[MainService.PHONE_INDEX_MAIN]
//                );
//                if (userUuid == null || userUuid.equals("")) continue;
//                localContact =
//                        userUuid + "," +
//                                phoneNumber[MainService.PHONE_INDEX_COUNTRY] + "," +
//                                phoneNumber[MainService.PHONE_INDEX_MAIN];
//                editor.putString(originalPhone, localContact);
//            }
//            else
//            {
//                String[] fields = localContact.split(",");
//                String localUuid = fields[0];
//                if (localUuid == null || localUuid.equals(""))
//                {
//                    Log.e(TAG, "Bad local contact fields");
//                    continue;
//                }
//                userUuid = mCloud.getUserUuid(
//                        fields[2]
//                        , fields[1]
//                );
//                if (userUuid == null || userUuid.equals(""))
//                {
//                    Log.d(TAG, "The user {" + userUuid + "," + fields[1] + "," + fields[2] + "} has removed the profile");
//                    editor.remove(originalPhone);
//                }
//                else if (!userUuid.equals(localUuid))
//                {
//                    Log.d(TAG, "The user {" + userUuid + "," + fields[1] + "," + fields[2] + "} has changed the profile (used to be " + localUuid + ")");
//                    editor.remove(originalPhone);
//                    String[] phoneNumber = splitPhoneNumber(originalPhone);
//                    if (phoneNumber == null)
//                    {
//                        Log.e(TAG, "Bad phoneNumber - the splitPhoneNumber method has probably changed since last time");
//                        continue;
//                    }
//                    localContact =
//                            userUuid + "," +
//                                    phoneNumber[MainService.PHONE_INDEX_COUNTRY] + "," +
//                                    phoneNumber[MainService.PHONE_INDEX_MAIN];
//                    editor.putString(originalPhone, localContact);
//                }
//            }
//            uuidsList.add(userUuid);
//        }
//        editor.commit();
//        return uuidsList;
//    }
}
