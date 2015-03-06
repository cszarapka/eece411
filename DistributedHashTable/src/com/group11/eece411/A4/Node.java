package com.group11.eece411.A4;

/*
 * Node data structure
 */
public class Node {
	private String IP;
	private int port;
	private int nodeNumber;
	
	public Node(String IP, int port, int nodeNumber){
		this.IP = IP;
		this.port = port;
		this.nodeNumber = nodeNumber;
	}
	
	public String getIP(){
		return this.IP;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public int getNodeNum(){
		return this.nodeNumber;
	}
	
}
