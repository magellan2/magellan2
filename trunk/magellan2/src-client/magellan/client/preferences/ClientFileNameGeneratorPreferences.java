// class magellan.client.preferences.ClientFileNameGeneratorPreferences
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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.FileNameGenerator;
import magellan.library.utils.Resources;

public class ClientFileNameGeneratorPreferences extends AbstractPreferencesAdapter implements PreferencesAdapter {
  private Properties settings;
  protected JTextField patternField;

  /**
   * Creates a new FileNameGenPrefAdapter object.
   */
  public ClientFileNameGeneratorPreferences(Properties settings) {
    this.settings = settings;
    initGUI();
  }

  private void initGUI() {
    getFileNameGeneratorPanel(addPanel(Resources.get("util.filenamegenerator.prefs.title")));
  }

  private Component getFileNameGeneratorPanel(JPanel fileNameGeneratorPanel) {
    fileNameGeneratorPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 1, 0,
                            GridBagConstraints.WEST,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 10, 1, 10), 0, 0);

    JLabel ordersSaveFileNamePatternLabel = new JLabel(Resources.get("util.filenamegenerator.field.ordersSaveFileNamePattern.label"));
    fileNameGeneratorPanel.add(ordersSaveFileNamePatternLabel,c);
    
    c.gridy++;
    patternField = new JTextField(settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern"),20);
    fileNameGeneratorPanel.add(patternField, c);
    
    c.gridy++;
    JLabel ordersSaveFileNamePatternInfo = new JLabel(Resources.get("util.filenamegenerator.field.ordersSaveFileNameInfo.label"));
    fileNameGeneratorPanel.add(ordersSaveFileNamePatternInfo,c);

    return fileNameGeneratorPanel;
  }

      public void initPreferences() {
          // what to do?
      }

  /**
   * Saves the editid pattern to the properties or removes the entry if pattern is 
   * not fine
   */
  public void applyPreferences() {
    String newPattern = patternField.getText();
    if (newPattern != null && newPattern.length()>2) {
      settings.setProperty("FileNameGenerator.ordersSaveFileNamePattern", newPattern);
    } else {
      settings.remove("FileNameGenerator.ordersSaveFileNamePattern");
    }
    FileNameGenerator.init(settings);
  }

  /**
   * Returns the component for showing in preferences dialog
   *
   * @return The Component
   */
  public Component getComponent() {
    return this;
  }

  /**
   * DOCUMENT-ME
   *
   * 
   */
  public String getTitle() {
    return Resources.get("util.filenamegenerator.prefs.title");
  }
}
