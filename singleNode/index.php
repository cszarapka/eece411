<!DOCTYPE html>
  <html>
  <body>

    <h1>Node Status</h1>


    <table border="1">
      <tr>
        <th>Node</th>
        <th>Node Status</th>
        <th>Login status</th>
        <th>Disk space used</th>
        <th>Disk space available</th>
        <th>Uptime/</th>
        <th>Load Average</th>
        <th>Access Time (UTC)</th>
        <th>PHP Version</th>
      </tr>

        <?php
          //echo "hi\n";

          $nodesText = trim(file_get_contents('currServerStatus.txt'));

          /*
          echo "<pre>";
          echo "hi\n";
          echo $nodesText;
          echo "</pre>";
          */

          $nodesArray = explode ( "\n" , $nodesText );
        
          echo '<pre>Group 11 - EECE411 - Assignment 2</pre>';

          
          foreach( $nodesArray as $key => $slice )
          {
            echo '<tr>';
            $nodesArray[$key] = trim($slice);
            $nodeSpaceArray = explode("|",$nodesArray[$key]);
            echo '<th>'.$nodeSpaceArray[0].'</th>';
            if($nodeSpaceArray[1] == 2){
              $status = "Online";
              $logInStatus = "Able to log in";
              echo '<th bgcolor="#40FF00">'.$status.'</th>';
              echo '<th bgcolor="#40FF00">'.$logInStatus.'</th>';
            } elseif ($nodeSpaceArray[1] == 1) {
              $status = "Online";
              $logInStatus = "Not able to log in";
              echo '<th bgcolor="#40FF00">'.$status.'</th>';
              echo '<th bgcolor="#FF0000">'.$logInStatus.'</th>';
            } else{
              $status = "Offline";
              $logInStatus = "Not Able to log in";
              echo '<th bgcolor="#FF0000">'.$status.'</th>';
              echo '<th bgcolor="#FF0000">'.$logInStatus.'</th>';
            }

            echo '<th>'.$nodeSpaceArray[2].'</th>';
            echo '<th>'.$nodeSpaceArray[3].'</th>';
            echo '<th>'.$nodeSpaceArray[4].'</th>';
            echo '<th>'.$nodeSpaceArray[5].'</th>';
            echo '<th>'.$nodeSpaceArray[6].'</th>';
            echo '<th>'.$nodeSpaceArray[7].'</th>';
            echo '</tr>';
          }
          //print_r($nodesString);

        ?>
      

    </table>

  </body>
</html>


