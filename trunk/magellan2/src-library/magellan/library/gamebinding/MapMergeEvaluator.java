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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Score;
import magellan.library.utils.mapping.BuildingIDMapping;
import magellan.library.utils.mapping.DataMapping;
import magellan.library.utils.mapping.EasyLevelMapping;
import magellan.library.utils.mapping.LevelMapping;
import magellan.library.utils.mapping.LevelRelation;
import magellan.library.utils.mapping.MappingEvaluator;
import magellan.library.utils.mapping.RegionIDMapping;
import magellan.library.utils.mapping.RegionNameMapping;
import magellan.library.utils.mapping.SavedTranslationsMapping;
import magellan.library.utils.mapping.ShipIDMapping;
import magellan.library.utils.mapping.TerrainMappingEvaluator;
import magellan.library.utils.mapping.UnitIDMapping;

/**
 * TODO This class must be commented
 * 
 * @author Ralf Duckstein
 * @version 1.1, 21.05.2008
 */

public class MapMergeEvaluator {
  public Collection<DataMapping> getDataMappingVariants(int level) {
    Collection<DataMapping> variants = new ArrayList<DataMapping>();
    // this is a summarization of all XXXIDMapping classes     
//    variants.add(GameObjectIDMapping.getSingleton());
    // for testing purposes we use all on it's own
    variants.add(RegionIDMapping.getSingleton());
    variants.add(BuildingIDMapping.getSingleton());
    variants.add(ShipIDMapping.getSingleton());
    variants.add(UnitIDMapping.getSingleton());
    // the traditional name mapping
    variants.add(RegionNameMapping.getSingleton());
    // saved mappings
    variants.add(SavedTranslationsMapping.getSingleton());
    return variants;
  }

  public Collection<LevelMapping> getLevelMappingVariants(int fromLevel, int toLevel) {
    Collection<LevelMapping> variants = new ArrayList<LevelMapping>(1);
    variants.add(EasyLevelMapping.getSingleton());
    return variants;
  }
  
  public MappingEvaluator getMappingEvaluator(int level) {
    // by default all layers are checked by terrain only
    return TerrainMappingEvaluator.getSingleton();
  }
  
  /**
   * This is the main function that should be called from outside to
   * retrieve possible mappings.
   * @param fromData - GameData
   * @param toData   - GameData
   * @param level    - the level/layer in which the mapping should be estimated
   */
  public final Collection<Score<CoordinateID>> getDataMappings(GameData fromData, GameData toData, int level) {
    return getDataMappings(fromData, toData, level, null);
  }
  
  public final Collection<Score<CoordinateID>> getDataMappings(GameData fromData, GameData toData, int level, Collection<CoordinateID> otherLevels) {
    Collection<DataMapping> mappingVariants = getDataMappingVariants(level);
    if (mappingVariants == null) {
      return null;
    }
    
    Map<CoordinateID,Score<CoordinateID>> mappings = new HashMap<CoordinateID,Score<CoordinateID>>(1);
    for (DataMapping dm : mappingVariants) {
      CoordinateID mapping = dm.getMapping(fromData, toData, level);
      if (mapping != null) {
        Score<CoordinateID> score;
        if (!mappings.containsKey(mapping)) {
          score = getMappingEvaluator(level).getRatedMapping(fromData, toData, mapping, dm.toString());
          mappings.put(mapping, score);
        } else {
          score = mappings.get(mapping);
          score.setType(score.getType()+", "+dm.toString());
        }
      }
    }
    // now add transitiv mappings
    if (otherLevels != null) {
      for (CoordinateID otherMapping : otherLevels) {
        CoordinateID mapping = getTransitivMapping(fromData, toData, level, otherMapping);
        if (mapping != null) {
          Score<CoordinateID> score;
          if (!mappings.containsKey(mapping)) {
            score = getMappingEvaluator(level).getRatedMapping(fromData, toData, mapping, "Transitive("+otherMapping.z+")");
            mappings.put(mapping, score);
          } else {
            score = mappings.get(mapping);
            score.setType(score.getType()+", Transitive("+otherMapping.z+")");
          }
        }
      }
    }
    return mappings.values();
  }

  /**
   * This method should not be called directly, as results of calling this
   * method are cached in the corresponding GameData object.
   * Please call data.getLevelRelation(fromLevel, toLevel) instead.
   *
   * @param data
   * @param fromLevel
   * @param toLevel
   * @return
   */
  public final LevelRelation getLevelRelation(GameData data, int fromLevel, int toLevel) {
    Collection<LevelMapping> mappingVariants = getLevelMappingVariants(fromLevel, toLevel);
    if (mappingVariants == null) {
      return null;
    }
    
    for (LevelMapping dm : mappingVariants) {
      LevelRelation relation = dm.getMapping(data, fromLevel, toLevel);
      if (relation != null) {
        return relation;
      }
    }
    return null;
  }

  public static Set<Integer> getLayers(GameData data) {
    Set<Integer> layers = new HashSet<Integer>(2);
    for (Region region : data.regions().values()) {
      layers.add(Integer.valueOf(region.getCoordinate().z));
    }
    return layers;
  }  
  
  public final CoordinateID getTransitivMapping(GameData fromData, GameData toData, int layer, CoordinateID mapping) {
    // first chance
    LevelRelation fromLR = fromData.getLevelRelation(layer, mapping.z);
    LevelRelation toLR = toData.getLevelRelation(layer, mapping.z);
    if ((fromLR != null) && (toLR != null)) {
      CoordinateID c = new CoordinateID(mapping.x + fromLR.x, mapping.y + fromLR.y, mapping.z);
      CoordinateID cNew = toLR.getInverseRelatedCoordinate(c);
      if (cNew != null) {
        return cNew;
      }
    }
    // second chance - maybe we have a relation in the other direction

    fromLR = fromData.getLevelRelation(mapping.z, layer);
    toLR = toData.getLevelRelation(mapping.z, layer);
    if ((fromLR != null) && (toLR != null)) {
      CoordinateID cNew = fromLR.getRelatedCoordinate(mapping);
      if (cNew != null) {
        cNew.x-=toLR.x;
        cNew.y-=toLR.y;
        return cNew;
      }
    }
        
    return null;
  }
  
}
