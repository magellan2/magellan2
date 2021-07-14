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

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

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
  private static final Logger log = Logger.getInstance(ProfileDialog.class);

  private boolean abort = true;

  private DefaultListModel profiles;

  private JList profileList;

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

    SwingUtils.center(this);
  }

  private void initGUI() {
    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());

    setModal(true);

    GridBagConstraints gc =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 1, 1);

    Component comment = WrappableLabel.getLabel(Resources.get("profiledialog.explanation"));
    comment.setFont(new JLabel().getFont());

    profiles = initProfiles();
    profileList = new JList(profiles);
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

    gc.fill = GridBagConstraints.HORIZONTAL;
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

    gc.gridy = 0;
    gc.gridwidth = 2;
    gc.weightx = 1.0;
    gc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(comment, gc);
    gc.gridwidth = 1;
    gc.gridy++;
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = 1.0;
    mainPanel.add(new JScrollPane(profileList), gc);
    gc.fill = GridBagConstraints.NONE;
    gc.weightx = 0.0;
    gc.gridx++;
    mainPanel.add(buttonPanel, gc);
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weighty = 1.0;
    gc.gridx = 0;
    gc.gridy++;
    gc.gridwidth = 2;
    mainPanel.add(bAlwaysAsk, gc);

    getContentPane().add(mainPanel);

    pack();
  }

  /**
   * Closes the window. Stores profile information if <code>!abort</code>.
   * 
   * @param abort
   */
  protected void quit(boolean abort) {
    this.abort = abort;
    if (!abort) {
      ProfileManager.setProfile((String) profileList.getSelectedValue());
      ProfileManager.setAlwaysAsk(bAlwaysAsk.isSelected());
    }
    dispose();
  }

  /**
   * Adds a new profile.
   */
  protected void createProfile() {
    String name =
        JOptionPane.showInputDialog(Resources.get("profiledialog.inputdialog.create.message"));
    if (name != null) {
      try {
        ProfileManager.add(name, null);
        profiles.addElement(name);
        profileList.setSelectedValue(name, true);
      } catch (ProfileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
  }

  /**
   * Copies an existing profile.
   */
  protected void copy() {
    String name =
        JOptionPane.showInputDialog(Resources.get("profiledialog.inputdialog.copy.message"));
    if (name != null) {
      try {
        ProfileManager.add(name, (String) profileList.getSelectedValue());
        profiles.addElement(name);
        profileList.setSelectedValue(name, true);
      } catch (ProfileException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }

  }

  /**
   * Removes a profile.
   */
  protected void remove() {
    if (profiles.size() > 1) {
      String name = (String) profileList.getSelectedValue();
      int remove =
          JOptionPane
              .showConfirmDialog(this, Resources.get("profiledialog.inputdialog.remove.message",
                  ProfileManager.getProfileDirectory(name)));
      if (remove != JOptionPane.CANCEL_OPTION)
        if (ProfileManager.remove(name, remove == JOptionPane.YES_OPTION)) {
          profiles.removeElement(profileList.getSelectedValue());
          profileList.setSelectedIndex(0);
        }
    }
  }

  /**
   * Acquires the profile list from ProfileManager.
   * 
   * @return List of profile names
   */
  private DefaultListModel initProfiles() {
    profiles = new DefaultListModel();
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

}
