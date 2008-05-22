// class magellan.library.utils.mapping.RegionNameMapping
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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.utils.Score;

public class SavedTranslationsMapping implements DataMapping {
  private static SavedTranslationsMapping singleton = new SavedTranslationsMapping();
  public static SavedTranslationsMapping getSingleton() {
    return singleton;
  }

  public String toString() {
    return "SavedTranslation";
  }

  public CoordinateID getMapping(GameData fromData, GameData toData, int level) {
    Map<CoordinateID, Score<CoordinateID>> translationMap = new Hashtable<CoordinateID, Score<CoordinateID>>();
    
    // compare all saved translations in both reports
    // special handling for owner faction may be required
    // if not saved -> owner faction has 0,0 in this level
    
    for (Faction faction : fromData.factions().values()) {
      EntityID fid = (EntityID) faction.getID();
      CoordinateID fromTrans = fromData.getCoordinateTranslation(fid, level);
      CoordinateID toTrans = toData.getCoordinateTranslation(fid, level);
      if ((fromTrans != null) && (toTrans != null)) {
        toTrans.z = 0;
        CoordinateID translation = toTrans.createDistanceCoordinate(fromTrans);

        Score<CoordinateID> score = translationMap.get(translation);
        if (score == null) {
          score = new Score<CoordinateID>(translation);
          translationMap.put(translation, score);
        }
        score.addScore(1);
      }
    }
    
    if (translationMap.size() > 0) {
      return Collections.max(translationMap.values()).getKey();
    } else {
      return null;
    }
  }
}
