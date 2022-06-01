// class magellan.client.swing.AskForPasswordDialog
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
package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import magellan.client.Client;
import magellan.client.utils.SwingUtils;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.TrustLevel;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;

/**
 * This class is called by the Client if there is a report with factions but without a passwort for
 * one of them. This dialog presents all available factions and a password field. If you press
 * Cancel, none password will be set.
 *
 * @author Thoralf Rickert
 * @version 1.0, 15.02.2008
 */
public class AskForPasswordDialog extends JDialog implements ActionListener {

  private JComboBox<FactionItem> factionBox = null;
  private JPasswordField passwordField = null;
  private Client client;
  private GameData data;

  public AskForPasswordDialog(Client client, GameData data) {
    super(client);
    this.client = client;
    this.data = data;
    init();
  }

  private void init() {
    setTitle(Resources.get("client.msg.askforpassword.title"));
    setSize(400, 260);
    setResizable(false);

    SwingUtils.center(this);

    JPanel panel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JPanel mainPanel = new JPanel(new GridBagLayout());

    JButton okButton = new JButton(Resources.get("button.ok"));
    okButton.setActionCommand("button.ok");
    okButton.addActionListener(this);
    JButton cancelButton = new JButton(Resources.get("button.cancel"));
    cancelButton.setActionCommand("button.cancel");
    cancelButton.addActionListener(this);

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    JTextArea comment1 = new JTextArea(Resources.get("client.msg.askforpassword.comment1"));
    comment1.setEditable(false);
    comment1.setSelectionColor(comment1.getBackground());
    comment1.setSelectedTextColor(comment1.getForeground());
    comment1.setRequestFocusEnabled(false);
    comment1.setBackground(getContentPane().getBackground());
    comment1.setSelectionColor(getContentPane().getBackground());
    comment1.setSelectedTextColor(getContentPane().getForeground());
    comment1.setFont(okButton.getFont());
    comment1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.0;
    mainPanel.add(comment1, c);

    JLabel label = new JLabel(Resources.get("client.msg.askforpassword.faction"));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    mainPanel.add(label, c);

    // Vector<FactionItem> items = new Vector<FactionItem>();
    Vector<FactionItem> items = getProbablyPriviligedFactionItems();
    if (items == null) {
      items = getAllFactionItems();
    }
    if (items.size() > 1) {
      // add only a "plz select" if we have more than one candidate
      items.add(new FactionItem(null));
    }

    // FactionItem first = items.get(1);
    Collections.sort(items, new FactionItemComparator());
    /*
     * (what for?) if (first!=null) items.add(1, first);
     */

    factionBox = new JComboBox<FactionItem>(items);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    mainPanel.add(factionBox, c);

    label = new JLabel(Resources.get("client.msg.askforpassword.password"));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    mainPanel.add(label, c);

    passwordField = new JPasswordField();
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    mainPanel.add(passwordField, c);

    panel.add(mainPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    getContentPane().add(panel);
    pack();
    factionBox.requestFocusInWindow();
    getRootPane().setDefaultButton(okButton);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand() == null)
      return;
    if (e.getActionCommand().equals("button.cancel")) {
      setVisible(false);
    } else if (e.getActionCommand().equals("button.ok")) {
      FactionItem item = (FactionItem) factionBox.getSelectedItem();
      Faction faction = item.getFaction();
      String password = new String(passwordField.getPassword());
      if (faction != null && password.length() != 0) {
        faction.setPassword(password);
        if (!faction.isTrustLevelSetByUser()) {
          faction.setTrustLevel(TrustLevel.TL_PRIVILEGED);
        }

        if (client.getProperties() != null) {
          client.getProperties().setProperty("Faction.password." + (faction.getID()).intValue(),
              faction.getPassword());
        }
        client.getDispatcher().fire(new GameDataEvent(this, data, true));
      }
      TrustLevels.recalculateTrustLevels(data);
      setVisible(false);
    }

  }

  /**
   * Is a container for a faction for the combobox.
   */
  static class FactionItem {
    Faction faction = null;

    public FactionItem(Faction f) {
      faction = f;
    }

    public Faction getFaction() {
      return faction;
    }

    @Override
    public String toString() {
      if (faction == null)
        return Resources.get("client.msg.askforpassword.pleasechoose");
      return faction.getName() + "(" + faction.getID() + ")";
    }
  }

  /**
   * Used to sort the FactionsItems
   */
  static class FactionItemComparator implements Comparator<FactionItem> {

    public int compare(FactionItem o1, FactionItem o2) {
      if (o1.getFaction() == null)
        return Integer.MIN_VALUE;
      if (o2.getFaction() == null)
        return Integer.MAX_VALUE;
      return o1.toString().compareTo(o2.toString());
    }

  }

  /**
   * tries to find some hints if a faction is "owned" by the user...and possible a password may make
   * sense... uses battle-status to identify such factions (-1 by default)
   *
   * @param f
   * @param data
   * @return
   */
  private boolean isProbablyPriviligedFaction(Faction f) {
    for (Unit u : data.getUnits()) {
      if (u.getFaction().equals(f) && u.isDetailsKnown())
        return true;
    }
    return false;
  }

  /**
   * Builds a list with factions with </code>isProbablyPriviligedFaction=true</code>
   */
  private Vector<FactionItem> getProbablyPriviligedFactionItems() {
    Vector<FactionItem> erg = null;
    for (Faction f : data.getFactions()) {
      if (isProbablyPriviligedFaction(f)) {
        if (erg == null) {
          erg = new Vector<FactionItem>();
        }
        erg.add(new FactionItem(f));
      }
    }
    return erg;
  }

  /**
   * Builds a list with factions with </code>isProbablyPriviligedFaction=true</code>
   *
   * @return
   */
  private Vector<FactionItem> getAllFactionItems() {
    Vector<FactionItem> erg = null;
    for (Faction f : data.getFactions()) {
      if (erg == null) {
        erg = new Vector<FactionItem>();
      }
      erg.add(new FactionItem(f));
    }
    return erg;
  }

}
