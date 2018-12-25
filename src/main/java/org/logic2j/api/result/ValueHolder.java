package org.logic2j.api.result;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    default Set<T> set() {
        return addTo(new HashSet<T>());
    }

    /**
     * Collect solutions into a user-specified Collection
     *
     * @param theTargetToAddTo The target collection (with user-chosen semantics) where all solutions should be added to.
     * @return the argument "theTargetToAddTo"
     */
    default <R extends Collection<T>> R addTo(R theTargetToAddTo) {
        theTargetToAddTo.addAll(list());
        return theTargetToAddTo;
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default <T> T[] array(T[] destinationArray) {
        return list().toArray(destinationArray);
    }

    Iterator<T> iterator();

    // Transform cardinality

    ValueHolder<T> exactly(int nbFirst);

    ValueHolder<T> atMost(int nbFirst);

    ValueHolder<T> atLeast(int nbFirst);

    ValueHolder<T> limit(int nbFirst);

    ValueHolder<T> page(int first, int number);

    ValueHolder<T> distinct();

    // Aggregation

    default Optional<T> min(Comparator<? super T> comparator) {
        return stream().min(comparator);
    }

    default Optional<T> max(Comparator<? super T> comparator) {
        return stream().max(comparator);
    }

}
