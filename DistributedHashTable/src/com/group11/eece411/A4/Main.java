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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

	public static byte[][][] uniqueIdList = new byte[1][3][16];

	//TODO: BYTE IS SIGNED SO THAT MIGHT FUCK SHIT UP

	private static final boolean VERBOSE = true;
	public static final int NUMBER_OF_NODES = 5;
	public static final int TIMEOUT = 10000;
	public static int NODE_NUM;
	public static int UPPER_RANGE;
	private final static ConcurrentHashMap<byte[], byte[]> db = new ConcurrentHashMap<byte[], byte[]>();
	public static int i = 0;

	public static void main(String [] args) throws IOException {
		/*
			String host = "planetlab2.cs.ubc.ca";
	      int port = 4003;

	      byte[] message = "Java Source and Support".getBytes();

	      // Get the internet address of the specified host
	      InetAddress address = InetAddress.getByName(host);

	      // Initialize a datagram packet with data and address
	      DatagramPacket packet = new DatagramPacket(message, message.length,
	          address, port);

	      // Create a datagram socket, send the packet through it, close it.
	      DatagramSocket dsocket = new DatagramSocket();
	      dsocket.send(packet);
	      dsocket.close();


		 */

		Node node = new Node(InetAddress.getLocalHost().getHostName());

		//Parse the node list and store it in nodeList
		System.out.println("opening file" + "\n");

		//function for joining the table initially
		FileInputStream fin;
		String [] nodeList = new String[NUMBER_OF_NODES];
		try	{
			// Open an input stream
			fin = new FileInputStream ("smallNodes.txt");

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


		//
		byte[] receiveData = new byte[16000];
		boolean foundDht = false;
		int addressToTry = (int)(Math.random()*NUMBER_OF_NODES);
		DatagramSocket socket = new DatagramSocket(4003);
		DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
		Message message;

		if(args.length == 0) {	

			while(!foundDht) {

				if(++addressToTry == NUMBER_OF_NODES) {
					addressToTry = 0;
				}
				String myName = InetAddress.getLocalHost().getHostName();
				System.out.println("CHECK THIS THING" + myName + nodeList[addressToTry]);
				if(!myName.equals(nodeList[addressToTry])) {
					message = new Message(node.getHostName(), 4003);
					message.buildRequestMessage(Codes.REQUEST_TO_JOIN);
					Node.sendMessage(message, InetAddress.getByName(nodeList[addressToTry]), 4003);	


					socket.setSoTimeout(TIMEOUT);
					packet = new DatagramPacket(receiveData, receiveData.length);
					try {
						//Node's living
						socket.receive(packet);
						foundDht = true;
					} catch (SocketTimeoutException e) {
						//Node's dead
					}
				}
			}
			node.joinTable(new Message(packet.getData()));
			
			Successor newSuccessor = new Successor(nodeList[addressToTry], new Message(packet.getData()).originNodeNumber);
			node.successors.add(newSuccessor);
			System.out.println("THESE ARE MY SUCCESSORS:" + node.successors.get(0).getHostName());
			
			
			
			message = new Message(node.getHostName(), 4003);
			message.buildAddSuccessorMessage(node.nodeNumber);
			Node.sendMessage(message, InetAddress.getByName(nodeList[addressToTry]), 4003);
		} else {

			// set node number
			node.nodeNumber = 0;
			node.successors = new ArrayList<Successor>();
			// what else needs to be done?

		}

		


		while(true)
		{
			System.out.println("waiting to receive");
			try{

				System.out.println("THESE ARE MY SUCCESSORS:" + node.successors.get(0).getHostName());
			} catch (IndexOutOfBoundsException e){
				//welp nothing to print
			}
			
			packet = new DatagramPacket(receiveData, receiveData.length);
			System.out.println("Packet created");
			socket.setSoTimeout(0);
			socket.receive(packet);
			System.out.println("Packet received");
			message = new Message(packet.getData());
			System.out.println("Message: "+message);
			message.parseReceivedRequestMessage();
			System.out.println(packet.getData());
			message.originIP = Node.toObjects(packet.getAddress().getAddress());
			node.respondToMessage(message);
		}


	}










	/*
		System.out.println("let's start: "+ args.length);
		//initialize successors to ip 0.0.0.0 and node number -1

		Node node1 = new Node("0.0.0.0", -1);
		Node node2 = new Node("0.0.0.0", -1);
		Node node3 = new Node("0.0.0.0", -1);
		successors.addSuccessor(node1, 0);
		successors.addSuccessor(node2, 1);
		successors.addSuccessor(node3, 2);


		if(args.length == 0) {
		//set up table
			System.out.println("enter joinTable" + "\n");
			joinTable();
			System.out.println("exit joinTable" + "\n");
		}

		//im a table
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
				System.out.println("FROM: " + receivePacket.getAddress().getHostName());
			}
			sendData = receivePacket.getData();
			if(sendData[16] == (byte)0x35) {
				UPPER_RANGE = (int) sendData[17] & 0xFF;
			}
			ip = receivePacket.getAddress();
			new ServerResponseThread(sendData, ip, db, NODE_NUM, UPPER_RANGE, uniqueIdList, successors).start();


		}
	}*/


	/*

	@SuppressWarnings("deprecation")
	private static void joinTable() throws IOException {
		System.out.println("opening file" + "\n");

		//function for joining the table initially
		FileInputStream fin;
		String [] nodeList = new String[NUMBER_OF_NODES];
		try	{
			// Open an input stream
			fin = new FileInputStream ("node.txt");

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
		System.out.println("file opened" + "\n");
		int addressToTry = (int)(Math.random()*NUMBER_OF_NODES);


		System.out.println("try to contact server" + "\n");
		DatagramSocket clientSocket;

		try {
			clientSocket = new DatagramSocket(4003);



			DatagramPacket receivePacket = null;
			InetAddress IPAddress;
			try {


				byte[] receiveData = new byte[16000];
				byte[] sendData = new byte[16000];
				boolean foundDht = false;
				while(!foundDht) {

					try {

						if(++addressToTry >= NUMBER_OF_NODES) {
							addressToTry = 0;
						}
						IPAddress = InetAddress.getByName(nodeList[addressToTry]);
						System.out.println("number of nodes tried: "+i+++"\n" + "specific node tried: " + IPAddress.getHostName());
						byte[] uniqueID = MessageFormatter.generateUniqueID();
						for(int i = 0; i < 16; i++) {
							sendData[i] = uniqueID[i];
						}
						sendData[16] = (byte)0x21;	
						//Send the join table request
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4003);
						clientSocket.send(sendPacket);

						clientSocket.setSoTimeout(TIMEOUT);
						receivePacket = new DatagramPacket(receiveData, receiveData.length);
						try {
							//Node's living
							clientSocket.receive(receivePacket);
							foundDht = true;
						} catch (SocketTimeoutException e) {
							//Node's dead
						}
					} catch (UnknownHostException e) {
						// ignore this host
						System.out.println("i am stuck");
					}


				}
				System.out.println("sent and received data" + "\n");

				System.out.println("breaking down message received" + "\n");
				//get the node number byte
				byte nodeNum = receiveData[16];
				NODE_NUM = nodeNum;

				if(receivePacket.getLength() >= 21){
					//get first IP and node num and make new node object
					byte[] successorIP1 = Arrays.copyOfRange(receiveData, 17, 21);
					String successorIP1str = getIpAddress(successorIP1);
					int successorNum1 = receiveData[21] & 0xFF;
					Node successor1 = new Node(successorIP1str, successorNum1);
					UPPER_RANGE = successorNum1;
					successors.addSuccessor(successor1, 0);
				}

				if(receivePacket.getLength() >= 26){
					//get second IP and node num and make new node object
					byte[] successorIP2 = Arrays.copyOfRange(receiveData, 22, 26);
					String successorIP2str = getIpAddress(successorIP2);
					int successorNum2 = receiveData[26] & 0xFF;
					Node successor2 = new Node(successorIP2str, successorNum2);
					successors.addSuccessor(successor2, 1);
				}

				if(receivePacket.getLength() >= 31){
					//get third IP and node num and make new node object
					byte[] successorIP3 = Arrays.copyOfRange(receiveData, 27, 31);
					String successorIP3str = getIpAddress(successorIP3);
					int successorNum3 = receiveData[31] & 0xFF;
					Node successor3 = new Node(successorIP3str, successorNum3);
					successors.addSuccessor(successor3, 2);
				}

				//find the total file list size of the predecessor
				byte[] fileListLength = Arrays.copyOfRange(receiveData,32,36);
				ByteBuffer wrapped = ByteBuffer.wrap(fileListLength);
				short length = wrapped.getShort();


				byte[] receiveData2 = new byte[16000];
				//send a request for each file that this node covers

				System.out.println("requesting each file" + "\n");
				for(int i=0; i < length; i++){
					byte[] key = Arrays.copyOfRange(receiveData, 36+(32*i), 68+(32*i));
					try{
						MessageDigest md = MessageDigest.getInstance("MD5");
						int keyHash = md.digest(key, 0, 2);

						// if the hash of the file is in range
						if(keyHash >= NODE_NUM && keyHash < successors.getSuccessor(0).getNodeNum()){
							sendData = MessageFormatter.createRequest(2, key, null);

							InetAddress ip = InetAddress.getByName(successors.getSuccessor(0).getIP());
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
								System.exit(0);
							}
						}
					} catch (Exception e){

					}

				}

				byte[] massage = new byte[18];
				byte[] id = MessageFormatter.generateUniqueID();
				for(int i = 0; i < 16; i++) {
					massage[i] = id[i];
				}
				massage[16] = (byte) 0x35;
				massage[17] = (byte)NODE_NUM;
				InetAddress ip = InetAddress.getByName(successors.getSuccessor(0).getIP());
				DatagramPacket sendPacket = new DatagramPacket(massage, massage.length, ip, 4003);
				clientSocket.send(sendPacket);
				clientSocket.close();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("exiting joinTable" + "\n");
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
	}*/
}
