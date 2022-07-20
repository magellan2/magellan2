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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import magellan.client.Client;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.AbstractNameGenerator;
import magellan.library.utils.NameGenerator;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;

/**
 * 
 */
public class ClientNameGeneratorPreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter, ActionListener {
  protected JCheckBox active;
  protected JTextField fileField;
  protected Properties settings;
  private JLabel namesLeft;
  private NameGenerator namegen;

  /**
   * Creates a new NameGenPrefAdapter object.
   */
  public ClientNameGeneratorPreferences(Client client, Properties settings, NameGenerator gen) {
    this.settings = settings;
    namegen = gen;
    initGUI();
  }

  private void initGUI() {
    getNameGeneratorPanel(addPanel(Resources.get("util.namegenerator.prefs.title")));
  }

  private Component getNameGeneratorPanel(JPanel parent) {
    parent.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(2, 10, 1, 10), 0, 0);

    active =
        new JCheckBox(Resources.get("util.namegenerator.prefs.active"), namegen.isActive());
    parent.add(active, c);

    c.gridy++;
    c.gridwidth = 1;
    c.insets.right = 1;

    JComponent helpText = WrappableLabel.getLabel(Resources.get("util.namegenerator.prefs.help")).getComponent();
    parent.add(helpText, c);
    c.gridy++;

    fileField = new JTextField(settings.getProperty("NameGenerator.Source"), 20);
    parent.add(fileField, c);
    c.gridx++;
    c.insets.right = 10;
    c.fill = GridBagConstraints.NONE;

    JButton b = new JButton("...");
    b.addActionListener(this);
    parent.add(b, c);

    c.gridy++;
    c.gridx = 0;
    JButton reloadButton = new JButton(new AbstractAction(Resources.get("util.namegenerator.prefs.reload")) {
      public void actionPerformed(ActionEvent e) {
        reload();
      }
    });

    parent.add(reloadButton, c);

    c.gridy++;
    namesLeft = new JLabel(Resources.get("util.namegenerator.prefs.namesleft", namegen.getNamesCount()));
    parent.add(namesLeft, c);

    if (namegen instanceof AbstractNameGenerator) {
      namesLeft.setToolTipText(Resources.get("util.namegenerator.prefs.cache", ((AbstractNameGenerator) namegen)
          .getCache()));
    }

    fileField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        update(e);
      }

      private void update(DocumentEvent e) {
        String text = fileField.getText().trim();
        reloadButton.setEnabled(text.length() != 0);
      }

    });

    return parent;
  }

  private void reload() {
    namegen.load(fileField.getText().trim());
    updateLeft();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    fileField.setText(".");
    fileField.setText(settings.getProperty("NameGenerator.Source"));
    active.setSelected(namegen.isActive() && namegen.getNamesCount() > 0);
    updateLeft();
  }

  private void updateLeft() {
    namesLeft.setText(Resources.get("util.namegenerator.prefs.namesleft", namegen.getNamesCount()));
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    String old = settings.getProperty("NameGenerator.Source");
    String filename = fileField.getText().trim();
    if (!filename.equals(old)) {
      namegen.load(filename);
    }

    boolean available = active.isSelected() && namegen.getNamesCount() > 0;
    settings.setProperty("NameGenerator.Source", filename);
    namegen.setEnabled(available);

    if (available) {
      settings.setProperty("NameGenerator.active", "true");
    } else {
      settings.remove("NameGenerator.active");
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
    return Resources.get("util.namegenerator.prefs.title");
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    String filename = settings.getProperty("NameGenerator.Source");
    File dir = null;
    if (filename != null) {
      dir = new File(filename).getParentFile();
    }
    if (dir == null) {
      dir = new File(Client.getResourceDirectory(), "etc/names");
    }

    JFileChooser f = new JFileChooser(dir);
    int ret = f.showOpenDialog(this);

    if (ret == JFileChooser.APPROVE_OPTION) {
      fileField.setText(f.getSelectedFile().toString());
      active.setSelected(!Utils.isEmpty(fileField.getText()));
      reload();
    }
  }
}
