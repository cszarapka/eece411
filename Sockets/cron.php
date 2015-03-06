<?php
$output = shell_exec( "ps -ef | grep /etc/bin/php ~/ServerForAss3.php");
$res = stristr( $output, "00:00:00 php ServerForAss3.php");
if( $res == false )
{
	system( "nohup php ServerForAss3.php > /dev/null 2>&1 &" );
}
?>