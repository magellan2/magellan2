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
import magellan.library.tasks.GameDataInspector.GameDataProblemTypes;
import magellan.library.tasks.Problem.Severity;

/**
 * @author stm
 */
public class TransferInspector extends AbstractInspector {

  /** */
  public enum TransferProblemTypes {
    /** empty unit loses items */
    LOST_ITEMS;

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
    if (u.getModifiedPersons() == 0) {
      for (Item i : u.getModifiedItems()) {
        if (i.getAmount() > 0)
          return Collections.<Problem> singletonList(ProblemFactory.createProblem(Severity.WARNING,
              TransferProblemTypes.LOST_ITEMS.getType(), u, this, -1));
      }
    }

    return Collections.emptyList();
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
