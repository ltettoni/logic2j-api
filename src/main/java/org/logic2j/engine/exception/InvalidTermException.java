/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.logic2j.engine.exception;

import java.io.Serial;

/**
 * Exception used to indicate that a {@link org.logic2j.engine.model.Term} cannot be parsed from a text, of is used in a context where it should not.
 */
public class InvalidTermException extends Logic2jException {

  @Serial
  private static final long serialVersionUID = -4416801118548866803L;

  public InvalidTermException(String message) {
    super(message);
  }

  public InvalidTermException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

}
