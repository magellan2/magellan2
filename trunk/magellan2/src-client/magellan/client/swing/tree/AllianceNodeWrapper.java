// class magellan.client.swing.tree.AllianceNodeWrapper
// created on Jan 13, 2008
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
package magellan.client.swing.tree;

import java.util.Map;

import magellan.library.Alliance;
import magellan.library.EntityID;
import magellan.library.Faction;

/**
 * Displays a alliance node with alliance as icon and text.
 *
 * @author ...
 * @version 1.0, Jan 13, 2008
 */
public class AllianceNodeWrapper extends FactionNodeWrapper implements CellObject2,
    SupportsClipboard, SupportsEmphasizing {

  /**
   * @param f
   * @param activeAlliances
   */
  public AllianceNodeWrapper(Faction f, Map<EntityID, Alliance> activeAlliances) {
    super(f, null, activeAlliances);
  }

  @Override
  public String toString() {
    Alliance a = getAlliance(getFaction().getID());
    if (a==null) {
      return null;
    } else {
      return a.toString();
    }
  }

}
