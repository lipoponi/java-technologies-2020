package ru.ifmo.rain.tebloev.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class Util {
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static final int DEFAULT_BUFFER_LENGTH = 1024;

    static void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static DatagramPacket createDefaultReceivePacket() {
        byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
        return new DatagramPacket(buffer, buffer.length);
    }

    static DatagramPacket createDefaultSendPacket(SocketAddress address, String data) {
        byte[] buffer = data.getBytes(DEFAULT_CHARSET);
        return new DatagramPacket(buffer, 0, buffer.length, address);
    }

    static String extractString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), DEFAULT_CHARSET);
    }
}
