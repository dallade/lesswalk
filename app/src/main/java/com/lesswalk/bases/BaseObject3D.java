package com.lesswalk.bases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.res.Resources;
import android.opengl.GLES20;

public class BaseObject3D 
{
    protected static String loadShaderFromGlslFile(Resources getter, int id)
    {
        BufferedReader reader = null;
        String ans  = "";
        String line = null;

        try
        {
            reader = new BufferedReader(new InputStreamReader(getter.openRawResource(id)));
            while((line = reader.readLine()) != null)
            {
                ans += line + "\n";
            }

            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return ans;
    }

    protected static int loadShader(int type, String shaderCode)
    {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    protected static int createProgram(String vertexShaderCode, String fragmentShaderCode)
    {
        int mProgram       = 0;
        int vertexShader   = 0;
        int fragmentShader = 0;

        vertexShader   = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        return mProgram;
    }
}
