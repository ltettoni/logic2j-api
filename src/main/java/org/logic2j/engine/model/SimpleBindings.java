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


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Static factories for {@link Constant}, used to provide data to predicates: one or several values of a given type.
 * Can provide values from {@link Iterator}s or {@link Stream}s.
 * Only one method: {@link #empty(Class)} allows empty content. Other methods require at least one element to determine the data type.
 * TODO: factories for collections should maybe ensure the data type by scanning all elements, not only checking on the first?
 */
public class SimpleBindings {

  /**
   * Forbid instantiation
   */
  private SimpleBindings() {
  }

  /**
   * @param type
   * @param <T>
   * @return A {@link Constant} representing no data.
   */
  public static <T> Constant<T> empty(Class<T> type) {
    return new ConstantBase<T>() {

      @Override
      public boolean isUniqueFeed() {
        return false;
      }

      @Override
      public long size() {
        return 0;
      }

      @Override
      public T[] toArray() {
        return genericArray(type, 0);
      }

      @Override
      public T toScalar() {
        return null;
      }

      @Override
      public Stream<T> toStream() {
        return Stream.empty();
      }

      @Override
      public boolean contains(T value) {
        return false;
      }

      @Override
      public Class getType() {
        return type;
      }
    };
  }

  /**
   * @param supplier
   * @param <T>
   * @return A {@link Constant} that supplies one value, must be non-null so that its type can be determined.
   * @note If you need to optionally have null values specify an {@link java.util.Optional}
   */
  public static <T> Constant<T> bind(Supplier<T> supplier) {
    return new ConstantBase<T>() {
      @Override
      public boolean isUniqueFeed() {
        return false;
      }

      @Override
      public long size() {
        return 1;
      }

      @Override
      public T[] toArray() {
        final T single = supplier.get();
        final T[] objects = genericArray((Class<T>) single.getClass(), 1);
        objects[0] = single;
        return objects;
      }

      @Override
      public T toScalar() {
        return supplier.get();
      }

      @Override
      public Stream<T> toStream() {
        return Stream.of(supplier.get());
      }

      @Override
      public boolean contains(T value) {
        return value.equals(supplier.get());
      }

    };
  }

  /**
   * @param values
   * @param <T>
   * @return A {@link Constant} that supplies several values.
   */
  @SafeVarargs
  public static <T> Constant<T> bind(T... values) {
    return new ConstantBase<T>() {
      @Override
      public boolean isUniqueFeed() {
        return false;
      }

      @Override
      public Class getType() {
        if (values.length == 0) {
          throw new IllegalStateException("Trying to get type from empty array");
        }
        final T first = values[0];
        if (first == null) {
          throw new IllegalStateException("Cannot determine type from array, first element is null");
        }
        return first.getClass();
      }

      @Override
      public long size() {
        return values.length;
      }

      @Override
      public T[] toArray() {
        return values;
      }

      @Override
      public T toScalar() {
        if (values.length != 1) {
          throw new IllegalStateException("Trying to get scalar from array of " + values.length + " elements: " + Arrays.asList(values));
        }
        return values[0];
      }

      @Override
      public Stream<T> toStream() {
        return Arrays.stream(values);
      }

      @Override
      public boolean contains(T value) {
        for (T value1 : values) {
          if (value.equals(value1)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Bind a Java collection.
   *
   * @param coll
   * @param <T>
   * @return A {@link Constant} that supplies several values.
   */
  public static <T> Constant<T> bind(Collection<T> coll) {
    return new ConstantBase<T>() {
      @Override
      public boolean isUniqueFeed() {
        return false;
      }

      @Override
      public Class getType() {
        if (coll.isEmpty()) {
          throw new IllegalStateException("Trying to get type from empty collection");
        }
        final T first = coll.iterator().next();
        if (first == null) {
          throw new IllegalStateException("Cannot determine type from collection, first element is null");
        }
        return first.getClass();
      }

      @Override
      public long size() {
        return coll.size();
      }

      @Override
      public T[] toArray() {
        return (T[]) coll.toArray();
      }

      @Override
      public T toScalar() {
        if (coll.size() != 1) {
          throw new IllegalStateException("Trying to get scalar from collection of " + coll.size() + " elements: " + coll);
        }
        return coll.iterator().next();
      }

      @Override
      public Stream<T> toStream() {
        return coll.stream();
      }

      @Override
      public boolean contains(T value) {
        return coll.contains(value);
      }
    };
  }

  /**
   * Consume the stream immediately and only once, cache all data in this object.
   *
   * @param stream
   * @param <T>
   * @return
   */
  public static <T> Constant<T> bind(Stream<T> stream) {
    return new ConstantBase<T>() {
      private T[] data = null;

      @Override
      public boolean isUniqueFeed() {
        return data == null;
      }

      @Override
      public long size() {
        consumeNow();
        return this.data.length;
      }

      @Override
      public T[] toArray() {
        consumeNow();
        return data;
      }

      @Override
      public T toScalar() {
        consumeNow();
        return Arrays.stream(this.data).findFirst().orElseThrow(() -> new IllegalArgumentException("Empty stream cannot provide a scalar form it"));
      }

      @Override
      public boolean contains(T value) {
        consumeNow();
        return Arrays.asList(this.data).contains(value);
      }

      @Override
      public Stream<T> toStream() {
        return stream;
      }

      private void consumeNow() {
        if (this.data == null) {
          final Object[] asObjects = stream.toArray(Object[]::new);
          if (asObjects.length == 0) {
            throw new IllegalArgumentException("Empty SimpleBinding stream, cannot determine data type of instances.");
          }
          final Class<T> elementClass = (Class<T>) asObjects[0].getClass();
          this.data = Arrays.stream(asObjects).toArray(n -> (T[]) Array.newInstance(elementClass, n));
        }
      }

      @Override
      public String toString() {
        return "Constant>Stream(values-not-shown)";
      }
    };
  }

  /**
   * Consume the iterator immediately and only once, cache all data in this object.
   *
   * @param iterator
   * @param <T>
   * @return
   */
  public static <T> Constant<T> bind(Iterator<T> iterator) {
    return new ConstantBase<T>() {
      private T[] data = null;

      @Override
      public boolean isUniqueFeed() {
        return data == null;
      }

      @Override
      public long size() {
        consumeNow();
        return this.data.length;
      }

      @Override
      public T[] toArray() {
        consumeNow();
        return data;
      }

      @Override
      public T toScalar() {
        consumeNow();
        return Arrays.stream(this.data).findFirst().orElseThrow(() -> new IllegalArgumentException("Empty stream cannot provide a scalar form it"));
      }

      @Override
      public boolean contains(T value) {
        consumeNow();
        for (T elem : this.data) {
          if (value.equals(elem)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public Stream<T> toStream() {
        final Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
      }

      private void consumeNow() {
        if (this.data == null) {
          final List<T> coll = new ArrayList<>();
          iterator.forEachRemaining(coll::add);
          if (coll.isEmpty()) {
            throw new IllegalArgumentException("Empty Constant iterator, cannot determine data type of instances.");
          }
          final Class<T> elementType = (Class<T>) coll.get(0).getClass();
          this.data = coll.stream().toArray(n -> (T[]) Array.newInstance(elementType, n));
        }
      }

      @Override
      public String toString() {
        return "Constant>Iterator(values-not-shown)";
      }
    };
  }

  public static <T> Constant<T> bind(Iterable<T> iterable) {
    return bind(iterable.iterator());
  }

  private abstract static class ConstantBase<T> implements Constant<T> {

    protected static <T> T[] genericArray(Class<T> elementType, int length) {
      return (T[]) Array.newInstance(elementType, length);
    }

    @Override
    public Class getType() {
      return toScalar().getClass();
    }

    public String toString() {
      final StringBuilder sb = new StringBuilder();
      if (getType() != null) {
        sb.append(getType().getSimpleName());
        sb.append('s');
      } else {
        sb.append("NoType");
      }
      if (this.size() >= 0) {
        sb.append(this.toStream().map(Object::toString).collect(Collectors.joining(",", "<", ">")));
      } else {
        sb.append("<no-data-yet>");
      }
      return sb.toString();
    }

  }
}
