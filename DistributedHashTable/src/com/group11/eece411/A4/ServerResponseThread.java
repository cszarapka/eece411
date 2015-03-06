package com.group11.eece411.A4;

import java.net.DatagramPacket;

public class ServerResponseThread extends Thread {

	private DatagramPacket requestPacket;
	
	public ServerResponseThread(DatagramPacket d) {
		requestPacket = d;
	}
	
	@Override
	public void run() {
		
		
	}

}
