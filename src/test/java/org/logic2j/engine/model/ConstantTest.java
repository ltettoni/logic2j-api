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

import java.util.Random;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.logic2j.engine.model.SimpleBindings.bind;
import static org.logic2j.engine.model.SimpleBindings.empty;

public class ConstantTest {

  @Test
  public void emptyBinding() {
    final Constant<Integer> binding = empty(Integer.class);
    assertThat(binding.size()).isEqualTo(0L);
    assertThat(binding.isUniqueFeed()).isFalse();
    assertThat(binding.toArray().length).isEqualTo(0);
    assertThat(binding.toStream().count()).isEqualTo(0L);
  }

  @Test
  public void array() {
    final Constant<Integer> binding = bind(1, 2, 3, 4, 5, 4, 3, 2, 1);
    assertThat(binding.size()).isEqualTo(9L);
    // Can get several times as an array
    assertThat(binding.toArray().length).isEqualTo(9);
    assertThat(binding.toArray().length).isEqualTo(9);
    // But can get also several times as Stream
    assertThat(binding.toStream().count()).isEqualTo(9L);
    assertThat(binding.toStream().count()).isEqualTo(9L);
  }

  @Test
  public void setStream1() {
    final Constant<Long> binding = bind(LongStream.range(1, 1000).boxed());
    assertThat(binding.isUniqueFeed()).isTrue();
    assertThat(binding.size()).isEqualTo(999L);
    // Can get several times as an array
    assertThat(binding.toArray().length).isEqualTo(999);
    assertThat(binding.toArray().length).isEqualTo(999);
  }

  @Test
  public void setIterator1() {
    final Constant<Long> binding = bind(LongStream.range(1, 1000).boxed().toList().iterator());
    assertThat(binding.isUniqueFeed()).isTrue();
    assertThat(binding.size()).isEqualTo(999L);
  }

  @Test
  public void infiniteStream1() {
    final Constant<Integer> binding = bind(new Random().ints().limit(20000).boxed());
    assertThat(binding.isUniqueFeed()).isTrue();
    assertThat(binding.size()).isEqualTo(20000L);
  }

  @Test(expected = IllegalStateException.class)
  public void consumeStream() {
    final Constant<Integer> binding = bind(new Random().ints().limit(10).boxed());
    assertThat(binding.toStream().count()).isEqualTo(10L);
    assertThat(binding.toStream().count()).isEqualTo(10L);
  }

}
