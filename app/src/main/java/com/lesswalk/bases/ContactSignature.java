package com.lesswalk.bases;

public class ContactSignature 
{
	public static enum SignatureType
	{
		NO_TYPE,
		HOME,
		WORK,
		FAMILY,
		SOCIAL,
		SHOPING,
		MEDICAL,
		FOOD,
		ACCOMMODATION,
		COFFE,
		CINEMA,
		THEATRE,
		CONCERTION,
		SMOKING,
		PARK,
		SCHOOL,
		MEETING,
		SPORT,
		PICNIC
	};
	
	private String           phoneNumber;
	private String           signutarePath;
	private SignatureType    type;
	
	public ContactSignature(String number, SignatureType type, String path) 
	{
		phoneNumber   = "" + number;
		signutarePath = "" + path;
		this.type     = type;
	}
	
	public String getPhoneNumber() 
	{
		return phoneNumber;
	}
	
	public String getSignutarePath() 
	{
		return signutarePath;
	}
	
	public SignatureType getType() 
	{
		return type;
	}
}
