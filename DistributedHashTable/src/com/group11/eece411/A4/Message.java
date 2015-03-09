package com.group11.eece411.A4;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a message used in the DHT
 * @author Group 11
 * @version a4
 */
public class Message {

	// The raw data received 
	private Byte[] rawData;

	// The fields to be extracted or inserted into the raw data:
	// Specific to professor's Wire Protocol:
	private Byte[] uniqueID = new Byte[16];
	private int command;
	private int echoedCommand;
	private int responseCode;
	private Byte[] key = new Byte[32];
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
	public Message() {

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
			// TODO: get all the fields that were sent to me
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

	public byte[] getUniqueId() {

	}
	
	public int getNodeNumber() {
		
	}
	
	public ArrayList<Successor> getSuccessorList() {
		
	}
	
	public byte[] getBuffer() {
		
	}
	
	public int getBufferLength() {
		
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
	 * | 1 byte  |     1 byte	  |     1 byte	    | 5 bytes each |      4 bytes     |          unsure....			  |
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
