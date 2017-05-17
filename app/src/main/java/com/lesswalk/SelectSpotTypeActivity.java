package com.lesswalk;

import android.os.Bundle;

import com.lesswalk.bases.BaseActivity;

public class SelectSpotTypeActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_spot_type);
    }

    @Override
    protected void mainServiceConnected()
    {

    }
}
