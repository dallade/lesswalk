package com.lesswalk.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.RestUtils;
import com.amazonaws.util.Md5Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by elad on 31/10/16.
 */

public class AWS {
    private static final String TAG = "AWS";
    //
    private static final String IDENTITY_POOL_ID = "us-east-1:528e5e4d-4722-45da-ab53-13c39938b355";
    private static final Regions REGION = Regions.US_EAST_1;
    private static final String BUCKET = "com.lesswalk.one-amir";
    //
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat DATE_FORMAT_yyyyMMdd_HHmmss = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static AWS instance;

    private CognitoCachingCredentialsProvider credentialsProvider;
    private CognitoSyncManager syncClient;
    private final AmazonS3Client s3;
    private final TransferUtility transferUtility;

    private AWS(Context context){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context, // Context
                IDENTITY_POOL_ID, // Identity Pool ID
                REGION // Region
        );
        syncClient = new CognitoSyncManager(
                context, // Context
                REGION, // Region
                credentialsProvider
        );
        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, context);
    }

    private static AWS getInstance(Context c){
        if (instance == null){
            instance = new AWS(c);
        }
        return instance;
    }

    public static CognitoCachingCredentialsProvider getCredentials(Context c){
        return getInstance(c).credentialsProvider;
    }

    public static CognitoSyncManager getSyncClient(Context c){
        return getInstance(c).syncClient;
    }

    public static AmazonS3Client getS3(Context c){
        return getInstance(c).s3;
    }

    public static TransferUtility getTransferUtility(Context c){
        return getInstance(c).transferUtility;
    }

    public static void upload(Context context, final String path_to_file, final OnUploadListener onUploadListener) {
        File file = new File(path_to_file);
        String key = generateKey();
        TransferObserver observer = getInstance(context).transferUtility.upload(BUCKET, key, file);
        observer.setTransferListener(new TransferListener() {
            boolean hasStarted = false;

            @Override
            public void onStateChanged(int id, TransferState state) {
                switch (state){

                    case WAITING:
                        break;
                    case IN_PROGRESS:
                        if (!hasStarted){
                            hasStarted = true;
                            onUploadListener.onUploadStarted(path_to_file);
                        }
                        break;
                    case PAUSED:
                        break;
                    case RESUMED_WAITING:
                        break;
                    case COMPLETED:
                        onUploadListener.onUploadFinished(path_to_file);
                        break;
                    case CANCELED:
                        break;
                    case FAILED:
                        break;
                    case WAITING_FOR_NETWORK:
                        break;
                    case PART_COMPLETED:
                        break;
                    case PENDING_CANCEL:
                        break;
                    case PENDING_PAUSE:
                        break;
                    case PENDING_NETWORK_DISCONNECT:
                        break;
                    case UNKNOWN:
                    default:
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentage = 0;
                try{
                    percentage = (bytesCurrent/bytesTotal)*100;
                }catch (ArithmeticException e){
                    Log.d(TAG, "onProgressChanged: bytesTotal="+bytesTotal);
                }
                onUploadListener.onUploadProgress(path_to_file, percentage);
            }

            @Override
            public void onError(int id, Exception ex) {
                onUploadListener.onUploadError(path_to_file, id, ex);
            }
        });
    }

    private static String generateKey() {
        String str = UUID.randomUUID().toString();
        //str += DATE_FORMAT_yyyyMMdd_HHmmss.format(new Date());
        return str;
    }

    public interface OnUploadListener {
        void onUploadStarted(String path);
        void onUploadProgress(String path, float percentage);
        void onUploadFinished(String path);
        void onUploadError(String path, int errorId, Exception ex);
    }

//    void other(){
//        Dataset dataset = syncClient.openOrCreateDataset("myDataset");
//        dataset.put("myKey", "myValue");
//        dataset.synchronize(new DefaultSyncCallback() {
//            @Override
//            public void onSuccess(Dataset dataset, List newRecords) {
//                //Your handler code here
//            }
//        });
//    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
