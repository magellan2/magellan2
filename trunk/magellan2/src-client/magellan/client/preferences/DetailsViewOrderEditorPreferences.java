// class magellan.client.preferences.OrderEditorPreferences
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
package magellan.client.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import magellan.client.swing.completion.MultiEditorOrderEditorList;
import magellan.client.swing.completion.OrderEditor;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

public class DetailsViewOrderEditorPreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter {
  private MultiEditorOrderEditorList source = null;
  private JPanel pnlStandardColor = null;
  private JPanel pnlStandardColorConfirmed = null;
  private JPanel pnlActiveColor = null;
  private JPanel pnlActiveColorConfirmed = null;
  private JPanel pnlStylesColor = null;
  private JPanel pnlBackgroundColor = null;
  private JCheckBox chkMultiEditorLayout;
  private JCheckBox chkHideButtons;
  private JCheckBox chkEditAllFactions;
  private JCheckBox chkSyntaxHighlighting;
  private JComboBox comboSHColors = null;
  private Dimension prefDim = new Dimension(20, 20);
  private JCheckBox listModes[];

  /**
   * Creates a new OrderEditorListPreferences object.
   */
  public DetailsViewOrderEditorPreferences(MultiEditorOrderEditorList source) {
    this.source = source;

    getLayoutPanel();

    getColorPanel();

    getHighlightPanel();

    getListModePanel();

  }

  protected Container getLayoutPanel() {
    JPanel content =
        addPanel(Resources.get("completion.multieditorordereditorlist.prefs.layout"),
            new FlowLayout(FlowLayout.LEADING, 3, 0));

    chkMultiEditorLayout =
        new JCheckBox(Resources
            .get("completion.multieditorordereditorlist.prefs.multieditorlayout"), source
            .isMultiEditorLayout());
    content.add(chkMultiEditorLayout);

    chkHideButtons =
        new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.hidebuttons"),
            source.isHideButtons());
    content.add(chkHideButtons);

