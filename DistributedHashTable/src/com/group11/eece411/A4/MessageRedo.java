package com.group11.eece411.A4;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MessageRedo {

	// The raw data received 
	public byte[] rawData;

	public byte[] uniqueID;	// the unique ID of this message
	public byte[] ip;		// the ip address from the unique ID 
	public byte[] port;		// the port of the source host, from the unique ID

	public byte command;		// the command of this message
	public byte responseCode;	// response code of this message

	public byte offeredNodeNumber;	// offered node number of the message

	public byte[] key;	// key of the value to get, remove, or put
	public byte[] keys;	// keys of the values received from the message (invite to join)

	public byte valueLength;	// length of the value in this message
	public byte[] value;		// the value in this message (of the key/value pair)

	public static final int MESSAGE_TYPE_POSITION = 7;
	public static final int COMMAND_POSITION = 16;

	// Values appended to these messages for our own purposes:
	public Byte[] successorHostNames;
	public Byte[] successorNodeNumbers;

	// Message types
	public byte messageType;
	public static final int SEND_REQUEST 		= 0;
	public static final int SEND_RESPONSE 		= 1;
	public static final int SEND_UPDATE			= 2;
	public static final int RECEIVED_REQUEST 	= 3;
	public static final int RECEIVED_RESPONSE 	= 4;
	public static final int RECEIVED_UPDATE		= 5;

	/**
	 * Constructs an empty message
	 */
	public MessageRedo() {

	}

	public void setUniqueID(byte[] uniqueID) {
		this.uniqueID = uniqueID;
	}

	public void setCommand(int command) {
		this.command = intToByteArray(command)[0];
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = intToByteArray(responseCode)[0];
	}

	public void setOfferedNodeNumber(int offeredNodeNumber) {
		this.offeredNodeNumber = intToByteArray(offeredNodeNumber)[0];
	}


	private byte[] buildUniqueID(String hostName, int port, int messageType) {
		// Convert the string hostname to a byte array ip
		try {
			this.ip = Arrays.copyOf(InetAddress.getByName(hostName).getAddress(), 4);
		} catch (Exception e) {
			System.out.println("Message: Unknown host name in buildUniqueID");
			return null;
		}

		// Convert the port into a 2 byte array
		this.port = intToByteArray(port);

		// Generate the random byte
		Random rand = new Random();
		byte randomByte = (byte) rand.nextInt(255);
		
		// Get the message type
		this.messageType = intToByteArray(messageType)[0];

		// Get the time stamp
		byte[] timeStampBytes = longToByteArray(System.currentTimeMillis());

		// Put it all together
		byte[] returnValue = new byte[16];
		returnValue[0] = this.ip[0];
		returnValue[1] = this.ip[1];
		returnValue[2] = this.ip[2];
		returnValue[3] = this.ip[3];
		returnValue[4] = this.port[0];
		returnValue[5] = this.port[1];
		returnValue[6] = randomByte;
		returnValue[7] = this.messageType;
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

	public static byte[] intToByteArray(int i) {
		final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(i);
		return bb.array();
	}
	
	public static byte[] longToByteArray(long i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putLong(i);
	    return bb.array();
	}
}
