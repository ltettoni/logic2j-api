package org.logic2j.api.result;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Holds which kind of results are desired by a client invoker, before actually invoking the generation of results.
 * A default implementation is provided for most methods - yet they all rely on
 * {@link #list()} so the default implementation is not efficient, and implementers of this interface may want to
 * provide more efficient implementations.
 *
 * @param <T> Type of effective individual solutions
 */
public interface ResultsHolder<T> extends Iterable<T>, Supplier<T> {

  // -----------------------------------------
  // Check existence and cardinality, default implementations rely on count() which is far from optimal in certain cases.
  // -----------------------------------------

  /**
   * Check existence (presence of a solution).
   *
   * @return true if at least one result is available
   */
  default boolean isPresent() {
    return !isEmpty();
  }

  /**
   * Check absence of solution.
   *
   * @return Opposite to {@link #isPresent()}
   */
  default boolean isEmpty() {
    return count() == 0;
  }

  /**
   * Check for optional single-value: that there are no multiple solutions.
   *
   * @return true iff zero or one solution.
   */
  default boolean isSingle() {
    return count() <= 1;
  }

  /**
   * Check for mandatory single-value: that there is exactly one solution.
   *
   * @return true iff exactly one solution.
   */
  default boolean isUnique() {
    return count() == 1;
  }

  /**
   * Check for multiple values.
   *
   * @return true iff strictly more than one solution.
   */
  default boolean isMultiple() {
    return count() > 1;
  }

  /**
   * @return Number of solutions.
   */
  default int count() {
    return list().size();
  }

  // -----------------------------------------
  // Single-value cardinality
  // -----------------------------------------

  @Override
  default T get() {
    final List<T> list = list();
    if (list.size() == 0) {
      return null;
    }
    return list.get(0);
  }

  /**
   * @return Single value or empty. If more are produced they will be ignored.
   */
  default Optional<T> single() {
    final List<T> list = list();
    if (list.size() == 0) {
      return Optional.empty();
    }
    return Optional.ofNullable(list.get(0));
  }

  /**
   * @return First value or empty. If more are produced they will be ignored.
   */
  default Optional<T> first() {
    return single();
  }

  /**
   * @return Only value, never null, if no value is produced then an IllegalStateException is thrown.
   * @throws IllegalStateException if there is no result
   */
  default T unique() {
    final List<T> list = list();
    if (list.size() == 0) {
      throw new IllegalStateException("Cannot obtain unique element of empty " + this);
    }
    return list.get(0);
  }


  // -----------------------------------------
  // Multiple value cardinality
  // -----------------------------------------

  List<T> list();

  /**
   * @return All results as as {@link Set}
   */
  default Set<T> set() {
    return addTo(new HashSet<>());
  }

  /**
   * Collect solutions into a user-specified {@link Collection}
   *
   * @param targetToAddTo The target collection (with user-chosen semantics) where all solutions should be added to.
   * @return the argument "theTargetToAddTo"
   */
  default <R extends Collection<T>> R addTo(R targetToAddTo) {
    targetToAddTo.addAll(list());
    return targetToAddTo;
  }

  default Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  default T[] array(T[] destinationArray) {
    return list().toArray(destinationArray);
  }

  default T[] toArray(IntFunction<T[]> generator) {
    final List<T> list = list();
    return list.toArray(generator.apply(list.size()));
  }

  default Iterator<T> iterator() {
    return list().iterator();
  }


  // -----------------------------------------
  // Convert data type
  // -----------------------------------------

  default <R> ResultsHolder<R> map(Function<T, R> mapping) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }


  // -----------------------------------------
  // Filter cardinality
  // -----------------------------------------

  default ResultsHolder<T> exactly(int nbFirst) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  default ResultsHolder<T> atMost(int nbFirst) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  default ResultsHolder<T> atLeast(int nbFirst) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  default ResultsHolder<T> limit(int nbFirst) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  default ResultsHolder<T> page(int first, int number) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  default ResultsHolder<T> distinct() {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

  // Aggregation

  default Optional<T> min(Comparator<? super T> comparator) {
    return stream().min(comparator);
  }

  default Optional<T> max(Comparator<? super T> comparator) {
    return stream().max(comparator);
  }

}
