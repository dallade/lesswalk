package com.lesswalk.editor_pages;

import android.content.Context;
import android.util.Log;

import com.lesswalk.pagescarussel.CarusselPageInterface;
import com.lesswalk.pagescarussel.ICarusselMainItem;

import java.util.Vector;

public class CarusselEditorMainItem extends ICarusselMainItem 
{
//	private static final int STATE_HELP_INIT    = 0;
//	private static final int STATE_EDIT_PROCCES = 1;
//	private int state;
	
	private Vector<CarusselPageInterface> pages = null;
	
	private Vector<CarusselPageInterface> parent_container = null;
	
	public CarusselEditorMainItem(Context c) 
	{
		initOnStartPages(c);
	}
	
	private void initOnStartPages(Context c) 
	{
		if(pages == null) pages = new Vector<CarusselPageInterface>();
		
		pages.add(new GeneralEditPage("generalEditPage", c));
		pages.add(new ParkingEditPage("parkingEditPage", c));
		pages.add(new IndoorEditPage("indoorEditPage", c));
		
		for(int i = 0; i < pages.size(); i++)
		{
			pages.elementAt(i).setIndex(i);
		}
	}

	@Override
	public void fillContainerByItems(Vector<CarusselPageInterface> container) 
	{
		Log.d("elazarkin", "CarusselEditorMainItem - fillContainerByItems");
		
		parent_container = container;
		//
		if(pages != null && pages.size() > 0)
		{
			for(CarusselPageInterface p:pages)
			{
				Log.d("elazarkin", "CarusselEditorMainItem - fillContainerByItems add " + p.toString());
				container.add(p);
			}
		}
	}
}
