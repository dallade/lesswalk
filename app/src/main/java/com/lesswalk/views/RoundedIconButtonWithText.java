package com.lesswalk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lesswalk.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by elazarkin on 5/17/17.
 */

public class RoundedIconButtonWithText extends LinearLayout
{
    private String          text      = null;
    private int             textSize  = 16;
    private int             textColor = Color.WHITE;
    private Drawable        src       = null;
    private OnClickListener listener  = null;
    private String          icon_name = null;

    public RoundedIconButtonWithText(Context context, int resource, String text, int textColor)
    {
        super(context);

        this.text = text;
        this.textColor = textColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            this.src = getResources().getDrawable(resource, null);
        }
        else
        {
            //noinspection deprecation
            this.src = getResources().getDrawable(resource);
        }

        setItem();
    }

    public RoundedIconButtonWithText(Context context, File resource, String text, String icon_name, int textColor)
    {
        super(context);

        this.text = text;
        this.textColor = textColor;
        this.icon_name = icon_name;
        try
        {
            this.src = new BitmapDrawable(getResources(), BitmapFactory.decodeStream(new FileInputStream(resource)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        setItem();
    }

    public RoundedIconButtonWithText(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        if(attrs != null)
        {
            TypedArray a =getContext().obtainStyledAttributes(attrs, R.styleable.RoundedIconButtonWithText);

            text = a.getString(R.styleable.RoundedIconButtonWithText_android_text);
            //textSize = a.getFloat(R.styleable.RoundedIconButtonWithText_android_textSize, 16.0f);
            textSize = a.getDimensionPixelSize(R.styleable.RoundedIconButtonWithText_android_textSize, 16);
            textColor = a.getColor(R.styleable.RoundedIconButtonWithText_android_textColor, Color.WHITE);
            src = a.getDrawable(R.styleable.RoundedIconButtonWithText_android_src);

            a.recycle();
        }
        
        setItem();
    }

    private void setItem()
    {
        LinearLayout     item = (LinearLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.rounded_negative_icon_with_text, null);
        RoundedImageView icon = (RoundedImageView) item.findViewById(R.id.rounded_icon_with_text_icon);
        TextView         tv   = (TextView) item.findViewById(R.id.rounded_negative_icon_with_text_text);

        if (src != null)
        {
            icon.setImageDrawable(src);
        }

        if (text != null && text.length() > 0)
        {
            tv.setText(text);
            tv.setTextColor(textColor);
            tv.setTextSize(textSize);
        }

        tv.setOnClickListener(localListener);
        icon.setOnClickListener(localListener);

        addView(item);
    }

    private OnClickListener localListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if(listener != null) listener.onClick(RoundedIconButtonWithText.this);
        }
    };

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        listener = l;
    }

    public String getText() {return text;}

    public String getIcon() {return icon_name;}
}
