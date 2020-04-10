package ru.ifmo.rain.tebloev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;


public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> jobQueue;
    private final List<Thread> threadList;

    /**
     * Constructs ParallelMapperImpl with specified amount of worker threads.
     *
     * @param threads amount of worker threads
     */
    public ParallelMapperImpl(int threads) {
        jobQueue = new LinkedList<>();
        threadList = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(this::workerLoop);
            thread.start();
            threadList.add(thread);
        }
    }

    private void workerLoop() {
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
        }

        try {
            for (Thread thread : threadList) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }
    }
}