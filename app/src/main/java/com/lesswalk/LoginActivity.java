package com.lesswalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lesswalk.database.AmazonCloud;
import com.lesswalk.database.Cloud;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private EditText et_country_code;
    private EditText et_phone;
    private Button btn_signup;
    private Cloud cloud;
    private HashMap<String, String> loginResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        cloud = new AmazonCloud(this);
        //cloud.getUserJson();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        showLoggedInArea("bla");
    }

    public EditText getEditText() {
        return et_country_code;
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getEditText().getWindowToken(), 0);
    }

    private void initViews() {
        et_country_code = (EditText) findViewById(R.id.et_country_code);
        et_phone = (EditText) findViewById(R.id.et_phone);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpClicked();
            }
        });
    }

    private boolean verifyFields() {
        boolean anyInvalid = false;
        boolean isValid;
        //
        isValid = false;
        String text_et_country_code = et_country_code.getText().toString();
        int f_country_code = -1;
        try {
            f_country_code = Integer.parseInt(text_et_country_code);
            if (f_country_code<1) throw new Exception("et_country_code_non_positive");
            if (f_country_code>9999) throw new Exception("et_country_code_above_4_digits");
            isValid = true;
        } catch (Exception e) {
            e.printStackTrace();
            et_country_code.requestFocus();
        }
        anyInvalid |= !isValid;
        //
        isValid = false;
        String text_et_phone = et_phone.getText().toString();
        long f_phone = -1;
        try {
            f_phone = Long.parseLong(text_et_phone);
            if (f_phone<1) throw new Exception("et_phone_too_short");
            if (text_et_phone.length()>15) throw new Exception("et_phone_above_15_digits");
            isValid = true;
        } catch (Exception e) {
            e.printStackTrace();
            et_phone.requestFocus();
        }
        anyInvalid |= !isValid;
        //
        return !anyInvalid;
    }

    private void signUpClicked() {
        boolean isValid = verifyFields();
        if (!isValid) return;
        hideKeyboard();
        //
        String uuid = UUID.randomUUID().toString();
        int countryCodeInt = Integer.parseInt(et_country_code.getText().toString());
        long phoneLong = Long.parseLong(et_phone.getText().toString());
        @SuppressLint("DefaultLocale") String phone = String.format("+%d %d", countryCodeInt, phoneLong);
        cloud.createUser(phone, uuid, new Cloud.I_ProcessListener() {
            @Override
            public void onSuccess(HashMap<String, String> result) {
                String output = String.format(Locale.getDefault()
                        , "Login success!\n%s\n%s"
                        , result.get("dataset")
                        , result.get("updatedRecords")
                );

                Toast.makeText(LoginActivity.this, output, Toast.LENGTH_SHORT).show();
                Log.v(TAG, output);
            }

            @Override
            public void onFailure(HashMap<String, String> result) {
                String output = String.format(Locale.getDefault()
                        , "Login failed!\n%s"
                        , result.get("DataStorageException")
                );
                Toast.makeText(LoginActivity.this, output, Toast.LENGTH_SHORT).show();
                Log.v(TAG, output);
            }
        });
    }

    private void showLoggedInArea(String uuid) {
        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
        intent.putExtra("uuid", ""+uuid);
        startActivity(intent);
    }











}
