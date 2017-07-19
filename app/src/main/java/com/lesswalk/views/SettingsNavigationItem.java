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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

    private RadioButton radioButton   = null;
    private TextView    ifInstalledTv = null;
    private OnChooseCallback onChooseCallback;

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
        }

        setItem();
    }

    public String getPackageName()
    {
        return "" + description;
    }

    private void setItem()
    {
        RelativeLayout navigationItem = (RelativeLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_navigation_item, null);

        if(navigationItem != null)
        {
            TextView textTV = (TextView) (navigationItem.findViewById(R.id.setting_navigation_app_name));

            addView(navigationItem, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            radioButton = (RadioButton) navigationItem.findViewById(R.id.settings_navigation_item_radiobutton);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    if(onChooseCallback != null)
                    {
                        onChooseCallback.onChoose(SettingsNavigationItem.this);
                    }
                }
            });
            ifInstalledTv = (TextView) navigationItem.findViewById(R.id.setting_navigation_app_if_installed);

            if(textTV != null)
            {
                textTV.setText("" + text);
            }
        }
    }

    public boolean isEnabled()
    {
        return radioButton.isEnabled();
    }

    public void enableChoose(boolean isEnabled)
    {
        radioButton.setEnabled(isEnabled);
    }

    public void setIfInstalled(boolean ifInstalled)
    {
        enableChoose(ifInstalled);

        ifInstalledTv.setText(ifInstalled ? "Installed" : "Not Installed");
    }

    public void setChoosed(boolean choosed)
    {
        radioButton.setChecked(choosed);
    }

    public void setOnChooseCallback(OnChooseCallback onChooseCallback)
    {
        this.onChooseCallback = onChooseCallback;
    }

    public interface OnChooseCallback
    {
        void onChoose(SettingsNavigationItem item);
    }
}
