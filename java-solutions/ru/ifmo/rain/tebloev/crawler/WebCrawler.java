package ru.ifmo.rain.tebloev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
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

        layer.forEach(url -> {
            try {
                Semaphore hostSemaphore = hostSemaphores.computeIfAbsent(URLUtils.getHost(url), key -> new Semaphore(perHost));
                hostSemaphore.acquire();

                downloadExecutor.execute(() -> {
                    try {
                        Document document = this.downloader.download(url);
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
                    } finally {
                        downloadLatch.countDown();
                        hostSemaphore.release();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (MalformedURLException e) {
                IOException presentException = errors.putIfAbsent(url, e);
                if (presentException != null) {
                    presentException.addSuppressed(e);
                }
            }
        });

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

    private static class Validator {
        public static void notNull(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Null argument");
            }
        }

        public static void isNumber(String value) {
            notNull(value);

            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format", e);
            }
        }
    }

    private static void printError(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length < 1 || 5 < args.length) {
                throw new IllegalArgumentException();
            }

            Validator.notNull(args[0]);
            String[] preprocessedArgs = new String[5];
            preprocessedArgs[0] = args[0];
            preprocessedArgs[1] = "2";
            preprocessedArgs[2] = "5";
            preprocessedArgs[3] = "5";
            preprocessedArgs[4] = "5";

            for (int i = 1; i < args.length; i++) {
                Validator.isNumber(args[i]);
                preprocessedArgs[i] = args[i];
            }

            String url = preprocessedArgs[0];
            int depth = Integer.parseInt(preprocessedArgs[1]);
            int downloaderCount = Integer.parseInt(preprocessedArgs[2]);
            int extractorCount = Integer.parseInt(preprocessedArgs[3]);
            int perHost = Integer.parseInt(preprocessedArgs[4]);

            try {
                Downloader downloader = new CachingDownloader();
                try (Crawler crawler = new WebCrawler(downloader, downloaderCount, extractorCount, perHost)) {
                    Result result = crawler.download(url, depth);

                    System.out.println("Downloaded:");
                    for (String downloadedUrl : result.getDownloaded()) {
                        System.out.println(downloadedUrl);
                    }
                    System.out.println();

                    System.out.println("Errors:");
                    for (String failedUrl : result.getErrors().keySet()) {
                        System.out.println(failedUrl);
                    }
                }
            } catch (IOException e) {
                printError("Unable to create downloader");
            }
        } catch (IllegalArgumentException e) {
            printError("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        }
    }
}