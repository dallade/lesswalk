package com.lesswalk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lesswalk.database.AWS;

import java.util.Locale;

public class LoginActivity extends Activity {

    private EditText et_country_code;
    private EditText et_phone;
    private Button btn_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
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
        AWS.getSyncClient(this).
        //
        Toast.makeText(this, String.format(Locale.getDefault()
                , "Login success!\n+%d\n%d"
                , Integer.parseInt(
                        et_country_code.getText().toString()
                )
                , Long.parseLong(
                        et_phone.getText().toString()
                )
            ), Toast.LENGTH_SHORT
        ).show();
    }


}
