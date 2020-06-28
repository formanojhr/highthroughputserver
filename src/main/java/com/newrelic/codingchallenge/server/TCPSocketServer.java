package com.newrelic.codingchallenge.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.newrelic.codingchallenge.constants.Constants;
import com.newrelic.codingchallenge.handler.IncomingMessageHandler;
import com.newrelic.codingchallenge.log.LogWriter;
import com.newrelic.codingchallenge.stats.PeriodicReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP connection server which satisfies the following requirements
 * immediately followed by a server-native newline sequence; or a termination sequence as detailed in #9,
 * below.
 * Listens in port 4000 for client connections. Once accepted a connection hands over the socket to a dedicated
 * Thread class (@link {@link IncomingMessageHandler}) for I/O for that socket
 * @author mramakrishnan
 */
public class TCPSocketServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(TCPSocketServer.class);
    private static int DEFAULT_SERVER_PORT=4000;
    private int port;
    private ServerSocket serverSocket;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicInteger connectionCount = new AtomicInteger(0);// Maintain total connection count for server
    private LogWriter logWriter;
    private List<Socket> socketList;// a list of connection
    private final BlockingQueue fileWriterQueue = new LinkedBlockingQueue();
    private final PeriodicReportingService periodicReportingService;
    private int  maxConnWarnCount = 0;
    private AtomicBoolean orderShutdown; // a boolean to maintain order shutdown by terminate command

    public TCPSocketServer(){
         this.port=DEFAULT_SERVER_PORT;
         socketList = Collections.synchronizedList(new ArrayList<>());// Maintain a list of socket list. This uses synchronized list since
        // the terminate does not
         this.logWriter = new LogWriter(fileWriterQueue);
         log.info("Starting log writer..");
         logWriter.start();
         this.periodicReportingService = new PeriodicReportingService();
         this.periodicReportingService.start();
         this.orderShutdown = new AtomicBoolean(false);
         Thread shutdownThread = new Thread(new ShutdownTask());
        log.info("Starting shutdown threads..");
         shutdownThread.start();

    }

    /**
     * A shutdown task which will scan for shutdown order from a client and starts the process
     */
    public class ShutdownTask implements Runnable {
        public void run() {
            while(true) {
                if (orderShutdown.get()) {
                    log.info("Detected shutdown command ");
                    terminateAllClientConnections();
                    terminateWorkerThreads();
                    log.info("Exiting shutdown task");
                    break;
                }
            }
        }
    }

    /**
     * Starts a server listening for  client connections in the default port.
     */
    public void startServer() throws IOException {
        log.info("Starting server in port {}", port);
        try {
            serverSocket = new ServerSocket(port);
            this.start();
            log.info("Started server. Server listening in port {}", port);
        } catch (IOException e) {
            log.error("Error starting the server socket", e);
            throw e;
        }
    }

    @Override
    public void run() {
        // Set running flag to true
        this.running.set(true);
        Socket socket = null;
        // if flag is set to listen, keep listening for client connection request and keep accepting connections
        while (this.running.get()) {
            try {
                if (connectionCount.get() < Constants.MAX_CLIENT_CONNECTIONS) {
                    socket = serverSocket.accept();
                    socket.setSoTimeout(30000); // timeout set to 30,000 ms
                    socket.setKeepAlive(true);
                    log.info("Connection Accepted: Local Add {} Remote Add {}", socket.getLocalAddress(),
                            socket.getRemoteSocketAddress());
                    // add the connected socket to the list
                    socketList.add(socket);
                    // Pass the socket to the RequestHandler thread for processing
                    IncomingMessageHandler messageHandler = new IncomingMessageHandler(socket, logWriter, fileWriterQueue,
                            periodicReportingService, orderShutdown);
                    messageHandler.start();
                    connectionCount.getAndIncrement();// increment connection count
                    log.debug("Current number of connections {}", connectionCount.get());
                } else {
                    if(maxConnWarnCount < 4) {
                        log.warn("Reached maximum connection limit {}; Stopping to accept more client connections"
                                , connectionCount.get());
                        maxConnWarnCount++;
                    }
                }
            } catch (IOException e) {
                log.error("Error in accepting connections", e);
                if (e instanceof SocketException) {
                    log.error("SocketException for remote connection");
                }
            }
        }
    }

    /**
     * Terminate all connections and shutdown server
     */
    private void terminateAllClientConnections() {
        log.info("Terminate command received; Closing sockets..");
        log.info("Attempting closing client sockets..");
        this.socketList.forEach(socket -> {
            try {
                socket.close();
            } catch (Exception e) {
                log.error("Error closing client socket address", socket.getRemoteSocketAddress().toString());
            }
        });
        log.info("Attempting closing server listening socket");
        try {
            this.serverSocket.close();
            this.running.set(false);
        } catch (Exception e) {
            log.error("Error closing server socket port", port);
        }
    }

    /**
     * Terminating all worker threads
     */
    private void terminateWorkerThreads() {
        log.info("Terminating worker threads..");
        try {
            this.periodicReportingService.stopPeriodicTasks();
            this.running.set(false);
            this.logWriter.join();
            this.join();
        } catch (Exception e) {
            log.error("Error shutting down ", port);
        }
    }
}
