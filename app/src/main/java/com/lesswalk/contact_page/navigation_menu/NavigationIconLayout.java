package com.lesswalk.contact_page.navigation_menu;

import java.util.Vector;

import com.lesswalk.R;
import com.lesswalk.bases.BaseInterRendererLayout;
import com.lesswalk.bases.RectObject3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

/**
 * Created by root on 1/28/16.
 */
public class NavigationIconLayout extends BaseInterRendererLayout
{
    private static final int ICONS_IN_THE_ROW = 5;
    private static final int HANDLER_ATTR_VER_POS_INDEX   = 0;
    private static final int HANDLER_ATTR_TEX_COORD_INDEX = 1;
    private static final int HANDLER_ATTR_SIZE            = 2;

    private static final int HANDLER_UNIF_TEXTURE_INDEX   = 0;
    private static final int HANDLER_UNIF_FIX_MAT_INDEX   = 1;
    private static final int HANDLER_UNIF_SIZE            = 2;

    private Vector<NavigationIcons>   icons    = null;
    private int program                        = -1;
    private int attrHandlers[]                 = null;
    private int unifHandlers[]                 = null;

    private float basicTransformMatrix[] = null;

    public NavigationIconLayout(Context context, int w, int h, RendererLayoutParams params)
    {
        super(w, h, params);
        //
        init(context);
        setWhParams(w, h, params);
    }

    public NavigationIconLayout(Context context) 
    {
    	super();
	}
    
    @Override
    public void setWhParams(int _w, int _h, RendererLayoutParams _params) 
    {
    	super.setWhParams(_w, _h,_params);
    	
    	basicTransformMatrix = new float[9];
//    	w = _w;
//    	h = _h;
//    	params = _params;
    	
        createBasicTransformMatrix
        (
            basicTransformMatrix,
            (params.getEndWeightX() - params.getStartWeightX())*w,
            (params.getEndWeightY() - params.getStartWeightY())*h,
            1.0f
        );
    }
    
    public void init(Context context)
    {
        icons = new Vector<NavigationIcons>();
//        program = createProgram(navigationIconsVertexShader, navigationIconsFragmentShader);
        program = createProgram
        (
            RectObject3D.loadShaderFromGlslFile(context.getResources(), R.raw.navigation_icons_vertex_shader),
            RectObject3D.loadShaderFromGlslFile(context.getResources(), R.raw.navigation_icons_fragment_shader)
        );
        attrHandlers    = new int[getAttrHandlerAmmount()];
        unifHandlers    = new int[getUnifHandlerAmmount()];
        loadHandlers(attrHandlers, unifHandlers, program);
    }

	private void createBasicTransformMatrix(float mat[], float w, float h, float scale)
    {
        mat[0 ] = scale*h/w; mat[1 ] = 0.0f;       mat[2 ] = 0.0f;
        mat[3 ] = 0.0f;      mat[4 ] = scale*1.0f; mat[5 ] = 0.0f;
        mat[6 ] = 0.0f;      mat[7 ] = 0.0f;       mat[8 ] = 1.0f;
    }

    public void resetNavigationMenu()
    {
        for(int i = 0; i < icons.size(); i++)
        {
            GLES20.glDeleteTextures(1, new int[]{icons.elementAt(i).getTextureID()}, 0);
        }
        //
        icons.removeAllElements();
    }

    public void addIcon(Bitmap b, String text, int iconSize, int textSize, int index)
    {
        icons.add(new NavigationIcons(b, text, iconSize, textSize, index, ICONS_IN_THE_ROW));
    }

    protected int createProgram(String vertexShaderCode, String fragmentShaderCode)
    {
        int mProgram       = 0;
        int vertexShader   = 0;
        int fragmentShader = 0;

        vertexShader   = RectObject3D.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShader = RectObject3D.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

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

    protected int getAttrHandlerAmmount()
    {
        return HANDLER_ATTR_SIZE;
    }

    protected int getUnifHandlerAmmount()
    {
        return HANDLER_UNIF_SIZE;
    }

    protected void loadHandlers(int attrHandlers[], int uniformHandler[], int program)
    {
        String attrNames[] = new String[getAttrHandlerAmmount()];
        String unifNames[] = new String[getUnifHandlerAmmount()];

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(program);

        attrNames[HANDLER_ATTR_VER_POS_INDEX] = "vPosition";
        attrNames[HANDLER_ATTR_TEX_COORD_INDEX] = "a_TexCoordinate";

        for (int i = 0; i < attrNames.length; i++)
        {
            attrHandlers[i] = GLES20.glGetAttribLocation(program, attrNames[i]);
        }

        unifNames[HANDLER_UNIF_FIX_MAT_INDEX]   = "u_FixMat";
        unifNames[HANDLER_UNIF_TEXTURE_INDEX]   = "u_Texture";


        for (int i = 0; i < unifNames.length; i++)
        {
            uniformHandler[i] = GLES20.glGetUniformLocation(program, unifNames[i]);
        }
    }

    @Override
    public void draw()
    {
        GLES20.glUseProgram(program);

        GLES20.glViewport
        (
                (int) (params.getStartWeightX() * w + 0.5f),
                (int) (params.getStartWeightY() * h + 0.5f),
                (int) ((params.getEndWeightX() - params.getStartWeightX()) * w + 0.5f),
                (int) ((params.getEndWeightY() - params.getStartWeightY()) * h + 0.5f)
        );
        //
        GLES20.glUniformMatrix3fv(unifHandlers[HANDLER_UNIF_FIX_MAT_INDEX], 1, false, basicTransformMatrix, 0);

        for(int i = 0; i < icons.size(); i++)
        {
            icons.elementAt(i).drawSomeSelf(attrHandlers[HANDLER_ATTR_VER_POS_INDEX], attrHandlers[HANDLER_ATTR_TEX_COORD_INDEX], unifHandlers[HANDLER_UNIF_TEXTURE_INDEX]);
        }
        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    @Override
    public String getRendererName()
    {
        return this.getClass().getName();
    }

    @Override
    public void movedAction(float lastX, float lastY, float x, float y)
    {

    }

    @Override
    public void clickedAction(float x, float y)
    {

    }
}
