// class magellan.library.gamebinding.MapMergeEvaluator
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import magellan.library.GameData;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.rules.RegionType;

/**
 * TODO This class must be commented
 *
 * @author ...
 * @version 1.0, 14.12.2007
 */
public class MapMergeEvaluator {
  
  /**
   * An inner class holding a coordinate translation and its score.
   * 
   */
  protected class CoordinateTranslation implements Comparable<CoordinateTranslation>{
    int score = -1;
    CoordinateID translation = null;

    public CoordinateTranslation(CoordinateID trans, int score) {
      translation = trans;
      this.score = score;
    }

    public int getScore() {
      return score;
    }

    public void setScore(int score) {
      this.score = score;
    }

    public CoordinateID getTranslation() {
      return translation;
    }

    public int compareTo(CoordinateTranslation o) {
      return score > o.getScore() ? 1 : (score < o.getScore() ? -1 : 0);
    }
  }
  
  /**
   * This is the main function that should be called from outside. 
   * @param main
   * @param add
   */
  public Map<Integer, CoordinateID> getMappings(GameData main, GameData add) {
    Map<Integer, CoordinateID> mappings = new HashMap<Integer, CoordinateID>(2);
    Set<Integer> mainLayers = getLayers(main);
    for (Integer layer : getLayers(add)) {
      if (mainLayers.contains(layer)) {
        mappings.put(layer, getMapping(main, add, layer.intValue())); 
      } else {
        mappings.put(layer, new CoordinateID(0, 0, layer.intValue()));
      }
    }
    return mappings;
  }

  public CoordinateID getMapping(GameData main, GameData add, int layer) {
    return getMappingByNameAndTerrain(main, add, layer);
  }
  
  protected static Set<Integer> getLayers(GameData data) {
    Set<Integer> layers = new HashSet<Integer>(2);
    for (Region region : data.regions().values()) {
      layers.add(Integer.valueOf(region.getCoordinate().z));
    }
    return layers;
  }

  /**
   * The method calculates different translation posibilities by matching
   * the region names of the two reports. For each name match found the 
   * score is inceased by 5.
   * Afterwards, the score is adjusted by matching the terrain. 
   * The score for terrain match is between 1 and 5.
   * 
   * @param main - The current GameData
   * @param add  - The added GameData
   * @param layer - the layer in which the mapping should be performed
   * @return The best possible mapping in this layer
   */
  protected CoordinateID getMappingByNameAndTerrain(GameData main, GameData add, int layer) {
    // create possible translations by name
    //   equal name = 5 points per name
    Map<CoordinateID, CoordinateTranslation> translations = createTranslationsByName(main, add, layer);
    // adjust score
    adjustTranslationScoreByTerrain(main, add, translations);
    // decide best translation
    if (translations.size() > 0) {
      return Collections.max(translations.values()).getTranslation();
    } else {
      return new CoordinateID(0, 0, layer);
    }
  }

  /**
   * Add translation to map and increase score.
   * 
   * @param translationMap
   * @param translation
   */
  private void addTranslation(Map<CoordinateID, CoordinateTranslation> translationMap, CoordinateID translation, int score) {
    CoordinateTranslation translationCandidate = translationMap.get(translation);
  
    if (translationCandidate == null) {
      translationCandidate = new CoordinateTranslation(translation, score);
    } else {
      translationCandidate.setScore(translationCandidate.getScore() + score);
    }
    translationMap.put(translation, translationCandidate);
  }
  
  /**
   * Score for name match.
   */
  protected static final int SCORE_EQUAL_NAME = 5;
  
  /**
   * Tries to find matching regions and adds translations to the map accordingly.
   * 
   * @param newReport
   * @return
   */
  private Map<CoordinateID, CoordinateTranslation> createTranslationsByName(GameData main, GameData add, int layer) {
    Map<CoordinateID, CoordinateTranslation> translationMap = new Hashtable<CoordinateID, CoordinateTranslation>();
    
    // Create HashMap of regions in added report
    Map<String, Collection<Region>> regionMap = new HashMap<String, Collection<Region>>();
    for (Region region : add.regions().values()) {
      if ((region.getName() != null) && (region.getName().length() > 0) && (region.getCoordinate().z == layer)) {
        Collection<Region> regions = regionMap.get(region.getName());
        if (regions == null) {
          regions = new HashSet<Region>();
          regionMap.put(region.getName(), regions);
        }
        regions.add(region);
      }
    }
    
    // loop regions in main report
    for (Region region : main.regions().values()) {
      CoordinateID coord = region.getCoordinate();
  
      if ((coord.z == layer) && (region.getName() != null) && (region.getName().length() > 0)) {

        Collection<Region> result = regionMap.get(region.getName());
        if (result != null) {
          for (Region foundRegion : result) {
            if (foundRegion != null) {
              CoordinateID foundCoord = foundRegion.getCoordinate();
              CoordinateID translation = new CoordinateID(foundCoord.x - coord.x, foundCoord.y - coord.y);
              addTranslation(translationMap, translation, SCORE_EQUAL_NAME);
            }
          }
        }
      }
    }
    return translationMap;
  }
  
