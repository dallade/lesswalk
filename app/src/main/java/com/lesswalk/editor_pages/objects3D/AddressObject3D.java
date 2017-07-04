package com.lesswalk.editor_pages.objects3D;

import com.lesswalk.json.CarruselJson;
import com.lesswalk.pagescarussel.EditTextImageObject;

public class AddressObject3D extends EditTextImageObject 
{
	private static final String empty_text = "Locate on a Map or type an Exact Address";

	CarruselJson.MapAddress mapAddress = null;
	
//	private String country     = "Israel";
//	private String city        = null;
//	private String street      = null;
//	private String street_num  = null;
	
	public AddressObject3D() 
	{
		super("");
	}

	public void setAddress(String contry, String city, String street, String street_num)
	{
		if(mapAddress == null) mapAddress = new CarruselJson.MapAddress();

		mapAddress.setCountry(contry);
		mapAddress.setCity(city);
		if(street != null && street_num != null)
		{
			mapAddress.setStreet(street_num + " " + street);
		}
		else  mapAddress.setStreet(street);

		setRefreshFlag();
	}
	
	@Override
	protected String getText() 
	{
		String text = createText();
		return text == null || text.length() <= 0 ? empty_text:text;
	}

	private String createText() 
	{
		String ans = "";
		
		ans += (mapAddress.getStreet() != null && mapAddress.getStreet().length() > 0 ? mapAddress.getStreet():"");
		
		if(ans.length() > 0 && mapAddress.getCity() != null && mapAddress.getCity().length() > 0) ans += " Street, ";
		
		if(mapAddress.getCity() != null && mapAddress.getCity().length() > 0) ans += mapAddress.getCity() + " (City)";
		
		return ans;
	}
	
	public String getCountry() {return mapAddress.getCountry();}
	public String getCity() {return mapAddress.getCity();}
	public String getStreet() {return mapAddress.getStreet();}

    public CarruselJson.MapAddress getMapAddress()
    {
        return mapAddress;
    }
}
