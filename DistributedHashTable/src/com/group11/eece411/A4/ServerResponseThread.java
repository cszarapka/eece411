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
		
	}
	
	private boolean isInRange(int key) {
		return (nodeNumber < upperRange && nodeNumber < key && upperRange >= key)
				|| (nodeNumber > upperRange && (nodeNumber > key || upperRange <= key));
	}
}