  /**
   * check whether any of the normal space translations is impossible by
   * comparing the terrains. Adjust score according to number of mismatches.
   */
  private void adjustTranslationScoreByTerrain(GameData main, GameData add, Map<CoordinateID, CoordinateTranslation> translationMap) {
    RegionType forestTerrain = main.rules.getRegionType(StringID.create("Wald"));
    RegionType plainTerrain = main.rules.getRegionType(StringID.create("Ebene"));
    RegionType oceanTerrain = main.rules.getRegionType(StringID.create("Ozean"));
    RegionType firewallTerrain = main.rules.getRegionType(StringID.create("Feuerwand"));
    RegionType glacierTerrain = main.rules.getRegionType(StringID.create("Gletscher"));
    RegionType activeVolcanoTerrain = main.rules.getRegionType(StringID.create("Aktiver Vulkan"));
    RegionType volcanoTerrain = main.rules.getRegionType(StringID.create("Vulkan"));
    for (CoordinateTranslation translationCandidate : translationMap.values()) {
      /*
       * the number of regions not having the same region type at the current
       * translations
       */
      int addScore = 0; 
      CoordinateID t = translationCandidate.getTranslation();
  
      /* for each translation we have to compare the regions' terrains */
      for (Region region : main.regions().values()) {
        if ((region.getType() == null) || region.getType().equals(RegionType.unknown)) {
          continue;
        }
        
        CoordinateID c = region.getCoordinate();
        if (c.z != t.z) {
          continue;
        }
      /*
       * do the translation and find the corresponding region in the report
       * data
       */
          
        CoordinateID translatedCoord = new CoordinateID(c.x, c.y, 0);
        translatedCoord.translate(t);

        Region reportDataRegion = add.regions().get(translatedCoord);

        /*
         * are we able to compare the regions?
         */
        if ((reportDataRegion != null) && (reportDataRegion.getType() != null) && !(reportDataRegion.getType().equals(RegionType.unknown))) {
          /*
           * we increase the hit count further for each terrain match.
           * ocean    = 1 point
           * ice      = 1 point - not implemented
           * firewall = 2 points
           * glacier  = 3 points
           * forest  = 3 points
           * act. vul.= 3 points
           * plain    = 4 points
           * volcano  = 4 points
           * other    = 5 points
           * 
           * the different number of points respects the chance of terrain changing and of chance to be found in the world
           */
          if (region.getType().equals(reportDataRegion.getType())) {
            if (region.getType().equals(oceanTerrain)) {
              addScore+=1;
            } else if (region.getType().equals(firewallTerrain)) {
              addScore+=2;
            } else if (region.getType().equals(glacierTerrain)) {
              addScore+=3;
            } else if (region.getType().equals(forestTerrain)) {
              addScore+=3;
            } else if (region.getType().equals(activeVolcanoTerrain)) {
              addScore+=3;
            } else if (region.getType().equals(plainTerrain)) {
              addScore+=4;
            } else if (region.getType().equals(volcanoTerrain)) {
              addScore+=4;
            } else {
              addScore+=5;
            }
          }
        }
      }
      translationCandidate.setScore(translationCandidate.getScore() + addScore);
    }
  }
  
  protected CoordinateID getMappingByUnitID(GameData main, GameData add, int layer) {
    // create possible translations by same units / ships in both reports from same turn!
    if (!main.getDate().equals(add.getDate())) return null;
    
    for (Region region : main.regions().values()) {
      if (region.getCoordinate().z == layer) {
        for (Unit unit : region.units()) {
          Unit sameUnit = add.getUnit(unit.getID());
          if (sameUnit != null) {
            // match found
            Region sameRegion = sameUnit.getRegion();
            if (sameRegion != null) {
              CoordinateID sameCoord = sameRegion.getCoordinate();
              if (sameCoord.z == layer) {
                return new CoordinateID(sameCoord.x - region.getCoordinate().x , sameCoord.y - region.getCoordinate().y, layer);
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  /**
   * This method should wrap the mapping information currently contained in magellan.client.swing.MapperPanel.setLevel(int)

   * @param data
   * @param level
   * @return Mapped Coordinate
   */
  public CoordinateID getRelatedCoordinate(GameData data, CoordinateID c, int level) {
    return c;
  }

}
