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

package magellan.client.preferences;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This factory makes it possible to configure the layout of icons in the region overview and so on.
 * 
 * @author Andreas
 * @version 1.0
 */
public class IconPreferences extends AbstractPreferencesAdapter implements ExtendedPreferencesAdapter {
  protected List<NodeWrapperFactory> nwfactorys;
  protected List<PreferencesAdapter> nwadapters;
  protected IconStyleSetPreferences styles;
  protected IconColorMappingPreferences cmapping;
  protected EmphasizeStyle eStyle;
  protected JCheckBox toolTipOn;
  protected CardLayout cards;
  protected List<PreferencesAdapter> subAdapters;
  private JComponent content;

  /**
   * Creates new IconAdapter
   */
  public IconPreferences(List<NodeWrapperFactory> nw) {
    subAdapters = new ArrayList<PreferencesAdapter>(2);
    nwfactorys = nw;
    nwadapters = new ArrayList<PreferencesAdapter>(nw.size());

    content = addPanel(null, new GridBagLayout());

    GridBagConstraints con = new GridBagConstraints();
    con.fill = GridBagConstraints.NONE;
    con.anchor = GridBagConstraints.WEST;
    con.gridwidth = 1;
    con.gridheight = 1;
    con.gridx = 0;
    con.gridy = 0;
    toolTipOn =
        new JCheckBox(Resources.get("tree.iconadapter.tooltips.show.text"), CellRenderer
            .isShowTooltips());
    content.add(toolTipOn, con);
    con.gridx = 1;

    Insets old = con.insets;

    if (con.insets == null) {
      con.insets = new Insets(1, 1, 1, 1);
    } else {
      con.insets = new Insets(con.insets.top, con.insets.left, con.insets.bottom, con.insets.right);
    }

    con.insets.left += 20;
    eStyle = new EmphasizeStyle();
    content.add(eStyle, con);
    con.insets = old;
    ++con.gridx;
    con.weightx = 1.0;
    con.fill = GridBagConstraints.HORIZONTAL;
    content.add(new JPanel(), con);
    con.gridx = 0;

    Iterator<NodeWrapperFactory> it = nw.iterator();

    while (it.hasNext()) {
      PreferencesAdapter pref = ((PreferencesFactory) it.next()).createPreferencesAdapter();
      nwadapters.add(pref);

      JComponent comp = null;
      Component c = pref.getComponent();

      if (c instanceof JComponent) {
        comp = (JComponent) c;
      } else {
        comp = new JPanel(new GridLayout(1, 1));
        comp.add(c);
      }

      JPanel p = addPanel(pref.getTitle(), new GridLayout(1, 1));

      p.add(comp);
    }

    styles = new IconStyleSetPreferences();

    cmapping = new IconColorMappingPreferences();
    subAdapters.add(cmapping);
    subAdapters.add(styles);
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("tree.iconadapter.iconadapter.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    styles.updatePreferences();
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // FIXME: implement it
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    // apply node changes
    Iterator<PreferencesAdapter> it = nwadapters.iterator();

    while (it.hasNext()) {
      it.next().applyPreferences();
    }

    CellRenderer.setAdditionalValueProperties(CellRenderer.colorMap, toolTipOn.isSelected());
    eStyle.apply();
  }

  /**
   * @author ...
   * @version 1.0, 16.02.2008
   */
  protected static class EmphasizeStyle extends JPanel implements ActionListener {
    protected JCheckBox bold;
    protected JCheckBox italic;
    protected JCheckBox actColor;
    protected JButton colButton;

    /**
     * Creates a new EmphasizeStyle object.
     */
    public EmphasizeStyle() {
      JLabel eLabel = new JLabel(Resources.get("tree.iconadapter.emphasize.text"));

      eLabel.setToolTipText(Resources.get("tree.iconadapter.emphasize.tooltip"));

      this.add(eLabel);

      Container style = Box.createVerticalBox();
      style.add(bold =
          new JCheckBox(Resources.get("tree.iconadapter.emphasize.bold"),
              (CellRenderer.emphasizeStyleChange & Font.BOLD) != 0));
      style.add(italic =
          new JCheckBox(Resources.get("tree.iconadapter.emphasize.italic"),
              (CellRenderer.emphasizeStyleChange & Font.ITALIC) != 0));
      this.add(style);
      this.add(actColor =
          new JCheckBox(Resources.get("tree.iconadapter.emphasize.color.text"),
              CellRenderer.emphasizeColor != null));
      colButton = new JButton(" ");
      colButton.addActionListener(this);
      colButton.setFocusPainted(false);

      if (CellRenderer.emphasizeColor != null) {
        colButton.setBackground(CellRenderer.emphasizeColor);
      }

      this.add(colButton);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      JButton source = (JButton) e.getSource();
      Color col =
          JColorChooser.showDialog(this, Resources.get("tree.iconadapter.colorchooser.title"),
              source.getBackground());

      if (col != null) {
        source.setBackground(col);
        actColor.setSelected(true);
      }
    }

    /**
     * apply input to CellRenderer
     */
    public void apply() {
      try {
        int sChange = 0;

        if (bold.isSelected()) {
          sChange = Font.BOLD;
        }

        if (italic.isSelected()) {
          sChange |= Font.ITALIC;
        }

        Color sColor = null;

        if (actColor.isSelected()) {
          sColor = colButton.getBackground();
        }

        CellRenderer.setEmphasizeData(sChange, sColor);
      } catch (Exception exc) {
        Logger.getInstance(this.getClass()).error(exc);
      }
    }
  }

  /**
   * Returns a list of preferences adapters that should be displayed in the given order.
   */
  public List<PreferencesAdapter> getChildren() {
    return subAdapters;
  }
}
