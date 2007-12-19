// class magellan.library.gamebinding.EresseaMapMergeEvaluator
// created on 14.12.2007
//
// Copyright 2003-2007 by magellan project team
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
package magellan.library.gamebinding;

import java.util.HashMap;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
/**
 * TODO This class must be commented
 *
 * @author ...
 * @version 1.0, 14.12.2007
 */
public class EresseaMapMergeEvaluator extends MapMergeEvaluator {

  private static MapMergeEvaluator singleton = new EresseaMapMergeEvaluator();
  /**
   * 
   */
  private EresseaMapMergeEvaluator() {
    // TODO Auto-generated constructor stub
  }
  
  public static MapMergeEvaluator getSingleton() {
    return singleton;
  }
  public CoordinateID getMapping(GameData main, GameData add, int level) {
    if (level == 1) {
      return getAstral2AstralMapping(main, add);
    } else {
      return getMappingByNameAndTerrain(main, add, level);
    }
  }
  
  protected CoordinateID getAstral2AstralMapping(GameData main, GameData add) {
    return getAstral2AstralMapping(main, add, getMappingByNameAndTerrain(main, add, 0));
  }

  protected CoordinateID getAstral2AstralMapping(GameData main, GameData add, CoordinateID real2real) {
    return null;
  }

  public CoordinateID getAstral2RealMapping(GameData main) {
    /**
     *  Astral to real mapping can be done by some ways:
     *  1. from two neighbour astral spaces with schemes
     *     (there can be seen exactly one same scheme from both astral regions)
     *  2. from several astral regions with schemes, calculating the "extend" of the schemes
     *  3. proposed by Fiete: using one astral region only. Like variant 2 first the extend
     *     is calculated (something similar with sets of schemes), but then regions in
     *     real space not existing as scheme reduce the extend      
     **/

    CoordinateID astralToReal = null;
    CoordinateID minExtend = null;
    CoordinateID maxExtend = null;
    Map<CoordinateID,Region> dataSchemeMap = new HashMap<CoordinateID,Region>();
    for(Region region : main.regions().values()) {
      if (astralToReal != null) break;
      if(region.getCoordinate().z == 1) {
        for(Scheme scheme : region.schemes()) {
          Region otherRegion = (Region) dataSchemeMap.get(scheme.getCoordinate());
          if(otherRegion == null) {
            dataSchemeMap.put(scheme.getCoordinate(), region);
          } else {
            /**
             * 1.
             * This is the second astral region showing the same scheme.
             * From this we can calculate an astral to real mapping for the gamedata by variant 1
             */
            // in case of errors in the current Astral Regions (merged schemes) we will get a wrong mapping here. Therefore a scheme consistency check should be done in advance. (several possibilities)
            CoordinateID firstCoord = region.getCoordinate();
            CoordinateID secondCoord = otherRegion.getCoordinate();
            CoordinateID schemeCoord = scheme.getCoordinate();
            astralToReal = new CoordinateID(
              schemeCoord.x - 2 * (firstCoord.x + secondCoord.x),
              schemeCoord.y - 2 * (firstCoord.y + secondCoord.y));
            break;
          }
          /**
           * 2.
           * we may not find any astral to real mapping by variant 1 above
           * therefore also do calculations for variant 2 here
           * we "normalize" all schemes to be in the area
           */
          int nx = scheme.getCoordinate().x - 4 * region.getCoordinate().x;
          int ny = scheme.getCoordinate().y - 4 * region.getCoordinate().y;
          // this is a virtual third axis diagonal to x and y in the same level, but we store it in the z coordinate
          int nd = nx + ny;
          if (minExtend == null) {
            minExtend = new CoordinateID(nx, ny, nd);
            maxExtend = new CoordinateID(nx, ny, nd);
          } else {
            minExtend.x = Math.min(minExtend.x, nx);
            minExtend.y = Math.min(minExtend.y, ny);
            minExtend.z = Math.min(minExtend.z, nd);
            maxExtend.x = Math.max(maxExtend.x, nx);
            maxExtend.y = Math.max(maxExtend.y, ny);
            maxExtend.z = Math.max(maxExtend.z, nd);
          }
          // now check if we found an "extend of 4" in at least two directions of the three directions
          boolean dx = maxExtend.x-minExtend.x==4;
          boolean dy = maxExtend.y-minExtend.y==4;
          boolean dd = maxExtend.z-minExtend.z==4;
          if (dx&&dy) {
            astralToReal = new CoordinateID(maxExtend.x - 2, maxExtend.y - 2);
          } else if (dx&&dd) {
            astralToReal = new CoordinateID(maxExtend.x - 2, maxExtend.z - maxExtend.x);
          } else if (dy&&dd) {
            astralToReal = new CoordinateID(maxExtend.z - maxExtend.y, maxExtend.y - 2);
          }
        }
      }
    }
    /**
     * 3. 
     * If we are here and astralToReal is null then the extends of the schemes
     * alone are not enough. We have to take Fietes idea to look for land regions
     * near to the schemes to reduce number of possilbe mappings to 1.  
     */
    return astralToReal;
  }

}
