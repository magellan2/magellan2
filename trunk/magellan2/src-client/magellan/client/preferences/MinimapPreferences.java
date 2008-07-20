// class magellan.client.preferences.MinimapPreferences
// created on 16.02.2008
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import magellan.client.swing.MapperPanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

public class MinimapPreferences extends JPanel implements PreferencesAdapter, ActionListener {
  private JSlider sldZoom;
  private JComboBox cmbDisplayMode;
  private JCheckBox autoScale;
  private PreferencesAdapter renderers;
  
  private MapperPanel source;

  /**
   * Creates a new MinimapPreferences object.
   */
  public MinimapPreferences(MapperPanel source) {
    this.source = source;
    renderers = source.getMinimap().getPreferencesAdapter();

    // display mode combo box
    String items[] = new String[5];
    items[0] = Resources.get("mapperpanel.prefs.minimapitems.terrain");
    items[1] = Resources.get("mapperpanel.prefs.minimapitems.politics");
    items[2] = Resources.get("mapperpanel.prefs.minimapitems.allfactions");
    items[3] = Resources.get("mapperpanel.prefs.minimapitems.trustlevel");
    items[4] = Resources.get("mapperpanel.prefs.minimapitems.trustlevelguard");
    cmbDisplayMode = new JComboBox(items);
    cmbDisplayMode.setSelectedIndex(source.getMinimapMode());

    JLabel lblDisplayMode = new JLabel(Resources.get("mapperpanel.prefs.lbl.minimapoptions"));
    lblDisplayMode.setLabelFor(cmbDisplayMode);
    lblDisplayMode.setHorizontalTextPosition(SwingConstants.CENTER);

    // color synching button
    JButton btnSyncColors = new JButton(Resources.get("mapperpanel.prefs.lbl.synccolors.caption"));
    btnSyncColors.setActionCommand("mapperpanel.prefs.lbl.synccolors.caption");
    btnSyncColors.addActionListener(this);

    // zoom slider
    sldZoom = new JSlider(1, 25, 10);
    sldZoom.setLabelTable(sldZoom.createStandardLabels(5));
    sldZoom.setMajorTickSpacing(10);
    sldZoom.setMinorTickSpacing(5);
    sldZoom.setPaintLabels(true);
    sldZoom.setPaintTicks(true);
    sldZoom.setValue(source.getMinimapScale());

    JLabel lblZoom = new JLabel(Resources.get("mapperpanel.prefs.lbl.zoom"));
    lblZoom.setLabelFor(sldZoom);
    lblZoom.setHorizontalTextPosition(SwingConstants.CENTER);

    // auto scale checkbox
    autoScale = new JCheckBox(Resources.get("mapperpanel.prefs.lbl.minimapautoscale"), source.isAutoScaling());

    // panel grouping minimap stuff
    this.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("mapperpanel.prefs.border.minimap")));

    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    JPanel scalePanel = new JPanel(new GridBagLayout());
    scalePanel.setBorder(new TitledBorder( Resources.get("mapperpanel.prefs.border.zoom")));

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0;
    scalePanel.add(lblZoom, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    scalePanel.add(sldZoom, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 3;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 1;
    c.weighty = 0;
    scalePanel.add(autoScale, c);
    
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 4;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 1;
    this.add(scalePanel,c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 4;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 1;
    this.add(renderers.getComponent(), c);

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
    return Resources.get("mapperpanel.prefs.border.minimap");
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
    renderers.applyPreferences();

    // setMinimapMode(cmbDisplayMode.getSelectedIndex());
    if (autoScale.isSelected()) {
      source.setAutoScaling(true);
    } else {
      source.setAutoScaling(false);
      source.setMinimapScale(sldZoom.getValue());
    }

    source.getMinimapComponent().doLayout();
    source.getMinimapComponent().repaint(100);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand()==null) {
      return;
    }
    if (e.getActionCommand().equals("mapperpanel.prefs.lbl.synccolors.caption")) {
      source.synchronizeMinimap();
    }
  }
}
