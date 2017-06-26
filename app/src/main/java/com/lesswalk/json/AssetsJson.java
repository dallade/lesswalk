package com.lesswalk.json;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by elazar on 23/06/17.
 */

public class AssetsJson
{
    private Images images[]     = null;
    private String content_type = null;
    private String key          = null;

    public String getKey() {return key;}
    public String getContent_type() {return content_type;}
    public Images[] getImages() {return images;}

    public void setKey(String key)
    {
        this.key = key;
        Log.d("elazarkin", "somebody setKey: " + key);
    }
    public void setContent_type(String content_type)
    {
        this.content_type = content_type;
        Log.d("elazarkin", "somebody setContent_type: " + content_type);
    }
    public void setImages(Images[] images)
    {
        Log.d("elazarkin", "somebody setContent_images: " + images.length);
        this.images = images;
    }

    public void toJson(String file)
    {
        Gson gson = new Gson();

        try
        {
            String test = gson.toJson(this);
            Log.d("elazarkin",""+test);
            gson.toJson(this, new FileWriter(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static AssetsJson createDummy()
    {
        AssetsJson        ret      = new AssetsJson();

        ret.setKey("some key");
        ret.setContent_type("assets");

        Images images[] = new Images[3];

        for (int i = 0; i < images.length; i++)
        {
            images[i] = ret.new Images();

            images[i].setKey("key_" + i);
            images[i].setName("some_"+i+".png");
            images[i].setType("type_" + i);
            images[i].setTitle("title_" + i);

            if(i > 0) images[i].setHidden(i%2 == 0);
        }

        ret.setImages(images);

        return ret;
    }

    public static AssetsJson createFromFile(File file)
    {
        try
        {
            byte        buffer[] = new byte[(int) file.length()];
            InputStream is       = new FileInputStream(file);
            int         len      = 0;
            String      json     = null;

            is.read(buffer);
            is.close();

            len = removeTabs(buffer);
            json = new String(buffer, 0, len);

            Log.d("elazarkin", "without tabs json: ");
            Log.d("elazarkin", "" + json);
            json = replaceAll(json, ",}", "}");
            json = replaceAll(json, ",]", "]");
            Log.d("elazarkin", "fixed: ");
            Log.d("elazarkin", "" + json);

            return new Gson().fromJson(json, AssetsJson.class);
        }
        catch (Exception e){e.printStackTrace();}

        return null;
    }

    private static String replaceAll(String text, String _last, String _new)
    {
        int    MAX_LEN  = (_new.length() > _last.length()) ? (int) (1.0f * _new.length() / _last.length() + 1024) : text.length();
        byte   buffer[] = new byte[MAX_LEN];
        int    index    = 0;

        for (int i = 0; i < text.length();)
        {
            if (i < text.length()-_last.length() && text.substring(i, i+_last.length()).equals(_last))
            {
                //ans += _new;
                System.arraycopy(_new.getBytes(), 0, buffer, index, _new.length());
                i += _last.length();
                index += _new.length();
            }
            else
            {
                buffer[index++] = (byte) text.charAt(i++);
            }
        }

        return new String(buffer, 0, index);
    }

    private static int removeTabs(byte[] buffer)
    {
        int index = 0;
        for (int i = 0; i < buffer.length; i++)
        {
            if(buffer[i] >= ' ' && buffer[i] < 128)
            {
                if(i > index) buffer[index] = buffer[i];
                index++;
            }
        }
        buffer[index] = '\0';

        return index;
    }

    public class Images
    {
        private String  key    = null;
        private String  title  = null;
        private String  type   = null;
        private String  name   = null;
        private boolean hidden = false;

        public String getKey() {return key;}
        public String getTitle() {return title;}
        public String getType() {return type;}
        public String getName() {return name;}
        public boolean isHidden() {return hidden;}
        public void setKey(String key) {this.key = key;}
        public void setTitle(String title) {this.title = title;}
        public void setType(String type) {this.type = type;}
        public void setName(String name) {this.name = name;}
        public void setHidden(boolean hidden) {this.hidden = hidden;}
    }
}