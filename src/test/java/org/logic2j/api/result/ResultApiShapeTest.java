package org.logic2j.api.result;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.logic2j.engine.model.Var.doubleVar;
import static org.logic2j.engine.model.Var.strVar;

@Ignore("Cannot yet test")
public class ResultApiShapeTest {

  // Results

  @Test
  public void existence() {
    final boolean exists = solve().isPresent();
  }

  @Test
  public void cardinalityOne() {
    final boolean one = solve().isSingle();
  }

  @Test
  public void cardinalityMany() {
    final boolean many = solve().isMultiple();
  }

  @Test
  public void count() {
    final int nbr = solve().count();
  }

  // Values

  @Test
  public void projectStringsCheckExistence() {
    final boolean valueExists = solve().var(strVar()).isPresent();
  }


  @Test
  public void projectStringsAndCount() {
    final int numberOfValues = solve().var(strVar()).count();
  }


  @Test
  public void projectStringsToList() {
    final List<String> values = solve().var(strVar()).list();
  }

  @Test
  public void projectStringsToSet() {
    final Set<String> values = solve().var(strVar()).set();
  }

  // More projection methods needed here (iterator, spliterator, stream, array, etc)

  // Aggregation and consolidation

  @Test
  public void ensureOnlyThreeDistinctValues() {
    solve().var(strVar()).distinct().exactly(3).isPresent();
  }


  @Test
  public void min() {
    final Optional<Double> min = solve().var(doubleVar()).min(Double::compareTo);
  }


  // Limit arity

  @Test
  public void projectSomeValues() {
    List<String> values = solve().var(strVar()).limit(5).list();
  }

  // Convert arity

  @Test
  public void projectSingleValue() {
    final Optional<String> single = solve().var(strVar()).single();
  }


  // Helper
  private ResultsHolder<String> solve() {
    return new ListResultsHolder<>("some", "values");
  }

}
