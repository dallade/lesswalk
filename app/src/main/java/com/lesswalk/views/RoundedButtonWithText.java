package com.lesswalk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lesswalk.R;

/**
 * Created by elazarkin on 5/1/17.
 */

public class RoundedButtonWithText extends View
{
    private static final int LEFT  = 0x3;
    private static final int RIGHT = 0x5;

    private Drawable        origin_icon  = null;
    private Bitmap          icon         = null;
    private Canvas          cIcon        = null;
    private Bitmap          pressedIcon  = null;
    private Canvas          cPressedIcon = null;
    private String          text         = null;
    private int             textColor    = Color.WHITE;
    private int             fit          = 0;
    private boolean         pressed      = false;
    private OnClickListener listener     = null;

    public RoundedButtonWithText(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if(attrs != null)
        {
            TypedArray a =getContext().obtainStyledAttributes(attrs, R.styleable.RoundedButtonWithTextAttr);

            text = a.getString(R.styleable.RoundedButtonWithTextAttr_android_text);
            textColor = a.getColor(R.styleable.RoundedButtonWithTextAttr_android_textColor, Color.WHITE);
            origin_icon = a.getDrawable(R.styleable.RoundedButtonWithTextAttr_android_src);
            fit = a.getInt(R.styleable.RoundedButtonWithTextAttr_android_gravity, 0);

            a.recycle();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        listener = l;
    }

    @Override
    protected void onDraw(Canvas c)
    {
        Bitmap current = null;
        if(icon == null ||getWidth() != icon.getWidth() || getHeight() != icon.getHeight())
        {
            createIcon(getWidth(), getHeight());
        }

        if(pressed)
        {
            current = pressedIcon;
        }
        else current = icon;

        if(icon != null)
        {
            c.drawBitmap(current, 0, 0, null);
        }
    }

    private void createIcon(int w, int h)
    {
        int icon_h = (int) (h*0.8f + 0.5f);
        int text_h = h-icon_h;

        if(icon != null && (icon.getWidth() != w || icon.getHeight() != h))
        {
            icon.recycle();
            icon = null;
            cIcon = null;
        }

        if(pressedIcon != null && (pressedIcon.getWidth() != w || pressedIcon.getHeight() != h))
        {
            pressedIcon.recycle();
            pressedIcon = null;
            cPressedIcon = null;
        }

        if(icon == null)
        {
            icon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cIcon = new Canvas(icon);
        }

        if(pressedIcon == null)
        {
            pressedIcon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cPressedIcon = new Canvas(pressedIcon);
        }

        if(origin_icon != null)
        {
            Bitmap work  = null;
            Canvas cwork = null;
            int    x0    = 0;
            int    y0    = 0;

            work  = Bitmap.createBitmap(icon_h*origin_icon.getIntrinsicWidth()/origin_icon.getIntrinsicHeight(), icon_h, Bitmap.Config.ARGB_8888);
            cwork = new Canvas(work);
            origin_icon.setBounds(0, 0, cwork.getWidth(), cwork.getHeight());
            origin_icon.draw(cwork);

            Log.d("elazarkin6", "fit=" + fit);

            switch (fit)
            {
                case LEFT:
                {
                    x0 = 0;
                    y0 = 0;
                    break;
                }
                case RIGHT:
                {
                    x0 = icon.getWidth() - work.getWidth();
                    y0 = 0;
                    break;
                }
                default:
                {
                    x0 = icon.getWidth()/2-work.getWidth()/2;
                    y0 = 0;
                }
            }

            drawIcon(cIcon, work, x0, y0, Color.WHITE);
            drawIcon(cPressedIcon, work, x0, y0, Color.YELLOW);

            work.recycle();
        }
    }

    private void drawIcon(Canvas c, Bitmap icon, int x0, int y0, int color)
    {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(x0 + icon.getWidth()/2, y0 + icon.getHeight()/2, icon.getHeight()/2, p);
        c.drawBitmap(icon, x0, y0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int     x       = (int) (event.getX() + 0.5f);
        int     y       = (int) (event.getY() + 0.5f);
        boolean outside = false;
        //
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
        {
            outside = true;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            pressed = true;
            invalidate();
        }
        else if(outside || event.getAction() == MotionEvent.ACTION_UP)
        {
            pressed = false;
            invalidate();
            if(!outside && listener != null) listener.onClick(this);
            return false;
        }

        return true;
    }
}
