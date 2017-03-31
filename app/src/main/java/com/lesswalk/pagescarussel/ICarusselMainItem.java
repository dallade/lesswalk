package com.lesswalk.pagescarussel;

import com.lesswalk.bases.RectObject3D;

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
	
	abstract public void fillContainerByItems(Vector<CarusselPageInterface> container);
}
