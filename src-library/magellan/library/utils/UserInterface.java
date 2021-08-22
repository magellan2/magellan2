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

import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * This interface represents a UserInterface that shows the user the progress of an action.
 * 
 * @author unknown (extracted from ReportMerger)
 * @author Thoralf Rickert
 * @version 1.0, 07.11.2007
 */
public interface UserInterface {

  /**
   * A listener that reacts on WINDOW_CLOSING events of the dialog.
   */
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

  /**
   * Adds a listener that is called on WINDOW_CLOSING events.
   * 
   * @param listener
   */
  public void addClosingListener(ClosingListener listener);

  /**
   * Sets the progress of the action.
   */
  public void setProgress(String strMessage, int iProgress);

  /**
   * @return the current progress.
   */
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
  public void showException(String message, String description, Throwable exception);

  /**
   * Opens a confirm dialog.
   */
  public boolean confirm(String strMessage, String strTitle);

  /**
   * Opens an input dialog.
   */
  public Object input(String strMessage, String strTitle, Object[] values, Object initial);

  /**
   * Shows a message with an OK option
   * 
   * @param message
   */
  public void showMessageDialog(String message);

  /**
   * Displays a dialog. See JOptionPane.
   * 
   * @param title The title of a dialog
   * @param message The displayed object
   * @param messageType the type of message to be displayed: <code>ERROR_MESSAGE</code>,
   *          <code>INFORMATION_MESSAGE</code>, <code>WARNING_MESSAGE</code>,
   *          <code>QUESTION_MESSAGE</code>, or <code>PLAIN_MESSAGE</code>
   * @param options the options to display in the pane: <code>DEFAULT_OPTION</code>,
   *          <code>YES_NO_OPTION</code>, <code>YES_NO_CANCEL_OPTION</code>,
   *          <code>OK_CANCEL_OPTION</code>
   */
  public void showDialog(String title, String message, int messageType, int options);

  /**
   * Displays a dialog to ask for map bounds. See SetGirthDialog.
   * 
   * @param preset pre-set bounding box
   * @param layer the map layer, for example 0 for normal space, 1 for astral space
   * @param metric
   * @return the new box. <code>null</code> if none was given.
   */
  public BBox askForGirth(BBox preset, int layer, MapMetric metric);
}
