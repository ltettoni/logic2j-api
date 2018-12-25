package org.logic2j.api.result;

import org.logic2j.engine.model.Var;

public interface ResultHolder<T> {
    boolean exists();

    boolean isSingle();

    boolean isMultiple();

    int count();

    <R> ValueHolder<R> var(Var<R> variable);
}
