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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.logic2j.engine.exception.InvalidTermException;
import org.logic2j.engine.visitor.TermVisitor;

/**
 * A {@link Struct} represents either a Prolog compound {@link Term}s such as functor(arg1, arg2),
 * or a Prolog atom (a 0-arity compound).
 * Note: Instances MUST be immutable.
 */
public class Struct<T> extends Term implements Cloneable {
  private static final long serialVersionUID = 1L;

  // ---------------------------------------------------------------------------
  // Names of functors
  // ---------------------------------------------------------------------------

  // TODO Move these constants to a common place?
  /**
   * This is the logical "AND" operator, usable with arity of  /2 or /*.
   */
  public static final String FUNCTOR_COMMA = ","; // No need to "intern()" a compile-time constant

  /**
   * This is the logical "OR" operator, usable with arity of  /2 or /*.
   */
  public static final String FUNCTOR_SEMICOLON = ";"; // No need to "intern()" a compile-time constant
  public static final String FUNCTOR_TRUE = "true";
  // Would like .intern() but it's anyway the case, and using this constant from an annotation won't work

  public static final String FUNCTOR_FALSE = "false"; // TODO do we need "false" or is this "fail"? // No need to "intern()" a compile-time constant

  public static final String FUNCTOR_CUT = "!";
  // Would like .intern() but it's anyway the case, and using this constant from an annotation won't work

  public static final String FUNCTOR_CALL = "call"; // No need to "intern()" a compile-time constant

  public static final String FUNCTOR_CLAUSE = ":-"; // No need to "intern()" a compile-time constant
  // ---------------------------------------------------------------------------
  // Some key atoms as singletons
  // ---------------------------------------------------------------------------
  public static final Struct<?> ATOM_TRUE = new Struct<>(FUNCTOR_TRUE);
  public static final Struct<?> ATOM_FALSE = new Struct<>(FUNCTOR_FALSE);
  public static final char PAR_CLOSE = ')';
  public static final char PAR_OPEN = '(';
  /**
   * Indicate the arity of a variable arguments predicate, such as write/N.
   * This is an extension to classic Prolog where only fixed arity is supported.
   */
  public static final String VARARG_ARITY_SIGNATURE = "N";

  /**
   * Terminates a vararg predicate description: write/N
   */
  private static final String VARARG_PREDICATE_TRAILER = "/" + VARARG_ARITY_SIGNATURE;

  // Separator of functor arguments: f(a,b), NOT the ',' functor for logical AND.
  public static final String ARG_SEPARATOR = ", ";
  public static final char QUOTE = '\'';

  private static final Object[] EMPTY_ARGS_ARRAY = new Object[0];


  /**
   * The functor of the Struct is its "name". This is a final value but due to implementation via
   * method setNameAndArity(), we cannot declare it final here the compiler is not that smart.
   */
  private String name; // Always "internalized" with String.intern(), you can compare with ==.

  private int arity;

  private transient Object[] args;

  /**
   * The signature is internalized and allows for fast matching during unification
   */
  private String signature;

  /**
   * Payload
   */
  private transient T content;

  /**
   * Low-level constructor.
   *
   * @param functor
   * @param arity
   */
  private Struct(String functor, int arity) {
    setNameAndArity(functor, arity);
    // When arity is zero, don't even bother to allocate arguments!
    if (this.arity > 0) {
      this.args = new Object[this.arity];
    }
  }

  public Struct(String functor, Object... argList) {
    this(functor, argList.length);
    int i = 0;
    for (final Object element : argList) {
      if (element == null) {
        throw new InvalidTermException("Cannot create Struct \"" + functor + Arrays.asList(argList) + "\", found null argument at index " + i);
      }
      this.args[i++] = element;
    }
  }

  /**
   * Copy constructor.
   * Creates a shallow copy but with all children which are Struct also cloned.
   */
  public Struct(Struct<T> original) {
    this.name = original.getName();
    this.arity = original.arity;
    this.signature = original.signature;
    this.content = original.content;
    // What about "this.index" ?
    if (this.arity > 0) {
      this.args = new Object[this.arity];
      for (int i = 0; i < this.arity; i++) {
        Object cloned = original.args[i];
        if (cloned instanceof Struct) {
          cloned = new Struct<>((Struct<?>) cloned);
        }
        this.args[i] = cloned;
      }
    }
  }

  /**
   * Obtain an atom from the catalog if it pre-existed, or create one an register in the catalog.
   *
   * @param functor
   * @return Either a new one created or an existing one. It's actually either a String (if it can be),
   * but can be also a Struct of zero-arity for special functors such as "true", "false"
   */
  public static Object atom(String functor) {
    // Search in the catalog of atoms for exact match
    final String iFunctor = functor.intern();
    final boolean specialAtomRequiresStruct = iFunctor == Struct.FUNCTOR_CUT || iFunctor == Struct.FUNCTOR_TRUE || iFunctor == Struct.FUNCTOR_FALSE;
    if (!specialAtomRequiresStruct) {
      // We can return an internalized String
      return iFunctor;
    }
    return new Struct<>(iFunctor, 0);
  }

