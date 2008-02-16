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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.swing.layout.GridBagHelper;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.FileNameGenerator;
import magellan.library.utils.Resources;

public class ClientFileNameGeneratorPreferences extends JPanel implements PreferencesAdapter {
  private Client client;
  private Properties settings;
  protected JTextField patternField;

  /**
   * Creates a new FileNameGenPrefAdapter object.
   */
  public ClientFileNameGeneratorPreferences(Client client, Properties settings) {
    this.client = client;
    this.settings = settings;
    initGUI();
  }

  private void initGUI() {
    /*
    */

    // set up the panel for the maximum file history size
    // layout this container
    setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.insets.top = 10;
    c.insets.bottom = 10;
    GridBagHelper.setConstraints(c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
                   GridBagConstraints.NORTHWEST,
                   GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

    this.add(getFileNameGeneratorPanel(), c);

  }

  private Component getFileNameGeneratorPanel() {
    JPanel fileNameGeneratorPanel = new JPanel();
    fileNameGeneratorPanel.setLayout(new GridBagLayout());
    fileNameGeneratorPanel.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                               new EmptyBorder(0, 3, 3, 3)),
                    Resources.get("util.filenamegenerator.prefs.title")));

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
