package com.manoj.concurrent.server.stats;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
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
    private ConcurrentHashMap<String, String> seenIntegers= new ConcurrentHashMap<>();

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

    /**
     * Stop and shutdown period task executor
     */
    public void stopPeriodicTasks(){
        if (executorService != null) {
            log.info("Shutting down periodic executor service");
            executorService.shutdownNow();
            executorService = null;
        }
        if(taskFuture != null) {
            taskFuture.cancel(true);
            taskFuture = null;
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
        }
    };


    /**
     * Get new input integer and update the values; If the integer has been seen before
     * it will return back a true if not false. Update other metrics for this period
     * @param input
     * @return
     */
    public boolean updateIntegersAndCheckDupe(String input) {
        log.debug("Updating statistics...");
        if (seenIntegers.containsKey(input)) {// found a duplicate
            this.newDuplicates.getAndIncrement();
            return true;
        } else {
            this.seenIntegers.put(input, "");// add to the map
            this.uniqueIntegers.getAndIncrement();// not a duplicate so update unique integers
            this.totalIntegers.getAndIncrement(); // Update total unique count of integers since app start
        }
        log.debug("Finished updating statistics.");
        return false;
    }
}