  /**
   * Factory to builds a compound, with non-{@link Term} arguments that will be converted
   * by {@link TermApi#valueOf(Object)}.
   *
   * Note: This method is a static factory, not a constructor, to emphasize that arguments
   * are not of the type needed by this class, but need transformation.
   */
  public static Struct<?> valueOf(String functor, Object... argList) {
    final Struct<?> newInstance = new Struct<>(functor, argList.length);
    int i = 0;
    for (final Object element : argList) {
      newInstance.args[i++] = termApi().valueOf(element);
    }
    return newInstance;
  }

  /**
   * Clone with new arguments.
   *
   * @param newArguments New arguments, length must be same arity as original Struct
   * @return A clone of this.
   */
  public Struct<?> cloneWithNewArguments(Object[] newArguments) {
    // We can actually change arity, this is used when we clone ","(X,Y) to ","(X,Y,Z)
    try {
      final Struct<?> clone = (Struct<?>) this.clone();
      clone.args = newArguments;
      clone.setNameAndArity(clone.name, clone.args.length); // Also calculate the signature
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InvalidTermException("Could not clone Struct " + this + ": " + e);
    }
  }

  // ---------------------------------------------------------------------------
  // Template methods defined in abstract class Term
  // ---------------------------------------------------------------------------

  /**
   * Set Term.index to {@link Term#NO_INDEX}, recursively collect all argument's terms first,
   * then finally add this {@link Struct} to collectedTerms.
   * The functor alone (without its children) is NOT collected as a term. An atom is collected as itself.
   *
   * @param collectedTerms
   */
  void collectTermsInto(Collection<Object> collectedTerms) {
    clearIndex();
    if (this.arity > 0) {
      Arrays.stream(this.args).forEach(child -> termApi().collectTermsInto(child, collectedTerms));
    }
    collectedTerms.add(this);
  }

  Object factorize(Collection<Object> collectedTerms) {
    // Recursively factorize all arguments of this Struct
    final Object[] newArgs = new Object[this.arity];
    boolean anyChange = false;
    for (int i = 0; i < this.arity; i++) {
      newArgs[i] = termApi().factorize(this.args[i], collectedTerms);
      anyChange |= (newArgs[i] != this.args[i]);
    }
    // Now initialize result - a new Struct only if any change was found below
    final Struct<?> factorized;
    if (anyChange) {
      factorized = new Struct<>(this);
      factorized.args = newArgs;
    } else {
      factorized = this;
    }
    // If this Struct already has an equivalent in the provided collection, return that one
    final Object betterEquivalent = factorized.findStructurallyEqualWithin(collectedTerms);
    if (betterEquivalent != null) {
      return betterEquivalent;
    }
    return factorized;
  }

