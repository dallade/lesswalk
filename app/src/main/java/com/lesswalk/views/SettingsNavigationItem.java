package com.lesswalk.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lesswalk.R;

/**
 * Created by elazarkin on 5/15/17.
 */

public class SettingsNavigationItem extends LinearLayout
{
    private String  text        = null;
    private String  description = null;
    private int     textColor   = Color.BLACK;

    public SettingsNavigationItem(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        if(attrs != null)
        {
            TypedArray a =getContext().obtainStyledAttributes(attrs, R.styleable.SettingsNavigationItem);

            text = a.getString(R.styleable.SettingsNavigationItem_android_text);
            textColor = a.getColor(R.styleable.SettingsNavigationItem_android_textColor, Color.WHITE);
            description = a.getString(R.styleable.SettingsNavigationItem_android_description);

            a.recycle();

            RelativeLayout navigationItem = null;

            removeAllViews();

            navigationItem = (RelativeLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_navigation_item, null);

            if(navigationItem != null)
            {
                TextView textTV = (TextView) (navigationItem.findViewById(R.id.setting_navigation_app_name));

                addView(navigationItem, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                if(textTV != null)
                {
                    textTV.setText("" + text);

                    Log.d("elazarkin9", "SettingsNavigationItem contructor textTV != null, text = " + text);
                }
                else Log.d("elazarkin9", "SettingsNavigationItem contructor textTV = null");

                navigationItem.setVisibility(VISIBLE);
                navigationItem.bringToFront();
            }
            else
            {
                Log.d("elazarkin9", "SettingsNavigationItem contructor navigationItem = null");
            }

            Log.d("elazarkin9", "SettingsNavigationItem constructor! text = " + text);
        }
    }
}
