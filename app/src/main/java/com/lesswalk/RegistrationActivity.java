package com.lesswalk;

import android.os.Bundle;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

public class RegistrationActivity extends BaseActivity
{
    private static final int MIN_NUMBER_LENGTH = 9;

    private enum NextAction
    {
        GET_NUMBER,
        GET_NAME,
        CREATE_USER,
        SEND_SMS_VERIFICATION,
        SHOW_NAME_FROM_JSON,
        DOWNLOAD_SIGNATURES,
        SHOW_NUMBER_ERROR, SMS_VERIFICATION_FAILED, FINISH
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

        abstract String getDoneButtonTitle();
    }

    private TextView                  edit_title   = null;
    private ProgressBar               waitWheel    = null;
    private Button                    doneBT       = null;
    private Vector<RegistrationField> fields       = null;
    private NumberField               numberField  = null;
    private NameField                 nameField    = null;
    private SmsField                  smsField     = null;
    private RegistrationField         currentField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    private void setViews()
    {
        waitWheel = (ProgressBar) findViewById(R.id.registration_wait_wheel);
        doneBT = (Button) findViewById(R.id.registration_done_bt);
        edit_title = (TextView) findViewById(R.id.registration_request_title);

        fields = new Vector<>();

        fields.add((numberField = new NumberField()));
        fields.add((nameField = new NameField()));
        fields.add(smsField = new SmsField());

        for (RegistrationField field : fields)
        {
            field.init();
            field.setCallback(onFinishCallback);
        }

        setCurrentField(numberField);

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
                case SEND_SMS_VERIFICATION:
                {
                    setCurrentField(smsField);

                    new HandlerThread("")
                    {
                        @Override
                        public void run()
                        {
                            // TODO handle return value
                            getService().sendVerificationSms(numberField.getNumber(), smsField.generateSmsCode(4));
                        }
                    }.start();

                    break;
                }

                case CREATE_USER:
                {
                    break;
                }

                case SHOW_NAME_FROM_JSON:
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

                case GET_NAME:
                {
                    setCurrentField(nameField);
                    break;
                }

                case SHOW_NUMBER_ERROR:
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(RegistrationActivity.this, "Please enter a right Number!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }

                case SMS_VERIFICATION_FAILED:
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(RegistrationActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                }

                case FINISH:
                {
                    break;
                }
            }

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    doneBT.setEnabled(true);
                    waitWheel.setVisibility(View.GONE);
                }
            });
        }
    };

    private void setCurrentField(final RegistrationField field)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                edit_title.setText(field.getTitle());

                for (int i = 0; i < fields.size(); i++)
                {
                    fields.elementAt(i).setVisibility(!field.equals(fields.elementAt(i)) ? View.INVISIBLE : View.VISIBLE);
                }
                field.bringToFront();

                doneBT.setText(field.getDoneButtonTitle());

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
        View     registration_number_view = null;
        EditText number_et                = null;

        @Override
        public void init()
        {
            registration_number_view = findViewById(R.id.registration_number_view);
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
                    registration_number_view.setVisibility(status);
                }
            });
        }

        @Override
        void bringToFront()
        {
            registration_number_view.bringToFront();
        }

        @Override
        public String getTitle()
        {
            return "To Get Started, Please Enter your Phone Number";
        }

        @Override
        public void onDonePressed()
        {
            callback.onFinish(NextAction.SEND_SMS_VERIFICATION);
            //new Thread(checkIfUserExist).start();
        }

        @Override
        String getDoneButtonTitle()
        {
            return "Next";
        }

        public String getNumber()
        {
            return number_et.getText().toString();
        }
    }

    private class NameField extends RegistrationField
    {
        View     registration_name_view = null;
        EditText name_et                = null;
        EditText last_name_et           = null;

        @Override
        void init()
        {
            registration_name_view = findViewById(R.id.registration_name_lastname_view);
            name_et = (EditText) findViewById(R.id.registration_name_et);
            last_name_et = (EditText) findViewById(R.id.registration_lastname_et);
        }

        @Override
        void setVisibility(final int status)
        {
            RegistrationActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    registration_name_view.setVisibility(status);
                }
            });
        }

        @Override
        void bringToFront()
        {
            registration_name_view.bringToFront();
        }

        @Override
        String getTitle()
        {
            return "To Get Started, Please Enter your Name";
        }

        @Override
        void onDonePressed()
        {

        }

        @Override
        String getDoneButtonTitle()
        {
            return "Done";
        }
    }

    private class SmsField extends RegistrationField
    {
        private View     registration_sms_view = null;
        private EditText registration_sms_et   = null;
        private String   generatedSmsCode      = null;

        @Override
        void init()
        {
            registration_sms_view = findViewById(R.id.registration_sms_view);
            registration_sms_et = (EditText) findViewById(R.id.registration_sms_et);
        }

        @Override
        void setVisibility(final int status)
        {
            RegistrationActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    registration_sms_view.setVisibility(status);
                }
            });
        }

        @Override
        void bringToFront()
        {
            registration_sms_view.bringToFront();
        }

        @Override
        String getTitle()
        {
            return "Type the SMS verification code";
        }

        @Override
        void onDonePressed()
        {
            String currentEtCode = registration_sms_et.getText().toString();

            if
            (
                currentEtCode != null
                &&
                currentEtCode.length() == generatedSmsCode.length()
                &&
                currentEtCode.equals(generatedSmsCode))
            {
                new Thread(checkIfUserExist);
            }
            else callback.onFinish(NextAction.SMS_VERIFICATION_FAILED);
        }

        @Override
        String getDoneButtonTitle()
        {
            return "Next";
        }

        private Runnable checkIfUserExist = new Runnable()
        {
            @Override
            public void run()
            {
                String number = RegistrationActivity.this.numberField.getNumber();

                if (getService().checkIfUserExist(number))
                {
                    getService().downloadUserJsonIfNeed(number);
                    callback.onFinish(NextAction.SHOW_NAME_FROM_JSON);
                }
                else
                {
                    callback.onFinish(NextAction.GET_NAME);
                }
            }
        };

        public String generateSmsCode(int length)
        {
            Random random = new Random(new Date().getTime());

            generatedSmsCode = "";

            for (int i = 0; i < length; i++)
            {
                generatedSmsCode += random.nextInt(9);
            }

            return generatedSmsCode;
        }
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
//        setCurrentField(currentField);
//    }
