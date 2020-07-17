package org.logic2j.api.result;

import java.util.Arrays;
import java.util.List;

public class ListResultsHolder<T> implements ResultsHolder<T> {

  private final List<T> internal;

  public ListResultsHolder(List<T> elements) {
    this.internal = elements;
  }

  @SafeVarargs
  public ListResultsHolder(T... elements) {
    this(Arrays.asList(elements));
  }

  @Override
  public List<T> list() {
    return internal;
  }
}
