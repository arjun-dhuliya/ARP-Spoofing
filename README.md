ARP-SPOOFING :-
README: -
Author: Shailesh Vajpayee, Arjun Dhuliya

Files included are: -
ArpPacket.java, ArpPacketAnalyzer.java, Attacker.java, User.java, ChatBox.java, Router.java

A minimum of 4 machines are required to demonstrate ARP Spoofing.

ArpPacket.java and ArpPacketAnalyzer.java are required on every machine.

User.java and ChatBox.java are required at client.

To compile the codes:-
javac *.java

To run the codes: -
java “Filename”

1. Run Router.java to create router in one machine
   Assigns logical ips to the users to future communication

2. Run User.java to create first user in second machine
   program need router ip and port as parameters

3. Run User.java to create second user in third machine
   program need router ip and port as parameters

   ChatBox UI is generated. The Logical IP that you are assigned is shown in IP text box, starts from 192.168.1.2
   if you want to communicate with other user just change ip to the logical ip of the receiver and hit update button
   type a message and hit send message will be received by intended user.

4. Run Attacker.java to create a instance of attacker
   program need router ip and port as parameters
   It gets the arp cache from router.
   User is required to select which user you want to target
   Once you select the victim program initiates spoof attack and attacker gets all the messages from victim without its
   knowledge
