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

import javax.swing.JDialog;

import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * This class implements a UserInterface that shows no output.
 * 
 * @author ...
 * @version 1.0, 07.11.2007
 */
public class NullUserInterface implements UserInterface {
  private static final Logger log = Logger.getInstance(NullUserInterface.class);
  private int max = 100;
  private int progress;

  private static NullUserInterface singleton = new NullUserInterface();
  private static int logLevel = Logger.MAX_LEVEL;

  public static UserInterface getInstance() {
    return NullUserInterface.singleton;
  }

  /**
   * @see magellan.library.utils.UserInterface#ready()
   */
  public void ready() {
    // do nothing
    if (logLevel >= Logger.INFO) {
      log.info("Null user interface is ready...");
    }
  }

  /**
   * @see magellan.library.utils.UserInterface#show()
   */
  public void show() {
    // do nothing
    if (logLevel >= Logger.INFO) {
      log.info("Show null user interface...");
    }
  }

  /**
   * @see magellan.library.utils.UserInterface#setProgress(java.lang.String, int)
   */
  public void setProgress(String strMessage, int iProgress) {
    if (logLevel >= Logger.INFO) {
      log.info("Progress: " + strMessage + " (" + getPercent(iProgress) + "%)");
    }
    progress = iProgress;
  }

  public int getProgress() {
    return progress;
  }

  protected int getPercent(int iProgress) {
    if (max == 0)
      return 0;
    return iProgress * 100 / max;
  }

  /**
   * @see magellan.library.utils.UserInterface#confirm(java.lang.String, java.lang.String)
   */
  public boolean confirm(String strMessage, String strTitle) {
    return true;
  }

  /**
   * @see magellan.library.utils.UserInterface#input(java.lang.String, java.lang.String,
   *      java.lang.Object[], java.lang.Object)
   */
  public Object input(String strMessage, String strTitle, Object[] values, Object initialSelection) {
    return null;
  }

  /**
   * @see magellan.library.utils.UserInterface#setMaximum(int)
   */
  public void setMaximum(int maxProgress) {
    max = maxProgress;
  }

  /**
   * @see magellan.library.utils.UserInterface#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    if (logLevel >= Logger.INFO) {
      log.info("Null user interface: '" + title + "'");
    }
  }

  public void showException(String message, String description, Exception exception) {
    throw new RuntimeException(exception);
  }

  public void showMessageDialog(String message) {
    if (logLevel >= Logger.INFO) {
      NullUserInterface.log.info("Error: " + message + ")");
    }
  }

  /**
   * @param optionPane
   */
  public void showDialog(JDialog optionPane) {
    if (logLevel >= Logger.WARN) {
      log.warn("dialog suppressed");
    }
  }

  public void addClosingListener(ClosingListener listener) {
    // do nothing
  }

  public void showDialog(String title, String message, int messageType, int options) {
    if (logLevel >= Logger.WARN) {
      NullUserInterface.log.warn(title + " - " + message);
    }
  }

  public BBox askForGirth(BBox best, int layer) {
    return best;
  }

  /**
   * @see Logger#setLevel(int)
   */
  public static void setLogLevel(int level) {
    logLevel = level;
  }

  /**
   * @see Logger#getLevel()
   * @return the current log level
   */
  public static int getLogLevel() {
    return logLevel;
  }

}
