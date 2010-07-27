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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import magellan.client.extern.MagellanPlugIn;
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
    JPanel pnl = new JPanel(new GridBagLayout());
    pnl.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("plugins.pluginsettings.modulelist.title")));

    DefaultListModel model = new DefaultListModel();
    JList pluginList = new JList(model);

    for (MagellanPlugIn plugin : plugins) {
      model.addElement(plugin.getName());
    }

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTH,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0);

    JTextArea comment = new JTextArea(Resources.get("plugins.pluginsettings.comment"));
    comment.setEditable(false);
    comment.setWrapStyleWord(true);
    comment.setLineWrap(true);
    comment.setSelectionColor(pnl.getBackground());
    comment.setSelectedTextColor(pnl.getForeground());
    comment.setRequestFocusEnabled(false);
    comment.setBackground(pnl.getBackground());
    comment.setSelectionColor(pnl.getBackground());
    comment.setSelectedTextColor(pnl.getForeground());
    comment.setFont(new JLabel().getFont());

    pnl.add(comment, c);

    c.gridy = 2;
    pnl.add(new JPanel());

    c.gridy = 3;
    c.weighty = 1;
    pnl.add(pluginList, c);

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
