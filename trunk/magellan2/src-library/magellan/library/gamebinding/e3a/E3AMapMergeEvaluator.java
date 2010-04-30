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
package magellan.library.gamebinding.e3a;

import java.util.ArrayList;
import java.util.Collection;

import magellan.library.Rules;
import magellan.library.gamebinding.EresseaMapMergeEvaluator;
import magellan.library.utils.mapping.AstralMappingEvaluator;
import magellan.library.utils.mapping.DataMapping;
import magellan.library.utils.mapping.LevelMapping;
import magellan.library.utils.mapping.MappingEvaluator;
import magellan.library.utils.mapping.RegionIDMapping;
import magellan.library.utils.mapping.SavedTranslationsMapping;
import magellan.library.utils.mapping.SchemeExtendMapping;
import magellan.library.utils.mapping.SchemeNameMapping;
import magellan.library.utils.mapping.SchemeOverlapMapping;
import magellan.library.utils.mapping.UnitIDMapping;

/**
 * @author
 * @version 1.1, 21.05.2008
 */

public class E3AMapMergeEvaluator extends EresseaMapMergeEvaluator {
  protected E3AMapMergeEvaluator(Rules rules) {
    super(rules);
  }

  @Override
  public Collection<DataMapping> getDataMappingVariants(int level) {
    if (level == EresseaMapMergeEvaluator.ASTRAL_LAYER) {
      Collection<DataMapping> variants = new ArrayList<DataMapping>(3);
      variants.add(RegionIDMapping.getSingleton());
      variants.add(SchemeNameMapping.getSingleton());
      variants.add(UnitIDMapping.getSingleton());
      // saved mappings
      variants.add(SavedTranslationsMapping.getSingleton());
      return variants;
    }
    return super.getDataMappingVariants(level);
  }

  @Override
  public Collection<LevelMapping> getLevelMappingVariants(int fromLevel, int toLevel) {
    if ((fromLevel == EresseaMapMergeEvaluator.ASTRAL_LAYER)
        && (toLevel == EresseaMapMergeEvaluator.REAL_LAYER)) {
      Collection<LevelMapping> variants = new ArrayList<LevelMapping>(2);
      variants.add(SchemeOverlapMapping.getSingleton());
      variants.add(SchemeExtendMapping.getSingleton());
      return variants;
    } else
      return null;
  }

  @Override
  public MappingEvaluator getMappingEvaluator(int level) {
    if (level == EresseaMapMergeEvaluator.ASTRAL_LAYER)
      return AstralMappingEvaluator.getSingleton();
    else
      return super.getMappingEvaluator(level);
  }

}
