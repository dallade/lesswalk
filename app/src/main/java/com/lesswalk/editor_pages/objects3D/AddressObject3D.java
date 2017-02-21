package com.lesswalk.editor_pages.objects3D;

import com.lesswalk.pagescarussel.EditTextImageObject;

public class AddressObject3D extends EditTextImageObject 
{
	private static final String empty_text = "Locate on a Map or type an Exact Address";
	
	private String country     = "Israel";
	private String city        = null;
	private String street      = null;
	private String street_num  = null;
	
	public AddressObject3D() 
	{
		super("");
	}

	public void setAddress(String contry, String city, String street, String street_num)
	{
		this.country    = (contry == null || contry.length() <= 0) ? null:contry;
		this.city       = (city == null || city.length() <= 0) ? null:city;
		this.street     = (street == null || street.length() <= 0) ? null:street;
		this.street_num = (street_num == null || street_num.length() <= 0) ? null:street_num;
		
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
		
		ans += (street == null || street.length() <= 0 ? "":street);
		
		if(ans.length() > 0)
		{
			ans += (street_num == null || street_num.length() <= 0 ? "":" " + street_num);
		}
		
		if(ans.length() > 0 && city != null && city.length() > 0) ans += " Street, ";
		
		if(city != null && city.length() > 0) ans += city + " (City)";
		
		return ans;
	}
	
	public String getCountry() {return country;}
	public String getCity() {return city;}
	public String getStreet() {return street;}
	public String getStreet_num() {return street_num;}
}
