package com.lesswalk.database;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by elad on 29/11/16.
 */

public class AmazonCloud extends Cloud {

    protected static final String TAG = "AmazonCloud";
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

    @Override
    public void getUser(String phone, final Cloud.I_ProcessListener listener) {
    }
}
