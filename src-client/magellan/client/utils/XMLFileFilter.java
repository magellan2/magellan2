// class magellan.client.utils.XMLFileFilter
// created on 29.11.2007
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
package magellan.client.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import magellan.library.utils.Resources;

/**
 * Accepts XML files.
 *
 * @author ...
 * @version 1.0, 29.11.2007
 */
public class XMLFileFilter extends FileFilter {

  /**
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File f) {
    if (f==null) {
      return false;
    }
    return (f.getName().toLowerCase().endsWith(Resources.get("eresseafilefilter.dock.extension").toLowerCase()));
  }

  /**
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("eresseafilefilter.dock.description");
  }

}
