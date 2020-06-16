package ru.ifmo.rain.tebloev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class Util {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int AWAIT_TERMINATION_TIMEOUT_MS = 100;

    static void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(AWAIT_TERMINATION_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static int getChannelBufferSize(DatagramChannel channel) throws IOException {
        int receiveBufferSize = channel.getOption(StandardSocketOptions.SO_RCVBUF);
        int sendBufferSize = channel.getOption(StandardSocketOptions.SO_SNDBUF);

        return Math.max(receiveBufferSize, sendBufferSize);
    }

    static DatagramPacket createDefaultReceivePacket(int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        return new DatagramPacket(buffer, buffer.length);
    }

    static DatagramPacket createDefaultSendPacket(SocketAddress address, String data) {
        byte[] buffer = data.getBytes(DEFAULT_CHARSET);
        return new DatagramPacket(buffer, 0, buffer.length, address);
    }

    static String extractString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), DEFAULT_CHARSET);
    }

    static void handleException(Exception e) {
        System.err.println(e.getMessage());
    }

    static String extractString(ByteBuffer buffer) {
        return DEFAULT_CHARSET.decode(buffer).toString();
    }

    static void handleThreadException(int threadId, Exception e) {
        String msg = String.format("Exception in thread '%d': %s", threadId, e.getMessage());
        handleException(new Exception(msg, e));
    }
}
