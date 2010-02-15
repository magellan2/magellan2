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
 * Astral to real mapping from two neighbor astral spaces with schemes (there can be seen exactly
 * one same scheme from both astral regions).
 * 
 * <pre>
 *                   / \ 
 *                 S| s |S 
 *             / \ / \ / \ / \    
 *            | s | s | s | s |
 *   /1\ / \ /|\ / \ /2\ / \ /| 
 *  | A |   |S|S| s | A | s |S|S
 *   \1/ \ / \|/ \ / \2/ \ / \|
 *            | s | s | s | s |
 *             \ / \ / \ / \ /
 *                S | s | S
 *                   \ /
 * 
 * </pre>
 * 
 * Now <code>scheme</code> is one of the four S regions, firstCoord and second coord are A1 and A2.
 * Then, the real space region <code>(S.x-2*(A1.x + A2.x), 4*S.y-2*(A1.y + A2.y), 0)</code> is
 * centered below <code>(0,0,1)</code>.
 */

public class SchemeOverlapMapping implements LevelMapping {
  private static SchemeOverlapMapping singleton = new SchemeOverlapMapping();

  /**
   * Returns an instance of this class.
   */
  public static SchemeOverlapMapping getSingleton() {
    return SchemeOverlapMapping.singleton;
  }

  /**
   * Returns a mapping from "real space" to "astral space".
   * 
   * @see magellan.library.utils.mapping.LevelMapping#getMapping(magellan.library.GameData, int,
   *      int)
   */
  public LevelRelation getMapping(GameData data, int fromLevel, int toLevel) {
    Map<CoordinateID, Collection<Region>> astralRegions =
        getAstralRegionsBySchemeName(data, fromLevel, toLevel);

    for (CoordinateID schemeCoord : astralRegions.keySet()) {
      Collection<Region> regions = astralRegions.get(schemeCoord);
      if (regions.size() > 1) {
        if (regions.size() > 2) {
          // log.error("Report corrupted: scheme visible from more than two regions: " + scheme);
          break;
        }

        Iterator<Region> it = regions.iterator();
        CoordinateID firstCoord = it.next().getCoordinate();
        CoordinateID secondCoord = it.next().getCoordinate();
        return new LevelRelation(schemeCoord.getX() - 2 * (firstCoord.getX() + secondCoord.getX()),
            schemeCoord.getY() - 2 * (firstCoord.getY() + secondCoord.getY()), toLevel, 4, 4,
            fromLevel);
      }
    }
    return null;
  }

  private Map<CoordinateID, Collection<Region>> getAstralRegionsBySchemeName(GameData data,
      int astralLevel, int realLevel) {
    Map<CoordinateID, Collection<Region>> astralRegions =
        new HashMap<CoordinateID, Collection<Region>>(0);
    if (data != null) {
      for (Region region : data.getRegions()) {
        if (region.getCoordinate().getZ() == astralLevel) {
          for (Scheme scheme : region.schemes()) {
            CoordinateID coord = scheme.getCoordinate();
            if (coord.getZ() == realLevel) {
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
