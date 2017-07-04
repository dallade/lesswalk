package com.lesswalk.editor_pages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.lesswalk.R;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;
import com.lesswalk.database.AWS;
import com.lesswalk.editor_pages.objects3D.AddressObject3D;
import com.lesswalk.editor_pages.objects3D.EditorImageTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorTextTipObject3D;
import com.lesswalk.editor_pages.objects3D.EditorVideoTipObject3D;
import com.lesswalk.json.CarruselJson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ParkingEditPage extends EditParkingBasePage
{
    private ImageObject3D PARKING_ICON  = null;
    private ImageObject3D PARKING_TITLE = null;

	public ParkingEditPage(String title, Context context)
	{
		super(title, context);
	}

	@Override
	protected ImageObject3D getIcon() 
	{
    	// TODO check if it not mistake to use static Icon
        if(PARKING_ICON == null)
        {
        	Bitmap  icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.parking_icon_3x).copy(Bitmap.Config.ARGB_8888, true);
        	ImageObject3D.fixIconByColor(icon, Color.argb(255, 255, 176, 16));
        	//
            PARKING_ICON = new ImageObject3D("PARKING_ICON");
            PARKING_ICON.generateTextureID(icon);
            //
            icon.recycle();
        }
    	
        return PARKING_ICON;
	}

	@Override
	protected String getPageTitle() 
	{
		return "Set Parking Splot";
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
	}
	
	@Override
	protected RectObject3D createTitle(float aspect) 
	{
		if(PARKING_TITLE == null)
		{
			PARKING_TITLE = createTitleObj(getPageTitle(), aspect);
		}
		
		return PARKING_TITLE;
	}

	@Override
	protected int getEmptyMapThumnailResourceId() 
	{
		return R.drawable.parking_address_edit_card_icon_1x;
	}

	@Override
	public void save(File dir, int index, CarruselJson carruselJson)
	{
		AddressObject3D        addressObject  = getAddressObject();
		EditorTextTipObject3D  textTipObject  = getTextTipObject();
		EditorImageTipObject3D imageTipObject = getImageTipObject();
		//EditorVideoTipObject3D videoTipObject = getVideoTipObject();

		if(addressObject != null)
		{
			carruselJson.getParkings()[index].setMap_address(addressObject.getMapAddress());
		}

		if(textTipObject != null)
		{
			carruselJson.getParkings()[index].setTips(textTipObject.getTipText());
		}

		if(imageTipObject != null && imageTipObject.getImage() != null)
		{
			CarruselJson.Image image = new CarruselJson.Image();
			image.setName(AWS.generateKey() + ".png");
			try
			{
				imageTipObject.getImage().compress
				(
						Bitmap.CompressFormat.PNG,
						100,
						new FileOutputStream(new File(dir, image.getName()))
				);

				carruselJson.getParkings()[index].setImage(image);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
