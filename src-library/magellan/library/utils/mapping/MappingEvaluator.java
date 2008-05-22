// class magellan.library.utils.mapping.LevelMapping
// created on 19.05.2008
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.utils.mapping;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.utils.Score;

/**
 * 
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public abstract class MappingEvaluator {
  public final Score<CoordinateID> getRatedMapping(GameData fromData, GameData toData, CoordinateID mapping, String type) {
    Score<CoordinateID> score = evaluateMapping(fromData, toData, mapping);
    if (score != null) {
      score.setType(type);
    }
    return score;
  }

  public final void adjustRatedMapping(GameData fromData, GameData toData, Score<CoordinateID> ratedMapping) {
    Score<CoordinateID> score = evaluateMapping(fromData, toData, ratedMapping.getKey());
    if (score != null) {
      ratedMapping.addScore(score.getScore());
    }
  }

  protected abstract Score<CoordinateID> evaluateMapping(GameData fromData, GameData toData, CoordinateID mapping);
 
}
