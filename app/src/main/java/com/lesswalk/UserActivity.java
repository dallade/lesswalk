package com.lesswalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lesswalk.database.AWS;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by elad on 05/12/16.
 */
public class UserActivity extends Activity {

    private TextView tv_user_act_result;
    private String userUuid = "";
    private UserActivity curActivity;
    private Cloud cloud = new AmazonCloud(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        curActivity = this;
        initViews();
        try {
            userUuid = getIntent().getExtras().getString("uuid", "NADA");
        }catch (NullPointerException ex){
            ex.printStackTrace();
            userUuid = "";
        }

    }

    private void initViews() {
        tv_user_act_result = (TextView)findViewById(R.id.tv_user_act_result);
    }


    OkHttpClient client = new OkHttpClient();

    String httpReqUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        String result = null;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    void asyncHttpReqUrl(final String url){

        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }
            @Override
            protected String doInBackground(String... params) {
                //String url = params[0];
                return httpReqUrl(url);
            }
            @Override
            protected void onPostExecute(String s) {
                if (curActivity == null) return;
                if (tv_user_act_result == null) return;
                tv_user_act_result.setText(s);

            }
        }.execute(url);
    }

    void asyncFindSignatures(final String phone, final String countryCode){

        new AsyncTask<String, String, String>() {

            @SuppressLint("DefaultLocale")
            final AWS.OnDownloadListener onDownloadListener = new AWS.OnDownloadListener() {
                @Override
                public void onDownloadStarted(String path) {
                    publishProgress(String.format("state %s: '%s'", "Started", path));
                }

                @Override
                public void onDownloadProgress(String path, float percentage) {
                    publishProgress(String.format("state %s: (%3.1f) '%s'", "Progress", percentage, path));
                }

                @Override
                public void onDownloadFinished(String path) {
                    publishProgress(String.format("state %s: '%s'", "Finished", path));
                }

                @Override
                public void onDownloadError(String path, int errorId, Exception ex) {
                    publishProgress(String.format("state %s: {'%s', %d} for file '%s'", "Error", ex.getMessage(), errorId, path));
                    ex.printStackTrace();
                }
            };

            @Override
            protected void onProgressUpdate(String... values) {
                tv_user_act_result.setText(values[0]);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }
            @Override
            protected String doInBackground(String... params) {
                List<String> signatures = cloud.findSignaturesUuidsByOwnerPhone(phone, countryCode);
                String allTogether = "";
                String sigUuid = "";
                for (int i = 0; i < signatures.size(); i++) {
                    sigUuid = signatures.get(i);
                    allTogether += sigUuid + "\n";
                    cloud.downloadAndUnzipSignature(sigUuid);
                }
//                if (signatures.size() > 0) {
//                    cloud.downloadAndUnzipSignature(sigUuid, onDownloadListener);
//                }
                return allTogether;
            }
            @Override
            protected void onPostExecute(String s) {
                if (curActivity == null) return;
                if (tv_user_act_result == null) return;
                tv_user_act_result.setText(s);

            }
        }.execute();
    }
//    String phone = "0526807577";//sharon's
//    String countryCode = "972";
//    asyncFindSignatures(phone, countryCode);

    public void onClickUserActivityButtons(View view) {
        Button btn = (Button) view;
        String btnTxt = btn.getText().toString();
        tv_user_act_result.setText("");
        switch (btnTxt){
            case "My Signatures":{
//                String responseStr = httpReqUrl("http://52.70.155.39:30082/cmd/signature/findSignaturesByOwner?owner=" + userUuid);
//                tv_user_act_result.setText(responseStr);
                String req = "http://52.70.155.39:30082/cmd/signature/findByOwner/" + userUuid;
                asyncHttpReqUrl(req);
            }break;
            case "Friends Signatures":{
                String someonesUuid = "801BFAF1-6480-481C-92C6-619C381E0222";
                String req = "http://52.70.155.39:30082/cmd/signature/findByOwner/" + someonesUuid;
                asyncHttpReqUrl(req);
            }break;
            case "Another Option":{
                //String someonesUuid = "E68F1C41-656A-4DA8-A001-10093F93E000";
                String phone = "0526807577";//sharon's
                String countryCode = "972";
                asyncFindSignatures(phone, countryCode);
            }break;
        }
    }






















}
