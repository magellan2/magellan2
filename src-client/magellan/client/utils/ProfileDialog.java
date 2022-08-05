// class magellan.client.UpdateDialog
// created on Jan 21, 2010
//
// Copyright 2003-2010 by magellan project team
//
// $Author: stm$
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
package magellan.client.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

import magellan.client.swing.EresseaFileFilter;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.utils.ProfileManager.ProfileException;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A Dialog for selecting profiles.
 * 
 * @author stm
 */
public class ProfileDialog extends JDialog {
  private static Logger log = Logger.getInstance(ProfileDialog.class);

  private boolean abort = true;

  private DefaultListModel<String> profiles;

  private JList<String> profileList;

  private JCheckBox bAlwaysAsk;

  /**
   * Creates a new modal dialog.
   * 
   * @see JDialog#JDialog(Frame, String, boolean)
   * @param parent
   */
  public ProfileDialog(Frame parent) {
    super(parent, Resources.get("profiledialog.window.caption"), true);

    initGUI();
    setModal(true);

    SwingUtils.center(this);
  }

  private void initGUI() {
    final JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new GridBagLayout());
    // mainPanel.setMinimumSize(new Dimension(300, 1900));

    WrappableLabel comment = WrappableLabel.getLabel(Resources.get("profiledialog.explanation"));
    JComponent pcomment = comment.getComponent();
    profiles = initProfiles();
    profileList = new JList<String>(profiles);
    profileList.setSelectedIndex(profiles.indexOf(ProfileManager.getCurrentProfile()));
    profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JPanel buttonPanel = new JPanel();

    bAlwaysAsk = new JCheckBox(Resources.get("profiledialog.box.alwaysask.name"));
    bAlwaysAsk.setSelected(ProfileManager.isAlwaysAsk());

    buttonPanel.setLayout(new GridBagLayout());

