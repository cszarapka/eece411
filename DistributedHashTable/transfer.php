<?php 
$nodes
=
explode(
"\n"
,
file_get_contents(
"activeNodes.txt"
)
)
;
for(
$i
=
0
;
$i
<
count(
$nodes
)
;
$i++
)
{
echo
shell_exec(
"scp Server.jar ubc_eece411_5@"
.$nodes[$i]
.":"
)
."Transferred to "
.$i
." of "
.count(
$nodes
)
."\n"
;
}
?>