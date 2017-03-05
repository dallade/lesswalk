package com.lesswalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AWS;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;
import com.lesswalk.utils.PhoneUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
    private static final String LAST_UPDATE_ROW       = "last_update";// milliseconds as string

    private MainService      mParent         = null;
    private Cloud            mCloud          = null;
    private DataBaseUpdater  dataBaseUpdater = null;
    private LesswalkDbHelper usersDB         = null;
    private LesswalkDbHelper signaturesDB    = null;

    private boolean isAlive = false;

    public SyncThread(MainService parent)
    {
        Vector<DataBaseColums> usersColums      = new Vector<DataBaseColums>();
        Vector<DataBaseColums> signaturesColums = new Vector<DataBaseColums>();

        mParent = parent;
        mCloud = new AmazonCloud(mParent);

        usersColums.add(new DataBaseColums(FULL_PHONE_NUMBER_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        usersColums.add(new DataBaseColums(USER_UUID_ROW, DataBaseColums.TEXT));
        usersColums.add(new DataBaseColums(SIGNATURES_ROW, DataBaseColums.TEXT));

        usersDB = new LesswalkDbHelper(mParent, "users", usersColums);

        signaturesColums.add(new DataBaseColums(SIGNATURE_UUID_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        signaturesColums.add(new DataBaseColums(TYPE_ROW, DataBaseColums.TEXT));
        signaturesColums.add(new DataBaseColums(LAST_UPDATE_ROW, DataBaseColums.TEXT));

        signaturesDB = new LesswalkDbHelper(mParent, "signatures", signaturesColums);
    }

    private class ContactUpdateTask
    {
        static final int COMMAND_UPDATE_DB    = 0x1;
        static final int COMMAND_DOWNLOAD_ZIP = 0x2;
        static final int COMMAND_DELETE_ZIP   = 0x3;
        static final int COMMAND_UPDATE_ZIP   = 0x4;
        static final int UPDATE_SIGNATURE_DB  = 0x5;

        int    command     = 0x0;
        String description = null;

        ContactUpdateTask(int _command, String _description)
        {
            command = _command;
            description = "" + _description;
        }
    }

    // TODO add optimization thread that will update relevants contact (cause request all contacts take too much time)

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
            long MIN_LOOP_TIME     = 10 * 60 * 1000;
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

                if (t2sleep < MIN_TIME_TO_SLEEP) t2sleep = MIN_TIME_TO_SLEEP;

                try
                {
                    sleep(t2sleep);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        private void updateContactIfNeed(CarusselContact c)
        {
            String                    number[]         = PhoneUtils.splitPhoneNumber(c.getNumber());
            String                    userUuid         = null;
            Vector<String>            sinaturesList    = null;
            Cursor                    cursor           = null;
            String                    signaturesString = null;
            Vector<ContactUpdateTask> tasks            = null;

            if (number == null) return;

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " not lesswalk number");

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null) return;

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " uuid:" + userUuid);

            sinaturesList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            signaturesString = SignatureListToString(sinaturesList);

            tasks = new Vector<ContactUpdateTask>();

            checkUserChanges(parent.usersDB, number, userUuid, signaturesString, tasks);

            if (tasks.size() > 0)
            {
                for (ContactUpdateTask t : tasks)
                {
                    switch (t.command)
                    {
                        case ContactUpdateTask.COMMAND_UPDATE_DB:
                        {
                            Log.d("elazarkin", "will update database");
                            updateUserDataBase(parent.usersDB, number, userUuid, signaturesString);
                            break;
                        }
                        case ContactUpdateTask.COMMAND_DOWNLOAD_ZIP:
                        {
                            Log.d("elazarkin", "will dowload zip");
                            downloadZipIfNeed(t.description);
                            break;
                        }
                        case ContactUpdateTask.UPDATE_SIGNATURE_DB:
                        {
                            Log.d("elazarkin", "will update signatureDB");
                            updateSignatureDatabase(parent.signaturesDB, t.description, null);
                            break;
                        }
                    }
                }

            }
            else Log.d("elazarkin", "no database need update!");
        }

        private void updateUserDataBase(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString)
        {
            String        fullNumber = PhoneUtils.splitedNumberToFullNumber(number);
            ContentValues values     = new ContentValues();
            //
            values.put(FULL_PHONE_NUMBER_ROW, fullNumber);
            values.put(USER_UUID_ROW, userUuid);
            values.put(SIGNATURES_ROW, signaturesString);

            db.getWritableDatabase().replace(db.table_name, null, values);
        }

        private synchronized void updateSignatureDatabase(LesswalkDbHelper db, String uuid, ObjectMetadata metadata)
        {
            File          zip             = mCloud.getSignutareFilePathByUUID(uuid);
            File          outDir          = new File(parent.mParent.getCacheDir(), "updateSignaturesDB");
            ContentValues values          = new ContentValues();
            InputStream   fis             = null;
            String        contentFileName = "content.json";
            File          contentFile     = null;
            byte          buffer[]        = null;
            String        json            = null;

            //
            //mCloud.unzipSignatureByUUID(uuid, outDir);

            if (mCloud.unzipFileFromSignatureByUUID(uuid, outDir, contentFileName))
            {

                try
                {
                    String type       = "";
                    String searchKey  = "\"type\"";
                    int    MAX        = 0;
                    int    readedSize = 0;

                    contentFile = new File(outDir, contentFileName);
                    fis = new FileInputStream(contentFile);
                    buffer = new byte[(int) contentFile.length()];
                    //
                    while (readedSize < buffer.length)
                    {
                        readedSize += fis.read(buffer, readedSize, buffer.length - readedSize);
                    }
                    fis.close();

                    json = new String(buffer);

                    //Log.d("elazarkin", "" + json);

                    for (int i = 0; i < json.length() - searchKey.length(); i++)
                    {
                        if (json.substring(i, i + searchKey.length()).equals(searchKey))
                        {
                            i += searchKey.length();
                            while (i < json.length() && json.charAt(i) != '"')
                            {
                                i++;
                            }
                            i++;
                            while (i < json.length() && json.charAt(i) != '"')
                            {
                                type += json.charAt(i++);
                            }
                            i = json.length();
                            break;
                        }
                    }

                    Log.d("elazarkin", "updateSignatureDatabase type = " + type);

                    values.put(SIGNATURE_UUID_ROW, uuid);
                    values.put(TYPE_ROW, type);
                    if (metadata != null)
                    {
                        values.put(LAST_UPDATE_ROW, "" + metadata.getLastModified().getTime());
                    }

                    db.getWritableDatabase().replace(db.table_name, null, values);

                }
                catch (Exception e)
                {
                    Log.d("elazarkin", "updateSignatureDatabase error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private void checkUserChanges(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString, Vector<ContactUpdateTask> tasks)
        {
            String   fullNumber      = PhoneUtils.splitedNumberToFullNumber(number);
            String[] projection      = new String[db.colums.size()];
            String   selection       = FULL_PHONE_NUMBER_ROW + " = ?";
            String   selectionArgs[] = {fullNumber};
            Cursor   cursor          = null;
            String   sigs[]          = null;

            Log.d("elazarkin", "check database by " + fullNumber + " number");

            for (int i = 0; i < projection.length; i++)
            {
                projection[i] = db.colums.elementAt(i).name;
            }

            cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

            if (cursor == null || cursor.getCount() <= 0)
            {
                tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_UPDATE_DB, ""));
            }
            else if (cursor.getCount() != 1)
            {
                Log.e(TAG, "some problem with primary key in " + db.table_name + " database");
            }
            else
            {
                while (cursor.moveToNext())
                {
                    String signatures = cursor.getString(cursor.getColumnIndexOrThrow(SIGNATURES_ROW));
                    String uuid       = cursor.getString(cursor.getColumnIndexOrThrow(USER_UUID_ROW));

                    if (!userUuid.equals("" + uuid) || !signaturesString.equals("" + signatures))
                    {
                        if (!userUuid.equals("" + uuid))
                        {
                            tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_UPDATE_DB, uuid));
                        }
                    }
                }
            }

            if(cursor != null) cursor.close();

            // DOWNLOAD MISSED ZIPS TASKS
            sigs = signaturesString.split(",");

            if (sigs != null || sigs.length > 0)
            {
                for (String uuid : sigs)
                {
                    if (uuid.length() > 0)
                    {
                        File zip = mCloud.getSignutareFilePathByUUID(uuid);

                        if (!zip.exists())
                        {
                            tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_DOWNLOAD_ZIP, uuid));
                        }
                        else
                        {
                            /**
                             * we update signatures data base anyway to be shore that everything alright
                             *
                             * if need dowload this task will be done onDownloadFinished
                             */
                            tasks.add(new ContactUpdateTask(ContactUpdateTask.UPDATE_SIGNATURE_DB, uuid));

                        }
                    }
                }
            }

        }

        private boolean downloadZipIfNeed(String s)
        {
            File zip = mCloud.getSignutareFilePathByUUID(s);

            if (!zip.exists())
            {
                mCloud.downloadSignature(s, new AWS.OnDownloadListener()
                {
                    public ObjectMetadata mFileMetadata = null;

                    @Override
                    public void onDownloadStarted(String path)
                    {
                        Log.d("elazarkin", "onDownloadStarted " + path);
                    }

                    @Override
                    public void onDownloadProgress(String path, float percentage)
                    {
                        Log.d("elazarkin", "onDownloadProgress " + path + "(" + percentage + "%)");
                    }

                    @Override
                    public void onDownloadFinished(String path)
                    {
                        // TODO improve syntax
                        String fileName = new File(path).getName();
                        String uuid     = fileName.substring(0, fileName.length() - 4);

                        Log.d("elazarkin", "onDownloadFinished" + path + " uuid = " + uuid);
                        updateSignatureDatabase(parent.signaturesDB, uuid, mFileMetadata);
                    }

                    @Override
                    public void onDownloadError(String path, int errorId, Exception ex)
                    {
                        Log.d("elazarkin", "onDownloadError" + path + " errorID=" + errorId + " " + ex.getMessage());
                    }

                    @Override
                    public void onMetadataReceived(ObjectMetadata fileMetadata) {
                        mFileMetadata = fileMetadata;
                    }
                });

                return true;
            }

            return false;
        }

        private String SignatureListToString(Vector<String> signaturesList)
        {
            String ret = "";

            Collections.sort(signaturesList);

            for (int i = 0; i < signaturesList.size(); i++)
            {
                ret += signaturesList.elementAt(i);

                if (i < signaturesList.size() - 1) ret += ",";
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
        public static final int DATABASE_VERSION = 1;
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

            for (int i = 0; i < colums.size(); i++)
            {
                SQL_CREATE_ENTRIES += (colums.elementAt(i).name + " " + colums.elementAt(i).type);

                if (i < colums.size() - 1)
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
    }

    public void fillSignaturesOfPhoneNumber(String phoneNumber, Vector<ContactSignature> container)
    {
        String number[]     = PhoneUtils.splitPhoneNumber(phoneNumber);
        String fixedNumber  = PhoneUtils.splitedNumberToFullNumber(number);
        String signatures[] = getSignaturesOfNumber(usersDB, fixedNumber);

        Log.d("elazarkin1", "signatures = " + signatures);

        if(signatures != null)
        {
            Log.d("elazarkin1", "signatures.length = " + signatures.length);

            for (String uuid : signatures)
            {
                if (uuid.length() > 0)
                {
                    String type = getTypeOfSignature(signaturesDB, uuid);
                    container.add(new ContactSignature
                    (
                            phoneNumber,
                            ContactSignature.StringToType(type),
                            mCloud.getSignutareFilePathByUUID(uuid).getPath()
                    ));

                    Log.d("elazarkin1", "add " + uuid + " type = " + type);
                }
            }
        }
    }

    private String getTypeOfSignature(LesswalkDbHelper db, String uuid)
    {
        Cursor   cursor          = null;
        String[] projection      = new String[db.colums.size()];
        String   selection       = SIGNATURE_UUID_ROW + " = ?";
        String   selectionArgs[] = {uuid};
        String   type            = null;

        for (int i = 0; i < projection.length; i++)
        {
            projection[i] = db.colums.elementAt(i).name;
        }

        cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0)
        {
            // DO NOTHING
        }
        else if (cursor.getCount() != 1)
        {
            Log.e(TAG, "some problem with primary key in " + db.table_name + " database");
        }
        else
        {
            while (cursor.moveToNext())
            {
                type = cursor.getString(cursor.getColumnIndexOrThrow(TYPE_ROW));
            }
        }

        if(cursor != null) cursor.close();

        return type;
    }

    private String[] getSignaturesOfNumber(LesswalkDbHelper db, String fixedNumber)
    {
        Cursor   cursor          = null;
        String[] projection      = new String[db.colums.size()];
        String   selection       = FULL_PHONE_NUMBER_ROW + " = ?";
        String   selectionArgs[] = {fixedNumber};
        String   signatures[]    = null;

        for (int i = 0; i < projection.length; i++)
        {
            projection[i] = db.colums.elementAt(i).name;
        }

        cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0)
        {
            Log.d("elazarkin1", "getSignaturesOfNumber cursor == null || cursor.getCount() number=" + fixedNumber);
            // DO NOTHING
        }
        else if (cursor.getCount() != 1)
        {
            Log.e(TAG, "some problem with primary key in " + db.table_name + " database");
            Log.d("elazarkin1", "some problem with primary key in " + db.table_name + " database");
        }
        else
        {
            while (cursor.moveToNext())
            {
                String signaturesValue = cursor.getString(cursor.getColumnIndexOrThrow(SIGNATURES_ROW));
                //
                Log.d("elazarkin1", "signatureValue is: " + signaturesValue);
                //
                signatures = signaturesValue.split(",");
            }
        }

        if(cursor != null) cursor.close();

        return signatures;
    }
}
