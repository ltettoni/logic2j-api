package org.logic2j.api.result;

import org.logic2j.engine.model.Var;

public class AnyVarResultHolder extends ListResultsHolder<Void> {
  public <T> ResultsHolder<T> var(Var<T> aVar) {
    return new ListResultsHolder<T>();
  }

}
