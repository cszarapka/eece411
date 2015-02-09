<?php

// location of all nodes to test
$nodesText = file_get_contents('nodes.txt');
$nodesArray = explode ( "\n" , $nodesText );

// create array of nodes
foreach( $nodesArray as $key => $slice )
{
	$nodesArray[$key] = trim($slice);
}

// loop to continuously poll
while(true){
	// location to hold all the most up to date results
	// file format: hostname|status|diskUsed|diskAvailable|upTime|averageLoad|lastTimeInfoUpdated\n
	// status is defined as: 2 -> server is live and can be logged in to
	//						 1 -> server is alive but cannot be logged in to
	//						 0 -> server is dead and cannot be logged in to
	file_put_contents('results.txt',"");
	foreach( $nodesArray as $key => $slice )
	{
		// if ping is successful, connection occurs and commands are sent through ssh to the node
		if( ping($slice) )
		{

			echo 'CONNECTION: '.$slice."\n\n";
			//ssh in, try to set up php, get disk used, get disk available, get uptime
			$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "sudo yum install php -y && du -s -h && df -h && uptime"';
			$result = shell_exec($command." 2>&1");
			echo 'Result: '.$result."\n";
			echo stristr($result, 'Setting up');
			//trim du, df, uptime commands to usable strings
			$lines = explode("\n", $result);
			$diskUsed = trim(strstr($lines[4], ".", TRUE));
			$diskAvail = trim(strstr(trim(strstr($lines[6], " ")), " ", TRUE));
			$upTime = strstr(trim(strstr(strstr($lines[7], "up "), " "), " "),",  ", TRUE);
			$loadAverage = trim(strstr(strstr($lines[7], ": "), " "));
			$updateTime = trim(strstr(trim($lines[7]), " ", TRUE));
			// if the machine properly logs in, it will try to set up php - if it is successful, it will run other commands to determine other critical data

			if(stristr($result, 'Setting up') != false)
			{
				echo "LOGGED IN\n\n";
				file_put_contents("results.txt",$slice.'|2|'.$diskUsed.'|'.$diskAvail.'|'.$upTime.'|'.$loadAverage.'|'.$updateTime."\n", FILE_APPEND);
			} else { //otherwise, it will return slice, status 1
				echo "NOLOGIN \n\n";
				file_put_contents("results.txt",$slice."|1|n/a|n/a|n/a|n/a|n/a\n", FILE_APPEND);
			}
		}
		else
		{
			echo 'NO CONNECTION: '.$slice."\n\n";
			file_put_contents( 'results.txt', $slice."|0|n/a|n/a|n/a|n/a|n/a\n", FILE_APPEND );
			//WRITE $SLICE TO RESULTS.TXT + OFFLINE

		}
	}

	echo "-----------CYCLE COMPLETE-----------\n\n\n\n\n\n\n\n\n\n";
	// copy results computed to a complete file, so entire node status is available at all times
	copy('results.txt', 'currServerStatus.txt');
}

// Tries to ping the server, to see if it is active.
function ping($host)
{
		//windows version
		exec(sprintf('ping -n 1 -w 2 %s', escapeshellarg($host)), $res, $rval);
		//unix version
        //exec(sprintf('ping -c 1 -W 2 %s', escapeshellarg($host)), $res, $rval);
        return $rval === 0;
}



?>
