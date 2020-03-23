package ru.ifmo.rain.tebloev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class that implements IterativeParallelism.
 */
public class IterativeParallelism implements AdvancedIP {
    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, x -> x, monoid);
    }

    @Override
    public <T, R> R mapReduce(int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("invalid thread count");
        }
        threads = Math.min(threads, values.size());

        int segmentSize = values.size() / threads;
        int rest = values.size() % threads;
        List<Thread> threadList = new ArrayList<>();
        List<R> buffer = new ArrayList<>(Collections.nCopies(threads, null));

        for (int i = 0, l = 0; i < threads; i++) {
            final int idx = i;
            final int from = l;
            final int to = l + segmentSize + (i < rest ? 1 : 0);
            l = to;

            Thread thread = new Thread(
                    () -> buffer.set(idx, values.subList(from, to).stream()
                            .map(lift).reduce(monoid.getIdentity(), monoid.getOperator()))
            );

            thread.start();
            threadList.add(thread);
        }

        R result = monoid.getIdentity();
        InterruptedException collector = null;
        for (int i = 0; i < threads; i++) {
            try {
                threadList.get(i).join();
                result = monoid.getOperator().apply(result, buffer.get(i));
            } catch (InterruptedException e) {
                collector = collector == null ? new InterruptedException() : collector;
                collector.addSuppressed(e);
            }
        }

        if (collector != null) {
            throw collector;
        }

        return result;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return mapReduce(threads, values, Object::toString, new Monoid<>("", (a, b) -> a + b));
    }

    /**
     * Returns merge of two {@link List} objects. If first is modifiable, then second would be appended to it.
     * Otherwise new {@link LinkedList} is created from {@code left} contents, and {@code right} is appended to it.
     *
     * @param left  {@link List} object that contents would be prefix of result
     * @param right {@link List} object that contents would be suffix of result
     * @return merge of two {@link List} objects
     */
    private <T> List<T> listMerge(final List<T> left, final List<T> right) {
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
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mapReduce(threads, values,
                x -> predicate.test(x) ? List.of(x) : null,
                new Monoid<>(List.of(), this::listMerge)
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return mapReduce(threads, values,
                x -> List.of(f.apply(x)),
                new Monoid<>(List.of(), this::listMerge));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("empty list");
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
