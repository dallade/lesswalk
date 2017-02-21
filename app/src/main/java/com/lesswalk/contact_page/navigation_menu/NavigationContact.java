package com.lesswalk.contact_page.navigation_menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;

/**
 * Created by root on 1/28/16.
 */
public class NavigationContact
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

    private String name         = null;
    private String phone_number = null;
    private int    textureID    = -1;
    private float  iconFrame[]  = null;

    public NavigationContact(Bitmap pic, String _name, String _phone_number, int picSize, int textSize)
    {
        Bitmap pictureWithText = null;
        int    temp[]          = {-1};
        Paint  paint           = new Paint();
        Rect bounds            = new Rect();
        //
        this.name         = new String("" + _name);
        this.phone_number = new String("" + _phone_number);

        paint.setTextSize(textSize);
        paint.getTextBounds("*" + name + "*", 0, name.length() + 2, bounds);

        pictureWithText = Bitmap.createBitmap(picSize + bounds.width(), picSize, Bitmap.Config.ARGB_8888);

        createContactFrame(pictureWithText.getWidth(), pictureWithText.getHeight(), 6);
        //
        drawContactWithText(pictureWithText, pic, name, textSize, picSize);

        try
        {
            pictureWithText.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "test.png")));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        GLES20.glGenTextures(1, temp, 0);

        textureID = temp[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//GL_LINEAR_MIPMAP_LINEAR
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, pictureWithText, 0);

        // Set texture coordinates
//            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        pictureWithText.recycle();
    }

    public int getTextureID()
    {
        return textureID;
    }
    
    public String getName() 
    {
		return name;
	}

    private void drawContactWithText(Bitmap out, Bitmap pic, String name, int textSize, int picSize)
    {
        Canvas canvas  = null;
        Paint paint   = null;
        Rect bounds    = new Rect();
        //
        canvas       = new Canvas(out);
        paint        = new Paint();

        canvas.drawBitmap
        (
                pic,
                new Rect(0, 0, pic.getWidth(), pic.getHeight()),
                new Rect(0, 0, picSize, picSize),
                null
        );

        paint.setTextSize(textSize);
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);
        paint.getTextBounds(name, 0, name.length(), bounds);
        canvas.drawText(name, picSize, picSize/2 + textSize/2, paint);
    }

    private void createContactFrame(float w, float h, int amount_in_colums)
    {
        float aspect   = (float)w/h;
        float height   = 1.0f/amount_in_colums;

        iconFrame = new float[]
        {
            -1.0f                     , baseFrameObject[1]*height,
            -1.0f + 2.0f*aspect*height, baseFrameObject[3]*height,
            -1.0f                     , baseFrameObject[6]*height,
            -1.0f + 2.0f*aspect*height, baseFrameObject[7]*height
        };
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

	public float[] getCorners(float[] transformMatrix) 
	{
		float a[] = {iconFrame[0], iconFrame[1], 1.0f};
		float b[] = {iconFrame[2], iconFrame[3], 1.0f};
		float c[] = {iconFrame[4], iconFrame[5], 1.0f};
		float d[] = {iconFrame[6], iconFrame[7], 1.0f};
		
		float ans[]       = new float[4];
		float corners[][] = {a, b, c, d};
		
		for(int i = 0; i < corners.length; i++)
		{
			multiplyMvLikeGL(transformMatrix, corners[i], corners[i]);
			
			for(int j = 0; j < 3; j++) corners[i][j] /= corners[i][2];
		}
		
		Log.d("lesswalk_click", String.format
		(
			"Corners_%s: a(%3.1f, %3.1f) d(%3.1f, %3.1f)", //b(%3.1f, %3.1f) c(%3.1f, %3.1f)
			getName(),
			a[0], a[1],
			//b[0], b[1],
			//c[0], c[1],
			d[0], d[1]
		));
		
		ans[0] = a[0];
		ans[1] = a[1];
		ans[2] = d[0];
		ans[3] = d[1];
		
		return ans;
	}

	private void multiplyMvLikeGL(float[] transformMatrix, float[] vector, float[] result) 
	{
		float temp[] = (vector == result)?new float[result.length]:result;
		
		temp[0] = transformMatrix[0]*vector[0] + transformMatrix[3]*vector[1] + transformMatrix[6]*vector[2];
		temp[1] = transformMatrix[1]*vector[0] + transformMatrix[4]*vector[1] + transformMatrix[7]*vector[2];
		temp[2] = transformMatrix[2]*vector[0] + transformMatrix[5]*vector[1] + transformMatrix[8]*vector[2];
		
		if(vector == result)
		{
			System.arraycopy(temp, 0, result, 0, temp.length);
		}
	}

	public String getPhoneNumber() 
	{
		return phone_number;
	}
}
