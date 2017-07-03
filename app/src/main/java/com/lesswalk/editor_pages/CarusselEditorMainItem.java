package com.lesswalk.editor_pages;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lesswalk.bases.ILesswalkService;
import com.lesswalk.json.CarruselJson;
import com.lesswalk.pagescarussel.CarusselPageInterface;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.utils.Utils;

import java.io.File;
import java.util.Vector;

public class CarusselEditorMainItem extends ICarusselMainItem
{
	private CarruselJson                  carruselJson     = null;
	private GeneralEditPage               generalEditPage  = null;
	private Vector<ParkingEditPage>       parkingEditPages = null;
	private IndoorEditPage                indoorEditPage   = null;
	private Vector<CarusselPageInterface> pages            = null;
	private ILesswalkService              service          = null;
	private Context                       context          = null;
	
	public CarusselEditorMainItem(Context c)
	{
		context = c;

		initOnStartPages(context);
		// TODO check intent, maybe it edit of existed signature
	}

	private void initOnStartPages(Context c)
	{
		if(pages == null) pages = new Vector<CarusselPageInterface>();
		
		pages.add((generalEditPage=new GeneralEditPage("generalEditPage", c)));
		addParkingEditPage(pages, new ParkingEditPage("parkingEditPage", c));
		pages.add((indoorEditPage=new IndoorEditPage("indoorEditPage", c)));
		
		for(int i = 0; i < pages.size(); i++)
		{
			pages.elementAt(i).setIndex(i);
		}
	}

	private void addParkingEditPage(Vector<CarusselPageInterface> pages, ParkingEditPage parkingEditPage)
	{
		if(parkingEditPages==null) parkingEditPages = new Vector<ParkingEditPage>();
		parkingEditPages.add(parkingEditPage);
		pages.add(parkingEditPages.lastElement());
	}

	@Override
	public void fillContainerByItems(Vector<CarusselPageInterface> container) 
	{
		Log.d("elazarkin", "CarusselEditorMainItem - fillContainerByItems");
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

	@Override
	public String save()
	{
		if(service == null)
		{
			((Activity)context).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(context, "Fail save, please try again later!", Toast.LENGTH_LONG).show();
				}
			});
		}

		if(carruselJson == null) carruselJson = new CarruselJson();
		if(carruselJson.getKey() == null) carruselJson.setKey(service.generateUUID());

		if(pages != null)
		{
			CarruselJson jsonObject = new CarruselJson();
			File         dir        = new File(context.getCacheDir(), "signature");

			if(dir.exists())
			{
				Utils.removeAllInDir(dir);
			}
			else
			{
				dir.mkdirs();dir.mkdir();
			}

			generalEditPage.save(dir, jsonObject);
			jsonObject.setParkingsAmmount(parkingEditPages.size());
			for(int i = 0; i < parkingEditPages.size(); i++)
			{
				parkingEditPages.elementAt(i).save(dir, i, jsonObject);
			}
			indoorEditPage.save(dir, jsonObject);
		}

		return carruselJson.getKey();
	}

	@Override
	public void setService(ILesswalkService service)
	{
		this.service = service;
	}
}