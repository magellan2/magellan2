// class magellan.library.gamebinding.AllanonOrderParser
// created on 17.04.2008
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
package magellan.library.gamebinding;

import magellan.library.GameData;

/**
 * 
 *
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class AllanonOrderParser extends EresseaOrderParser {
  /**
   * Creates a new <tt>EresseaOrderParser</tt> object.
   */
  public AllanonOrderParser(GameData data) {
    super(data, null);
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
   * <tt>OrderCompleter</tt> object. This constructor should be used only by the
   * <tt>OrderCompleter</tt> class itself.
   */
  public AllanonOrderParser(GameData data, EresseaOrderCompleter cc) {
    super(data,cc);
  }
}
