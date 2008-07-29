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

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A Dialog that asks the user for a string input. Returns all orders containing
 * or beginning with that string.
 * 
 * @author stm
 */
public class RemoveOrderDialog extends InternationalizedDialog {
  private static final Logger log = Logger.getInstance(RemoveOrderDialog.class);

  public static final String BEGIN_ACTION = "begins";
  public static final String CONTAINS_ACTION = "contains";

  private ButtonGroup position;
  private JTextArea order;
  private JButton ok;
  private JButton cancel;

  private JCheckBox caseBox;

  /**
   * Creates a new removeOrderDialog object.
   */
  public RemoveOrderDialog(Frame owner, String caption) {
    super(owner, true);
    setTitle(Resources.get("removeorderdialog.window.title"));

    Container cp = getContentPane();
    cp.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(3, 3, 3, 3), 0, 0);

    c.gridwidth=2;
    JLabel captionLabel = new JLabel(caption);
    captionLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
    captionLabel.setHorizontalAlignment(SwingConstants.CENTER);
    captionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    cp.add(captionLabel, c);

    c.gridwidth=1;

    JRadioButton beginButton = new JRadioButton(Resources.get("removeorderdialog.radio.begins.title"));
    beginButton.setActionCommand(RemoveOrderDialog.BEGIN_ACTION);
    JRadioButton containsButton = new JRadioButton(Resources.get("removeorderdialog.radio.contains.title"));
    containsButton.setActionCommand(RemoveOrderDialog.CONTAINS_ACTION);
    position = new ButtonGroup();
    position.add(beginButton);
    position.add(containsButton);
    position.setSelected(beginButton.getModel(), true);
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    c.anchor = GridBagConstraints.WEST;
    cp.add(beginButton, c);
    c.gridx = 1;
    c.weightx = 0;
    c.anchor = GridBagConstraints.EAST;
    cp.add(containsButton, c);

    c.gridx = 0;
    c.gridy++;

    cp.add(new JLabel(Resources.get("removeorderdialog.window.message")), c);

    order = new JTextArea(1, 25);

    JScrollPane helperPane = new JScrollPane(order);

    c.gridx = 1;
    c.weightx = 0.5;
    cp.add(helperPane, c);

    caseBox = new JCheckBox(Resources.get("removeorderdialog.chkbox.matchcase.title"));
    caseBox.setSelected(true);
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    cp.add(caseBox, c);

    ok = new JButton(Resources.get("removeorderdialog.btn.ok.caption"));
    ok.setMnemonic(Resources.get("removeorderdialog.btn.ok.mnemonic").charAt(0));

    // actionListener is added in the show() method
    c.gridy++;
    c.anchor = GridBagConstraints.EAST;
    cp.add(ok, c);

    cancel = new JButton(Resources.get("removeorderdialog.btn.cancel.caption"));
    cancel.setMnemonic(Resources.get("removeorderdialog.btn.cancel.mnemonic").charAt(0));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    cp.add(cancel, c);
  }

  /**
   * Shows the dialog.
   * 
   * @return A string array with the following values: <br/> [0] : The order
   *         fragment that was given <br/> [1] : One of
   *         {@link RemoveOrderDialog#BEGIN_ACTION},
   *         {@link RemoveOrderDialog#CONTAINS_ACTION} <br/> [2] : "true" if
   *         case should not be ignored
   */
  public String[] showDialog() {
    final String retVal[] = new String[3];
    ActionListener okButtonAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        retVal[0] = RemoveOrderDialog.this.order.getText();
        retVal[1] = String.valueOf(position.getSelection().getActionCommand());
        retVal[2] = String.valueOf(caseBox.isSelected());
        quit();
      }
    };

    ok.addActionListener(okButtonAction);
    // order.addActionListener(okButtonAction);
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);

    return retVal;
  }
}
