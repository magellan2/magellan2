// class magellan.client.preferences.ClientFilePreferences
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.client.preferences;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import magellan.client.Client;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.FileNameGenerator;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

public class ClientFilePreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter {
  private final Logger log = Logger.getInstance(ClientFilePreferences.class);
  protected JSpinner txtFileHistorySize;
  protected JSpinner txtCRBackupsCount;
  protected JTextField patternField;

  protected Client source;
  protected Properties settings;

  /**
   * Creates a new HistoryPanel object.
   */
  public ClientFilePreferences(Client client, Properties settings) {
    source = client;
    this.settings = settings;
    initGUI();
  }

  private void initGUI() {
    JPanel help =
        addPanel(Resources.get("clientpreferences.border.filehistory"), new GridBagLayout());

    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 1);
    JLabel l = new JLabel(Resources.get("clientpreferences.lbl.filehistoryentries.caption"));
    help.add(l, con);
    con.gridx = 1;
    con.gridwidth = 2;
    txtFileHistorySize =
        new JSpinner(new SpinnerNumberModel(5, 0, 99, 1));

    help.add(txtFileHistorySize, con);

    JTextArea txtDescription =
        new JTextArea(Resources.get("clientpreferences.txt.filehistorydescription.text"));
    txtDescription.setLineWrap(true);
    txtDescription.setWrapStyleWord(true);
    txtDescription.setEditable(false);
    txtDescription.setOpaque(false);
    txtDescription.setSelectionColor(getBackground());
    txtDescription.setSelectedTextColor(getForeground());
    txtDescription.setFont(l.getFont());
    txtDescription.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults().get(
        "Label.foreground"));
    // need this to limit size of panel
    txtDescription.setPreferredSize(new java.awt.Dimension(50, 20));

    con.gridx = 0;
    con.gridy = 1;
    con.gridwidth = 2;
    con.weightx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;
    help.add(txtDescription, con);

    JPanel jpanel_CRBackups =
        addPanel(Resources.get("clientpreferences.border.crbackups"), new GridBagLayout());

    GridBagConstraints c_CR =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0);
    JLabel l2 = new JLabel(Resources.get("clientpreferences.lbl.numberofbackups.caption"));

    jpanel_CRBackups.add(l2, c_CR);

    txtCRBackupsCount =
        new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));

    c_CR.gridx++;
    jpanel_CRBackups.add(txtCRBackupsCount, c_CR);

    JTextArea txtDescription2 =
        new JTextArea(Resources.get("clientpreferences.txt.crbackupsdescription.text"));
    txtDescription2.setLineWrap(true);
    txtDescription2.setWrapStyleWord(true);
    txtDescription2.setEditable(false);
    txtDescription2.setOpaque(false);
    txtDescription2.setSelectionColor(getBackground());
    txtDescription2.setSelectedTextColor(getForeground());
    txtDescription2.setFont(l.getFont());
    txtDescription2.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults().get(
        "Label.foreground"));
    // need this to limit size of panel
    txtDescription2.setPreferredSize(new java.awt.Dimension(50, 20));

    c_CR.gridx = 0;
    c_CR.gridy = 1;
    c_CR.weightx = 1.0;
    c_CR.gridwidth = 2;
    c_CR.fill = GridBagConstraints.HORIZONTAL;
    jpanel_CRBackups.add(txtDescription2, c_CR);

    
    JPanel fileNameGeneratorPanel = addPanel(Resources.get("util.filenamegenerator.prefs.title"), new GridBagLayout());

    con =
        new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(2, 10, 1, 10), 0, 0);

    JLabel ordersSaveFileNamePatternLabel =
        new JLabel(Resources.get("util.filenamegenerator.field.ordersSaveFileNamePattern.label"));
    fileNameGeneratorPanel.add(ordersSaveFileNamePatternLabel, con);

    con.gridy++;
    patternField = new JTextField("", 20);
    fileNameGeneratorPanel.add(patternField, con);

    con.gridy++;
    JLabel ordersSaveFileNamePatternInfo =
        new JLabel(Resources.get("util.filenamegenerator.field.ordersSaveFileNameInfo.label"));
    fileNameGeneratorPanel.add(ordersSaveFileNamePatternInfo, con);

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
    return Resources.get("clientpreferences.border.filehistory");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    txtFileHistorySize.setValue(source.getMaxFileHistorySize());
    txtCRBackupsCount.setValue(PropertiesHelper.getInteger(settings, "Client.CRBackups.count",
        FileBackup.DEFAULT_BACKUP_LEVEL));
    patternField.setText(settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern"));
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    try {
      int i = (Integer) txtFileHistorySize.getValue();
      source.setMaxFileHistorySize(i);
    } catch (NumberFormatException e) {
      log.error("ClientPreferences(): Unable to set maximum file history size", e);
    }

    try {
      int i = (Integer) txtCRBackupsCount.getValue();
      settings.setProperty("Client.CRBackups.count", i + "");
    } catch (NumberFormatException e) {
      log.error("ClientPreferences(): Unable to set CR-Backup count", e);
    }

    String newPattern = patternField.getText();
    if (newPattern != null && newPattern.length() > 2) {
      settings.setProperty("FileNameGenerator.ordersSaveFileNamePattern", newPattern);
    } else {
      settings.remove("FileNameGenerator.ordersSaveFileNamePattern");
    }
    FileNameGenerator.init(settings);
  }
}
