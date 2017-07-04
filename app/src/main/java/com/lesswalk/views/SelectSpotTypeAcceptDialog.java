package com.lesswalk.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.lesswalk.R;

/**
 * Created by elazarkin on 5/17/17.
 */

public abstract class SelectSpotTypeAcceptDialog extends Dialog
{
    private EditText editText = null;
    private Context  context  = null;
    private String   icon     = null;
    private String   type     = null;

    public SelectSpotTypeAcceptDialog(@NonNull Context context)
    {
        super(context);

        this.context = context;

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        setContentView(R.layout.select_spot_type_accept_dialog);

        setDialogViews();

        editText  = (EditText) findViewById(R.id.select_spot_type_accept_dialog_edit_text);
    }

    private void setDialogViews()
    {
        Button doneBt = (Button) findViewById(R.id.select_spot_type_accept_dialog_done_bt);

        doneBt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                donePressed(editText.getText(), icon, type);
                dismiss();
            }
        });
    }

    protected abstract void donePressed(Editable text, String icon, String type);

    public void setText(final String text)
    {
        type = text;
        ((Activity)context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(text != null) editText.setText(text);
                else editText.setText("");
            }
        });
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }
}
