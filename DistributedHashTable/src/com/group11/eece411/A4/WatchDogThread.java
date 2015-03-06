package com.group11.eece411.A4;

public class WatchDogThread extends Thread {
	
	private final SuccessorList successors;
	
	public WatchDogThread(SuccessorList successors) {
		this.successors = successors;
	}
	
	@Override
	public void run() {
		// wait some amount of time
		// lock successors list
			// check status of successors
				// if 1st is alive
					// check that his last 2 successors are your first 2
					// if they aren't, make his first 2 your last 2
				// if 1st dead
					// delete it, send it shutdown, move on
		
				// if others alive
					// do nothing
				// if others dead
					//
		// Pause for 10 seconds
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// It woke up
		}
		
		// lock the successors list
		synchronized(successors) {
			// 
			// send returnSuccessors on 4003
			// listen on 4008 for reply
			
			
			// TESTS - what they will be testing for
			// edge cases
				// startup, node enters 
			// correctness
				// dump tests
					// insert data, I want to get it back
				// 2 streams
					// when there is a node failure
						// they will verify the shutdown command
						// put lots of data, should get most of it back
					// static
						// when no joins or failures
			// performance
				// are the keys being spread evenly
					// trying a wide range of keys
		}
	}
}
