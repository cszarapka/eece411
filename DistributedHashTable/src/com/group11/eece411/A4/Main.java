package com.group11.eece411.A4;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Main {

	private static final boolean VERBOSE = true;
	public static final int NUMBER_OF_NODES = 100;
	public static final int TIMEOUT = 2000;

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
			new ServerResponseThread(receivePacket).start();

		}
	}




	private static void joinTable() {
		//function for joining the table initially
		FileInputStream fin;
		String [] nodeList = new String[NUMBER_OF_NODES];
		try	{
		    // Open an input stream
		    fin = new FileInputStream ("myfile.txt");

		    // Read a line of text
		    DataInputStream din = new DataInputStream(fin);
		    for(int i = 0; i < NUMBER_OF_NODES; i++) {
		    	nodeList[i] = din.readLine();
		    }
		    // Close our input stream
		    fin.close();		
		} catch (IOException e) {
			System.err.println ("Unable to read from file");
			System.exit(-1);
		}
		int addressToTry = (int)(Math.random()*NUMBER_OF_NODES);
		while(true) {
			if(addressToTry++ >= NUMBER_OF_NODES) {
				addressToTry = 0;
			}
			DatagramSocket clientSocket = new DatagramSocket(4003);
			InetAddress IPAddress = InetAddress.getByName(nodeList[addressToTry]);
			byte[] sendData = new request("jointable", "");
			byte[] receiveData = new byte[16000];
			
			//Send the join table request
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4003);
			clientSocket.send(sendPacket);
			
			clientSocket.setSoTimeout(TIMEOUT);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				//Node's living
				clientSocket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException e) {
				//Node's dead
			}
			
			
		}
		clientSocket.close();
			






		


	}

}
