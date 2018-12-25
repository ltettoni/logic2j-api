package org.logic2j.api.result;

import java.util.List;
import java.util.Set;

public interface ValueHolder<T> {
    boolean exists();

    int count();

    List<T> list();

    Set<T> set();

    ValueHolder<T> exactly(int nbFirst);
    ValueHolder<T> atMost(int nbFirst);
    ValueHolder<T> atLeast(int nbFirst);
    ValueHolder<T> limit(int nbFirst);
    ValueHolder<T> page(int first, int number);

    ValueHolder<T> distinct();

    T min();
    T max();


    T single();
    T first();
    T unique();


    T sum();
}
