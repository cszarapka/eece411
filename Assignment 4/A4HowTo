1. Send a request to all of the nodes in our list until we find one that is in the table (we know it’s in the table because it will respond to us)
2. Receive your node # and your range from that node
3. Receive the file list of your predecessor (predecessor = node from step 1.)
4. Receive your successors from your predecessor
- at this point, the predecessor node adds this requesting node to its successor list, bumping the 3rd successor off its list
5. Receive all the files from your predecessor that fall within your range

This node is now in the DHT
There will now be 2 running threads.

Service Commands thread
respond to commands

Watchdog thread
Check if first successor is alive
if (dead)
Adjust upper range to equal second successor
Update successor list by getting new third successor from other successors
else (alive)
update successor list from successors #1 and #2

for each successor:
get file list
compare against current duplication
get any updated or new files
(for now, we can just replace all files)