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

	public static SignatureType StringToType(String s)
	{
		if(s == null || s.length() < 0) return SignatureType.NO_TYPE;
		else if(s.equals("home")) return SignatureType.HOME;
		else if(s.equals("work")) return SignatureType.WORK;
		else if(s.equals("family")) return SignatureType.FAMILY;
		else if(s.equals("social")) return SignatureType.SOCIAL;
		else if(s.equals("shoping")) return SignatureType.SHOPING;
		else if(s.equals("medical")) return SignatureType.MEDICAL;
		else if(s.equals("food")) return SignatureType.FOOD;
		else if(s.equals("accommodation")) return SignatureType.ACCOMMODATION;
		else if(s.equals("coffe")) return SignatureType.COFFE;
		else if(s.equals("cinema")) return SignatureType.CINEMA;
		else if(s.equals("theatre")) return SignatureType.THEATRE;
		else if(s.equals("smoking")) return SignatureType.SMOKING;
		else if(s.equals("park")) return SignatureType.PARK;
		else if(s.equals("school")) return SignatureType.SCHOOL;
		else if(s.equals("meeting")) return SignatureType.MEETING;
		else if(s.equals("sport")) return SignatureType.SPORT;
		else if(s.equals("picnic")) return SignatureType.PICNIC;

		// TODO in default new unknow type we need find some solution
		return SignatureType.MEETING;
	}
	
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
