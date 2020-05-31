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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import magellan.client.swing.MapperPanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

public class MinimapPreferences extends AbstractPreferencesAdapter implements PreferencesAdapter {
  private JSlider sldZoom;
  private JCheckBox autoScale;
  private PreferencesAdapter rendererPreferences;

  private MapperPanel source;

  /**
   * Creates a new MinimapPreferences object.
   */
  public MinimapPreferences(MapperPanel source) {
    this.source = source;
    rendererPreferences = source.getMinimap().getPreferencesAdapter();

    // zoom slider
    sldZoom = new JSlider(1, 26, 10);
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
    autoScale =
        new JCheckBox(Resources.get("mapperpanel.prefs.lbl.minimapautoscale"), source
            .isAutoScaling());

    // panel grouping minimap stuff
    // setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
    // .get("mapperpanel.prefs.border.minimap")));

    // setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // JPanel scalePanel = new JPanel(new GridBagLayout());
    // scalePanel.setBorder(new TitledBorder(Resources.get("mapperpanel.prefs.border.zoom")));
    JPanel scalePanel = addPanel(Resources.get("mapperpanel.prefs.border.zoom"),
        new GridBagLayout());

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

    addComponent(rendererPreferences.getComponent());

    // c.anchor = GridBagConstraints.CENTER;
    // c.gridx = 0;
    // c.gridy = 0;
    // c.gridwidth = 4;
    // c.gridheight = 1;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.weightx = 0.1;
    // c.weighty = 1;
    // this.add(scalePanel, c);
    //
    // c.anchor = GridBagConstraints.CENTER;
    // c.gridx = 0;
    // c.gridy = 1;
    // c.gridwidth = 4;
    // c.gridheight = 1;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.weightx = 1;
    // c.weighty = 1;
    // this.add(rendererPreferences.getComponent(), c);

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
    sldZoom.setValue(source.getMinimapScale());
    autoScale.setSelected(source.isAutoScaling());

    rendererPreferences.initPreferences();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    rendererPreferences.applyPreferences();

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

}
