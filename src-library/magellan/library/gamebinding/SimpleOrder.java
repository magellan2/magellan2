// class magellan.library.impl.MagellanOrder
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
package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Unit;
import magellan.library.utils.OrderToken;

/**
 * Magellan's standard order implementation.
 */
public class SimpleOrder implements Order {

  private List<OrderToken> tokens;
  private String text;
  private boolean valid;
  private boolean persistent;
  private String warning;
  private boolean isLong;

  // private UnitRelation relation;

  /**
   * Creates a new order from a list of tokens.
   * 
   * @param tokens
   * @param text
   * @param valid
   */
  public SimpleOrder(List<OrderToken> tokens, String text, boolean valid) {
    this.tokens = new ArrayList<OrderToken>(3);
    for (OrderToken t : tokens) {
      if (t.ttype == OrderToken.TT_PERSIST) {
        setPersistent(true);
      } else {
        this.tokens.add(t);
      }
    }

    this.tokens = Collections.unmodifiableList(this.tokens);
    this.text = text;
    this.valid = valid;
  }

  /**
   * @see magellan.library.Order#setPersistent(boolean)
   */
  public void setPersistent(boolean b) {
    persistent = b;
  }

  /**
   * @see magellan.library.Order#isPersistent()
   */
  public boolean isPersistent() {
    return persistent;
  }

  /**
   * @see magellan.library.Order#isEmpty()
   */
  public boolean isEmpty() {
    return tokens.size() == 0 || tokens.get(0).ttype == OrderToken.TT_EOC;
  }

  /**
   * @see magellan.library.Order#isValid()
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * @see magellan.library.Order#setValid(boolean)
   */
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  /**
   * @see magellan.library.Order#getToken(int)
   */
  public OrderToken getToken(int i) {
    return tokens.get(i);
  }

  /**
   * @see magellan.library.Order#getTokens()
   */
  public List<OrderToken> getTokens() {
    return tokens;
  }

  /**
   * @see magellan.library.Order#getText()
   */
  public String getText() {
    return text;
  }

  /**
   * @see magellan.library.Order#size()
   */
  public int size() {
    return tokens.size();
  }

  @Override
  public String toString() {
    return getText();
  }

  /**
   * The default implementation does nothing!
   * 
   * @see magellan.library.Order#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    // do nothing
  }

  /**
   * Sets the value of isLong.
   * 
   * @param isLong The value for isLong.
   */
  public void setLong(boolean isLong) {
    this.isLong = isLong;
  }

  /**
   * @see magellan.library.Order#isLong()
   */
  public boolean isLong() {
    return isLong;
  }

  /**
   * Sets the value of warning.
   * 
   * @param warning The value for warning.
   */
  public void setWarning(String warning) {
    this.warning = warning;
  }

  /**
   * Returns the value of warning.
   * 
   * @return Returns warning.
   */
  public String getWarning() {
    return warning;
  }

}
