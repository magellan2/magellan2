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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import magellan.client.swing.layout.WrappableLabel;
import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.Resources;
import magellan.library.utils.transformation.BoxTransformer.BBox;
import magellan.library.utils.transformation.BoxTransformer.BBoxes;

/**
 * Dialog for asking about world dimensions.
 * 
 * @author stm
 */
public class SetGirthDialog extends JDialog {
  /**
   * if true, SetOriginAction initiate client.setOrigin
   */
  private boolean approved = false;

  // private javax.swing.JButton btnOK;
  // private javax.swing.JButton btnCancel;
  private JTextField editXmin;
  private JTextField editXmax;
  private JTextField editYmin;
  private JTextField editYmax;
  private JTextField editLevel;

  /**
   * new dimensions, entered eventually by the user
   */
  private BBoxes newBoxes;

  private Color defaultColor;

  private Integer level;

  private BBox defaultBox;

  private MapMetric metric;

  /**
   * Creates new form.
   * 
   * @param parent the <code>Frame</code> from which the dialog is displayed
   * @param preset pre-set value of the input fields; if <code>null</code> there will be empty input
   *          fields
   * @param level if this is <code>null</code>, the level input will be editable, otherwise it will
   *          be fixed to this value
   * @param metric
   */
  public SetGirthDialog(java.awt.Frame parent, BBox preset, Integer level, MapMetric metric) {
    super(parent, true);
    approved = false;
    this.metric = metric;
    newBoxes = new BBoxes(metric);
    if (preset != null) {
      defaultBox = metric.createBBox();
      defaultBox.setX(preset.getMinx(), preset.getMaxx());
      defaultBox.setY(preset.getMiny(), preset.getMaxy());
    }
    this.level = level;
    initComponents();
    pack();
    centerWindow();
  }

