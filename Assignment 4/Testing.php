<?php
$ip = $argv[1];
$testToRun = $argv[2];
if($testToRun == '0') {
    for($i = 0;$i < $argv[3];$i++) {
        $key = generateRandomString();
        $value = generateRandomString();
        echo "\n\n\n";
        echo "testing key ".$key." and value ".$value;
        echo shell_exec("\nphp Client.php ".$ip." get ".$key);
        echo shell_exec("\nphp Client.php ".$ip." put ".$key." \"".$value."\"");
        echo shell_exec("\nphp Client.php ".$ip." get ".$key);
        echo shell_exec("\nphp Client.php ".$ip." remove ".$key);
        echo shell_exec("\nphp Client.php ".$ip." get ".$key);

    }
} elseif($testToRun == '1') {
    $key0 = generateRandomString();
    $key1 = generateRandomString();
    $key2 = generateRandomString();
    $key3 = generateRandomString();
    $key4 = generateRandomString();
    $value = generateRandomString();
    echo shell_exec("\nphp Client.php ".$ip." put ".$key0." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." put ".$key1." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." put ".$key2." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." put ".$key3." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." put ".$key4." \"".$value."\"");
    
    echo shell_exec("\nphp Client.php ".$ip." fileList");
    echo shell_exec("\nphp Client.php ".$ip." remove ".$key0." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." remove ".$key1." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." remove ".$key2." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." remove ".$key3." \"".$value."\"");
    echo shell_exec("\nphp Client.php ".$ip." remove ".$key4." \"".$value."\"");
} elseif($testToRun == '2'){
    echo shell_exec("\php Client.php ".$ip." successorList");   
}

echo "\n";
echo "\n";





function generateRandomString($length = 32) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}

?>