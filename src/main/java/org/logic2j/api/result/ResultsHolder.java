package org.logic2j.api.result;

import org.logic2j.engine.model.Var;

/**
 * Holds what results are expected before actually triggering any calculation.
 * @param <T> Type of effective individual solutions
 */
public interface ResultsHolder<T> {

    // Check existence and cardinality

    boolean exists();

    boolean isSingle();

    boolean isMultiple();

    int count();

    // Project one of the goal's variables as our results

    <R> ValueHolder<R> var(Var<R> variable);


}
