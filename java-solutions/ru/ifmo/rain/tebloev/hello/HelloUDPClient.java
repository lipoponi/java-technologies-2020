package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress serverAddress = new InetSocketAddress(host, port);

        ExecutorService senderExecutor = Executors.newFixedThreadPool(threads);
        CountDownLatch senderLatch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(threadId -> senderExecutor.execute(() -> {
            try (DatagramSocket socket = new DatagramSocket(null)) {
                socket.setSoTimeout(100);
                socket.connect(serverAddress);

                byte[] buffer = new byte[1024];
                for (int i = 0; i < requests; i++) {
                    String request = String.format("%s%d_%d", prefix, threadId, i);

                    byte[] queryBytes = request.getBytes(StandardCharsets.UTF_8);
                    socket.send(new DatagramPacket(queryBytes, queryBytes.length, serverAddress));

                    try {
                        DatagramPacket responsePacket = new DatagramPacket(buffer, 1024);
                        socket.receive(responsePacket);

                        String response = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), StandardCharsets.UTF_8);
                        if (response.contains(request)) {
                            System.out.println(String.format("%s%n%s", request, response));
                        } else {
                            i--;
                        }
                    } catch (IOException e) {
                        i--;
                    }
                }
            } catch (IOException ignored) {
            } finally {
                senderLatch.countDown();
            }
        }));

        try {
            senderLatch.await();
        } catch (
                InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
