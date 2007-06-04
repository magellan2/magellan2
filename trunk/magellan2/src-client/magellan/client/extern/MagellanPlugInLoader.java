// class magellan.client.extern.MagellanPlugIn
// created on 28.05.2007
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
package magellan.client.extern;

import java.util.Collection;
import java.util.Properties;

import magellan.library.utils.logging.Logger;

/**
 * This is the loader for all Magellan PlugIns.
 *
 * @author Thoralf Rickert
 * @version 1.0, 28.05.2007
 */
public class MagellanPlugInLoader extends AbstractPlugInLoader<MagellanPlugIn> {
  private static final Logger log = Logger.getInstance(MagellanPlugInLoader.class);

  /**
   * @see magellan.client.extern.AbstractPlugInLoader#getExternalModuleClasses(java.util.Properties)
   */
  @Override
  public Collection<Class<MagellanPlugIn>> getExternalModuleClasses(Properties settings) {
    log.info("Searching for magellan plugins...");

    long start = System.currentTimeMillis();
    
    Collection<Class<MagellanPlugIn>> classes =getExternalModuleClasses(settings, MagellanPlugIn.class);

    long end = System.currentTimeMillis();
    log.info("Searching for magellan plugins done. Found " + classes.size() + " instances in " + String.valueOf((end - start)) + " msecs");

    return classes;
  }
}
