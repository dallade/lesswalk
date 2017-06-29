package com.lesswalk.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.lesswalk.R;

/**
 * Created by elazarkin on 5/17/17.
 */

public class SelectSpotTypeItem extends LinearLayout
{
    private LinearLayout layouts[] = null;

    public SelectSpotTypeItem(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        setItems();
    }

    private void setItems()
    {
        ScrollView sv = (ScrollView) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.select_spot_item, null);

        LinearLayout item = (LinearLayout) sv.findViewById(R.id.select_spot_item_major_item);

        //select_spot_item_major_item

        layouts = new LinearLayout[item.getChildCount()];

        for(int i = 0; i < layouts.length; i++)
        {
            layouts[i] = (LinearLayout) item.getChildAt(i);
        }

        addView(sv, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void addRoundedIconButtonWithText(RoundedNegativeIconButtonWithText item)
    {
        int i = layouts.length-1;

        for(; i > 0; i--)
        {
            if(layouts[i].getChildCount() < layouts[i-1].getChildCount()) break;
        }

        Log.d("elazarkin17", "added addRoundedIconButtonWithText on " + i + " index");

        item.setGravity(Gravity.CENTER);
        layouts[i].addView(item);
    }

    public int getSelectSpotTypeChildCount()
    {
        int count = 0;

        for(LinearLayout l:layouts)
        {
            count += l.getChildCount();
        }

        return count;
    }

    public View getSelectSpotTypeChildAt(int index)
    {
        int layoutIndex = index%layouts.length;

        return layouts[layoutIndex].getChildAt(index/layouts.length);
    }
}
