<?php


$host    = trim($argv[1]);
$port    = 4003;
$password = trim($argv[2]);
$text = trim($argv[3]);
$message = $password.'--!--'.$text;
echo "Message To server:  ".$text."\n";
// create socket
$socket = socket_create(AF_INET, SOCK_STREAM, 0) or die("Could not create socket\n");
// connect to server
$result = socket_connect($socket, $host, $port) or die("Could not connect to server\n");  
// send string to server
socket_write($socket, $message, strlen($message)) or die("Could not send data to server\n");
// get server response
$result = socket_read ($socket, 1024) or die("Could not read server response\n");
echo "Reply From Server\n---------------------\n";
echo $result."\n---------------------\n";
// close socket
socket_close($socket);
?>