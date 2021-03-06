package com.lesswalk;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lesswalk.bases.BaseCarusselActivity;
import com.lesswalk.database.AWS;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.player_pages.CarusselPlayerMainItem;

public class PlayerActivity extends BaseCarusselActivity
{
    private static final String TAG            = PlayerActivity.class.getSimpleName();
    private ICarusselMainItem carusselMainItem = null;
    private String signatureKey = "";

    private void loadCarusselItems(String dir)
    {
        carusselMainItem = new CarusselPlayerMainItem(this);
        //
        carusselMainItem.loadJSON(dir);
        //
        getCarusselSurface().addCarusselMainItem(carusselMainItem);
        //
        //TODO elazar move the next few lines later to the designated place
//        Button deleteButton = new Button(this);
//        deleteButton.setText("Delete");
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ((LinearLayout)findViewById(R.id.player_layout)).addView(deleteButton, 0, lp);
//        Log.d(TAG, "signatureKey: "+signatureKey);
//        deleteButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view) {
//                getService().deleteSignature(signatureKey, new AWS.OnRequestListener()
//                {
//                    @Override
//                    public void onStarted() {
//                        runOnUiThread(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                Toast.makeText(PlayerActivity.this, "Deleting signature...", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        Log.d(TAG, "Deleting signature... ("+signatureKey+")");
//                    }
//
//                    @Override
//                    public void onFinished()
//                    {
//                        finish();
//                    }
//
//                    @Override
//                    public void onError(int errorId)
//                    {
//                        runOnUiThread(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                Toast.makeText(PlayerActivity.this, "Failed to delete signature!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        Log.d(TAG, "Failed to delete the signature! ("+signatureKey+")");
//                    }
//                });
//            }
//        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        getCarusselSurface().removeCarusselItems();
        carusselMainItem = null;
        super.onDestroy();
    }

    @Override
    public void onLoadCarusselItems()
    {
        signatureKey = getIntent().getExtras().getString(INTENT_EXTRA_NAME_SIGNATURE_UUID);
        String content_dir = getIntent().getExtras().getString(INTENT_EXTRA_NAME_CONTENT_DIR);
        if(content_dir != null && content_dir.length() > 0)
        {
            loadCarusselItems(content_dir);
        }
    }

    @Override
    public int getContentView()
    {
        return R.layout.player_activity;
    }

    @Override
    protected void mainServiceConnected()
    {
        // TODO Auto-generated method stub

    }
}
