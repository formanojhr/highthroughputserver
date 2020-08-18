# High throughputserver


Design Considerations
=====================
•	Maximum throughput This comes down to a few aspects of design and testing form experience. 
o	Optimal threading design to avoid threads spent time in blocked state on I/O, monitors etc 
o	Usage Appropriate Concurrency datastructures and locking levels for Java code
o	JVM parameters tuning (Optimal JVM collector algo etc)
o	Resources (Memory /CPU)

•	Server Socket acceptor thread logic is under TCPSocketServer. This thread’s main responsibility is to listen to client connection requests and hand over managing I/O for each client connection to a new thread.  Prevents any long running client read preventing other connection requests from being blocked. This thread listens infinitely until a terminate command is received. This maintains a state to understand the total number of client connections added to implement req 1 (at most 5 connections)
•	Client Socket I/O thread: The design uses a one thread per established client socket connection model. Reading from an established client connection is handled by IncomingMessageHandler.
•	Log writer : Since logging in another I/O work (other than reading client socket) it is done in another thread asynchronously (LogWriter) and order is not important in what order it goes to log based on requirement. A runnable thread that does the following:
o	Shares a LinkedBlockingQueue between the IncomingMessageHandler and the LogFileWriter in the producer consumer pattern.
o	This is done to make the writing to log file an asynchronous task to avoid blocking the incoming message listening thread from the client
•	Periodic StatisticsService
o	This  service’s primary responsibilities are twofold	
1.	 Keep up to date the 3 metrics requested. 
•	Each of the client reading processing will update this service when a valid 9 digit message is read. 
•	Concurrency choices: Maintains a ConcurrentHashMap(Key is the integers seen, value is just a placeholder empty sting)) to check for dupes through the entire lifetime of the app. (The assumption here is that this is not a production so it is fine since this map could contain large amount of unique integers seen in history). Needs more calculation here. Java util concurrency packages are used to maintain the lowest level of atomicity of code e.g. AtomicInteger etc. At max 5 threads could be updating the state here. This service is ready for more thread concurrency.
2.	Periodic printing of report to the standard out is done by a scheduled harvest task.   At the end of the harvest 10 sec cycle the period level stat values are set back to 0.
•	private final Runnable periodicStatsTask = new Runnable() {
    @Override
    public void run() {
        try {
            logStatistics();
        } catch (Exception e) {
            log.error("Exception in the periodic reporting thread", e);
        }
    }


Other Considerations: 
Java NIO was considered but since the maximum connections is restricted and the general understanding is that java nio helps scale and optimize for more client connections (100s or more avoid maxing threads handling client connection and read requests). In a typical production scenario for 100s/1000s of client requests NIO scales the best. 


This exercise was written to show off threading design in java.

Build instructions
==================
The given build instructions works as they are

Build
======
./gradlew build -x test (This skips the tests since the test are more of integration tests and needs the server to be running)

Run the TCP server
==================
From the root of the project run(include the used JVM GC tuning flags):
java -jar ./build/libs/coding-challenge-shadow.jar -Xms256m -Xmx2048m -XX:ParallelGCThreads=4 -XX:+UseParallelGC -XX:GCTimeRatio=9
Should start the server logging as below:
22:42:01.546 [main] INFO  com. - Starting TCP server....
22:42:01.549 [main] INFO  c.n.* - Opening file for logging numbers.log
22:42:01.549 [main] INFO  c.n.* - File numbers.log exists; Clearing it.
22:42:01.550 [main] INFO  c.n.c.server.TCPSocketServer - Starting log writer..
22:42:01.551 [main] INFO  c.n.c.stats.PeriodicReportingService - Starting Periodic metrics collector's task
22:42:01.552 [main] INFO  c.n.c.stats.PeriodicReportingService - Periodic metrics collector service started and will collect the metrics periodically for every 10 seconds
22:42:01.552 [main] INFO  c.n.c.server.TCPSocketServer - Starting shutdown threads..
22:42:01.552 [main] INFO  c.n.c.server.TCPSocketServer - Starting server in port 4000
22:42:01.558 [main] INFO  c.n.c.server.TCPSocketServer - Started server. Server listening in port 4000


Every 10 seconds should log below statistics
22:42:16.556 [pool-2-thread-1] INFO  c.n.c.stats.PeriodicReportingService - Received 0 unique numbers, 0 duplicates. Unique total: 0
                                          
Requirements
============
The Application must accept input from at most 5 concurrent clients on TCP/IP port 4000.

Input lines presented to the Application via its socket must either be composed of exactly nine decimal digits (e.g.: 314159265 or 007007009) immediately followed by a server-native newline sequence; or a termination sequence as detailed in #9, below.

Numbers presented to the Application must include leading zeros as necessary to ensure they are each 9 decimal digits.

The log file, to be named "numbers.log”, must be created anew and/or cleared when the Application starts.

Only numbers may be written to the log file. Each number must be followed by a server-native newline sequence.

No duplicate numbers may be written to the log file.

Any data that does not conform to a valid line of input should be discarded and the client connection terminated immediately and without comment.

Every 10 seconds, the Application must print a report to standard output:

The difference since the last report of the count of new unique numbers that have been received.

The difference since the last report of the count of new duplicate numbers that have been received.

The total number of unique numbers received for this run of the Application.

Example text for #8: Received 50 unique numbers, 2 duplicates. Unique total: 567231

If any connected client writes a single line with only the word "terminate" followed by a server-native newline sequence, the Application must disconnect all clients and perform a clean shutdown as quickly as possible.
Coding Challenge Build Framework

## Starter build framework for the coding challenge

First, you do not need to use this starter framework for your project.
If you would rather use a different build system (maven, javac, ...)
you are free to so long as you provide clear commands to build your
project and start your server.  Failure to do so will invalidate your
submission.


## Install Java

This coding challenge is in Java so it is recommended you install Java
1.8 from Oracle.


## Gradle

The build framework provided here uses gradle to build your project
and manage your dependencies.  The `gradlew` command used here will
automatically download gradle for you so you shouldn't need to install
anything other than java.


### Project Layout

All source code should be located in the `src/main/java` folder.
If you wish to write any tests (not a requirement) they should be
located in the `src/test/java` folder.

A starter `Main.java` file has been provided in the `com/newrelic/codingchallenge` package under `src/main/java`.


### Dependencies

If your project has any dependencies you can list them in the
`build.gradle` file in the `dependencies` section.


### Building your project from the command line

To build the project on Linux or MacOS run the command `./gradlew build` in a shell terminal.  This will build the source code in
`src/main/java`, run any tests in `src/test/java` and create an output
jar file in the `build/libs` folder.

To clean out any intermediate files run `./gradlew clean`.  This will
remove all files in the `build` folder.


### Running your application from the command line

You first must create a shadow jar file.  This is a file which contains your project code and all dependencies in a single jar file.  To build a shadow jar from your project run `./gradlew shadowJar`.  This will create a `codeing-challenge-shadow.jar` file in the `build/libs` directory.

You can then start your application by running the command
`java -jar ./build/lib/coding-challenge-shadow.jar`


JetBrains provides
a community edition of IDEA which you can download and use without
charge.

If you are planning to use IDEA you can generate the IDEA project files
by running `./gradlew idea` and directly opening the project folder
as a project in idea.