    // Create Button
    final JButton btnCreate = new JButton(Resources.get("profiledialog.btn.create.caption"));
    btnCreate.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        createProfile();
      }
    });

    final JButton btnCopy = new JButton(Resources.get("profiledialog.btn.copy.caption"));
    btnCopy.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        copy();
      }
    });

    // Remove Button
    final JButton btnRemove = new JButton(Resources.get("profiledialog.btn.remove.caption"));
    btnRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        remove();
      }
    });

    // OK Button
    final JButton btnOK = new JButton(Resources.get("profiledialog.btn.ok.caption"));
    btnOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        quit(false);
      }
    });

    // Quit Button
    final JButton btnQuit = new JButton(Resources.get("profiledialog.btn.quit.caption"));
    btnQuit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        quit(true);
      }
    });

    // Quit Button
    final JButton btnExport = new JButton(Resources.get("profiledialog.btn.export.caption"));
    btnExport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        exportProfiles();
      }
    });

    // Quit Button
    final JButton btnImport = new JButton(Resources.get("profiledialog.btn.import.caption"));
    btnImport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        importProfiles();
      }
    });

    profileList.addListSelectionListener((e) -> {
      boolean enabled = profileList.getSelectedValue() != null;
      btnOK.setEnabled(enabled);
      btnCopy.setEnabled(enabled);
      btnRemove.setEnabled(enabled);
    });
    if (profileList.getSelectedValue() == null) {
      btnOK.setEnabled(false);
      btnCopy.setEnabled(false);
      btnRemove.setEnabled(false);
    }

    GridBagConstraints gc =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 1, 1);

    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weighty = 0;
    buttonPanel.add(btnOK, gc);
    gc.gridy++;
    buttonPanel.add(btnQuit, gc);
    gc.gridy++;
    buttonPanel.add(new JSeparator(), gc);
    gc.gridy++;
    buttonPanel.add(btnCreate, gc);
    gc.gridy++;
    buttonPanel.add(btnCopy, gc);
    gc.gridy++;
    buttonPanel.add(btnRemove, gc);
    gc.gridy++;
    buttonPanel.add(new JSeparator(), gc);
    gc.gridy++;
    buttonPanel.add(btnExport, gc);
    gc.gridy++;
    buttonPanel.add(btnImport, gc);
    gc.gridy++;
    gc.fill = GridBagConstraints.BOTH;
    gc.weighty = 1;
    buttonPanel.add(new JPanel(), gc);

    gc.gridy = 0;
    gc.gridwidth = 2;
    gc.weightx = 1.0;
    gc.weighty = 1.0;
    gc.fill = GridBagConstraints.HORIZONTAL;
    // centerPanel.add(pcomment, gc);

    profileList.setPreferredSize(new Dimension(200, 300));
    JScrollPane psp = new JScrollPane(profileList);
    psp.setMinimumSize(new Dimension(100, 100));
    profileList.setMinimumSize(new Dimension(100, 100));
    setMinimumSize(new Dimension(200, 200));
    gc.gridwidth = 1;
    // gc.gridy++;
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = 1;
    gc.weighty = 0.1;
    centerPanel.add(psp, gc);

    gc.fill = GridBagConstraints.VERTICAL;
    gc.weightx = 0.0;
    gc.weighty = 0.0;
    gc.gridx++;
    centerPanel.add(buttonPanel, gc);

    gc.fill = GridBagConstraints.BOTH;
    gc.weighty = 0.0;
    gc.gridx = 0;
    gc.gridy++;
    gc.gridwidth = 2;
    centerPanel.add(bAlwaysAsk, gc);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    mainPanel.add(pcomment, BorderLayout.NORTH);

    getContentPane().add(mainPanel);

    pack();
    getRootPane().setDefaultButton(btnOK);
  }

  /**
   * Closes the window. Stores profile information if <code>!abort</code>.
   * 
   * @param abort
   */
  protected void quit(boolean abort) {
    this.abort = abort;
    if (!abort) {
      ProfileManager.setProfile(profileList.getSelectedValue());
      ProfileManager.setAlwaysAsk(bAlwaysAsk.isSelected());
    }
    dispose();
  }

  /**
   * Adds a new profile.
   */
  protected void createProfile() {
    String name =
        JOptionPane.showInputDialog(Resources.get("profiledialog.inputdialog.create.message")).strip();
    if (checkNewName(name)) {
      try {
        ProfileManager.add(name, null);
      } catch (ProfileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
      updateList(name);
    }
  }

  /**
   * Copies an existing profile.
   */
  protected void copy() {
    String name = null;
    if (profileList.getSelectedValue() == null)
      return;
    do {
      name = JOptionPane.showInputDialog(Resources.get("profiledialog.inputdialog.copy.message")).strip();
    } while (name.isBlank() || name.equals(profileList.getSelectedValue()));
    if (checkNewName(name)) {
      try {
        ProfileManager.add(name, profileList.getSelectedValue());
      } catch (ProfileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
      updateList(name);
    }
  }

  private boolean checkNewName(String name) {
    if (name == null || name.isBlank() || !name.strip().equals(name)) {
      JOptionPane.showMessageDialog(this, Resources.get("profiledialog.inputdialog.invalid.message", name));
      return false;
    }
    if (profiles.contains(name)) {
      JOptionPane.showMessageDialog(this, Resources.get("profiledialog.inputdialog.exists.message", name));
      return false;
    }
    return true;
  }

  private void updateList(String select) {
    profiles.clear();
    for (String name : ProfileManager.getProfiles()) {
      profiles.addElement(name);
    }
    if (select != null) {
      profileList.setSelectedValue(select, true);
    } else {
      profileList.setSelectedIndex(-1);
    }

  }

  /**
   * Removes a profile.
   */
  protected void remove() {
    if (profiles.size() > 1) {
      String name = profileList.getSelectedValue();
      int remove =
          JOptionPane
              .showConfirmDialog(this, Resources.get("profiledialog.inputdialog.remove.message",
                  ProfileManager.getProfileDirectory(name)));
      if (remove != JOptionPane.CANCEL_OPTION) {
        try {
          ProfileManager.remove(name, remove == JOptionPane.YES_OPTION);
        } catch (ProfileException e) {
          JOptionPane.showMessageDialog(this, e.getMessage());
        }
        updateList(null);
      }
    }
  }

  private void exportProfiles() {
    int choice = JOptionPane.YES_OPTION;
    do {
      JFileChooser fc = new JFileChooser();
      EresseaFileFilter ff;
      fc.addChoosableFileFilter(ff = new EresseaFileFilter(EresseaFileFilter.ZIP_FILTER));
      fc.setFileFilter(ff);
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fc.setDialogType(JFileChooser.SAVE_DIALOG);

      choice = JOptionPane.YES_OPTION;
      if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File targetFile = fc.getSelectedFile();
        if (targetFile.exists()) {
          choice = JOptionPane.showConfirmDialog(this, Resources.get("profiledialog.fileexists", targetFile));
        }

        if (choice == JOptionPane.CANCEL_OPTION) {
          break;
        }
        if (choice == JOptionPane.YES_OPTION) {
          ProfileManager.exportProfiles(targetFile.toPath());
        }
      } else {
        break;
      }

    } while (choice == JOptionPane.NO_OPTION);
  }

  private void importProfiles() {
    String[] options = new String[] { Resources.get("profiledialog.import.from.zip"), Resources.get(
        "profiledialog.import.from.directory"), Resources.get(
            "profiledialog.import.from.search") };
    int response = JOptionPane.showOptionDialog(this, Resources.get("profiledialog.import.modeselection"), null,
        JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    if (response == 0) {
      ProfileSearch.importFromZip(this);
    }
    if (response == 1) {
      ProfileSearch.importFromDir(this);
    }
    if (response == 2) {
      ProfileSearch.importFromSearch(this);
    }
    updateList(profileList.getSelectedValue());
  }

  /**
   * Acquires the profile list from ProfileManager.
   * 
   * @return List of profile names
   */
  private DefaultListModel<String> initProfiles() {
    profiles = new DefaultListModel<String>();
    for (String name : ProfileManager.getProfiles()) {
      profiles.addElement(name);
    }
    return profiles;
  }

  /**
   * @return <code>true</code> if the Okay button was hit.
   */
  public boolean getResult() {
    return !abort;
  }

  @Override
  public void setVisible(boolean b) {
    pack();
    super.setVisible(b);
  }
}
