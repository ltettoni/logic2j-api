package org.logic2j.api.result;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface ValueHolder<T> extends Iterable<T> {

    // Single-value cardinality

    boolean exists();

    /**
     * @return Single value or null. If more are produced they will be ignored.
     */
    T single();

    /**
     * @return First value or null. If more are produced they will be ignored.
     */
    T first();

    /**
     * @return Only value, never null, if more a produced an exception is thrown.
     */
    T unique();

    /**
     * @return Number of values
     */
    int count();

    List<T> list();

    Set<T> set();

    /**
     * Collect solutions into a user-specified Collection
     *
     * @param theTargetToAddTo The target collection (with user-chosen semantics) where all solutions should be added to.
     * @return the argument "theTargetToAddTo"
     */
    <R extends Collection<T>> R addTo(R theTargetToAddTo);

    Stream<T> stream();

    <T> T[] array(T[] destinationArray);

    Iterator<T> iterator();

    // Transform cardinality

    ValueHolder<T> exactly(int nbFirst);

    ValueHolder<T> atMost(int nbFirst);

    ValueHolder<T> atLeast(int nbFirst);

    ValueHolder<T> limit(int nbFirst);

    ValueHolder<T> page(int first, int number);

    ValueHolder<T> distinct();

    // Aggregation

    T min();

    T max();

    T sum();
}
