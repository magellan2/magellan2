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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import magellan.client.event.EventDispatcher;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.utils.SwingUtils;
import magellan.library.Region;
import magellan.library.Sign;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;

/**
 * A dialog for adding a sign to a region
 * 
 * @author Fiete
 */
public class AddSignDialog extends InternationalizedDialog {
  private Properties settings = null;
  private EventDispatcher dispatcher = null;
  private JTextField Line1 = null;
  private JTextField Line2 = null;
  private Region region = null;

  /**
   * Create a new dialog.
   */
  public AddSignDialog(Frame owner, boolean modal, Properties p, EventDispatcher dispatcher,
      Region r) {
    super(owner, modal);
    settings = p;
    region = r;
    this.dispatcher = dispatcher;
    init();
  }

  private void init() {
    setContentPane(getMainPane());
    setTitle(Resources.get("addsigndialog.window.title"));

    pack();
    SwingUtils.setLocation(this, settings, "AddSign.x", "AddSign.y");
  }

  private Container getMainPane() {
    SpringLayout layout = new SpringLayout();
    JPanel aPanel = new JPanel(layout);
    aPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

    JLabel label1 = new JLabel(Resources.get("addsigndialog.label.line1"));
    Line1 = new JTextField(30);
    aPanel.add(label1);
    aPanel.add(Line1);

    JLabel label2 = new JLabel(Resources.get("addsigndialog.label.line2"));
    Line2 = new JTextField(30);
    aPanel.add(label2);
    aPanel.add(Line2);
    // Lay out the panel.
    SpringUtilities.makeCompactGrid(aPanel, 2, 2, // rows, cols
        6, 6, // initX, initY
        6, 6); // xPad, yPad

    JButton okButton = new JButton(Resources.get("addsigndialog.btn.ok.caption"));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addSign();
      }
    });

    JButton cancelButton = new JButton(Resources.get("addsigndialog.btn.close.caption"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout());

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(aPanel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    return mainPanel;
  }

  /**
   * Stores all properties of AddSign that should be preserved to the global Properties object.
   */
  private void storeSettings() {
    settings.setProperty("AddSign.width", getWidth() + "");
    settings.setProperty("AddSign.height", getHeight() + "");
    settings.setProperty("AddSign.x", getX() + "");
    settings.setProperty("AddSign.y", getY() + "");
  }

  /**
   * going to make the change
   */
  private void addSign() {
    String s1 = Line1.getText();
    String s2 = Line2.getText();

    if (s1 != null && s1.length() > 0) {
      region.addSign(new Sign(s1));
    }
    if (s2 != null && s2.length() > 0) {
      region.addSign(new Sign(s2));
    }
    // TODO this should be a smaller scale event... Only the map should be interested...
    dispatcher.fire(new GameDataEvent(this, region.getData(), false));
    quit();
  }

  /**
   * stores position and exit
   */
  @Override
  protected void quit() {
    storeSettings();
    dispose();
  }

}
