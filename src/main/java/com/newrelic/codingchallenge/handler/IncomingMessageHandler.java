package com.newrelic.codingchallenge.handler;

import com.newrelic.codingchallenge.constants.Constants;
import com.newrelic.codingchallenge.log.LogWriter;
import com.newrelic.codingchallenge.stats.PeriodicReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * A message handler thread which will handle receiving and processing bytes for each of the client sockets.
 * Each handler corresponds to one thread.
 */
public class IncomingMessageHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(IncomingMessageHandler.class);
    private final Socket socket;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final LogWriter logWriter;
    private BlockingQueue<String> fileWriterQueue;
    private Pattern validPattern= Pattern.compile("\\d{9}");/// valid pattern for 9 digit number
    private PeriodicReportingService periodicReportingService;
    private final AtomicBoolean orderShutdown;


    /**
     * A constructor for incoming message handling
     * @param socket
     * @param logWriter
     * @param fileWriterQueue
     * @param periodicReportingService
     * @param orderShutdown
     */
    public IncomingMessageHandler(Socket socket, LogWriter logWriter, BlockingQueue fileWriterQueue, PeriodicReportingService periodicReportingService, AtomicBoolean orderShutdown) {
        this.socket = socket;
        this.logWriter = logWriter;
        this.fileWriterQueue = fileWriterQueue;
        this.periodicReportingService = periodicReportingService;
        this.orderShutdown = orderShutdown;
    }

    @Override
    public void run() {
        try {
            log.debug("Running message handler thread...");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request;
            while(true){// keep reading continuously
                if((request = in.readLine()) != null) {
                    processMessageAndWriteToLog(request);
                }
            }
        } catch (IOException e) {
            log.error("Error processing message", e);
            if (e instanceof SocketException) {
                //TODO Update a socket manager list to remove this used socket
                try {
                    log.error("Closing the socket since it is a SocketException for socket Local Add {} Remote Add {}", socket.getLocalAddress(),
                            socket.getRemoteSocketAddress());
                    socket.close();
                } catch (IOException ex) {
                    log.error("Error closing socket Local Add {} Remote Add {}", socket.getLocalAddress(),
                            socket.getRemoteSocketAddress());
                }
            }
        }
    }

    /**
     * Process the request and add to the log writer queue for async writing to file
      * @param msgString
     */
    private void processMessageAndWriteToLog(String msgString) {
        log.debug("Processing message " + msgString);
        if(msgString.contains(Constants.TERMINATE_CMD)){
            log.info("Got a shutdown message :" + msgString);
            this.orderShutdown.set(true);
        } else {
            try {
                if (validPattern.matcher(msgString).matches()) {
                    this.fileWriterQueue.put(msgString);// add to file queue shared with
                    this.periodicReportingService.incrementUniqueIntegers(msgString);
                } else {
                    log.debug("Message {} not matching expected pattern is being dropped", msgString);
                }
            } catch (Exception e) {
                log.error("Error adding message to log writer queue", e);
            }
        }
    }
}
