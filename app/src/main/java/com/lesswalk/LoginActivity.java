package com.lesswalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class LoginActivity extends BaseActivity
{

    private static final String TAG = "LoginActivity";
    private EditText et_country_code;
    private EditText et_phone;
    private Button   btn_signup;
    private Button   btn_login;
    private Cloud    cloud;
    private HashMap<String, String> loginResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        cloud = new AmazonCloud(this);
        //cloud.getUserJson();
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        //showLoggedInArea("bla");
        checkIsLoggedIn();
    }

    private void checkIsLoggedIn()
    {
        String[] mockLocalUserData = new String[]{"972", "0526807577", "abcdefg"};//as if it was stored in local phone db
        AsyncTask<String, String, String> checkIsLoggedInAsync = new AsyncTask<String, String, String>() {

            WeakReference<Activity> mParentActivityWeakRef = new WeakReference<Activity>(LoginActivity.this);

            @Override
            protected String doInBackground(String... params) {
                String countryCode = params[0];
                String phone = params[1];
                String uuid = params[2];
                //
                String userUuid = cloud.getUserUuid(phone, countryCode);
                if (null == userUuid || userUuid.equals("") || !userUuid.equals(uuid)) return null;
                return "logged-in";
            }

            @Override
            protected void onPostExecute(String s) {
                if (null==s)s="logged-out";
                if (isCancelled() || null == mParentActivityWeakRef.get()){
                    Log.d(TAG, "onPostExecute cancelled or died ("+s+")");
                    return;
                }
                Log.d(TAG, ""+s);
            }
        };
        checkIsLoggedInAsync.execute(mockLocalUserData[0], mockLocalUserData[1], mockLocalUserData[2]);
    }

    private void loginClicked()
    {
        boolean isValid = verifyFields();
        if (!isValid) return;
        hideKeyboard();
        //
        int    countryCodeInt = Integer.parseInt(et_country_code.getText().toString());
        long   phoneLong      = Long.parseLong(et_phone.getText().toString());
        int generatedNum = new Random(new Date().getTime()).nextInt(10000);//0-9999
        @SuppressLint("DefaultLocale")
        String generatedSmsCode = String.format("%04d", generatedNum);//0000-9999
        //
        AsyncTask<String, String, String> checkVerifyPhoneAsync = new AsyncTask<String, String, String>() {

            WeakReference<Activity> mParentActivityWeakRef = new WeakReference<Activity>(LoginActivity.this);

            @Override
            protected String doInBackground(String... params) {
                String countryCode = params[0];
                String phone = params[1];
                String verificationCode = params[2];
                //
                String userUuid = cloud.getUserUuid(phone, countryCode);
                if (null == userUuid || userUuid.equals("")) return null;
                String response = cloud.sendVerificationSms(countryCode, phone, verificationCode);
                //TODO read SMS and compare
                return "success";
            }

            @Override
            protected void onPostExecute(String s) {
                if (null==s)s="failed";
                if (isCancelled() || null == mParentActivityWeakRef.get()){
                    Log.d(TAG, "onPostExecute cancelled or died ("+s+")");
                    return;
                }
                Log.d(TAG, ""+s);
            }
        };
        checkVerifyPhoneAsync.execute(""+countryCodeInt, ""+phoneLong, generatedSmsCode);
    }

    public EditText getEditText()
    {
        return et_country_code;
    }

    protected void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getEditText().getWindowToken(), 0);
    }

    private void initViews()
    {
        et_country_code = (EditText) findViewById(R.id.et_country_code);
        et_phone = (EditText) findViewById(R.id.et_phone);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signUpClicked();
            }
        });
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loginClicked();
            }
        });
    }

    private boolean verifyFields()
    {
        boolean anyInvalid = false;
        boolean isValid;
        //
        isValid = false;
        String text_et_country_code = et_country_code.getText().toString();
        int    f_country_code       = -1;
        try
        {
            f_country_code = Integer.parseInt(text_et_country_code);
            if (f_country_code < 1) throw new Exception("et_country_code_non_positive");
            if (f_country_code > 9999) throw new Exception("et_country_code_above_4_digits");
            isValid = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            et_country_code.requestFocus();
        }
        anyInvalid |= !isValid;
        //
        isValid = false;
        String text_et_phone = et_phone.getText().toString();
        long   f_phone       = -1;
        try
        {
            f_phone = Long.parseLong(text_et_phone);
            if (f_phone < 1) throw new Exception("et_phone_too_short");
            if (text_et_phone.length() > 15) throw new Exception("et_phone_above_15_digits");
            isValid = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            et_phone.requestFocus();
        }
        anyInvalid |= !isValid;
        //
        return !anyInvalid;
    }

    private void signUpClicked()
    {
        boolean isValid = verifyFields();
        if (!isValid) return;
        hideKeyboard();
        //
        String uuid           = UUID.randomUUID().toString();
        int    countryCodeInt = Integer.parseInt(et_country_code.getText().toString());
        long   phoneLong      = Long.parseLong(et_phone.getText().toString());
        String phone          = String.format(Locale.getDefault(), "+%d %d", countryCodeInt, phoneLong);
        cloud.createUser(phone, uuid, new Cloud.I_ProcessListener()
        {
            @Override
            public void onSuccess(HashMap<String, String> result)
            {
                String output = String.format(Locale.getDefault()
                        , "Login success!\n%s\n%s"
                        , result.get("dataset")
                        , result.get("updatedRecords")
                );

                Toast.makeText(LoginActivity.this, output, Toast.LENGTH_SHORT).show();
                Log.v(TAG, output);
            }

            @Override
            public void onFailure(HashMap<String, String> result)
            {
                String output = String.format(Locale.getDefault()
                        , "Login failed!\n%s"
                        , result.get("DataStorageException")
                );
                Toast.makeText(LoginActivity.this, output, Toast.LENGTH_SHORT).show();
                Log.v(TAG, output);
            }
        });
    }

    private void showLoggedInArea(String uuid)
    {
//        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
//        intent.putExtra("uuid", ""+uuid);
//        startActivity(intent);
    }


    @Override
    protected void mainServiceConnected() {

    }

}
