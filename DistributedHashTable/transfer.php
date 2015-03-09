<?php $nodes = trim(explode('\n', file_get_contents("activeNodes.txt"))); for($i = 0; $i < count($nodes); $i++) 
{
echo "scp Server.jar ubc_eece411_5@".$nodes[$i].":"); 
}
sleep(3);?>