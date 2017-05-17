package com.lesswalk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by elazarkin on 5/17/17.
 */

public class RoundedNegativeImageView extends NegativeImageButton
{
    public RoundedNegativeImageView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
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
