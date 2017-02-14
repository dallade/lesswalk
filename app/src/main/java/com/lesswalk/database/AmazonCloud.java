package com.lesswalk.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by elad on 29/11/16.
 */

public class AmazonCloud extends Cloud {

    protected static final String TAG = "AmazonCloud";
    protected static final String CLOUD_SCHEME = "http";
    protected static final String CLOUD_HOST = "52.70.155.39";
    protected static final int CLOUD_PORT = 30082;
    protected static final String CLOUD_MODULE_signature = "signature";
    protected static final String CLOUD_FUNCTION_findByUser = "findByOwner";
    protected static final String GET_REQ_SIGNATURE_BY_OWNER = "%s://%s:%d/cmd/%s/%s/%s";
    protected static final String CLOUD_MODULE_user = "user";
    protected static final String CLOUD_FUNCTION_findByPhoneNumber = "findByPhoneNumber";
    protected static final String GET_REQ_USER_BY_PHONE = "%s://%s:%d/cmd/%s/%s?phone_number=%s&country_code=%s";
    protected static final String CLOUD_JSON_key = "key";
    protected static final String CLOUD_JSON_owner = "owner";
    protected CognitoSyncManager syncClient;

    public AmazonCloud(Context context) {
        super();
        syncClient = AWS.getSyncClient(context);
    }

    @Override
    public void createUser(String phone, String uuid, final Cloud.I_ProcessListener listener) {
        Dataset users = syncClient.openOrCreateDataset(Table.users);
        users.put(Field.users.get(Field.UsersField.PHONE), phone);
        users.put(Field.users.get(Field.UsersField.UUID), uuid);
        users.synchronize(new Dataset.SyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                Log.v(TAG, String.format(Locale.getDefault(), "onSuccess:\t%s\n%s", dataset.toString(), updatedRecords.toString()));
                HashMap<String, String> result = new HashMap<>();
                result.put("uuid", dataset.get("uuid"));
                result.put("dataset", dataset.getAll().toString());
                result.put("updatedRecords", updatedRecords.toString());
                listener.onSuccess(result);
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                Log.v(TAG, "onConflict");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                Log.v(TAG, "onDatasetDeleted");
                return false;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                Log.v(TAG, "onDatasetsMerged");
                return false;
            }

            @Override
            public void onFailure(DataStorageException dse) {
                Log.v(TAG, String.format(Locale.getDefault(), "onFailure:\t%s", dse.getMessage()));
                HashMap<String, String> result = new HashMap<>();
                result.put("DataStorageException", dse.getMessage());
                listener.onFailure(result);
            }
        });
    }


    OkHttpClient httpClient = new OkHttpClient();

    String reqHttp(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        String result = null;
        try {
            response = httpClient.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONObject getUserJson(String phone, String countryCode) {
        @SuppressLint("DefaultLocale") String url = String.format( GET_REQ_USER_BY_PHONE,
                CLOUD_SCHEME,
                CLOUD_HOST,
                CLOUD_PORT,
                CLOUD_MODULE_user,
                CLOUD_FUNCTION_findByPhoneNumber,
                phone,
                countryCode
        );
        String responseBody = reqHttp(url);
        JSONArray jsonArr = null;
        JSONObject json = null;
        try {
            jsonArr = new JSONArray(responseBody);
            if (jsonArr.length() == 0) return null;
            json = jsonArr.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String getUserUuid(String phone, String countryCode) {
        JSONObject userJson = getUserJson(phone, countryCode);
        if (userJson == null || !userJson.has(CLOUD_JSON_owner)) return null;
        String userUuid = null;
        try {
            userUuid = userJson.getString(CLOUD_JSON_owner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userUuid;
    }

    @Override
    public JSONArray findSignaturesByOwner(String uuid) {
        @SuppressLint("DefaultLocale") String url = String.format( GET_REQ_SIGNATURE_BY_OWNER,
                CLOUD_SCHEME,
                CLOUD_HOST,
                CLOUD_PORT,
                CLOUD_MODULE_signature,
                CLOUD_FUNCTION_findByUser,
                uuid
        );
        String responseBody = reqHttp(url);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(responseBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public List<String> findSignaturesUuidsByOwnerUuid(String uuid) {
        if (null == uuid) return null;
        List<String> list = new ArrayList<>();
        JSONArray signaturesJsonArr = findSignaturesByOwner(uuid);
        try {
            for (int i = 0; i < signaturesJsonArr.length(); i++) {
                JSONObject signatureJson = signaturesJsonArr.getJSONObject(i);
                if (signatureJson == null || !signatureJson.has(CLOUD_JSON_key)){
                    Log.e(TAG, "findSignaturesUuidsByOwnerUuid - i="+i+": "+((signatureJson==null)?"signatureJson==null":"signatureJson.has(CLOUD_JSON_key)==false"));
                    return null;
                }
                list.add(signatureJson.getString(CLOUD_JSON_key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    @Override
    public List<String> findSignaturesUuidsByOwnerPhone(String phone, String countryCode) {
        String userUuid = getUserUuid(phone, countryCode);
        List<String> signaturesUuids = findSignaturesUuidsByOwnerUuid(userUuid);
        if (signaturesUuids == null){
            return new ArrayList<>();
        }
        return signaturesUuids;
    }
}
