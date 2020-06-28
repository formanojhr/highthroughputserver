package com.newrelic.codingchallenge.stats;


import com.sun.javafx.font.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A reporting service which maintains the statistics around the application's received numbers and aggregated
 * metrics and prints every 10 seconds the report summary as per requirement
 * Every 10 seconds, the Application must print a report to standard output:
 * The difference since the last report of the count of new unique numbers that have been received.
 * The difference since the last report of the count of new duplicate numbers that have been received.
 * The total number of unique numbers received for this run of the Application.
 * Example text for #8: Received 50 unique numbers, 2 duplicates. Unique total: 567231
 * @author mramakrishnan
 */
public class PeriodicReportingService {
    private static final Logger log = LoggerFactory.getLogger(PeriodicReportingService.class);
    private static final int DEFAULT_METRICS_HARVEST_TIME_INTERVAL = (int) TimeUnit.SECONDS.toSeconds(10);
    private static final long INITIAL_DELAY = TimeUnit.SECONDS.toSeconds(5);
    private ScheduledExecutorService executorService;
    private Future<?> taskFuture;
    private AtomicInteger uniqueIntegers = new AtomicInteger(0);// total number of unique integers
    private AtomicInteger totalIntegers = new AtomicInteger(0);// total integers
    private AtomicInteger newDuplicates = new AtomicInteger(0);// new duplicate numbers since last run
    private AtomicInteger newUniqueNos = new AtomicInteger(0);// new unique numbers since last run
    private HashSet<String> currentSetIntegers = new HashSet<String>();
    private AtomicBoolean isRun = new AtomicBoolean(true);

    /**
     * A start method to kick off the scheduled task for collecting statistics and printing
     */
    public void start() {
        log.info("Starting Periodic metrics collector's task");
        executorService = Executors.newScheduledThreadPool(1);
        taskFuture = executorService
                .scheduleAtFixedRate(periodicStatsTask, INITIAL_DELAY, DEFAULT_METRICS_HARVEST_TIME_INTERVAL, TimeUnit.SECONDS);
        log.info("Periodic metrics collector service started and will collect the metrics periodically " +
                "for every {} seconds", DEFAULT_METRICS_HARVEST_TIME_INTERVAL);
    }

    public void stopPeriodicTasks(){
        if (executorService != null) {
            log.info("Shutting down periodic executor service");
            executorService.shutdownNow();
            executorService = null;
        }
        if(taskFuture != null) {
            taskFuture.cancel(true);
            taskFuture = null;
//            this.isRun.set(false);
        }
    }

    /**
     * Periodic  statistics printout task
     */
    private final Runnable periodicStatsTask = new Runnable() {
        @Override
        public void run() {
            try {
                logStatistics();
            } catch (Exception e) {
                log.error("Exception in the periodic reporting thread", e);
            }
        }

        private void logStatistics() {
            log.info("Received {} unique numbers, {} duplicates. Unique total: {}",uniqueIntegers.get(),
                    newDuplicates.get(), totalIntegers.get());
            //now reset values
            uniqueIntegers.set(0);
            newDuplicates.set(0);
            synchronized (currentSetIntegers) {// synchronize and clear the hashset
                currentSetIntegers.clear();
            }
        }
    };


    /**
     * Get new input integer and update the values
     * @param input
     */
    public void incrementUniqueIntegers(String input) {
        synchronized (this.currentSetIntegers) {// sync
            log.debug("Updating statistics...");
            int newInt = Integer.parseInt(input);
            if (currentSetIntegers.contains(newInt)) {// found a duplicate
                this.newDuplicates.getAndIncrement();
            } else {
                this.currentSetIntegers.add(input);// add to the hashset
                this.uniqueIntegers.getAndIncrement();// not a duplicate so update unique integers
                this.totalIntegers.getAndIncrement(); // Update total unique count of integers since app start
            }
            log.debug("Finished updating statistics.");
        }
    }

}
