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
echo "Reply From Server\n---------------------\n";
switch ($result) {
	case "00":
		$outcome = "Success";
		break;
	case "01":
		$outcome = "Key not found";
		break;
	case "02":
		$outcome = "Out of space";
		break;
	default:
		$outcome = "Unknown response";
		break;
}
echo $result." - ".$outcome."\n---------------------\n";
// close socket
socket_close($socket);
?>