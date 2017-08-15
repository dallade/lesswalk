package com.lesswalk.system;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.Gson;
import com.lesswalk.bases.ILesswalkService;
import com.lesswalk.contact_page.navigation_menu.CarusselContact;
import com.lesswalk.database.AWS;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.AwsDownloadItem;
import com.lesswalk.database.Cloud;
import com.lesswalk.database.ZipManager;
import com.lesswalk.json.CarruselJson;
import com.lesswalk.utils.PhoneUtils;
import com.lesswalk.utils.Utils;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class SyncThread {
    public static final String TAG = "LesswalkSyncThread";

    private static final String FULL_PHONE_NUMBER_ROW = "phone_number";
    private static final String USER_UUID_ROW         = "uuid";
    private static final String SIGNATURES_ROW        = "signatures_uuids";
    private static final String SIGNATURE_UUID_ROW    = "uuid";
    private static final String TYPE_ROW              = "type";
    private static final String SIGNATURE_ETAG_ROW    = "etag";// milliseconds as string

    private static Semaphore mutex = new Semaphore(1);

    public String uuidToPath(String uuid) {
        return mCloud.getLocalSignatureFile(uuid).getAbsolutePath();
    }

    private class UserContent {
        String country_dial_code = null;
        String first_name        = null;
        String key               = null;
        String last_name         = null;
        String phone_number      = null;

        public String getCountry_dial_code() {
            return country_dial_code;
        }

        public String getFirst_name() {
            return first_name;
        }

        public String getKey() {
            return key;
        }

        public String getLast_name() {
            return last_name;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setCountry_dial_code(String country_dial_code) {
            this.country_dial_code = country_dial_code;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }
    }

    private MainService      mParent         = null;
    private Cloud            mCloud          = null;
    private DataBaseUpdater  dataBaseUpdater = null;
    private LesswalkDbHelper usersDB         = null;// PATH: /data/user/0/com.lesswalk/databases/users.db
    private LesswalkDbHelper signaturesDB    = null;
    private File             userContentDir  = null;
    private File             userJsonFile    = null;
    private boolean          isAlive         = false;
    private UserContent      userContent     = null;

    public boolean checkIfUserExisted(String _number) {
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        String userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

        return userUuid != null;
    }

    public void downloadUserJsonIfNeed(String _number) {
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        JSONObject json = mCloud.getUserJson(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

        Log.d("elazarkin14", "downloadUserJsonIfNeed_1: " + userJsonFile.getAbsolutePath());

        Log.d("elazarkin14", "userJson: " + (json == null ? "null" : json.toString()));

        try {
            JSONObject content = json.getJSONObject("content");
            FileOutputStream os = new FileOutputStream(userJsonFile);

            Log.d("elazarkin14", "userJson_content: " + content.toString());

            os.write(content.toString().getBytes());
            os.close();

            Log.d("elazarkin14", "donload userJson Success!!");

            reloadUserJson();
        } catch (Exception e) {
            Log.d("elazarkin14", "donload userJson unsuccess!! " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void reloadUserJson() {
        Gson gson = new Gson();

        userContent = null;

        if (userContentDir == null) {
            userContentDir = new File(mParent.getFilesDir(), "userContent");
            userContentDir.mkdirs();
            userContentDir.mkdir();
        }

        if (userJsonFile == null) {
            userJsonFile = new File(userContentDir, "userJson.json");
        }

        try (Reader reader = new FileReader(userJsonFile)) {
            // Convert JSON to Java Object
            userContent = gson.fromJson(reader, UserContent.class);

            Log.d("elazarkin14", "first=" + userContent.first_name + " last=" + userContent.last_name + " num=" + userContent.phone_number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserFirstName() {
        if (userContent != null) {
            return userContent.getFirst_name();
        }
        return null;
    }

    public String getUserLastName() {
        if (userContent != null) {
            return userContent.getLast_name();
        }
        return null;
    }

    public String getLocalNumber() {
        if (userContent != null) {
            return userContent.getPhone_number();
        }

        return null;
    }

    public void sendVerificationSms(String _number, String code) {
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        // TODO
        mCloud.sendVerificationSms(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN], code);
    }

    enum SyncThreadIDs {
        STORE_LOCAL_CONTACT_TASK,
        SYNC_SOME_CONTACT_SIGNATURES_TASK,
        CHECK_CONTACT_UPDATE,
        DELETE_USER_ACCOUNT,
        SAVE_SIGNATURE,
        DELETE_SIGNATURE
    }

    interface ISyncThreadTasks {
        // TODO remove syncThread
        void DO(SyncThread syncThread, LesswalkDbHelper userDB, LesswalkDbHelper signaturesDB);

        SyncThreadIDs getID();
    }

    class SyncSomeContactSignaturesTask implements ISyncThreadTasks {
        ILesswalkService.ISetLocalNumberCallback voidCallback = new ILesswalkService.ISetLocalNumberCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorID) {
            }

            @Override
            public void onProgress(String path) {
            }

            @Override
            public void notSuccessFinish() {
            }
        };
        String                                   number       = null;
        ILesswalkService.ISetLocalNumberCallback callback     = null;

        SyncSomeContactSignaturesTask(String number, ILesswalkService.ISetLocalNumberCallback callback) {
            Log.d("syncSignature", "SyncSomeContactSignaturesTask " + number);
            this.number = number;
            this.callback = callback != null ? callback : voidCallback;
        }

        @Override
        public void DO(final SyncThread syncThread, final LesswalkDbHelper userDB, final LesswalkDbHelper signaturesDB) {
            // TODO have doble code - // FIXME: 23/4/17
            String number[] = PhoneUtils.splitPhoneNumber(this.number);
            final String fixed_number = PhoneUtils.splitedNumberToFullNumber(number);
            String userUuid = null;
            Vector<String> signatureList = null;

            Log.d("elazarkin8", "SyncSomeContactSignaturesTask - " + fixed_number + "(" + number[0] + " ," + number[1] + ")");

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null) {
                callback.onError(ILesswalkService.REGISTRATION_ERROR_STILL_NOT_REGISTRED);
                return;
            }

            signatureList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            if (signatureList.size() > 0) {
                String signaturesString = SignatureListToString(signatureList);
                Vector<String> signaturesToDownload = new Vector<>();

                updateUserDataBase(userDB, number, userUuid, signaturesString);

                for (String uuid : signatureList) {
                    boolean shouldDownload = false;
                    if (mCloud.getLocalSignatureFile(uuid).exists()) {
                        String sql = "select " + SIGNATURE_ETAG_ROW + " from " + signaturesDB.table_name + " where " + SIGNATURE_UUID_ROW + "=?";
                        String selections[] = {uuid};
                        Cursor cursor = signaturesDB.getReadableDatabase().rawQuery(sql, selections);
                        if (cursor.getCount() <= 0) {
                            shouldDownload = true;
                            Log.e("syncSignature", "OK, nothing to do");
                        } else if (cursor.moveToFirst() && cursor.isLast()) {
                            String localEtag = cursor.getString(cursor.getColumnIndex(SIGNATURE_ETAG_ROW));
                            String cloudEtag = "";
                            try {
                                cloudEtag = mCloud.getSignatureEtag(uuid);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                e.printStackTrace();
                                callback.onError(ILesswalkService.CODE_ERROR_CLOUD_CONNECTION_FAILED);
                                return;
                            }
                            shouldDownload = !cloudEtag.equals(localEtag);
                            Log.e("syncSignature", String.format(
                                    "userUuid: %s, uuid: %s, localEtag: %s, cloudEtag: %s, shouldDownload: %s"
                                    , userUuid
                                    , uuid
                                    , localEtag
                                    , cloudEtag
                                    , shouldDownload ? "true" : "false"
                            ));
                        } else {
                            Log.e("syncSignature", "more than one etags for signature");
                        }
                        cursor.close();
                    } else {
                        shouldDownload = true;
                    }
                    if (shouldDownload) {
                        signaturesToDownload.add(uuid);
                    }
                }

                if (signaturesToDownload.size() > 0) {
                    try {
                        mCloud.downloadSignatures(signaturesToDownload, new AWS.OnDownloadListener() {
                            private Vector<AwsDownloadItem> items = null;
                            private int errorCount = 0;

                            @Override
                            public void onDownloadStarted(String path) {
                                Log.d("elazarkin", "onDownloadStarted " + path);
                            }

                            @Override
                            public void onDownloadProgress(String path, float percentage) {
                                Log.d("elazarkin", "onDownloadProgress " + path + "(" + percentage + "%)");
                            }

                            @Override
                            public synchronized void onDownloadFinished(String path) {
                                String fileName = new File(path).getName();
                                String uuid = fileName.substring(0, fileName.length() - 4);

                                Log.d("elazarkin10", "onDownloadFinished " + path + " items.size()=" + items.size());

                                if (items != null && items.size() > 0) {
                                    for (AwsDownloadItem item : items) {
                                        if (item.getFilePath().equals(path)) {
                                            updateSignatureDatabase(syncThread.mParent, syncThread.mCloud, signaturesDB, uuid, item.getMetadata());
                                            items.removeElement(item);

                                            if (items.size() <= 0) {
                                                if (errorCount <= 0) {
                                                    Log.d("elazarkin10", "finish all");
                                                    callback.onSuccess();
                                                } else {
                                                    Log.d("elazarkin10", "not success 2");
                                                    callback.notSuccessFinish();
                                                }
                                            } else {
                                                Log.d("elazarkin10", "onProccess!");
                                                callback.onProgress(path);
                                            }
                                            return;
                                        }
                                    }
                                } else {
                                    callback.onError(ILesswalkService.CODE_ERROR_METADATA_NOT_CREATED);
                                    return;
                                }

                                callback.onError(ILesswalkService.CODE_ERROR_STRANGE_METADATA_ITEM);
                            }

                            @Override
                            public void onDownloadError(String path, int errorId, Exception ex) {
                                errorCount++;

                                for (AwsDownloadItem item : items) {
                                    if (item.getFilePath().equals(path)) {
                                        items.removeElement(item);

                                        if (items.size() <= 0) {
                                            callback.notSuccessFinish();
                                        } else {
                                            callback.onError(ILesswalkService.REGISTRATION_ERROR_DOWNLOAD_SIGNATURES);
                                        }
                                    }
                                }
                                Log.d("elazarkin", "onDownloadError" + path + " errorID=" + errorId + " " + ex.getMessage());
                            }

                            @Override
                            public void onMetadataReceived(AwsDownloadItem item) {
                                Log.d("elazarkin10", "onMetadataReceived " + item.getFilePath());

                                if (items == null) {
                                    items = new Vector<AwsDownloadItem>();
                                }

                                items.add(item);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        callback.onError(ILesswalkService.CODE_ERROR_CLOUD_CONNECTION_FAILED);
                    }
                } else callback.onSuccess();
            } else {
                callback.onSuccess();
            }
        }

        @Override
        public SyncThreadIDs getID() {
            return SyncThreadIDs.SYNC_SOME_CONTACT_SIGNATURES_TASK;
        }
    }

    private class DeleteAccountTask implements ISyncThreadTasks {
        private AWS.OnRequestListener callback = null;

        public DeleteAccountTask(AWS.OnRequestListener onRequestListener) {
            callback = onRequestListener;
        }

        @Override
        public void DO(SyncThread syncThread, LesswalkDbHelper userDB, LesswalkDbHelper signaturesDB) {
            callback.onStarted();
            if (mCloud.deleteUserAccount(userContent.getKey())) {
                Log.d("elazarkin16", "deleteUserAccount success before remove dir");
                Utils.removeDir(userContentDir);
                reloadUserJson();
                Log.d("elazarkin16", "removeDir success before send onfinish");
                callback.onFinished();
            } else {
                Log.d("elazarkin16", "SyncThread before send onError");
                callback.onError(-1);
            }
        }

        @Override
        public SyncThreadIDs getID() {
            return SyncThreadIDs.DELETE_USER_ACCOUNT;
        }
    }

    private class DeleteSignatureTask implements ISyncThreadTasks {
        private final String signatureKey;
        private AWS.OnRequestListener callback = null;

        public DeleteSignatureTask(String key, AWS.OnRequestListener onRequestListener) {
            signatureKey = key;
            callback = onRequestListener;
        }

        @Override
        public void DO(SyncThread syncThread, LesswalkDbHelper userDB, LesswalkDbHelper signaturesDB) {
            callback.onStarted();
            try {
                Vector<String> signatureList = mCloud.findSignaturesUuidsByOwnerUuid(userContent.getKey());
                boolean wasFound = false;
                for (String uuid : signatureList) {
                    if (uuid.equals(signatureKey)) {
                        wasFound = true;
                        break;
                    }
                }
                if (!wasFound)
                    throw new Exception("No signature was found on the current user list of signatures");
                File localSignatureFile;
                localSignatureFile = mCloud.getLocalSignatureFile(signatureKey);
                if (!localSignatureFile.exists() || !localSignatureFile.isFile())
                    throw new Exception("Signature file not found");
                if (!localSignatureFile.delete())
                    throw new Exception("Signature file could not get deleted");
                // TODO LOCAL_DB_LINES BEGIN: start using the class 'LocalDB' and move these following lines as a method there:
                {
                    String sql = "select " + SIGNATURE_UUID_ROW + " from " + signaturesDB.table_name + " where " + SIGNATURE_UUID_ROW + "=?";
                    String selections[] = {signatureKey};
                    Cursor cursor = signaturesDB.getReadableDatabase().rawQuery(sql, selections);
                    if (cursor.getCount() <= 0 || !cursor.moveToFirst() && !cursor.isLast()) {
                        cursor.close();
                        throw new Exception("Signature search within the local DB came up short");
                    }
                    String localSigUuid = cursor.getString(cursor.getColumnIndex(SIGNATURE_UUID_ROW));
                    cursor.close();
                    if (!signatureKey.equals("" + localSigUuid))
                        throw new Exception("Failed to find the signature (" + signatureKey + ") within the local DB (" + localSigUuid + ")");
                    int numOfDeletedRows = signaturesDB.getWritableDatabase().delete(signaturesDB.table_name, SIGNATURE_UUID_ROW + "=?", selections);
                    if (numOfDeletedRows != 1)
                        throw new Exception("Failed to delete just that one signature from the local DB (" + numOfDeletedRows + " rows were deleted)");
                }
                //
                // and delete it also from the user's row:
                {
                    String sql = "select "+USER_UUID_ROW+","+SIGNATURES_ROW+" from " + userDB.table_name + " where " + SIGNATURES_ROW + " like ?";
                    String selections[] = {"%"+signatureKey+"%"};
                    SQLiteDatabase db = userDB.getWritableDatabase();
                    Cursor cursor = db.rawQuery(sql, selections);
                    if (cursor.getCount() <= 0 || !cursor.moveToFirst() && !cursor.isLast()) {
                        cursor.close();
                        throw new Exception("Signature search within the local DB came up short");
                    }
                    do {
                        String userKey = cursor.getString(cursor.getColumnIndex(USER_UUID_ROW));
                        String signatures = cursor.getString(cursor.getColumnIndex(SIGNATURES_ROW));
                        signatures = signatures.replaceAll(signatureKey, "").replaceAll("(^,)|(,$)", "").replaceAll(",,", ",");
                        ContentValues values = new ContentValues();
                        values.put(SIGNATURES_ROW, signatures);
                        db.update(userDB.table_name, values, String.format("%s==?", USER_UUID_ROW), new String[]{userKey});
                        cursor.moveToNext();
                    }while(!cursor.isAfterLast());
                    cursor.close();
                    db.close();
                }
                //
                // TODO LOCAL_DB_LINES END
                if (!mCloud.deleteSignature(userContent.getKey(), signatureKey))
                    throw new Exception("Cloud failed to delete signature");
                Log.d(TAG, "deleteSignature succeeded to delete " + signatureKey);
                reloadUserJson();//TODO should we call this or it's equivalent here?
                callback.onFinished();
            } catch (Exception e) {
                Log.d(TAG, "DeleteSignatureTask failed - key: " + signatureKey + ", e: " + e.getMessage());
                e.printStackTrace();
                callback.onError(-1);
            }
        }

        @Override
        public SyncThreadIDs getID() {
            return SyncThreadIDs.DELETE_SIGNATURE;
        }
    }

    class CheckIfContactNeedBeUpdated implements ISyncThreadTasks {
        DataBaseUpdater dataBaseUpdater = null;
        CarusselContact contact         = null;

        CheckIfContactNeedBeUpdated(DataBaseUpdater _dataBaseUpdater, CarusselContact c) {
            dataBaseUpdater = _dataBaseUpdater;
            contact = c;
        }

        @Override
        public void DO(final SyncThread syncThread, final LesswalkDbHelper userDB, final LesswalkDbHelper signaturesDB) {
            dataBaseUpdater.updateContactIfNeed(contact);
        }

        @Override
        public SyncThreadIDs getID() {
            return SyncThreadIDs.CHECK_CONTACT_UPDATE;
        }
    }

    SyncThread(MainService parent) {
        Vector<DataBaseColums> usersColums = new Vector<DataBaseColums>();
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
        signaturesColums.add(new DataBaseColums(SIGNATURE_ETAG_ROW, DataBaseColums.TEXT));

        signaturesDB = new LesswalkDbHelper(mParent, "signatures", signaturesColums);

        if (!getAssetsDir().exists() || getAssetsDir().listFiles().length <= 0) {
            new Thread() {
                @Override
                public void run() {
                    mCloud.downloadAssets(mParent.getFilesDir().getAbsolutePath(), new AWS.OnDownloadListener() {
                        @Override
                        public void onDownloadStarted(String path) {
                        }

                        @Override
                        public void onDownloadProgress(String path, float percentage) {
                        }

                        @Override
                        public void onDownloadFinished(String path) {
                            ZipManager.unzip(mParent, path, getAssetsDir().getAbsolutePath());
                        }

                        @Override
                        public void onDownloadError(String path, int errorId, Exception ex) {
                        }

                        @Override
                        public void onMetadataReceived(AwsDownloadItem dowloadItem) {
                        }
                    });
                }
            }.start();
        }
    }

    private File getAssetsDir() {
        File dir = new File(mParent.getFilesDir(), "assets");

        return dir;
    }

    ;

    private class ContactUpdateTask {
        static final int COMMAND_UPDATE_DB    = 0x1;
        static final int COMMAND_DOWNLOAD_ZIP = 0x2;
        static final int COMMAND_DELETE_ZIP   = 0x3;
        static final int COMMAND_UPDATE_ZIP   = 0x4;
        static final int UPDATE_SIGNATURE_DB  = 0x5;

        int    command     = 0x0;
        String description = null;

        ContactUpdateTask(int _command, String _description) {
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

    private class DataBaseUpdater extends Thread {
        private MainService              service = null;
        private SyncThread               parent  = null;
        private Vector<ISyncThreadTasks> tasks   = null;

        public DataBaseUpdater(SyncThread _parent, MainService _service) {
            parent = _parent;
            service = _service;

            tasks = new Vector<ISyncThreadTasks>();
        }

        @Override
        public void run() {
            long TIME_TO_SYNC_CONTACTS = 10 * 60 * 1000;
            long IDLE_SLEEP_TYPE = 200;
            long t0 = 0L;
            long currentTime = 0L;

            Vector<CarusselContact> contacts = new Vector<CarusselContact>();

            while (isAlive) {
                contacts.removeAllElements();
                service.getContactManager().fillContactVector(contacts);

                if (tasks.size() > 0) {
                    try {
                        mutex.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("elazarkin8", "do task: " + tasks.elementAt(0).getID());
                    tasks.elementAt(0).DO(parent, parent.usersDB, parent.signaturesDB);
                    tasks.removeElementAt(0);
                    mutex.release();
                } else {
                    try {
                        sleep(IDLE_SLEEP_TYPE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void updateContactIfNeed(CarusselContact c) {
            String number[] = null;
            String userUuid = null;
            Vector<String> sinaturesList = null;
            String signaturesString = null;
            Vector<ContactUpdateTask> tasks = null;

            if (c == null) return;

            number = PhoneUtils.splitPhoneNumber(c.getNumber());

            if (number == null) return;

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " not lesswalk number");

            userUuid = mCloud.getUserUuid(number[PhoneUtils.PHONE_INDEX_COUNTRY], number[PhoneUtils.PHONE_INDEX_MAIN]);

            if (userUuid == null) return;

            Log.d("elazarkin", "fixed number: " + number[PhoneUtils.PHONE_INDEX_COUNTRY] + " " + number[PhoneUtils.PHONE_INDEX_MAIN] + " uuid:" + userUuid);

            sinaturesList = mCloud.findSignaturesUuidsByOwnerUuid(userUuid);

            signaturesString = SignatureListToString(sinaturesList);

            tasks = new Vector<ContactUpdateTask>();

            checkUserChanges(parent.usersDB, number, userUuid, signaturesString, tasks);

            if (tasks.size() > 0) {
                for (ContactUpdateTask t : tasks) {
                    switch (t.command) {
                        case ContactUpdateTask.COMMAND_UPDATE_DB: {
                            Log.d("elazarkin", "will update database");
                            updateUserDataBase(parent.usersDB, number, userUuid, signaturesString);
                            break;
                        }
                        case ContactUpdateTask.COMMAND_DOWNLOAD_ZIP: {
                            Log.d("elazarkin", "will dowload zip");
                            downloadZipIfNeed(t.description);
                            break;
                        }
                        case ContactUpdateTask.UPDATE_SIGNATURE_DB: {
                            Log.d("elazarkin", "will update signatureDB");
                            updateSignatureDatabase(parent.mParent, parent.mCloud, parent.signaturesDB, t.description, null);
                            break;
                        }
                    }
                }
            } else Log.d("elazarkin", "no database need update!");
        }

        private synchronized void checkUserChanges(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString, Vector<ContactUpdateTask> tasks) {
            String fullNumber = PhoneUtils.splitedNumberToFullNumber(number);
            String[] projection = new String[db.colums.size()];
            String selection = FULL_PHONE_NUMBER_ROW + " = ?";
            String selectionArgs[] = {fullNumber};
            Cursor cursor = null;
            String sigs[] = null;

            Log.d("elazarkin", "check database by " + fullNumber + " number");

            for (int i = 0; i < projection.length; i++) {
                projection[i] = db.colums.elementAt(i).name;
            }

            cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

            if (cursor == null || cursor.getCount() <= 0) {
                tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_UPDATE_DB, ""));
            } else if (cursor.getCount() != 1) {
                Log.e(TAG, "some problem with primary signatureKey in " + db.table_name + " database");
            } else {
                while (cursor.moveToNext()) {
                    String signatures = cursor.getString(cursor.getColumnIndexOrThrow(SIGNATURES_ROW));
                    String uuid = cursor.getString(cursor.getColumnIndexOrThrow(USER_UUID_ROW));

                    if (!userUuid.equals("" + uuid) || !signaturesString.equals("" + signatures)) {
                        if (!userUuid.equals("" + uuid)) {
                            tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_UPDATE_DB, uuid));
                        }
                    }
                }
            }

            if (cursor != null) cursor.close();

            // DOWNLOAD MISSED ZIPS TASKS
            sigs = signaturesString.split(",");

            if (sigs != null || sigs.length > 0) {
                for (String uuid : sigs) {
                    if (uuid.length() > 0) {
                        File zip = mCloud.getLocalSignatureFile(uuid);

                        if (!zip.exists()) {
                            tasks.add(new ContactUpdateTask(ContactUpdateTask.COMMAND_DOWNLOAD_ZIP, uuid));
                        } else {
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

        private boolean downloadZipIfNeed(String s) {
            File zip = mCloud.getLocalSignatureFile(s);

            if (!zip.exists()) {
                mCloud.downloadSignature(s, new AWS.OnDownloadListener() {
                    ObjectMetadata mFileMetadata = null;

                    @Override
                    public void onDownloadStarted(String path) {
                        Log.d("elazarkin", "onDownloadStarted " + path);
                    }

                    @Override
                    public void onDownloadProgress(String path, float percentage) {
                        Log.d("elazarkin", "onDownloadProgress " + path + "(" + percentage + "%)");
                    }

                    @Override
                    public void onDownloadFinished(String path) {
                        // TODO improve syntax
                        String fileName = new File(path).getName();
                        String uuid = fileName.substring(0, fileName.length() - 4);

                        Log.d("elazarkin", "onDownloadFinished" + path + " uuid = " + uuid);
                        updateSignatureDatabase(parent.mParent, parent.mCloud, parent.signaturesDB, uuid, mFileMetadata);
                    }

                    @Override
                    public void onDownloadError(String path, int errorId, Exception ex) {
                        Log.d("elazarkin", "onDownloadError" + path + " errorID=" + errorId + " " + ex.getMessage());
                    }

                    @Override
                    public void onMetadataReceived(AwsDownloadItem item) {
                        mFileMetadata = item.getMetadata();
                    }
                });

                return true;
            }

            return false;
        }
    }

    private class DataBaseColums {
        static final String INTEGER_PRIMARY_KEY = "INTEGER PRIMARY KEY";
        static final String INTEGER             = "INTEGER";
        static final String TEXT_PRIMARY_KEY    = "TEXT PRIMARY KEY";
        static final String TEXT                = "TEXT";

        String name = null;
        String type = null;

        public DataBaseColums(String _name, String _type) {
            name = "" + _name;
            type = "" + _type;
        }
    }

    private class LesswalkDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        //public static final String DATABASE_NAME    = "users.db";

        private Vector<DataBaseColums> colums     = null;
        private String                 table_name = null;

        public LesswalkDbHelper(Context context, String _table_name, Vector<DataBaseColums> _colums) {
            super(context, _table_name + ".db", null, DATABASE_VERSION);

            colums = new Vector<DataBaseColums>(_colums);
            table_name = "" + _table_name;
        }

        public void onCreate(SQLiteDatabase db) {
            String SQL_CREATE_ENTRIES = "CREATE TABLE " + table_name + " (";

            for (int i = 0; i < colums.size(); i++) {
                SQL_CREATE_ENTRIES += (colums.elementAt(i).name + " " + colums.elementAt(i).type);

                if (i < colums.size() - 1) {
                    SQL_CREATE_ENTRIES += ",";
                } else SQL_CREATE_ENTRIES += ")";
            }
            Log.d(TAG, "Create Table: " + SQL_CREATE_ENTRIES);

            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + table_name;
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public void start() {
        if (!isAlive) {
            isAlive = true;
            (dataBaseUpdater = new DataBaseUpdater(this, mParent)).start();
        }
    }

    public void stop() {
        isAlive = false;
        try {
            dataBaseUpdater.join();
            dataBaseUpdater = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void fillSignaturesOfPhoneNumber(String phoneNumber, Vector<CarruselJson> container) {
        String number[] = PhoneUtils.splitPhoneNumber(phoneNumber);
        String fixedNumber = PhoneUtils.splitedNumberToFullNumber(number);
        String signatures[] = getSignaturesOfNumber(usersDB, fixedNumber);

        Log.d("elazarkin1", "signatures = " + signatures);

        if (signatures != null) {
            Log.d("elazarkin1", "signatures.length = " + signatures.length);

            for (String uuid : signatures) {
                if (uuid.length() > 0) {
                    CarruselJson item = getUserSignatureObject(mParent, mCloud, uuid);

                    if (item != null) {
                        container.add(item);
                        Log.d("elazarkin1", "add " + uuid + " type = " + container.lastElement().getType());
                    }
                }
            }
        }
    }

    private String getTypeOfSignature(LesswalkDbHelper db, String uuid) {
        Cursor cursor = null;
        String[] projection = new String[db.colums.size()];
        String selection = SIGNATURE_UUID_ROW + " = ?";
        String selectionArgs[] = {uuid};
        String type = null;

        for (int i = 0; i < projection.length; i++) {
            projection[i] = db.colums.elementAt(i).name;
        }

        cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            // DO NOTHING
        } else if (cursor.getCount() != 1) {
            Log.e(TAG, "some problem with primary signatureKey in " + db.table_name + " database");
        } else {
            while (cursor.moveToNext()) {
                type = cursor.getString(cursor.getColumnIndexOrThrow(TYPE_ROW));
            }
        }

        if (cursor != null) cursor.close();

        return type;
    }

    private String[] getSignaturesOfNumber(LesswalkDbHelper db, String fixedNumber) {
        Cursor cursor = null;
        String[] projection = new String[db.colums.size()];
        String selection = FULL_PHONE_NUMBER_ROW + " = ?";
        String selectionArgs[] = {fixedNumber};
        String signatures[] = null;

        for (int i = 0; i < projection.length; i++) {
            projection[i] = db.colums.elementAt(i).name;
        }

        cursor = db.getReadableDatabase().query(db.table_name, projection, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            Log.d("elazarkin1", "getSignaturesOfNumber cursor == null || cursor.getCount() number=" + fixedNumber);
            // DO NOTHING
        } else if (cursor.getCount() != 1) {
            Log.e(TAG, "some problem with primary signatureKey in " + db.table_name + " database");
            Log.d("elazarkin1", "some problem with primary signatureKey in " + db.table_name + " database");
        } else {
            while (cursor.moveToNext()) {
                String signaturesValue = cursor.getString(cursor.getColumnIndexOrThrow(SIGNATURES_ROW));
                //
                Log.d("elazarkin1", "signatureValue is: " + signaturesValue);
                //
                signatures = signaturesValue.split(",");
            }
        }

        if (cursor != null) cursor.close();

        return signatures;
    }

    private static synchronized void updateUserDataBase(LesswalkDbHelper db, String[] number, String userUuid, String signaturesString) {
        String fullNumber = PhoneUtils.splitedNumberToFullNumber(number);
        ContentValues values = new ContentValues();
        //
        values.put(FULL_PHONE_NUMBER_ROW, fullNumber);
        values.put(USER_UUID_ROW, userUuid);
        values.put(SIGNATURES_ROW, signaturesString);

        db.getWritableDatabase().replace(db.table_name, null, values);
    }

    private static synchronized CarruselJson getUserSignatureObject(Context context, Cloud mCloud, String uuid) {
        Gson gson = new Gson();
        File outDir = new File(context.getCacheDir(), "updateSignaturesDB");
        String contentFileName = "content.json";
        CarruselJson ret = null;

        if (mCloud.unzipFileFromSignatureByUUID(uuid, outDir, contentFileName)) {
            File contentFile = new File(outDir, contentFileName);

            try {
                ret = gson.fromJson(new FileReader(contentFile), CarruselJson.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    private static synchronized void updateSignatureDatabase(Context context, Cloud mCloud, LesswalkDbHelper db, String uuid, ObjectMetadata metadata) {
        File outDir = new File(context.getCacheDir(), "updateSignaturesDB");
        ContentValues values = new ContentValues();
        String contentFileName = "content.json";

        if (mCloud.unzipFileFromSignatureByUUID(uuid, outDir, contentFileName)) {
            try {
                CarruselJson currusel = new Gson().fromJson(new FileReader(new File(outDir, contentFileName)), CarruselJson.class);

                values.put(SIGNATURE_UUID_ROW, uuid);
                values.put(TYPE_ROW, currusel.getType());
                if (metadata != null) {
                    values.put(SIGNATURE_ETAG_ROW, "" + metadata.getETag());
                }

                db.getWritableDatabase().replace(db.table_name, null, values);

            } catch (Exception e) {
                Log.d("elazarkin", "updateSignatureDatabase error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static String SignatureListToString(Vector<String> signaturesList) {
        String ret = "";

        //Collections.sort(signaturesList);

        for (int i = 0; i < signaturesList.size(); i++) {
            ret += signaturesList.elementAt(i);

            if (i < signaturesList.size() - 1) ret += ",";
        }

        return ret;
    }

    void addImportantTask(ISyncThreadTasks task) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("elazarkin8", "add addImportantTask " + task.getID());
        dataBaseUpdater.tasks.add(0, task);
        mutex.release();
    }

    public void updateUserJson(String first, String last, String _number) {
        Gson gson = new Gson();
        String number[] = PhoneUtils.splitPhoneNumber(_number);
        String signupNumber = String.format(Locale.getDefault(), "+%s %s", number[0], number[1]);

        if (userContent == null) userContent = new UserContent();

        if (userContent.getKey() == null) userContent.setKey(UUID.randomUUID().toString());
        userContent.setFirst_name(first);
        userContent.setLast_name(last);
        userContent.setCountry_dial_code(number[PhoneUtils.PHONE_INDEX_COUNTRY]);
        userContent.setPhone_number(number[PhoneUtils.PHONE_INDEX_MAIN]);

        try {
            String json = gson.toJson(userContent);
            BufferedWriter bw = new BufferedWriter(new FileWriter(userJsonFile));
            bw.write(json);
            bw.close();
            Log.d("elazarkin", "" + json);

            mCloud.uploadUser(userContent.getKey(), json, userContentDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUserAccount(AWS.OnRequestListener onRequestListener) {
        addImportantTask(new DeleteAccountTask(onRequestListener));
    }

    public void deleteSignature(String key, AWS.OnRequestListener onRequestListener) {
        addImportantTask(new DeleteSignatureTask(key, onRequestListener));
    }

    public boolean checkLogin() {
        Log.d("elazarkin17", "" + (userContent != null ? userContent.getKey() : "userContent=null"));
        return userContent != null && userContent.getKey() != null;
    }

    public void saveSignature(String key, File dir, AWS.OnRequestListener onRequestListener) {
        addImportantTask(new SaveSignatureTask(key, dir, onRequestListener));
    }


    private class SaveSignatureTask implements ISyncThreadTasks {
        private AWS.OnRequestListener callback = null;
        private final String signatureKey;
        private final File   dir;

        public SaveSignatureTask(String signatureKey, File dir, AWS.OnRequestListener onRequestListener) {
            callback = onRequestListener;
            this.signatureKey = signatureKey;
            this.dir = dir;
        }

        @Override
        public void DO(SyncThread syncThread, LesswalkDbHelper userDB, LesswalkDbHelper signaturesDB) {
            callback.onStarted();
            //TODO saveSignature syncThread implementation

            //
            // zip signature:
            //
            File zipFile;
            boolean hasCreated;
            try {
                File internalStorageDir = mParent.getApplicationContext().getFilesDir();
                File signaturesZipsDir = new File(internalStorageDir, "sig_zips");
                if (!signaturesZipsDir.exists() && !signaturesZipsDir.mkdirs())
                    throw new Exception("The path '" + signaturesZipsDir.getAbsolutePath() + "' failed to get created");
                zipFile = new File(signaturesZipsDir, signatureKey + ".zip");
                hasCreated = zipFile.createNewFile();
                if (!hasCreated || !zipFile.exists())
                    throw new Exception("Zip couldn't get created");
                ZipManager.zip(dir.listFiles(), zipFile);
                if (zipFile.length() < 2) throw new Exception("Zip size doesn't make any sense");
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(-1);
                return;
            }
            //
            // upload signature:
            //
            String jsonPath = new File(dir, "content.json").getAbsolutePath();
            String eTag = mCloud.uploadSignature(signatureKey, userContent.getKey(), jsonPath, zipFile.getAbsolutePath());
            if (eTag == null) {
                callback.onError(-1);
                return;
            }
            Log.d(TAG, "uploaded signature. etag: " + eTag);
            addImportantTask(new SyncSomeContactSignaturesTask(getLocalNumber(), new ILesswalkService.ISetLocalNumberCallback() {
                @Override
                public void onSuccess() {
                    callback.onFinished();
                }

                @Override
                public void onError(int errorID) {
                    callback.onError(0);
                }

                @Override
                public void onProgress(String path) {
                }

                @Override
                public void notSuccessFinish() {
                    callback.onError(0);
                }
            }));

        }

        @Override
        public SyncThreadIDs getID() {
            return SyncThreadIDs.SAVE_SIGNATURE;
        }
    }
}

