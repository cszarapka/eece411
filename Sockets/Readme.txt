EECE 411 - Assignment 3

Group 11, Slice 5 (ubc_eece411_5):
22936116 - Cam Szarapka
00000000 - Max Parker
45866100 - Ryan Clarke
00000000 - Stephan Bouthot

3 locations our service is running:
	1. <node> : <port>
	2. <node> : <port>
	3. <node> : <port>

In the two files included with this readme you will find our server and client code.


/* Design Choices */


/* Testing */
Correctness:
	We ensured the correctness of our system through extensive use of the "echo" function (we are using PHP) on the client and server side, and active monitoring of the terminals of each node. Before each command is sent by the client we ensure that the location being delivered to is as entered, the command is correct, and the data to be sent is correct. Upon receival at the server, we verify this information by outputting all the same fields. Finally, at the client, we ensure the correctness of the response from the server is as expected. Due to output at every step of the process of communication, when an error did occur, it was quite easy to trace its origin.
Performance:
	Using the Time command in terminal we ran our client as "Time [command]"
	When the client finished running, we had learned how long it takes to get a reply from our server: 15ms.

/* Performance Characterization Summary */