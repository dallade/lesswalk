package com.lesswalk;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.views.RoundedNegativeIconButtonWithText;
import com.lesswalk.views.SelectSpotTypeAcceptDialog;
import com.lesswalk.views.SelectSpotTypeItem;

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
            protected void donePressed(Editable text, Drawable icon)
            {
                if(text != null && text.length() > 0)
                {
                    Intent intent = new Intent(SelectSpotTypeActivity.this, EditorActivity.class);

                    intent.putExtra(INTENT_EXTRA_NAME_SPOT_NAME, text);
                    startActivity(intent);
                }
            }
        };
    }

    private void addSpotTypes()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SelectSpotTypeItem list = (SelectSpotTypeItem) findViewById(R.id.select_spot_activity_list_item);

                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Home", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Work", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Family", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Social", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Shopping", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Medical", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Food", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Accomodation", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Coffe", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Cinema", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Theatre", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Concert", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Smoking", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Park", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "School", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Meeting", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Sport", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Picnic", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Pub", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Party", Color.WHITE));
                list.addRoundedNegativeIconButtonWithText(new RoundedNegativeIconButtonWithText(SelectSpotTypeActivity.this, R.drawable.home_icon, "Restaurant", Color.WHITE));

                for(int i = 0; i < list.getSelectSpotTypeChildCount(); i++)
                {
                    list.getSelectSpotTypeChildAt(i).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            RoundedNegativeIconButtonWithText button = (RoundedNegativeIconButtonWithText) view;

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

    @Override
    protected void mainServiceConnected()
    {

    }
}
