<?php

$tableText = trim(file_get_contents('currServerStatus.txt'));
echo $tableText."\n";

$nodesText = explode( "\n", $tabelText );
foreach( $nodesText as $key => $nodeText )
{
	$nodes[$key] = explode( "|", trim($nodeText));
}






?>
