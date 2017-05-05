package com.lesswalk.database;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.amazonaws.services.s3.model.ObjectMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by elad on 29/11/16.
 */

public class AmazonCloud extends Cloud
{

    protected static final String TAG                              = "AmazonCloud";
    protected static final String CLOUD_SCHEME                     = "http";//"https";//http
    protected static final String CLOUD_HOST                       = "52.70.155.39";
    protected static final int    CLOUD_PORT                       = 30082;//443;//30082
    protected static final String CLOUD_MODULE_signature           = "signature";
    protected static final String CLOUD_FUNCTION_findByUser        = "findByOwner";
    protected static final String GET_REQ_SIGNATURE_BY_OWNER       = "%s://%s:%d/cmd/%s/%s/%s";
    protected static final String PUT_REQ_SIGNATURE_BY_OWNER       = "%s://%s:%d/cmd/%s/%s/%s";
    protected static final String CLOUD_MODULE_user                = "user";
    protected static final String CLOUD_FUNCTION_findByPhoneNumber = "findByPhoneNumber";
    protected static final String CLOUD_FUNCTION_sendVerSms        = "request-confirmation-sms";
    protected static final String CLOUD_FUNCTION_upload            = "upload";
    protected static final String GET_REQ_USER_BY_PHONE            = "%s://%s:%d/cmd/%s/%s?phone_number=%s&country_code=%s";
    protected static final String PUT_REQ_USER_BY_PHONE            = "%s://%s:%d/cmd/%s/%s";//?phone_number=%s&country_code=%s
    protected static final String PUT_REQ_USER_VER_SMS             = "%s://%s:%d/cmd/%s/%s";
    protected static final String PUT_REQ_USER_UPLOAD              = "%s://%s:%d/cmd/%s/%s/%s";
    protected static final String PUT_VERSMS_field_countryCode     = "country-code";
    protected static final String PUT_VERSMS_field_phone           = "phone-number";
    protected static final String PUT_VERSMS_field_veriCode        = "confirmation-code";
    protected static final String PUT_USER_UPLOAD_field_countryCode= "country_dial_code";//"0"
    protected static final String PUT_USER_UPLOAD_field_firstName  = "first_name";//"elad"
    protected static final String PUT_USER_UPLOAD_field_key        = "key";//013F70D2-3141-4BB3-8830-F3F6142BC9D9
    protected static final String PUT_USER_UPLOAD_field_lastName   = "last_name";//"elad11_last"
    protected static final String PUT_USER_UPLOAD_field_phoneNumber= "phone_number";//"+0 888 888 888 8"
    protected static final String CLOUD_JSON_key                   = "key";
    protected static final String CLOUD_JSON_owner                 = "owner";
    private static final   String SIGNATURES_EXTRACT_PATH          = "sig_extracted";
    private static final   String SIGNATURES_PATH                  = "signatures";
    private static final   String SIGNATURE_EXTENSION              = ".zip";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected Context               mContext;
    protected CognitoSyncManager    syncClient;
    protected OkHttpClient          httpClient = null;

    public AmazonCloud(Context context)
    {
        super();
        mContext = context;
        syncClient = AWS.getSyncClient(mContext);
        httpClient = new OkHttpClient();
    }

