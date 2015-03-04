<?php
// set ip and port
$host = "0.0.0.0";
$port = 4003;
$VERBOSE = true;


// don't timeout!
set_time_limit(0);
// create socket
$socket = socket_create(AF_INET, SOCK_DGRAM, 0);
// bind socket to port
$result = socket_bind($socket, $host, $port) or die("Could not bind port, please try again later\n");
// start listening for connections
//$result = socket_listen($socket, 3);
$lowerRange = 0; //later we will get the range by joining the node network and requesting a node number
$upperRange = 255;

//create successor list
$successorList = [];
    

echo trim(shell_exec('hostname'))."\n";
echo "Port: $port\n";
echo "----------------\n";
while(1)
{
    
    /* Incoming Message format: [16 byte unique message ID]
    *                           [1 byte command]
    *                           [32 byte key]
    *                           [2 byte value length]
    *                           [variable length value]
    *
    *  Outgoing Message format: [16 byte unique message ID]
    *                           [1 byte response]
    *                           [2 byte value length]
    *                           [variable length value]
    */      
    
    // accept incoming connections
    // spawn another socket to handle communication
    //$spawn = socket_accept($socket) or die("Could not accept incoming connection\n");
    
    // read client input
    socket_recvfrom($socket, $buffer, 2048, 0, $remoteIP, $remotePort);
    $input = $buffer;//trim($buffer);
    
    // parse the data coming in
    $messageID = substr($input,0,16);
    $messageArray = unpack('H*',substr($input,16,1));
    $command = $messageArray[1];
    $key = substr($input,17,32);
    if($command == 1) {
        $array = unpack('v',substr($input,49,2));
        $valueLength = $array[1];
        $value = substr($input,51,$valueLength);
    }
    
    //hash the key, get the first two characters, which represent an int between 0 and 255
    $hashKey = intval(substr(hash('md5',$key),0,2),16);

    //print relevant data for testing
    if($VERBOSE) {
        echo "\n------------------------------------------------------";
        echo "\nMessage Recieved: ".$input;
        echo "\nMessage ID:       ".$messageID;
        echo "\nCommand:          ".$command;
        echo "\nKey:              ".$key;
        echo "\nKey Hash:         ".$hashKey;
        if($command == 1) {
            echo "\nValue Length:     ".$valueLength; 
            echo "\nValue:            ".$value;   
        }
        echo "\n------------------------------------------------------";
    }
    
    /* 
    *   Command List:
    *   0x01    put                                             [filename][file contents]
    *   0x02    get                                             [filename]
    *   0x03    remove                                          [filename]
    *   0x21    return list of all files hashing within range   
[successor list]    
    *   0x22    return list successors
    */
    
    //list all files that hash 
    if($command == 21) 
    {
        
        //get a list of all files in local database
        $fileList = scandir("~/database");
        var_dump($fileList);
        //assemble the response
        $response = $messageID;
        
        //directory exists
        if($fileList != false) {
            $response = $response.pack('H',"0");
            echo "\nFiles: \n";
            for($i = 2; $i < count($fileList); $i++)
            {
                $hashKey = intval(substr(hash('md5',$fileList[$i]),0,2),16);
                if(($lowerRange < $upperRange and $lowerRange < $hashKey and $upperRange >= $hashKey) 
            or ($lowerRange > $upperRange and ($lowerRange > $hashKey or $upperRange <= $hashKey))) {
                    $response = $response.substr($fileList[$i],0,32);
                }
            }
        } else {
            $response = $response.pack('H',"1");
        }
    } 
    //space delimited list of ip's of successors
    elseif($command == 22){
        $response = $messageID.pack('H',"1").$successorList[0]." ".$successorList[1]." ".$successorList[2];
    }
        
    
    //check if the value is in the range serviced by this node
    elseif(($lowerRange < $upperRange and $lowerRange < $hashKey and $upperRange >= $hashKey) 
        or ($lowerRange > $upperRange and ($lowerRange > $hashKey or $upperRange <= $hashKey))) 
    {
        
        //put operation
        if($command == 1) 
        { 
            //write value to file
            //assemble response if operation successful
            if(file_put_contents("~/database/".$key.".txt",$value) == true) 
            {
                $response = $messageID.pack('H',"0"); 
            } 
            
            //assemble response if out of space
            else 
            {
                $response = $messageID.pack('H',"2"); //operation successful response
            }

        } 
        
        //get operation
        elseif ($command == 2) 
        { 
            
            //get contents from file
            $filecontents = file_get_contents("~/database/".$key.".txt");
            
            //assemble response if operation successful
            if($filecontents != false) {
                $response = $messageID.pack('H',"0").pack('v',strlen($filecontents)).$filecontents; 
            } 
            
            //assemble response if key not found
            else 
            {
                $response = $messageID.pack('H',"1"); 
            }

        } 
        
        //remove operation
        elseif ($command == 3) 
        { 
            
            //remove file matching key
            //response if operation successful
            if(unlink("~/database/".$key.".txt") == true) 
            {
                $response = $messageID.pack('H',"0"); 
            } 
            
            //response if file not found
            else 
            {
                $response = $messageID.pack('H',"1"); 
            }
        
        }
        
    //pass message on to the next node    
    } else {
        
    }

    if($VERBOSE) {
        echo "\n------------------------------------------------------";
        echo "\nMessage Sent:";
        echo "\n".$response;
        echo "\n------------------------------------------------------";
    }
    
    //send the response
    socket_sendto($socket, $response, strlen($response), 0, $remoteIP, $remotePort);

}

//exit gracefully
socket_close($spawn);
socket_close($socket);


?>