package com.lesswalk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by elazarkin on 5/17/17.
 */

public class RoundedImageView extends ImageView
{
    public RoundedImageView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable)
    {
        //drawable.setColorFilter( 0xffff0000, PorterDuff.Mode.MULTIPLY);
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        final float halfWidth  = canvas.getWidth() / 2;
        final float halfHeight = canvas.getHeight() / 2;
        final float radius     = Math.max(halfWidth, halfHeight);
        Paint       paint      = new Paint();

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(halfWidth, halfHeight, radius, paint);

        super.onDraw(canvas);
    }
}
