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

import static org.assertj.core.api.Assertions.assertThat;
import static org.logic2j.engine.model.Var.anon;
import static org.logic2j.engine.model.Var.anyVar;

import org.junit.Test;
import org.logic2j.engine.exception.InvalidTermException;

public class VarTest {

  @Test
  public void constructorValid() {
    final Var<?>v1 = anyVar("X");
    assertThat(v1.getName()).isEqualTo("X");
    assertThat(v1.hasIndex()).isFalse();
  }

  @Test(expected = InvalidTermException.class)
  public void constructorNull() {
    anyVar(null);
  }

  @Test(expected = InvalidTermException.class)
  public void constructorSameAsAnonymous1() {
    anyVar("_");
  }

  @Test(expected = InvalidTermException.class)
  public void constructorSameAsAnonymous() {
    anyVar("_"); // Voluntary composition via StringBuilder
  }

  @Test(expected = InvalidTermException.class)
  public void constructorEmpty() {
    anyVar("");
  }

  @Test(expected = InvalidTermException.class)
  public void constructorCannotInstantiateAnonymous() {
    anyVar("_");
  }


  @Test
  public void constructorWithCharSequence() {
    final Var<?>v1 = anyVar(new StringBuilder("X"));
    assertThat(v1.getName()).isEqualTo("X");
    assertThat(v1.hasIndex()).isFalse();
  }

  @Test
  public void automaticName() {
    final Var<String> stringVar = new Var<>(String.class);
    final String name1 = stringVar.getName();
    assertThat(name1).isNotEmpty();
    assertThat(name1).startsWith("_");
    final Var<String> stringVar2 = new Var<>(String.class);
    final String name2 = stringVar2.getName();
    assertThat(name2).isNotSameAs(name1);
  }

  @Test
  public void idempotence() {
    final Var<?>v1 = anyVar("X");
    assertThat(v1).isEqualTo(v1);
  }


  @Test
  public void equality() {
    final Var<?>v1 = anyVar("X");
    final Var<?>v2 = anyVar("X");
    assertThat(v2).isNotSameAs(v1);
    assertThat(v2).isEqualTo(v1);
    assertThat(v1).isEqualTo(v2);
  }


  @Test
  public void lowerCaseIsValid() {
    final Var<?>v1 = anyVar("lowercase");
    assertThat(v1.getName()).isEqualTo("lowercase");
    assertThat(v1.hasIndex()).isFalse();
  }


  @Test(expected = InvalidTermException.class)
  public void cannotCloneAnonymous() {
    Var.copy(anon());
  }

  @Test
  public void isAnonymousTrue() {
    assertThat(anon().isAnon()).isTrue();
  }

  @Test
  public void isAnonymousFalse() {
    assertThat(anyVar("X").isAnon()).isFalse();
  }

  @Test
  public void isUnderscoredFalse() {
    assertThat(anyVar("X").isUnderscoredVar()).isFalse();
  }

  @Test
  public void isUnderscoredTrue() {
    assertThat(anyVar("_x").isUnderscoredVar()).isTrue();
  }

}
