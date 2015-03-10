package com.group11.eece411.A4;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Representation of a message used in the DHT
 * @author Group 11
 * @version a4
 */
public class Message {

	// The raw data received 
	private Byte[] rawData;
	
	private String hostName;
	private int port;

	// The fields to be extracted or inserted into the raw data:
	// Specific to professor's Wire Protocol:
	private Byte[] uniqueID = new Byte[16];
	private int command;
	private int echoedCommand;
	private int responseCode;
	private int nodeNumber;
	private Byte[] key = new Byte[32];
	private ArrayList<Byte[]> keys;
	private int valueLength;
	private Byte[] value;
	
	private Byte[] originIP;
	private int originNodeNumber;
	
	public static final int MESSAGE_TYPE_POSITION = 7;
	public static final int COMMAND_POSITION = 16;
	
	// Values appended to these messages for our own purposes:
	private Byte[] successorHostNames;
	private Byte[] successorNodeNumbers;

	// Message types
	private int messageType;
	public static final int SEND_REQUEST 		= 0;
	public static final int SEND_RESPONSE 		= 1;
	public static final int SEND_UPDATE			= 2;
	public static final int RECEIVED_REQUEST 	= 3;
	public static final int RECEIVED_RESPONSE 	= 4;
	public static final int RECEIVED_UPDATE		= 5;

