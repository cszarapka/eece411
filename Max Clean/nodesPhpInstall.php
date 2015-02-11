<?php
error_reporting(0);
$nodesText = trim(file_get_contents("nodes.txt"));

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




function ping($host)
{
        exec(sprintf('ping -c 1 -W 2 %s', escapeshellarg($host)), $res, $rval);
        return $rval === 0;
}



?>
