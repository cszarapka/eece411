<?php
error_reporting(0);
$i=1;

while(1)
{
	file_put_contents('addingPhpStatus.txt', "Run $i\n", FILE_APPEND);
	file_put_contents('missingPHP.txt', "");
	$currStatusString = trim(file_get_contents('currServerStatus.txt'));
	$currStatusArray = explode ( "\n" , $currStatusString );

	$q=0;
	$j=0;
      
	foreach( $currStatusArray as $key => $slice )
	{
		$q++;
		$nodeSpaceArray = explode("|",trim($slice));
		if($nodeSpaceArray[7]!='PHP 5.2.6')
		{
			$j++;
			file_put_contents('missingPHP.txt', $nodeSpaceArray[0]."\n", FILE_APPEND);
		}
	}
	file_put_contents('addingPhpStatus.txt', "$j of $q machines do not have PHP\n", FILE_APPEND);


	$nodesText = trim(file_get_contents("missingPHP.txt"));

	//echo $nodesText."\n";

	$nodesArray = explode ( "\n" , $nodesText );


	foreach( $nodesArray as $key => $slice )
	{
		$nodesArray[$key] = trim($slice);
	}

	//print_r($nodesArray);


	foreach( $nodesArray as $key => $slice )
	{
		//echo shell_exec( "ping -c 1 $slice" )."\n";
		if( ping($slice) )
		{
			echo 'Yes: '.$slice."\n\n";
			$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa '.$slice.' "sudo yum install php -y"';
			system($command);

		}
		else
		{
			echo 'No: '.$slice."\n\n";
		}
	}
	file_put_contents('addingPhpStatus.txt', "---------------------------------\n", FILE_APPEND);
}



function ping($host)
{
        exec(sprintf('ping -c 1 -W 2 %s', escapeshellarg($host)), $res, $rval);
        return $rval === 0;
}



?>
