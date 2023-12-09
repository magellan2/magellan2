// class magellan.client.preferences.DetailsViewPreferences
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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import magellan.client.EMapDetailsPanel;
import magellan.client.EMapDetailsPanel.ShowItems;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

/**
 * Preferences adapter for EMapDetails
 */
public class DetailsViewPreferences extends JPanel implements ExtendedPreferencesAdapter {
  private EMapDetailsPanel source = null;
  private List<PreferencesAdapter> subAdapters;
  private PreferencesAdapter regionPref;
  private JCheckBox chkShowTagButtons;
  private JCheckBox chkAllowCustomIcons;
  private JCheckBox chkCompact;
  private JRadioButton rdbCapacityAllItems;
  private JRadioButton rdbCapacityFriendlyItems;
  private JRadioButton rdbCapacityMyItems;
  private JRadioButton rdbCapacitySomeItems;

  /**
   * Creates a new EMapDetailsPreferences object.
   */
  public DetailsViewPreferences(EMapDetailsPanel source) {
    this.source = source;

    subAdapters = new ArrayList<PreferencesAdapter>(2);
    subAdapters.add(source.getEditor().getPreferencesAdapter());
    subAdapters.add(source.getOrders().getPreferencesAdapter());

    // layout this container
    setLayout(new BorderLayout());

    // data view panel
    JPanel help = getDataViewPanel();
    this.add(help, BorderLayout.NORTH);

    // region panel information
    regionPref = source.getRegionPanel().getPreferredAdapter();
    this.add(regionPref.getComponent(), BorderLayout.CENTER);
  }

  private JPanel getDataViewPanel() {
    JPanel outerPanel = new JPanel(new GridBagLayout());
    outerPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("emapdetailspanel.prefs.datadisplay")));

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 2, 1, 1.0, 0, GridBagConstraints.NORTH,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;

    // JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

    chkShowTagButtons =
        new JCheckBox(Resources.get("emapdetailspanel.prefs.showTagButtons"), source
            .isShowingTagButtons());
    innerPanel.add(chkShowTagButtons);

    chkAllowCustomIcons =
        new JCheckBox(Resources.get("emapdetailspanel.prefs.allowCustomIcons"), source
            .isAllowingCustomIcons());
    innerPanel.add(chkAllowCustomIcons);

    chkCompact =
        new JCheckBox(Resources.get("emapdetailspanel.prefs.compact"), source.isCompactLayout());
    innerPanel.add(chkCompact);

    JLabel lblCapacity = new JLabel(Resources.get("emapdetailspanel.prefs.capacity.label"));
    ButtonGroup showButtons = new ButtonGroup();
    rdbCapacityAllItems =
        new JRadioButton(Resources.get("emapdetailspanel.prefs.capacity.all"), source
            .getShowCapacityItems() == ShowItems.SHOW_ALL);
    rdbCapacityFriendlyItems =
        new JRadioButton(Resources.get("emapdetailspanel.prefs.capacity.privilegedfactions"),
            source.getShowCapacityItems() == ShowItems.SHOW_PRIVILEGED_FACTIONS);
    rdbCapacityMyItems =
        new JRadioButton(Resources.get("emapdetailspanel.prefs.capacity.myfaction"), source
            .getShowCapacityItems() == ShowItems.SHOW_MY_FACTION);
    rdbCapacitySomeItems =
        new JRadioButton(Resources.get("emapdetailspanel.prefs.capacity.allfactions"), source
            .getShowCapacityItems() == ShowItems.SHOW_ALL_FACTIONS);
    showButtons.add(rdbCapacityAllItems);
    showButtons.add(rdbCapacityFriendlyItems);
    showButtons.add(rdbCapacityMyItems);
    showButtons.add(rdbCapacitySomeItems);

    lblCapacity.setLabelFor(rdbCapacityAllItems);

    innerPanel.add(lblCapacity, c);

    innerPanel.add(rdbCapacityAllItems);
    innerPanel.add(rdbCapacityFriendlyItems);
    innerPanel.add(rdbCapacityMyItems);
    innerPanel.add(rdbCapacitySomeItems);

    c.gridy++;
    outerPanel.add(innerPanel, c);

    return outerPanel;
  }

  /**
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
  public List<PreferencesAdapter> getChildren() {
    return subAdapters;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    chkShowTagButtons.setSelected(source.isShowingTagButtons());
    chkAllowCustomIcons.setSelected(source.isAllowingCustomIcons());
    chkCompact.setSelected(source.isCompactLayout());

    if (source.getShowCapacityItems() == ShowItems.SHOW_ALL) {
      rdbCapacityAllItems.setSelected(true);
    } else if (source.getShowCapacityItems() == ShowItems.SHOW_ALL_FACTIONS) {
      rdbCapacitySomeItems.setSelected(true);
    } else if (source.getShowCapacityItems() == ShowItems.SHOW_MY_FACTION) {
      rdbCapacityMyItems.setSelected(true);
    } else {
      rdbCapacityFriendlyItems.setSelected(true);
    }
    regionPref.initPreferences();
  }

  /**
   * preferences adapter code:
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    source.setShowTagButtons(chkShowTagButtons.isSelected());
    source.setAllowCustomIcons(chkAllowCustomIcons.isSelected());
    source.setCompactLayout(chkCompact.isSelected());

    if (rdbCapacityAllItems.isSelected()) {
      source.setShowCapacityItems(ShowItems.SHOW_ALL);
    } else if (rdbCapacitySomeItems.isSelected()) {
      source.setShowCapacityItems(ShowItems.SHOW_ALL_FACTIONS);
    } else if (rdbCapacityMyItems.isSelected()) {
      source.setShowCapacityItems(ShowItems.SHOW_MY_FACTION);
    } else {
      source.setShowCapacityItems(ShowItems.SHOW_PRIVILEGED_FACTIONS);
    }
    regionPref.applyPreferences();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("emapdetailspanel.prefs.title");
  }
}
