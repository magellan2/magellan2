// class magellan.library.utils.NullUserInterface
// created on 07.11.2007
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
package magellan.library.utils;

import magellan.library.utils.logging.Logger;

/**
 * This class implements a UserInterface that shows no
 * output.
 *
 * @author ...
 * @version 1.0, 07.11.2007
 */
public class NullUserInterface implements UserInterface {
  private static final Logger log = Logger.getInstance(NullUserInterface.class);
  private int max = 100;
  
  /**
   * @see magellan.library.utils.UserInterface#ready()
   */
  public void ready() {}
  
  /**
   * @see magellan.library.utils.UserInterface#show()
   */
  public void show() {}
  
  /**
   * @see magellan.library.utils.UserInterface#setProgress(java.lang.String, int)
   */
  public void setProgress(String strMessage, int iProgress) {
    log.info("Progress: "+strMessage+" ("+getPercent(iProgress)+"%)");
  }
  
  protected int getPercent(int progress) {
    if (max==0) return 0;
    return progress*100/max;
  }
  
  /**
   * @see magellan.library.utils.UserInterface#confirm(java.lang.String, java.lang.String)
   */
  public boolean confirm(String strMessage, String strTitle) {
    return true;
  }
  
  /**
   * @see magellan.library.utils.UserInterface#setMaximum(int)
   */
  public void setMaximum(int maxProgress) {
    this.max = maxProgress;
  }

  /**
   * @see magellan.library.utils.UserInterface#setTitle(java.lang.String)
   */
  public void setTitle(String title) {}
}