    @Override
    public void createUser(String phone, String uuid, final Cloud.I_ProcessListener listener)
    {
        Dataset users = syncClient.openOrCreateDataset(Table.users);
        users.put(Field.users.get(Field.UsersField.PHONE), phone);
        users.put(Field.users.get(Field.UsersField.UUID), uuid);
        users.synchronize(new Dataset.SyncCallback()
        {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords)
            {
                Log.v(TAG, String.format(Locale.getDefault(), "onSuccess:\t%s\n%s", dataset.toString(), updatedRecords.toString()));
                HashMap<String, String> result = new HashMap<>();
                result.put("uuid", dataset.get("uuid"));
                result.put("dataset", dataset.getAll().toString());
                result.put("updatedRecords", updatedRecords.toString());
                listener.onSuccess(result);
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts)
            {
                Log.v(TAG, "onConflict");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName)
            {
                Log.v(TAG, "onDatasetDeleted");
                return false;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames)
            {
                Log.v(TAG, "onDatasetsMerged");
                return false;
            }

            @Override
            public void onFailure(DataStorageException dse)
            {
                Log.v(TAG, String.format(Locale.getDefault(), "onFailure:\t%s", dse.getMessage()));
                HashMap<String, String> result = new HashMap<>();
                result.put("DataStorageException", dse.getMessage());
                listener.onFailure(result);
            }
        });
    }

    String reqHttpGet(String url)
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        String   result   = null;
        try
        {
            response = httpClient.newCall(request).execute();
            result = response.body().string();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONObject getUserJson(String phone, String countryCode)
    {
        String url = String.format
        (
            Locale.getDefault(),
                PUT_REQ_USER_BY_PHONE,
            CLOUD_SCHEME,
            CLOUD_HOST,
            CLOUD_PORT,
            CLOUD_MODULE_user,
            CLOUD_FUNCTION_findByPhoneNumber
        );
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("phone_number", phone);
            jsonParams.put("country_code", countryCode);
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't build the params JSON for the post request. e.msg="+e.getMessage());
            e.printStackTrace();
            return null;
        }
        String     responseBody = reqHttpPut(url, jsonParams.toString());
        JSONArray  jsonArr      = null;
        JSONObject json         = null;
        try
        {
            jsonArr = new JSONArray(responseBody);
            if (jsonArr.length() == 0) return null;
            json = jsonArr.getJSONObject(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String getUserUuid(String countryCode, String phone)
    {
        JSONObject userJson = getUserJson(phone, countryCode);
        if (userJson == null || !userJson.has(CLOUD_JSON_owner)) return null;
        String userUuid = null;
        try
        {
            userUuid = userJson.getString(CLOUD_JSON_owner);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return userUuid;
    }

//    /updateAllReferences


//    public JSONArray updateAllReferences()
//    {
//        String url = String.format
//        (
//                Locale.getDefault(),
//                "%s://%s:%d/cmd/%s/%s/%s",
//                CLOUD_SCHEME,
//                CLOUD_HOST,
//                CLOUD_PORT,
//                CLOUD_MODULE_signature,
//                "updateAllReferences"
//        );
//
//        return response(url);
//    };

    @Override
    public JSONArray findSignaturesByOwner(String uuid)
    {
        String url = String.format
        (
                Locale.getDefault(),
                GET_REQ_SIGNATURE_BY_OWNER,
                CLOUD_SCHEME,
                CLOUD_HOST,
                CLOUD_PORT,
                CLOUD_MODULE_signature,
                CLOUD_FUNCTION_findByUser,
                uuid
        );
        JSONArray jsonArray;
        try
        {
            String jsonArrStr = reqHttpGet(url);
            jsonArray = new JSONArray(jsonArrStr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }

    @Override
    public Vector<String> findSignaturesUuidsByOwnerUuid(String uuid)
    {
        Vector<String> list              = null;
        JSONArray      signaturesJsonArr = null;

        if (null == uuid) return null;

        list = new Vector<String>();

        signaturesJsonArr = findSignaturesByOwner(uuid);

        Log.d("elazarkin", "" + signaturesJsonArr.toString());

        try
        {
            for (int i = 0; i < signaturesJsonArr.length(); i++)
            {
                JSONObject signatureJson = signaturesJsonArr.getJSONObject(i);
                if (signatureJson == null || !signatureJson.has(CLOUD_JSON_key))
                {
                    Log.e(TAG, "findSignaturesUuidsByOwnerUuid - i=" + i + ": " + ((signatureJson == null) ? "signatureJson==null" : "signatureJson.has(CLOUD_JSON_key)==false"));
                    return null;
                }
                list.add(signatureJson.getString(CLOUD_JSON_key));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public File getSignutareFilePathByUUID(String uuid)
    {
        File dir = new File(mContext.getFilesDir(), SIGNATURES_PATH);

        dir.mkdir();dir.mkdirs();

        return new File(dir, uuid + SIGNATURE_EXTENSION);
    }

    @Override
    public String downloadSignature(final String uuid, final AWS.OnDownloadListener onDownloadListener)
    {
//        File signatureDir = Utils.createDirIfNeeded(mContext, SIGNATURES_PATH);
//        if (signatureDir == null) return null;
//        String filePath = new File(signatureDir, uuid + SIGNATURE_EXTENSION).getPath();
        String filePath = getSignutareFilePathByUUID(uuid).getPath();
        //AWS.download(mContext, SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION, filePath, onDownloadListener);
        String pathOnServer = SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION;
        ObjectMetadata fileMetadata = AWS.getFileMetadata(mContext, pathOnServer);
        onDownloadListener.onMetadataReceived(fileMetadata);
        AWS.download(mContext, pathOnServer, filePath, onDownloadListener);
        return filePath;
    }

//    @Override
//    public String getSignatureHeader(String uuid, AWS.OnDownloadListener onDownloadListener) {
//        String filePath = getSignutareFilePathByUUID(uuid).getPath();
//        //AWS.download(mContext, SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION, filePath, onDownloadListener);
//        //AWS.download(mContext, SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION, filePath, onDownloadListener);
//        AWS.getFileHeader(mContext, SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION, filePath);
//        return filePath;
//    }

    @Override
    public String unzipSignatureByUUID(String uuid, File unzippedDir)
    {
        String zipPath = getSignutareFilePathByUUID(uuid).getPath();

        unzippedDir.mkdir();unzippedDir.mkdirs();

        if (!ZipManager.unzip(mContext.getApplicationContext(), zipPath, unzippedDir.getPath()))
        {
            Log.e(TAG, String.format("Unzip failed for - '%s'", zipPath));
        }
        else
        {
            Log.d(TAG, String.format("Unzip succeeded into - '%s'", unzippedDir.getPath()));
        }
        return unzippedDir.getPath();
    }

    @Override
    public boolean unzipFileFromSignatureByUUID(String uuid, File unzippedDir, String file)
    {
        String zipPath = getSignutareFilePathByUUID(uuid).getPath();

        unzippedDir.mkdir();unzippedDir.mkdirs();

        return ZipManager.unzip(mContext.getApplicationContext(), zipPath, unzippedDir.getPath(), file);
    }

    String reqHttpPut(String url, String json)
    {
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        Response response = null;
        String   result   = null;
        try
        {
            response = httpClient.newCall(request).execute();
            result = response.body().string();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String sendVerificationSms(String countryCode, String phone, String verificationCode)
    {
        String url = String.format
                (
                        Locale.getDefault(),
                        PUT_REQ_USER_VER_SMS,
                        CLOUD_SCHEME,
                        CLOUD_HOST,
                        CLOUD_PORT,
                        CLOUD_MODULE_user,
                        CLOUD_FUNCTION_sendVerSms
                );
        JSONObject bodyJson = new JSONObject();
        JSONObject json         = null;
        try {
            bodyJson.put(PUT_VERSMS_field_countryCode, countryCode);
            bodyJson.put(PUT_VERSMS_field_phone, phone);
            bodyJson.put(PUT_VERSMS_field_veriCode, verificationCode);
            String     responseBody = reqHttpPut(url, bodyJson.toString());
            JSONArray  jsonArr      = null;
            jsonArr = new JSONArray(responseBody);
            if (jsonArr.length() == 0) return null;
            json = jsonArr.getJSONObject(0);
            if (null == json) return "";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
        return json.toString();
    }

    @Override
    public String uploadUser(String countryCode, String phone, String firstName, String lastName) {
        final String uuid = AWS.generateKey();
        String url = String.format
                (
                        Locale.getDefault(),
                        PUT_REQ_USER_UPLOAD,
                        CLOUD_SCHEME,
                        CLOUD_HOST,
                        CLOUD_PORT,
                        CLOUD_MODULE_user,
                        CLOUD_FUNCTION_upload,
                        uuid
                );
        JSONObject bodyJson = new JSONObject();
        JSONObject json         = null;
        try {
            bodyJson.put(PUT_USER_UPLOAD_field_countryCode, countryCode);
            bodyJson.put(PUT_USER_UPLOAD_field_firstName, firstName);
            bodyJson.put(PUT_USER_UPLOAD_field_key, uuid);
            bodyJson.put(PUT_USER_UPLOAD_field_lastName, lastName);
            bodyJson.put(PUT_USER_UPLOAD_field_phoneNumber, phone);

            String     responseBody = reqHttpPut(url, bodyJson.toString());
            JSONObject  jsonObject      = null;
            jsonObject = new JSONObject(responseBody);
            json = jsonObject;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
        return json.toString();
    }

//    @Override
//    public List<String> findSignaturesUuidsByOwnerPhone(String countryCode, String phone)
//    {
//        String       userUuid        = getUserUuid(countryCode, phone);
//        List<String> signaturesUuids = findSignaturesUuidsByOwnerUuid(userUuid);
//        if (signaturesUuids == null)
//        {
//            return new ArrayList<>();
//        }
//        return signaturesUuids;
//    }

//    @Override
//    public String downloadAndUnzipSignature(final String uuid)
//    {
//        File signatureDir = Utils.createDirIfNeeded(mContext, SIGNATURES_PATH);
//        if (signatureDir == null) return null;
//        String filePath = new File(signatureDir, uuid + SIGNATURE_EXTENSION).getPath();
//        AWS.download(mContext, SIGNATURES_PATH + File.separator + uuid + SIGNATURE_EXTENSION, filePath, new AWS.OnDownloadListener()
//        {
//            @Override
//            public void onDownloadStarted(String path)
//            {
//
//            }
//
//            @Override
//            public void onDownloadProgress(String path, float percentage)
//            {
//
//            }
//
//            @Override
//            public void onDownloadFinished(String path)
//            {
//                File unzippedParentDir = new File(mContext.getExternalFilesDir(null), SIGNATURES_EXTRACT_PATH);
//                File unzippedDir;
//                if (null == Utils.createDirIfNeeded(mContext, unzippedParentDir.getPath()))
//                {
//                    Log.e(TAG, String.format("state: Unzip failed. Couldn't create dir %s - %s", unzippedParentDir.getPath(), path));
//                    return;
//                }
//                unzippedDir = new File(unzippedParentDir, uuid);
//                if (null == Utils.createDirIfNeeded(mContext, unzippedDir.getPath()))
//                {
//                    Log.e(TAG, String.format("state: Unzip failed. Couldn't create dir %s - %s", unzippedDir.getPath(), path));
//                    return;
//                }
//                if (!ZipManager.unzip(mContext.getApplicationContext(), path, unzippedDir.getPath()))
//                {
//                    Log.e(TAG, String.format("Unzip failed for - '%s'", path));
//                }
//                else
//                {
//                    Log.d(TAG, String.format("Unzip succeeded into - '%s'", unzippedDir.getPath()));
//                }
//            }
//
//            @Override
//            public void onDownloadError(String path, int errorId, Exception ex)
//            {
//                Log.e(TAG, String.format("state %s: {'%s', %d} for file '%s'", "Error", ex.getMessage(), errorId, path));
//            }
//        });
//        return filePath;
//    }
}
