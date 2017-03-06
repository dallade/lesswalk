package com.lesswalk;

import com.lesswalk.bases.BaseCarusselActivity;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.player_pages.CarusselPlayerMainItem;

public class PlayerActivity extends BaseCarusselActivity
{
    private ICarusselMainItem carusselMainItem = null;

    private void loadCarusselItems(String dir)
    {
        carusselMainItem = new CarusselPlayerMainItem(this);
        //
        carusselMainItem.loadJSON(dir);
        //
        getCarusselSurface().addCarusselMainItem(carusselMainItem);
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
        String contect_dir = getIntent().getExtras().getString("content_dir");
        if(contect_dir != null && contect_dir.length() > 0)
        {
            loadCarusselItems(contect_dir);
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
