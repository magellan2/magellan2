// class magellan.library.gamebinding.AtlantisOrderWriter
// created on Apr 23, 2013
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

import java.io.BufferedWriter;
import java.io.IOException;

import magellan.library.Region;
import magellan.library.utils.OrderWriter;

/**
 * Order writer for Atlantis game.
 */
public class AtlantisOrderWriter extends OrderWriter implements GameSpecificOrderWriter {

  public boolean useChecker() {
    return false;
  }

  public String getCheckerName() {
    return null;
  }

  public String getCheckerDefaultParameter() {
    return null;
  }

  @Override
  protected void writeLocale(BufferedWriter stream) throws IOException {
    // don't write locale
  }

  @Override
  protected void writeRegionLine(BufferedWriter stream, Region r) throws IOException {
    // REGION line is unknown
    stream.write(commentStart + getOrderTranslation(EresseaConstants.OC_REGION));
    stream.write(" " + r.getID().toString(",") + " ");
    writeCommentLine(stream, r.getName());
  }

  @Override
  protected void writeFooter(BufferedWriter stream) throws IOException {
    // no end marker
  }

}
