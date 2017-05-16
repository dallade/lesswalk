package com.lesswalk.views;

import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by elazarkin on 5/16/17.
 */

public class NegativeImageButton extends ImageButton
{
    private static final float[] NEGATIVE =
    {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };

    public NegativeImageButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    @Override
    public void setImageDrawable(@Nullable Drawable drawable)
    {
        drawable.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));

        super.setImageDrawable(drawable);
    }
}