  Var<?>findVar(String varName) {
    for (int i = 0; i < this.arity; i++) {
      final Object term = this.args[i];
      final Var<?>found = termApi().findVar(term, varName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }


  /**
   * @param theOther
   * @return true when references are the same, or when theOther Struct has same predicate name, arity, and all arguments are also equal.
   */
  boolean structurallyEquals(Object theOther) {
    if (theOther == this) {
      return true; // Same reference
    }
    if (!(theOther instanceof Struct)) {
      return false;
    }
    final Struct<?> that = (Struct<?>) theOther;
    // Arity and names must match.
    if (this.arity == that.arity && this.name == that.name) { // Names are {@link String#intern()}alized so OK to check by reference
      for (int i = 0; i < this.arity; i++) {
        if (!termApi().structurallyEquals(this.args[i], that.args[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Write major properties of the Struct, and also calculate read-only indexing signature for efficient access.
   *
   * @param functor whose named is internalized by {@link String#intern()}
   * @param arity
   */
  private void setNameAndArity(String functor, int arity) {
    if (functor == null) {
      throw new InvalidTermException("The functor of a Struct cannot be null");
    }
    if (functor.isEmpty() && arity > 0) {
      throw new InvalidTermException("The functor of a non-atom Struct cannot be an empty string");
    }
    this.name = functor.intern();
    this.arity = arity;
    this.signature = (this.getName() + '/' + this.arity).intern();
  }

  // --------------------------------------------------------------------------
  // Accessors
  // --------------------------------------------------------------------------

  /**
   * @return A cloned array of all arguments (cloned to avoid any possibility to mutate)
   */
  public Object[] getArgs() {
    if (this.args == null) {
      return EMPTY_ARGS_ARRAY;
    }
    return this.args;
  }

  /**
   * @return the i-th element of this structure
   * Note: No bound check is done
   */
  public Object getArg(int argIndex) {
    return this.args[argIndex];
  }

  /**
   * A unique identifier that determines the family of the predicate represented by this {@link Struct}.
   *
   * @return The predicate's name + '/' + arity
   */
  public String getPredicateSignature() {
    return this.signature;
  }

  public String getVarargsPredicateSignature() {
    return this.getName() + VARARG_PREDICATE_TRAILER;
  }

  // ---------------------------------------------------------------------------
  // Helpers for binary predicates: defined LHS (left-hand side) and RHS (right-hand side)
  // ---------------------------------------------------------------------------

  /**
   * @return Left-hand-side term, this is, {@link #getArg(int)} at index 0.
   * It is assumed that the term MUST have
   * an arity of exactly 2, because when there's a LHS, there's also a RHS!
   */
  public Object getLHS() {
    if (this.arity != 2) {
      throw new InvalidTermException(
              "Can't get the left-hand-side argument of \"" + this + "\" (predicate arity is: " + getPredicateSignature() + ")");
    }
    return this.args[0];
  }

  /**
   * @return Right-hand-side term, this is, {@link #getArg(int)} at index 1.
   * It is assumed that the term MUST have an arity of 2.
   */
  public Object getRHS() {
    if (this.arity != 2) {
      throw new InvalidTermException(
              "Can't get the right-hand-side argument of \"" + this + "\" (predicate arity is: " + getPredicateSignature() + ")");
    }
    return this.args[1];
  }

  // ---------------------------------------------------------------------------
  // TermVisitor
  // ---------------------------------------------------------------------------

  @Override
  public <R> R accept(TermVisitor<R> visitor) {
    return visitor.visit(this);
  }


  // ---------------------------------------------------------------------------
  // Management of index, cycles, and traversal
  // ---------------------------------------------------------------------------

  /**
   * For {@link Struct}s, the Term.index will be the maximal index of any variables that can be found, recursively, under all
   * children arguments.
   *
   * Note: Assigning indexes, for example with a base index of 0, will proceed sequentially by depth-first
   * traversal. The first Vars encountered in sequence will receive indexes 0, 1, 2. Therefore a term such as
   * goal(A, Z, Y) will guarantee that indexes are: A=0, Z=1, Y=2.
   */
  int assignIndexes(int indexOfNextNonIndexedVar) {
    if (hasIndex()) {
      // Already assigned, do nothing and return the argument since we did
      // not assigned anything new
      return indexOfNextNonIndexedVar;
    }
    // Recursive assignment
    int runningIndex = indexOfNextNonIndexedVar;
    for (int i = 0; i < this.arity; i++) {
      runningIndex = termApi().assignIndexes(this.args[i], runningIndex);
    }
    setIndex(runningIndex);
    return runningIndex;
  }

  public void avoidCycle(List<Term> visited) {
    for (final Term term : visited) {
      if (term == this) {
        throw new InvalidTermException("Cycle detected");
      }
    }
    visited.add(this);
    for (final Object term : this.args) {
      if (term instanceof Struct) {
        ((Struct<?>) term).avoidCycle(visited);
      }
    }
  }


  // ---------------------------------------------------------------------------
  // Accessors
  // ---------------------------------------------------------------------------

  /**
   * Gets the number of elements of this structure
   */
  public int getArity() {
    return this.arity;
  }

  /**
   * Gets the functor name of this structure
   */
  public String getName() {
    return this.name;
  }

  public T getContent() {
    return content;
  }

  public void setContent(T content) {
    this.content = content;
  }

  // ---------------------------------------------------------------------------
  // Methods of java.lang.Object
  // ---------------------------------------------------------------------------


  @Override
  public int hashCode() {
    int result = this.getName().hashCode();
    result ^= this.arity << 8;
    for (int i = 0; i < this.arity; i++) {
      result ^= this.args[i].hashCode();
    }
    return result;
  }

  /**
   * @param other
   * @return true if other is a Struct of same arity, name, and all params are equal too.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Struct)) {
      return false;
    }
    final Struct<?> that = (Struct<?>) other;
    if (!(this.arity == that.arity && this.name == that.name)) { // Names are {@link String#intern()}alized so OK to check by reference
      return false;
    }
    for (int i = 0; i < this.arity; i++) {
      if (!this.args[i].equals(that.args[i])) {
        return false;
      }
    }
    return true;
  }

  // ---------------------------------------------------------------------------
  // Basic formatting
  // ---------------------------------------------------------------------------

  public String toString() {
    return termApi().formatStruct(this);
  }

}
