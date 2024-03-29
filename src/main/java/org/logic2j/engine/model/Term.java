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

import static org.logic2j.engine.model.TermApiLocator.termApi;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import org.logic2j.engine.visitor.TermVisitor;

/**
 * <p>
 * Term class is the root abstract class for all Prolog data types. The following notions apply on terms, see also the {@link TermApi} class
 * for methods to manage {@link Term}s.
 * <ul>
 * <li>Structural equality, see {@link TermApi#structurallyEquals(Object, Object)}</li>
 * <li>Factorization, see {@link TermApi#factorize(Object)}</li>
 * <li>Initialization of {@link Var} indexes, see {@link TermApi#assignIndexes(Object, int)}</li>
 * <li>Normalization: includes initialization of indexes, factorization, and identification of primitive functors</li>
 * </ul>
 * <p>
 * TODO Why Serializable?
 */
public abstract class Term implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * A value of index=={@value} (NO_INDEX) means it was not initialized.
   */
  public static final int NO_INDEX = -1;
  /**
   * A value of index=={@value} (ANON_INDEX) means this is the anonymous variable.
   */
  public static final int ANON_INDEX = -2;

  /**
   * For {@link Var}s: defines the unique index to the variable.
   * For a {@link Struct}: defines the number of distinct variables within all nested substructures.
   * The default value is NO_INDEX.
   */
  private int index = NO_INDEX;

  // ---------------------------------------------------------------------------
  // Accessors
  // ---------------------------------------------------------------------------

  public int getIndex() {
    return this.index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  protected void clearIndex() {
    setIndex(NO_INDEX);
  }

  public boolean hasIndex() {
    return getIndex() != NO_INDEX;
  }

  // ---------------------------------------------------------------------------
  // TermVisitor
  // ---------------------------------------------------------------------------

  public abstract <R> R accept(TermVisitor<R> visitor);


  // ---------------------------------------------------------------------------
  // Graph traversal methods, template methods with "protected" scope, user code should use TermApi methods instead.
  // Some traversal is implemented by the Visitor design pattern and the #accept() method
  // ---------------------------------------------------------------------------

  /**
   * Find the first {@link Term} that is either same, or structurally equal to this.
   *
   * @param findWithin
   * @return The {@link Term} found or null when none found.
   */
  protected Object findStructurallyEqualWithin(Collection<Object> findWithin) {
    for (final Object term : findWithin) {
      if (term != this && termApi().structurallyEquals(term, this)) {
        return term;
      }
    }
    return null;
  }

}
