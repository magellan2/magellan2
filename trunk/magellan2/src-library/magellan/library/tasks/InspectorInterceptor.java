// class magellan.library.tasks.InspectorInterceptor
// created on 18.12.2015
//
// Copyright 2003-2015 by magellan project team
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
package magellan.library.tasks;

import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;

/**
 * This interface can be implemented to be used as an programmatic interceptor for an inspector. For
 * example rule based do not check a specific object because it's not in scope.
 *
 * @author ...
 * @version 1.0, 18.12.2015
 */
public interface InspectorInterceptor {
  /**
   * You can call this method to find out, if there is an interceptor for your checks... at least
   * one of the parameters must be not null.
   */
  public boolean ignore(Faction f, Region r, Unit u, Ship s);
}
