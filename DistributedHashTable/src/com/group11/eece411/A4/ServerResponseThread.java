package com.group11.eece411.A4;

import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.security.MessageDigest;

public class ServerResponseThread extends Thread {
	
	private final ConcurrentHashMap<byte[], byte[]> db;
	private final MessageDigest md = MessageDigest.getInstance("MD5");
	
	private final DatagramPacket requestPacket;
	private final byte[] data;
	private final int command;
	private final int nodeNumber;
	private final int upperRange;
	private final byte[] key;
	private final int keyHash;
	
	private final byte[] value;
	
	public ServerResponseThread(DatagramPacket d,
			ConcurrentHashMap<byte[], byte[]> db,
			int nodeNumber,
			int upperRange) {
		requestPacket = d;
		this.db = db;
		this.nodeNumber = nodeNumber;
		this.upperRange = upperRange;
		data = requestPacket.getData();
		command = MsgFormatter.getCommand(data);
	}
	
	@Override
	public void run() {	
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
		}
	}

	private void put() {
		// Get the key and hash it
		key = MsgFormatter.getKey(data);
		keyHash = md.digest(key, 0, 2);
		
		// Only perform put if its in our range to screw with
		if (isInRange(keyHash)) {
			db.put(key, MsgFormatter.getValue(data));
			//TODO test what happens when it is full
			//TODO build response with code 0
		} else {
			//TODO send put request to node in successor list that has it/should have it, or farthest away successor
		}
	}
	
	private void get() {
		// Get the key and hash it
		key = MsgFormatter.getKey(data);
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
	}
	
	private void remove() {
		// Get the key and hash it
		key = MsgFormatter.getKey(data);
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
	}
	
	private void shutdown() {
		// kill this node
		System.exit(0);
	}
	
	private boolean isInRange(int key) {
		return (nodeNumber < upperRange && nodeNumber < key && upperRange >= key)
				|| (nodeNumber > upperRange && (nodeNumber > key || upperRange <= key));
	}
}
