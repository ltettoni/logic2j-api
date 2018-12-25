package org.logic2j.api.result;

import org.junit.Test;

import java.util.List;

import static org.logic2j.engine.model.Var.strVar;

public class ResultApiShapeTest {

    @Test
    public void existence() {
        boolean b = solve().exists();
    }

    @Test
    public void cardinalityOne() {
        boolean one = solve().isSingle();
    }

    @Test
    public void cardinalityMany() {
        boolean many = solve().isMultiple();
    }

    @Test
    public void count() {
        int nbr = solve().count();
    }


    @Test
    public void projectStrings() {
        final List<String> values = solve().var(strVar()).list();
    }


    private ResultHolder solve() {
        return null;
    }

}
