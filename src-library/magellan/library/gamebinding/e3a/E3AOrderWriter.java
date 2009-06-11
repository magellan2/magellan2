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
package magellan.library.gamebinding.e3a;

import magellan.library.gamebinding.EresseaOrderWriter;
import magellan.library.gamebinding.GameSpecificOrderWriter;

/**
 * 
 *
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class E3AOrderWriter extends EresseaOrderWriter {
  private static final E3AOrderWriter instance = new E3AOrderWriter();
  /** Current ECheck version */
  public static final String ECHECKVERSION = "4.3.2";
  
  /**
   * 
   */
  protected E3AOrderWriter() {
    // do nothing...
  }
  
  /**
   * Returns the instance of this class.
   */
  public static GameSpecificOrderWriter getSingleton() {
    return E3AOrderWriter.instance;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#getCheckerName()
   */
  public String getCheckerName() {
    return "ECheck";
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#useChecker()
   */
  public boolean useChecker() {
    return true;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#getCheckerDefaultParameter()
   */
  public String getCheckerDefaultParameter() {
    return " -s -l -w4 -v" + E3AOrderWriter.ECHECKVERSION;
  }

}
