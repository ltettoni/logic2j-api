package org.logic2j.api.result;

import org.logic2j.engine.model.Var;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Holds what results are desired by a client app until actually invoking any data generation.
 * Default implementation of most methods are provided - yet inefficient they will rely on
 * {@link #count()}.
 *
 * @param <T> Type of effective individual solutions
 */
public interface ResultsHolder<T> extends Iterable<T> {

  // -----------------------------------------
  // Check existence and cardinality, default implems rely on count()
  // -----------------------------------------

  /**
   * Check existence (presence of a solution)
   *
   * @return true if at least one result available
   */
  default boolean isPresent() {
    return count() > 0;
  }

  default boolean isEmpty() {
    return !isPresent();
  }

  /**
   * Check for optional single-value.
   *
   * @return true iff zero or one solution.
   */
  default boolean isSingle() {
    return count() <= 1;
  }

  /**
   * Check for mandatory single-value.
   *
   * @return true iff exactly one solution.
   */
  default boolean isUnique() {
    return count() == 1;
  }

  /**
   * Check for multiple values.
   *
   * @return true iff strictly more than one solutions.
   */
  default boolean isMultiple() {
    return count() > 1;
  }

  default int count() {
    return list().size();
  }

  // -----------------------------------------
  // Single-value cardinality
  // -----------------------------------------

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
   * @return Only value, never null, if more a produced an exception is thrown.
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

  default <R> ResultsHolder<R> var(Var<R> variable) {
    throw new UnsupportedOperationException("Operation not supported on " + this);
  }

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
