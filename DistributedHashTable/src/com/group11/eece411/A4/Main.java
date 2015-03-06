package com.group11.eece411.A4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

	private static final boolean VERBOSE = false;




	public static void main(String [] args) throws IOException {
		joinTable();
		DatagramSocket serverSocket = new DatagramSocket(4003);
		byte[] receiveData = new byte[15500];
        byte[] sendData = new byte[15500];
        while(true)
        {
           DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           serverSocket.receive(receivePacket);
           if(VERBOSE) {
	           String sentence = new String( receivePacket.getData());
	           System.out.println("RECEIVED: " + sentence);
           }
        }
	}
	
	
	
	
	private static void joinTable() {
		
	}
	
}
