// class magellan.library.gamebinding.GameSpecificOrderWriter
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
package magellan.library.gamebinding;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.Region;

/**
 * This is an interface for a game specific order writer. At the beginning it is used for changing
 * ECHECK to ACHECK if the game is Allanon instead of Eressea...
 * 
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public interface GameSpecificOrderWriter {
  /**
   * Returns true, if this game specific order writer uses a syntax check tool like ECheck.
   */
  public boolean useChecker();

  /**
   * Returns the name of the Syntax Checker like "ECheck" or "ACheck".
   */
  public String getCheckerName();

  /**
   * Returns a list of default parameters for the Syntax Checker. The parameters are used, if no
   * parameters are set.
   */
  public String getCheckerDefaultParameter();

  /**
   * Sets the GameData to write.
   */
  public void setGameData(GameData gameData);

  /**
   * Sets the faction whose orders to write.
   */
  public void setFaction(Faction selectedFaction);

  /**
   * Set set of regions whose unit orders are written.
   */
  public void setRegions(Collection<Region> regions);

  /**
   * Sets option used by ECheck order checker (or similar). Requires faction to be set.
   */
  public void setECheckOptions(String options);

  /**
   * Write only confirmed orders.
   */
  public void setConfirmedOnly(boolean selected);

  /**
   * Write comments used by ECheck order checker.
   */
  public void setAddECheckComments(boolean selected);

  /**
   * Remove transient (semicolon type) and permanent (// type) comments.
   */
  public void setRemoveComments(boolean semicolon, boolean slashslash);

  /**
   * Writes unit tags as commments, usable by the Vorlage tool.
   */
  public void setWriteUnitTagsAsVorlageComment(boolean selected);

  /**
   * Enforce that only Unix-style linebreaks are used. This is necessary when writing to the
   * clipboard under Windows.
   */
  public void setForceUnixLineBreaks(boolean forceUnixLineBreaks);

  /**
   * Set a group. Only orders of units in this group are written.
   */
  public void setGroup(Group group);

  /**
   * Writes the faction's orders to the stream. World and faction must be set, possibly using
   * {@link #setGameData(GameData)} and {@link #setFaction(Faction)}.
   * 
   * @param stream
   * @return The number of written units
   * @throws IOException If an I/O error occurs
   */
  public int write(BufferedWriter stream) throws IOException;

  /**
   * As {@link #write(BufferedWriter)}, but using a plain writer.
   */
  public int write(Writer stream) throws IOException;

}
