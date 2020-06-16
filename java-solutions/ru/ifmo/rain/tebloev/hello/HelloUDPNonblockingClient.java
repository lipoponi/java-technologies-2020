package ru.ifmo.rain.tebloev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient implements HelloClient {
    private Consumer<SelectionKey> getSelectionHandler(SocketAddress serverAddress, String prefix,
                                                       int requests, ScheduledExecutorService wakeupExecutor, CountDownLatch countDown) {
        return key -> {
            ChannelAttachment attachment = (ChannelAttachment) key.attachment();

            try {
                if (key.isWritable()) {
                    synchronized (attachment) {
                        if (!attachment.sent) {
                            String request = String.format("%s%d_%d", prefix, attachment.index, attachment.receivedCount);
                            attachment.buffer.clear().put(Util.getBytes(request));
                            attachment.channel.send(attachment.buffer.flip(), serverAddress);
                            attachment.sent = true;
                            key.interestOps(SelectionKey.OP_READ);

                            attachment.tick++;
                            int tickNow = attachment.tick;
                            wakeupExecutor.schedule(() -> {
                                synchronized (attachment) {
                                    if (attachment.sent && attachment.tick == tickNow) {
                                        System.out.println(String.format("[Not reach %d_%d]", attachment.index, attachment.receivedCount));
                                        attachment.sent = false;
                                        key.interestOps(SelectionKey.OP_WRITE);
                                        key.selector().wakeup();
                                    }
                                }
                            }, Util.RECEIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            } catch (IOException e) {
                Util.handleException(new Exception("IO error while sending", e));
            }

            try {
                if (key.isReadable()) {
                    synchronized (attachment) {
                        if (attachment.receivedCount < requests && attachment.sent) {
                            attachment.channel.receive(attachment.buffer.clear());
                            String response = Util.extractString(attachment.buffer.flip());

                            if (Util.isResponseCorrect(response, attachment.index, attachment.receivedCount)) {
                                System.out.println("[Correct] " + response);
                                attachment.receivedCount++;
                            } else {
                                System.out.println("[Incorrect] " + response);
                            }

                            attachment.tick++;
                            attachment.sent = false;
                            key.interestOps(SelectionKey.OP_WRITE);
                            if (requests <= attachment.receivedCount) {
                                key.interestOps(0);
                                key.cancel();
                                countDown.countDown();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Util.handleException(new Exception("IO error while receiving", e));
            }
        };
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress serverAddress = new InetSocketAddress(host, port);

        try (Selector selector = Selector.open()) {
            IntStream.range(0, threads).boxed()
                    .forEach(IOConsumer.unchecked(index -> new ChannelAttachment(index, selector, serverAddress)));

            ScheduledExecutorService wakeupExecutor = Executors.newSingleThreadScheduledExecutor();

            CountDownLatch countDown = new CountDownLatch(threads);
            Consumer<SelectionKey> selectionHandler = getSelectionHandler(serverAddress, prefix, requests, wakeupExecutor, countDown);
            while (!Thread.currentThread().isInterrupted() && countDown.getCount() != 0) {
                selector.select(selectionHandler);
            }
            Util.shutdownAndAwaitTermination(wakeupExecutor);
        } catch (IOException e) {
            Util.handleException(e);
        }
    }

    private static class ChannelAttachment implements Closeable {
        private final int index;
        private final ByteBuffer buffer;
        private final DatagramChannel channel;
        private boolean sent = false;
        private int receivedCount = 0;
        private int tick = 0;

        private ChannelAttachment(int index, Selector selector, SocketAddress serverAddress) throws IOException {
            this.index = index;

            this.channel = DatagramChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(serverAddress);
            this.channel.register(selector, SelectionKey.OP_WRITE, this);

            this.buffer = ByteBuffer.allocate(this.channel.getOption(StandardSocketOptions.SO_RCVBUF));
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}
