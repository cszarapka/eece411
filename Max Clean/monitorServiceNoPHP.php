<?php

// location of all nodes to test
$nodesText = trim(file_get_contents('nodes.txt'));
$nodesArray = explode ( "\n" , $nodesText );
$nodeNum = sizeof($nodesArray);
// create array of nodes
foreach( $nodesArray as $key => $slice )
{
	$nodesArray[$key] = trim($slice);
}

// loop to continuously poll
while(true){
	$i=1;
	// location to hold all the most up to date results
	// file format: hostname|status|diskUsed|diskAvailable|upTime|averageLoad|lastTimeInfoUpdated\n
	// status is defined as: 2 -> server is live and can be logged in to
	//						 1 -> server is alive but cannot be logged in to
	//						 0 -> server is dead and cannot be logged in to
	file_put_contents('results.txt',"");
	echo "--------------------------------\n";
	foreach( $nodesArray as $key => $slice )
	{
		echo "Node $i out of $nodeNum\n";	
		// if ping is successful, connection occurs and commands are sent through ssh to the node
		if( ping($slice) )
		{
			echo 'CONNECTION: '.$slice."\n";
			//ssh in, try to set up php, get disk used, get disk available, get uptime
			$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "df -h && du -s -h && uptime && php --version"';
			$result = shell_exec($command." 2>&1");
			//echo 'Result: '.$result."\n";
			//echo stristr($result, 'Filesystem');

			// if the machine properly logs in, it will try to set up php - if it is successful, it will run other commands to determine other critical data

			if(stristr($result, 'Filesystem') != false)
			{
				$result = stristr($result, 'Filesystem');
				echo "LOGGED IN\n";
				//trim du, df, uptime commands to usable strings
				$lines = explode("\n", $result);
				//print_r($lines);
				$diskUsed = trim(strstr($lines[2], ".", TRUE));
				$diskAvail = trim(strstr(trim(strstr($lines[1], " ")), " ", TRUE));
				$loadAverage = trim(strstr(strstr($lines[3], ": "), " "));
				$updateTime = trim(strstr(trim($lines[3]), " ", TRUE));
				
				$upTimeTemp = trim(strstr(strstr($lines[3], "up "), " "), " ");
				$upTimeTemp = explode(",", $upTimeTemp);
				//print_r($upTime);
				if(strpos ( $upTimeTemp[0] , "days")){
					$upTime = trim($upTimeTemp[0])." ".trim($upTimeTemp[1]);
				} else{
					$upTime = trim($upTimeTemp[0]);
				}

				if(strstr( $result, "php: command not found" ))
				{
					$php = "No PHP";

				}
				else
				{
					$php = trim(stristr(stristr( $result, "PHP" ), "(cli)", TRUE));
					
				}
				//echo "\n-----\n".$php."\n-----\n";

				//file_put_contents("results.txt",$slice.'|2|'.$diskUsed.'|'.$diskAvail.'|'.$upTime.'|'.$loadAverage.'|'.$updateTime.'|'.$php."\n", FILE_APPEND);

				$resString = $slice.'|2|'.$diskUsed.'|'.$diskAvail.'|'.trim($upTime).'|'.$loadAverage.'|'.$updateTime.'|'.$php."\n";
				echo $resString;
				file_put_contents("results.txt", $resString, FILE_APPEND);	
				//*/

			} else { //otherwise, it will return slice, status 1
				echo "NOLOGIN \n";
				file_put_contents("results.txt",$slice."|1|n/a|n/a|n/a|n/a|n/a|n/a\n", FILE_APPEND);
			}
		}
		else
		{
			echo 'NO CONNECTION: '.$slice."\n";
			file_put_contents( 'results.txt', $slice."|0|n/a|n/a|n/a|n/a|n/a|n/a\n", FILE_APPEND );
			//WRITE $SLICE TO RESULTS.TXT + OFFLINE

		} 
		echo "--------------------------------\n";
		$i++;
	}

	echo "-----------CYCLE COMPLETE-----------\n\n\n";
	// copy results computed to a complete file, so entire node status is available at all times
	copy('results.txt', 'currServerStatus.txt');
}

// Tries to ping the server, to see if it is active.
function ping($host)
{
		//windows version
		//exec(sprintf('ping -n 1 -w 2 %s', escapeshellarg($host)), $res, $rval);
		//unix version
        exec(sprintf('ping -c 1 -W 2 %s', escapeshellarg($host)), $res, $rval);
        return $rval === 0;
}



?>
