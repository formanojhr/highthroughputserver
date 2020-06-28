package com.newrelic.codingchallenge;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MainTest {
    private static final Logger log = LoggerFactory.getLogger(MainTest.class);
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private static int SERVER_SOCKET = 9000;
    @Test
    public void testMultiThreaded() throws InterruptedException, IOException {

        // Test something here (optional)
        int numSockets = 5;
        log.info("Creating new sockets of no. 5...");
        System.out.println("Creating 5 sockets on port 4000");
        Socket socket1 = new Socket("localhost", SERVER_SOCKET);
        Socket socket2 = new Socket("localhost", SERVER_SOCKET);
        Socket socket3 = new Socket("localhost", SERVER_SOCKET);
        Socket socket4 = new Socket("localhost", SERVER_SOCKET);
        Socket socket5 = new Socket("localhost", SERVER_SOCKET);

//        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 4000);
//        Socket socket = new Socket("localhost", 4000);
        log.info("Generating 4 threads for generating traffic on the 5 sockets...");
        Thread t1 = new Thread(new SocketTrafficGen(socket1));
        Thread t2 = new Thread(new SocketTrafficGen(socket2));
        Thread t3 = new Thread(new SocketTrafficGen(socket3));
        Thread t4 = new Thread(new SocketTrafficGen(socket4));
        Thread t5 = new Thread(new SocketTrafficGen(socket5));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        log.info("Sleeping test thread for 60 mins...");
        Thread.sleep(TimeUnit.MINUTES.toMillis(60));
    }


    @Test
    public void testMaxingConnections() throws InterruptedException, IOException {

        // Test something here (optional)
        int numSockets = 5;
        log.info("Creating new sockets of no. 5...");
        System.out.println("Creating 5 sockets on port 4000");
        Socket socket1 = new Socket("localhost", SERVER_SOCKET);
        Socket socket2 = new Socket("localhost", SERVER_SOCKET);
        Socket socket3 = new Socket("localhost", SERVER_SOCKET);
        Socket socket4 = new Socket("localhost", SERVER_SOCKET);
        Socket socket5 = new Socket("localhost", SERVER_SOCKET);
        Socket socket6 = new Socket("localhost", SERVER_SOCKET);

        log.info("Generating 4 threads for generating traffic on the 5 sockets...");
        Thread t1 = new Thread(new SocketTrafficGen(socket1));
        Thread t2 = new Thread(new SocketTrafficGen(socket2));
        Thread t3 = new Thread(new SocketTrafficGen(socket3));
        Thread t4 = new Thread(new SocketTrafficGen(socket4));
        Thread t5 = new Thread(new SocketTrafficGen(socket5));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        log.info("Sleeping test thread for 60 mins...");
        Thread.sleep(TimeUnit.MINUTES.toMillis(60));
    }


    @Test
    public void testDupes() throws InterruptedException, IOException {

        // Test something here (optional)
        System.out.println("Testing");

        Socket socket = new Socket("localhost", SERVER_SOCKET);
        socket.setKeepAlive(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        log.info("Connecting to Server on port ..." + SERVER_SOCKET);
        int min = 0;
        int max = 999999999;
        Integer randomNum;
        List<Integer> integerList = new ArrayList<>();
        integerList.add(421179925);
        integerList.add(533436692);
        integerList.add(533436692);
        integerList.add(421179925);

        integerList.forEach(integer -> {
            try {
                String message = integer.toString() + "\n";
                writer.write(message);
                log.info("sending: " + message);
                writer.flush();
            } catch (Exception e){
                log.error("",e);
            }
        });

        Thread.sleep(2000);
    }

    @Test
    public void testTerminateCommand() throws InterruptedException, IOException {
        // Test something here (optional)
        System.out.println("Testing");

        Socket socket = new Socket("localhost", SERVER_SOCKET);
        socket.setKeepAlive(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        log.info("Connecting to Server on port "+ SERVER_SOCKET);
        int min = 0;
        int max = 999999999;
        Integer randomNum;
        String message = null;

        for (int i = 0; i < 3; i++) {
            if(i < 2) {
                randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                message = randomNum.toString() + "\n";
            } else {
                log.info("Sending terminate message");
                 message= "terminate" + "\n";
            }
            byte[] messageBytes = message.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
            writer.write(message);
            log.info("sending: " + message);
            buffer.clear();
            writer.flush();
            // wait for 2 seconds before sending next message
            Thread.sleep(1000);
        }
    }


    @Test
    public void testSingleThreaded() throws InterruptedException, IOException {

        // Test something here (optional)
        System.out.println("Testing");

        Socket socket = new Socket("localhost", SERVER_SOCKET);
//        SocketChannel socketChannel = SocketChannel.open(socketAddress);
        socket.setKeepAlive(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        log.info("Connecting to Server on port ..."+SERVER_SOCKET);
        int min = 0;
        int max = 999999999;
        Integer randomNum;

//        for (int i = 0; i < 10; i++) {
        while(true){
            randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
            String numbWithEsc = randomNum.toString() + "\n";
            byte[] message = randomNum.toString().getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            writer.write(numbWithEsc);
            log.info("sending: " + numbWithEsc);
            buffer.clear();
            writer.flush();
            // wait for 2 seconds before sending next message
            Thread.sleep(2000);
        }
    }

    public class SocketTrafficGen implements Runnable {
        private Socket socket;

        int min = 0;
        int max = 999999999;
        Integer randomNum;

        public SocketTrafficGen(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                socket.setKeepAlive(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//        for (int i = 0; i < 10; i++) {
                while (true) {
                    randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                    String numbWithEsc = randomNum.toString() + "\n";
                    byte[] message = randomNum.toString().getBytes();
                    ByteBuffer buffer = ByteBuffer.wrap(message);
                    writer.write(numbWithEsc);
                    log.info("sending: " + numbWithEsc);
                    buffer.clear();
                    writer.flush();
                    // wait for 2 seconds before sending next message
                    Thread.sleep(10);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}