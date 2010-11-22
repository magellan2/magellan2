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
import java.util.HashSet;
import java.util.Set;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.RegionType;
import magellan.library.utils.Score;

/**
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public class AstralMappingEvaluator extends MappingEvaluator {
  private static AstralMappingEvaluator singleton = new AstralMappingEvaluator();

  public static AstralMappingEvaluator getSingleton() {
    return AstralMappingEvaluator.singleton;
  }

  public static final int SCORE_TERRAIN = 1;
  public static final int SCORE_SCHEME = 2;

  @Override
  protected Score<CoordinateID> evaluateMapping(GameData fromData, GameData toData,
      CoordinateID mapping) {

    RegionType fogTerrain = fromData.rules.getRegionType(EresseaConstants.RT_FOG);

    int score = 0;
    int uidMatches = 0;

    // for each translation we have to compare the regions' terrains
    for (Region region : fromData.getRegions()) {
      if (region.getType() == null || region.getType().equals(RegionType.unknown)) {
        continue;
      }

      CoordinateID c = region.getCoordinate();

      // do the translation and find the corresponding region in the report
      // data
      if (c.getZ() == mapping.getZ()) {
        CoordinateID translatedCoord = c.translateInLayer(mapping);
        Region sameRegion = toData.getRegion(translatedCoord);

        // the hit count for the current translation must only be modified, if
        // there actually are regions to be compared and their terrains are
        // valid

        if ((sameRegion != null)) {
          // try to compare region ID
          boolean idCheck = false;
          if (sameRegion.hasUID() && region.hasUID()) {
            if (sameRegion.getUID() > 0 && region.getUID() > 0) {
              if (sameRegion.getUID() == region.getUID()) {
                uidMatches += 2;
              }
              idCheck = true;
            } else if (sameRegion.getUID() == region.getUID()) {
              uidMatches += 1;
              idCheck = true;
            }
          }
          if (!idCheck)
            if ((sameRegion.getType() != null)
                && !(sameRegion.getType().equals(RegionType.unknown))) {
              if (region.getType().equals(sameRegion.getType())) {
                score += AstralMappingEvaluator.SCORE_TERRAIN;
                if ((region.getType().equals(fogTerrain)) && (region.schemes() != null)
                    && (region.schemes().size() > 0) && (sameRegion.schemes() != null)
                    && (sameRegion.schemes().size() > 0)) {
                  // both regions have schemes - lets compare them
                  if (equalSchemes(region.schemes(), sameRegion.schemes())) {
                    score += AstralMappingEvaluator.SCORE_SCHEME;
                    // do not multiply * region.schemes().size()
                  } else {
                    score -= AstralMappingEvaluator.SCORE_SCHEME;
                    // do not multiply * region.schemes().size()
                  }
                }
              } else {
                if ((fromData.getDate() != null) && fromData.getDate().equals(toData.getDate())) {
                  score -= AstralMappingEvaluator.SCORE_TERRAIN;
                }
              }
            }
        }
      }
    }

    score = (score + uidMatches) / 2;
    return new Score<CoordinateID>(mapping, score);
  }

  private boolean equalSchemes(Collection<Scheme> schemes1, Collection<Scheme> schemes2) {
    if (schemes1 == null) {
      if (schemes2 == null)
        return true;
      else
        return false;
    }

    if (schemes1.size() != schemes2.size())
      return false;

    Set<String> schemeNames1 = new HashSet<String>();
    Set<String> schemeNames2 = new HashSet<String>();

    for (Scheme s : schemes1) {
      schemeNames1.add(s.getName());
    }

    for (Scheme s : schemes2) {
      schemeNames2.add(s.getName());
    }

    return schemeNames1.containsAll(schemeNames2);
  }

}
