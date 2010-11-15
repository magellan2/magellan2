// class magellan.library.Order
// created on Jul 28, 2010
//
// Copyright 2003-2010 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library;

import java.util.List;

import magellan.library.gamebinding.ExecutionState;
import magellan.library.tasks.Problem;
import magellan.library.utils.OrderToken;

/**
 * Represents a unit order (one line).
 */
public interface Order {

  /** stands for the amount "ALLES" */
  public static final int ALL = Integer.MIN_VALUE + 1;

  /**
   * @return <code>true</code> if the order consists of no tokens or only the
   *         {@link OrderToken#TT_EOC} token.
   */
  public boolean isEmpty();

  /**
   * @return <code>true</code> if the order has been found valid by the order parser.
   */
  public boolean isValid();

  //
  // /**
  // * Changes the valid property.
  // *
  // * @param valid
  // */
  // public void setValid(boolean valid);

  /**
   * Determines if this is a persistent ("@") order.
   */
  public void setPersistent(boolean b);

  /**
   * @return true if this is a persistent ("@") order.
   */
  public boolean isPersistent();

  /**
   * @param pos
   * @return the order at the specified position (between 0 and size()-1
   */
  public OrderToken getToken(int pos);

  /**
   * Returns a view on the order tokens. These includes the {@link OrderToken#TT_EOC} token at the
   * end, but not the persistent ("@") token at the start.
   * 
   * @return an unmodifiable view on the tokens
   */
  public List<OrderToken> getTokens();

  /**
   * Returns the original text that the order was parsed from, so it contains all white space
   * characters and the "@".
   * 
   * @return the original text that the order was parsed from.
   */
  public String getText();

  /**
   * @return The number of tokens (including the {@link OrderToken#TT_EOC} token.
   */
  public int size();

  /**
   * Execute this order by registering UnitRelations etc.
   * 
   * @param state
   * @param data
   * @param unit
   * @param line
   */
  public void execute(ExecutionState state, GameData data, Unit unit, int line);

  /**
   * Returns true if this is a "long" order.
   */
  public boolean isLong();

  /**
   * Sets a warning or error.
   * 
   * @param problem The value for warning.
   */
  public void setProblem(Problem problem);

  /**
   * Returns the problem.
   * 
   * @return Returns warning.
   */
  public Problem getProblem();
}
