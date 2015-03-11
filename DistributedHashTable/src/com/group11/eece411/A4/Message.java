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
	public Byte[] rawData;

	public String hostName;
	public int port;

	// The fields to be extracted or inserted into the raw data:
	// Specific to professor's Wire Protocol:
	public Byte[] uniqueID = new Byte[16];
	public int command;
	public int echoedCommand;
	public int responseCode;
	public int nodeNumber;
	public Byte[] key = new Byte[32];
	public ArrayList<Byte[]> keys;
	public int valueLength;
	public Byte[] value;

	public Byte[] originIP = new Byte[4];
	public int originNodeNumber;

	public static final int MESSAGE_TYPE_POSITION = 7;
	public static final int COMMAND_POSITION = 16;

	// Values appended to these messages for our own purposes:
	public Byte[] successorHostNames;
	public Byte[] successorNodeNumbers;

	// Message types
	public int messageType;
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

		this.originIP = Arrays.copyOfRange(rawData, 0, 4);

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
		//case Message.RECEIVED_UPDATE: parseReceivedUpdateMessage();
		//break;
		}
	}

	/**
	 * Parses a received request-message, based on the command code.
	 * See the documentation at the bottom for protocol and 
	 * message format details.
	 */
	public void parseReceivedRequestMessage() {

		if (command == Codes.CMD_SHUTDOWN || command == Codes.REQUEST_TO_JOIN || command == Codes.GET_SUCCESSOR_LIST) {
			// nothing left to parse
			// command is either get, remove, put, or an echo of these
			return;
		}

		int commandToUse = command;
		int index = 17;

		// Check for an echoed command
		if (command == Codes.ECHOED_CMD) {
			originIP = Arrays.copyOfRange(rawData, index, index+4);
			echoedCommand = rawData[index+4].intValue();
			index += 5;
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

		if(command == Codes.GET_SUCCESSOR_LIST){
			// Get the number of successors following
			int numSuccessors = rawData[COMMAND_POSITION+1].intValue();
			final int BEGIN_SUCCESSORS = COMMAND_POSITION+2;

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
		}

		// Check for an invite to join message
		if (command == Codes.ADD_SUCCESSOR) {
			final int BEGIN_SUCCESSORS = COMMAND_POSITION+1;
			byte[] successorHostName = new byte[4];
			for(int k = 0; k < 4; k++){
				successorHostNames[k] = rawData[BEGIN_SUCCESSORS+k];
			}
			successorNodeNumbers[0] =rawData[BEGIN_SUCCESSORS+5];
			return;
		}

		int commandToUse = command;
		// Check for an echoed command
		if (command == Codes.ECHOED_CMD) {
			originIP = Arrays.copyOfRange(rawData, index, index+4);
			echoedCommand = rawData[index+4].intValue();
			index += 5;
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
		int numSuccessors = 0;
		if(successorNodeNumbers != null) {
			numSuccessors = successorNodeNumbers.length;
		}

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
		//System.out.println("getBuffer() rawData.length=" + rawData.length + " " + rawBytes.length);
		for (int i = 0; i < rawData.length; i++) {
			//System.out.println("\n" + i);
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
	 * @param command shutdown, request to join, or Are You Alive
	 */
	public boolean buildRequestMessage(int command) {
		// Set the unique ID
		setUniqueID(Message.SEND_REQUEST);

		// Set the command
		this.command = command;

		if (command == Codes.CMD_SHUTDOWN || command == Codes.REQUEST_TO_JOIN || command == Codes.GET_SUCCESSOR_LIST) {
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
	
	public boolean buildAddSuccessorMessage(int nodeNum) {
		
		setUniqueID(Message.SEND_REQUEST);
		this.command = Codes.ADD_SUCCESSOR;
		rawData = new Byte[16+2];
		for (int i = 0; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}
		rawData[16] = Byte.valueOf(intToByteArray(command)[0]);
		rawData[17] = (byte)(nodeNum & 0xFF);
		return true;
	}

	public void buildAppLevelRequestMessage(int command, Byte[] key) {
		// Set the unique ID
		//setUniqueID(Message.SEND_REQUEST);

		// set the command
		this.command = command;

		if (command == Codes.CMD_GET || command == Codes.CMD_REMOVE) {
			// add the key to the message
			this.key = key;
			rawData = new Byte[uniqueID.length + 1 + key.length];
			for (int i = 0; i < uniqueID.length;) {
				rawData[i] = uniqueID[i];
			}
			rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
			int index = uniqueID.length + 1;
			for (int i = index; i < key.length + index; i++) {
				rawData[i] = key[i-index];
			}
		}
	}


	/**
	 * Builds an echoed put request-message with the specified fields.
	 * Assembles the raw data to be sent.
	 * @param uniqueID			the unique ID to put at the front of this message
	 * @param originAddress 	the IP address of the node who started the request
	 * @param key 				the key of value to be put
	 * @param valueLength 		the length of the value to be put
	 * @param value 			the value to be put
	 */
	public void buildEchoedPutRequestMessage(Byte[] uniqueID, InetAddress originAddress, Byte[] key, int valueLength, Byte[] value) {
		command = Codes.ECHOED_CMD;
		echoedCommand = Codes.CMD_PUT;

		// Get the IP of the origin
		byte[] originIPbytes = originAddress.getAddress();
		// Do the stupid byte - Byte transfer.
		for (int i = 0; i < originIPbytes.length; i++) {
			this.originIP[i] = Byte.valueOf(originIPbytes[i]);
		}

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

		setUniqueID(uniqueID);
		int index = 0;
		rawData = new Byte[uniqueID.length + 1 + 4 + 1 + key.length + 1 + valueLength];

		// Add the unique ID
		for (int i = index; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}

		// Add the command
		rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
		index = uniqueID.length + 1;

		// Add the origin: 4byte IP
		for (int i = index; i < originIPbytes.length + index; i++) {
			rawData[i] = originIPbytes[i - index];
		}
		index = originIPbytes.length + index;

		// Add the echoed command (this is the original command from Matei)
		rawData[index] = Byte.valueOf(intToByteArray(echoedCommand)[0]);

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

	/**
	 * Builds an echoed GET or REMOVE request-message
	 * @param uniqueID		the unique ID to put at the front of this message
	 * @param originAddress the IP address of the node who started the request
	 * @param command 		either GET or REMOVE
	 * @param key 			the key of the value to get or remove
	 */
	public void buildEchoedAppLevelRequestMessage(Byte[] uniqueID, InetAddress originAddress, int command, Byte[] key) {
		if (command != Codes.CMD_GET || command != Codes.CMD_REMOVE) {
			System.out.println("Message: buildEchoedAppLevelRequestMessage:");
			System.out.println("You passed me a command I don't accept: " + command);
			return;
		}

		this.command = Codes.ECHOED_CMD;
		this.echoedCommand = command;

		// Get the IP of the origin
		byte[] originIPbytes = originAddress.getAddress();
		// Do the stupid byte - Byte transfer.
		for (int i = 0; i < originIPbytes.length; i++) {
			this.originIP[i] = Byte.valueOf(originIPbytes[i]);
		}

		// Get the key as a Byte array
		this.key = key;

		/*
		 * Assemble the raw data
		 */

		setUniqueID(uniqueID);
		int index = 0;
		rawData = new Byte[uniqueID.length + 1 + 4 + 1 + key.length];

		// Add the unique ID
		for (int i = index; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}

		// Add the command
		rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
		index = uniqueID.length + 1;

		// Add the origin: 4byte IP
		for (int i = index; i < originIPbytes.length + index; i++) {
			rawData[i] = originIPbytes[i - index];
		}
		index = originIPbytes.length + index;

		// Add the echoed command (this is the original command from Matei)
		rawData[index] = Byte.valueOf(intToByteArray(echoedCommand)[0]);

		// Add the key to be "put" to the raw data
		for (int i = index; i < key.length + index; i++) {
			rawData[i] = key[i-index];
		}
		index = key.length + index;

	}


	public void buildEchoedShutdownRequestMessage(Byte[] uniqueID, InetAddress originAddress, int command) {
		if (command != Codes.CMD_SHUTDOWN) {
			System.out.println("Message: buildEchoedShutdownRequestMessage:");
			System.out.println("You passed me a command I don't accept: " + command);
			return;
		}

		this.command = Codes.ECHOED_CMD;
		this.echoedCommand = command;

		// Get the IP of the origin
		byte[] originIPbytes = originAddress.getAddress();
		// Do the stupid byte - Byte transfer.
		for (int i = 0; i < originIPbytes.length; i++) {
			this.originIP[i] = Byte.valueOf(originIPbytes[i]);
		}

		/*
		 * Assemble the raw data
		 */

		setUniqueID(uniqueID);
		int index = 0;
		rawData = new Byte[uniqueID.length + 1 + 4 + 1 + key.length];

		// Add the unique ID
		for (int i = index; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}

		// Add the command
		rawData[uniqueID.length] = Byte.valueOf(intToByteArray(command)[0]);
		index = uniqueID.length + 1;

		// Add the origin: 4byte IP
		for (int i = index; i < originIPbytes.length + index; i++) {
			rawData[i] = originIPbytes[i - index];
		}
		index = originIPbytes.length + index;

		// Add the echoed command (this is the original command from Matei)
		rawData[index] = Byte.valueOf(intToByteArray(echoedCommand)[0]);
	}

	/*
	 * The building of the response messages
	 */

	public boolean buildWireResponseMessage(int command, int responseCode) {
		if (command == Codes.CMD_PUT || command == Codes.CMD_REMOVE || command == Codes.CMD_SHUTDOWN) {
			setUniqueID(Message.SEND_RESPONSE);
			this.command = command;
			this.responseCode = responseCode;

			rawData = new Byte[16+1];

			// assemble unique id and response
			for(int i = 0; i < 16; i++){
				rawData[i] = uniqueID[i];
			}

			rawData[16] = Byte.valueOf((byte) (responseCode & 0xFF));

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
		if(valueLength != 0){
			rawData = new Byte[16+1+4+valueLength];
		} else {
			rawData = new Byte[16+1];
		}
		for(int i = 0; i < 16; i++){
			rawData[i] = uniqueID[i];
		}
		rawData[16] = Byte.valueOf((byte) (responseCode & 0xFF));



		// something to send
		if(valueLength != 0 && value != null) {
			// put value length
			for(int i = 0; i < 4; i++){
				rawData[i+17] = Byte.valueOf(ByteBuffer.allocate(4).putInt(valueLength).array()[i]);
			}

			for(int i = 0; i < valueLength; i++){
				rawData[21+i] = value[i];
			}

			//put value
		}


	}

	/**
	 * builds a message to return a successor list in format:
	 * 1B	num successors
	 * 		then repeat for number of successors
	 * 4B 	successor address
	 * 1B 	successor nodeNum
	 * @param successors ArrayList of successors
	 */
	public void buildReturnSuccessors(ArrayList<Successor> successors){
		setUniqueID(Message.SEND_RESPONSE);
		// get number of successors

		int numSuccessors = 0;		

		if(!successors.isEmpty()) {
			numSuccessors = successors.size();
		} 

		rawData = new Byte[16+1+5*numSuccessors];
		for(int i = 0; i < 16; i++){
			rawData[i] = uniqueID[i];
		}

		this.responseCode = Codes.SUCCESS;
		rawData[16] = Byte.valueOf(intToByteArray(this.responseCode)[0]);

		rawData[17] = Byte.valueOf(intToByteArray(numSuccessors)[0]);

		int BEGIN_SUCCESSORS = 16;
		Byte[] nextSuccessorAddress = new Byte[4];
		// put data into successor message
		for(int i = 0; i < numSuccessors; i++){
			for(int k = 0; k < 4; k++){
				nextSuccessorAddress[k] = Byte.valueOf(successors.get(i).getInetAddress().getAddress()[k]);
			}

			for(int k = 0; k < nextSuccessorAddress.length; k++){
				rawData[BEGIN_SUCCESSORS+k+i*5] = nextSuccessorAddress[k];
			}

			// puts the node number into a byte[]
			Byte nextSuccessorNodeNum = Byte.valueOf(ByteBuffer.allocate(1).putInt(successors.get(i).getNodeNumber()).array()[0]);
			rawData[BEGIN_SUCCESSORS+i*5 + 4] = nextSuccessorNodeNum;
		}

	}
	
	/**
	 * Construct rawData so that can be sent as an invite to join the table.
	 * An invite is a response to a request to join the table.
	 * @param uniqueID			the unique ID of the request
	 * @param offeredNodeNumber	the node number to offer the node requesting to join
	 * @param successors		our successors
	 * @param keyListLength		number of keys we are sending them
	 * @param keys				the keys in our KVStore
	 */
	public void buildJoinResponse(Byte[] uniqueID, int offeredNodeNumber, ArrayList<Successor> successors, int keyListLength, byte[] keys) {
		// Set the unique ID
		this.uniqueID = uniqueID;
		
		// Set the command (response code)
		this.command = Codes.SUCCESS;
		
		// Set the node number we will offer the node
		this.nodeNumber = offeredNodeNumber;
		
		// Number of successors
		int numOfSuccessors = successors.size();
		
		// Create new Byte[] for the rawData
		rawData = new Byte[uniqueID.length + 1 + 1 + 1 + (5*numOfSuccessors) + keyListLength + (32*keyListLength)];
		
		/*
		 * Assemble the raw  data
		 */
		int index = 0;
		// Add the unique id
		for (int i = index; i < uniqueID.length; i++) {
			rawData[i] = uniqueID[i];
		}
		index = uniqueID.length;
		
		// Add the command/response code
		rawData[index] = Byte.valueOf(intToByteArray(this.command)[0]);
		index++;
		
		// Add the offered node number
		rawData[index] = Byte.valueOf(intToByteArray(this.nodeNumber)[0]);
		index++;
		
		// Add the number of successors
		rawData[index] = Byte.valueOf(intToByteArray(numOfSuccessors)[0]);
		//index++;
		
		// Only add successors if there are any to add
		byte[] address = new byte[4];
		for (int i = 0; i < numOfSuccessors; i++) {
			address = successors.get(i).getInetAddress().getAddress();
			rawData[++index] = address[0];
			rawData[++index] = address[1];
			rawData[++index] = address[2];
			rawData[++index] = address[3];
			rawData[++index] = Byte.valueOf(intToByteArray(successors.get(i).getNodeNumber())[0]);
		}
		index++;
		
		// Add the key list length
		rawData[index] = Byte.valueOf(intToByteArray(keyListLength)[0]);
		
		// Only add keys if there are any to add
		if (keyListLength > 0) {
			for (int i = 0; i < keys.length; i++) {
				rawData[++index] = keys[i];
			}
		}
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
		/*| command | offered node # | # of successors |  successors  | file list length | hashed file names (file list) |
		 * | 1 byte  |     1 byte	  |     1 byte	    | 5 bytes each |      4 bytes     |      up to 32 bytes each	  |
		 */ 
		int numSuccessors = 0;

		if(!successors.isEmpty()) {
			numSuccessors = successors.size();
		} 

		setUniqueID(Message.SEND_RESPONSE);
		
		rawData = new Byte[16+1+1+1+5*numSuccessors+4+32*keyListLength];

		for(int i = 0; i < 16; i++){
			rawData[i] = uniqueID[i];
		}

		this.responseCode = Codes.SUCCESS;
		rawData[16] = Byte.valueOf(intToByteArray(this.responseCode)[0]);


		rawData[17] = Byte.valueOf(intToByteArray(offeredNodeNumber)[0]);


		this.nodeNumber = offeredNodeNumber;
		rawData[18] = (byte)(numSuccessors & 0xFF);

		int BEGIN_SUCCESSORS = 19;
		Byte[] nextSuccessorAddress = new Byte[4];
		// put data into successor message
		for(int i = 0; i < numSuccessors; i++){
			for(int k = 0; k < 4; k++){
				nextSuccessorAddress[k] = Byte.valueOf(successors.get(i).getInetAddress().getAddress()[k]);
			}

			for(int k = 0; k < nextSuccessorAddress.length; k++){
				rawData[BEGIN_SUCCESSORS+k+i*5] = nextSuccessorAddress[k];
			}

			// puts the node number into a byte[]
			Byte nextSuccessorNodeNum = (byte)(successors.get(i).getNodeNumber() & 0xFF);
			rawData[BEGIN_SUCCESSORS+i*5 + 4] = nextSuccessorNodeNum;
		}

		
		int BEGIN_KEY_LIST_LENGTH = BEGIN_SUCCESSORS + numSuccessors*5;
		
		// now get key list length and put it into value
		int INT_LENGTH = 4;
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		b.putInt(keyListLength);
		byte[] result = b.array();
		
		for(int i = 0; i < INT_LENGTH; i++){
			rawData[BEGIN_KEY_LIST_LENGTH+i] = result[i]; 
		}

		int BEGIN_KEY_LIST = BEGIN_KEY_LIST_LENGTH+4;
		// get all key names and put into value

		for(int i = 0; i < keyListLength; i++){
			// copy key name
			for(int k = 0; k < 32; k++){
				rawData[BEGIN_KEY_LIST+i*32+k] = Byte.valueOf(keyNames[i*32+k]);
			}
		}	
	}


	/*
	 * The building of the update messages
	 */



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

	public void setUniqueID(Byte[] id) {

		for (int i = 0; i < Byte.SIZE; i++) {
			this.uniqueID[i] = id[i];
		}
	}

	private void setUniqueID(int messageType) {
		byte[] temp = generateUniqueID(messageType);
		this.messageType = messageType;
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
	private static String getIpAddress(byte[] rawBytes) {
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
