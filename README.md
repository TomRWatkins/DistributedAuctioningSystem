# Distributed Auctioning System

This is a distributed auctioning system built in Java using RMI and JGroups. This implementation allows for creating new backend servers on the fly to store and manage the state of the system. Active replication is used to ensure all replicas recieved auction requests and the state is conistent.

## Usage Server
In server dir run the following commands:
```bash
javac *.java
```
```bash
rmiregistry
```
Repeat N times to create N backend servers
```bash
java -cp "./jgroups-3.6.20.Final.jar":. -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 Backend
```
Run 1 frontend server
```bash
java -cp "./jgroups-3.6.20.Final.jar":. -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 Frontend
```
## Usage Client
In client dir run the following commands:
```bash
java ClientSeller
```
```bash
java ClientBuyer
```

