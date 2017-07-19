package com.lesswalk;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lesswalk.bases.BaseActivity;
import com.lesswalk.database.AWS;
import com.lesswalk.views.SettingsNavigationItem;

import java.util.Properties;

public class LesswalkSettings extends BaseActivity
{
    private Button deleteAccountButton                   = null;
    private int navigationItemsIds[]                     =
    {
        R.id.settings_item_waze,
        R.id.settings_item_google_map,
        R.id.settings_item_igo,
        R.id.settings_item_tomtom,
        R.id.settings_item_navigon,
    };

    private SettingsNavigationItem navigationItems[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesswalk_settings);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account);
        deleteAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                view.setClickable(false);
                getService().deleteUserAccount(new AWS.OnRequestListener()
                {
                    @Override
                    public void onStarted() {}

                    @Override
                    public void onFinished()
                    {
                        finish();
                    }

                    @Override
                    public void onError(int errorId)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(LesswalkSettings.this, "Deleting account...", Toast.LENGTH_SHORT).show();
                                view.setClickable(true);
                            }
                        });
                    }
                });
            }
        });
    }

    private void initNavigationItems()
    {
        navigationItems = new SettingsNavigationItem[navigationItemsIds.length];

        for(int i = 0; i < navigationItems.length; i++)
        {
            navigationItems[i] = (SettingsNavigationItem) findViewById(navigationItemsIds[i]);
            navigationItems[i].setIfInstalled(checkIfAppInstalled(navigationItems[i].getPackageName()));
            navigationItems[i].setOnChooseCallback(new SettingsNavigationItem.OnChooseCallback()
            {
                @Override
                public void onChoose(SettingsNavigationItem item)
                {
                    getService().getSettingsProps().setProperty(PROP_NAVIGATION_APP_PACKAGE, item.getPackageName());
                    getService().saveSettingsProps();
                    setNavigationApp(item.getPackageName());
                }
            });
        }

        if(getService().getSettingsProps().getProperty(PROP_NAVIGATION_APP_PACKAGE) == null)
        {
            for (int i = 0; i < navigationItems.length; i++)
            {
                if(navigationItems[i].isEnabled())
                {
                    getService().getSettingsProps().setProperty(PROP_NAVIGATION_APP_PACKAGE, navigationItems[i].getPackageName());
                    getService().saveSettingsProps();
                    break;
                }
            }
        }

        setNavigationApp(getService().getSettingsProps().getProperty(PROP_NAVIGATION_APP_PACKAGE));
    }


    @Override
    protected void mainServiceConnected()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                initNavigationItems();
            }
        });
    }

    private boolean checkIfAppInstalled(String uri)
    {
        PackageManager pm = getPackageManager();
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
        }

        return false;
    }

    public void setNavigationApp(String navigationApp)
    {
        for (int i = 0; i < navigationItems.length; i++)
        {
            navigationItems[i].setChoosed(navigationItems[i].getPackageName().equals(navigationApp));
        }
    }
}
