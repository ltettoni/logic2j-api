package org.logic2j.api.result;

import java.util.Collections;
import java.util.List;

/**
 * Ad minima there is only one method to implement: list().
 */
public class ResultsHolderMinimalImplementation implements ResultsHolder<Object> {
  @Override
  public List<Object> list() {
    return Collections.emptyList();
  }
}
