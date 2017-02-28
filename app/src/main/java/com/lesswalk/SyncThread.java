package com.lesswalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;
import com.lesswalk.utils.PhoneUtils;

import java.util.Collections;
import java.util.Vector;

public class SyncThread
{
    public static final String TAG = "LesswalkSyncThread";

    private static final String FULL_PHONE_NUMBER_ROW = "phone_number";
    private static final String USER_UUID_ROW         = "uuid";
    private static final String SIGNATURES_ROW        = "signatures_uuids";
    private static final String SIGNATURE_UUID_ROW    = "uuid";
    private static final String TYPE_ROW              = "type";
    private static final String LAST_UPDATE_ROW       = "last_update";

    private MainService      mParent         = null;
    private Cloud            mCloud          = null;
    private DataBaseUpdater  dataBaseUpdater = null;
    private LesswalkDbHelper usersDB         = null;
    private LesswalkDbHelper signaturesDB    = null;

    private boolean isAlive = false;

    public SyncThread(MainService parent)
    {
        mParent = parent;
        mCloud = new AmazonCloud(mParent);
    }

    /**
     * This thread will update data (files and database) by user existed contacts
     * <p>
     * first (by loop of exist numbers) the thread check differences about the number on Amazon cloud and local
     * and update it if need
     * <p>
     * databases:
     * 1 - number, uuid, signatures_uuids (sign1,sign2...)
     * 2 - signature_uuid, type, last_update
     * <p>
     * files of signatures will be as zip files
     */

    private class DataBaseUpdater extends Thread
    {
        private MainService service = null;
        private SyncThread  parent  = null;

        public DataBaseUpdater(SyncThread _parent, MainService _service)
        {
            parent = _parent;
            service = _service;
        }

        @Override
        public void run()
        {
            long MIN_LOOP_TIME     = 3 * 60 * 1000;
            long MIN_TIME_TO_SLEEP = 100;

            Vector<CarusselContact> contacts = new Vector<CarusselContact>();

            while (isAlive)
            {
                long t0      = System.currentTimeMillis();
                long tdiff   = 0L;
                long t2sleep = 0L;

                contacts.removeAllElements();
                service.getContactManager().fillContactVector(contacts);

                for (CarusselContact c : contacts) updateContactIfNeed(c);

                tdiff = System.currentTimeMillis() - t0;
                t2sleep = MIN_LOOP_TIME - tdiff;

                if(t2sleep < MIN_TIME_TO_SLEEP) t2sleep = MIN_TIME_TO_SLEEP;

                try {sleep(t2sleep);} catch (InterruptedException e) {e.printStackTrace();}
            }
        }

        private void updateContactIfNeed(CarusselContact c)
        {
            String         number[]         = PhoneUtils.splitPhoneNumber(c.getNumber());
            String         userUuid         = null;
            Vector<String> sinaturesList    = null;
            //SQLiteDatabase signaturesReadDB = parent.signaturesDB.getReadableDatabase();
            Cursor         cursor           = null;
            ContentValues  values           = new ContentValues();
            String         signaturesString = null;

            if (number == null) return;

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null) return;

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " uuid:" + userUuid);

            sinaturesList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            signaturesString = SignatureListToString(sinaturesList);

            if(checkUserChanges(parent.usersDB, number, userUuid, signaturesString))
            {
                Log.d("elazarkin", "will update database");
            }
        }

        private boolean checkUserChanges(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString)
        {
            String   fullNumber      = splitedNumberToFullNumber(number);
            String[] projection      = new String[db.colums.size()];
            String   selection       = FULL_PHONE_NUMBER_ROW + " = ?";
            String   selectionArgs[] = {fullNumber};
            Cursor   cursor          = null;

            Log.d("elazarkin", "check database by " + fullNumber + " number");

            for(int i = 0; i < projection.length; i++)
            {
                projection[i] = db.colums.elementAt(i).name;
            }

            cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

            if(cursor == null || cursor.getCount() <= 0) return true;
            else if(cursor.getCount() != 1)
            {
                Log.e(TAG, "some problem with primary key in " + db.table_name + " database");
                return false;
            }

            while (cursor.moveToNext())
            {
                
            }

            cursor.close();

            return false;
        }

        private String splitedNumberToFullNumber(String[] number)
        {
            return "+" + number[PhoneUtils.PHONE_INDEX_COUNTRY] + number[PhoneUtils.PHONE_INDEX_MAIN].substring(1);
        }

        private String SignatureListToString(Vector<String> sinaturesList)
        {
            String ret = "";

            Collections.sort(sinaturesList);

            for(int i = 0; i < sinaturesList.size(); i++)
            {
                ret += sinaturesList.elementAt(i);

                if(i < sinaturesList.size()-1) ret += ",";
            }

            return ret;
        }
    }

    private class DataBaseColums
    {
        static final String INTEGER_PRIMARY_KEY = "INTEGER PRIMARY KEY";
        static final String INTEGER             = "INTEGER";
        static final String TEXT_PRIMARY_KEY    = "TEXT PRIMARY KEY";
        static final String TEXT                = "TEXT";

        String name = null;
        String type = null;

        public DataBaseColums(String _name, String _type)
        {
            name = "" + _name;
            type = "" + _type;
        }
    }

    private class LesswalkDbHelper extends SQLiteOpenHelper
    {
        public static final int    DATABASE_VERSION = 1;
        //public static final String DATABASE_NAME    = "users.db";

        private Vector<DataBaseColums> colums     = null;
        private String                 table_name = null;

        public LesswalkDbHelper(Context context, String _table_name, Vector<DataBaseColums> _colums)
        {
            super(context, _table_name + ".db", null, DATABASE_VERSION);

            colums = new Vector<DataBaseColums>(_colums);
            table_name = "" + _table_name;
        }

        public void onCreate(SQLiteDatabase db)
        {
            String SQL_CREATE_ENTRIES = "CREATE TABLE " + table_name + " (";

            for(int i = 0; i < colums.size(); i++)
            {
                SQL_CREATE_ENTRIES += (colums.elementAt(i).name + " " + colums.elementAt(i).type);

                if(i < colums.size() - 1)
                {
                    SQL_CREATE_ENTRIES += ",";
                }
                else SQL_CREATE_ENTRIES += ")";
            }
            Log.d(TAG, "Create Table: " + SQL_CREATE_ENTRIES);

            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + table_name;
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public void start()
    {
        Vector<DataBaseColums> usersColums      = new Vector<DataBaseColums>();
        Vector<DataBaseColums> signaturesColums = new Vector<DataBaseColums>();

        usersColums.add(new DataBaseColums(FULL_PHONE_NUMBER_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        usersColums.add(new DataBaseColums(USER_UUID_ROW, DataBaseColums.TEXT));
        usersColums.add(new DataBaseColums(SIGNATURES_ROW, DataBaseColums.TEXT));

        usersDB = new LesswalkDbHelper(mParent, "users", usersColums);

        signaturesColums.add(new DataBaseColums(SIGNATURE_UUID_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        signaturesColums.add(new DataBaseColums(TYPE_ROW, DataBaseColums.TEXT));
        signaturesColums.add(new DataBaseColums(LAST_UPDATE_ROW, DataBaseColums.TEXT));

        signaturesDB = new LesswalkDbHelper(mParent, "signatures", signaturesColums);

        if (!isAlive)
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

        usersDB.close();
        signaturesDB.close();
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
