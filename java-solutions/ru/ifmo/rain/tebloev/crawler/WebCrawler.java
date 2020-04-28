package ru.ifmo.rain.tebloev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadExecutor;
    private final ExecutorService extractExecutor;
    private final int perHost;
    private final ConcurrentMap<String, Semaphore> hostSemaphores;

    public WebCrawler(Downloader downloader, int downloaderCount, int extractorCount, int perHost) {
        this.downloader = downloader;
        this.downloadExecutor = Executors.newFixedThreadPool(downloaderCount);
        this.extractExecutor = Executors.newFixedThreadPool(extractorCount);
        this.perHost = perHost;
        this.hostSemaphores = new ConcurrentHashMap<>();
    }

    private Document downloadHostLimited(String url) throws IOException, InterruptedException {
        Semaphore hostSemaphore = hostSemaphores.computeIfAbsent(URLUtils.getHost(url), key -> new Semaphore(perHost));

        hostSemaphore.acquire();
        try {
            return this.downloader.download(url);
        } finally {
            hostSemaphore.release();
        }
    }

    private List<String> processLayer(List<String> layer, List<String> downloaded, ConcurrentMap<String, IOException> errors, Set<String> seen, boolean extractLinks) {
        CountDownLatch downloadLatch = new CountDownLatch(layer.size());
        Queue<Future<List<String>>> extractFutures = new ConcurrentLinkedQueue<>();

        layer.forEach(url -> downloadExecutor.execute(() -> {
            try {
                Document document = downloadHostLimited(url);
                downloaded.add(url);

                if (extractLinks) {
                    extractFutures.add(extractExecutor.submit(() -> {
                        try {
                            return document.extractLinks();
                        } catch (IOException e) {
                            IOException presentException = errors.putIfAbsent(url, e);
                            if (presentException != null) {
                                presentException.addSuppressed(e);
                            }
                        }

                        return List.of();
                    }));
                }
            } catch (IOException e) {
                IOException presentException = errors.putIfAbsent(url, e);
                if (presentException != null) {
                    presentException.addSuppressed(e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                downloadLatch.countDown();
            }
        }));

        try {
            downloadLatch.await();
            List<List<String>> extractResults = new ArrayList<>();
            for (Future<List<String>> future : extractFutures) {
                extractResults.add(future.get());
            }
            return extractResults.stream().flatMap(Collection::stream).distinct()
                    .filter(url -> !seen.contains(url)).peek(seen::add)
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ignored) {
            assert false : "Unreachable";
        }

        return List.of();
    }

    @Override
    public Result download(String startUrl, int depth) {
        if (downloadExecutor.isShutdown() || extractExecutor.isShutdown()) {
            throw new UnsupportedOperationException("WebCrawler is shutdown");
        }

        List<String> downloaded = Collections.synchronizedList(new ArrayList<>());
        ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> seen = new HashSet<>();
        List<String> activeLayer = List.of(startUrl);

        seen.add(startUrl);

        for (int i = 1; i <= depth && !activeLayer.isEmpty(); i++) {
            activeLayer = processLayer(activeLayer, downloaded, errors, seen, i < depth);
        }

        return new Result(downloaded, errors);
    }

    private void shutdownAndAwaitTermination(ExecutorService executor) {
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

    @Override
    public void close() {
        shutdownAndAwaitTermination(downloadExecutor);
        shutdownAndAwaitTermination(extractExecutor);
    }
}