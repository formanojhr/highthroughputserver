# High throughputserver


Design Considerations
=====================
https://github.com/formanojhr/highthroughputserver/blob/master/HighThroughputServerDesignTestNotes.pdf

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



## Install Java
It is recommended you install Java 1.8 from Oracle.


## Gradle

The build framework provided here uses gradle to build your project
and manage your dependencies.  The `gradlew` command used here will
automatically download gradle for you so you shouldn't need to install
anything other than java.


### Project Layout

All source code should be located in the `src/main/java` folder.
If you wish to write any tests (not a requirement) they should be
located in the `src/test/java` folder.


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
