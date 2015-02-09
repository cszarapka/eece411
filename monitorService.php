<?php
//error_reporting(0);
$nodesText = file_get_contents("nodes.txt");
//DELETE NODES.TXT
//CREATE RESULTS.TXT

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
		if( $ping1 == '' )
		{
			$ping1 = $slice;
			//WRITE $SLICE TO RESULTS.TXT + ONLINE
		}
		elseif( $ping2 == '' )
		{
			$ping2 = $slice;
			//WRITE $SLICE TO RESULTS.TXT + ONLINE
			break;
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
file_put_contents( 'nodes.txt', $array1 );
//upload ssh shit


echo "\n\n\n";

$array2String = trim( implode("\n", $array2) );
echo $array2String;
file_put_contents( 'nodes.txt', $array2 );
//upload ssh shit




//CREATE NODES.TXT
//PUT FRONT HALF OF REMAINING NODES ARRAY IN NODES.TXT
//SEND NODES.TXT TO PING1
//SEND THIS PHP SCRIPT TO PING1
//DELETE NODES.TXT
//CREATE NODES.TXT
//PUT BACK HALF OF REMAINING NODES ARRAY IN NODES.TXT
//SEND NODES.TXT TO PING2
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
