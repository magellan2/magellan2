// class magellan.library.utils.mapping.GameObjectIDMapping
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

/**
 * 
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public class GameObjectIDMapping implements DataMapping {
  private static GameObjectIDMapping singleton = new GameObjectIDMapping();
  public static GameObjectIDMapping getSingleton() {
    return GameObjectIDMapping.singleton;
  }

  @Override
  public String toString() {
    return "GameObjectID";
  }
  
  public CoordinateID getMapping(GameData fromData, GameData toData, int level) {
    // HIGHTODO Automatisch generierte Methode implementieren
    CoordinateID translation = RegionIDMapping.getSingleton().getMapping(fromData, toData, level);
    if (translation != null ) {
      return translation;
    }
    
    translation = BuildingIDMapping.getSingleton().getMapping(fromData, toData, level);
    if (translation != null ) {
      return translation;
    }

    translation = ShipIDMapping.getSingleton().getMapping(fromData, toData, level);
    if (translation != null ) {
      return translation;
    }

    translation = UnitIDMapping.getSingleton().getMapping(fromData, toData, level);
    if (translation != null ) {
      return translation;
    }

    return null;
  }

}
