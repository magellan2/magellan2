// class magellan.client.preferences.ClientNameGeneratorPreferences
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
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.swing.layout.GridBagHelper;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.NameGenerator;
import magellan.library.utils.Resources;


 public class ClientNameGeneratorPreferences extends JPanel implements PreferencesAdapter, ActionListener {
  protected JCheckBox active;
  protected JTextField fileField;
  protected Properties settings;

  /**
   * Creates a new NameGenPrefAdapter object.
   */
  public ClientNameGeneratorPreferences(Client client, Properties settings) {
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

    // help panel
    this.add(getNameGeneratorPanel(), c);
    c.insets.top = 0;
  }

  private Component getNameGeneratorPanel() {
    JPanel help = new JPanel();
    help.setLayout(new GridBagLayout());
    help.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                               new EmptyBorder(0, 3, 3, 3)),
                    Resources.get("util.namegenerator.prefs.title")));

    GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 1, 0,
                            GridBagConstraints.WEST,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(2, 10, 1, 10), 0, 0);

    active = new JCheckBox(Resources.get("util.namegenerator.prefs.active"), NameGenerator.getInstance().isActive());
    help.add(active, c);

    c.gridy++;
    c.gridwidth = 1;
    c.insets.right = 1;
    fileField = new JTextField(settings.getProperty("NameGenerator.Source"), 20);
    help.add(fileField, c);
    c.gridx++;
    c.insets.right = 10;
    c.fill = GridBagConstraints.NONE;

    JButton b = new JButton("...");
    b.addActionListener(this);
    help.add(b, c);

    return help;
  }

      public void initPreferences() {
          // TODO: implement it
      }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    boolean available = active.isSelected();
    settings.setProperty("NameGenerator.Source", fileField.getText());
    NameGenerator.getInstance().setEnabled(available);

    if(available) {
      settings.setProperty("NameGenerator.active", "true");
    } else {
      settings.remove("NameGenerator.active");
    }

    NameGenerator.getInstance().load(fileField.getText());
  }

  /**
   * DOCUMENT-ME
   *
   * 
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
    return Resources.get("util.namegenerator.prefs.title");
  }

  /**
   * DOCUMENT-ME
   *
   * 
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    String s = settings.getProperty("NameGenerator.Source");

    if(s == null) {
      s = ".";
    } else {
      int i = s.lastIndexOf(File.separatorChar);

      if(i > 0) {
        s = s.substring(0, i);
      } else {
        s = ".";
      }
    }

    JFileChooser f = new JFileChooser(s);
    int ret = f.showOpenDialog(this);

    if(ret == JFileChooser.APPROVE_OPTION) {
      fileField.setText(f.getSelectedFile().toString());
    }
  }
}
