package com.group11.eece411.A4;

/**
 * A class to hold static variables for the command
 * codes used in the DHT
 * @author cam
 * @version a4
 */
public class Codes {
	// Application layer commands
	public static final int CMD_PUT 		= 1;
	public static final int CMD_GET 		= 2;
	public static final int CMD_REMOVE 		= 3;
	public static final int CMD_SHUTDOWN 	= 4;
	
	// Lower layer commands
	public static final int REQUEST_TO_JOIN		= 30;
	public static final int IM_LEAVING			= 31;
	public static final int GET_SUCCESSOR_LIST		= 32;
	public static final int ADD_SUCCESSOR		= 33;
	public static final int ECHOED_CMD			= 34;
	public static final int IS_ALIVE			= 35;
	public static final int IS_DEAD				= 36;
	
	// Response codes
	public static final int SUCCESS 				= 0;
	public static final int KEY_DOES_NOT_EXIST		= 1;
	public static final int OUT_OF_SPACE 			= 2;
	public static final int SYSTEM_OVERLOAD 		= 3;
	public static final int INTERNAL_FAILURE 		= 4;
	public static final int UNRECOGNIZED_COMMAND 	= 5;
}
