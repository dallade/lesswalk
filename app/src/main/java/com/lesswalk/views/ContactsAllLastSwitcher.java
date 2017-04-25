package com.lesswalk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.lesswalk.R;

/**
 * Created by elazarkin on 4/24/17.
 */

public class ContactsAllLastSwitcher extends View
{
    public enum ContactAllLastSwitcherModes
    {
        MODE_RECENT,
        MODE_ALL
    }

    private Bitmap                      recentOn = null;
    private Bitmap                      allOn    = null;
    private ContactAllLastSwitcherModes mode     = ContactAllLastSwitcherModes.MODE_ALL;

    public ContactsAllLastSwitcher(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    private void createBitmaps(int w, int h)
    {
        float  STROKE    = 2.0f;
        Bitmap all       = BitmapFactory.decodeResource(getResources(), R.drawable.contacts_segment_icon_2x);
        Bitmap recent    = BitmapFactory.decodeResource(getResources(), R.drawable.recents_segment_icon_2x);
        Bitmap work      = null;
        Canvas cwork     = null;
        Canvas cAllOn    = null;
        Canvas cRecentOn = null;
        Paint  p         = new Paint();

        float fixed_w = 0.0f;
        float fixed_h = 0.0f;

        if(w > h*2.0f)
        {
            fixed_w = h*2.0f;
            fixed_h = h;
        }
        else
        {
            fixed_w = w;
            fixed_h = w/2.0f;
        }

        p.setColor(Color.WHITE);
        p.setStrokeWidth(STROKE);
        p.setStyle(Paint.Style.STROKE);

        allOn = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        recentOn = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        work = Bitmap.createBitmap((int)fixed_w, (int) fixed_h, Bitmap.Config.ARGB_8888);

        cwork = new Canvas(work);
        cAllOn = new Canvas(allOn);
        cRecentOn = new Canvas(recentOn);

        drawRoundRect(cwork, STROKE/2, STROKE/2, work.getWidth()-STROKE, work.getHeight()-STROKE/2, 10, p);
        cwork.drawLine(work.getWidth()/2, 0, work.getWidth()/2, work.getHeight(), p);

        cAllOn.drawBitmap(work, w/2 - fixed_w/2, h/2 - fixed_h/2, null);
        cRecentOn.drawBitmap(work, w/2 - fixed_w/2, h/2 - fixed_h/2, null);

        all.recycle();
        recent.recycle();
        work.recycle();
    }

    private void drawRoundRect(Canvas c, float x0, float y0, float w, float h, float r, Paint p)
    {
        c.drawLine(x0 + r, y0, x0 + w - r, y0, p);
        c.drawLine(x0 + r, y0+h, x0 + w - r, y0+h, p);
        c.drawLine(x0, y0 + r, x0, y0 + h - r, p);
        c.drawLine(x0+w, y0 + r, x0+w, y0 + h - r, p);

        c.drawArc(new RectF(x0, y0, x0+r*2, y0+r*2), -90, -90, false, p);
        c.drawArc(new RectF(x0, y0+h-r*2, x0+r*2, y0+h), 180, -90, false, p);
        c.drawArc(new RectF(x0+w-r*2, y0, x0+w, y0 +r*2), -90, 90, false, p);
        c.drawArc(new RectF(x0+w-r*2, y0+h-r*2, x0+w, y0+h), 0, 90, false, p);
    }

    @Override
    protected void onDraw(Canvas c)
    {
        if(recentOn == null || allOn == null)// TODO || w or h changed
        {
            createBitmaps(c.getWidth(), c.getHeight());
        }

        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if(mode == ContactAllLastSwitcherModes.MODE_ALL)
        {
            c.drawBitmap(allOn, 0, 0, null);
        }
    }
}
