package com.lesswalk;

import com.lesswalk.bases.BaseCarusselActivity;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.player_pages.CarusselPlayerMainItem;

public class PlayerActivity extends BaseCarusselActivity 
{
	private ICarusselMainItem carusselMainItem      = null;
	
    private void loadCarusselItems(String number)
    {
        carusselMainItem = new CarusselPlayerMainItem(this);
        //
        carusselMainItem.loadJSON(number);
        //
        getCarusselSurface().addCarusselMainItem(carusselMainItem);
    }
    
    @Override
    protected void onPause() 
    {
    	getCarusselSurface().removeCarusselItems();
        carusselMainItem = null;
        //
        finish();
        //
    	super.onPause();
    }

	@Override
	public void onLoadCarusselItems() 
	{
		loadCarusselItems("054-4952127");
	}

	@Override
	public int getContentView() 
	{
		return R.layout.player_activity;
	}

	@Override
	protected void mainServiceConnected() {
		// TODO Auto-generated method stub
		
	}
}