    chkEditAllFactions =
        new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.editallfactions"),
            source.isEditAllFactions());
    content.add(chkEditAllFactions);

    return content;
  }

  protected Container getColorPanel() {
    JPanel content =
        addPanel(Resources.get("completion.multieditorordereditorlist.prefs.colors"),
            new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 2, 1, 1), 0, 0);

    JLabel lblStandardColor =
        new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.inactivebackground")
            + ": ");

    pnlStandardColor = new JPanel();
    pnlStandardColor.setBorder(new LineBorder(Color.black));
    pnlStandardColor.setPreferredSize(prefDim);
    pnlStandardColor.setBackground(source.getStandardBackgroundColor());
    pnlStandardColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        Color newColor =
            JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(), Resources
                .get("completion.multieditorordereditorlist.prefs.backgroundcolor"), ((Component) e
                .getSource()).getBackground());

        if (newColor != null) {
          ((Component) e.getSource()).setBackground(newColor);
        }
      }
    });

    JLabel lblStandardColorConfirmed =
        new JLabel(Resources
            .get("completion.multieditorordereditorlist.prefs.inactivebackground.confirmed")
            + ": ");

    pnlStandardColorConfirmed = new JPanel();
    pnlStandardColorConfirmed.setBorder(new LineBorder(Color.black));
    pnlStandardColorConfirmed.setPreferredSize(prefDim);
    pnlStandardColorConfirmed.setBackground(source.getStandardBackgroundColorConfirmed());
    pnlStandardColorConfirmed.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        Color newColor =
            JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(), Resources
                .get("completion.multieditorordereditorlist.prefs.backgroundcolor"), ((Component) e
                .getSource()).getBackground());

        if (newColor != null) {
          ((Component) e.getSource()).setBackground(newColor);
        }
      }
    });

    JLabel lblActiveColor =
        new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.activebackground")
            + ": ");

    pnlActiveColor = new JPanel();
    pnlActiveColor.setBorder(new LineBorder(Color.black));
    pnlActiveColor.setPreferredSize(prefDim);
    pnlActiveColor.setBackground(source.getActiveBackgroundColor());
    pnlActiveColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        Color newColor =
            JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(), Resources
                .get("completion.multieditorordereditorlist.prefs.backgroundcolor"), ((Component) e
                .getSource()).getBackground());

        if (newColor != null) {
          ((Component) e.getSource()).setBackground(newColor);
        }
      }
    });

    JLabel lblActiveColorConfirmed =
        new JLabel(Resources
            .get("completion.multieditorordereditorlist.prefs.activebackground.confirmed")
            + ": ");

    pnlActiveColorConfirmed = new JPanel();
    pnlActiveColorConfirmed.setBorder(new LineBorder(Color.black));
    pnlActiveColorConfirmed.setPreferredSize(prefDim);
    pnlActiveColorConfirmed.setBackground(source.getActiveBackgroundColorConfirmed());
    pnlActiveColorConfirmed.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        Color newColor =
            JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(), Resources
                .get("completion.multieditorordereditorlist.prefs.backgroundcolor"), ((Component) e
                .getSource()).getBackground());

        if (newColor != null) {
          ((Component) e.getSource()).setBackground(newColor);
        }
      }
    });

    content.add(lblActiveColor, c);
    c.gridx++;
    content.add(pnlActiveColor, c);

    c.gridx = 0;
    c.gridy++;

    content.add(lblActiveColorConfirmed, c);
    c.gridx++;
    content.add(pnlActiveColorConfirmed, c);

    c.gridx = 0;
    c.gridy++;

    content.add(lblStandardColor, c);
    c.gridx++;
    content.add(pnlStandardColor, c);

    c.gridx = 0;
    c.gridy++;

    content.add(lblStandardColorConfirmed, c);
    c.gridx++;
    content.add(pnlStandardColorConfirmed, c);

    return content;
  }

  protected Container getHighlightPanel() {
    JPanel content =
        addPanel(Resources.get("completion.multieditorordereditorlist.prefs.syntaxhighlighting"),
            new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 2, 1, 1), 0, 0);

    chkSyntaxHighlighting =
        new JCheckBox(Resources
            .get("completion.multieditorordereditorlist.prefs.syntaxhighlighting.caption"), source
            .getUseSyntaxHighlighting());
    content.add(chkSyntaxHighlighting, c);

    c.gridy++;

    Container styles = createStylesContainer();
    content.add(styles, c);

    return content;
  }

  protected Container getListModePanel() {
    JPanel content =
        addPanel(Resources.get("completion.multieditorordereditorlist.prefs.listMode"),
            new BorderLayout(2, 2));

    JLabel dummy = new JLabel();

    JTextArea text =
        new JTextArea(Resources.get("completion.multieditorordereditorlist.prefs.listMode.text"));
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setSelectionColor(text.getBackground());
    text.setSelectedTextColor(text.getForeground());
    text.setRequestFocusEnabled(false);
    text.setBackground(content.getBackground());
    text.setSelectionColor(content.getBackground());
    text.setSelectedTextColor(content.getForeground());
    text.setFont(dummy.getFont());
    content.add(text, BorderLayout.NORTH);

    JPanel help = new JPanel(new GridBagLayout());
    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 0.5, 0, GridBagConstraints.WEST,
            GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
    listModes = new JCheckBox[3];

    for (int i = 0; i < 3; i++) {
      listModes[i] =
          new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.listMode." + i));
      listModes[i].setSelected(((source.getListMode() >> (3 - i)) & 1) != 0);
      help.add(listModes[i], con);
      con.gridy++;
    }

    con.gridy = 0;
    con.gridx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;

    for (int i = 0; i < 3; i++) {
      text =
          new JTextArea(Resources.get("completion.multieditorordereditorlist.prefs.listMode." + i
              + ".text"));
      text.setEditable(false);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);

      text.setSelectionColor(text.getBackground());
      text.setSelectedTextColor(text.getForeground());
      text.setRequestFocusEnabled(false);
      text.setBackground(content.getBackground());
      text.setSelectionColor(content.getBackground());
      text.setSelectedTextColor(content.getForeground());
      text.setFont(dummy.getFont());
      help.add(text, con);
      con.gridy++;
    }

    content.add(help, BorderLayout.CENTER);

    help = new JPanel();
    help.setPreferredSize(new Dimension(15, 5));
    content.add(help, BorderLayout.WEST);

    return content;
  }

  protected Container createStylesContainer() {
    JPanel content = new JPanel(new GridBagLayout());

    comboSHColors = new JComboBox(getStyles());
    comboSHColors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        StyleContainer c = (StyleContainer) comboSHColors.getSelectedItem();
        pnlStylesColor.setBackground(c.color);
      }
    });

    pnlStylesColor = new JPanel();
    pnlStylesColor.setBorder(new LineBorder(Color.black));
    pnlStylesColor.setPreferredSize(prefDim);
    pnlStylesColor.setBackground(((StyleContainer) comboSHColors.getItemAt(0)).color);
    pnlStylesColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {
        StyleContainer sc = (StyleContainer) comboSHColors.getSelectedItem();
        Color newColor =
            JColorChooser.showDialog(((JComponent) me.getSource()).getTopLevelAncestor(),
                sc.description + " Farbe", sc.color);

        if (newColor != null) {
          sc.color = newColor;
          pnlStylesColor.setBackground(newColor);
        }
      }
    });

    pnlBackgroundColor = new JPanel();
    pnlBackgroundColor.setBorder(new LineBorder(Color.black));
    pnlBackgroundColor.setPreferredSize(prefDim);
    pnlBackgroundColor.setBackground(source.getErrorBackground());
    pnlBackgroundColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {
        Color newColor =
            JColorChooser.showDialog(((JComponent) me.getSource()).getTopLevelAncestor(), Resources
                .get("completion.multieditorordereditorlist.prefs.backgroundcolorchooser.title"),
                source.getErrorBackground());

        if (newColor != null) {
          source.setErrorBackground(newColor);
          pnlBackgroundColor.setBackground(newColor);
        }
      }
    });

    GridBagConstraints gbc =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0);
    content.add(comboSHColors, gbc);

    gbc.gridx++;
    content.add(Box.createRigidArea(new Dimension(5, 5)));

    gbc.gridx++;
    content.add(pnlStylesColor);

    gbc.gridx++;
    content.add(Box.createRigidArea(new Dimension(10, 5)));

    gbc.gridx++;
    content.add(new JLabel(Resources
        .get("completion.multieditorordereditorlist.prefs.backgroundcolorchooser.label")));

    gbc.gridx++;
    content.add(Box.createRigidArea(new Dimension(5, 5)));

    gbc.gridx++;
    content.add(pnlBackgroundColor);

    return content;
  }

  private Object[] getStyles() {
    Object styles[] = new Object[6];

    styles[0] =
        new StyleContainer(Resources
            .get("completion.multieditorordereditorlist.prefs.colors.standard"),
            OrderEditor.S_REGULAR, source.getTokenColor(OrderEditor.S_REGULAR));
    styles[1] =
        new StyleContainer(Resources
            .get("completion.multieditorordereditorlist.prefs.colors.keywords"),
            OrderEditor.S_KEYWORD, source.getTokenColor(OrderEditor.S_KEYWORD));
    styles[2] =
        new StyleContainer(Resources
            .get("completion.multieditorordereditorlist.prefs.colors.strings"),
            OrderEditor.S_STRING, source.getTokenColor(OrderEditor.S_STRING));
    styles[3] =
        new StyleContainer(Resources
            .get("completion.multieditorordereditorlist.prefs.colors.numbers"),
            OrderEditor.S_NUMBER, source.getTokenColor(OrderEditor.S_NUMBER));
    styles[4] =
        new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.ids"),
            OrderEditor.S_ID, source.getTokenColor(OrderEditor.S_ID));
    styles[5] =
        new StyleContainer(Resources
            .get("completion.multieditorordereditorlist.prefs.colors.comments"),
            OrderEditor.S_COMMENT, source.getTokenColor(OrderEditor.S_COMMENT));

    return styles;
  }

  public void initPreferences() {
    // TODO: implement it
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    source.setActiveBackgroundColor(pnlActiveColor.getBackground());
    source.setActiveBackgroundColorConfirmed(pnlActiveColorConfirmed.getBackground());
    source.setStandardBackgroundColor(pnlStandardColor.getBackground());
    source.setStandardBackgroundColorConfirmed(pnlStandardColorConfirmed.getBackground());
    source.setMultiEditorLayout(chkMultiEditorLayout.isSelected());
    source.setHideButtons(chkHideButtons.isSelected());
    source.setEditAllFactions(chkEditAllFactions.isSelected());
    source.setUseSyntaxHighlighting(chkSyntaxHighlighting.isSelected());

    source.setErrorBackground(pnlBackgroundColor.getBackground());

    for (int i = 0; i < comboSHColors.getItemCount(); i++) {
      StyleContainer sc = (StyleContainer) comboSHColors.getItemAt(i);
      source.setTokenColor(sc.name, sc.color);
    }

    source.setListMode(0);

    for (int i = 0; i < 3; i++) {
      if (listModes[i].isSelected()) {
        source.setListMode(source.getListMode() | (1 << (3 - i)));
      }
    }

    if (source.getListMode() == 0) {
      source.setListMode(1 << MultiEditorOrderEditorList.LIST_REGION);
    }

    source.saveListProperty();
  }

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
    return Resources.get("completion.multieditorordereditorlist.prefs.title");
  }

  private static class StyleContainer {
    /** DOCUMENT-ME */
    public String description;

    /** DOCUMENT-ME */
    public String name;

    /** DOCUMENT-ME */
    public Color color;

    /**
     * Creates a new StyleContainer object.
     */
    public StyleContainer(String description, String name, Color color) {
      this.description = description;
      this.name = name;
      this.color = color;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      return description;
    }
  }
}
