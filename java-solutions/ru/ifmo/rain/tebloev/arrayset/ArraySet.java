package ru.ifmo.rain.tebloev.arrayset;

import java.util.*;
import java.util.function.Predicate;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private class ArraySetIterator implements Iterator<E> {
        private final List<E> data;
        private final boolean reversed;
        private int position = -1;

        public ArraySetIterator(final List<E> data, final boolean reversed) {
            this.data = data;
            this.reversed = reversed;
        }

        @Override
        public boolean hasNext() {
            return position + 1 < data.size();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            position++;
            int idx = reversed ? data.size() - 1 - position : position;

            return data.get(idx);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private List<E> data;
    private final Comparator<? super E> comparator;
    private final boolean reversed;

    public ArraySet() {
        data = List.copyOf(new ArrayList<>());
        comparator = null;
        reversed = false;
    }

    public ArraySet(final Collection<? extends E> source) {
        this(source, null);
    }

    public ArraySet(final Collection<? extends E> source, final Comparator<? super E> comparator) {
        this.comparator = comparator;

        if (isSorted(source)) {
            putUniqueList(source);
        } else {
            final TreeSet<E> tree = new TreeSet<>(comparator);
            tree.addAll(source);

            data = List.copyOf(tree);
        }

        reversed = false;
    }

    private ArraySet(final List<E> source, final Comparator<? super E> comparator, boolean isReversed) {
        this.data = source;
        this.comparator = comparator;
        this.reversed = isReversed;
    }

    private int compare(final E lhs, final E rhs) {
        if (comparator == null) {
            @SuppressWarnings("unchecked")
            Comparable<E> e = (Comparable<E>) lhs;
            return e.compareTo(rhs);
        } else {
            return comparator.compare(lhs, rhs);
        }
    }

    private boolean isSorted(final Collection<? extends E> source) {
        Iterator<? extends E> iterator = source.iterator();
        E current = null;
        while (iterator.hasNext()) {
            E last = current;
            current = iterator.next();
            if (last != null && 0 < compare(last, current)) {
                return false;
            }
        }

        return true;
    }

    private void putUniqueList(final Collection<? extends E> source) {
        List<E> result = new ArrayList<>();

        for (E e : source) {
            if (!result.isEmpty() && compare(result.get(result.size() - 1), e) == 0) {
                continue;
            }

            if (e == null) {
                throw new NullPointerException();
            }

            result.add(e);
        }

        data = List.copyOf(result);
    }

    private int find(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        @SuppressWarnings("unchecked") final E k = (E) key;
        if (comparator == null) {
            @SuppressWarnings("unchecked")
            List<? extends Comparable<? super E>> list = (List<? extends Comparable<? super E>>) data;
            return Collections.binarySearch(list, k);
        } else {
            return Collections.binarySearch(data, k, comparator);
        }
    }

    private E getElementNullable(final int idx) {
        if (idx < 0 || data.size() <= idx) {
            return null;
        }

        return data.get(idx);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return data.get(reversed ? data.size() - 1 : 0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return data.get(reversed ? 0 : data.size() - 1);
    }

    private int lowerBound(E e, boolean inclusive) {
        int idx = find(e);
        idx = idx < 0 ? -idx - 1 : (idx + (inclusive ? 0 : 1));

        return idx;
    }

    private int upperBound(E e, boolean inclusive) {
        int idx = find(e);
        idx = idx < 0 ? -idx - 2 : (idx - (inclusive ? 0 : 1));

        return idx;
    }


    @Override
    public E lower(E e) {
        int idx = reversed ? lowerBound(e, false) : upperBound(e, false);
        return getElementNullable(idx);
    }


    @Override
    public E floor(E e) {
        int idx = reversed ? lowerBound(e, true) : upperBound(e, true);
        return getElementNullable(idx);
    }


    @Override
    public E ceiling(E e) {
        int idx = reversed ? upperBound(e, true) : lowerBound(e, true);
        return getElementNullable(idx);
    }


    @Override
    public E higher(E e) {
        int idx = reversed ? upperBound(e, false) : lowerBound(e, false);
        return getElementNullable(idx);
    }

    @Override
    public int size() {
        return data.size();
    }


    @Override
    public Comparator<? super E> comparator() {
        return reversed ? comparator.reversed() : comparator;
    }

    @Override
    public boolean contains(Object o) {
        try {
            /**
             * find(null) throws NullPointerException
             * if o is incompatible with this collection find(o) will throw ClassCastException
             * if find(o) returned correct index this function should return true
             */
            return 0 <= find(o);
        } catch (ClassCastException e) {
            return false;
        }
    }


    @Override
    public Iterator<E> iterator() {
        return new ArraySetIterator(data, reversed);
    }


    @Override
    public ArraySet<E> descendingSet() {
        return new ArraySet<E>(data, comparator, !reversed);
    }


    @Override
    public Iterator<E> descendingIterator() {
        return new ArraySetIterator(data, !reversed);
    }



    private ArraySet<E> subSetByIndices(int l, int r) {
        return new ArraySet<E>(data.subList(l, r), comparator, false);
    }


    @Override
    public ArraySet<E> subSet(E le, E re) {
        return subSet(le, true, re, false);
    }


    @Override
    public ArraySet<E> subSet(E le, boolean li, E re, boolean ri) {
        @SuppressWarnings("unchecked")
        int result = comparator == null
                ? ((Comparable<E>) le).compareTo(re)
                : comparator.compare(le, re);

        if (0 < result) {
            throw new IllegalArgumentException();
        }

        return tailSet(le, li).headSet(re, ri);
    }


    @Override
    public ArraySet<E> headSet(E e) {
        return headSet(e, false);
    }


    @Override
    public ArraySet<E> headSet(E e, boolean inclusive) {
        if (reversed) {
            int idx = lowerBound(e, inclusive);
            return subSetByIndices(idx, data.size());
        } else {
            int idx = upperBound(e, inclusive);
            return subSetByIndices(0, idx + 1);
        }
    }


    @Override
    public ArraySet<E> tailSet(E e) {
        return tailSet(e, true);
    }


    @Override
    public ArraySet<E> tailSet(E e, boolean inclusive) {
        if (reversed) {
            int idx = upperBound(e, inclusive);
            return subSetByIndices(0, idx + 1);
        } else {
            int idx = lowerBound(e, inclusive);
            return subSetByIndices(idx, data.size());
        }
    }


    @Override
    public boolean add(E ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> ignored) {
        throw new UnsupportedOperationException();
    }


    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }


    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }
}
