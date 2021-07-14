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
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import magellan.client.Client;
import magellan.client.swing.MagellanLookAndFeel;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This component serves the Look'n Feel Preferences
 * 
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class ClientLookAndFeelPreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter, ActionListener {
  private final Logger log = Logger.getInstance(ClientLookAndFeelPreferences.class);

  protected JTextField editFontSize;
  protected JList jComboBoxLaF;

  /** if selected, region overview's and faction stat's top nodes will have handles */
  public JCheckBox chkRootHandles = null;

  // /** if selected, messages are linewrapped */
  // protected JCheckBox lineWrap;
  //
  protected Client source;
  protected Properties settings;

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
    createFontPanel(addPanel(Resources.get("clientpreferences.border.fontsize")));
    createMiscPanel(addPanel(Resources.get("clientpreferences.border.misc")));
  }

  protected Container createFontPanel(JPanel panel) {
    panel.setLayout(new GridBagLayout());

    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);

    panel.add(new JLabel(Resources.get("clientpreferences.lbl.relativefontsize.caption")), con);

    editFontSize = new JTextField(5);
    editFontSize.setPreferredSize(new java.awt.Dimension(50, 20));
    editFontSize.setText("100");

    try {
      float fScale = Float.valueOf(settings.getProperty("Client.FontScale", "1.0")).floatValue();
      fScale *= 100.0f;
      editFontSize.setText(Float.toString(fScale));
    } catch (Exception exc) {
    }

    editFontSize.setMinimumSize(new java.awt.Dimension(50, 20));

    con.insets.left = 0;
    con.gridx = 1;
    panel.add(editFontSize, con);

    con.gridx = 2;
    JLabel l = new JLabel("%");
    panel.add(l, con);

    WrappableLabel help = WrappableLabel.getLabel(Resources.get("clientpreferences.txt.restartforfontsize.caption"));

    con.gridx = 0;
    con.gridy = 1;
    con.gridwidth = 3;
    con.weightx = 1;
    panel.add(help, con);

    return panel;
  }

  protected Container createLAndFPanel(JPanel panel) {
    panel.setLayout(new GridBagLayout());

    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);

    panel.add(new JLabel(Resources.get("clientpreferences.lbl.lafrenderer.caption")), con);

    String renderer[] = source.getLookAndFeels();
    jComboBoxLaF = new JList(renderer);
    jComboBoxLaF.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jComboBoxLaF.setSelectedValue(settings.getProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL,
        Client.DEFAULT_LAF), true);
    con.gridx = 1;
    con.weightx = 1;
    panel.add(new JScrollPane(jComboBoxLaF), con);

    JButton button = new JButton(Resources.get("clientpreferences.desktopcolor.button"));
    button.addActionListener(this);
    con.gridx = 1;
    con.gridy = 1;
    con.weightx = 1;
    panel.add(button, con);

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

    // lineWrap = new JCheckBox(Resources.get("messagepanel.prefs.linewrap"),
    // source.getMessagePanel().isLineWrap());
    // panel.add(lineWrap);

    return panel;
  }

  /**
   * DOCUMENT-ME
   */
  public Component getComponent() {
    return this;
  }

  /**
   * DOCUMENT-ME
   */
  public String getTitle() {
    return Resources.get("clientpreferences.border.lookandfeel");
  }

  /**
   * TODO: implement it
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // TODO: implement it
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    try {
      float fScale = Float.valueOf(editFontSize.getText()).floatValue();
      fScale /= 100.0f;

      settings.setProperty("Client.FontScale", Float.toString(fScale));
    } catch (NumberFormatException ex) {
      log.error(ex);
      javax.swing.JOptionPane.showMessageDialog(this, Resources
          .get("clientpreferences.msg.fontsizeerror.text")
          + ex.toString(), Resources.get("clientpreferences.msg.fontsizeerror.title"),
          javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    // source.setLookAndFeel((String)jComboBoxLaF.getSelectedItem());
    source.setLookAndFeel((String) jComboBoxLaF.getSelectedValue());

    settings.setProperty("EMapOverviewPanel.treeRootHandles", String.valueOf(chkRootHandles
        .isSelected()));

    // source.getMessagePanel().setLineWrap(lineWrap.isSelected());
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
