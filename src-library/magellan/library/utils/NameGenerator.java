// class magellan.client.utils.NameGen
// created on Jul 18, 2022
//
// Copyright 2003-2022 by magellan project team
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
package magellan.library.utils;

/**
 * A class that provides names.
 */
public interface NameGenerator {

  /**
   * Returns true, if there is a name generator configured.
   */
  boolean isActive();

  /**
   * Returns true, if there is a name generator configured and names are available.
   */
  boolean isAvailable();

  /**
   * Enabled the name generator.
   */
  void setEnabled(boolean available);

  /**
   * Returns the next name.
   */
  String getName();

  /**
   * Returns the number of available names. If the number is positive, but unknown or infinite,
   * {@link Integer#MAX_VALUE} is returned.
   */
  int getNamesCount();

}