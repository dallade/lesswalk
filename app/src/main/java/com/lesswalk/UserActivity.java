package com.lesswalk;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

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
                String someonesUuid = "E68F1C41-656A-4DA8-A001-10093F93E000";
                String req = "http://52.70.155.39:30082/cmd/signature/findByOwner/" + someonesUuid;
                asyncHttpReqUrl(req);
            }break;
        }
    }






















}
