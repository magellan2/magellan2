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

import magellan.library.GameData;

/**
 * This is a simple mapping that assumes no translation and no scaling. Thus the same coordinates
 * (x,y) in different level are related to each other.
 * 
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 **/

public class EasyLevelMapping implements LevelMapping {
  private static EasyLevelMapping singleton = new EasyLevelMapping();

  public static EasyLevelMapping getSingleton() {
    return EasyLevelMapping.singleton;
  }

  public LevelRelation getMapping(GameData data, int fromLevel, int toLevel) {
    return new LevelRelation(0, 0, toLevel, 1, 1, fromLevel);
  }
}
