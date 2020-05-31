// class magellan.library.tasks.OrderSyntaxInspector
// created on 02.03.2008
//
// Copyright 2003-2008 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Unit;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.GameDataInspector.GameDataProblemTypes;
import magellan.library.tasks.Problem.Severity;

/**
 * @author stm
 */
public class TransferInspector extends AbstractInspector {

  /** */
  public enum TransferProblemTypes {
    /** empty unit loses items */
    LOST_ITEMS, ENTER_GIVE;

    /**    */
    public ProblemType type;

    TransferProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.transferinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  protected TransferInspector(GameData data) {
    super(data);
  }

  @Override
  public void setGameData(GameData gameData) {
    super.setGameData(gameData);
  }

  /**
   * Returns an instance of OrderSyntaxInspector.
   *
   * @return The singleton instance of OrderSyntaxInspector
   */
  public static TransferInspector getInstance(GameData data) {
    return new TransferInspector(data);
  }

  @Override
  public List<Problem> findProblems(Unit u) {
    List<Problem> p = findLost(u);
    if (p != null)
      return p;

    p = findEnterConflict(u);
    if (p != null)
      return p;

    return Collections.emptyList();
  }

  private List<Problem> findEnterConflict(Unit u) {
    List<? extends UnitRelation> reorder = u.getRelations(EnterRelation.class);
    if (reorder.isEmpty()) {
      reorder = u.getRelations(ControlRelation.class);
    }
    if (!reorder.isEmpty()) {
      List<? extends UnitRelation> transfers = u.getRelations(TransferRelation.class);
      if (transfers.isEmpty()) {
        transfers = u.getRelations(ReserveRelation.class);
      }
      if (!transfers.isEmpty())
        return singletonList(ProblemFactory.createProblem(Severity.WARNING,
            TransferProblemTypes.ENTER_GIVE.getType(), u, this, -1));
    }
    return null;
  }

  private List<Problem> singletonList(SimpleProblem problem) {
    LinkedList<Problem> problems = new LinkedList<Problem>();
    problems.add(problem);
    return problems;
  }

  private List<Problem> findLost(Unit u) {
    if (u.getModifiedPersons() == 0) {
      for (Item i : u.getModifiedItems()) {
        if (i.getAmount() > 0)
          return singletonList(ProblemFactory.createProblem(Severity.WARNING,
              TransferProblemTypes.LOST_ITEMS.getType(), u, this, -1));
      }
    }
    return null;
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (GameDataProblemTypes t : GameDataProblemTypes.values()) {
        types.add(t.getType());
      }
      for (GameDataProblemTypes t : GameDataProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }
}
