package com.lesswalk;

public class SyncThread
{
	public SyncThread(MainService parent) 
	{
	}

	// TODO update databases
	// TODO update 
	
	public void start() 
	{
		// Start threads:
		Thread syncThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
		syncThread.start();
	}
}
