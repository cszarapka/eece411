package com.group11.eece411.A4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A stripped down version of a node, holding only the bare minimum
 * details of it.
 * @author Group 11
 * @version a4
 *
 */
public class Successor {
	private String hostName;
	private int nodeNumber;
	private InetAddress inetAddress;
	
	public Successor(String hostName, int nodeNumber) {
		this.hostName = hostName;
		this.nodeNumber = nodeNumber;
		try {
			inetAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			System.out.println("Successor - Exception:");
			System.out.println("Unknown host name, inetAddress could not be assigned.");
			e.printStackTrace();
		}
	}
	
	/*
	 * Standard getters and setters
	 */
	
	public String getHostName() {
		return hostName;
	}
	
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
		try {
			inetAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			System.out.println("Successor - Exception:");
			System.out.println("Unknown host name, inetAddress could not be assigned.");
			e.printStackTrace();
		}
	}
	
	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
}
