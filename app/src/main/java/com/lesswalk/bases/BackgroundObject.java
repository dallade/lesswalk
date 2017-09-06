package com.lesswalk.bases;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.lesswalk.R;

import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Created by elazar on 02/09/17.
 */

public class BackgroundObject extends BaseObject3D
{
    private int textID[] = {-1};
    private int program  = -1;

    protected static final int   HANDLER_ATTR_VER_POS_INDEX     = 0;
    protected static final int   HANDLER_ATTR_TEX_COORD_INDEX   = 1;
    protected static final int   HANDLER_ATTR_SIZE              = 2;
    protected static final int   HANDLER_UNIF_TEXTURE_INDEX     = 0;
    protected static final int   HANDLER_UNIF_SIZE              = 1;

    private int attrHandlers[] = null;
    private int unifHandlers[] = null;

    private FloatBuffer vertexesBuffer   = null;
    private FloatBuffer textureMapBuffer = null;

    private static final float[] baseFrameObject =
    {
        -1.0f, -1.0f, // top left
        1.0f, -1.0f,  // bottom left
        -1.0f,  1.0f, // bottom right
        1.0f,  1.0f  // top right
    };

    private static final float textureMap[] =
    {
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    };


    public BackgroundObject(Context context)
    {
        program = createProgram
        (
            loadShaderFromGlslFile(context.getResources(), R.raw.background_vertex_shader),
            loadShaderFromGlslFile(context.getResources(), R.raw.background_fragment_shader)
        );

        attrHandlers     = new int[HANDLER_ATTR_SIZE];
        unifHandlers     = new int[HANDLER_UNIF_SIZE];

        String attrNames[] = new String[HANDLER_ATTR_SIZE];
        String unifNames[] = new String[HANDLER_UNIF_SIZE];

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(program);

        attrNames[HANDLER_ATTR_VER_POS_INDEX]   = "aPosition";
        attrNames[HANDLER_ATTR_TEX_COORD_INDEX] = "textureCoord";

        for (int i = 0; i < attrNames.length; i++)
        {
            attrHandlers[i] = GLES20.glGetAttribLocation(program, attrNames[i]);
            Log.d("elazarkin", "attr: " + attrNames[i] + " = " + attrHandlers[i]);
        }

        unifNames[HANDLER_UNIF_TEXTURE_INDEX]      = "texSampler2D";

        for (int i = 0; i < unifNames.length; i++)
        {
            unifHandlers[i] = GLES20.glGetUniformLocation(program, unifNames[i]);
            Log.d("elazarkin", "unif: " + unifNames[i] + " = " + unifHandlers[i]);
        }
    }

    protected void generateTextureID()
    {
        GLES20.glGenTextures(1, textID, 0);
    }

    public void generateTextureID(Bitmap image)
    {
        generateTextureID();
        //
        setImage(getTextureID(), image);
    }

    private int getTextureID()
    {
        return textID[0];
    }

    private static void setImage(int id, Bitmap bit)
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bit, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
    }

    protected Buffer getVertexesBuffer()
    {
        if(vertexesBuffer == null)
        {
            vertexesBuffer = loadFloatBuffer(baseFrameObject);
        }
        return vertexesBuffer;
    }

    protected Buffer getTextureMapBuffer()
    {
        if(textureMapBuffer == null)
        {
            textureMapBuffer = loadFloatBuffer(textureMap);
        }
        return textureMapBuffer;
    }

    public void drawSelf()
    {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(program);

        GLES20.glEnableVertexAttribArray(attrHandlers[HANDLER_ATTR_VER_POS_INDEX]);
        GLES20.glVertexAttribPointer(attrHandlers[HANDLER_ATTR_VER_POS_INDEX], 2, GLES20.GL_FLOAT, false, 0, getVertexesBuffer());
        GLES20.glEnableVertexAttribArray(attrHandlers[HANDLER_ATTR_TEX_COORD_INDEX]);
        GLES20.glVertexAttribPointer(attrHandlers[HANDLER_ATTR_TEX_COORD_INDEX], 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE / 8, getTextureMapBuffer());
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(unifHandlers[HANDLER_UNIF_TEXTURE_INDEX], 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureID());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
