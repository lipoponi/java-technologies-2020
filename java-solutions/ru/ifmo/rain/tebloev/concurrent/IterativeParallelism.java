package ru.ifmo.rain.tebloev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class that implements IterativeParallelism.
 *
 * @author Stanislav Tebloev
 */
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper mapper;

    /**
     * Constructs instance without mapper.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Constructs instance with specified mapper.
     *
     * @param mapper {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns concat of two {@link List} objects. If first is modifiable, then second would be appended to it.
     * Otherwise new {@link LinkedList} is created from {@code left} contents, and {@code right} is appended to it.
     *
     * @param left  {@link List} object that contents would be prefix of result
     * @param right {@link List} object that contents would be suffix of result
     * @return merge of two {@link List} objects
     */
    private static <T> List<T> listConcat(final List<T> left, final List<T> right) {
        if (left == null && right == null) {
            return null;
        } else if (left == null || right == null) {
            return new LinkedList<>(left == null ? right : left);
        }

        List<T> result = left;

        try {
            result.addAll(right);
        } catch (UnsupportedOperationException e) {
            result = new LinkedList<>(left);
            result.addAll(right);
        }

        return result;
    }

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    @Override
    public <T, R> R mapReduce(int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("Invalid thread count");
        }
        threads = Math.min(threads, values.size());

        int segmentSize = threads != 0 ? values.size() / threads : 0;
        int rest = values.size() % threads;

        List<List<T>> segments = new ArrayList<>();
        for (int i = 0, l = 0; i < threads; i++) {
            int from = l;
            int to = l + segmentSize + (i < rest ? 1 : 0);
            l = to;

            List<T> subList = values.subList(from, to);
            segments.add(subList);
        }

        if (mapper != null) {
            List<R> resultBuffer = mapper.map(
                    element -> element.stream().map(lift).reduce(monoid.getIdentity(), monoid.getOperator()),
                    segments
            );

            return resultBuffer.stream().reduce(monoid.getIdentity(), monoid.getOperator());
        } else {
            List<Thread> threadList = new ArrayList<>();
            List<R> resultBuffer = new ArrayList<>(Collections.nCopies(threads, null));

            for (int i = 0; i < threads; i++) {
                final int segmentId = i;
                Thread thread = new Thread(
                        () -> resultBuffer.set(segmentId, segments.get(segmentId).stream()
                                .map(lift).reduce(monoid.getIdentity(), monoid.getOperator()))
                );
                thread.start();
                threadList.add(thread);
            }

            InterruptedException exception = null;
            for (Thread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                }
            }

            if (exception != null) {
                throw exception;
            }

            return resultBuffer.stream().reduce(monoid.getIdentity(), monoid.getOperator());
        }
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return mapReduce(threads, values, Object::toString, new Monoid<>("", (a, b) -> a + b));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mapReduce(threads, values,
                x -> predicate.test(x) ? List.of(x) : null,
                new Monoid<>(List.of(), IterativeParallelism::listConcat)
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return mapReduce(threads, values,
                x -> List.of(f.apply(x)),
                new Monoid<>(List.of(), IterativeParallelism::listConcat));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("Empty list");
        }

        return reduce(threads, values, new Monoid<>(null, (a, b) -> {
            if (a == null || b == null) {
                return a == null ? b : a;
            } else if (comparator.compare(a, b) < 0) {
                return b;
            } else {
                return a;
            }
        }));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mapReduce(threads, values, predicate::test, new Monoid<>(true, (a, b) -> a & b));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
