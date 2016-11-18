package com.lesswalk;

import com.lesswalk.pagescarussel.CarusselMainItem;
import com.lesswalk.pagescarussel.CarusselSurface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PlayerActivity extends Activity 
{
	private CarusselSurface  carusselGlSurfaceBase = null;
	private CarusselMainItem carusselMainItem      = null;
    private Context context;

    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
        context = this;
		//
		initButtons();
        initSurface();
	}
	
    private void initSurface()
    {
        RelativeLayout surfaceScreen = null;

        surfaceScreen = (RelativeLayout) findViewById(R.id.player_gl_screen);

        carusselGlSurfaceBase = new CarusselSurface(this);
        carusselGlSurfaceBase.initiation();

        surfaceScreen.addView(carusselGlSurfaceBase);
        //
//        mapView = new MapView(this, "AIzaSyBrSg5cJyTE-HMNEEOASuqbVn8O0nThfkc");
//        surfaceScreen.addView(mapView, new RelativeLayout.LayoutParams(300, 300));
    }

    private void initButtons()
    {
        Button backButton    = null;
        Button callButton = null;
        //
        backButton = (Button) findViewById(R.id.player_back_button);
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        callButton = (Button) findViewById(R.id.player_call_button);
        callButton.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View view) 
            {
                Toast.makeText(PlayerActivity.this, "under develop TODO - back to 'CALL' action", Toast.LENGTH_SHORT).show();
            }
        });

        Button homeBtn = (Button) findViewById(R.id.player_my_home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MoreViewsActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    	//
    	loadCarusselItems("054-4952127");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        Log.d("elazarkin", "Activity - onWindowFocusChanged " + hasFocus);
    }

    private void loadCarusselItems(String number)
    {
        carusselMainItem = new CarusselMainItem(this);
        //
        carusselMainItem.loadJSON(number);
        //
        carusselGlSurfaceBase.addCarusselMainItem(carusselMainItem);
    }
    
    @Override
    protected void onPause() 
    {
    	carusselGlSurfaceBase.removeCarusselItems();
        carusselMainItem = null;
        //
    	super.onPause();
    	//
//    	System.exit(0);
    }
}
