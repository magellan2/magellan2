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

package magellan.client.swing.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class DetailedPreferencesAdapter extends JPanel implements PreferencesAdapter {
  /** DOCUMENT-ME */
  public static final String TRUE = "true";

  /** DOCUMENT-ME */
  public static final String FALSE = "false";

  /** DOCUMENT-ME */
  public boolean properties[];
  protected JCheckBox boxes[];
  protected JPanel detailContainers[];
  protected String detailHelps[];
  protected String detailTitles[];
  protected String settKeys[][];
  protected String langKeys[];
  protected Properties settings;
  protected String prefix;
  protected int rows = 0;
  protected int count = 0;
  protected int subcount[];

  /**
   * Creates new DetailedPreferencesAdapter
   * 
   * @param count Number of options (top level check boxes)
   * @param subcount Number of sub-options (check boxes in details entry)
   * @param p Properties to store options
   * @param prefix Prefix for properties keys
   * @param sK suffixes of properties keys
   * @param lK suffixes of resource keys
   * @param rows number of rows of boxes
   */
  public DetailedPreferencesAdapter(int count, int subcount[], Properties p, String prefix,
      String sK[][], String lK[], int rows) {
    this(count, subcount, p, prefix, sK, lK, rows, false);
  }

  /**
   * Creates a new DetailedPreferencesAdapter object.
   * 
   * @param count Number of options (top level check boxes)
   * @param subcount Number of sub-options (check boxes in details entry)
   * @param p Properties to store options
   * @param prefix Prefix for properties keys
   * @param sK suffixes of properties keys
   * @param lK suffixes of resource keys
   * @param rows number of rows of boxes
   * @param waitWithInit
   */
  public DetailedPreferencesAdapter(int count, int subcount[], Properties p, String prefix,
      String sK[][], String lK[], int rows, boolean waitWithInit) {
    settings = p;
    this.prefix = prefix;
    settKeys = sK;
    langKeys = lK;
    this.rows = rows;
    this.subcount = subcount;
    this.count = count;

    int sum = 0;

    if (subcount != null) {
      for (int i = 0; i < count; i++) {
        sum += Math.max(0, subcount[i]);
        sum++;
      }
    } else {
      sum = count;
    }

    properties = new boolean[sum];
    boxes = new JCheckBox[sum];

    initProperties();

    if (!waitWithInit) {
      init();
    }
  }

  protected void init() {
    initBoxes();

    detailContainers = new JPanel[count];
    detailTitles = new String[count];
    detailHelps = new String[count];

    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    JPanel help = new JPanel(new GridBagLayout());
    GridBagConstraints con = new GridBagConstraints();
    con.gridwidth = 1;
    con.gridheight = 1;
    con.fill = GridBagConstraints.NONE;
    con.anchor = GridBagConstraints.NORTHWEST;
    con.gridx = 0;
    con.gridy = 0;

    int cBox = 0;
    ActionListener al = new DetailListener(this);
    String bTitle = Resources.get("preferences.detailedpreferencesadapter.button.title");

    for (int i = 0; i < count; i++) {
      help.add(boxes[cBox], con);
      cBox++;

      if ((subcount != null) && (subcount[i] != 0)) { // create detail

        // create the dialog panel
        if (subcount[i] > 0) {
          detailContainers[i] = new JPanel();
          detailContainers[i].setLayout(new BoxLayout(detailContainers[i], BoxLayout.Y_AXIS));

          for (int j = 0; j < subcount[i]; j++) {
            detailContainers[i].add(boxes[cBox + j]);
          }

          cBox += subcount[i];
        } else {
          detailContainers[i] = getExternalDetailContainer(i);
        }

        detailTitles[i] = getString("prefs.dialogs." + String.valueOf(i) + ".title");

        try {
          detailHelps[i] = getString("prefs.dialogs." + String.valueOf(i) + ".help");
        } catch (Exception mexc) {
        }

        // maybe no help available
        // create the button
        JButton button = new JButton(bTitle);
        button.setActionCommand(String.valueOf(i));
        button.addActionListener(al);
        con.gridx++;
        help.add(button, con);
        con.gridx--;
      }

      con.gridy++;

      if ((rows > 0) && (con.gridy >= rows)) {
        con.gridy = 0;
        con.gridx += 2;
      }
    }

    add(help);
  }

  protected JPanel getExternalDetailContainer(int index) {
    return null;
  }

  protected void initProperties() {
    for (int i = 0; i < properties.length; i++) {
      properties[i] =
          settings.getProperty(prefix + "." + settKeys[i][0], settKeys[i][1]).equals(
              DetailedPreferencesAdapter.TRUE);
    }
  }

  protected void initBoxes() {
    for (int i = 0; i < boxes.length; i++) {
      boxes[i] = new JCheckBox(getString(langKeys[i]), properties[i]);

      try {
        boxes[i].setToolTipText(getString(langKeys[i] + ".tooltip"));
      } catch (Exception mexc) {
      }

      // maybe no tooltip
    }
  }

  protected boolean checkBox(int i, String key, String def) {
    if (properties[i] != boxes[i].isSelected()) {
      properties[i] = boxes[i].isSelected();

      if ((properties[i] && def.equals(DetailedPreferencesAdapter.TRUE))
          || (!properties[i] && def.equals(DetailedPreferencesAdapter.FALSE))) {
        settings.remove(prefix + "." + key);
      } else {
        settings.setProperty(prefix + "." + key, properties[i] ? DetailedPreferencesAdapter.TRUE
            : DetailedPreferencesAdapter.FALSE);
      }

      return true;
    }

    return false;
  }

  public void initPreferences() {
    // TODO: implement it
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    List<Integer> l = new LinkedList<Integer>();

    for (int i = 0; i < boxes.length; i++) {
      if (checkBox(i, settKeys[i][0], settKeys[i][1])) {
        l.add(Integer.valueOf(i));
      }
    }

    if (l.size() > 0) {
      int indices[] = new int[l.size()];

      for (int i = 0, max = l.size(); i < max; i++) {
        indices[i] = (l.get(i)).intValue();
      }

      applyChanges(indices);
    }
  }

  protected abstract void applyChanges(int indices[]);

  /**
   * DOCUMENT-ME
   */
  public Component getComponent() {
    return this;
  }

  /**
   * DOCUMENT-ME
   */
  public String getTitle() {
    return getString("prefs.title");
  }

  protected abstract String getString(String key);

  protected class DetailListener implements ActionListener {
    protected JComponent src;

    /**
     * Creates a new DetailListener object.
     */
    public DetailListener(JComponent src) {
      this.src = src;
    }

    /**
     * DOCUMENT-ME
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      int index = -1;

      try {
        index = Integer.parseInt(actionEvent.getActionCommand());
      } catch (Exception exc) {
        return;
      }

      java.awt.Component c = src.getTopLevelAncestor();
      DetailsDialog dialog = null;
      String title = detailTitles[index];

      if (c instanceof java.awt.Frame) {
        dialog = new DetailsDialog((java.awt.Frame) c, title);
      } else if (c instanceof java.awt.Dialog) {
        dialog = new DetailsDialog((java.awt.Dialog) c, title);
      }

      if (dialog != null) {
        dialog.init(detailContainers[index], detailHelps[index]);
        dialog.pack();
        dialog.setLocationRelativeTo(src);
        dialog.setVisible(true);
      }
    }
  }

  protected static class DetailsDialog extends JDialog implements ActionListener {
    /**
     * Creates new DetailsDialog
     */
    public DetailsDialog(Frame parent, String title) {
      super(parent, title, true);
    }

    /**
     * Creates a new DetailsDialog object.
     */
    public DetailsDialog(Dialog parent, String title) {
      super(parent, title, true);
    }

    /**
     * DOCUMENT-ME
     */
    public void init(JComponent adapter, String help) {
      adapter.setBorder(BorderFactory.createTitledBorder(Resources
          .get("preferences.detailedpreferencesadapter.ddialog.options.title")));

      JButton okButton = new JButton(Resources.get("preferences.detailedpreferencesadapter.ok"));
      okButton.addActionListener(this);

      JPanel south = null;

      if (help != null) {
        JTextArea text = new JTextArea(help, 5, 40);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setBackground(getContentPane().getBackground());
        text.setForeground(Color.black); // don't show in disabled color

        JScrollPane pane =
            new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(Resources
            .get("preferences.detailedpreferencesadapter.ddialog.help.title")));

        south = new JPanel(new BorderLayout());
        south.add(pane, BorderLayout.CENTER);

        JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
        button.add(okButton);
        south.add(button, BorderLayout.SOUTH);
      } else {
        south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(okButton);
      }

      getContentPane().add(adapter, BorderLayout.CENTER);
      getContentPane().add(south, BorderLayout.SOUTH);
    }

    /**
     * DOCUMENT-ME
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
