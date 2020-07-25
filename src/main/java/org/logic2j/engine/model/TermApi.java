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


import static org.logic2j.engine.model.Var.strVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.logic2j.engine.exception.InvalidTermException;
import org.logic2j.engine.visitor.ExtendedTermVisitor;
import org.logic2j.engine.visitor.TermVisitor;

/**
 * Facade API to the {@link Term} hierarchy, to ease their handling. This class resides in the same package than the {@link Term}
 * subclasses, so they can invoke its package-scoped methods. See important notes re. Term factorization ({@link #factorize(Object)}) and
 * normalization ({@link #normalize(Object)} .
 *
 * Note: This class knows about the subclasses of {@link Term}, it breaks the OO design pattern a little but avoid defining many methods
 * there. I find it acceptable since subclasses of {@link Term} don't sprout every day and are not for end-user extension.
 * Note: Avoid static methods, prefer instantiating this class where needed.
 */
public class TermApi {

  private static final Pattern ATOM_PATTERN = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)");

  // TODO Currently unused but we probably should use an assertion method with very clean error handling as this one
  private static Struct<?> requireStruct(Object term, String functor, int arity) {
    final String functorSpec = functor != null ? "functor \"" + functor + '"' : "any functor";
    final String aritySpec = arity >= 0 ? "arity=" + arity : "any arity";
    if (!(term instanceof Struct)) {
      final String message =
              "A Struct of " + functorSpec + " and " + aritySpec + " was expected, got instead: " + term + " of class " + term.getClass().getName();
      throw new InvalidTermException(message);
    }
    final Struct<?> s = (Struct<?>) term;
    //noinspection StringEquality - we internalized strings so it is licit to copmare references
    if (functor != null && s.getName() != functor) {
      throw new InvalidTermException("Got a Struct of wrong functor \"" + s.getName() + "\" instead of " + functorSpec + " and " + aritySpec);
    }
    if (arity >= 0 && s.getArity() != arity) {
      throw new InvalidTermException("Got a Struct of wrong arity (" + s.getArity() + ") instead of " + aritySpec);
    }
    return s;
  }

  /**
   * Apply a {@link ExtendedTermVisitor} to visit term.
   *
   * @param visitor
   * @param term
   * @return The transformed result as per visitor's logic
   */
  public <T> T accept(ExtendedTermVisitor<T> visitor, Object term) {
    // Most common cases are Struct and Var, handled by super interface TermVisitor
    if (term instanceof Struct) {
      return visitor.visit((Struct<?>) term);
    }
    if (term instanceof Var) {
      return visitor.visit((Var<?>) term);
    }
    // Other possible cases require instanceof since any Object can be
    if (term instanceof String) {
      return visitor.visit((String) term);
    }
    return visitor.visit(term);
  }

  public boolean isAtom(Object term) {
    if (term instanceof String) {
      // Now plain Strings are atoms!
      return true;
    }
    if (term instanceof Struct) {
      final Struct<?> s = (Struct<?>) term;
      return s.getArity() == 0;
    }
    return false;
  }

  public boolean isAtomic(Object term) {
    return isAtom(term) || term instanceof Number;
  }

  /**
   * Check free variable (incl. anonymous)
   *
   * @param term
   * @return true if term denotes a free variable, or the anonymous variable.
   */
  public boolean isFreeVar(Object term) {
    return term instanceof Var;
  }

  /**
   * Check free variable (not including anonymous)
   *
   * @param term
   * @return true if term denotes a free variable, but not anonymous variable.
   */
  public boolean isFreeNamedVar(Object term) {
    return term instanceof Var<?>&& Var.anon() != term;
  }

  /**
   * Recursively collect all terms and add them to the collectedTerms collection, and also initialize their Term.index to
   * {@link Term#NO_INDEX}. This is an internal template method: the public API entry point is {@link TermApi#collectTerms(Object)}; see a
   * more
   * detailed description there.
   *
   * @param collection Recipient collection, {@link Term}s add here.
   */
  public void collectTermsInto(Object term, Collection<Object> collection) {
    if (term instanceof Struct) {
      ((Struct<?>) term).collectTermsInto(collection);
    } else if (term instanceof Var) {
      ((Var<?>) term).collectTermsInto(collection);
    } else {
      // Not a Term but a plain Java object
      collection.add(term);
    }
  }

  /**
   * Recursively collect all terms at and under term, and also reinit their Term.index to {@link Term#NO_INDEX}. For
   * example for a
   * structure "s(a,b(c),d(b(a)),X,X,Y)", the result Collection will hold [a, c, b(c), b(a), c(b(a)), X, X, Y]
   *
   * @param term
   * @return A collection of terms, never empty. Same terms may appear multiple times.
   */
  public Collection<Object> collectTerms(Object term) {
    final ArrayList<Object> recipient = new ArrayList<>();
    collectTermsInto(term, recipient);
    // Remove ourself from the result - we are always at the end of the collection
    recipient.remove(recipient.size() - 1);
    return recipient;
  }

  /**
   * Factorize a {@link Term}, this means recursively traversing the {@link Term} structure and assigning any duplicates substructures to
   * the same references.
   *
   * @param term
   * @return The factorized term, may be same as argument term in case nothing was needed, or a new object.
   */
  public <T> T factorize(T term) {
    final Collection<Object> collection = collectTerms(term);
    return (T) factorize(term, collection);
  }

  /**
   * Factorizing will either return a new {@link Term} or this {@link Term} depending if it already exists in the supplied Collection.
   * This will factorize duplicated atoms, numbers, variables, or even structures that are statically equal. A factorized {@link Struct}
   * will have all occurrences of the same {@link Var}iable sharing the same object reference. This is an internal template method: the
   * public API entry point is {@link TermApi#factorize(Object)}; see a more detailed description there.
   *
   * @return Either this, or a new equivalent but factorized Term.
   */
  public Object factorize(Object term, Collection<Object> collection) {
    if (term instanceof Struct) {
      return ((Struct<?>) term).factorize(collection);
    } else if (term instanceof Var) {
      return ((Var<?>) term).factorize(collection);
    } else {
      // Not a Term but a plain Java object - won't factorize
      return term;
    }
  }

  /**
   * Check structural equality, this means that the names of atoms, functors, arity and numeric values are all equal, that the same
   * variables are referred to, but irrelevant of the bound values of those variables.
   *
   * @param theOther
   * @return true when theOther is structurally equal to this. Same references (==) will always yield true.
   */
  public boolean structurallyEquals(Object term, Object theOther) {
    if (term instanceof Struct) {
      return ((Struct<?>) term).structurallyEquals(theOther);
    } else if (term instanceof Var) {
      return ((Var<?>) term).structurallyEquals(theOther);
    } else {
      // Not a Term but a plain Java object - calculate equality
      return term.equals(theOther);
    }
  }

  /**
   * Find the first instance of {@link Var} by name inside a Term, most often a {@link Struct}.
   *
   * @param varName
   * @return A {@link Var} with the specified name, or null when not found.
   */
  public Var<?>findVar(Object term, String varName) {
    //noinspection StringEquality - we internalized strings so it is licit to copmare references
    if (varName == Var.WHOLE_SOLUTION_VAR_NAME) {
      return Var.WHOLE_SOLUTION_VAR;
    }
    if (term instanceof Struct) {
      return ((Struct<?>) term).findVar(varName);
    } else //noinspection StringEquality - we internalized strings so it is licit to copmare references
      if (term instanceof Var<?> && ((Var<?>) term).getName() == varName) {
      return (Var<?>) term;
    } else {
      // Not a Term but a plain Java object - no var
      return null;
    }
  }

  /**
   * Assign the Term.index value for {@link Var} and {@link Struct}s.
   * Will recurse through Struct.
   *
   * @param indexOfNextNonIndexedVar
   * @return The next value for indexOfNextNonIndexedVar, allow successive calls to increment. First caller
   * must pass 0.
   */
  public int assignIndexes(Object term, int indexOfNextNonIndexedVar) {
    if (term instanceof Struct) {
      return ((Struct<?>) term).assignIndexes(indexOfNextNonIndexedVar);
    } else if (term instanceof Var) {
      return ((Var<?>) term).assignIndexes(indexOfNextNonIndexedVar);
    } else {
      // Not a Term but a plain Java object - can't assign an index
      return indexOfNextNonIndexedVar;
    }
  }


  /**
   * Depth-first traversal with potential remapping of any {@link Struct} instance.
   *
   * @param term         Term to traverse
   * @param structMapper Transformation function
   * @return When nothing changed, must return the argument term (same reference), otherwise
   * return transformed object
   */
  public Object depthFirstStructTransform(Object term, Function<Struct<?>, Struct<?>> structMapper) {
    if (!(term instanceof Struct<?>)) {
      return term;
    }
    final Struct struct = (Struct<?>) term;
    final Object[] args = struct.getArgs();
    if (args.length > 0) {
      // Transform args, depth first
      final Object[] newArgs = new Object[args.length];
      boolean argsChanged = false;
      for (int i = 0; i < args.length; i++) {
        final Object argi = struct.getArg(i);
        if (argi instanceof Struct<?>) {
          final Struct<?> remapped = structMapper.apply((Struct<?>) argi);
          if (remapped != argi) {
            argsChanged = true;
          }
          newArgs[i] = remapped;
        } else {
          newArgs[i] = argi;
        }
      }
      if (argsChanged) {
        return structMapper.apply(struct.cloneWithNewArguments(newArgs));
      } else {
        return structMapper.apply(struct);
      }
    } else {
      return structMapper.apply(struct);
    }
  }


  /**
   * A unique identifier that determines the family of the predicate represented by this {@link Struct}.
   *
   * @return The predicate's name + '/' + arity for normal {@link Struct}, or just the toString() of any other Object
   */
  public String predicateSignature(Object predicate) {
    if (predicate instanceof Struct) {
      return ((Struct<?>) predicate).getPredicateSignature();
    }
    return predicate + "/0";
  }

  public String functorFromSignature(String signature) {
    int pos = signature.lastIndexOf('/');
    if (pos <= 0) {
      throw new InvalidTermException("Cannot find character '/' in predicate signature \"" + signature + "\" (supposed to be functor/arity)");
    }
    return signature.substring(0, pos);
  }

  public int arityFromSignature(String signature) {
    int pos = signature.lastIndexOf('/');
    if (pos <= 0) {
      throw new InvalidTermException("Cannot find character '/' in predicate signature \"" + signature + "\" (supposed to be functor/arity)");
    }
    return Integer.parseInt(signature.substring(pos + 1));
  }

  /**
   * Quote atoms if needed.
   *
   * @param text
   * @return theText, quoted if necessary (typically "X" will become "'X'" whereas "x" will remain unchanged.
   * Null will return null. The empty string will become "''". If not quoted, the same reference (theText) is returned.
   */
  public CharSequence quoteIfNeeded(CharSequence text) {
    if (text == null) {
      return null;
    }
    if (text.length() == 0) {
      // Probably that the empty string is not allowed in regular Prolog
      return "''";
    }
    final String textAsString = text.toString();
    final boolean needQuote =
            /* Fast check */ !Character.isLowerCase(text.charAt(0)) ||
            /* For numbers */ textAsString.indexOf('.') >= 0 ||
            /* Much slower */ !ATOM_PATTERN.matcher(textAsString).matches();
    if (needQuote) {
      final StringBuilder sb = new StringBuilder(text.length() + 2);
      sb.append(Struct.QUOTE); // Opening quote
      for (final char c : textAsString.toCharArray()) {
        sb.append(c);
        if (c == Struct.QUOTE) {
          sb.append(c); // Quotes are doubled
        }
      }
      sb.append(Struct.QUOTE); // Closing quote
      return sb;
    }
    return text;
  }

  public <T> String formatStruct(Struct<T> struct) {
    final StringBuilder sb = new StringBuilder();
    final int nArity = struct.getArity();
    sb.append(quoteIfNeeded(struct.getName()));
    if (nArity > 0) {
      sb.append(Struct.PAR_OPEN);
      for (int c = 0; c < nArity; c++) {
        final Object arg = struct.getArg(c);
        final String formatted = arg.toString();
        sb.append(formatted);
        if (c < nArity - 1) {
          sb.append(Struct.ARG_SEPARATOR);
        }
      }
      sb.append(Struct.PAR_CLOSE);
    }
    return sb.toString();
  }

  // TODO Currently unused - but probably we should detect cycles!
  void avoidCycle(Struct<?> clause) {
    final List<Term> visited = new ArrayList<>(20);
    clause.avoidCycle(visited);
  }

  /**
   * Normalize a term: factorize common sub-terms (see {@link #factorize(Object)}
   * and assigning var indexes.
   * This method should be overloaded in a real Prolog execution environment where
   * primitives and operators need to be dealt with.
   *
   * @param term To be normalized
   * @return A normalized COPY of term ready to be used for inference (in a Theory ore as a goal)
   */
  public Object normalize(Object term) {
    final Object factorized = factorize(term);
    assignIndexes(factorized, 0);
    return factorized;
  }

  /**
   * Primitive factory for simple {@link Term}s from plain Java {@link Object}s, use this
   * with parsimony at low-level.
   * <p>
   * Character input will be converted to Struct or Var according to Prolog's syntax convention:
   * when starting with an underscore or an uppercase, this is a {@link Var}.
   * This method is not capable of instantiating a compound {@link Struct}, it may only create atoms.
   *
   * @param anyObject Should usually be {@link CharSequence}, {@link Number}, {@link Boolean}
   * @return An instance of a subclass of {@link Term}.
   * @throws InvalidTermException If theObject cannot be converted to a Term
   */
  public Object valueOf(Object anyObject) {
    if (anyObject == null) {
      throw new InvalidTermException("Cannot create Term from a null argument");
    }
    final Object result;
    if (anyObject instanceof Term) {
      // Idempotence
      result = anyObject;
    } else if (anyObject instanceof Integer) {
      result = anyObject;
    } else if (anyObject instanceof Long) {
      result = ((Long) anyObject).intValue();
    } else if (anyObject instanceof Float) {
      result = ((Float) anyObject).doubleValue();
    } else if (anyObject instanceof Double) {
      result = anyObject;
    } else if (anyObject instanceof Boolean) {
      result = (Boolean) anyObject ? Struct.ATOM_TRUE : Struct.ATOM_FALSE;
    } else if (anyObject instanceof CharSequence || anyObject instanceof Character) {
      // Very very vary rudimentary parsing
      final String chars = anyObject.toString();

      if (Var.ANONYMOUS_VAR_NAME.equals(chars)) {
        result = Var.anon();
      } else if (chars.isEmpty()) {
        // Dubious for real programming, but some data sources may contain empty fields, and this is the only way to represent
        // them
        // as a Term
        result = new Struct<>("");
      } else if (Character.isUpperCase(chars.charAt(0)) || chars.startsWith(Var.ANONYMOUS_VAR_NAME)) {
        // Use Prolog's convention re variables starting with uppercase or underscore
        result = strVar(chars);
      } else {
        // Otherwise it's an atom
        result = chars.intern();
      }
    } else if (anyObject instanceof Number) {
      // Other types of numbers
      final Number nbr = (Number) anyObject;
      if (nbr.doubleValue() % 1 != 0) {
        // Has floating point number
        result = nbr.doubleValue();
      } else {
        // Is just an integer
        result = nbr.longValue();
      }
    } else if (anyObject instanceof Enum<?>) {
      // Enums are just valid terms
      result = anyObject;
    } else {
      // POJOs are also valid terms now
      result = anyObject;
    }
    return result;
  }

  /**
   * All distinct (unique) Vars in the specified term.
   *
   * @param term
   * @return Array of unique Vars, in the order found by depth-first traversal.
   */
  public Var<?>[] distinctVars(Object term) {
    final Var<?>[] tempArray = new Var[100]; // Enough for the moment - we could plan an auto-allocating array if needed, I doubt it
    final int[] nbVars = new int[]{0};

    final TermVisitor<Void> findVarsVisitor = new TermVisitor<Void>() {
      @Override
      public Void visit(Var<?> var) {
        if (!var.isAnon()) {
          // Insert into array (even if may duplicate) - this will act as a sentinel
          final int highest = nbVars[0];
          tempArray[highest] = var;
          // Search if we already have this var in the array - due to the sentinel we will always find it!
          int foundIndex = 0;
          while (tempArray[foundIndex] != var) {
            foundIndex++;
          }
          // Did we hit the sentinel?
          if (foundIndex == highest) {
            // Was not present already - let's use the sentinel as a real value - increment size of found vars
            nbVars[0]++;
          } // Else: already present - leave the sentinel there we don't care but don't consider it as a new value
        }
        return null;
      }

      @Override
      public Void visit(Struct<?> struct) {
        // Recurse through children
        final Object[] args = struct.getArgs();
        for (final Object arg : args) {
          if (arg instanceof Term) {
            ((Term) arg).accept(this);
          }
        }
        return null;
      }
    };
    if (term instanceof Term) {
      ((Term) term).accept(findVarsVisitor);
    }
    // Now copy the values found as the tempArray
    return Arrays.copyOf(tempArray, nbVars[0]);
  }

}
