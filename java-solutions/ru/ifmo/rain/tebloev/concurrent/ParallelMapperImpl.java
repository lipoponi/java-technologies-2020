package ru.ifmo.rain.tebloev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Mapper that applies map operation using variable number of worker threads.
 *
 * @author Stanislav Tebloev
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> jobQueue;
    private final List<Thread> threadList;

    /**
     * Constructs ParallelMapperImpl with specified number of worker threads.
     *
     * @param threads number of worker threads
     */
    public ParallelMapperImpl(int threads) {
        jobQueue = new LinkedList<>();
        threadList = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(this::handleJobs);
            thread.start();
            threadList.add(thread);
        }
    }

    private void handleJobs() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable job;
                synchronized (jobQueue) {
                    while (jobQueue.isEmpty()) {
                        jobQueue.wait();
                    }

                    job = jobQueue.poll();
                }

                job.run();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final List<T> argsArray = new ArrayList<>(args);
        final List<R> resultBuffer = new ArrayList<>(Collections.nCopies(args.size(), null));
        final int[] counter = {0};

        synchronized (jobQueue) {
            for (int i = 0; i < args.size(); i++) {
                int index = i;

                jobQueue.add(() -> {
                    resultBuffer.set(index, f.apply(argsArray.get(index)));
                    synchronized (counter) {
                        if (++counter[0] == resultBuffer.size()) {
                            counter.notify();
                        }
                    }
                });
            }

            jobQueue.notifyAll();
        }

        while (counter[0] < args.size()) {
            synchronized (counter) {
                counter.wait();
            }
        }

        return resultBuffer;
    }

    @Override
    public void close() {
        for (Thread thread : threadList) {
            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}