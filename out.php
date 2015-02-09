<?php

$str = "Filesystem            Size  Used Avail Use% Mounted on \n
 /dev/hdv1             9.6G  181M  9.2G   2% /";


//because reasons
$str = trim(strstr(trim(strstr(trim(strstr( $str, "\n" ))," ")), " ", TRUE));
echo $str."\n";

$str = "41M       .";
//gets 
$str = strstr($str, " ", TRUE);

echo $str."\n";


?>