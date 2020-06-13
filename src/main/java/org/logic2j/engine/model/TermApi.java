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


import org.logic2j.engine.exception.InvalidTermException;
import org.logic2j.engine.visitor.ExtendedTermVisitor;
import org.logic2j.engine.visitor.TermVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.logic2j.engine.model.Var.strVar;

/**
 * Facade API to the {@link Term} hierarchy, to ease their handling. This class resides in the same package than the {@link Term}
 * subclasses, so they can invoke its package-scoped methods. See important notes re. Term factorization ({@link #factorize(Object)}) and
 * normalization ({@link #normalize(Object)} .
 *
 * @note This class knows about the subclasses of {@link Term}, it breaks the OO design pattern a little but avoid defining many methods
 * there. I find it acceptable since subclasses of {@link Term} don't sprout every day and are not for end-user extension.
 * @note Avoid static methods, prefer instantiating this class where needed.
 */
public class TermApi {

  private static final Pattern ATOM_PATTERN = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)");

  // TODO Currently unused but we probably should use an assertion method with very clean error handling as this one
  private static Struct requireStruct(Object theTerm, String theFunctor, int theArity) {
    final String functorSpec = theFunctor != null ? "functor \"" + theFunctor + '"' : "any functor";
    final String aritySpec = theArity >= 0 ? "arity=" + theArity : "any arity";
    if (!(theTerm instanceof Struct)) {
      final String message =
              "A Struct of " + functorSpec + " and " + aritySpec + " was expected, got instead: " + theTerm + " of class " + theTerm.getClass().getName();
      throw new InvalidTermException(message);
    }
    final Struct s = (Struct) theTerm;
    if (theFunctor != null && s.getName() != theFunctor) {
      throw new InvalidTermException("Got a Struct of wrong functor \"" + s.getName() + "\" instead of " + functorSpec + " and " + aritySpec);
    }
    if (theArity >= 0 && s.getArity() != theArity) {
      throw new InvalidTermException("Got a Struct of wrong arity (" + s.getArity() + ") instead of " + aritySpec);
    }
    return s;
  }

  /**
   * Apply a {@link ExtendedTermVisitor} to visit theTerm.
   *
   * @param theVisitor
   * @param theTerm
   * @return The transformed result as per theVisitor's logic
   */
  public <T> T accept(ExtendedTermVisitor<T> theVisitor, Object theTerm) {
    // Most common cases are Struct and Var, handled by super interface TermVisitor
    if (theTerm instanceof Struct) {
      return theVisitor.visit((Struct) theTerm);
    }
    if (theTerm instanceof Var) {
      return theVisitor.visit((Var) theTerm);
    }
    // Other possible cases require instanceof since any Object can be
    if (theTerm instanceof String) {
      return theVisitor.visit((String) theTerm);
    }
    return theVisitor.visit(theTerm);
  }

  public boolean isAtom(Object theTerm) {
    if (theTerm instanceof String) {
      // Now plain Strings are atoms!
      return true;
    }
    if (theTerm instanceof Struct) {
      final Struct s = (Struct) theTerm;
      return s.getArity() == 0;
    }
    return false;
  }

  public boolean isAtomic(Object theTerm) {
    return isAtom(theTerm) || theTerm instanceof Number;
  }

  /**
   * Check free variable (incl. anonymous)
   *
   * @param theTerm
   * @return true if theTerm denotes a free variable, or the anonymous variable.
   */
  public boolean isFreeVar(Object theTerm) {
    return theTerm instanceof Var;
  }

  /**
   * Check free variable (not including anonymous)
   *
   * @param theTerm
   * @return true if theTerm denotes a free variable, but not anonymous variable.
   */
  public boolean isFreeNamedVar(Object theTerm) {
    return theTerm instanceof Var && Var.anon() != theTerm;
  }

  /**
   * Recursively collect all terms and add them to the collectedTerms collection, and also initialize their Term.index to
   * {@link Term#NO_INDEX}. This is an internal template method: the public API entry point is {@link TermApi#collectTerms(Object)}; see a
   * more
   * detailed description there.
   *
   * @param collection Recipient collection, {@link Term}s add here.
   */
  public void collectTermsInto(Object theTerm, Collection<Object> collection) {
    if (theTerm instanceof Struct) {
      ((Struct) theTerm).collectTermsInto(collection);
    } else if (theTerm instanceof Var) {
      ((Var) theTerm).collectTermsInto(collection);
    } else {
      // Not a Term but a plain Java object
      collection.add(theTerm);
    }
  }

  /**
   * Recursively collect all terms at and under theTerm, and also reinit their Term.index to {@link Term#NO_INDEX}. For
   * example for a
   * structure "s(a,b(c),d(b(a)),X,X,Y)", the result Collection will hold [a, c, b(c), b(a), c(b(a)), X, X, Y]
   *
   * @param theTerm
   * @return A collection of terms, never empty. Same terms may appear multiple times.
   */
  public Collection<Object> collectTerms(Object theTerm) {
    final ArrayList<Object> recipient = new ArrayList<>();
    collectTermsInto(theTerm, recipient);
    // Remove ourself from the result - we are always at the end of the collection
    recipient.remove(recipient.size() - 1);
    return recipient;
  }

  /**
   * Factorize a {@link Term}, this means recursively traversing the {@link Term} structure and assigning any duplicates substructures to
   * the same references.
   *
   * @param theTerm
   * @return The factorized term, may be same as argument theTerm in case nothing was needed, or a new object.
   */
  public <T> T factorize(T theTerm) {
    final Collection<Object> collection = collectTerms(theTerm);
    return (T) factorize(theTerm, collection);
  }

  /**
   * Factorizing will either return a new {@link Term} or this {@link Term} depending if it already exists in the supplied Collection.
   * This will factorize duplicated atoms, numbers, variables, or even structures that are statically equal. A factorized {@link Struct}
   * will have all occurrences of the same {@link Var}iable sharing the same object reference. This is an internal template method: the
   * public API entry point is {@link TermApi#factorize(Object)}; see a more detailed description there.
   *
   * @return Either this, or a new equivalent but factorized Term.
   */
  public Object factorize(Object theTerm, Collection<Object> collection) {
    if (theTerm instanceof Struct) {
      return ((Struct) theTerm).factorize(collection);
    } else if (theTerm instanceof Var) {
      return ((Var) theTerm).factorize(collection);
    } else {
      // Not a Term but a plain Java object - won't factorize
      return theTerm;
    }
  }

  /**
   * Check structural equality, this means that the names of atoms, functors, arity and numeric values are all equal, that the same
   * variables are referred to, but irrelevant of the bound values of those variables.
   *
   * @param theOther
   * @return true when theOther is structurally equal to this. Same references (==) will always yield true.
   */
  public boolean structurallyEquals(Object theTerm, Object theOther) {
    if (theTerm instanceof Struct) {
      return ((Struct) theTerm).structurallyEquals(theOther);
    } else if (theTerm instanceof Var) {
      return ((Var) theTerm).structurallyEquals(theOther);
    } else {
      // Not a Term but a plain Java object - calculate equality
      return theTerm.equals(theOther);
    }
  }

  /**
   * Find the first instance of {@link Var} by name inside a Term, most often a {@link Struct}.
   *
   * @param theVariableName
   * @return A {@link Var} with the specified name, or null when not found.
   */
  public Var findVar(Object theTerm, String theVariableName) {
    if (theVariableName == Var.WHOLE_SOLUTION_VAR_NAME) {
      return Var.WHOLE_SOLUTION_VAR;
    }
    if (theTerm instanceof Struct) {
      return ((Struct) theTerm).findVar(theVariableName);
    } else if (theTerm instanceof Var && ((Var) theTerm).getName() == theVariableName) {
      return (Var) theTerm;
    } else {
      // Not a Term but a plain Java object - no var
      return null;
    }
  }

  /**
   * Assign the Term.index value for {@link Var} and {@link Struct}s.
   * Will recurse through Struct.
   *
   * @param theIndexOfNextNonIndexedVar
   * @return The next value for theIndexOfNextNonIndexedVar, allow successive calls to increment. First caller
   * must pass 0.
   */
  public int assignIndexes(Object theTerm, int theIndexOfNextNonIndexedVar) {
    if (theTerm instanceof Struct) {
      return ((Struct) theTerm).assignIndexes(theIndexOfNextNonIndexedVar);
    } else if (theTerm instanceof Var) {
      return ((Var) theTerm).assignIndexes(theIndexOfNextNonIndexedVar);
    } else {
      // Not a Term but a plain Java object - can't assign an index
      return theIndexOfNextNonIndexedVar;
    }
  }

  /**
   * A unique identifier that determines the family of the predicate represented by this {@link Struct}.
   *
   * @return The predicate's name + '/' + arity for normal {@link Struct}, or just the toString() of any other Object
   */
  public String predicateSignature(Object thePredicate) {
    if (thePredicate instanceof Struct) {
      return ((Struct) thePredicate).getPredicateSignature();
    }
    return thePredicate + "/0";
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
   * @param theText
   * @return theText, quoted if necessary (typically "X" will become "'X'" whereas "x" will remain unchanged.
   * Null will return null. The empty string will become "''". If not quoted, the same reference (theText) is returned.
   */
  public CharSequence quoteIfNeeded(CharSequence theText) {
    if (theText == null) {
      return null;
    }
    if (theText.length() == 0) {
      // Probably that the empty string is not allowed in regular Prolog
      return "''";
    }
    final String textAsString = theText.toString();
    final boolean needQuote =
            /* Fast check */ !Character.isLowerCase(theText.charAt(0)) ||
            /* For numbers */ textAsString.indexOf('.') >= 0 ||
            /* Much slower */ !ATOM_PATTERN.matcher(textAsString).matches();
    if (needQuote) {
      final StringBuilder sb = new StringBuilder(theText.length() + 2);
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
    return theText;
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
  void avoidCycle(Struct theClause) {
    final List<Term> visited = new ArrayList<>(20);
    theClause.avoidCycle(visited);
  }

  /**
   * Normalize a term, NOT taking into account existing operators and primitives.
   * In principle this method should not be used. Side-effect (execution) of primitives is not guaranteed to occur.
   *
   * @param theTerm To be normalized
   * @return A normalized COPY of theTerm ready to be used for inference (in a Theory ore as a goal)
   */
  public Object normalize(Object theTerm) {
    final Object factorized = factorize(theTerm);
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
   * @param theObject Should usually be {@link CharSequence}, {@link Number}, {@link Boolean}
   * @return An instance of a subclass of {@link Term}.
   * @throws InvalidTermException If theObject cannot be converted to a Term
   */
  public Object valueOf(Object theObject) {
    if (theObject == null) {
      throw new InvalidTermException("Cannot create Term from a null argument");
    }
    final Object result;
    if (theObject instanceof Term) {
      // Idempotence
      result = theObject;
    } else if (theObject instanceof Integer) {
      result = theObject;
    } else if (theObject instanceof Long) {
      result = ((Long) theObject).intValue();
    } else if (theObject instanceof Float) {
      result = ((Float) theObject).doubleValue();
    } else if (theObject instanceof Double) {
      result = theObject;
    } else if (theObject instanceof Boolean) {
      result = (Boolean) theObject ? Struct.ATOM_TRUE : Struct.ATOM_FALSE;
    } else if (theObject instanceof CharSequence || theObject instanceof Character) {
      // Very very vary rudimentary parsing
      final String chars = theObject.toString();

      if (Var.ANONYMOUS_VAR_NAME.equals(chars)) {
        result = Var.anon();
      } else if (chars.isEmpty()) {
        // Dubious for real programming, but some data sources may contain empty fields, and this is the only way to represent
        // them
        // as a Term
        result = new Struct("");
      } else if (Character.isUpperCase(chars.charAt(0)) || chars.startsWith(Var.ANONYMOUS_VAR_NAME)) {
        // Use Prolog's convention re variables starting with uppercase or underscore
        result = strVar(chars);
      } else {
        // Otherwise it's an atom
        result = chars.intern();
      }
    } else if (theObject instanceof Number) {
      // Other types of numbers
      final Number nbr = (Number) theObject;
      if (nbr.doubleValue() % 1 != 0) {
        // Has floating point number
        result = nbr.doubleValue();
      } else {
        // Is just an integer
        result = nbr.longValue();
      }
    } else if (theObject instanceof Enum<?>) {
      // Enums are just valid terms
      result = theObject;
    } else {
      // POJOs are also valid terms now
      result = theObject;
    }
    return result;
  }

  /**
   * All distinct (unique) Vars in the specified term.
   *
   * @param term
   * @return Array of unique Vars, in the order found by depth-first traversal.
   */
  public Var[] distinctVars(Object term) {
    final Var[] tempArray = new Var[100]; // Enough for the moment - we could plan an auto-allocating array if needed, I doubt it
    final int[] nbVars = new int[]{0};

    final TermVisitor<Void> findVarsVisitor = new TermVisitor<Void>() {
      @Override
      public Void visit(Var theVar) {
        if (!theVar.isAnon()) {
          // Insert into array (even if may duplicate) - this will act as a sentinel
          final int highest = nbVars[0];
          tempArray[highest] = theVar;
          // Search if we already have this var in the array - due to the sentinel we will always find it!
          int foundIndex = 0;
          while (tempArray[foundIndex] != theVar) {
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
      public Void visit(Struct theStruct) {
        // Recurse through children
        final Object[] args = theStruct.getArgs();
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
