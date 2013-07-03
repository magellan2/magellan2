// class magellan.library.gamebinding.GameSpecificOrderReader
// created on Apr 24, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.gamebinding;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

/**
 * Reads an order file.
 */
public interface GameSpecificOrderReader {

  /**
   * Sets the order locale.
   */
  public void setLocale(Locale locale);

  /**
   * Returns the order locale.
   */
  public Locale getLocale();

  /**
   * Reads the orders from the specified Reader. Orders for multiple factions can be read. Region
   * lines are ignored. Unit are not created. If there are orders for a unit that cannot be found in
   * the game data these orders are ignored. Lines containing ECHECK comments are always ignored.
   * Comments starting with a semicolon and containing the literal 'bestaetigt' (case and umlaut
   * insensitive) after an arbitrary number of whitespace characters are never added to a unit's
   * orders, instead they set the order confirmation status of the unit to true.
   * 
   * @throws IOException if an I/O error occurs
   */
  public void read(Reader in) throws IOException;

  /**
   * Returns whether all read orders get automatically confirmed.
   */
  public boolean getAutoConfirm();

  /**
   * Sets whether all read orders get automatically confirmed.
   */
  public void setAutoConfirm(boolean autoConfirm);

  /**
   * Returns whether all comments in the orders starting with a semicolon (except confirmation
   * comments) are ignored.
   */
  public boolean isIgnoringSemicolonComments();

  /**
   * Sets whether all comments in the orders starting with a semicolon (except confirmation
   * comments) are ignored.
   */
  public void setIgnoreSemicolonComments(boolean ignoreSemicolonComments);

  /**
   * Returns the number of factions and units that were read. This method should only be called
   * after reading the orders has finished.
   */
  public Status getStatus();

  /**
   * Describes a few aspects of the orders read.
   */
  public static class Status {
    /** Counts the number of units for which orders where read. */
    public int units;

    /** Counts the number of factions for which orders where read. */
    public int factions;

    /** counts units in orders that were not present in the report */
    public int unknownUnits;

    /**
     * if doNotOverwriteConfirmedorders=true then this is a counter of the units which were
     * protected by this setting and left unchanged
     */
    public int confirmedUnitsNotOverwritten;

    /** Counter for other errors. */
    public int errors;
  }

  /**
   * Returns whether orders of confirmed units should be overwritten.
   * 
   * @return Returns doNotOverwriteConfirmedOrders.
   */
  public boolean isDoNotOverwriteConfirmedOrders();

  /**
   * Sets whether orders of confirmed units should be overwritten. If set to <code>true</code>,
   * orders of confirmed units will not be changed.
   * 
   * @param doNotOverwriteConfirmedOrders The value for doNotOverwriteConfirmedOrders.
   */
  public void setDoNotOverwriteConfirmedOrders(boolean doNotOverwriteConfirmedOrders);

  /**
   * Return the name of the order checker like ECheck.
   */
  String getCheckerName();

}
