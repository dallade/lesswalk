package com.lesswalk.bases;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lesswalk.R;
import com.lesswalk.pagescarussel.CarusselSurface;

// release 1.1

public abstract class BaseCarusselActivity extends BaseActivity 
{
	private CarusselSurface carusselGlSurfaceBase = null;
	private RelativeLayout  screen                = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        Log.d("elazarkin2", "BaseCarusselActivity onCreate");

		setContentView(getContentView());
		
		screen = (RelativeLayout) findViewById(R.id.bc_gl_screen);

        initButtons();
        initSurface();
        onLoadCarusselItems();
	}
	
    private void initSurface()
    {
        carusselGlSurfaceBase = new CarusselSurface(this);
        carusselGlSurfaceBase.initiation();
        carusselGlSurfaceBase.setVisiable(true);

        screen.addView(carusselGlSurfaceBase);

//        mapView = new MapView(this, "AIzaSyBrSg5cJyTE-HMNEEOASuqbVn8O0nThfkc");
//        surfaceScreen.addView(mapView, new RelativeLayout.LayoutParams(300, 300));
    }

    private void initButtons()
    {
        Button backButton = null;
        Button callButton = null;
        //
        backButton = (Button) findViewById(R.id.bc_back_button);
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        callButton = (Button) findViewById(R.id.bc_call_button);
        callButton.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) 
            {
                Toast.makeText(BaseCarusselActivity.this, "under develop TODO - back to 'CALL' action", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    protected CarusselSurface getCarusselSurface()
    {
    	return carusselGlSurfaceBase;
    }
    
    protected RelativeLayout getScreen() 
    {
		return screen;
	}
	
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus)
//    {
//        super.onWindowFocusChanged(hasFocus);
//
//        Log.d("elazarkin", "Activity - onWindowFocusChanged " + hasFocus);
//    }
	
    public abstract void onLoadCarusselItems();
    
    public abstract int getContentView();
}
