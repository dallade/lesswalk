package com.lesswalk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lesswalk.R;
import com.lesswalk.contact_page.navigation_menu.ContactSignatureSlideLayout;

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

    public interface IContactsAllLastSwitcherCallback
    {
        void onModeChanged(ContactAllLastSwitcherModes mode);
    }

    private Bitmap                           recentOn        = null;
    private Bitmap                           allOn           = null;
    private ContactAllLastSwitcherModes      mode            = ContactAllLastSwitcherModes.MODE_ALL;
    private RectF                            recentTouchArea = null;
    private RectF                            allTouchArea    = null;
    private IContactsAllLastSwitcherCallback callback        = null;

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
        allTouchArea = new RectF(w*3/4 - fixed_h/2, h/2 - fixed_h/2, w*3/4 + fixed_h/2, h/2 + fixed_h/2);
        cRecentOn.drawBitmap(work, w/2 - fixed_w/2, h/2 - fixed_h/2, null);
        recentTouchArea = new RectF(w/4 - fixed_h/2, h/2 - fixed_h/2, w/4 + fixed_h/2, h/2 + fixed_h/2);

        fillColor(allOn, allOn.getWidth()*3/4, allOn.getHeight()/2, Color.WHITE);
        fillColor(recentOn, recentOn.getWidth()/4, recentOn.getHeight()/2, Color.WHITE);

        drawIconLeft(allOn, recent, work.getHeight(), Color.WHITE, Color.BLACK);
        drawIconRigth(allOn, all, work.getHeight(), Color.BLACK, Color.WHITE);

        drawIconLeft(recentOn, recent, work.getHeight(), Color.BLACK, Color.WHITE);
        drawIconRigth(recentOn, all, work.getHeight(), Color.WHITE, Color.BLACK);

        all.recycle();
        recent.recycle();
        work.recycle();
    }

    private void fillColor(Bitmap bit, int x0, int y0, int color)
    {
        int startColor = bit.getPixel(x0, y0);

        if (color != startColor)
        {
            fillColor(bit, x0, y0, color, startColor);
        }
    }

    private void fillColor(Bitmap bit, int x0, int y0, int color, int startColor)
    {
        Point vector[]     = new Point[bit.getWidth() * bit.getHeight()];
        int   currentIndex = 0;
        int   size         = 0;
        int   X_OFFSET[]   = {-1,  0, 1, -1, 0, 1, -1, 0, 1};
        int   Y_OFFSET[]   = {-1, -1,-1,  0, 0, 0,  1, 1, 1};
        int   x            = 0;
        int   y            = 0;

        vector[size++] = new Point(x0, y0);
        bit.setPixel(x, y, color);

        while(currentIndex < size)
        {
            x = vector[currentIndex].x;
            y = vector[currentIndex].y;

            for(int i = 0; i < 8; i++)
            {
                x = vector[currentIndex].x + X_OFFSET[i];
                y = vector[currentIndex].y + Y_OFFSET[i];

                if(x < 0 || x >= bit.getWidth() || y < 0 || y >= bit.getHeight()) continue;

                if(bit.getPixel(x, y) == startColor)
                {
                    vector[size++] = new Point(x, y);
                    bit.setPixel(x, y, color);
                }
            }
            currentIndex++;
        }
    }

    private void drawIconRigth(Bitmap bit, Bitmap icon, int h, int color, int negative)
    {
        drawIconIntoSwitcher(bit, bit.getWidth()*3/4, bit.getHeight()/2, icon, h*2/3, color, negative);
    }

    private void drawIconLeft(Bitmap bit, Bitmap icon, int h, int color, int negative)
    {
        drawIconIntoSwitcher(bit, bit.getWidth()/4, bit.getHeight()/2, icon, h*2/3, color, negative);
    }

    private void drawIconIntoSwitcher(Bitmap bit, int cx, int cy, Bitmap icon, int h, int color, int negative)
    {
        Bitmap work = icon.createScaledBitmap(icon, h, h, false);

        for(int y = 0; y < work.getHeight(); y++)
        {
            for (int x = 0; x < work.getWidth(); x++)
            {
                int alpha = (work.getPixel(x, y)>>24);

                if(alpha > 0) bit.setPixel(cx - work.getWidth()/2 + x, cy - work.getHeight()/2 + y, color);
                else bit.setPixel(cx - work.getWidth()/2 + x, cy - work.getHeight()/2 + y, negative);
                //bit.setPixel(cx - work.getWidth()/2 + x, cy - work.getHeight()/2 + y, work.getPixel(x, y));
                //bit.setPixel(x, y, work.getPixel(x, y));
            }
        }

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
        else if(mode == ContactAllLastSwitcherModes.MODE_RECENT)
        {
            c.drawBitmap(recentOn, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        if(e.getAction() == MotionEvent.ACTION_UP)
        {
            float x = e.getX();
            float y = e.getY();

            boolean mode_changed = false;

            Log.d("elazarkin3", "" + x + "," + y + "  " + allTouchArea + "  " + recentTouchArea);

            if(allTouchArea.contains(x, y))
            {
                if(mode != ContactAllLastSwitcherModes.MODE_ALL)
                {
                    mode = ContactAllLastSwitcherModes.MODE_ALL;
                    mode_changed = true;
                }
            }
            else if(recentTouchArea.contains(x, y))
            {
                if(mode != ContactAllLastSwitcherModes.MODE_RECENT)
                {
                    mode = ContactAllLastSwitcherModes.MODE_RECENT;
                    mode_changed = true;
                }
            }

            if(mode_changed)
            {
                invalidate();
                if(callback != null)
                {
                    callback.onModeChanged(mode);
                }
            }

            Log.d("elazarkin3", "" + mode);

            return false;
        }
        else return true;
    }

    public void setCallback(IContactsAllLastSwitcherCallback callback)
    {
        this.callback = callback;
    }
}
