package com.lesswalk.contact_page.navigation_menu;

import java.util.Vector;

import com.lesswalk.bases.BaseInterRendererLayout;

/**
 * Created by elazarkin on 1/22/16.
 */
public abstract class BaseNavigationMenuRenderer
{
    protected Vector<BaseInterRendererLayout> layouts = null;

    public interface OnSurfaceChangedCallback
    {
        void onSurfaceChanged(int w, int h);
    }

    public BaseNavigationMenuRenderer()
    {
        layouts = new Vector<BaseInterRendererLayout>();
    }

    public void addLayoutItem(BaseInterRendererLayout l)
    {
        layouts.add(l);
    }

    public abstract void moved(float lastX, float lastY, float x, float y);

    public abstract void clicked(float x, float y);
}