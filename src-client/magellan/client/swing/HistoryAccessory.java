/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.library.utils.Bucket;
import magellan.library.utils.PropertiesHelper;

/**
 * A FileChooser that provides a combobox with previously used directories (loading them from and
 * storing them to properties).
 */
public class HistoryAccessory extends JPanel {
  protected Properties settings = null;
  protected JFileChooser chooser = null;
  private Bucket<DirWrapper> history = new Bucket<DirWrapper>(6);

  /**
   * Creates a new HistoryAccessory object.
   */
  public HistoryAccessory(Properties setting, JFileChooser fileChooser) {
    settings = setting;
    chooser = fileChooser;

    if (PropertiesHelper.getList(settings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY)
        .isEmpty()) {
      PropertiesHelper.setList(settings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY,
          Collections.singletonList(fileChooser.getCurrentDirectory()));
    }

    // load history fifo buffer
    for (String dirName : PropertiesHelper.getList(settings,
        PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY)) {
      File dir = new File(dirName);

      if (dir.exists() && dir.isDirectory()) {
        history.add(new DirWrapper(dir));
      }
    }

    // intercept file chooser selection approvals
    chooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          approveSelection();
        }
      }
    });

    // set-up GUI
    setLayout(new GridBagLayout());
    if (history.size() > 1) {

      GridBagConstraints c = new GridBagConstraints();

      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.weighty = 0.0;
      this.add(new JLabel("Directory History:"), c);

      JComboBox cmbHistory = new JComboBox(history.toArray());
      cmbHistory.setEditable(false);
      cmbHistory.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chooser.setCurrentDirectory(((DirWrapper) ((JComboBox) e.getSource()).getSelectedItem())
              .getDirectory());

          // chooser.setSelectedFile(new File(""));
        }
      });

      c.anchor = GridBagConstraints.NORTH;
      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.weighty = 0.0;
      this.add(cmbHistory, c);
    }
  }

  protected void approveSelection() {
    history.add(new DirWrapper(chooser.getSelectedFile().getParentFile()));

    List<String> dirs = new ArrayList<String>(7);

    for (DirWrapper dirWrapper : history) {
      dirs.add((dirWrapper).getDirectory().getAbsolutePath());
    }

    Collections.reverse(dirs);
    PropertiesHelper.setList(settings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY, dirs);
  }
}

/**
 * Wrapper class to put a File (directory) into a bucket.
 */
class DirWrapper {
  private File dir = null;

  /**
   * Creates a new DirWrapper object.
   */
  public DirWrapper(File f) {
    dir = f;
  }

  /**
   * Returns the directory.
   */
  public File getDirectory() {
    return dir;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String dirName = dir.getAbsolutePath();

    if (dirName.length() > 30) {
      dirName = dirName.substring(0, 10) + "..." + dirName.substring(dirName.length() - 18);
    }

    return dirName;
  }

  // Bucket needs this
  /**
   * Compares the directories.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof DirWrapper) {
      if (this == o)
        return true;
      return getDirectory().equals(((DirWrapper) o).getDirectory());
    }
    return false;

  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getDirectory() == null ? 42 : getDirectory().hashCode();
  }
}
