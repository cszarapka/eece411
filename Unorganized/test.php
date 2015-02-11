<?php

$command = 'ssh -o StrictHostKeyChecking=no -l ubc_eece411_5 -i ~/.ssh/id_rsa planetlab2.cs.ubc.ca "wget -O jre.rpm http://javadl.sun.com/webapps/download/AutoDL?BundleId=101397  > /dev/null 2>&1 &"';
system($command);

?>