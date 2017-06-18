package com.lesswalk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lesswalk.bases.BaseActivity;

import java.util.Vector;

public class RegistrationActivity extends BaseActivity
{
    private static final String PHONE_NAME_TITLE = "To Get Started, Please Enter your Name";

    private enum NextAction
    {
        GET_NUMBER,
        GET_NAMES,
        CREATE_USER,
        SEND_SMS_TO_CHECK_EXISTED_USER,
        SEND_SMS_TO_CHECK_NEW_USER,
        SHOW_NAMES_FROM_ZIP,
        DOWNLOAD_SIGNATURES,
        FINISH
    }

    private interface RegistrationFieldCallback
    {
        void onFinish(NextAction action);
    }

    private abstract class RegistrationField
    {
        RegistrationFieldCallback callback = null;

        void setCallback(RegistrationFieldCallback callback)
        {
            this.callback = callback;
        }

        abstract void init();
        abstract void setVisibility(int status);
        abstract void bringToFront();
        abstract String getTitle();
        abstract void onDonePressed();
    }

    private TextView                  edit_title         = null;
    private ProgressBar               waitWheel          = null;
    private Button                    doneBT             = null;
    private View                      name_lastname_view = null;
    private EditText                  name_et            = null;
    private EditText                  lastname_et        = null;
    private Vector<RegistrationField> fields             = null;
    private NumberField               numberField        = null;
    private RegistrationField         currentField       = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    private void setViews()
    {
        waitWheel = (ProgressBar) findViewById(R.id.registration_wait_wheel);
        doneBT    = (Button) findViewById(R.id.registration_done_bt);
        edit_title = (TextView) findViewById(R.id.registration_request_title);

        fields = new Vector<>();

        fields.add((numberField=new NumberField()));

        for (RegistrationField field:fields)
        {
            field.init();
            field.setCallback(onFinishCallback);
        }

        setRequest(numberField);

        doneBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                doneBT.setEnabled(false);
                waitWheel.setVisibility(View.VISIBLE);
                currentField.onDonePressed();
            }
        });
    }

    private RegistrationFieldCallback onFinishCallback = new RegistrationFieldCallback()
    {
        @Override
        public void onFinish(NextAction action)
        {
            switch (action)
            {
                case SEND_SMS_TO_CHECK_EXISTED_USER:
                {
                    break;
                }

                case SEND_SMS_TO_CHECK_NEW_USER:
                {
                    break;
                }

                case CREATE_USER:
                {
                    break;
                }

                case SHOW_NAMES_FROM_ZIP:
                {
                    break;
                }

                case DOWNLOAD_SIGNATURES:
                {
                    break;
                }

                case GET_NUMBER:
                {
                    break;
                }

                case GET_NAMES:
                {
                    break;
                }

                case FINISH:
                {
                    break;
                }
            }

            doneBT.setEnabled(true);
            waitWheel.setVisibility(View.GONE);
        }
    };

    private void setRequest(final RegistrationField field)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                edit_title.setText(field.getTitle());

                for (int i = 0; i < fields.size(); i++)
                {
                    fields.elementAt(i).setVisibility(!field.equals(fields.elementAt(i)) ? View.INVISIBLE:View.VISIBLE);
                }
                field.bringToFront();

                doneBT.setText((!field.equals(fields.lastElement()) ? "Next":"Done"));

                currentField = field;
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

    private class NumberField extends RegistrationField
    {
        private EditText number_et = null;

        @Override
        public void init()
        {
            number_et = (EditText) findViewById(R.id.registration_number_et);
        }

        @Override
        public void setVisibility(final int status)
        {
            RegistrationActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    number_et.setVisibility(status);
                }
            });
        }

        @Override
        void bringToFront()
        {
            number_et.bringToFront();
        }

        @Override
        public String getTitle()
        {
            return "To Get Started, Please Enter your Phone Number";
        }

        @Override
        public void onDonePressed()
        {

        }
    }

//    if (currentField >= fields.length - 1)
//    {

//        getService().setLocalNumber(fields[REGISTRATION_FIELD_NAME].value, fields[REGISTRATION_FIELD_NUMBER].value, new ILesswalkService.ISetLocalNumberCallback()
//        {
//            @Override
//            public void onSuccess()
//            {
//                Log.d("elazarkin8", "onSuccess!");
//                waitWheel.setVisibility(View.INVISIBLE);
//                doneBT.setEnabled(true);
//                finish();
//            }
//
//            @Override
//            public void onError(int errorID)
//            {
//                switch (errorID)
//                {
//                    case ILesswalkService.REGISTRATION_ERROR_STILL_NOT_REGISTRED:
//                    {
//                        Toast.makeText(RegistrationActivity.this, "In this alpha version still need be activated user from IPHONE", Toast.LENGTH_LONG);
//                        Log.d("elazarkin8", "onError: " + errorID);
//                        waitWheel.setVisibility(View.INVISIBLE);
//                        doneBT.setEnabled(true);
//                        break;
//                    }
//                    case ILesswalkService.REGISTRATION_ERROR_FILE_SYSTEM:
//                    {
//                        Toast.makeText(RegistrationActivity.this, "Some problem with file system, please check your memory!", Toast.LENGTH_LONG);
//                        Log.d("elazarkin8", "onError: " + errorID);
//                        waitWheel.setVisibility(View.INVISIBLE);
//                        doneBT.setEnabled(true);
//                        break;
//                    }
//                    default: finish();
//                }
//            }
//
//            @Override
//            public void onProgress(String path)
//            {
//                Toast.makeText(RegistrationActivity.this, path + " finish", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void notSuccessFinish()
//            {
//
//            }
//        });
//    }
//                else
//    {
//        currentField++;
//        setRequest(currentField);
//    }
}
