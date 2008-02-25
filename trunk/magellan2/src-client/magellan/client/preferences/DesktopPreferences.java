// class magellan.client.preferences.DesktopPreferences
// created on 15.02.2008
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.desktop.DockingFrameworkBuilder;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Encapsulates the preferences tab for the desktop.
 */
public class DesktopPreferences extends JPanel implements ActionListener, ExtendedPreferencesAdapter {
  private static final Logger log = Logger.getInstance(DesktopPreferences.class);
  
  private MagellanDesktop desktop;
  private Client client;
  private Properties settings;
  
  private JPanel center;
  private List<PreferencesAdapter> scList;
  private JCheckBox enableWorkSpaceChooser;
  private JCheckBox dontShowTabs;

  /**
   * Creates a new DesktopPreferences object.
   */
  public DesktopPreferences(MagellanDesktop desktop, Client client, Properties settings) {
    this.desktop = desktop;
    this.client = client;
    this.settings = settings;
    this.setLayout(new BorderLayout());

    center = new JPanel();
    center.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("desktop.magellandesktop.prefs.border.options")));
    center.setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    enableWorkSpaceChooser = new JCheckBox(Resources.get("desktop.magellandesktop.prefs.displaychooser"), desktop.getWorkSpace().isEnabledChooser());
    panel.add(enableWorkSpaceChooser,c);
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);

    // load last report on startup
    c.gridx = 0;
    c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    log.info("Dont Show tabs? "+PropertiesHelper.getBoolean(Client.INSTANCE.getProperties(), PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS, false));
    dontShowTabs = new JCheckBox( Resources.get("desktop.magellandesktop.prefs.dontShowTabs"), PropertiesHelper.getBoolean(Client.INSTANCE.getProperties(), PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS, false));
    dontShowTabs.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(dontShowTabs,c);
    c.gridx = 1;
    c.gridy = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);

    
    center.add(panel,BorderLayout.NORTH);

    add(center, BorderLayout.CENTER);

    scList = new ArrayList<PreferencesAdapter>(1);
    scList.add(new DesktopShortCutPreferences(desktop,client,settings));
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent p1) {
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public java.awt.Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public java.lang.String getTitle() {
    return Resources.get("desktop.magellandesktop.prefs.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // TODO: implement it
  }
      
  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    desktop.setWorkSpaceChooser(enableWorkSpaceChooser.isSelected());
    desktop.setTabVisibility(!dontShowTabs.isSelected());
    DockingFrameworkBuilder.getInstance().setTabVisibility(!dontShowTabs.isSelected());

  }

  /**
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
  public List<PreferencesAdapter> getChildren() {
    return scList;
  }

}
