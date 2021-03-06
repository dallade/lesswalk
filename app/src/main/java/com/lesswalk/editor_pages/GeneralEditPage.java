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

public class GeneralEditPage extends EditParkingBasePage 
{
	private ImageObject3D GENERAL_ICON  = null;
	private ImageObject3D GENERAL_TITLE = null;

	public GeneralEditPage(String title, Context context)
	{
		super(title, context);
	}

	@Override
	protected ImageObject3D getIcon() 
	{
        if(GENERAL_ICON == null)
        {
        	Bitmap  icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.form_address_icon_2x).copy(Bitmap.Config.ARGB_8888, true);
        	ImageObject3D.fixIconByColor(icon, Color.argb(255, 255, 176, 16));
        	//
            GENERAL_ICON = new ImageObject3D("Icon");
            GENERAL_ICON.generateTextureID(icon);
            //
            icon.recycle();
        }
    	
        return GENERAL_ICON; 
	}

	@Override
	protected String getPageTitle() 
	{
		return "Set your Address";
	}

	@Override
	protected void addChilds(RectObject3D drawableArea) 
	{
		super.addChilds(drawableArea);
	}

	@Override
	protected RectObject3D createTitle(float aspect) 
	{
		if(GENERAL_TITLE == null)
		{
			GENERAL_TITLE = createTitleObj(getPageTitle(), aspect);
		}
		
		return GENERAL_TITLE;
	}

	@Override
	protected int getEmptyMapThumnailResourceId() 
	{
		return R.drawable.address_edit_card_icon;
	}

	@Override
	public void save(File dir, CarruselJson carruselJson)
	{
		AddressObject3D        addressObject  = getAddressObject();
		EditorTextTipObject3D  textTipObject  = getTextTipObject();
		EditorImageTipObject3D imageTipObject = getImageTipObject();
		EditorVideoTipObject3D videoTipObject = getVideoTipObject();

		if(addressObject != null)
		{
			carruselJson.setMap_address(addressObject.getMapAddress());
		}

		if(textTipObject != null)
		{
			carruselJson.setTips(textTipObject.getTipText());
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

				carruselJson.setImage(image);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
