package com.manoj.concurrent.server;

import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LinkedBlockingQueueTest {


    @Test
    public void testAddingToQueue() throws InterruptedException {
        BlockingQueue<String> sharedQueue = new LinkedBlockingQueue<>();
        sharedQueue.put("Test1");

    }
}
