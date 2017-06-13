package com.lesswalk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.bases.ILesswalkService;

public class RegistrationActivity extends BaseActivity
{
    private TextView edit_title = null;

    private class RegistrationField
    {
        int    id    = -1;
        String title = null;
        String value = null;
        View   view  = null;

        public RegistrationField(String title, int id)
        {
            this.title = title;
            this.id = id;
        }
    }

    private RegistrationField fields[] =
    {
        new RegistrationField("To Get Started, Please Enter your Name", R.id.registration_name_et),
        new RegistrationField("To Get Started, Please Enter your Phone Number", R.id.registration_number_et),
    };

    private static final int REGISTRATION_FIELD_NAME   = 0;
    private static final int REGISTRATION_FIELD_NUMBER = 1;

    private int currentField = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    private void setViews()
    {
//        final EditText    numberET  = (EditText) findViewById(R.id.registration_number_et);
        final ProgressBar waitWheel  = (ProgressBar) findViewById(R.id.registration_wait_wheel);
        final Button      doneBT     = (Button) findViewById(R.id.registration_done_bt);

        edit_title = (TextView) findViewById(R.id.registration_request_title);

        for (int i = 0; i < fields.length; i++)
        {
            fields[i].view = findViewById(fields[i].id);
        }

        setRequest(currentField);

        doneBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText et = (EditText) fields[currentField].view;
                if (et.getText() == null || et.getText().length() <= 0) return;

                fields[currentField].value = et.getText().toString();

                if (currentField >= fields.length - 1)
                {
                    doneBT.setEnabled(false);
                    waitWheel.setVisibility(View.VISIBLE);
                    getService().setLocalNumber(fields[REGISTRATION_FIELD_NAME].value, fields[REGISTRATION_FIELD_NUMBER].value, new ILesswalkService.ISetLocalNumberCallback()
                    {
                        @Override
                        public void onSuccess()
                        {
                            Log.d("elazarkin8", "onSuccess!");
                            waitWheel.setVisibility(View.INVISIBLE);
                            doneBT.setEnabled(true);
                            finish();
                        }

                        @Override
                        public void onError(int errorID)
                        {
                            switch (errorID)
                            {
                                case ILesswalkService.REGISTRATION_ERROR_STILL_NOT_REGISTRED:
                                {
                                    Toast.makeText(RegistrationActivity.this, "In this alpha version still need be activated user from IPHONE", Toast.LENGTH_LONG);
                                    Log.d("elazarkin8", "onError: " + errorID);
                                    waitWheel.setVisibility(View.INVISIBLE);
                                    doneBT.setEnabled(true);
                                    break;
                                }
                                case ILesswalkService.REGISTRATION_ERROR_FILE_SYSTEM:
                                {
                                    Toast.makeText(RegistrationActivity.this, "Some problem with file system, please check your memory!", Toast.LENGTH_LONG);
                                    Log.d("elazarkin8", "onError: " + errorID);
                                    waitWheel.setVisibility(View.INVISIBLE);
                                    doneBT.setEnabled(true);
                                    break;
                                }
                                default: finish();
                            }
                        }

                        @Override
                        public void onProgress(String path)
                        {
                            Toast.makeText(RegistrationActivity.this, path + " finish", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void notSuccessFinish()
                        {

                        }
                    });
                }
                else
                {
                    currentField++;
                    setRequest(currentField);
                }
            }
        });
    }

    private void setRequest(final int currentField)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                edit_title.setText(fields[currentField].title);
                for(int i = 0; i < fields.length; i++)
                {
                    if(i != currentField)
                    {
                        fields[i].view.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        fields[i].view.setVisibility(View.VISIBLE);
                        fields[i].view.bringToFront();
                    }
                }
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
