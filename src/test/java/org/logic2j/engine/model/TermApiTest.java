/*
 * logic2j - "Bring Logic to your Java" - Copyright (c) 2017 Laurent.Tettoni@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.logic2j.engine.model;

import org.junit.Test;
import org.logic2j.engine.exception.InvalidTermException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.logic2j.engine.model.TermApiLocator.termApi;
import static org.logic2j.engine.model.Var.anon;
import static org.logic2j.engine.model.Var.anyVar;

/**
 * Low-level tests of the {@link TermApi} facade.
 */
public class TermApiTest {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TermApiTest.class);

    @Test
    public void structurallyEquals() {
        // Vars are never structurally equal ...
        assertThat(anyVar("X").structurallyEquals(anyVar("Y"))).isFalse();
        final Var x1 = anyVar("X");
        final Var x2 = anyVar("X");
        // ... even when they have the same name
        assertThat(x1.structurallyEquals(x2)).isFalse();
        final Struct s = new Struct("s", x1, x2);
        assertThat(termApi().structurallyEquals(s.getArg(0), s.getArg(1))).isFalse();
        // After factorization, the 2 X will be same
        final Struct s2 = termApi().factorize(s);
        assertThat(s2).isNotSameAs(s);
        assertThat(s.structurallyEquals(s2)).isFalse();
        assertThat(termApi().structurallyEquals(s2.getArg(0), s2.getArg(1))).isTrue();
    }

    @Test
    public void collectTerms() {
        Term term;
        //
        term = Struct.valueOf("p", "X", 2);
        logger.info("Flat terms: {}", termApi().collectTerms(term));
        //
        term = Struct.valueOf("a", new Struct("b"), "c");
        logger.info("Flat terms: {}", termApi().collectTerms(term));
        //
        term = new Struct(Struct.FUNCTOR_CLAUSE, new Struct("a", Struct.valueOf("p", "X", "Y")), Struct.valueOf("p", "X", "Y"));
        logger.info("Flat terms: {}", termApi().collectTerms(term));
        //
        final Term clause = new Struct(Struct.FUNCTOR_CLAUSE, new Struct("a", Struct.valueOf("p", "X", "Y")), Struct.valueOf("p", "X", "Y"));
        logger.info("Flat terms of original {}", termApi().collectTerms(clause));
        final Object t2 = termApi().normalize(clause);
        logger.info("Found {} bindings", ((Struct) t2).getIndex());
        assertThat(((Struct) t2).getIndex()).isEqualTo(2);
        logger.info("Flat terms of copy     {}", termApi().collectTerms(t2));
        assertThat(t2.toString()).isEqualTo(clause.toString());
    }

    @Test
    public void assignIndexes() {
        int nbVars;
        nbVars = termApi().assignIndexes(new Struct("f"), 0);
        assertThat(nbVars).isEqualTo(0);
        nbVars = termApi().assignIndexes(anyVar("X"), 0);
        assertThat(nbVars).isEqualTo(1);
        nbVars = termApi().assignIndexes(anon(), 0);
        assertThat(nbVars).isEqualTo(0);
        //
        nbVars = termApi().assignIndexes(2L, 0);
        assertThat(nbVars).isEqualTo(0);
        nbVars = termApi().assignIndexes(1.1, 0);
        assertThat(nbVars).isEqualTo(0);
    }


    @Test(expected = InvalidTermException.class)
    public void functorFromSignatureFails() {
        termApi().functorFromSignature("toto4");
    }


    @Test
    public void functorFromSignature1() {
        assertThat(termApi().functorFromSignature("toto/4")).isEqualTo("toto");
    }


    @Test(expected = InvalidTermException.class)
    public void arityFromSignatureFails() {
        termApi().arityFromSignature("toto4");
    }

    @Test
    public void arityFromSignature1() {
        assertThat(termApi().arityFromSignature("toto/4")).isEqualTo(4);
    }

    @Test
    public void quoteIfNeeded() {
        assertThat(termApi().quoteIfNeeded(null)).isNull();
        assertThat(termApi().quoteIfNeeded("").toString()).isEqualTo("''");
        assertThat(termApi().quoteIfNeeded(" ").toString()).isEqualTo("' '");
        assertThat(termApi().quoteIfNeeded("ab").toString()).isEqualTo("ab");
        assertThat(termApi().quoteIfNeeded("Ab").toString()).isEqualTo("'Ab'");
        assertThat(termApi().quoteIfNeeded("it's").toString()).isEqualTo("'it''s'");
        assertThat(termApi().quoteIfNeeded("a''b").toString()).isEqualTo("'a''''b'");
        assertThat(termApi().quoteIfNeeded("'that'").toString()).isEqualTo("'''that'''");
    }
}
