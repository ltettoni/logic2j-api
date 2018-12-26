package org.logic2j.api.result;

import org.logic2j.engine.model.Var;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListResultHolder<T> implements ResultsHolder<T> {


  private final List<T> internal;

  public ListResultHolder(List<T> internal) {
    this.internal = internal;
  }

  public ListResultHolder(T... elements) {
    this(Arrays.asList(elements));
  }


  @Override
  public List<T> list() {
    return internal;
  }
}
