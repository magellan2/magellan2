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

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.rules.RegionType;
import magellan.library.utils.Score;

public class TerrainMappingEvaluator extends MappingEvaluator {
  private static TerrainMappingEvaluator singleton = new TerrainMappingEvaluator();

  public static TerrainMappingEvaluator getSingleton() {
    return TerrainMappingEvaluator.singleton;
  }

  public static final double PERCENT_MISMATCHES = 0.03;

  @Override
  protected Score<CoordinateID> evaluateMapping(GameData fromData, GameData toData,
      CoordinateID mapping) {

    int maxTerrainMismatches =
        (int) (Math.max(fromData.getRegions().size(), toData.getRegions().size()) * TerrainMappingEvaluator.PERCENT_MISMATCHES);

    RegionType forestTerrain = fromData.rules.getRegionType(StringID.create("Wald"));
    RegionType plainTerrain = fromData.rules.getRegionType(StringID.create("Ebene"));
    RegionType oceanTerrain = fromData.rules.getRegionType(StringID.create("Ozean"));
    RegionType glacierTerrain = fromData.rules.getRegionType(StringID.create("Gletscher"));
    RegionType activeVolcanoTerrain =
        fromData.rules.getRegionType(StringID.create("Aktiver Vulkan"));
    RegionType volcanoTerrain = fromData.rules.getRegionType(StringID.create("Vulkan"));

    int mismatches = 0;
    int score = 0;
    int oceanMatches = 0;
    int uidMatches = 0;

    /* for each translation we have to compare the regions' terrains */
    for (Region region : fromData.getRegions()) {
      if ((region.getType() == null) || region.getType().equals(RegionType.unknown)) {
        continue;
      }

      CoordinateID c = region.getCoordinate();

      /*
       * do the translation and find the corresponding region in the report data
       */
      if (c.getZ() == mapping.getZ()) {
        CoordinateID translatedCoord = c.translateInLayer(mapping);
        Region sameRegion = toData.getRegion(translatedCoord);

        /*
         * the hit count for the current translation must only be modified, if there actually are
         * regions to be compared and their terrains are valid
         */

        if ((sameRegion != null)) {
          // try to compare region ID
          boolean idCheck = false;
          if (sameRegion.hasUID() && region.hasUID()) {
            if (sameRegion.getUID() > 0 && region.getUID() > 0) {
              if (sameRegion.getUID() == region.getUID()) {
                uidMatches += 2;
              } else {
                mismatches += 2;
              }
              idCheck = true;
            } else if (sameRegion.getUID() == region.getUID()) {
              uidMatches += 1;
              idCheck = true;
            }
          }
          if (!idCheck) {
            // if there are no region IDs, compare terrain
            if ((sameRegion.getType() != null)
                && !(sameRegion.getType().equals(RegionType.unknown))) {
              if (region.getType().equals(sameRegion.getType())) {
                score++;
                if (region.getType().equals(oceanTerrain)) {
                  oceanMatches++;
                }
              } else {
                /*
                 * now we have a mismatch. If the reports are from the same turn, terrains may not
                 * differ at all. If the reports are from different turns, some terrains can be
                 * transformed.
                 */
                if ((fromData.getDate() != null) && fromData.getDate().equals(toData.getDate())) {
                  mismatches++;
                  score--;
                } else {
                  if (!(((forestTerrain != null) && (plainTerrain != null) && ((forestTerrain
                      .equals(region.getType()) && plainTerrain.equals(sameRegion.getType())) || (plainTerrain
                      .equals(region.getType()) && forestTerrain.equals(sameRegion.getType()))))
                      || ((oceanTerrain != null) && (glacierTerrain != null) && ((oceanTerrain
                          .equals(region.getType()) && glacierTerrain.equals(sameRegion.getType())) || (glacierTerrain
                          .equals(region.getType()) && oceanTerrain.equals(sameRegion.getType())))) || ((activeVolcanoTerrain != null)
                      && (volcanoTerrain != null) && ((activeVolcanoTerrain
                      .equals(region.getType()) && volcanoTerrain.equals(sameRegion.getType())) || (volcanoTerrain
                      .equals(region.getType()) && activeVolcanoTerrain
                      .equals(sameRegion.getType())))))) {
                    mismatches++;
                    score--;
                  }
                }

                if (mismatches > maxTerrainMismatches && score <= 0) {
                  break;
                }
              }
            }
          }
        }
      }
    }
    score = (score * 4 - oceanMatches + uidMatches * 2) / 4;
    return new Score<CoordinateID>(mapping, mismatches > maxTerrainMismatches ? Math.min(-1, score)
        : score);
  }
}
