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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.event.EventDispatcher;
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 324 $
 */
public class FactionPropertiesDialog extends InternationalizedDataDialog {
  private JTextField txtPassword;
  private Faction faction;
  private JButton btnOK;
  private JButton btnCancel;
  private boolean approved = false;
  private JCheckBox chkOwner;
  private JList translationList;
  private JTextField tx;
  private JTextField ty;
  private JTextField tz;

  /**
   * Create a new dialog for faction properties for the specified faction.
   * 
   * @param owner
   * @param modal
   * @param dispatcher
   * @param d
   * @param p
   * @param f
   */
  public FactionPropertiesDialog(Frame owner, boolean modal, EventDispatcher dispatcher,
      GameData d, Properties p, Faction f) {
    super(owner, modal, dispatcher, d, p);
    // unnecessary
    // dispatcher.addGameDataListener(this);

    data = d;
    settings = p;
    this.dispatcher = dispatcher;
    faction = f;

    setTitle(Resources.getFormatted("factionpropertiesdialog.window.title", f));
    setContentPane(getMainPane());
    setSize(420, 500);

    pack();
    SwingUtils.center(this);
  }

  /**
   * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    data = e.getGameData();
  }

  private JPanel getMainPane() {
    JPanel main = new JPanel();
    main.setLayout(new GridBagLayout());
    main.setBorder(new EmptyBorder(4, 4, 4, 4));

    btnOK = new javax.swing.JButton();
    btnCancel = new javax.swing.JButton();

    btnOK.setText(Resources.get("button.ok"));
    btnOK.setMnemonic(Resources.get("button.ok.mnemonic").charAt(0));
    btnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnOKActionPerformed(evt);
      }
    });

    btnCancel.setText(Resources.get("button.cancel"));
    btnCancel.setMnemonic(Resources.get("button.cancel.mnemonic").charAt(0));
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCancelActionPerformed(evt);
      }
    });

    txtPassword = new JTextField(faction.getPassword());
    txtPassword.setEditable(true);
    txtPassword.setCursor(new Cursor(Cursor.TEXT_CURSOR));
    txtPassword.setPreferredSize(new Dimension(100, 25));
    txtPassword.setToolTipText(Resources.get("factionpropertiesdialog.password.tooltip"));

    JLabel l = new JLabel(Resources.get("factionpropertiesdialog.password.label"));
    // l.setDisplayedMnemonic(Resources.get("finddialog.lbl.pattern.mnemonic").charAt(0));
    l.setLabelFor(txtPassword);
    l.setToolTipText(Resources.get("factionpropertiesdialog.password.tooltip"));

    JPanel pnlPassword = new JPanel(new BorderLayout());
    pnlPassword.setBorder(new EmptyBorder(4, 4, 4, 4));
    pnlPassword.add(l, BorderLayout.WEST);
    pnlPassword.add(txtPassword, BorderLayout.CENTER);

    chkOwner = new JCheckBox(Resources.get("factionpropertiesdialog.owner.label"));
    chkOwner.setSelected(faction.getID().equals(getData().getOwnerFaction()));
    chkOwner.setToolTipText(Resources.get("factionpropertiesdialog.owner.tooltip"));

    JPanel translationPanel = new JPanel();

    translationPanel.setLayout(new GridBagLayout());
    translationPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("factionpropertiesdialog.translations.title")));
    translationPanel.setToolTipText(Resources.get("factionpropertiesdialog.translations.tooltip"));

    tx = new JTextField();
    tx.setPreferredSize(new java.awt.Dimension(55, 20));
    tx.setMinimumSize(new java.awt.Dimension(50, 20));
    JLabel lx = new JLabel("x: ");
    lx.setLabelFor(tx);

    ty = new JTextField();
    ty.setPreferredSize(new java.awt.Dimension(55, 20));
    ty.setMinimumSize(new java.awt.Dimension(50, 20));
    JLabel ly = new JLabel("y: ");
    ly.setLabelFor(ty);

    tz = new JTextField();
    tz.setPreferredSize(new java.awt.Dimension(55, 20));
    tz.setMinimumSize(new java.awt.Dimension(50, 20));
    JLabel lz = new JLabel("z: ");
    lz.setLabelFor(tz);

    JButton addButton = new JButton(Resources.get("factionpropertiesdialog.button.add"));
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CoordinateID translation = getTranslation();
        if (translation == null)
          return;
        DefaultListModel model = (DefaultListModel) translationList.getModel();
        int pos = -1;
        for (int i = 0; i < model.size(); ++i) {
          CoordinateID oldTranslation = (CoordinateID) model.get(i);
          if (oldTranslation.getZ() == translation.getZ()) {
            pos = i;
            break;
          }
        }
        if (pos >= 0) {
          model.remove(pos);
          model.add(pos, translation);
        } else {
          model.add(0, translation);
        }
      }
    });

    translationList = new JList();
    translationList
        .setToolTipText(Resources.get("factionpropertiesdialog.translationlist.tooltip"));
    translationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane pane = new JScrollPane(translationList);

    DefaultListModel model = new DefaultListModel();

    if (getData().getCoordinateTranslationMap(faction.getID()) != null) {
      for (CoordinateID translation : getData().getCoordinateTranslationMap(faction.getID())
          .values()) {
        model.addElement(translation);
      }
    }
    translationList.setModel(model);

    GridBagConstraints cc = new GridBagConstraints();
    cc.anchor = GridBagConstraints.CENTER;
    cc.gridx = 0;
    cc.gridy = 0;
    cc.gridwidth = 1;
    cc.gridheight = 1;
    cc.fill = GridBagConstraints.HORIZONTAL;
    cc.weightx = 0.0;
    cc.weighty = 0.0;
    cc.insets = new Insets(5, 5, 5, 5);
    translationPanel.add(lx, cc);
    cc.gridx++;
    cc.weightx = .5;
    translationPanel.add(tx, cc);
    cc.gridx++;
    translationPanel.add(ly, cc);
    cc.gridx++;
    translationPanel.add(ty, cc);
    cc.gridx++;
    translationPanel.add(lz, cc);
    cc.gridx++;
    translationPanel.add(tz, cc);

    cc.gridx = 0;
    cc.gridy++;
    cc.gridwidth = 6;
    translationPanel.add(addButton, cc);

    cc.gridx = 0;
    cc.gridy++;
    cc.gridwidth = 6;
    translationPanel.add(pane, cc);

    JPanel addTranslationPanel = new JPanel();
    addTranslationPanel.setLayout(new BorderLayout(0, 0));

    JButton removeButton = new JButton(Resources.get("factionpropertiesdialog.button.remove"));
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int pos = translationList.getSelectedIndex();
        if (pos == -1)
          return;
        DefaultListModel listModel = getModel();
        listModel.remove(pos);
      }
    });

    cc.gridx = 0;
    cc.gridy++;
    translationPanel.add(removeButton, cc);

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;

    main.add(chkOwner, c);

    c.gridy++;
    main.add(pnlPassword, c);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    c.gridheight = 4;
    c.insets = new java.awt.Insets(0, 0, 5, 5);
    main.add(translationPanel, c);

    c.gridx = 0;
    c.gridy += 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.insets = new java.awt.Insets(0, 0, 5, 5);
    main.add(btnOK, c);

    c.gridx = 1;
    // c.gridy = 2;
    c.insets = new java.awt.Insets(0, 0, 5, 5);
    main.add(btnCancel, c);

    getRootPane().setDefaultButton(btnOK);

    return main;
  }

  private DefaultListModel getModel() {
    return (DefaultListModel) translationList.getModel();
  }

  protected CoordinateID getTranslation() {
    CoordinateID result = null;
    try {
      result =
          CoordinateID.create(Integer.parseInt(tx.getText()), Integer.parseInt(ty.getText()),
              Integer.parseInt(tz.getText()));
    } catch (NumberFormatException e) {
      return null;
    }
    return result;
  }

  private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
    approved = true;

    setVisible(false);
    dispose();
  }

  private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
    quit();
    approved = false;
  }

  public boolean approved() {
    return approved;
  }

  public String getPassword() {
    if (approved)
      return txtPassword.getText();
    else
      return null;
  }

  public Collection<CoordinateID> getTranslations() {
    Collection<CoordinateID> result = new ArrayList<CoordinateID>(getModel().size());

    for (Enumeration<?> elements = getModel().elements(); elements.hasMoreElements();) {
      result.add((CoordinateID) elements.nextElement());
    }
    return result;
  }

  public boolean isOwner() {
    return chkOwner.isSelected();
  }

}
