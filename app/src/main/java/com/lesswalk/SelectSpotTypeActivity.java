package com.lesswalk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.json.AssetsJson;
import com.lesswalk.views.RoundedIconButtonWithText;
import com.lesswalk.views.SelectSpotTypeAcceptDialog;
import com.lesswalk.views.SelectSpotTypeItem;

import java.io.File;

public class SelectSpotTypeActivity extends BaseActivity
{
    private SelectSpotTypeAcceptDialog acceptDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_spot_type);

        addSpotTypes();

        setViewsActions();
    }

    private void setViewsActions()
    {
        Button cancel = (Button) findViewById(R.id.select_spot_type_activity_cances_button);

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onBackPressed();
            }
        });

        acceptDialog = new SelectSpotTypeAcceptDialog(this)
        {
            @Override
            protected void donePressed(Editable text, String icon_name, String type)
            {
                if(text != null && text.length() > 0)
                {
                    Intent intent = new Intent(SelectSpotTypeActivity.this, EditorActivity.class);

                    Log.d("elazarkin", "icon_name = " + icon_name);

                    intent.putExtra(INTENT_EXTRA_NAME_SPOT_NAME, type);
                    intent.putExtra(INTENT_EXTRA_NAME_ICON_UUID, icon_name);
                    intent.putExtra(INTENT_EXTRA_NAME_TITLE, text.toString());
                    intent.putExtra(INTENT_EXTRA_SLIDER_INDEX, getIntent().getIntExtra(INTENT_EXTRA_SLIDER_INDEX, 0));
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    private void addSpotTypes()
    {
        final SelectSpotTypeItem list      = (SelectSpotTypeItem) findViewById(R.id.select_spot_activity_list_item);
        final File               assetsDir = new File(this.getFilesDir(), "assets");

        if(assetsDir.exists())
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AssetsJson assets = AssetsJson.createFromFile(new File(assetsDir, "content.json"));

                    for (int i = 0; i < assets.getImages().length; i++)
                    {
                        //if(i == 2) continue;
                        if(!assets.getImages()[i].isHidden())
                        {
                            list.addRoundedIconButtonWithText(new RoundedIconButtonWithText
                            (
                                    SelectSpotTypeActivity.this,
                                    new File(assetsDir, assets.getImages()[i].getName()),
                                    assets.getImages()[i].getTitle(),
                                    assets.getImages()[i].getName(),
                                    Color.WHITE
                            ));
                        }
                    }

                    for (int i = 0; i < list.getSelectSpotTypeChildCount(); i++)
                    {
                        list.getSelectSpotTypeChildAt(i).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                RoundedIconButtonWithText button = (RoundedIconButtonWithText) view;

                                acceptDialog.setText(button.getText());
                                acceptDialog.setIcon(button.getIcon());
                                acceptDialog.show();
                            }
                        });
                    }
                    list.invalidate();
                }
            });
        }
    }

    @Override
    protected void mainServiceConnected()
    {

    }
}
