<?php
$ip = $argv[1];
for($i = 0;$i < $argv[2];$i++) {
	$key = generateRandomString();
	$value = generateRandomString();
	echo "\n\n\n";
	echo "testing key ".$key." and value ".$value;
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." put ".$key." \"".$value."\"");
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." remove ".$key);
	echo shell_exec("\nTime php ClientForAss3.php ".$ip." get ".$key);

}





function generateRandomString($length = 64) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}

?>