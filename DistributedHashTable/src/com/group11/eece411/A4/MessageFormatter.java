package com.group11.eece411.A4;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class MessageFormatter {
	
	private static byte GET = (byte) 1;
	private static byte PUT = (byte) 2;
	private static byte REMOVE = (byte) 3;
	private static byte SHUTDOWN = (byte) 4;
	private static byte GIVETOMEYOURSUCCESSORSLISTINGS = (byte) 34;
	
	/**
	 * Generates a byte array response
	 */
	public static byte[] createResponse(byte[] uniqueID, int responseCode, String value){
		byte[] code = ByteBuffer.allocate(4).putInt(responseCode).array();
		byte[] val = value.getBytes();
		byte[] retval = new byte[uniqueID.length + code.length + val.length];
		for(int i = 0; i < uniqueID.length; i++) {
			retval[i] = uniqueID[i];
		}
		for(int i = 0; i < code.length; i++) {
			retval[i + uniqueID.length] = code[i];
		}
		for(int i = 0; i < val.length; i++) {
			retval[i + uniqueID.length + code.length] = val[i];
		}
		return retval;	
	}
	
	public static byte[] getUniqueID(byte[] data) {
		return Arrays.copyOfRange(data, 0, 16);
	}
	
	public static int getCommand(byte[] data) {
		return (int)data[16];
	}
	
	public static byte[] getKey(byte[] data) {
		return Arrays.copyOfRange(data, 17, 32);
	}
	
	public static byte[] getValueRequest(byte[] data) {
		int valueLength = (int)data[49] << 8 | (int)data[50];
		return Arrays.copyOfRange(data, 51, 51+valueLength);
	}
	
	public static byte[] getValueResponse(byte[] data) {
		int valueLength = (int)data[17] << 8 | (int)data[18];
		return Arrays.copyOfRange(data, 19, 19+valueLength);
	}
	
	public static byte[] createRequest(int command, byte[] key, byte[] value) throws UnknownHostException{
		byte[] uniqueID = generateUniqueID();
		
		switch(command) {
		case 1: //put
		{
			byte[] request = new byte[16 + 1 + 32 + 2 + value.length];
			for(int i = 0; i < 16; i++) {
				request[i] = uniqueID[i];
			}
			request[17] = PUT;
			for(int i = 0; i < 32; i++) {
				request[i + 16 + 1] = key[i];
			}
			int valueLength = value.length;
			request[16 + 1 + 32 + 1] = (byte) (valueLength & 0xFF);
			request[16 + 1 + 32] = (byte) ((valueLength >> 8) & 0xFF);
			for(int i = 0; i < valueLength; i++ ) {
				request[i + 16 + 1 + 32 + 2] = value[i];
			}
			return request;
		}
			
		case 2: //get
		{
			byte[] request = new byte[16 + 1 + 32];
			for(int i = 0; i < 16; i++) {
				request[i] = uniqueID[i];
			}
			request[17] = GET;
			for(int i = 0; i < 32; i++) {
				request[i + 16 + 1] = key[i];
			}
		}
		case 3: //remove
		{
			byte[] request = new byte[16 + 1 + 32];
			for(int i = 0; i < 16; i++) {
				request[i] = uniqueID[i];
			}
			request[17] = REMOVE;
			for(int i = 0; i < 32; i++) {
				request[i + 16 + 1] = key[i];
			}
		}
			break;
			
		case 4: //shutdown
		{
			byte[] request = new byte[16 + 1 + 32];
			for(int i = 0; i < 16; i++) {
				request[i] = uniqueID[i];
			}
			request[17] = SHUTDOWN;
			for(int i = 0; i < 32; i++) {
				request[i + 16 + 1] = key[i];
			}
		}
			break;
		
		case 34: //give to me your successors listings
		{
			byte[] request = new byte[16 + 1 + 32];
			for(int i = 0; i < 16; i++) {
				request[i] = uniqueID[i];
			}
			request[17] = GIVETOMEYOURSUCCESSORSLISTINGS;
			for(int i = 0; i < 32; i++) {
				request[i + 16 + 1] = key[i];
			}
		}
			break;
			
		default:
			
		}
		return new byte[0];
		
	}
	
	public static byte[] generateUniqueID() {
		Random rand = new Random();
		byte[] retval = new byte[16];
		for(int i = 0; i < 16; i++) {
			retval[i] = (byte)rand.nextInt(256);
		}
		return retval;
	}
	
	
}