	/**
	 * Constructs an empty message to be built into a request
	 * or response to be sent to another node
	 */
	public Message(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	/**
	 * Constructs a message from a received byte array and then
	 * parses it, determining the data inside
	 * @param rawData	the messaged received
	 */
	public Message(byte[] rawData) {
		// Convert the primitive type to the object Byte, it's easier to work with
		this.rawData = new Byte[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			this.rawData[i] = Byte.valueOf(rawData[i]);
		}
		parseMessage();
	}

	/**
	 * Fills the local variables based on the content of the message
	 * received. Starts with unique ID, gets the message type and command code,
	 * and parses accordingly.
	 */
	private void parseMessage() {
		// Get the unique ID, the first 16 bytes
		uniqueID = Arrays.copyOfRange(rawData, 0, 16);
		System.out.println("uniqueID: " + uniqueID.toString());
		System.out.println("uniqueID length: " + uniqueID.length);

		// Get the message type, as an int
		messageType = rawData[MESSAGE_TYPE_POSITION].intValue();
		System.out.println("Message type: " + messageType);

		// Get the command code, as an int
		command = rawData[COMMAND_POSITION].intValue();
		System.out.println("Command: " + command);

		// Parse the message based on whether it is a request or response
		switch (messageType) {
		case Message.RECEIVED_REQUEST: parseReceivedRequestMessage();
			break;
		case Message.RECEIVED_RESPONSE: parseReceivedResponseMessage();
			break;
		case Message.RECEIVED_UPDATE: parseReceivedUpdateMessage();
			break;
		}
	}

	/**
	 * Parses a received request-message, based on the command code.
	 * See the documentation at the bottom for protocol and 
	 * message format details.
	 */
	public void parseReceivedRequestMessage() {
		
		if (command == Codes.CMD_SHUTDOWN || command == Codes.REQUEST_TO_JOIN || command == Codes.ARE_YOU_ALIVE) {
			// nothing left to parse
			return;
		}
		
		int commandToUse = command;
		int index = 17;
		
		// Check for an echoed command
		if (command == Codes.ECHOED_CMD) {
			originIP = new Byte[4];
			originIP = Arrays.copyOfRange(rawData, index, index+4);
			originNodeNumber = rawData[index+4].intValue();
			echoedCommand = rawData[index+5].intValue();
			index += 6;
			commandToUse = echoedCommand;
		}

		// Get the 32-byte key
		key = Arrays.copyOfRange(rawData, index, (index + 32));

		// Get the value length and value if it's a put command
		if (commandToUse == Codes.CMD_PUT) {
			valueLength = bytesToValueLength((index + 33), (index + 32));
			readValue(index + 34);
		}
	}

	/**
	 * Parses a received response-message, based on the command code.
	 * See the documentation at the bottom for protocol and 
	 * message format details.
	 */
	public void parseReceivedResponseMessage() {
		int index = 17;
		
		// Check for an invite to join message
		if (command == Codes.INVITE_TO_JOIN) {
			nodeNumber = rawData[COMMAND_POSITION+1];
			
			// Get the number of successors following
			int numSuccessors = rawData[COMMAND_POSITION+2].intValue();
			final int BEGIN_SUCCESSORS = COMMAND_POSITION+3;
			
			// Get each successor
			for(int i = 0; i < numSuccessors; i++){
				
				// Get each of the four bytes of the IP address, offset by the number of successors already received
				for(int k = 0; k < 4; k++){
					successorHostNames[i] = rawData[BEGIN_SUCCESSORS+k+i*5];
				}
				
				// Get the node number, which is 4 past the beginning of the node, then offset 
				//by the number of successor infomation already received
				successorNodeNumbers[i] = rawData[BEGIN_SUCCESSORS+4+i*5];
			}
			
			// Offset the begin successors index by the number of successors to find the beginning of the key list length
			final int BEGIN_KEY_LIST_LENGTH = BEGIN_SUCCESSORS + numSuccessors*5;
			
			// Get the file list length
			int keyListLength = rawData[BEGIN_KEY_LIST_LENGTH].intValue();
			
			final int BEGIN_KEYS = BEGIN_KEY_LIST_LENGTH+4;
			final int KEY_LENGTH = 32;
			
			// Get each key
			Byte[] newKey = new Byte[32];
			for(int i = 0; i < keyListLength; i++){
				// Get each key
				for(int k = 0; k < 32; k++){
					newKey[k] = rawData[BEGIN_KEYS+i*KEY_LENGTH];
				}
				// Put each key in the ArrayList
				keys.add(newKey);
			}
			
			return;
		}
		
		int commandToUse = command;
		// Check for an echoed command
		if (command == Codes.ECHOED_CMD) {
			originIP = new Byte[4];
			originIP = Arrays.copyOfRange(rawData, index, index+4);
			originNodeNumber = rawData[index+4].intValue();
			echoedCommand = rawData[index+5].intValue();
			index += 6;
			commandToUse = echoedCommand;
		}
		
		// get the response code
		responseCode = rawData[index].intValue();
		
		// If it is a successful GET command, read the value
		if (commandToUse == Codes.CMD_GET && responseCode == Codes.SUCCESS) {
			valueLength = bytesToValueLength(index+2, index+1);
			readValue(index+3);
		}
	}
	
	
	public void parseReceivedUpdateMessage() {
		
	}

	/**
	 * Returns the unique ID of the message as a byte array.
	 * Warning - could be called on messages who do not
	 * yet have a unique ID
	 * @return	the unique ID as a byte array.
	 */
	public byte[] getUniqueID() {
		byte[] returnValue = new byte[uniqueID.length];
		for (int i = 0; i < uniqueID.length; i++) {
			returnValue[i] = uniqueID[i].byteValue();
		}
		return returnValue;
	}
	
	/**
	 * returns the node number assigned
	 * @return assigned node number
	 */
	public int getNodeNumber() {
		return nodeNumber;
	}
	
	
	/**
	 * returns an ArrayList of all the successors
	 * @return all the successors
	 */
	public ArrayList<Successor> getSuccessorList() {
		ArrayList<Successor> als = new ArrayList<Successor>();
		
		// get number of successors
		int numSuccessors = successorNodeNumbers.length;
		
		byte[] host = new byte[4];
		for(int i = 0; i < numSuccessors; i++){
			// get the Bytes for the ith successor's ip
			for(int k = 0; k < 4; k++){
				// offset by 4 per iteration
				host[k] = successorHostNames[k+i*4].byteValue();
				
			}
			
			// convert the Byte of the successor's node number
			int nodeNum = successorNodeNumbers[i].intValue();
			
			//TODO convert host to string representation
			hostName = getIpAddress(host);
			Successor success = new Successor(hostName, nodeNum);
			als.add(success);
		}
		
		return als;
	}
	
	/**
	 * Returns the byte array representation of Message.rawData
	 * @return 	byte array value
	 */
	public byte[] getBuffer() {
		byte[] rawBytes = new byte[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			rawBytes[i] = rawData[i].byteValue();
		}
		return rawBytes;
	}
	
	/**
	 * Returns the length of the buffer
	 * @return	buffer length
	 */
	public int getBufferLength() {
		return rawData.length;
	}
	
	/*
	 * The building of the request messages
	 */
	
	/**
	 * Builds a message to be sent based on the type specified and the
	 * values passed and assembles the raw data to be sent.
	 * @param type
	 */
	public boolean buildRequestMessage(int command) {
		// Set the unique ID
		setUniqueID(Message.SEND_REQUEST);
		
		// Set the command
		this.command = command;
		
		if (command == Codes.CMD_SHUTDOWN || command == Codes.REQUEST_TO_JOIN || command == Codes.ARE_YOU_ALIVE) {
			// nothing left to add to the message
			// assemble the raw data, length is uniqueID.length + 1
			rawData = new Byte[uniqueID.length + 1];
			for (int i = 0; i < uniqueID.length; i++) {
				rawData[i] = uniqueID[i];
			}
			rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
			return true;
		}
		return false;
	}
	
	/**
	 * Builds an echoed put request-message with the specified fields.
	 * Assembles the raw data to be sent.
	 * @param originHostName	the host name of the node who started the request
	 * @param originNodeNumber	the node number of the node who started the request
	 * @param key				the key of value to be put
	 * @param valueLength		the length of the value to be put
	 * @param value				the value to be put
	 * @return					true if the message was built successfully
	 */
	public void buildEchoedPutRequestMessage(String originHostName, int originNodeNumber, Byte[] key, int valueLength, Byte[] value) {
		command = Codes.ECHOED_CMD;
		echoedCommand = Codes.CMD_PUT;
		
		// Get the IP of the origin
		byte[] originIPbytes;
		try {
			originIPbytes = InetAddress.getByName(originHostName).getAddress();
		} catch (UnknownHostException e) {
			System.out.println("Message: Echoed Put Request-Message unknown host error");
			return;
		}
		
		// Get the node number of the origin as an int
		this.originNodeNumber = originNodeNumber;
		
		// Get the key as a Byte array
		this.key = key;
		
		// Get the value length; int
		this.valueLength = valueLength;
		
		// Get the value
		this.value = new Byte[valueLength];
		this.value = value;
		
		/*
		 * Assemble the raw data from all of this info
		 */
		setUniqueID(Message.SEND_REQUEST);
		int index = 0;
		rawData = new Byte[uniqueID.length + 1 + 5 + 1 + key.length + 1 + valueLength];
		
		// Add the unique ID
		for (int i = index; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}
		
		// Add the command
		rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
		index = uniqueID.length + 1;
		
		// Add the origin: 4byte IP, then 1 byte node #
		for (int i = index; i < originIPbytes.length + index; i++) {
			rawData[i] = originIPbytes[i - index];
		}
		index = originIPbytes.length + index;
		rawData[index] = Byte.valueOf(intToByteArray(originNodeNumber)[0]);
		index += 1;
		
		// Add the key to be "put" to the raw data
		for (int i = index; i < key.length + index; i++) {
			rawData[i] = key[i-index];
		}
		index = key.length + index;
		
		// Add the value length
		rawData[index] = Byte.valueOf(intToByteArray(valueLength)[0]);
		index += 1;
		
		// Add the value 
		for (int i = index; i < valueLength + index; i++) {
			rawData[index] = value[i - index];
		}
	}
		
	public void buildEchoedRequestMessage(String originHostName, int originNodeNumber, Byte[] key) {
		command = Codes.ECHOED_CMD;
		echoedCommand = Codes.CMD_PUT;
		rawData = new Byte[4 + 1 + 32];
		
		
		
	}
	
	public void buildEchoedShutdownRequestMessage(String originHostName, int originNodeNumber) {
		
	}
	
	public void buildAppLevelRequestMessage(int command, Byte[] key) {
		
	}
	
	public void buildPutRequestMessage(Byte[] key, int valueLength, Byte[] value) {
		
	}
	
	/*
	 * The building of the response messages
	 */
	
	public boolean buildWireResponseMessage(int command, int responseCode) {
		if (command == Codes.CMD_PUT || command == Codes.CMD_REMOVE || command == Codes.CMD_SHUTDOWN) {
			setUniqueID(Message.SEND_RESPONSE);
			this.command = command;
			this.responseCode = responseCode;
			return true;
		}
		return false;
	}
	
	/**
	 * Builds a response message to the GET request
	 * @param responseCode response code for the request
	 * @param valueLength length of the value byte[] sent
	 * @param value the message to be sent
	 * @return
	 */
	public void buildGetResponseMessage(int responseCode, int valueLength, Byte[] value) {
		setUniqueID(Message.SEND_RESPONSE);
		this.responseCode = responseCode;
		this.valueLength = valueLength;
		this.value = value;
	}
	
	/**
	 * builds a message to respond to a  table invite request
	 * @param offeredNodeNumber the node number to be offered
	 * @param successors an ArrayList of the successors
	 * @param fileListLength length of the key list
	 * @param keyNames byte[] of all keys put together end to end (32 bytes each)
	 * @return
	 */
	public void buildInviteMessage(int offeredNodeNumber, ArrayList<Successor> successors, int keyListLength, byte[] keyNames) {
		setUniqueID(Message.SEND_RESPONSE);
		this.responseCode = Codes.SUCCESS;
		this.value[0] = Byte.valueOf(intToByteArray(offeredNodeNumber)[0]);
		
		
		this.nodeNumber = offeredNodeNumber;
		int numSuccessors = successors.size();
		this.value[1] = Byte.valueOf(intToByteArray(numSuccessors)[0]);
		
		int BEGIN_SUCCESSORS = 2;
		Byte[] nextSuccessorAddress = new Byte[4];
		// put data into successor message
		for(int i = 0; i < numSuccessors; i++){
			for(int k = 0; k < 4; k++){
				nextSuccessorAddress[k] = Byte.valueOf(successors.get(i).getInetAddress().getAddress()[k]);
			}
			
			for(int k = 0; k < nextSuccessorAddress.length; k++){
				this.value[BEGIN_SUCCESSORS+k+i*5] = nextSuccessorAddress[k];
			}
			
			// puts the node number into a byte[]
			Byte nextSuccessorNodeNum = Byte.valueOf(ByteBuffer.allocate(1).putInt(successors.get(i).getNodeNumber()).array()[0]);
			this.value[BEGIN_SUCCESSORS+i*5 + 4] = nextSuccessorNodeNum;
		}
		
		int BEGIN_KEY_LIST_LENGTH = BEGIN_SUCCESSORS + numSuccessors*5;
		
		// now get key list length and put it into value
		int INT_LENGTH = 4;
		for(int i = 0; i < INT_LENGTH; i++){
			this.value[BEGIN_KEY_LIST_LENGTH+i] = Byte.valueOf(ByteBuffer.allocate(1).putInt(keyListLength).array()[i]); 
		}
		
		int BEGIN_KEY_LIST = BEGIN_KEY_LIST_LENGTH + 4;
		// get all key names and put into value
		
		for(int i = 0; i < keyListLength; i++){
			// copy key name
			for(int k = 0; k < keyListLength; k++){
				this.value[BEGIN_KEY_LIST+i*32+k] = Byte.valueOf(keyNames[i*32+k]);
			}
		}	
	}
	
	
	/*
	 * The building of the update messages
	 */
	
	
	{
		int commandToUse = command;
		int index = 17;
		
		// Check for an echoed command
		if (command == Codes.ECHOED_CMD) {
			originIP = new Byte[4];
			originIP = Arrays.copyOfRange(rawData, index, index+4);
			originNodeNumber = rawData[index+4].intValue();
			echoedCommand = rawData[index+5].intValue();
			index += 6;
			commandToUse = echoedCommand;
		}

		// Get the 32-byte key
		key = Arrays.copyOfRange(rawData, index, (index + 32));

		// Get the value length and value if it's a put command
		if (commandToUse == Codes.CMD_PUT) {
			valueLength = bytesToValueLength((index + 33), (index + 32));
			readValue(index + 34);
		}
	}
	
	/**
	 * Generates the unique ID
	 * @param hostName		the host name of the sending host
	 * @param port			the port the message is sent on
	 * @param messageType	the type of message following the unique id
	 */
	public byte[] generateUniqueID(int messageType) {
		// Convert the hostname to an IP byte array
		byte[] ip;
		try {
			ip = Arrays.copyOf(InetAddress.getByName(hostName).getAddress(), 4);
		} catch (Exception e) {
			System.out.println("Unknown host name in Generate Unique ID");
			return null;
		}
		
		// Convert the port into a 2 byte array
		byte[] portBytes = intToByteArray(port);
		
		// Generate the random byte
		Random rand = new Random();
		byte randomByte = (byte) rand.nextInt(255);
		
		// Get the time stamp
		byte[] timeStampBytes = longToByteArray(System.currentTimeMillis());
		
		// Put it all together
		byte[] returnValue = new byte[16];
		returnValue[0] = ip[0];
		returnValue[1] = ip[1];
		returnValue[2] = ip[2];
		returnValue[3] = ip[3];
		returnValue[4] = portBytes[0];
		returnValue[5] = portBytes[1];
		returnValue[6] = randomByte;
		returnValue[7] = intToByteArray(messageType)[0];
		returnValue[8] = timeStampBytes[0];
		returnValue[9] = timeStampBytes[1];
		returnValue[10] = timeStampBytes[2];
		returnValue[11] = timeStampBytes[3];
		returnValue[12] = timeStampBytes[4];
		returnValue[13] = timeStampBytes[5];
		returnValue[14] = timeStampBytes[6];
		returnValue[15] = timeStampBytes[7];
		
		return returnValue;
	}
	
	
	private void setUniqueID(int messageType) {
		byte[] temp = generateUniqueID(messageType);
		for (int i = 0; i < temp.length; i++) {
			this.uniqueID[i] = Byte.valueOf(temp[i]);
		}
	}
	

	/**
	 * Returns the integer value of 2 bytes in rawData
	 * @param msb	the most significant byte
	 * @param lsb	the least significant byte
	 * @return		the integer value
	 */
	private int bytesToValueLength(int msb, int lsb) {
		return (rawData[msb].intValue() << 8) + rawData[lsb].intValue();
	}
	
	public static int byteArrayToInt(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getInt();
	}
	
	public static byte[] intToByteArray(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}
	
	public static long byteArrayToLong(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getLong();
	}
	
	public static byte[] longToByteArray(long i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putLong(i);
	    return bb.array();
	}
	

	/**
	 * Reads the value from message's raw data into the local
	 * variable value
	 * @param offset the offset to start reading the value from
	 */
	private void readValue(int offset) {
		value = new Byte[valueLength];
		for (int i = 0; i < valueLength; i ++) {
			value[i] = rawData[i + offset];
		}
	}
	
	/**
	 * returns a string representation of an IP address
	 * @param rawBytes the data to be converted into an IP address
	 * @return IP address
	 */
	public static String getIpAddress(byte[] rawBytes) {
		int i = 4;
		String ipAddress = "";
		for (byte raw : rawBytes)
		{
			ipAddress += (raw & 0xFF);
			if (--i > 0)
			{
				ipAddress += ".";
			}
		}

		return ipAddress;
	}


	/*
	 * Our DHT Messaging protocol
	 */

	/*
	 * All messages have a 16 byte unique ID at the beginning with
	 * the following format:
	 * 
	 * | host IP |   port  | random | message type | time stamp |
	 * | 4 bytes | 2 bytes | 1 byte |    1 byte    |   8 bytes  |
	 */

	/*
	 * App-layer/level messages : Requests
	 */

	/*
	 * Put - Command: 0x01
	 * 
	 * | command |    key   |   value length   |      value	    |
	 * | 1 byte  | 32 bytes | int, 2 bytes, LE | 0-15,000 bytes |
	 * 
	 * 
	 * Get, Remove - Commands: 0x02, 0x03
	 * 
	 * | command |    key   |
	 * | 1 byte  | 32 bytes |
	 * 
	 * 
	 * Shutdown - Command: 0x04
	 * 
	 * | command |
	 * | 1 byte  |
	 * 
	 */

	/*
	 * App-layer/level messages : Replies
	 */

	/*
	 * Replies differ by request type.
	 * It is necessary for us to extend his wire protocol with the command code
	 * so that we know which of these formats the message is, and can parse accordingly.
	 * 
	 * Put, Remove, Shutdown - Commands: 0x01, 0x03, 0x04
	 * 
	 * | command | response code |
	 * | 1 byte  |    1 byte     |
	 * |		 |				 |
	 * 
	 * 
	 * Get - Command: 0x02
	 * 
	 * | command | response code |   value length   |      value	 |
	 * | 1 byte  |    1 byte     | int, 2 bytes, LE | 0-15,000 bytes |
	 * |		 |				 |   only for get   |				 |
	 * 
	 */

	/*
	 * Lower layer messages
	 */

	/*
	 * These are messages sent between nodes. It is important to distinguish these
	 * so we are aware of varying message formats for echoed commands.
	 * 
	 * Request to join - made by a node desiring to enter the DHT
	 * Command: (int) 30
	 * 
	 * | command | 
	 * | 1 byte  |
	 * 
	 * 
	 * I'm leaving - made by a node leaving the DHT, unless the node was shutdown or crashed
	 * Command: (int) 31
	 * 
	 * | command | my node number |
	 * | 1 byte  |     1 byte     |
	 * 
	 * 
	 * Are you alive? - made by the check successors thread, querying nodes if they are alive
	 * Command: (int) 32
	 * 
	 * | command | 
	 * | 1 byte  |
	 * 
	 * 
	 * Invite to join - made by a node responding to a request to join the DHT
	 * Command: (int) 33
	 * 
	 * | command | offered node # | # of successors |  successors  | file list length | hashed file names (file list) |
	 * | 1 byte  |     1 byte	  |     1 byte	    | 5 bytes each |      4 bytes     |      up to 32 bytes each	  |
	 * 
	 * successors: 4 bytes for ip, 1 byte for node number
	 * 
	 * 
	 * Echoed command - commands propagated from an origin node throughout the DHT
	 * Command: (int) 34
	 * 
	 * | command | origin  | echoed command | ... same as command
	 * | 1 byte  | 5 bytes |     1 byte	    |
	 * 
	 * the origin is 4 bytes for ip, 1 byte for node number
	 * 
	 * 
	 * Is alive update - a broadcast from a node, alerting other nodes of the life of the included nodes
	 * Command: (int) 35
	 * 
	 * | command | # of nodes |    nodes    | 
	 * | 1 byte  |   1 byte   | 1 byte each |
	 * 
	 * If you receive information about a node not in your successor list, add it - careful to maintain ordering in the array list
	 * 
	 * 
	 * Is dead update - a broadcast from a node, alerting other nodes of the death of the included nodes
	 * Command: (int) 36
	 * 
	 * | command | # of nodes |    nodes    | 
	 * | 1 byte  |   1 byte   | 1 byte each |
	 * 
	 */
}
