package com.lesswalk.contact_page.navigation_menu;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by root on 1/28/16.
 */
public class NavigationIcons
{
    private static final float[] baseFrameObject =
    {
            -1.0f, -1.0f, //0.0f,  // top left
             1.0f, -1.0f, //0.0f,  // bottom left
            -1.0f,  1.0f, //0.0f,  // bottom right
             1.0f,  1.0f, //0.0f  // top right
    };

    private static final float textureMap[] =
    {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private int textureID = -1;
    private float iconFrame[] = null;

    public NavigationIcons(Bitmap icon, String text, int iconSize, int textSize, int index, int icons_in_the_row)
    {
        Bitmap iconWithText = null;
        int    temp[]       = {-1};

        iconWithText = Bitmap.createBitmap(iconSize, (int) (iconSize + textSize*2.5f), Bitmap.Config.ARGB_8888);

        createIconFrame(iconWithText.getWidth(), iconWithText.getHeight(), index, icons_in_the_row);
        drawIconWithText(iconWithText, icon, text, textSize, iconSize);

        GLES20.glGenTextures(1, temp, 0);

        textureID = temp[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//GL_LINEAR_MIPMAP_LINEAR
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, iconWithText, 0);

        // Set texture coordinates
//            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        iconWithText.recycle();
    }

    public int getTextureID()
    {
        return textureID;
    }

    private void drawIconWithText(Bitmap out, Bitmap icon, String text, int textSize, int iconSize)
    {
        Canvas canvas  = null;
        Paint paint   = null;
        String words[] = null;
        Rect bounds    = new Rect();
        //
        canvas       = new Canvas(out);
        paint        = new Paint();
        words        = text.split(" ");

        canvas.drawBitmap
        (
                icon,
                new Rect(0, 0, icon.getWidth(), icon.getHeight()),
                new Rect(0, 0, iconSize, iconSize),
                null
        );

        paint.setTextSize(textSize);
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);
        for(int i = 0; i < words.length && i < 2; i++)
        {
            paint.getTextBounds(words[i], 0, words[i].length(), bounds);
            canvas.drawText(words[i], out.getWidth()/2 - bounds.width()/2, iconSize + textSize*(i+1.0f), paint);
        }
    }

    private void createIconFrame(float w, float h, int index, int amount_in_row)
    {
        float scale    = 0.8f;
        float aspect   = (float)w/h;
        float x0       = -(amount_in_row>>1)/aspect;
        float xend     =  (amount_in_row>>1)/aspect;
        float y0       =  0.0f;
        float xStep    =  aspect*2.0f*(amount_in_row+1)/amount_in_row;
//        float x0       =-1.0f + aspect/amount_in_row;
//        float y0       = 0.0f;
//        float xStep    = 2.0f/amount_in_row;

        iconFrame = new float[]
        {
                baseFrameObject[0]*aspect, baseFrameObject[1],
                baseFrameObject[2]*aspect, baseFrameObject[3],
                baseFrameObject[4]*aspect, baseFrameObject[6],
                baseFrameObject[6]*aspect, baseFrameObject[7]
        };

        for(int i = 0; i < iconFrame.length; i++)
        {
            iconFrame[i] *= scale;
        }

        for(int i = 0; i < iconFrame.length; i += 2)
        {
            iconFrame[i]   += (x0 + xStep*index);
            iconFrame[i+1] += (y0);
        }
    }

    private FloatBuffer loadFloatBuffer(float buff[])
    {
        FloatBuffer ret = ByteBuffer.allocateDirect(buff.length * Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
        ret.put(buff);
        ret.position(0);
        return ret;
    }


    public void drawSomeSelf(int posHandler, int textCoordsHandler, int textureHandler)
    {
        FloatBuffer vertexBuffer   = null;
        FloatBuffer texCoordBuffer = null;
        //
        vertexBuffer   = loadFloatBuffer(iconFrame);
        texCoordBuffer = loadFloatBuffer(textureMap);
        //
        GLES20.glEnableVertexAttribArray(posHandler);
        GLES20.glVertexAttribPointer(posHandler, 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE / 8, vertexBuffer);
        GLES20.glEnableVertexAttribArray(textCoordsHandler);
        GLES20.glVertexAttribPointer(textCoordsHandler, 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE / 8, texCoordBuffer);
        //
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureHandler, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
