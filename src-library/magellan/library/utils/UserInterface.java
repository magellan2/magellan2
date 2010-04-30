// class magellan.library.utils.UserInterface
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.library.utils;

import java.awt.event.WindowEvent;

import javax.swing.JDialog;

/**
 * This interface represents a UserInterface that shows the user the progress of an action.
 * 
 * @author unknown (extracted from ReportMerger)
 * @author Thoralf Rickert
 * @version 1.0, 07.11.2007
 */
public interface UserInterface {

  public interface ClosingListener {

    /**
     * Returns <code>true</code> if the dialog should be closed after receiving an event
     * <code>e</code> of type {@link WindowEvent#WINDOW_CLOSING}.
     * 
     * @param e
     * @return <code>true</code> if the dialog should be closed.
     */
    public boolean close(WindowEvent e);
  }

  /**
   * Shows the dialog. If the action is done in a different thread, this method should be called in
   * the calling thread (usually the EventDispatchThread).
   */
  public void show();

  /**
   * Notifies the interface that the task is done. Destroys progress dialog. If the action is done
   * in a different thread, this method should be called in the working thread.
   */
  public void ready();

  public void addClosingListener(ClosingListener listener);

  /**
   * Sets the progress of the action.
   */
  public void setProgress(String strMessage, int iProgress);

  public int getProgress();

  /**
   * Sets the maximum number of steps. A value of 0 or less means, that the maximum is
   * indeterminate.
   */
  public void setMaximum(int maxProgress);

  /**
   * Sets the title of the progress dialog.
   */
  public void setTitle(String title);

  /**
   * Displays an error window
   * 
   * @param message A message for the user, may be <code>null</code>.
   * @param description An explanatory text, may be <code>null</code>
   * @param exception
   */
  public void showException(String message, String description, Exception exception);

  /**
   * Opens a confirm dialog.
   */
  public boolean confirm(String strMessage, String strTitle);

  /**
   * Opens an input dialog.
   */
  public Object input(String strMessage, String strTitle, Object[] values, Object initial);

  /**
   * Shows a message with an ok option
   * 
   * @param message
   */
  public void showMessageDialog(String message);

  /**
   * Displays the dialog.
   * 
   * @param dialog
   */
  public void showDialog(JDialog dialog);

}
