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

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;

public class UnitIDMapping implements DataMapping {
  private static UnitIDMapping singleton = new UnitIDMapping();

  public static UnitIDMapping getSingleton() {
    return UnitIDMapping.singleton;
  }

  @Override
  public String toString() {
    return "UnitID";
  }

  public CoordinateID getMapping(GameData fromData, GameData toData, int level) {
    // create possible translations by same unit in both reports from same turn!
    if ((fromData.getDate() == null) || (!fromData.getDate().equals(toData.getDate())))
      return null;

    for (Region region : fromData.regions().values()) {
      if (region.getCoordinate().getZ() == level) {
        for (Unit unit : region.units()) {
          Unit sameUnit = toData.getUnit(unit.getID());

          if (sameUnit != null) {
            // match found
            Region sameRegion = sameUnit.getRegion();
            if (sameRegion != null) {
              CoordinateID sameCoord = sameRegion.getCoordinate();
              if (sameCoord.getZ() == level)
                return CoordinateID.create(sameCoord.getX() - region.getCoordinate().getX(), sameCoord.getY()
                    - region.getCoordinate().getY(), level);
            }
          }
        }
      }
    }
    return null;
  }
}
