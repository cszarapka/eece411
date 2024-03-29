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
$successorList = array();
$successorListNum = array();

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
    *   0x22    return list successors
    *   0x23    request to enter the hash table
    *   0x24    request for server status (alive)
    *   0x25    return list of all files locally (unhashed or not)
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
    //string format after messageID:
    //  "[successorNum1] [successorNum2] [successorNum3] [successor1] [successor2] [successor3]
    // i.e.
    //   "24 pl1.cs.ubc.ca 32 pl2.cs.ubc.ca 36  pl3.cs.ubc.ca"
    elseif($command == 22){
        $response = $messageID.pack('H',"0").$successorListNum[0]." ".$successorList[0]." ".$successorListNum[1]." ".$successorList[1]." ".$successorListNum[2]." ".$successorList[2];
    }
    elseif($command == 23) {
        /*
         * Respond to opcode: 0x23, a request to enter the hash table
         * Action to take: cut our range in half, send the requesting node
         * the upper half, and add that node to our successor list.
         * The response follows this format:
         *      $messageID . $opCode . $newLowerRange . $upperRange
         */
        // Cut the range in half
        $newLowerRange = ceil(upperRange/2);

        // Assemble the response
        $response = $messageID;
        $response = $response.pack('H', '23');
        $response = $response.pack('I', $newLowerRange);
        $response = $response.pack('I', $upperRange);

        // Cut our own range
        $upperRange = $newLowerRange;

        // Add this node to our successor list; bump the current 1st and 2nd to 2nd and 3rd respectively
        $successorList[2] = $successorList[1];
        $successorList[1] = $successorList[0];
        $successorList[0] = $remoteIP;
        $successorListNum[2] = $successorListNum[1];
        $successorListNum[1] = $successorListNum[0];
        $successorListNum[0] = $upperRange;
    }
    //returns a generic message proving that the server is alive
    elseif($command == 24){
            $response = $messageID.pack('H',"0");
    }
    // returns list of ALL files locally
    elseif($command == 25){
        //get a list of all files in local database
        $fileList = scanDir("~/database");
        var_dump($fileList);
        
        //assemble the response
        $response = $messageID;
        
        if($fileList != false){
            $response = $response.pack('H',"0");
            for($i = 2; $i < count($fileList); $i++){
                   $response = $response.substr($fileList[$i],0,32);
            }
        }
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