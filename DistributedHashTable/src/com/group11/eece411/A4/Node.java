package com.group11.eece411.A4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A representation of a physical node on the PlanetLab test-bed.
 * @author Group 11
 * @version a4
 */
public class Node {

	// Information about this physical machine:
	private static String hostName;
	
	// Information relative to the DHT:
	public static final int maxNodeNumber = 255;
	public static final int minNodeNumber = 0;
	private int nodeNumber;
	private static ArrayList<Successor> successors;
	private ConcurrentHashMap<byte[], byte[]> KVStore = new ConcurrentHashMap<byte[], byte[]>();
	
	/**
	 * Builds a node with just information about the physical machine.
	 * @param hostName		the host name of the node
	 */
	public Node(String hostName){
		this.hostName = hostName;
	}
	
	/*
	 * DHT specific methods
	 */
	
	/**
	 * Causes this node to join the DHT, assigning values to the node's
	 * fields relative to the DHT.
	 * @param	a response to a join request made by this node
	 * @return	true or false depending on the success of joinTable()
	 */
	public boolean joinTable(Message joinResponse) {
		boolean result = false;
		this.nodeNumber = joinResponse.getNodeNumber();
		this.successors = joinResponse.getSuccessorList();
		
		// Ensure the node number and successor list are correct
		if (nodeNumber >= Node.minNodeNumber &&
				nodeNumber <= Node.maxNodeNumber &&
				successors != null) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Sends a message (request or response) to a specified node.
	 * @param message	the message to send
	 * @param host		the host to send the message to
	 * @param port		the host's port to send the message to
	 * @return			true/false = success/failure
	 */
	public static boolean sendMessage(Message message, InetAddress host, int port) {
		boolean result = false;
		try {
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(message.getBuffer(), message.getBufferLength(), host, port);
			socket.send(packet);
			socket.close();
		} catch (Exception e) {
			System.out.println("Node: Sending a message failed.");
		}
		return result;
	}
	
	/**
	 * Starts a thread to respond to the specified message
	 * @param message	the message to parse and respond to
	 */
	public void respondToMessage(Message message) {
		Thread t = new Thread(new ResponseThread(message));
		t.start();
	}
	
	/**
	 * Starts a thread to periodically check the status of successors
	 */
	public void startCheckingSuccessors() {
		Thread t = new Thread(new CheckSuccessorsThread());
		t.start();
	}
	
	/**
	 * Causes this node to leave the DHT. I do not know the proper
	 * course of action here, for now we will just turn off this
	 * node.
	 */
	public void leaveTable() {
		System.out.println("Node: " + nodeNumber + " has left the DHT");
		// TODO: alert others that I have left the DHT? Maybe just my successors?
		System.exit(0);
	}
	
	/*
	 * Private thread classes
	 */
	
	/**
	 * Implements the thread that will respond to each new message
	 * received
	 * @author Group 11
	 * @version a4
	 */
	private static class ResponseThread implements Runnable {
		private final Message tMessage;
		
		public ResponseThread(Message tMessage) {
			this.tMessage = tMessage;
		}
		
		public void run() {
			// TODO:
			// receive message from instigator
			// parse and act accordingly
		}
	}
	
	/**
	 * Implements the thread that will periodically check the status
	 * of this node's successors
	 * @author Group 11
	 * @version a4
	 */
	private static class CheckSuccessorsThread implements Runnable {
		private final int WAIT_TIME = 10000;
		private boolean forever = true;
		private final int SEND_PORT = 4003;
		private final int RECEIVE_PORT = 4003;
		private final int TIMEOUT = 5000;
		public void run() {
			// TODO: implement it			
			// wait some amount of time
			while(forever){
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException e) {
					// continue
				}
				
				// lock successors list
				synchronized(successors){
					
					// check status of successors iteratively
					int numSuccessors = successors.size();
					for(int i = 0; i < numSuccessors; i++){
						// build and send message to successor i
						Message msg = new Message(hostName, SEND_PORT);
						msg.buildRequestMessage(32);
						sendMessage(msg, successors.get(i).getInetAddress(), SEND_PORT);
						
						
						
						try {
							byte[] receiveData = new byte[15500];
							DatagramSocket serverSocket = new DatagramSocket();
							serverSocket.setSoTimeout(TIMEOUT);
							serverSocket = new DatagramSocket(RECEIVE_PORT);
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							serverSocket.receive(receivePacket);
						} catch (SocketTimeoutException e){
							// remove successor because it is dead
							successors.remove(i);
						} catch (SocketException e) {
							// continue on
							// TODO remove successor?
						} catch (IOException e) {
							// continue on
							// TODO remove successor?
						}
						
						
					}
				}
			}
		}
	}
	
