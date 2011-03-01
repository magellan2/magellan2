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
import magellan.library.tasks.OrderSyntaxInspector.OrderSemanticsProblemTypes;
import magellan.library.tasks.OrderSyntaxInspector.OrderSyntaxProblemTypes;
import magellan.library.tasks.Problem;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.tasks.SimpleProblem;
import magellan.library.utils.OrderToken;

/**
 * Magellan's standard order implementation.
 */
public class SimpleOrder implements Order {


  private List<OrderToken> tokens;
  private String text;
  private boolean persistent;
  private boolean isLong;
  private Problem problem;

  /**
   * Creates a new order from a list of tokens.
   * 
   * @param tokens
   * @param text
   */
  public SimpleOrder(List<OrderToken> tokens, String text) {
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
    // FIXME setting a problem makes the order valid??
    return problem == null;
    // return problem == null || problem.getType() != SyntaxProblem;
  }

  /**
   * Marks the order as valid or unvalid.
   */
  public void setValid(boolean valid) {
    if (valid && !isValid()) {
      problem = null;
    } else if (!valid && isValid()) {
      problem =
          ProblemFactory
              .createProblem(Severity.WARNING, OrderSyntaxProblemTypes.PARSE_WARNING.type);
    }
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
   * @see magellan.library.Order#getProblem()
   */
  public Problem getProblem() {
    return problem;
  }

  /**
   * @see magellan.library.Order#setProblem(magellan.library.tasks.Problem)
   */
  public void setProblem(Problem problem) {
    this.problem = problem;
  }

  /**
   * Registers a warning for a unit order.
   * 
   * @see SimpleProblem
   */
  protected void setWarning(Unit unit, int line, String string) {
    setProblem(ProblemFactory.createProblem(Severity.WARNING,
        OrderSemanticsProblemTypes.SEMANTIC_ERROR.type, unit, null, string, line));
  }

  /**
   * Registers a severe error for a unit order.
   * 
   * @see SimpleProblem
   */
  protected void setError(Unit unit, int line, String string) {
    setProblem(ProblemFactory.createProblem(Severity.ERROR,
        OrderSemanticsProblemTypes.SEMANTIC_ERROR.type, unit, null, string, line));
  }
}
