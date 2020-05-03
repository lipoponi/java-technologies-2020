package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {
    private ExecutorService receiveExecutor = null;
    private DatagramSocket socket = null;

    @Override
    public synchronized void start(int port, int threads) {
        try {
            receiveExecutor = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);

            IntStream.range(0, threads).forEach(threadId -> receiveExecutor.execute(() -> {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        DatagramPacket receivePacket = Util.createDefaultReceivePacket();
                        socket.receive(receivePacket);
                        String request = Util.extractString(receivePacket);

                        String response = String.format("Hello, %s", request);
                        socket.send(Util.createDefaultSendPacket(receivePacket.getSocketAddress(), response));
                    } catch (IOException ignored) {
                    }
                }
            }));
        } catch (SocketException ignored) {
        }
    }

    @Override
    public synchronized void close() {
        socket.close();
        Util.shutdownAndAwaitTermination(receiveExecutor);
    }
}
