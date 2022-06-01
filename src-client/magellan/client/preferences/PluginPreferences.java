// class magellan.client.preferences.PluginPreferences
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
package magellan.client.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.utils.Resources;

/**
 * Preference adapter for plug ins
 * 
 * @author ...
 * @version 1.0, Feb 19, 2008
 */
public class PluginPreferences implements ExtendedPreferencesAdapter {

  private Collection<MagellanPlugIn> plugins;

  public PluginPreferences(Collection<MagellanPlugIn> plugins, Properties settings) {
    this.plugins = plugins;
  }

  public void applyPreferences() {
    // nothing to apply
  }

  /**
   * Returns the plugin's preferences adapters
   * 
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
  public Collection<PreferencesAdapter> getChildren() {
    Collection<PreferencesAdapter> preferencesAdapterList =
        new ArrayList<PreferencesAdapter>(plugins.size());
    for (MagellanPlugIn plugIn : plugins) {
      PreferencesFactory plugInPreferenceFactory = plugIn.getPreferencesProvider();
      if (plugInPreferenceFactory != null) {
        preferencesAdapterList.add(plugInPreferenceFactory.createPreferencesAdapter());
      }
    }

    return preferencesAdapterList;
  }

  /**
   * Draws a list of known plugins.
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    JPanel pnl = new JPanel(new BorderLayout());
    pnl.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("plugins.pluginsettings.modulelist.title")));

    DefaultListModel<String> model = new DefaultListModel<String>();
    JList<String> pluginList = new JList<String>(model);

    for (MagellanPlugIn plugin : plugins) {
      model.addElement(plugin.getName());
    }

    WrappableLabel comment = WrappableLabel.getLabel(Resources.get("plugins.pluginsettings.comment"));
    comment.setPreferredWidth(PreferencesAdapter.PREFERRED_WIDTH);

    pnl.add(comment.getComponent(), BorderLayout.NORTH);

    pluginList.setPreferredSize(new Dimension(500, 500));
    pnl.setPreferredSize(new Dimension(500, 500));

    pnl.add(pluginList, BorderLayout.CENTER);

    return pnl;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("plugins.pluginsettings.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
  }

}
