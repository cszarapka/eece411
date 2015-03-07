package com.group11.eece411.A4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * Node data structure
 */
public class Node {

	private String IP;
	private int nodeNumber;
	
	public Node(String IP, int nodeNumber){
		this.IP = IP;
		this.nodeNumber = nodeNumber;
	}
	
	public String getIP(){
		return this.IP;
	}
	
	public int getNodeNum(){
		return this.nodeNumber;
	}
	
	public InetAddress getInet() {
		return InetAddress.get
	}
}
