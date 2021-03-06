package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private static final int ATTEMPTS_PER_REQUEST = 100;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress serverAddress = new InetSocketAddress(host, port);

        ExecutorService senderExecutor = Executors.newFixedThreadPool(threads);
        CountDownLatch senderLatch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(threadIndex -> senderExecutor.execute(() -> {
            try (DatagramSocket socket = new DatagramSocket(null)) {
                socket.setSoTimeout(Util.RECEIVE_TIMEOUT_MS);
                socket.connect(serverAddress);

                IntStream.range(0, requests).forEach(requestIndex -> {
                    String request = String.format("%s%d_%d", prefix, threadIndex, requestIndex);
                    DatagramPacket sendPacket = Util.createDefaultSendPacket(serverAddress, request);

                    for (int j = 0; j < ATTEMPTS_PER_REQUEST; j++) {
                        try {
                            socket.send(sendPacket);
                            DatagramPacket receivePacket = Util.createDefaultReceivePacket(socket.getReceiveBufferSize());
                            socket.receive(receivePacket);
                            String response = Util.extractString(receivePacket);

                            if (Util.isResponseCorrect(response, threadIndex, requestIndex)) {
                                System.out.println(String.format("%s%n%s", request, response));
                                break;
                            }
                        } catch (IOException e) {
                            Util.handleThreadException(threadIndex, e);
                        }
                    }
                });
            } catch (SocketException e) {
                Util.handleThreadException(threadIndex, new IOException("Cannot open socket", e));
            } finally {
                senderLatch.countDown();
            }
        }));

        try {
            senderLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            Util.shutdownAndAwaitTermination(senderExecutor);
        }
    }
}
