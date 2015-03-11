Assignment 4 - Group 11

Our node list contains one node as we were only able to correctly implement the DHT to work on one node.

Design Process:

The overall design of our distributed hash table is to create an initial table with only one node, and to then launch all other nodes and have them join. When any node goes down, it will be relaunched by a cron job on the server and attempt to join a table. When a node joins, the node that invites it into the table gives the newly joined node half its range. The new node then gets all the files within its range and begins servicing requests. During normal operation, a request is received and if its key is in the receiving node's range it is responded to. If not, the request is passed along to the node that services that range. In the background, a thread pings other nodes in its list to see if they are online. It also asks for their successor list to determine if there are any new node joins that it doesn't already know about. Files are stored locally in a thread-safe data structure. When any node detects another node is down, it removes the dead node from its successor list. 

The strategy we use for testing is to issue for a generated key the following commands to a node in the table: get, put, get, remove, get. This is used to test for correct command usage and key lookup.

Our performance was impossible to test, as the table does not work.

How we distribute the code:

We use Ansible (a command line utility for mass ssh) as well as transer.php to push our JAR file and other required files to all our nodes as well as to update our cron job that reboots server.jar.

Required files:
server.jar
nodes.txt
javaCheck.sh

Cron Job:
*/5 * * * * sh /home/ubc_eece411_5/javaCheck.sh

Our cron job runs a shell script that checks for our java process and acts accordingly, either exiting if it finds the process ID, or reboots it otherwise.

