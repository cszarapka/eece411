package com.group11.eece411.A4;

public class WatchDogThread extends Thread {
	
	public WatchDogThread() {
		
	}
	
	@Override
	public void run() {
		// wait some amount of time
		// lock successors list
			// check status of successors
				// if alive
					// great, do nothing
				// if dead
					// delete it from successor list
	}
}
