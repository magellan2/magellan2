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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.StringID;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;
/**
 * TODO This class must be commented
 *
 * @author ...
 * @version 1.0, 14.12.2007
 */
public class EresseaMapMergeEvaluator extends MapMergeEvaluator {
  private static final Logger log = Logger.getInstance(EresseaMapMergeEvaluator.class);

  private static MapMergeEvaluator singleton = new EresseaMapMergeEvaluator();
  protected static final Integer REAL_LAYER = Integer.valueOf(0); 
  protected static final Integer ASTRAL_LAYER = Integer.valueOf(1);
  /**
   * 
   */
  private EresseaMapMergeEvaluator() {
    // TODO Auto-generated constructor stub
  }
  
  public static MapMergeEvaluator getSingleton() {
    return singleton;
  }
  public Map<Integer, CoordinateID> getMappings(GameData main, GameData add) {
    Map<Integer, CoordinateID> mappings = super.getMappings(main, add);

    // now fallback calculation of astral mapping if no overlapping in the astral space
    // we therefore calculate the mapping to real space for both reports and map them
    // over the real space
    // we only need to go on, if a real space mapping is available
    if ((mappings.containsKey(ASTRAL_LAYER)) && (mappings.get(ASTRAL_LAYER) == null) && 
        (mappings.get(REAL_LAYER) != null)) {
      mappings.put(ASTRAL_LAYER, getAstralMappingByRealSpace(main, add, ASTRAL_LAYER.intValue(), mappings.get(REAL_LAYER))); 
    }
    return mappings;
  }

  public CoordinateID getMapping(GameData main, GameData add, int layer) {
    if (ASTRAL_LAYER.intValue() == layer) { 
      return getMappingByEqualSchemes(main, add, layer); 
    } else {
      return getMappingByNameAndTerrain(main, add, layer); 
    }
  }
  
  protected CoordinateID getMappingByEqualSchemes(GameData main, GameData add, int layer) {
    // create possible translations
    // calculate score
    // decide best translation
    return null;
  }

  protected CoordinateID getAstralMappingByRealSpace(GameData main, GameData add, int layer, CoordinateID real2real) {
    CoordinateID mainA2R = getAstral2RealMapping(main);
    CoordinateID addA2R = getAstral2RealMapping(add);
    if ((mainA2R != null) && (addA2R != null) && (real2real != null)) {
      return new CoordinateID((mainA2R.x - addA2R.x + real2real.x)/4, (mainA2R.y - addA2R.y + real2real.y)/4, layer);
    } else {
      return null;
    }
  }

  public CoordinateID getAstral2RealMapping(GameData main) {
    CoordinateID c = getAstral2RealBySchemeOverlap(main, ASTRAL_LAYER, null);
    if (c != null) return c;
    c = getAstral2RealByNormalizedExtend(main, ASTRAL_LAYER);
    return c;
  }
  
