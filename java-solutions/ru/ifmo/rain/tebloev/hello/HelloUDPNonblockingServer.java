package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HelloUDPNonblockingServer implements HelloServer {
    private boolean running = false;
    private Thread serverThread = null;
    private ExecutorService workerExecutor = null;

    private Consumer<SelectionKey> getSelectionHandler(Selector selector, Queue<Packet> forSendQueue) {
        return key -> {
            DatagramChannel channel = (DatagramChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();

            try {
                if (key.isReadable()) {
                    SocketAddress clientAddress = channel.receive(buffer.clear());
                    String request = Util.extractString(buffer.flip());

                    workerExecutor.execute(() -> {
                        String result = String.format("Hello, %s", request);

                        forSendQueue.add(new Packet(clientAddress, result));
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        selector.wakeup();
                    });
                }
            } catch (IOException e) {
                Util.handleException(new Exception("IO error while receiving", e));
            }

            try {
                if (key.isWritable() && !forSendQueue.isEmpty()) {
                    Packet packet = forSendQueue.poll();
                    if (forSendQueue.isEmpty()) {
                        key.interestOps(SelectionKey.OP_READ);
                    }

                    buffer.clear().put(Util.getBytes(packet.message));
                    channel.send(buffer.flip(), packet.address);
                }
            } catch (IOException e) {
                Util.handleException(new Exception("IO error while sending", e));
            }
        };
    }

    @Override
    public synchronized void start(int port, int threads) {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        try {
            Selector selector = Selector.open();
            DatagramChannel channel = DatagramChannel.open();
            ByteBuffer buffer = ByteBuffer.allocate(Util.getChannelBufferSize(channel));
            workerExecutor = Executors.newFixedThreadPool(threads);

            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_READ, buffer);

            (serverThread = new Thread(() -> {
                Queue<Packet> forSendQueue = new ConcurrentLinkedQueue<>();
                Consumer<SelectionKey> selectionHandler = getSelectionHandler(selector, forSendQueue);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        selector.select(selectionHandler);
                    } catch (IOException e) {
                        Util.handleException(new Exception("IO error while selecting", e));
                    }
                }
            })).start();

            running = true;
        } catch (IOException e) {
            Util.handleException(new Exception("Unable to open resources", e));
        }
    }

    @Override
    public synchronized void close() {
        if (!running) {
            throw new IllegalStateException("Server isn't running");
        }

        running = false;
        try {
            serverThread.interrupt();
            serverThread.join();
        } catch (InterruptedException ignored) {
        }

        Util.shutdownAndAwaitTermination(workerExecutor);
    }

    private static class Packet {
        private final SocketAddress address;
        private final String message;

        private Packet(SocketAddress address, String message) {
            this.address = address;
            this.message = message;
        }
    }
}
