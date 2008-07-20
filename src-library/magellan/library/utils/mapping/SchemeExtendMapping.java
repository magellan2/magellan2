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

import java.util.HashSet;
import java.util.Iterator;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.RegionType;

/**
 *  Astral to real mapping from several astral regions with schemes, calculating the
 *  "extend" of the schemes
 *  Proposed by Fiete: taking into account land regions in the neighbourhood that
 *  would appear as schems, but don' do. So they further decrease the number of posibilities        
 **/
public class SchemeExtendMapping implements LevelMapping {
  private static SchemeExtendMapping singleton = new SchemeExtendMapping();
  public static SchemeExtendMapping getSingleton() {
    return SchemeExtendMapping.singleton;
  }
  
  public LevelRelation getMapping(GameData data, int fromLevel, int toLevel) {
    CoordinateID astralToReal = null;
    CoordinateID minExtend = null;
    CoordinateID maxExtend = null;
    for(Region region : data.regions().values()) {
      if((region.getCoordinate().z == fromLevel)&&(region.schemes().size()>0)) {
        for(Scheme scheme : region.schemes()) {
          /**
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
        }

        // after each astral region has been processed we can check if  
        // the following prevents us from checking the set when there is definitly more than one mapping
        if (maxExtend.x-minExtend.x+maxExtend.y-minExtend.y+maxExtend.z-minExtend.z >= 8) {
          // now check if there is one possible mapping only
          int cntr = 0;
          for (int x=maxExtend.x-2; (x<=minExtend.x+2) && (cntr<2); x++) {
            for (int y=maxExtend.y-2; (y<=minExtend.y+2) && (cntr<2); y++) {
              if ((maxExtend.z-2 <= x+y) && (x+y <= minExtend.z+2)) {
                astralToReal = new CoordinateID(x, y, toLevel);
                cntr++;
              }
            }
          }
          if (cntr == 1) {
            break;
          } else {
            astralToReal = null;
          }
        }
      }
    }
    // we should have found a result in 95% of the reports up to here. The following
    // includes fietes idea to look for other land regions out of the current scheme
    // area, however we extend this to all astral regions with schemes by normalization
    if ((astralToReal == null) && (minExtend != null) && (maxExtend != null)) {
      // create possible mappings
      HashSet<CoordinateID> relations = new HashSet<CoordinateID>();
      for (int x=maxExtend.x-2; (x<=minExtend.x+2); x++) {
        for (int y=maxExtend.y-2; (y<=minExtend.y+2); y++) {
          if ((maxExtend.z-2 <= x+y) && (x+y <= minExtend.z+2)) {
            relations.add(new CoordinateID(x, y, toLevel));
          }
        }
      }
      HashSet<RegionType> scheme_rt = new HashSet<RegionType>();
      // create possible scheme terrains
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_PLAIN));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_WOOD));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_GLACIER));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_SWAMP));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_HIGHLAND));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_DESSERT));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_MOUNTAIN));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_VOLCANO));
      scheme_rt.add(data.rules.getRegionType(EresseaConstants.RT_ACTIVE_VOLCANO));

      // now loop over all surounding regions for all astral regions 
      for(Region region : data.regions().values()) {
        if((region.getCoordinate().z == fromLevel)&&(region.schemes().size()>0)) {
          // surounding regions
          for (int x=maxExtend.x-4; (x<=minExtend.x+4); x++) {
            for (int y=maxExtend.y-4; (y<=minExtend.y+4); y++) {
              int d = x+y;
              if ((maxExtend.z-4 <= d) && (d <= minExtend.z+4)) {
                if (x < minExtend.x || x > maxExtend.x ||
                    y < minExtend.y || y > maxExtend.y ||
                    d < minExtend.z || d > maxExtend.z) {
                  // check terrain
                  Region realRegion = data.getRegion(new CoordinateID(x + 4 * region.getCoordinate().x,
                                                                      y + 4 * region.getCoordinate().y, toLevel));
                  if (realRegion != null) {
                    if (scheme_rt.contains(realRegion.getRegionType())) {
                      // distance check
                      Iterator<CoordinateID> it = relations.iterator();
                      while (it.hasNext()) {
                        CoordinateID realCoord = it.next();
                        if (Math.abs(realCoord.x-x)<=2 &&
                            Math.abs(realCoord.y-y)<=2 &&
                            Math.abs(realCoord.x+realCoord.y-d)<=2) {
                          it.remove();
                        }
                      }
                      if (relations.size() == 1) {
                        return new LevelRelation(relations.iterator().next(), 4, 4, fromLevel);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (astralToReal != null) {
      return new LevelRelation(astralToReal, 4, 4, fromLevel);
    } 
    return null;
  }
}
