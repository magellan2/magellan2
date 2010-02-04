// class magellan.plugin.extendedcommands.ContainerType
// created on 02.02.2008
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
package magellan.plugin.extendedcommands;

import magellan.library.rules.BuildingType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Utils;

/**
 * An enum for all container types in this plugin.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public enum ContainerType {
  REGIONTYPE, RACE, BUILDINGTYPE, SHIPTYPE, UNKNOWN;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public static ContainerType getType(String name) {
    if (Utils.isEmpty(name))
      return UNKNOWN;
    for (ContainerType type : ContainerType.values()) {
      if (type.toString().equalsIgnoreCase(name))
        return type;
    }
    return UNKNOWN;
  }

  public static ContainerType getType(UnitContainerType uctype) {
    if (Utils.isEmpty(uctype))
      return UNKNOWN;
    if (uctype instanceof RegionType)
      return REGIONTYPE;
    if (uctype instanceof Race)
      return RACE;
    if (uctype instanceof BuildingType)
      return BUILDINGTYPE;
    if (uctype instanceof ShipType)
      return SHIPTYPE;
    return UNKNOWN;
  }

}
