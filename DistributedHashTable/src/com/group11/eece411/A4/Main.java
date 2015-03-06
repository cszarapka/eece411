package com.group11.eece411.A4;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

	public static byte[][][] uniqueIdList;

	//TOODOO: BYTE IS SIGNED SO THAT MIGHT FUCK SHIT UP

	private static final boolean VERBOSE = true;
	public static final int NUMBER_OF_NODES = 100;
	public static final int TIMEOUT = 2000;
	public static int NODE_NUM;
	public static int UPPER_RANGE;
	private final static ConcurrentHashMap<byte[], byte[]> db = new ConcurrentHashMap<byte[], byte[]>();
	public static SuccessorList successors = new SuccessorList();

	public static void main(String [] args) throws IOException {
		
		//initialize successors to ip 0.0.0.0 and node number -1
		Node node1 = new Node("0.0.0.0", -1);
		Node node2 = new Node("0.0.0.0", -1);
		Node node3 = new Node("0.0.0.0", -1);
		successors.addSuccessor(node1, 1);
		successors.addSuccessor(node2, 2);
		successors.addSuccessor(node3, 3);
		
		//set up table
		joinTable();
		
		
		DatagramSocket serverSocket = new DatagramSocket(4003);
		byte[] receiveData = new byte[15500];
		byte[] sendData = new byte[15500];
		InetAddress ip;
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			if(VERBOSE) {
				String sentence = new String( receivePacket.getData());
				System.out.println("RECEIVED: " + sentence);
			}
			sendData = receivePacket.getData();
			ip = receivePacket.getAddress();
			new ServerResponseThread(sendData, ip, db, NODE_NUM, UPPER_RANGE, uniqueIdList).start();

		}
	}




	@SuppressWarnings("deprecation")
	private static void joinTable() throws IOException {
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

		
		
		DatagramSocket clientSocket;
		
		try {
			clientSocket = new DatagramSocket(4003);
		
		
		
		InetAddress IPAddress;
		try {
			IPAddress = InetAddress.getByName(nodeList[addressToTry]);
		
		DatagramPacket receivePacket;
		byte[] receiveData = new byte[16000];
		byte[] sendData = new byte[16000];
		while(true) {
			if(addressToTry++ >= NUMBER_OF_NODES) {
				addressToTry = 0;
			}
			
			byte[] uniqueID = MessageFormatter.generateUniqueID();
			for(int i = 0; i < 16; i++) {
				sendData[i] = uniqueID[i];
			}
			sendData[17] = (byte)0x21;	
			//Send the join table request
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4003);
			clientSocket.send(sendPacket);
			
			clientSocket.setSoTimeout(TIMEOUT);
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				//Node's living
				clientSocket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException e) {
				//Node's dead
			}
			
			
		}
		
		
		//get the node number byte
		byte nodeNum = receiveData[16];
		NODE_NUM = nodeNum;
		
		if(receivePacket.getLength() >= 21){
			//get first IP and node num and make new node object
			byte[] successorIP1 = Arrays.copyOfRange(receiveData, 17, 21);
			String successorIP1str = getIpAddress(successorIP1);
			int successorNum1 = receiveData[21];
			Node successor1 = new Node(successorIP1str, successorNum1);
			UPPER_RANGE = successorNum1;
			successors.addSuccessor(successor1, 1);
		}
		
		if(receivePacket.getLength() >= 26){
			//get second IP and node num and make new node object
			byte[] successorIP2 = Arrays.copyOfRange(receiveData, 22, 26);
			String successorIP2str = getIpAddress(successorIP2);
			int successorNum2 = receiveData[26];
			Node successor2 = new Node(successorIP2str, successorNum2);
			successors.addSuccessor(successor2, 2);
		}
		
		if(receivePacket.getLength() >= 31){
			//get third IP and node num and make new node object
			byte[] successorIP3 = Arrays.copyOfRange(receiveData, 27, 31);
			String successorIP3str = getIpAddress(successorIP3);
			int successorNum3 = receiveData[31];
			Node successor3 = new Node(successorIP3str, successorNum3);
			successors.addSuccessor(successor3, 3);
		}
		
		//find the total file list size of the predecessor
		byte[] fileListLength = Arrays.copyOfRange(receiveData,32,36);
		ByteBuffer wrapped = ByteBuffer.wrap(fileListLength);
		short length = wrapped.getShort();
		
		
		byte[] receiveData2 = new byte[16000];
		//send a request for each file that this node covers
		
		for(int i=0; i < length; i++){
			byte[] key = Arrays.copyOfRange(receiveData, 36+(32*i), 68+(32*i));
			try{
				MessageDigest md = MessageDigest.getInstance("MD5");
				int keyHash = md.digest(key, 0, 2);
				
				// if the hash of the file is in range
				if(keyHash >= NODE_NUM && keyHash < successors.getSuccessor(1).getNodeNum()){
					sendData = MessageFormatter.createRequest(2, key, null);
					
					InetAddress ip = InetAddress.getByName(successors.getSuccessor(1).getIP());
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 4003);
					clientSocket.send(sendPacket);
					
					clientSocket.setSoTimeout(TIMEOUT);
					receivePacket = new DatagramPacket(receiveData2, receiveData2.length);
					try {
						//Node's living
						clientSocket.receive(receivePacket);
						//check that both unique id's are the same
						if(Arrays.equals(MessageFormatter.getUniqueID(sendData),
								MessageFormatter.getUniqueID(receiveData2)) ){
							int cmd = MessageFormatter.getCommand(receiveData2);
							
							//success
							if(cmd == 0){
								byte[] data = MessageFormatter.getValueResponse(receiveData2);
								db.put(key, data);
							}
						}
						
					} catch (SocketTimeoutException e) {
						//Node's dead TODO handle this case, shit just went down
						// 					- kill process, start again another day
					}
				}
			} catch (Exception e){
				
			}
			
		}
		clientSocket.close();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        String ipAddress = "";
        for (byte raw : rawBytes)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }
 
        return ipAddress;
    }
}
