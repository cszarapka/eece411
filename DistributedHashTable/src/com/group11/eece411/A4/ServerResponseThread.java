package com.group11.eece411.A4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class ServerResponseThread extends Thread {
	
	private final ConcurrentHashMap<byte[], byte[]> db;
	private byte[][][] uniqueIdList;

	private MessageDigest md;
	private final byte[] data;
	private final InetAddress senderAddress;
	private final int command;
	private final int nodeNumber;
	private final int upperRange;
	private byte[] key;
	private int keyHash;
	
	private byte[] value;
	private byte[] uniqueId;
	
	public ServerResponseThread(byte[] d, InetAddress i,
			ConcurrentHashMap<byte[], byte[]> db, int nodeNumber,	int upperRange, byte[][][] u) {
		data = d;
		this.db = db;
		this.nodeNumber = nodeNumber;
		this.upperRange = upperRange;
		senderAddress = i;
		uniqueId = MessageFormatter.getUniqueID(data);
		command = MessageFormatter.getCommand(data);
		this.uniqueIdList = u;
		
		md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {	
		InetAddress responseTo;
		synchronized (uniqueIdList) {
			for (int i = 0; i < uniqueIdList.length; i++) {
				if(Arrays.equals(uniqueIdList[i][1], uniqueId)) {
					try {
						responseTo = InetAddress.getByAddress(uniqueIdList[i][2]);
						for(int j = 0; j < 16; j++) {
							data[j] = uniqueIdList[i][0][j];
						}
						DatagramSocket serverSocket = new DatagramSocket();
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, responseTo, 4003);
						serverSocket.send(sendPacket);
					} catch (Exception e) {
						//fuck it
						System.out.println("couldn't find address");
						shutdown();
					}
					uniqueIdList[i] = uniqueIdList[uniqueIdList.length];
					uniqueIdList = Arrays.copyOf(uniqueIdList, uniqueIdList.length - 1);
					return;
				}
			}
		}
		switch (command) {
		case 1:
			put();
			break;
		case 2:
			get();
			break;
		case 3:
			remove();
			break;
		case 4:
			shutdown();
			break;
		case 33: //0x21
			joinTable();
			break;
		}
	}

	private void put() {
		// Get the key and hash it
		key = MessageFormatter.getKey(data);
		try {
			keyHash = md.digest(key, 0, 2);
			// Only perform put if its in our range to screw with
			if (isInRange(keyHash)) {
				db.put(key, MessageFormatter.getValueRequest(data));
				//TODO test what happens when it is full
				//TODO build response with code 0
			} else {
				//TODO send put request to node in successor list that has it/should have it, or farthest away successor
			}
		} catch (DigestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	private void get() {
		// Get the key and hash it
		key = MessageFormatter.getKey(data);
		try {
			keyHash = md.digest(key, 0, 2);
			if (isInRange(keyHash)) {
				// they want a value we are responsible for; get it
				value = db.get(key);
				if (value == null) {
					// we don't have this key-value pair, let em know
					//TODO build response with code 1
				} else {
					// we've got it, give it to em
					//TODO build response with code 0
				}
			} else {
				//TODO send request to next node
			}
		} catch (DigestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void remove() {
		// Get the key and hash it
		key = MessageFormatter.getKey(data);
		try {
			keyHash = md.digest(key, 0, 2);
			if(isInRange(keyHash)) {
				// a value we are responsible for
				value = db.remove(key);
				if(value == null) {
					// we don't have this key-value pair, let em know
					//TODO build response with code 1
				} else {
					// we removed it
					//TODO build response with code 0
				}
			} else {
				//TODO send request to next node
			}
		} catch (DigestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void shutdown() {
		// kill this node
		System.exit(0);
	}
	
	private void joinTable(){
		/*craft a response that sends:
		*		16B 	[uniqueID]
		*		1B		[assigned node number]
		*		4B		[successor(1)]
		*		1B		[successorNodeNum(1)]
		*		4B		[successor(2)]
		*		1B		[successorNodeNum(2)]
		*		4B		[successor(3)]
		*		1B		[successorNodeNum(3)]
		*		4B		[file list length]
		*		32B*file list length 	[hashed file names]		
		*/
		int newNodeNum = (((upperRange - nodeNumber)/2) + nodeNumber) & 0xFF;
		
		
		byte[] successor1;
		byte[] successor2;
		byte[] successor3;
		byte successorNodeNum1;
		byte successorNodeNum2;
		byte successorNodeNum3;
		
		byte nodeNum = (byte) newNodeNum;
		synchronized(successors){
			InetAddress ip = InetAddress.getByName(successors.getSuccessor(0).getIP());
			successor1 = ip.getAddress();
			int nodeNum1 = successors.getSuccessor(0).getNodeNum() & 0xFF;
			successorNodeNum1 = (byte) nodeNum1;
			
			InetAddress ip2 = InetAddress.getByName(successors.getSuccessor(1).getIP());
			successor2 = ip2.getAddress();
			int nodeNum2 = successors.getSuccessor(1).getNodeNum() & 0xFF;
			successorNodeNum2 = (byte) nodeNum2;
			
			InetAddress ip3 = InetAddress.getByName(successors.getSuccessor(2).getIP());
			successor3 = ip3.getAddress();
			int nodeNum3 = successors.getSuccessor(2).getNodeNum() & 0xFF;
			successorNodeNum3 = (byte) nodeNum3;
		}
		
		byte[] response = new byte[16000];
		//add unique ID
		for(int i = 0; i < 16; i++){
			response[i] = uniqueId[i];
		}
		//add assigned node number
		response[16] = nodeNum;
		
		//add successors
		for(int i = 0; i<4; i++){
			response[i+17] = successor1[i];
		}
		response[21] = successorNodeNum1;

		for(int i = 0; i<4; i++){
			response[i+22] = successor1[i];
		}
		response[26] = successorNodeNum1;

		for(int i = 0; i<4; i++){
			response[i+27] = successor1[i];
		}
		response[31] = successorNodeNum1;
		
		
		int numFiles = db.size(); //do i mask this?
		byte[] fileListLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(numFiles).array();
		
		for(int i =0; i<4;i++){
			response[i+32]=fileListLength[i];
		}
		
		int offset=0;
		for( byte[] key : db.keySet()){
			for(int i = 0; i < 32; i++){
				response[i+36+(offset*32)]=key[i];
			}
		}
		
		DatagramSocket clientSocket;
		clientSocket = new DatagramSocket(4004);
		DatagramPacket sendPacket = new DatagramPacket(response, response.length, senderAddress, 4003);
		
		
		
		
	}
	
	private boolean isInRange(int key) {
		return (nodeNumber < upperRange && nodeNumber < key && upperRange >= key)
				|| (nodeNumber > upperRange && (nodeNumber > key || upperRange <= key));
	}
}
