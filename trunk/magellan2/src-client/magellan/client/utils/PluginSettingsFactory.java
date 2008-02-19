// class magellan.client.utils.PluginSettingsFactory
// created on Feb 19, 2008
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
package magellan.client.utils;

import java.util.Collection;
import java.util.Properties;

import magellan.client.extern.MagellanPlugIn;
import magellan.client.preferences.PluginPreferences;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;

/**
 * Creates an adapter for settings concerning plugins.
 *
 * @author ...
 * @version 1.0, Feb 19, 2008
 */
public class PluginSettingsFactory implements PreferencesFactory {
  private Properties settings;
  private Collection<MagellanPlugIn> plugins;

  public PluginSettingsFactory(Collection<MagellanPlugIn> plugIns, Properties properties) {
    this.settings = properties;
    this.plugins = plugIns;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new PluginPreferences(plugins, settings);
  }
}
