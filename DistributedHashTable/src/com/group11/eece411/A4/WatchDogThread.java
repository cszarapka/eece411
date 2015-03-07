package com.group11.eece411.A4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class WatchDogThread extends Thread {
	
	private final SuccessorList successors;
	private final byte[] data;
	private final InetAddress senderAddress;
	private byte[] uniqueId;
	
	public WatchDogThread(SuccessorList successors, byte[] data,
			InetAddress senderAddress) {
		this.successors = successors;
		this.data = data;
		this.senderAddress = senderAddress;
		this.uniqueId = MessageFormatter.getUniqueID(data);
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
			// send returnSuccessors on port 4003
			
			try {
				// Create a get-successors request (command: 34)
				byte[] message = MessageFormatter.createRequest(34, null, null);
				DatagramSocket serverSocket = new DatagramSocket();
				for (InetAddress address : )
				DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
				serverSocket.send(sendPacket);
			} catch (Exception e) {
				
			}
			// listen on 4008 for reply
			
		}
	}
	
	private void returnSuccessors() {
		try {
			byte[] message = MessageFormatter.createResponse(uniqueId, 34, null);
			int length = message.length;
			message = Arrays.copyOf(message,  length + 15);
			synchronized (successors) {
				byte[] address1 = InetAddress.getByName(successors.getSuccessor(0).getIP()).getAddress();
				byte[] address2 = InetAddress.getByName(successors.getSuccessor(1).getIP()).getAddress();
				byte[] address3 = InetAddress.getByName(successors.getSuccessor(2).getIP()).getAddress();
				
				for(int i = 0; i < 4; i++) {
					message[length - 1 + i] = address1[i];
				}
				message[length + 5] = (byte)successors.getSuccessor(0).getNodeNum();
				for(int i = 0; i < 4; i++) {
					message[length + i + 5] = address2[i];
				}
				message[length + 10] = (byte)successors.getSuccessor(1).getNodeNum();
				for(int i = 0; i < 4; i++) {
					message[length + i + 10] = address3[i];
				}
				message[length + 15] = (byte)successors.getSuccessor(2).getNodeNum();
			}
			DatagramSocket serverSocket = new DatagramSocket();
			DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
			serverSocket.send(sendPacket);
		} catch (Exception e) {
			//fuck it
			System.out.println("couldn't find address");
			return;
		}
	}
}
