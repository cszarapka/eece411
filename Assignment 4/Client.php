<?php


$host    = trim($argv[1]);
$port    = 4003;
$command = trim($argv[2]);
$key = str_pad(trim($argv[3]), 32, '0', STR_PAD_LEFT);
$messageID = generateRandomString(16);

// assemble message
if($command == "put") {
	$value = trim($argv[4]);
	$valueLength = pack('v',strlen($value));
	$message = $messageID.pack('H*',"01").$key.str_pad($valueLength,2,'0',STR_PAD_LEFT).$value;
} elseif ($command == "get") {
	$message = $messageID.pack('H*',"02").$key;
} elseif ($command == "remove") {
	$message = $messageID.pack('H*',"03").$key;
} elseif ($command == "fileList") {
    $message = $messageID.pack('H*',"21");
} else {
	die("Command not recognized");
}
echo "\nMessage To server:  ".$message."\n";

// create socket
$socket = socket_create(AF_INET, SOCK_DGRAM, 0) or die("Could not create socket\n");

// send string to server
socket_sendto($socket, $message, strlen($message), 0x00,$host, $port) or die("Could not send data to server\n");

// get server response
socket_recvfrom($socket, $result, 2048, 0,$a, $b) or die("Could not read server response\n");

//parse server response
$messageID = substr($result,0,16);
$responseArray = unpack('H',substr($result,16,1));
$returnStatus = $responseArray[1];
if($command == "get" and $returnStatus == 0) {
    $array = unpack('v',substr($result,17,2));
    $valueLength = $array[1];
    $value = substr($result,19,$valueLength);
} elseif($command == "fileList" and $returnStatus == 0) {
    $filenamesString = substr($result,17);
    $filenames = array();
    for($i = 0; $i < strlen($filenamesString) / 32; $i++) {
        $filenames[] = substr($filenamesString, 32 * $i, 32);
    }
}

// print results
echo "\n------------------------------------------------------";
echo "\nMessage Recieved: ".$result;
echo "\nMessage ID:       ".$messageID;
echo "\nStatus:           ".$returnStatus;
if($command == "get" and $returnStatus == 0) {
    echo "\nValue Length:     ".$valueLength; 
    echo "\nValue:            ".$value;   
} elseif($command == "fileList" and $returnStatus == 0) {
    for($i = 0;$i < count($filenames); $i++) {
        echo "\nFile:             ".$filenames[$i];
    }
}
echo "\n------------------------------------------------------";



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