  private void initComponents() {
    getContentPane().setLayout(new java.awt.GridBagLayout());
    setTitle(Resources.get("setgirthdialog.window.title"));

    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt) {
        dispose();
        // quit();
      }
    });

    JButton btnOK = new JButton();
    JButton btnCancel = new JButton();
    JPanel coordPanel = new JPanel();

    btnOK.setText(Resources.get("setgirthdialog.btn.ok.caption"));
    btnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnOKActionPerformed(evt);
      }
    });

    btnCancel.setText(Resources.get("setgirthdialog.btn.cancel.caption"));
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCancelActionPerformed(evt);
      }
    });

    GridBagConstraints gc = new java.awt.GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.FIRST_LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(3, 1, 3, 1), 0, 0);
    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridwidth = 2;
    // gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weightx = 1.0;
    gc.weighty = 1.0;
    gc.anchor = GridBagConstraints.NORTH;
    getContentPane().add(coordPanel, gc);

    gc.gridwidth = 2;
    gc.gridx = 0;
    gc.gridy++;
    // gc.fill = GridBagConstraints.BOTH;
    gc.weighty = 1;
    getContentPane().add(new JPanel());

    gc.gridwidth = 1;
    gc.gridx = 0;
    gc.gridy++;
    gc.weighty = 0;
    // gc.insets = new java.awt.Insets(0, 0, 5, 5);
    getContentPane().add(btnOK, gc);

    gc.gridx = 1;
    // gc.insets = new java.awt.Insets(0, 0, 5, 5);
    getContentPane().add(btnCancel, gc);

    coordPanel.setLayout(new java.awt.GridBagLayout());

    gc =
        new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.NONE, new Insets(3, 1, 3, 1), 0, 0);
    // new java.awt.GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
    // GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 4, 4);

    Component xmessage = WrappableLabel.getLabel(Resources.get("setgirthdialog.lbl.msgx.caption"));
    JLabel xminLabel = new JLabel(Resources.get("setgirthdialog.lbl.xmin.caption") + ":");
    JLabel xmaxLabel = new JLabel(Resources.get("setgirthdialog.lbl.xmax.caption") + ":");
    Component ymessage = WrappableLabel.getLabel(Resources.get("setgirthdialog.lbl.msgy.caption"));
    JLabel yminLabel = new JLabel(Resources.get("setgirthdialog.lbl.ymin.caption") + ":");
    JLabel ymaxLabel = new JLabel(Resources.get("setgirthdialog.lbl.ymax.caption") + ":");
    JLabel levelLabel = new JLabel(Resources.get("setgirthdialog.lbl.level.caption") + ":");

    editXmin = new JTextField(3);
    editXmax = new JTextField(3);
    editYmin = new JTextField(3);
    editYmax = new JTextField(3);
    editLevel = new JTextField(3);
    defaultColor = editLevel.getBackground();

    if (level != null) {
      editLevel.setEditable(false);
      editLevel.setText(String.valueOf(level));
    } else {
      editLevel.setText("0");
    }
    if (defaultBox != null) {
      editXmin.setText(String.valueOf(defaultBox.getMinx()));
      editXmax.setText(String.valueOf(defaultBox.getMaxx()));
      editYmin.setText(String.valueOf(defaultBox.getMiny()));
      editYmax.setText(String.valueOf(defaultBox.getMaxy()));
    }

    gc.gridwidth = 4;
    gc.anchor = GridBagConstraints.NORTHEAST;
    gc.fill = GridBagConstraints.BOTH;
    gc.gridx = 0;
    gc.gridy = 0;
    gc.gridx = 0;
    gc.gridy++;
    coordPanel.add(levelLabel, gc);
    gc.gridx = 1;
    coordPanel.add(editLevel, gc);

    gc.gridx = 0;
    gc.gridy++;
    coordPanel.add(xmessage, gc);
    gc.gridwidth = 1;
    gc.gridy++;
    gc.gridx = 0;
    coordPanel.add(xminLabel, gc);
    gc.gridx++;
    coordPanel.add(editXmin, gc);
    gc.gridx++;
    coordPanel.add(xmaxLabel, gc);
    gc.gridx++;
    coordPanel.add(editXmax, gc);

    gc.gridwidth = 4;
    gc.gridx = 0;
    gc.gridy++;
    coordPanel.add(ymessage, gc);
    gc.gridwidth = 1;
    gc.gridy++;
    coordPanel.add(yminLabel, gc);
    gc.gridx++;
    coordPanel.add(editYmin, gc);
    gc.gridx++;
    coordPanel.add(ymaxLabel, gc);
    gc.gridx++;
    coordPanel.add(editYmax, gc);

    pack();
    Dimension d = getSize();
    d.height = d.height * 2;
    d.width = 400;
    setPreferredSize(d);

    getRootPane().setDefaultButton(btnOK);
  }

  /**
   * This method sets the window dimension and positions the window to the center of the screen.
   */

  public void centerWindow() {
    int xSize = (int) getBounds().getWidth();
    int ySize = (int) getBounds().getHeight();
    if (xSize > 0 && ySize > 0) {
      int x = getToolkit().getScreenSize().width;
      int y = getToolkit().getScreenSize().height;
      setLocation(new Point((x / 2 - xSize / 2), (y / 2 - ySize / 2)));
    }
  }

  private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
    // reset colors
    editXmin.setBackground(defaultColor);
    editXmax.setBackground(defaultColor);
    editYmin.setBackground(defaultColor);
    editYmax.setBackground(defaultColor);
    editLevel.setBackground(defaultColor);

    // parse coordinates: empty field = MAX resp. MIN value
    int xmin = 0;
    try {
      if (editXmin.getText().length() == 0) {
        xmin = Integer.MAX_VALUE;
      } else {
        xmin = parse(editXmin);
      }
    } catch (NumberFormatException e) {
      // do not close dialog on error
      return;
    }
    int xmax = 0;
    try {
      if (editXmax.getText().length() == 0) {
        xmax = Integer.MIN_VALUE;
      } else {
        xmax = parse(editXmax);
      }
    } catch (NumberFormatException e) {
      return;
    }
    int ymin = 0;
    try {
      if (editYmin.getText().length() == 0) {
        ymin = Integer.MAX_VALUE;
      } else {
        ymin = parse(editYmin);
      }
    } catch (NumberFormatException e) {
      return;
    }
    int ymax = 0;
    try {
      if (editYmax.getText().length() == 0) {
        ymax = Integer.MIN_VALUE;
      } else {
        ymax = parse(editYmax);
      }
    } catch (NumberFormatException e) {
      return;
    }
    int layer;
    try {
      layer = parse(editLevel);
    } catch (NumberFormatException e) {
      return;
    }

    BBox box = metric.createBBox();
    box.setX(xmin, xmax);
    box.setY(ymin, ymax);

    // do not allow xmin=something, xmax = nothing
    if ((xmin != Integer.MAX_VALUE && xmax == Integer.MIN_VALUE)
        || (xmin == Integer.MAX_VALUE && xmax != Integer.MIN_VALUE)) {
      editXmin.requestFocusInWindow();
      editXmin.setBackground(Color.RED);
      return;
    }
    if ((ymin != Integer.MAX_VALUE && ymax == Integer.MIN_VALUE)
        || (ymin == Integer.MAX_VALUE && ymax != Integer.MIN_VALUE)) {
      editYmin.requestFocusInWindow();
      editYmin.setBackground(Color.RED);
      return;
    }

    // all's well
    approved = true;
    newBoxes.setBox(layer, box);
    setVisible(false);
    dispose();
  }

  /**
   * parse text field, mark red on error.
   */
  private int parse(JTextField edit) throws NumberFormatException {
    int result;
    try {
      result = Integer.parseInt(edit.getText());
    } catch (NumberFormatException e) {
      edit.requestFocusInWindow();
      edit.setBackground(Color.RED);
      throw e;
    }
    return result;
  }

  private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
    // quit();
    dispose();

    approved = false;
  }

  /**
   * Returns if user has pressed the OK-Button.
   * 
   * @return true if OK-Button has been pressed
   */
  public boolean approved() {
    return approved;
  }

  /**
   * Return the selected bounding boxes. These have the usual semantics (minimum and maximum x and y
   * values of the report, {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE} for undefined max
   * or min values) with one exception: If xmin and xmax value are identical, these values shall be
   * unchanged in the transformed report.
   * 
   * @return The coordinates of the new origin
   */
  public BBoxes getNewBorders() {
    return newBoxes;
  }
}