  /**
   *  Astral to real mapping from two neighbour astral spaces with schemes
   *  (there can be seen exactly one same scheme from both astral regions)
   **/
  public CoordinateID getAstral2RealBySchemeOverlap(GameData data, int layer, Map<String, Collection<Region>> astralRegions) {

    if (astralRegions == null) {
      astralRegions = getAstralRegionsBySchemeName(data);
    }

    Map<CoordinateID,Region> dataSchemeMap = new HashMap<CoordinateID,Region>();
    for(Region region : data.regions().values()) {
      if(region.getCoordinate().z == ASTRAL_LAYER) {
        for(Scheme scheme : region.schemes()) {
          Collection<Region> regionsForScheme = astralRegions.get(scheme.getName());
          if (regionsForScheme.size() > 1) {
            if (regionsForScheme.size() > 2) {
              log.error("Report corrupted: scheme visible from more than two regions: " + scheme);
              break;
            }
            /**
             * This is the second astral region showing the same scheme. From
             * this we can calculate an astral to real mapping for the new
             * report by variant 1
             */
            for (Region secondRegion : regionsForScheme) {
              if (!secondRegion.equals(region)) {
                CoordinateID firstCoord = region.getCoordinate();
                CoordinateID secondCoord = secondRegion.getCoordinate();
                CoordinateID schemeCoord = scheme.getCoordinate();
                return new CoordinateID(schemeCoord.x - 2 * (firstCoord.x + secondCoord.x), schemeCoord.y - 2 * (firstCoord.y + secondCoord.y), schemeCoord.z);
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   *  Astral to real mapping from several astral regions with schemes, calculating the
   *  "extend" of the schemes
   *  Proposed by Fiete: taking into account land regions in the neighbourhood that
   *  would appear as schems, but don' do. So they further decrease the number of posibilities        
   **/
  public CoordinateID getAstral2RealByNormalizedExtend(GameData main, int astral) {

    RegionType firewallTerrain = main.rules.getRegionType(StringID.create("Feuerwand"));
    RegionType maelstromTerrain = main.rules.getRegionType(StringID.create("Mahlstrom"));

    CoordinateID astralToReal = null;
    CoordinateID minExtend = null;
    CoordinateID maxExtend = null;
    for(Region region : main.regions().values()) {
      if((region.getCoordinate().z == astral)&&(region.schemes().size()>0)) {
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
        // look for near land regions not appearing as schemes
        for (int x=maxExtend.x-4; x<=minExtend.x+4; x++) {
          for (int y=maxExtend.y-4; y<=minExtend.y+4; y++) {
            int d = x+y;
            if ((maxExtend.z-4 <= d) && (d <= minExtend.z+4)) {
              Region realRegion = main.regions().get(new CoordinateID(x + 4*region.getCoordinate().x, y + 4*region.getCoordinate().y, REAL_LAYER));
              if (realRegion != null) {
                RegionType rt=realRegion.getRegionType();
                if (!rt.isOcean()&&
                    !rt.equals(RegionType.unknown)&&
                    !rt.equals(firewallTerrain)&&
                    !rt.equals(maelstromTerrain)) {
                  // this is a region that should be seen as scheme
                  // if it's out of our ranges we dont see it, therefore 
                  // the ranges need to be adjusted
                  if (x < minExtend.x) { maxExtend.x = x+4; }
                  if (y < minExtend.y) { maxExtend.y = y+4; }
                  if (d < minExtend.z) { maxExtend.z = d+4; }
                  if (x > maxExtend.x) { minExtend.x = x-4; }
                  if (y > maxExtend.y) { minExtend.y = y-4; }
                  if (d > maxExtend.z) { minExtend.z = d-4; }
                }
              }
            }
          }
        }
        // after each astral region has been process we can check if  
        // the following prevents us from checking the set when there is definitly more than one mapping
        if (maxExtend.x-minExtend.x+maxExtend.y-minExtend.y+maxExtend.z-minExtend.z >= 8) {
          // now check if there is one possible mapping only
          int cntr = 0;
          for (int x=maxExtend.x-2; (x<=minExtend.x+2) && (cntr<2); x++) {
            for (int y=maxExtend.y-2; (y<=minExtend.y+2) && (cntr<2); y++) {
              if ((maxExtend.z-2 <= x+y) && (x+y <= minExtend.z+2)) {
                astralToReal = new CoordinateID(x, y, REAL_LAYER);
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
    /**
     * 3. 
     * If we are here and astralToReal is null then the extends of the schemes
     * alone are not enough. We have to take Fietes idea to look for land regions
     * near to the schemes to reduce number of possilbe mappings to 1.  
     */
    return astralToReal;
  }

  private Map<String, Collection<Region>> getAstralRegionsBySchemeName(GameData data) {
    Map<String, Collection<Region>> astralRegions = new HashMap<String, Collection<Region>>(0);
    if (data != null) {
      for (Region region : data.regions().values()) {
        if (region.getCoordinate().z == ASTRAL_LAYER) {
          for (Scheme scheme : region.schemes()) {
            String name = scheme.getName();
            if ((name != null) && (name.length()>0)) {
              Collection<Region> col = astralRegions.get(name);
              if (col == null) {
                astralRegions.put(name, col = new HashSet<Region>());
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
