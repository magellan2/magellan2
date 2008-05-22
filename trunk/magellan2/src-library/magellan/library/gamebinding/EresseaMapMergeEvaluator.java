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

import java.util.ArrayList;
import java.util.Collection;

import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.*;

/**
 * TODO This class must be commented
 *
 * 
 * @author Ralf Duckstein
 * @version 1.1, 21.05.2008
 */

public class EresseaMapMergeEvaluator extends MapMergeEvaluator {
  private static final Logger log = Logger.getInstance(EresseaMapMergeEvaluator.class);

  private static MapMergeEvaluator singleton = new EresseaMapMergeEvaluator();
  public static MapMergeEvaluator getSingleton() {
    return singleton;
  }

  protected static final Integer REAL_LAYER = Integer.valueOf(0); 
  protected static final Integer ASTRAL_LAYER = Integer.valueOf(1);

  public Collection<DataMapping> getDataMappingVariants(int level) {
    if (level == ASTRAL_LAYER) {
      Collection<DataMapping> variants = new ArrayList<DataMapping>(3);
      variants.add(RegionIDMapping.getSingleton());
      variants.add(SchemeNameMapping.getSingleton());
      variants.add(UnitIDMapping.getSingleton());
      return variants;     
    } 
    return super.getDataMappingVariants(level);
  }
  
  public Collection<LevelMapping> getLevelMappingVariants(int fromLevel, int toLevel) {
    if ((fromLevel == ASTRAL_LAYER) && (toLevel == REAL_LAYER)) {
      Collection<LevelMapping> variants = new ArrayList<LevelMapping>(2);
      variants.add(SchemeOverlapMapping.getSingleton());
      variants.add(SchemeExtendMapping.getSingleton());
      return variants;
    } else {
      return null;
    }
  }

  public MappingEvaluator getMappingEvaluator(int level) {
    if (level == ASTRAL_LAYER) {
      return AstralMappingEvaluator.getSingleton();
    } else return super.getMappingEvaluator(level);
  }
   
}
