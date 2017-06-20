package com.lesswalk.system;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.Gson;
import com.lesswalk.bases.ContactSignature;
import com.lesswalk.bases.ILesswalkService;
import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AWS;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.AwsDownloadItem;
import com.lesswalk.database.Cloud;
import com.lesswalk.database.ZipManager;
import com.lesswalk.utils.PhoneUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class SyncThread
{
    public static final String TAG = "LesswalkSyncThread";

    private static final String FULL_PHONE_NUMBER_ROW = "phone_number";
    private static final String USER_UUID_ROW         = "uuid";
    private static final String SIGNATURES_ROW        = "signatures_uuids";
    private static final String SIGNATURE_UUID_ROW    = "uuid";
    private static final String TYPE_ROW              = "type";
    private static final String LAST_UPDATE_ROW       = "last_update";// milliseconds as string

    private static Semaphore mutex = new Semaphore(1);

    private class UserContent
    {
        String country_dial_code = null;
        String first_name        = null;
        String key               = null;
        String last_name         = null;
        String phone_number      = null;

        public String getCountry_dial_code() {return country_dial_code;}
        public String getFirst_name() {return first_name;}
        public String getKey() {return key;}
        public String getLast_name() {return last_name;}
        public String getPhone_number() {return phone_number;}

        public void setCountry_dial_code(String country_dial_code) {this.country_dial_code = country_dial_code;}
        public void setFirst_name(String first_name) {this.first_name = first_name;}
        public void setKey(String key) {this.key = key;}
        public void setLast_name(String last_name) {this.last_name = last_name;}
        public void setPhone_number(String phone_number) {this.phone_number = phone_number;}
    }

    private MainService      mParent         = null;
    private Cloud            mCloud          = null;
    private DataBaseUpdater  dataBaseUpdater = null;
    private LesswalkDbHelper usersDB         = null;
    private LesswalkDbHelper signaturesDB    = null;
    private File             userJsonFile    = null;
    private boolean          isAlive         = false;
    private UserContent      userContent     = null;

    public boolean checkIfUserExisted(String _number)
    {
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        String userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

        return userUuid != null;
    }

    public void downloadUserJsonIfNeed(String _number)
    {
        String     number[] = PhoneUtils.splitPhoneNumber(_number);
        JSONObject json     = mCloud.getUserJson(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

        Log.d("elazarkin14", "downloadUserJsonIfNeed_1: " + userJsonFile.getAbsolutePath());

        Log.d("elazarkin14", "userJson: " + (json == null ? "null":json.toString()));

        try
        {
            JSONObject       content = json.getJSONObject("content");
            FileOutputStream os      = new FileOutputStream(userJsonFile);

            Log.d("elazarkin14", "userJson_content: " + content.toString());

            os.write(content.toString().getBytes());
            os.close();

            Log.d("elazarkin14", "donload userJson Success!!");

            reloadUserJson();
        }
        catch (Exception e)
        {
            Log.d("elazarkin14", "donload userJson unsuccess!! " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void reloadUserJson()
    {
        Gson gson = new Gson();

        if(userJsonFile == null)
        {
            userJsonFile = new File(mParent.getFilesDir(), "userJson.json");
        }

        try (Reader reader = new FileReader(userJsonFile))
        {

            // Convert JSON to Java Object
            userContent = gson.fromJson(reader, UserContent.class);

            Log.d("elazarkin14", "first=" + userContent.first_name + " last=" +userContent.last_name + " num=" + userContent.phone_number);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getUserFirstName()
    {
        if(userContent != null)
        {
            return userContent.getFirst_name();
        }
        return null;
    }

    public String getUserLastName()
    {
        if(userContent != null)
        {
            return userContent.getLast_name();
        }
        return null;
    }

    public String getLocalNumber()
    {
        if(userContent != null)
        {
            return userContent.getPhone_number();
        }

        return null;
    }

    public void sendVerificationSms(String _number, String code)
    {
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        // TODO
        mCloud.sendVerificationSms(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN], code);
    }

    enum SyncThreadIDs
    {
        STORE_LOCAL_CONTACT_TASK,
        SYNC_SOME_CONTACT_SIGNATURES_TASK,
        CHECK_CONTACT_UPDATE
    }

    interface ISyncThreadTasks
    {
        // TODO remove syncThread
        void DO(SyncThread syncThread, LesswalkDbHelper userDB, LesswalkDbHelper signaturesDB);
        SyncThreadIDs getID();
    }

    class SyncSomeContactSignaturesTask implements ISyncThreadTasks
    {
        String                                   number   = null;
        ILesswalkService.ISetLocalNumberCallback callback = null;

        SyncSomeContactSignaturesTask(String number, ILesswalkService.ISetLocalNumberCallback callback)
        {
            this.number = number;
            this.callback = callback;
        }

        @Override
        public void DO(final SyncThread syncThread, final LesswalkDbHelper userDB, final LesswalkDbHelper signaturesDB)
        {
            // TODO have doble code - // FIXME: 23/4/17
            String         number[]      = PhoneUtils.splitPhoneNumber(this.number);
            final String   fixed_number  = PhoneUtils.splitedNumberToFullNumber(number);
            String         userUuid      = null;
            Vector<String> sinaturesList = null;

            Log.d("elazarkin8", "StoreLocalContactTask - " + fixed_number + "(" + number[0] + " ," + number[1] + ")");

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null)
            {
                String signupNumber = String.format(Locale.getDefault(), "+%d %d", number[0], number[1]);
                String uuid         = UUID.randomUUID().toString();
                // TODO FIXME
                mCloud.createUser(signupNumber, uuid, new Cloud.I_ProcessListener()
                {
                    @Override
                    public void onSuccess(HashMap<String, String> result)
                    {

                    }

                    @Override
                    public void onFailure(HashMap<String, String> result)
                    {

                    }
                });
                callback.onError(ILesswalkService.REGISTRATION_ERROR_STILL_NOT_REGISTRED);
                return;
            }

            sinaturesList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            if(sinaturesList.size() > 0)
            {
                String         signaturesString           = SignatureListToString(sinaturesList);
                Vector<String> needBeDownloadedSignatures = new Vector<>();

                updateUserDataBase(userDB, number, userUuid, signaturesString);

                for(String uuid:sinaturesList)
                {
                    // FIXME TODO check if signature not exist or signature need be update

                    Log.d("elazarkin12", "mCloud.getSignutareFilePathByUUID(uuid).exists()=" + mCloud.getSignutareFilePathByUUID(uuid).exists());

                    if(!mCloud.getSignutareFilePathByUUID(uuid).exists())
                    {
                        needBeDownloadedSignatures.add(uuid);
                    }
                }

                if(needBeDownloadedSignatures.size() > 0)
                {
                    mCloud.downloadSignatures(needBeDownloadedSignatures, new AWS.OnDownloadListener()
                    {
                        private Vector<AwsDownloadItem> items = null;
                        private int errorCount = 0;

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
                        public synchronized void onDownloadFinished(String path)
                        {
                            String fileName = new File(path).getName();
                            String uuid     = fileName.substring(0, fileName.length() - 4);

                            Log.d("elazarkin10", "onDownloadFinished " + path + " items.size()=" + items.size());

                            if (items != null && items.size() > 0)
                            {
                                for (AwsDownloadItem item : items)
                                {
                                    if (item.getFilePath().equals(path))
                                    {
                                        updateSignatureDatabase(syncThread.mParent, syncThread.mCloud, signaturesDB, uuid, item.getMetadata());
                                        items.removeElement(item);

                                        if (items.size() <= 0)
                                        {
                                            if (errorCount <= 0)
                                            {
                                                Log.d("elazarkin10", "finish all");
                                                callback.onSuccess();
                                            }
                                            else
                                            {
                                                Log.d("elazarkin10", "not success 2");
                                                callback.notSuccessFinish();
                                            }
                                        }
                                        else
                                        {
                                            Log.d("elazarkin10", "onProccess!");
                                            callback.onProgress(path);
                                        }
                                        return;
                                    }
                                }
                            }
                            else
                            {
                                callback.onError(ILesswalkService.CODE_ERROR_METADATA_NOT_CREATED);
                                return;
                            }

                            callback.onError(ILesswalkService.CODE_ERROR_STRANGE_METADATA_ITEM);
                        }

                        @Override
                        public void onDownloadError(String path, int errorId, Exception ex)
                        {
                            errorCount++;

                            for (AwsDownloadItem item : items)
                            {
                                if (item.getFilePath().equals(path))
                                {
                                    items.removeElement(item);

                                    if (items.size() <= 0)
                                    {
                                        callback.notSuccessFinish();
                                    }
                                    else
                                    {
                                        callback.onError(ILesswalkService.REGISTRATION_ERROR_DOWNLOAD_SIGNATURES);
                                    }
                                }
                            }
                            Log.d("elazarkin", "onDownloadError" + path + " errorID=" + errorId + " " + ex.getMessage());
                        }

                        @Override
                        public void onMetadataReceived(AwsDownloadItem item)
                        {
                            Log.d("elazarkin10", "onMetadataReceived " + item.getFilePath());

                            if (items == null)
                            {
                                items = new Vector<AwsDownloadItem>();
                            }

                            items.add(item);
                        }
                    });
                }
                else callback.onSuccess();
            }
            else
            {
                callback.onSuccess();
            }
        }

        @Override
        public SyncThreadIDs getID()
        {
            return null;
        }
    }

//    class StoreLocalContactTask extends SyncSomeContactSignaturesTask
//    {
//        private String name = null;
//
//        StoreLocalContactTask(String name, String number, ILesswalkService.ISetLocalNumberCallback callback)
//        {
//            super(number, callback);
//            this.name = name;
//        }
//        @Override
//        public void DO(final SyncThread syncThread, final LesswalkDbHelper userDB, final LesswalkDbHelper signaturesDB)
//        {
//            String         number[]      = PhoneUtils.splitPhoneNumber(this.number);
//            final String   fixed_number  = PhoneUtils.splitedNumberToFullNumber(number);
//            //
//            try
//            {
//                // TODO improve syntax
//                OutputStream os       = new FileOutputStream(syncThread.localNumberStoreFile);
//                os.write(fixed_number.getBytes());
//                os.close();
//
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//                callback.onError(ILesswalkService.REGISTRATION_ERROR_FILE_SYSTEM);
//            }
//
//            super.DO(syncThread, userDB, signaturesDB);
//        }
//
//        @Override
//        public SyncThreadIDs getID()
//        {
//            return SyncThreadIDs.SYNC_SOME_CONTACT_SIGNATURES_TASK;
//        }
//    }

    class CheckIfContactNeedBeUpdated implements ISyncThreadTasks
    {
        DataBaseUpdater dataBaseUpdater = null;
        CarusselContact contact         = null;

        CheckIfContactNeedBeUpdated(DataBaseUpdater _dataBaseUpdater, CarusselContact c)
        {
            dataBaseUpdater = _dataBaseUpdater;
            contact = c;
        }

        @Override
        public void DO(final SyncThread syncThread, final LesswalkDbHelper userDB, final LesswalkDbHelper signaturesDB)
        {
            dataBaseUpdater.updateContactIfNeed(contact);
        }

        @Override
        public SyncThreadIDs getID()
        {
            return SyncThreadIDs.CHECK_CONTACT_UPDATE;
        }
    }

    SyncThread(MainService parent)
    {
        Vector<DataBaseColums> usersColums      = new Vector<DataBaseColums>();
        Vector<DataBaseColums> signaturesColums = new Vector<DataBaseColums>();

        mParent = parent;
        mCloud = new AmazonCloud(mParent);

        reloadUserJson();

        usersColums.add(new DataBaseColums(FULL_PHONE_NUMBER_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        usersColums.add(new DataBaseColums(USER_UUID_ROW, DataBaseColums.TEXT));
        usersColums.add(new DataBaseColums(SIGNATURES_ROW, DataBaseColums.TEXT));

        usersDB = new LesswalkDbHelper(mParent, "users", usersColums);

        signaturesColums.add(new DataBaseColums(SIGNATURE_UUID_ROW, DataBaseColums.TEXT_PRIMARY_KEY));
        signaturesColums.add(new DataBaseColums(TYPE_ROW, DataBaseColums.TEXT));
        signaturesColums.add(new DataBaseColums(LAST_UPDATE_ROW, DataBaseColums.TEXT));

        signaturesDB = new LesswalkDbHelper(mParent, "signatures", signaturesColums);

        if(!getAssetsDir().exists() || getAssetsDir().listFiles().length <= 0)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    mCloud.downloadAssets(mParent.getFilesDir().getAbsolutePath(), new AWS.OnDownloadListener()
                    {
                        @Override
                        public void onDownloadStarted(String path) {}

                        @Override
                        public void onDownloadProgress(String path, float percentage) {}

                        @Override
                        public void onDownloadFinished(String path)
                        {
                            ZipManager.unzip(mParent, path, getAssetsDir().getAbsolutePath());
                        }

                        @Override
                        public void onDownloadError(String path, int errorId, Exception ex) {}

                        @Override
                        public void onMetadataReceived(AwsDownloadItem dowloadItem) {}
                    });
                }
            }.start();
        }
    }

    private File getAssetsDir()
    {
        File dir = new File(mParent.getFilesDir(), "assets");

        return dir;
    };

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
        private Vector<ISyncThreadTasks> tasks = null;

        public DataBaseUpdater(SyncThread _parent, MainService _service)
        {
            parent = _parent;
            service = _service;

            tasks = new Vector<ISyncThreadTasks>();
        }

        @Override
        public void run()
        {
            long TIME_TO_SYNC_CONTACTS = 10 * 60 * 1000;
            long IDLE_SLEEP_TYPE       = 200;
            long t0                    = 0L;
            long currentTime           = 0L;

            Vector<CarusselContact> contacts = new Vector<CarusselContact>();

            while (isAlive)
            {
                contacts.removeAllElements();
                service.getContactManager().fillContactVector(contacts);

                if (tasks.size() > 0)
                {
                    try {mutex.acquire();} catch (InterruptedException e) {e.printStackTrace();}
                    Log.d("elazarkin8", "do task: " + tasks.elementAt(0).getID());
                    tasks.elementAt(0).DO(parent, parent.usersDB, parent.signaturesDB);
                    tasks.removeElementAt(0);
                    mutex.release();
                }
                else
                {
                    try
                    {
                        sleep(IDLE_SLEEP_TYPE);
                    }
                    catch (InterruptedException e)  {e.printStackTrace();}
                }
            }
        }

        private void updateContactIfNeed(CarusselContact c)
        {
            String                    number[]         = null;
            String                    userUuid         = null;
            Vector<String>            sinaturesList    = null;
            String                    signaturesString = null;
            Vector<ContactUpdateTask> tasks            = null;

            if (c == null) return;

            number = PhoneUtils.splitPhoneNumber(c.getNumber());

            if(number == null) return;

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
                            updateSignatureDatabase(parent.mParent, parent.mCloud, parent.signaturesDB, t.description, null);
                            break;
                        }
                    }
                }
            }
            else Log.d("elazarkin", "no database need update!");
        }

        private synchronized void checkUserChanges(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString, Vector<ContactUpdateTask> tasks)
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
                    ObjectMetadata mFileMetadata = null;

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
                        updateSignatureDatabase(parent.mParent, parent.mCloud, parent.signaturesDB, uuid, mFileMetadata);
                    }

                    @Override
                    public void onDownloadError(String path, int errorId, Exception ex)
                    {
                        Log.d("elazarkin", "onDownloadError" + path + " errorID=" + errorId + " " + ex.getMessage());
                    }

                    @Override
                    public void onMetadataReceived(AwsDownloadItem item)
                    {
                        mFileMetadata = item.getMetadata();
                    }
                });

                return true;
            }

            return false;
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

    private static synchronized void updateUserDataBase(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString)
    {
        String        fullNumber = PhoneUtils.splitedNumberToFullNumber(number);
        ContentValues values     = new ContentValues();
        //
        values.put(FULL_PHONE_NUMBER_ROW, fullNumber);
        values.put(USER_UUID_ROW, userUuid);
        values.put(SIGNATURES_ROW, signaturesString);

        db.getWritableDatabase().replace(db.table_name, null, values);
    }

    private static synchronized void updateSignatureDatabase(Context context, Cloud mCloud, LesswalkDbHelper db, String uuid, ObjectMetadata metadata)
    {
        // TODO move type to function values (and logic of getType up) and remove context value!
        File          outDir          = new File(context.getCacheDir(), "updateSignaturesDB");
        File          zip             = mCloud.getSignutareFilePathByUUID(uuid);
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

    private static String SignatureListToString(Vector<String> signaturesList)
    {
        String ret = "";

        //Collections.sort(signaturesList);

        for (int i = 0; i < signaturesList.size(); i++)
        {
            ret += signaturesList.elementAt(i);

            if (i < signaturesList.size() - 1) ret += ",";
        }

        return ret;
    }

    void addImportantTask(ISyncThreadTasks task)
    {
        try {mutex.acquire();} catch (InterruptedException e) {e.printStackTrace();}
        Log.d("elazarkin8", "add addImportantTask " + task.getID());
        dataBaseUpdater.tasks.add(0, task);
        mutex.release();
    }

    public void updateUserJson(String first, String last, String number)
    {
        Gson gson = new Gson();

        userContent.setFirst_name(first);
        userContent.setLast_name(last);
        userContent.setPhone_number(number);

        try
        {
            gson.toJson(userContent, new FileWriter(userJsonFile));
        }
        catch (Exception e) {e.printStackTrace();}

        // TODO finish this and update amazon cloude
    }
}

