package com.lesswalk.contact_page.navigation_menu;

import android.util.Log;

import java.io.InputStream;

/**
 * Created by root on 2/28/16.
 */
public class CarusselContact
{
	private static final String TAG = "lesswalkCarusselContact";
    //private Bitmap picture = null;
	private InputStream pic_src = null;
    private String      name    = null;
    private String      number  = null;

    public CarusselContact(InputStream pic_src, String name, String number)
    {
        this.pic_src = pic_src;
        this.name    = name;
        this.number  = number;
        
        //Log.d(TAG, String.format("CarusselContact added: %s %s", this.name, this.number));
    }

    public InputStream getPictureIS()
    {
        return pic_src;
    }

    public String getName()
    {
        return name;
    }
    
    public String getNumber() 
    {
		return number;
	}
}