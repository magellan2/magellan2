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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;

/**
 *  Astral to real mapping from two neighbour astral spaces with schemes
 *  (there can be seen exactly one same scheme from both astral regions)
 **/
public class SchemeOverlapMapping implements LevelMapping {
  private static SchemeOverlapMapping singleton = new SchemeOverlapMapping();
  public static SchemeOverlapMapping getSingleton() {
    return singleton;
  }

  public LevelRelation getMapping(GameData data, int fromLevel, int toLevel) {
    Map<CoordinateID, Collection<Region>> astralRegions = getAstralRegionsBySchemeName(data, fromLevel, toLevel);

    for(CoordinateID schemeCoord : astralRegions.keySet()) {
      Collection<Region> regions = astralRegions.get(schemeCoord);
      if (regions.size()>1) {
        if (regions.size() > 2) {
//              log.error("Report corrupted: scheme visible from more than two regions: " + scheme);
          break;
        }
        /**
         * He we have two astral regions showing the same scheme. From
         * this we can calculate an astral to real mapping for the data.
         */
        Iterator<Region> it = regions.iterator();
        CoordinateID firstCoord = it.next().getCoordinate();
        CoordinateID secondCoord = it.next().getCoordinate();
        return new LevelRelation(schemeCoord.x - 2 * (firstCoord.x + secondCoord.x), schemeCoord.y - 2 * (firstCoord.y + secondCoord.y), toLevel, 4, 4, fromLevel);
      }
    }
    return null;
  }

  private Map<CoordinateID, Collection<Region>> getAstralRegionsBySchemeName(GameData data, int astralLevel, int realLevel) {
    Map<CoordinateID, Collection<Region>> astralRegions = new HashMap<CoordinateID, Collection<Region>>(0);
    if (data != null) {
      for (Region region : data.regions().values()) {
        if (region.getCoordinate().z == astralLevel) {
          for (Scheme scheme : region.schemes()) {
            CoordinateID coord = scheme.getCoordinate();
            if (coord.z == realLevel) {
              Collection<Region> col = astralRegions.get(coord);
              if (col == null) {
                astralRegions.put(coord, col = new HashSet<Region>());
              }
              col.add(region);
            }
          }
        }
      }
    }
    return astralRegions;
  }
}
