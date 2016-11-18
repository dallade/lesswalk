package com.lesswalk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.lesswalk.database.AWS;
import com.lesswalk.database.DBController;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MoreViewsActivity extends Activity {

    private static final String TAG = "MoreViewsActivity";

    private Button btnFirst;
    private Button btnSecond;
    private Button btnThird;
    protected Context context;
    protected Context appContext;
    private View hover_add_family;
    private View hover_watch_out_from;
    private View hover_http_client;
    private ArrayList<View> hovers;
    private EditText http_request_edit_text;
    //private WebView webview;
    private TextView response_text_view;
    private Button http_request_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        appContext = getApplicationContext();
        setContentView(R.layout.activity_more_views);
        initButtons();
    }

    private void initButtons() {
        hovers = new ArrayList<>();
        Collections.addAll(hovers
                , hover_add_family = findViewById(R.id.hover_add_family)
                , hover_watch_out_from = findViewById(R.id.hover_watch_out_from)
                , hover_http_client = findViewById(R.id.hover_http_client)
        );
        //
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id)
                {
                    case R.id.btnHoverFamily:{
                        showHover(hover_add_family.getId());
                    }break;

                    case R.id.btnHoverWatch:{
                        showHover(hover_watch_out_from.getId());
                    }break;

                    case R.id.btnHoverHttpClient:{
                        showHover(hover_http_client.getId());
                        sendHttpRequest();
                    }break;

                    case 0:
                        // custom dialog
                        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        dialog.setContentView(R.layout.layout_watch_out_from);
                        dialog.setTitle("Watch Out");

                        Button dialogButton = (Button) dialog.findViewById(R.id.doneBtn);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    default:{
                        hideHover();
                    }
                }
            }
        };
        //
        btnFirst = (Button) findViewById(R.id.btnHoverFamily);
        btnFirst.setOnClickListener(onClickListener);
        //
        btnSecond = (Button) findViewById(R.id.btnHoverWatch);
        btnSecond.setOnClickListener(onClickListener);
        //
        btnThird = (Button) findViewById(R.id.btnHoverHttpClient);
        btnThird.setOnClickListener(onClickListener);
        //
        //
        response_text_view = (TextView) findViewById(R.id.response_text_view);
        http_request_edit_text = (EditText) findViewById(R.id.http_request_edit_text);
        http_request_btn = (Button) findViewById(R.id.http_request_btn);
        http_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHttpRequest();
            }
        });
    }

    private void sendHttpRequest() {
        sendHttpRequest(http_request_edit_text.getText().toString());
    }

    private void sendHttpRequest(String request) {
        if (!isConnected())
        {
            Toast.makeText(MoreViewsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        new DownloadWebPageTask(http_request_edit_text, response_text_view, http_request_btn).execute(request);
    }

    private boolean isConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return  (networkInfo != null && networkInfo.isConnected());
    }

    private void showHover(int id) {
        for (View hover : hovers) {
            hover.setVisibility(hover.getId()==id ? View.VISIBLE : View.GONE);
        }
    }

    private boolean hideHover() {
        boolean wasAnyHovering = false;
        for (View hover : hovers) {
            wasAnyHovering |= hover.getVisibility()==View.VISIBLE;
            hover.setVisibility(View.GONE);
        }
        return wasAnyHovering;
    }

    @Override
    public void onBackPressed() {
        if (!hideHover())
        {
            super.onBackPressed();
        }
    }


    public void onHoverBackgroundClick(View view) {
        hideHover();
    }

    public void OnNonClickableClick(View view) {
        // do nothing
    }

    private class DownloadWebPageTask extends AsyncTask<String, String, String> {

        private final EditText editText;
        //private final WebView webView;
        private final TextView textView;
        private final Button goButton;
        DBController db;

        public DownloadWebPageTask(EditText editText, TextView textView, Button goButton) {
            this.editText = editText;
            //this.webView = webView;
            this.textView = textView;
            this.goButton = goButton;
            //
            db = new DBController(context);
            HashMap<String, String> queryValues = new HashMap<String, String>();
            queryValues.put("userName", "elad");
            if (queryValues.get("userName") != null) {
                db.insertUser(queryValues);
            }
        }

        @Override
        protected String doInBackground(String... args) {
            String request = args[0];
            String response;

            // params comes from the execute() call: params[0] is the url.
            try {
                response = downloadUrl(request);
            } catch (IOException e) {
                response = "Unable to retrieve web page. URL may be invalid.";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //this.webView.loadData(s, "text/html; charset=utf-8", "UTF-8");
            //this.webView.loadUrl(editText.getText().toString());
            if (s==null){
                Toast.makeText(context, "response is null", Toast.LENGTH_SHORT).show();
                return;
            }
            //this.textView.setText(s);
            ArrayList<HashMap<String, String>> allUsers = db.getAllUsers();
            this.textView.setText(allUsers.toString());
            //
            //TransferUtility awsFtp = AWS.getTransferUtility(getApplicationContext());
            //awsFtp.upload()
            AWS.upload(appContext, "path_to_file");

        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String urlString) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readAll(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }



        }

        private String readAll(InputStream is, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }
}
