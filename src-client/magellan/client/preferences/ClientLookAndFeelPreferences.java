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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import magellan.client.Client;
import magellan.client.swing.MagellanLookAndFeel;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.SwingUtils;
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

  protected JList<String> jComboBoxLaF;

  /** if selected, region overview's and faction stat's top nodes will have handles */
  public JCheckBox chkRootHandles = null;

  protected Client source;
  protected Properties settings;

  private DefaultListModel<String> lafList;

  private JButton colorButton;

  private JPanel panelColor;

  private JTextField editFontSize;

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
            GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);

    panel.add(new JLabel(Resources.get("clientpreferences.lbl.relativefontsize.caption")), con);

    editFontSize = new JTextField(5);
    editFontSize.setText("100");

    resetFontSize();
    editFontSize.setInputVerifier(new FontSizeVerifier());
    editFontSize.setColumns(5);

    con.gridx = 1;
    panel.add(editFontSize, con);

    con.gridx = 2;
    JLabel l = new JLabel("%");
    panel.add(l, con);
    con.gridx++;
    con.weightx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;
    panel.add(new JPanel(), con);

    JComponent help = WrappableLabel.getLabel(Resources.get("clientpreferences.txt.restartforfontsize.caption"))
        .getComponent();

    con.gridx = 0;
    con.gridy = 1;
    con.gridwidth = 4;
    panel.add(help, con);

    return panel;
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
    con.gridwidth = 3;

    panel.add(new JScrollPane(jComboBoxLaF), con);
    con.gridwidth = 1;
    jComboBoxLaF.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent e) {
        colorButton.setEnabled(isMetal(jComboBoxLaF.getSelectedValue()));
      }
    });

    colorButton = new JButton(Resources.get("clientpreferences.desktopcolor.button"));
    colorButton.setEnabled(UIManager.getLookAndFeel() instanceof MetalLookAndFeel);
    colorButton.addActionListener(this);
    con.gridx = 1;
    con.gridy = 1;
    con.weightx = 0;
    panel.add(colorButton, con);

    panelColor = new JPanel();
    panelColor.setBorder(new LineBorder(Color.black));
    SwingUtils.setPreferredSize(panelColor, 1.5, 1.5, false);
    panelColor.setBackground(MetalLookAndFeel.getWindowBackground());

    con.gridx++;
    con.anchor = GridBagConstraints.CENTER;
    panel.add(panelColor, con);

    return panel;
  }

  protected boolean isMetal(String selectedValue) {
    Map<String, ? extends LookAndFeel> lafs = MagellanLookAndFeel.getLookAndFeels();
    for (String l : lafs.keySet()) {
      if (l.equals(selectedValue))
        return lafs.get(l) instanceof MetalLookAndFeel;
    }
    return false;
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

  public class FontSizeVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent input) {
      float size = getFontSize();
      if (Float.isNaN(size) || size <= .01 || size >= 10) {
        resetFontSize();
        return false;
      }
      return true;
    }

  }

  protected float getFontSize() {
    try {
      float fScale = Float.valueOf(editFontSize.getText()).floatValue();
      fScale /= 100.0f;
      return fScale;
    } catch (NumberFormatException ex) {
      return Float.NaN;
    }
  }

  protected void resetFontSize() {
    try {
      float fScale = Float.valueOf(settings.getProperty("Client.FontScale", "1.0")).floatValue();
      fScale *= 100.0f;
      editFontSize.setText(Float.toString(fScale));
    } catch (Exception exc) {
      editFontSize.setText("100.0");
    }
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
    jComboBoxLaF.setSelectedValue(source.getLookAndFeel(), true);
    chkRootHandles.setSelected(PropertiesHelper.getBoolean(settings,
        "EMapOverviewPanel.treeRootHandles", true));
    colorButton.setEnabled(isMetal(jComboBoxLaF.getSelectedValue()));
    panelColor.setBackground(MetalLookAndFeel.getWindowBackground());
    resetFontSize();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    settings.setProperty("EMapOverviewPanel.treeRootHandles", String.valueOf(chkRootHandles
        .isSelected()));

    float fScale = getFontSize();
    boolean update = false;
    if (!Float.isNaN(fScale)) {
      float oldScale = PropertiesHelper.getFloat(settings, "Client.FontScale", 1.0f);
      if (oldScale != fScale) {
        settings.setProperty("Client.FontScale", String.valueOf(fScale));
        update = true;
      }
    } else {
      Logger.getInstance(this.getClass()).warn("Invalid font size: " + editFontSize.getText());
    }

    if (isMetal(jComboBoxLaF.getSelectedValue())) {
      if (!MetalLookAndFeel.getWindowBackground().equals(panelColor.getBackground())) {
        MagellanLookAndFeel.setBackground(panelColor.getBackground(), settings);
        update = true;
      }
    }
    if (update || !jComboBoxLaF.getSelectedValue().equals(source.getLookAndFeel())) {
      source.setLookAndFeel(jComboBoxLaF.getSelectedValue(), true);
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    changeMetalBackground();
  }

  protected void changeMetalBackground() {
    if (isMetal(jComboBoxLaF.getSelectedValue())) {
      Color col =
          JColorChooser.showDialog(source, Resources.get("clientpreferences.desktopcolor.title"),
              MetalLookAndFeel.getWindowBackground());

      if (col != null) {
        panelColor.setBackground(col);
      }
    }
  }

}
