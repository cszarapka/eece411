<?php


$host    = trim($argv[1]);
$port    = 4003;
$command = trim($argv[2]);
$key = str_pad(trim($argv[3]), 64, '0', STR_PAD_LEFT);
if($command == "put") {
	$value = trim($argv[4]);
	$valueLength = strlen($value);
	$message = "01".$key.str_pad($valueLength,4,'0',STR_PAD_LEFT).$value;
} elseif ($command == "get") {
	$message = "02".$key;
} elseif ($command == "remove") {
	$message = "03".$key;
} else {
	die("Command not recognized");
}
echo "Message To server:  ".$message."\n";
// create socket
$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
// connect to server
$result = socket_connect($socket, $host, $port) or die("Could not connect to server\n");  
// send string to server
socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
// get server response
$result = socket_read ($socket, 1024) or die("Could not read server response\n");
$returnStatus = intval(substr($result,0,2),10);
if($returnStatus == '00') {
	echo "Operation Successful\n";
} elseif (returnStatus == '01') {
	echo "Non-existent Key\n";
} elseif (returnStatus == '02') {
	echo "Out of space in filesystem\n";
} elseif (returnStatus == '03') {
	echo "System Overload\n";
} elseif (returnStatus == '04') {
	echo "Internal KVStore failure\n";
} elseif (returnStatus == '05') {
	echo "Unrecognized command\n";
} 
if($command == "get") {
	$resultLength = intval(substr($results,2,2),16);
	$resultContents = substr($results,6,$resultLength);
	echo "The file contents were:\n".$resultContents;
}
echo "Reply From Server\n---------------------\n";
echo $result."\n---------------------\n";
// close socket
socket_close($socket);
?>