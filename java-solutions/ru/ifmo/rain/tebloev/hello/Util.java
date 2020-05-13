package ru.ifmo.rain.tebloev.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class Util {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int AWAIT_TERMINATION_TIMEOUT_MS = 500;

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

    static void handleThreadException(int threadId, Exception e) {
        String msg = String.format("Exception in thread '%d': %s", threadId, e.getMessage());
        handleException(new Exception(msg, e));
    }
}
