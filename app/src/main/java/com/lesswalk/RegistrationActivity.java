package com.lesswalk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ILesswalkService;

public class RegistrationActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    private void setViews()
    {
        final EditText    firstNameET  = (EditText) findViewById(R.id.registration_first_name_et);
        final EditText    lastNameET  = (EditText) findViewById(R.id.registration_last_name_et);
        final EditText    numberET  = (EditText) findViewById(R.id.registration_number_et);
        final ProgressBar waitWheel = (ProgressBar) findViewById(R.id.registration_wait_wheel);
        final Button      doneBT    = (Button) findViewById(R.id.registration_done_bt);

        doneBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                doneBT.setEnabled(false);
                waitWheel.setVisibility(View.VISIBLE);
                getService().setLocalNumberAndName(
                        numberET.getText().toString()
                        , firstNameET.getText().toString()
                        , lastNameET.getText().toString()
                        , new ILesswalkService.ISetLocalNumberCallback()
                {
                    @Override
                    public void onSuccess()
                    {
                        Log.d("elazarkin8", "onSuccess!");
                        RegistrationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                waitWheel.setVisibility(View.INVISIBLE);
                                doneBT.setEnabled(true);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError(final int errorID)
                    {
                        switch (errorID)
                        {
                            case ILesswalkService.REGISTRATION_ERROR_STILL_NOT_REGISTRED:
                            {
                                RegistrationActivity.this.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(RegistrationActivity.this, "In this alpha version still need be activated user from IPHONE", Toast.LENGTH_LONG).show();
                                        Log.d("elazarkin8", "onError: " + errorID);
                                        waitWheel.setVisibility(View.INVISIBLE);
                                        doneBT.setEnabled(true);
                                    }
                                });
                            }break;
                            case ILesswalkService.REGISTRATION_ERROR_SMS_STATE_NOT_READY:
                            {
                                RegistrationActivity.this.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(RegistrationActivity.this, "Please wait while we're verifying your phone...", Toast.LENGTH_LONG).show();
                                        Log.d("elazarkin8", "onError: " + errorID);
                                        waitWheel.setVisibility(View.VISIBLE);
                                        firstNameET.setEnabled(false);
                                        lastNameET.setEnabled(false);
                                        numberET.setEnabled(false);
                                        doneBT.setEnabled(false);
                                    }
                                });
                            }break;
                            case ILesswalkService.REGISTRATION_ERROR_FILE_SYSTEM:
                            {
                                RegistrationActivity.this.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(RegistrationActivity.this, "Some problem with file system, please check your memory!", Toast.LENGTH_LONG).show();
                                        Log.d("elazarkin8", "onError: " + errorID);
                                        waitWheel.setVisibility(View.INVISIBLE);
                                        doneBT.setEnabled(true);
                                    }
                                });
                            }
                            default:finish();
                        }
                    }

                    @Override
                    public void onProgress(String path) {
                        Log.d("elazarkin8", path + "finish");
                    }

                    @Override
                    public void notSuccessFinish() {

                    }
                });
            }
        });
    }

    @Override
    protected void mainServiceConnected()
    {
        setViews();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}
