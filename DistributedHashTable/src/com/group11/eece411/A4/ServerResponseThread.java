package com.group11.eece411.A4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	private byte[] data;
	private final InetAddress senderAddress;
	private final int command;
	private final int nodeNumber;
	private final int upperRange;
	private final SuccessorList successors;
	private byte[] key;
	private int keyHash;

	private byte[] value = null;
	private byte[] uniqueId;

	public ServerResponseThread(byte[] d, InetAddress i,
			ConcurrentHashMap<byte[], byte[]> db, int nodeNumber,	int upperRange, byte[][][] u, SuccessorList s) {
		data = d;
		successors = s;
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
						return;
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
		case 34:
			returnSuccessors();
			break;
		case 35:
			changeUpperRange();
			break;
		}
	}

	private void returnSuccessors() {
		try {
			byte[] message = MessageFormatter.createResponse(uniqueId, 34, null);
			int length = message.length;
			message = Arrays.copyOf(message,  length + 15);
			byte[] address1 = InetAddress.getByName(successors.getSuccessor(0).getIP()).getAddress();
			byte[] address2 = InetAddress.getByName(successors.getSuccessor(1).getIP()).getAddress();
			byte[] address3 = InetAddress.getByName(successors.getSuccessor(2).getIP()).getAddress();
			
			for(int i = 0; i < 4; i++) {
				message[length - 1 + i] = address1[i];
			}
			message[length + 5] = (byte)successors.getSuccessor(0).getNodeNum();
			for(int i = 0; i < 4; i++) {
				message[length + i + 5] = address1[i];
			}
			message[length + 10] = (byte)successors.getSuccessor(1).getNodeNum();;
			for(int i = 0; i < 4; i++) {
				message[length + i + 10] = address1[i];
			}
			message[length + 15] = (byte)successors.getSuccessor(2).getNodeNum();;
			
			DatagramSocket serverSocket = new DatagramSocket();
			DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4008);
			serverSocket.send(sendPacket);
		} catch (Exception e) {
			//fuck it
			System.out.println("couldn't find address");
			return;
		}
	}
	
	private void changeUpperRange() {
		
	}
	
	private void put() {
		// Get the key and hash it
		key = MessageFormatter.getKey(data);
		try {
			keyHash = md.digest(key, 0, 2);
			// Only perform put if its in our range to screw with
			if (isInRange(keyHash)) {
				db.put(key, MessageFormatter.getValueRequest(data));
				try {
					byte[] message = MessageFormatter.createResponse(uniqueId, 0, null);
					DatagramSocket serverSocket = new DatagramSocket();
					DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
					serverSocket.send(sendPacket);
				} catch (Exception e) {
					//fuck it
					System.out.println("couldn't find address");
					return;
				}
			} else {
				passQuery();
				return;
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
					try {
						byte[] message = MessageFormatter.createResponse(uniqueId, 1, null);
						DatagramSocket serverSocket = new DatagramSocket();
						DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
						serverSocket.send(sendPacket);
					} catch (Exception e) {
						//fuck it
						System.out.println("couldn't find address");
						return;
					}
				} else {
					try {
						byte[] message = MessageFormatter.createResponse(uniqueId, 0, value);
						DatagramSocket serverSocket = new DatagramSocket();
						DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
						serverSocket.send(sendPacket);
					} catch (Exception e) {
						//fuck it
						System.out.println("couldn't find address");
						return;
					}
				}
			} else {
				passQuery();
				return;
			}
		} catch (DigestException e) {
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
				if (value == null) {
					try {
						byte[] message = MessageFormatter.createResponse(uniqueId, 1, null);
						DatagramSocket serverSocket = new DatagramSocket();
						DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
						serverSocket.send(sendPacket);
					} catch (Exception e) {
						//fuck it
						System.out.println("couldn't find address");
						return;
					}
				} else {
					try {
						byte[] message = MessageFormatter.createResponse(uniqueId, 0, value);
						DatagramSocket serverSocket = new DatagramSocket();
						DatagramPacket sendPacket = new DatagramPacket(message, message.length, senderAddress, 4003);
						serverSocket.send(sendPacket);
					} catch (Exception e) {
						//fuck it
						System.out.println("couldn't find address");
						return;
					}
				}
			} else {
				passQuery();
				return;
			}
		} catch (DigestException e) {
			e.printStackTrace();
		}
	}

	private void passQuery(){
		InetAddress sendTo;
		byte[] newMessageId = MessageFormatter.generateUniqueID();
		byte[] oldMessageId = Arrays.copyOf(data,  16);
		synchronized(successors) {
			try {
				if(keyHash < successors.getSuccessor(1).getNodeNum()) {
					sendTo = InetAddress.getByName(successors.getSuccessor(0).getIP());
				} else if(keyHash < successors.getSuccessor(2).getNodeNum()) {
					sendTo = InetAddress.getByName(successors.getSuccessor(1).getIP());
				} else {
					sendTo = InetAddress.getByName(successors.getSuccessor(2).getIP());
				}
				for(int i = 0; i < 16; i++) {
					data[i] = newMessageId[i];
				}
				DatagramSocket serverSocket = new DatagramSocket();
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, sendTo, 4003);
				serverSocket.send(sendPacket);
			} catch (Exception e) {
				//fuck it
				System.out.println("couldn't find address");
				return;
			}
			synchronized(uniqueIdList) {
				uniqueIdList = Arrays.copyOf(uniqueIdList, uniqueIdList.length);
				uniqueIdList[uniqueIdList.length-1][0] = oldMessageId;
				uniqueIdList[uniqueIdList.length-1][1] = newMessageId;
				uniqueIdList[uniqueIdList.length-1][2] = senderAddress.getAddress();
			}
		}
	}

	private void shutdown() {
		// kill this node
		System.exit(0);
	}

	private void joinTable(){

	}

	private boolean isInRange(int key) {
		return (nodeNumber < upperRange && nodeNumber < key && upperRange >= key)
				|| (nodeNumber > upperRange && (nodeNumber > key || upperRange <= key));
	}
}
