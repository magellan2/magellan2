// class magellan.library.utils.comparator.TrustLevel
// created on Apr 20, 2021
//
// Copyright 2003-2021 by magellan project team
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
package magellan.library;

/**
 * 
 */
public enum TrustLevel {

  PRIVILEGED(100), ALLIED(1), DEFAULT(0), ENEMY(-9999), MONSTROUS(-10000);

  public static final int TL_PRIVILEGED = PRIVILEGED.getIntLevel();
  public static final int TL_DEFAULT = DEFAULT.getIntLevel();
  public static final int TL_MONSTROUS = MONSTROUS.getIntLevel();

  private int intVal;

  private TrustLevel(int level) {
    intVal = level;
  }

  public int getIntLevel() {
    return intVal;
  }

  public boolean le(int level) {
    return level >= intVal;
  }

  public boolean ge(int level) {
    return level <= intVal;
  }

  public static TrustLevel getLevel(int trustLevel) {
    for (TrustLevel l : TrustLevel.values()) {
      if (trustLevel >= l.intVal)
        return l;
    }
    return MONSTROUS;
  }

  public boolean is(int level) {
    return level >= intVal;
  }

}