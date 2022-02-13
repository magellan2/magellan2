// class magellan.client.preferences.ClientLaFPreferences
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import magellan.client.Client;
import magellan.client.swing.MagellanLookAndFeel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * This component serves the Look'n Feel Preferences
 * 
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class ClientLookAndFeelPreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter, ActionListener {

  protected JList<String> jComboBoxLaF;

  /** if selected, region overview's and faction stat's top nodes will have handles */
  public JCheckBox chkRootHandles = null;

  protected Client source;
  protected Properties settings;

  private DefaultListModel<String> lafList;

  private JButton colorButton;

  /**
   * Creates a new LAndF object.
   */
  public ClientLookAndFeelPreferences(Client client, Properties settings) {
    source = client;
    this.settings = settings;
    initGUI();
  }

  private void initGUI() {
    createLAndFPanel(addPanel(Resources.get("clientpreferences.border.lookandfeel")));
    createMiscPanel(addPanel(Resources.get("clientpreferences.border.misc")));
  }

  protected Container createLAndFPanel(JPanel panel) {
    panel.setLayout(new GridBagLayout());

    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);

    panel.add(new JLabel(Resources.get("clientpreferences.lbl.lafrenderer.caption")), con);

    lafList = new DefaultListModel<String>();
    jComboBoxLaF = new JList<String>(lafList);
    jComboBoxLaF.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    con.gridx = 1;
    con.weightx = 1;
    panel.add(new JScrollPane(jComboBoxLaF), con);

    colorButton = new JButton(Resources.get("clientpreferences.desktopcolor.button"));
    colorButton.setEnabled(UIManager.getLookAndFeel() instanceof MetalLookAndFeel);
    colorButton.addActionListener(this);
    con.gridx = 1;
    con.gridy = 1;
    con.weightx = 1;
    panel.add(colorButton, con);

    return panel;
  }

  protected Container createMiscPanel(JPanel panel) {
    panel.setLayout(new GridBagLayout());

    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);

    chkRootHandles = new JCheckBox(Resources.get("clientpreferences.lbl.roothandles"));
    chkRootHandles.setSelected(PropertiesHelper.getBoolean(settings,
        "EMapOverviewPanel.treeRootHandles", true));
    panel.add(chkRootHandles, con);

    return panel;
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
    return Resources.get("clientpreferences.border.lookandfeel");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    lafList.clear();
    lafList.addAll(Arrays.asList(source.getLookAndFeels()));
    jComboBoxLaF.setSelectedValue(settings.getProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL,
        Client.DEFAULT_LAF), true);
    chkRootHandles.setSelected(PropertiesHelper.getBoolean(settings,
        "EMapOverviewPanel.treeRootHandles", true));
    colorButton.setEnabled(UIManager.getLookAndFeel() instanceof javax.swing.plaf.metal.MetalLookAndFeel);
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    source.setLookAndFeel(jComboBoxLaF.getSelectedValue());

    settings.setProperty("EMapOverviewPanel.treeRootHandles", String.valueOf(chkRootHandles
        .isSelected()));
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    changeMetalBackground();
  }

  protected void changeMetalBackground() {
    LookAndFeel laf = UIManager.getLookAndFeel();

    if (laf instanceof javax.swing.plaf.metal.MetalLookAndFeel) {
      Color col =
          JColorChooser.showDialog(source, Resources.get("clientpreferences.desktopcolor.title"),
              MetalLookAndFeel.getWindowBackground());

      if (col != null) {
        MagellanLookAndFeel.setBackground(col, settings);
      }
    }
  }

}
