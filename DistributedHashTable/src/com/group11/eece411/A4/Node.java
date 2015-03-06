package com.group11.eece411.A4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * Node data structure
 */
public class Node {
<<<<<<< HEAD
	private byte[] IP;
	private int port;
	private int nodeNumber;
	
	public Node(String IP, int port, int nodeNumber){
		InetAddress ipholder;
		try {
			ipholder = InetAddress.getByName(IP);
			this.IP = ipholder.getAddress();
		} catch (UnknownHostException e) {
			//everything explodes
		}
		this.port = port;
=======
	private String IP;
	private int nodeNumber;
	
	public Node(String IP, int nodeNumber){
		this.IP = IP;
>>>>>>> origin/master
		this.nodeNumber = nodeNumber;
	}
	
	public byte[] getIP(){
		return this.IP;
	}
	
	public int getNodeNum(){
		return this.nodeNumber;
	}
	
}
