package com.manoj.concurrent.server.log;

import com.manoj.concurrent.server.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.BlockingQueue;


/**
 *  A log writer which initializes a file named numbers.log in application's root path and provide
 *  a queue based mechanism to write to a file. It is single threaded to avoid corruption.
 */
public class LogWriter extends Thread {
    private static final Logger log = LoggerFactory.getLogger(LogWriter.class);
    private BlockingQueue<String> fileWriterQueue;
    private File logFile;
//    private AtomicBoolean isRunning;
    /**
     * Constructor creates the file if it does not exist or opens the file.
     * @param fileWriterQueue
     */
    public LogWriter(BlockingQueue fileWriterQueue) {
        log.info("Opening file for logging {}", Constants.LOG_FILE_NAME);
        logFile = new File(Constants.LOG_FILE_NAME);
        this.fileWriterQueue = fileWriterQueue;
        try {
            if (!logFile.exists()) {
                log.info("File does not exist; Creating a new file named {}", Constants.LOG_FILE_NAME);
                logFile.createNewFile();
            } else {
                log.info("File {} exists; Clearing it.",Constants.LOG_FILE_NAME);
                new PrintWriter(Constants.LOG_FILE_NAME).close();
            }
        } catch (IOException e) {
            log.error("Error creating opening the file {}", Logger.ROOT_LOGGER_NAME, e);
        }
    }

    @Override
    public void run() {
//        isRunning = true;
        BufferedWriter oWriter = null;
        while (true) {
            try {
                if (fileWriterQueue.isEmpty()) {
//                    log.debug("Queue is empty; Nothing to write to file.");
                } else {
                    log.debug("Queue is not empty; Writing to file.");
                    String number = fileWriterQueue.take();
                    oWriter = new BufferedWriter(new FileWriter(Constants.LOG_FILE_NAME, true));
                    oWriter.write(number + "\n");
                }
            } catch (Exception e) {
                log.error("Exception writing to log file", e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {// close the file
                try {
                    if (oWriter != null) {
                        oWriter.close();
                    }
                } catch (IOException e) {
                    log.error("Error closing the file {}", Logger.ROOT_LOGGER_NAME, e);
                }
            }
        }
    }

    /**
     * Adds a file appending job
     * @param message
     * @throws InterruptedException
     */
    public void writeToFile(String message) throws Exception {
        this.fileWriterQueue.put(message);
    }
}