	/*
	 * Getters, setters, and some successorList modifiers from here on
	 */
	
	/**
	 * Returns the host name of this node.
	 * @return	this node's host name
	 */
	public String getHostName() {
		return this.hostName;
	}
	
	/**
	 * Returns this node's number (position in DHT).
	 * @return	this node's number
	 */
	public int getNodeNumber(){
		return this.nodeNumber;
	}
	
	/**
	 * Returns this node's successors as a SuccessorList.
	 * @return	list of this node's successors
	 */
	public ArrayList<Successor> getSuccessorList() {
		return this.successors;
	}
	
	/**
	 * Set this node's number (position in DHT) to the specified value.
	 * @param nodeNumber	the new position in DHT
	 */
	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
	
	/**
	 * Remove all of this node's successors
	 * @return the result (success = true) of the clear
	 */
	public boolean clearSuccessors() {
		boolean result = false;
		successors.clear();
		if (successors.isEmpty()) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Appends the specified successor to our successor list.
	 * @param successor	the successor to add
	 * @return			the result (success = true) of the add
	 */
	public boolean addSuccessor(Successor successor) {
		return successors.add(successor);
	}
	
	/**
	 * Adds the specified successor at the specified index
	 * in our successor list.
	 * @param index		the position to add to
	 * @param successor	the successor to add
	 * @return			the result (success = true) of the add
	 */
	public boolean addSuccessor(int index, Successor successor) {
		boolean result = false;
		successors.add(index, successor);
		if(successors.get(index) == successor) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Removes the specified successor from the successor list.
	 * @param successor	the successor to remove
	 * @return			true if successor was in list and removed
	 */
	public boolean removeSuccessor(Successor successor) {
		return successors.remove(successor);
	}
	
	/**
	 * Removes the specified successor from the successor list.
	 * @param index	the index of the successor to remove
	 * @return		true if successor is in list and removed
	 */
	public boolean removeSuccessor(int index) {
		return (successors.remove(index) != null) ? true : false;
	}
	
	/**
	 * Returns the successor at the specified index
	 * @param index	the index of the successor to retrieve
	 * @return		the successor at the specified index
	 */
	public Successor getSuccessor(int index) {
		return successors.get(index);
	}
	
	/**
	 * Returns the number of successors
	 * @return	the number of successors
	 */
	public int getNumberOfSuccessors() {
		return successors.size();
	}
}

/*
 * I need to know my:
 * 	IP address
 * 	hostname
 * 		(perhaps an object for all the network info)
 * 	node #
 * 	successor list
 * 	state
 * 		in table
 * 		not in table
 * 		crashed - recovering
 * 	message
 * 		received message: RequestMessage or ResponseMessage
 *		message to send: ResponseMessage
 */

/*
 * I need to be able to:
 * 	join the table
 * 		- set my nodenumber and successor list
 * 	leave the table
 * 		- set my nodenumber, alert successors
 * 	get successor list - need a successor object, a successor could be a child of a node
 * 	check successors, alive, dead, still in the system
 * 	get node number
 * build request
 * build response
 * send request
 * send response
 * 	
 */
