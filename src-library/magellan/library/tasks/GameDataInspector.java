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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.tasks.Problem.Severity;

/**
 * This inspectors checks all syntax.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 02.03.2008
 */
public class GameDataInspector extends AbstractInspector {

  public enum GameDataProblemTypes {
    DUPLICATEBUILDINGID, DUPLICATEREGIONID, DUPLICATEREGIONUID, DUPLICATESHIPID, DUPLICATEUNITID,
    OUTOFMEMORY;

    public ProblemType type;

    GameDataProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("gamedata.problem", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  protected GameDataInspector(GameData data) {
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
  public static GameDataInspector getInstance(GameData data) {
    return new GameDataInspector(data);
  }

  @Override
  public List<Problem> reviewRegion(Region r, Severity severity) {
    List<Problem> errors = new ArrayList<Problem>();
    if (r == getData().getRegions().iterator().next()) {
      for (Problem p : getData().getErrors()) {
        if (p.getSeverity() == severity) {
          errors.add(p);
        }
      }
    }
    return errors;
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
