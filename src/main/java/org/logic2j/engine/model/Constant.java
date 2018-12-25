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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * A typed {@link Binding} that provides a constant value (scalar) or multiple constant values (vector)
 * from various collections or suppliers of Java objects.
 * Use static factories in {@link SimpleBindings} to instantiate {@link Constant}s.
 */
public interface Constant<T> extends Binding<T> {

  /**
   * @return true if data comes from a stream that cannot be consumed more than once.
   */
  boolean isUniqueFeed();

  /**
   * Calculate the size. In case of a stream this will consumes it.
   *
   * @return Cardinality of data: 0=empty, 1=scalar, >1=vector, -1=unknown
   */
  long size();

  /**
   * Check content; in case of a stream this will consumes it.
   *
   * @param value
   * @return
   */
  boolean contains(T value);

  /**
   * Convert to array; in case of a stream this will consumes it.
   *
   * @return
   */
  T[] toArray();

  /**
   * Convert to single value; in case of a stream this will consumes it.
   *
   * @return
   */
  T toScalar();

  /**
   * Convert to a Stream.
   *
   * @return
   */
  Stream<T> toStream();

  default List<T> toList() {
    return Arrays.asList(toArray());
  }
}
