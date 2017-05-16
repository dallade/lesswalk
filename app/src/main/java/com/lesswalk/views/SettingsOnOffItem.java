package com.lesswalk.views;

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

import com.lesswalk.R;

/**
 * Created by elazarkin on 5/16/17.
 */

public class SettingsOnOffItem extends LinearLayout
{
    private String  text        = null;
    private String  description = null;
    private int     textColor   = Color.BLACK;

    public SettingsOnOffItem(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        if(attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SettingsOnOffItem);

            text = a.getString(R.styleable.SettingsOnOffItem_android_text);
            textColor = a.getColor(R.styleable.SettingsOnOffItem_android_textColor, Color.WHITE);

            Log.d("elazarkin9", "SettingsOnOffItem contsructor text=" + text);

            a.recycle();
        }
        setItem();
    }

    private void setItem()
    {
        RelativeLayout item = (RelativeLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_on_off_item, null);

        if(item != null)
        {
            TextView textTV = (TextView) (item.findViewById(R.id.settings_on_off_text));

            addView(item, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if(textTV != null)
            {
                textTV.setText("" + text);
            }
        }
    }
}
