package com.group11.eece411.A4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A representation of a physical node on the PlanetLab test-bed.
 * @author Group 11
 * @version a4
 */
public class Node {

	// Information about this physical machine:
	public static String hostName;
	
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
	 * @throws UnknownHostException 
	 */
	public boolean joinTable(Message joinResponse) {
		boolean result = false;
		this.nodeNumber = joinResponse.getNodeNumber();
		this.successors = joinResponse.getSuccessorList();

		// go through each key to determine if it should be get
		for(int i = 0 ;joinResponse.keys.size() > i; i++){
			Byte[] key = joinResponse.keys.get(i);
			byte[] receiveData = new byte[15500];
			
			
			
			if(getIndexOfSuccessorThatCoversRangeOf(key) == -1){
				
				//build message
				Message msg = new Message(hostName, 4003);
				msg.buildAppLevelRequestMessage(Codes.CMD_GET, key);
				
				try {
					sendMessage(msg, InetAddress.getByAddress(toPrimitives(joinResponse.originIP)), 4003);
					
					// receive message
					DatagramSocket serverSocket = new DatagramSocket();
					serverSocket.setSoTimeout(5000);
					serverSocket = new DatagramSocket(4003);
					
					// try to receive message
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					Message receivedMessage = new Message(receiveData);
					receivedMessage.parseReceivedResponseMessage();
					byte[] value = toPrimitives(receivedMessage.value);
					KVStore.put(toPrimitives(key), value);
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SocketTimeoutException e){
					// data loss
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			/* //try to send message
							byte[] receiveData = new byte[15500];
							DatagramSocket serverSocket = new DatagramSocket();
							serverSocket.setSoTimeout(TIMEOUT);
							serverSocket = new DatagramSocket(RECEIVE_PORT);
							
							// try to receive message
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							serverSocket.receive(receivePacket);
							Message receivedMessage = new Message(receiveData);
							receivedMessage.parseReceivedResponseMessage();
							*/
			
			
		}
		// TODO: get files

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
			System.out.println("point 0");
			DatagramPacket packet = new DatagramPacket(message.getBuffer(), message.getBufferLength(), host, port);
			System.out.println("point 1");
			socket.send(packet);
			socket.close();
		} catch (Exception e) {
			System.out.println("Node: Sending a message failed.");
			System.out.println("Exception: "+e);
		}
		return result;
	}

	/**
	 * Starts a thread to respond to the specified message
	 * @param message	the message to parse and respond to
	 */
	public void respondToMessage(Message message) {
		Thread t = new Thread(new ResponseThread(message, this));
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
		private final Node tNode;

		public ResponseThread(Message tMessage, Node tNode) {
			this.tMessage = tMessage;
			this.tNode = tNode;
			
		}

		public void run() {
			// Check if the message is for me
			if (tMessage.originNodeNumber == tNode.nodeNumber) {
				
			}

			int n;
			switch (tMessage.command) {
			case Codes.CMD_GET:
				n = tNode.getIndexOfSuccessorThatCoversRangeOf(tMessage.key);
				if(n == -1) {
					byte[] value = tNode.KVStore.get(toPrimitives(tMessage.key));
					Message response = new Message(tNode.getHostName(), 4003);
					if(value == null) {
						response.buildGetResponseMessage(Codes.KEY_DOES_NOT_EXIST, 0, null);
					} else {
						response.buildGetResponseMessage(Codes.SUCCESS, value.length, toObjects(value));
					}
					response.setUniqueID(Node.toObjects(tMessage.getUniqueID()));
					try {
						sendMessage(response, InetAddress.getByAddress(toPrimitives(tMessage.originIP)), 4003);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					sendMessage(tMessage, tNode.getSuccessor(n).getInetAddress(), 4003);
				}

				break;
			case Codes.CMD_PUT:
				n = tNode.getIndexOfSuccessorThatCoversRangeOf(tMessage.key);
				if(n == -1) {
					byte[] value = tNode.KVStore.put(toPrimitives(tMessage.key), toPrimitives(tMessage.value));
					Message response = new Message(tNode.getHostName(), 4003);
					//if(value == !value) {
						//response.buildGetResponseMessage(Codes.KEY_DOES_NOT_EXIST, 0, null);
					//} else {
						response.buildWireResponseMessage(Codes.CMD_PUT,Codes.SUCCESS);
					//} 
					response.setUniqueID(Node.toObjects(tMessage.getUniqueID()));
					try {
						sendMessage(response, InetAddress.getByAddress(toPrimitives(tMessage.originIP)), 4003);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					sendMessage(tMessage, tNode.getSuccessor(n).getInetAddress(), 4003);
				}
				break;
			case Codes.CMD_REMOVE:
				n = tNode.getIndexOfSuccessorThatCoversRangeOf(tMessage.key);
				if(n == -1) {
					boolean value = tNode.KVStore.remove(toPrimitives(tMessage.key), toPrimitives(tMessage.value));
					Message response = new Message(tNode.getHostName(), 4003);
					if(!value) {
						response.buildWireResponseMessage(Codes.KEY_DOES_NOT_EXIST, 0);
					} else {
						response.buildWireResponseMessage(Codes.CMD_REMOVE,Codes.SUCCESS);
					} 
					response.setUniqueID(Node.toObjects(tMessage.getUniqueID()));
					try {
						sendMessage(response, InetAddress.getByAddress(toPrimitives(tMessage.originIP)), 4003);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					sendMessage(tMessage, tNode.getSuccessor(n).getInetAddress(), 4003);
				}
				break;
			case Codes.CMD_SHUTDOWN:
				System.exit(0);
				break;
			case Codes.ADD_SUCCESSOR:
				tNode.addSuccessor(new Successor(tMessage.hostName, ((int)Node.toPrimitives(tMessage.value)[1]) * 16 + ((int)Node.toPrimitives(tMessage.value)[0])));
				break;
			case Codes.REQUEST_TO_JOIN:
				Message response = new Message(tNode.getHostName(), 4003);
				int offeredNodeNumber = (tNode.getNodeNumber() + Node.maxNodeNumber / 2) % Node.maxNodeNumber;
				int firstSuccessor = tNode.getFirstSuccessor();
				if(firstSuccessor != -1) {
					offeredNodeNumber = ((firstSuccessor - tNode.getNodeNumber()) / 2) % Node.maxNodeNumber;
				}
				int size = tNode.KVStore.size();
				Enumeration<byte[]> e = tNode.KVStore.keys();
				byte[] fileList = new byte[size*32];
				byte[] file = new byte[32];
				for(int i = 0; i < size; i++) {
					file = e.nextElement();
					for(int j = 0; j < 32; j++) {
						fileList[32*i+j] = file[j];
					}
				}
				response.buildInviteMessage(offeredNodeNumber, successors, tNode.KVStore.size(), fileList);
				try {
					sendMessage(response,InetAddress.getByAddress(toPrimitives(tMessage.originIP)), 4003);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case Codes.GET_SUCCESSOR_LIST:
			{
				Message re = new Message(tNode.getHostName(), 4009);
				re.buildReturnSuccessors(tNode.getSuccessorList());
				try {
					sendMessage(re, InetAddress.getByAddress(toPrimitives(tMessage.originIP)), 4009);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				break;
			}
			}
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
		private ArrayList<Successor> ss = new ArrayList<Successor>();
		ArrayList<Successor> successorCopy = new ArrayList<Successor>();
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
						msg.buildReturnSuccessors(successors);
						//try to send message
						sendMessage(msg, successors.get(i).getInetAddress(), SEND_PORT);
						
						
						
						try {
							
							byte[] receiveData = new byte[15500];
							DatagramSocket serverSocket = new DatagramSocket();
							serverSocket.setSoTimeout(TIMEOUT);
							serverSocket = new DatagramSocket(RECEIVE_PORT);
							
							// try to receive message
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							serverSocket.receive(receivePacket);
							Message receivedMessage = new Message(receiveData);
							receivedMessage.parseReceivedResponseMessage();
							
							// get the successor's successorlist
							for(int l = 0; l < receivedMessage.getSuccessorList().size(); l++){
								ss.add(receivedMessage.getSuccessorList().get(l));
							
							}
							// TODO ADD SUCCESSORS'S SUCCESSORS IF THEY AREN'T ON THIS NODE'S SUCCESSOR LIST
							
							// get all the current node nums
							ArrayList<Integer> successorNodeNums = new ArrayList<Integer>();
							for(int p = 0; p < successors.size(); p++){
								successorNodeNums.add(successors.get(p).getNodeNumber());
							}
							
							// add successors if their node number isn't in the list already
							for(int p = 0; p < receivedMessage.getSuccessorList().size(); p++){
								
								//check if the list doesn't contain the node with that number yet
								if(!successorNodeNums.contains(receivedMessage.getSuccessorList().get(p).getNodeNumber())){
									//if not then add it to the successor list
									
									String newSuccessorHostname = receivedMessage.getSuccessorList().get(p).getHostName();
									int newSuccessorNodeNum = receivedMessage.getSuccessorList().get(p).getNodeNumber();
									Successor newSuccessor = new Successor(newSuccessorHostname, newSuccessorNodeNum);
									successors.add(newSuccessor);
								}
								// if already exists then do nothing
							}
							
						} catch (SocketTimeoutException e){
							// remove successor because it is dead
							successors.remove(i);
						} catch (SocketException e) {
							// continue on
							// TODO remove successor?
						} catch (IOException e) {
							// continue on
							// TODO remove successor?
						} finally {
							//reorder successors
							//add successor's successors if they aren't included already
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
	
	public int getFirstSuccessor() {
		int upper = this.nodeNumber;
		int lower = this.nodeNumber;
		int nodeIndex = -1;

		for(int j = 0; j < this.getNumberOfSuccessors(); j++){
			if((upper - this.getNodeNumber()) % 256 > (this.getSuccessor(j).getNodeNumber() - this.getNodeNumber()) % 256) {
				upper = this.getSuccessor(j).getNodeNumber();
				nodeIndex = j;
			}
			
		}
		return nodeIndex;
		 
	}
	
	public int getIndexOfSuccessorThatCoversRangeOf(Byte[] key) {

		int hashValue;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] thedigest = md.digest(toPrimitives(key));
		hashValue = ((int)thedigest[1]) * 16 + ((int)thedigest[0]);
		int upper = this.nodeNumber;
		int lower = this.nodeNumber;
		int nodeIndex = -1;

		for(int j = 0; j < this.getNumberOfSuccessors(); j++){
			if((hashValue - lower) % 256 > (hashValue - this.getSuccessor(j).getNodeNumber()) % 256) {
				lower = this.getSuccessor(j).getNodeNumber();
				nodeIndex = j;
			}
			
			if((upper - hashValue) % 256 > (this.getSuccessor(j).getNodeNumber() - hashValue) % 256) {
				upper = this.getSuccessor(j).getNodeNumber();
			}
			
		}
		return nodeIndex;
		 
	}
	

	static public Byte[] toObjects(byte[] bytesPrim) {

		Byte[] bytes = new Byte[bytesPrim.length];
		int i = 0;
		for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
		return bytes;

	}

	static public byte[] toPrimitives(Byte[] oBytes)
	{

		byte[] bytes = new byte[oBytes.length];
		for(int i = 0; i < oBytes.length; i++){
			bytes[i] = oBytes[i];
		}
		return bytes;

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
