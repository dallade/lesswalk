package com.lesswalk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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
                getService().setLocalNumber(numberET.getText().toString(), new ILesswalkService.ISetLocalNumberCallback()
                {
                    @Override
                    public void onSuccess()
                    {
                        waitWheel.setVisibility(View.INVISIBLE);
                        doneBT.setEnabled(true);
                        finish();
                    }

                    @Override
                    public void onError(int errorID)
                    {
                        waitWheel.setVisibility(View.INVISIBLE);
                        doneBT.setEnabled(true);
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
