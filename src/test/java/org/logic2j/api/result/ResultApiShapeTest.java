package org.logic2j.api.result;

import org.junit.Ignore;
import org.junit.Test;
import org.logic2j.test.domain.MyDTO;

import java.util.List;
import java.util.Set;

import static org.logic2j.engine.model.Var.*;

@Ignore("Cannot yet test")
public class ResultApiShapeTest {

    // Results

    @Test
    public void existence() {
        final boolean exists = solve().exists();
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
        final boolean valueExists = solve().var(strVar()).exists();
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
        solve().var(strVar()).distinct().exactly(3).exists();
    }


    @Test
    public void min() {
        final Double min = solve().var(doubleVar()).min();
    }

    @Test
    public void sum() {
        final Integer sum = solve().var(intVar()).sum();
    }


    // Limit arity

    @Test
    public void projectSomeValues() {
        List<String> values = solve().var(strVar()).limit(5).list();
    }

    // Convert arity

    @Test
    public void projectSingleValue() {
        final String single = solve().var(strVar()).single();
    }


    // Helper
    private ResultsHolder<MyDTO> solve() {
        return null;
    }

}
