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
 * This interface represents a UserInterface that shows the user
 * the progress of an action.
 *
 * @author unknown (extracted from ReportMerger)
 * @author Thoralf Rickert
 * @version 1.0, 07.11.2007
 */
public interface UserInterface {
  /**
   * Notifies the userface that the task is done. Destroys progress dialog.
   */
  public void ready();
  
  /**
   * Shows the dialog
   */ 
  public void show();
  
  /**
   * Sets the progress of the action.
   */
  public void setProgress(String strMessage, int iProgress);
  
  /**
   * Sets the maximum number of steps.
   */
  public void setMaximum(int maxProgress);
  
  /**
   * Opens a confirm dialog.
   */
  public boolean confirm(String strMessage, String strTitle);
  
  /**
   * Opens an input dialog.
   */
  public Object input(String strMessage, String strTitle, Object [] values, Object initial);
  
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
}
