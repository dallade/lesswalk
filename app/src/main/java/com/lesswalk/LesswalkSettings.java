package com.lesswalk;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.database.AWS;

public class LesswalkSettings extends BaseActivity
{
    private Button deleteAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesswalk_settings);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String str = "DELETE http://52.70.155.39:30082/cmd/user/drop/AD1038E8-887B-4939-9412-DC716DCE38FB";
                //TODO delete zip from cloud
                Toast.makeText(LesswalkSettings.this, str, Toast.LENGTH_SHORT).show();
                view.setClickable(false);
                getService().deleteUserAccount(new AWS.OnRequestListener()
                {
                    @Override
                    public void onStarted()
                    {
                        Log.d("elazarkin16", "LesswalkSettings delete acount onStarted");
                    }

                    @Override
                    public void onFinished()
                    {
                        Log.d("elazarkin16", "LesswalkSettings delete acount onFinished");
                        finish();
                    }

                    @Override
                    public void onError(int errorId)
                    {
                        Log.d("elazarkin16", "LesswalkSettings delete acount onError");
                        view.setClickable(true);
                    }
                });
            }
        });
    }

    @Override
    protected void mainServiceConnected()
    {

    }

}
