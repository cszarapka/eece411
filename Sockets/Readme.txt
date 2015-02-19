EECE 411 - Assignment 3

Group 11, Slice 5 (ubc_eece411_5):
22936116 - Cam Szarapka
15678113 - Max Parker
45866100 - Ryan Clarke
22689129 - Stephan Bouthot

3 locations our service is running:
	1. 142.103.2.2 : 4003
	2. 142.103.2.1 : 4003
	3. 131.247.2.248 : 4003

In the two files included with this readme you will find our server and client code.


/* Design Choices */
We decided to do 

/* Testing */
Correctness:
	We ensured the correctness of our system through extensive use of the "echo" function (we are using PHP) on the client and server side, and active monitoring of the terminals of each node. Before each command is sent by the client we ensure that the location being delivered to is as entered, the command is correct, and the data to be sent is correct. Upon receival at the server, we verify this information by outputting all the same fields. Finally, at the client, we ensure the correctness of the response from the server is as expected. Due to output at every step of the process of communication, when an error did occur, it was quite easy to trace its origin.
	To ensure that the commands were working correctly, we ran tests of the following: get, put, get, remove, get. We expected: non-existent, successful, proper value, successful, nonexistent. The test file is called Testing.php and runs these many times with random keys and values.
Performance:
	Using the Time command in terminal we ran our client as "Time [command]"
	When the client finished running, we had learned how long it takes to get a reply from our server: 12 - 15ms. 

/* Performance Characterization Summary */

Testing.php:

<?php
$ip = $argv[1];
for($i = 0;$i < $argv[2];$i++) {
	$key = generateRandomString();
	$value = generateRandomString();
	echo "\n\n\n";
	echo "testing key ".$key." and value ".$value;
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." put ".$key." \"".$value."\"");
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." remove ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);

}





function generateRandomString($length = 64) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}

?>