package com.lesswalk.pagescarussel;

import com.lesswalk.bases.ILesswalkService;
import com.lesswalk.bases.RectObject3D;

import java.io.File;
import java.util.Vector;

public abstract class ICarusselMainItem 
{

	/*
	 * Global Functions
	 */
	
	public void clearScreen()
	{
		RectObject3D.clear();
	}

	public void setPerspectiveMatrix(float[] perspectiveMat) 
	{
		RectObject3D.setPerspectiveMatrix(perspectiveMat);
	}
	
	/*
	 * 
	 */
	
	public void loadJSON(String dir){}
	
	/*
	 * Must use Functions
	 */
	
	public abstract void fillContainerByItems(Vector<CarusselPageInterface> container);


	/**
	 *
	 * @return uuid
	 * @param signature
	 * @param s
	 */
	public String save(File signature, String s){return null;}

	public void setService(ILesswalkService service){}
}
