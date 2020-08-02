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

/**
 * Use {@link Binding}s to pass {@link Var}s or {@link Constant}s as arguments to a {@link Struct} predicate.
 *
 * The reason for this interface is to support:
 * - Strong typing: Although the arguments to a {@link Struct} may be any Java Object, you may prefer strongly-typed predicates which you
 * derive from Struct. For that use {@link Binding} with a generic type.
 * - Single or multiple values
 * - Allow to pass input (constants) our output (free {@link Var}s) to predicates.
 * - Be a "pivot" object for many ways to specify values (collectons, arrays, suppliers, etc). This reduces the need for too
 * many predicate constructors.
 *
 * You can find implementations, and a static factory in {@link SimpleBindings#newBinding(Object)}
 */
public interface Binding<T> {

  /**
   * @return The effective type of the bound values.
   */
  Class<T> getType();

}
