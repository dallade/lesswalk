package com.lesswalk;

import android.os.Bundle;
import android.app.Activity;

import com.lesswalk.bases.BaseActivity;

public class LesswalkSettings extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesswalk_settings);
    }

    @Override
    protected void mainServiceConnected()
    {

    }

}
