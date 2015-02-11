<?php
// set ip and port
$host = "0.0.0.0";
$port = 4003;
// don't timeout!
set_time_limit(0);
// create socket
$socket = socket_create(AF_INET, SOCK_STREAM, 0);
// bind socket to port
$result = socket_bind($socket, $host, $port) or die("Could not bind port, please try again later\n");
// start listening for connections
$result = socket_listen($socket, 3);

echo trim(shell_exec('hostname'))."\n";
echo "Port: $port\n";
echo "----------------\n";
while(1)
{
    // accept incoming connections
    // spawn another socket to handle communication
    $spawn = socket_accept($socket) or die("Could not accept incoming connection\n");
    // read client input
    $input = socket_read($spawn, 1024) or die("Could not read input\n");
    $input = trim($input);
   

    $password = substr($input, 0, strpos($input, "--!--"));
    $command = trim(substr($input, strpos($input, "--!--")+5));


    //echo $input."\n";
    //echo $password."\n";
    //echo $command."\n";
    //echo trim(hash('md5', $password))."\n";
    //echo trim(file_get_contents('pass.txt'))."\n";


    if( trim(hash('md5', $password)) == trim(file_get_contents('pass.txt')) )
    {
        $output = shell_exec(trim($command).' 2>&1');
        echo 'executed: '.$command."\n";
    } 
    else
    {
        $output = "Wrong password!";
        echo "bad password\n";
    }
    //echo $output."\n";

    //echo "Client Message : ".$input."\n";
    // reverse client input and send back
    //$output = strrev($input) ."\n";
    if(!socket_write($spawn, $output, strlen ($output)))
    {
        socket_close($socket);
        socket_close($spawn);
        die("Could not write to socket\n");

    }
    // close sockets

}

socket_close($spawn);
socket_close($socket);
?>