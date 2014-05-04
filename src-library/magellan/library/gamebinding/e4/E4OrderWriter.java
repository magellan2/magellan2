// class magellan.library.gamebinding.EresseaOrderWriter
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
package magellan.library.gamebinding.e4;

import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.gamebinding.e3a.E3AOrderWriter;

/**
 * 
 */
public class E4OrderWriter extends E3AOrderWriter {
  private static final E4OrderWriter instance = new E4OrderWriter();

  /**
   * 
   */
  protected E4OrderWriter() {
    super();
  }

  /**
   * Returns the instance of this class.
   */
  public static GameSpecificOrderWriter getSingleton() {
    return E4OrderWriter.instance;
  }

}
