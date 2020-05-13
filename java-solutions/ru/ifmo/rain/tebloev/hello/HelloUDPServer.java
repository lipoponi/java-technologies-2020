package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket = null;
    private ExecutorService receiveExecutor = null;
    private CountDownLatch executorLatch = null;

    /**
     * Starts a new Hello server.
     *
     * @param port    server port
     * @param threads number of working threads
     * @throws UncheckedIOException if socket cannot be allocated
     */
    @Override
    public synchronized void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new UncheckedIOException("Socket cannot be allocated", e);
        }

        receiveExecutor = Executors.newFixedThreadPool(threads);
        executorLatch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(threadId -> receiveExecutor.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        DatagramPacket receivePacket = Util.createDefaultReceivePacket(socket.getReceiveBufferSize());
                        socket.receive(receivePacket);
                        String request = Util.extractString(receivePacket);

                        String response = String.format("Hello, %s", request);
                        socket.send(Util.createDefaultSendPacket(receivePacket.getSocketAddress(), response));
                    } catch (IOException e) {
                        Util.handleThreadException(threadId, e);
                    }
                }
            } finally {
                executorLatch.countDown();
            }
        }));
    }

    @Override
    public synchronized void close() {
        if (socket == null || receiveExecutor == null || executorLatch == null) {
            return;
        }

        socket.close();
        Util.shutdownAndAwaitTermination(receiveExecutor);

        try {
            executorLatch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
