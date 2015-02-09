<?php
//error_reporting(0);
$hostname = trim(shell_exec('hostname'));
$nodesText = file_get_contents('nodes.txt');
//DELETE NODES.TXT
//CREATE RESULTS.TXT
$resultFile = file_put_contents('results.txt', $hostname."|2|");
$diskUsed = trim(shell_exec('du -s -h'));
$diskUsed = trim(strstr($diskUsed, ".", TRUE));

$diskAvail = trim(shell_exec('df -h'));
$diskAvail = trim(strstr(trim(strstr(trim(strstr( $diskAvail, "\n" ))," ")), " ", TRUE));

$upTime = trim(shell_exec('uptime'));
//echo $upTime."\n";

file_put_contents('results.txt', $diskUsed.'|'.$diskAvail.'|'.$upTime."\n" , FILE_APPEND);

//echo $nodesText."\n";

$nodesArray = explode ( "\n" , $nodesText );


foreach( $nodesArray as $key => $slice )
{
	$nodesArray[$key] = trim($slice);
}

//print_r($nodesArray);
$ping1 = '';
$ping2 = '';


foreach( $nodesArray as $key => $slice )
{
	//echo shell_exec( "ping -c 1 $slice" )."\n";
	if( ping($slice) )
	{
		echo 'Yes: '.$slice."\n\n";
		$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "sudo yum install php -y"';
		$result = shell_exec($command." 2>&1");
		echo 'Result: '.$result."\n";
		echo stristr($result, 'Setting up');
		if( $ping1 == '' and stristr($result, 'Setting up') != false)
		{
			echo 'Found Server';

			$ping1 = $slice;
			//WRITE $SLICE TO RESULTS.TXT + ONLINE
		} 
		else 
		{
			file_put_contents( 'results.txt', $slice."|1\n" , FILE_APPEND);
		}

		
		if( $ping1 != '' && $ping2 == ''  and stristr($result, 'Setting up') != false)
		{
			echo 'Found Server';
			$ping2 = $slice;
			//WRITE $SLICE TO RESULTS.TXT + ONLINE
			break;
		}
		elseif($ping1 != '') 
		{
			file_put_contents( 'results.txt', $slice."|1\n" , FILE_APPEND);
		}
		/*
		echo 'Yes: '.$slice."\n\n";
		$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "sudo yum install php -y"';
		system($command);
	
		*/
	}
	else
	{
		echo 'No: '.$slice."\n\n";
		file_put_contents( 'results.txt', $slice."|0\n", FILE_APPEND );
		//WRITE $SLICE TO RESULTS.TXT + OFFLINE

	}
}



echo $key."\n";



for($i = 0; $i < ((sizeof($nodesArray)-($key+1))/2); $i++) {
	$array1[$i] = $nodesArray[$i + $key + 1];
}
for($i;$i < sizeof($nodesArray)-($key+1); $i++) {
	$array2[$i] = $nodesArray[$i + $key + 1];
}

print_r($nodesArray);
print_r($array1);
print_r($array2);
//for( $i = )

$array1String = trim( implode("\n", $array1) );
echo $array1String;
file_put_contents( 'nodes.txt', $array1String );
//upload ssh shit
echo "\n";
$command = 'scp -i ~/.ssh/id_rsa \'nodes.txt\' id_rsa id_rsa.pub monitorService.php ubc_eece411_5@'.$ping1.':';
echo $command."\n";
system($command);
$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$ping1.' "php monitorService.php > /dev/null 2>&1 & && exit"';
system($command);

echo "\n\n\n";



$array2String = trim( implode("\n", $array2) );
file_put_contents( 'nodes.txt', $array2String );
echo $array2String;
$command = 'scp -i ~/.ssh/id_rsa \'nodes.txt\' id_rsa id_rsa.pub monitorService.php ubc_eece411_5@'.$ping2.':';
echo $command."\n";
system($command);
$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$ping2.' "php monitorService.php > /dev/null 2>&1 & && exit"';
system($command);
//file_put_contents( 'nodes.txt', $array2 );
//upload ssh shit



//$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "sudo yum install php -y"';



//SEND THIS PHP SCRIPT TO PING1


//SEND THIS PHP SCRIPT TO PING2

//SSH INTO PING1 AND INSTALL PHP AND RUN SCRIPT
//SSH INTO PING2 AND INSTALL PHP AND RUN SCRIPT

//SSH INTO ORIGINAL NODE AND COPY APPEND RESULTS.TXT TO ORIGINAL NODE'S RESULTS.TXT




function ping($host)
{
        exec(sprintf('ping -c 1 -W 2 %s', escapeshellarg($host)), $res, $rval);
        return $rval === 0;
}



?>
