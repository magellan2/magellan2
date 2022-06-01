// class magellan.client.preferences.IconStyleSetPreferences
// created on 16.02.2008
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.client.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.GraphicsElement;
import magellan.client.swing.tree.GraphicsStyleset;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Preferences adapter for icon styles (see {@link GraphicsElement}, {@link CellRenderer}).
 */
public class IconStyleSetPreferences extends AbstractPreferencesAdapter implements ActionListener,
    TreeSelectionListener, PreferencesAdapter {

  /** For each styleset a mapping of the name to a copy of the styleset (used in the styleSetPanel) */
  protected Map<String, GraphicsStyleset> stylesetCopies;
  /** For each styleset a mapping of the name to the original styleset (used for actual changes) */
  protected Map<String, GraphicsStyleset> stylesetOriginals;

  protected Map<String, TreeNode> nodeMap;
  protected JTree stylesets;
  protected AbstractButton removeButton;
  protected DefaultTreeModel treeModel;
  private StylesetPanel styleSetPanel;

  /**
   * Creates a new Stylesets object.
   */
  public IconStyleSetPreferences() {
    JPanel root = addPanel(Resources.get("tree.iconadapter.styles.title"), new GridBagLayout());

    // left: "add" & "remove" buttons + styleset combobox
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));

    // the tree of stylesets
    treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    stylesets = new JTree(treeModel);
    stylesets.setRootVisible(false);
    stylesets.setEditable(false);
    stylesets.addTreeSelectionListener(this);

    JScrollPane pane = new JScrollPane(stylesets);
    pane.setPreferredSize(new Dimension(150, 2 * pane.getPreferredSize().height));

    JButton button = new JButton(Resources.get("tree.iconadapter.styles.add"));
    button.addActionListener(this);
    p.add(button);
    button = new JButton(Resources.get("tree.iconadapter.styles.remove"));
    button.addActionListener(this);
    p.add(button);
    removeButton = button;
    button.setEnabled(false);

    JPanel p2 = new JPanel();
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
    p2.add(p);
    p2.add(new JSeparator());

    // center content
    stylesetCopies = new HashMap<String, GraphicsStyleset>();
    stylesetOriginals = new HashMap<String, GraphicsStyleset>();

    for (int i : new int[] { CellRenderer.SIMPLE_STYLE, CellRenderer.MAIN_STYLE,
        CellRenderer.ADDITIONAL_STYLE }) {
      stylesetCopies.put(CellRenderer.STYLE_NAMES[i], CellRenderer.getTypeset(i).clone());
      stylesetOriginals.put(CellRenderer.STYLE_NAMES[i], CellRenderer.getTypeset(i));
    }

    // this is re-initialized every time a new styleset is selected in the tree
    styleSetPanel = new StylesetPanel(CellRenderer.getTypeset(CellRenderer.SIMPLE_STYLE));

    GridBagConstraints gbc =
        new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 5);
    root.add(p2, gbc);
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.5;
    gbc.weighty = 1;
    root.add(pane, gbc);

    gbc.gridx++;
    gbc.weightx = 1;
    root.add(styleSetPanel, gbc);
  }

  /**
   * Re-builds the tree from {@link CellRenderer#getStylesets()} and re-fills the styleset mappings.
   */
  public void updatePreferences() {
    DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode();

    if (nodeMap == null) {
      nodeMap = new HashMap<String, TreeNode>();
    } else {
      nodeMap.clear();
    }

    DefaultMutableTreeNode node =
        new DefaultMutableTreeNode(new TreeObject(
            CellRenderer.STYLE_NAMES[CellRenderer.SIMPLE_STYLE], Resources
                .get("tree.iconadapter.styles.simple")));
    DefaultMutableTreeNode firstNode = node;
    newRoot.add(node);
    nodeMap.put(CellRenderer.STYLE_NAMES[CellRenderer.SIMPLE_STYLE], node);
    node =
        new DefaultMutableTreeNode(new TreeObject(
            CellRenderer.STYLE_NAMES[CellRenderer.MAIN_STYLE], Resources
                .get("tree.iconadapter.styles.main")));
    newRoot.add(node);
    nodeMap.put(CellRenderer.STYLE_NAMES[CellRenderer.MAIN_STYLE], node);
    node =
        new DefaultMutableTreeNode(new TreeObject(
            CellRenderer.STYLE_NAMES[CellRenderer.ADDITIONAL_STYLE], Resources
                .get("tree.iconadapter.styles.additional")));
    newRoot.add(node);
    nodeMap.put(CellRenderer.STYLE_NAMES[CellRenderer.ADDITIONAL_STYLE], node);

    List<String> stylesetNames = null;

    if (CellRenderer.getStylesets() != null) {
      stylesetNames = new LinkedList<String>(CellRenderer.getStylesets().keySet());
      stylesetNames.remove(CellRenderer.STYLE_NAMES[CellRenderer.DEFAULT_STYLE]);
      stylesetNames.remove(CellRenderer.STYLE_NAMES[CellRenderer.MAIN_STYLE]);
      stylesetNames.remove(CellRenderer.STYLE_NAMES[CellRenderer.SIMPLE_STYLE]);
      stylesetNames.remove(CellRenderer.STYLE_NAMES[CellRenderer.ADDITIONAL_STYLE]);

      if (stylesetNames.size() > 0) {
        Collections.sort(stylesetNames);

        for (String s : stylesetNames) {

          node = new DefaultMutableTreeNode(new TreeObject(s));
          nodeMap.put(s, node);

          if (s.indexOf('.') > 0) {
            String parent = s.substring(0, s.lastIndexOf('.'));

            if (nodeMap.containsKey(parent)) {
              DefaultMutableTreeNode pNode = (DefaultMutableTreeNode) nodeMap.get(parent);
              pNode.add(node);
            } else { // this may happen if the user inserted one directly and it was not needed
              newRoot.add(node);
            }
          } else {
            newRoot.add(node);
          }
        }
      }
    }

    treeModel.setRoot(newRoot);

    Map<String, GraphicsStyleset> oldCopies = new HashMap<String, GraphicsStyleset>(stylesetCopies);
    Map<String, GraphicsStyleset> oldOriginals =
        new HashMap<String, GraphicsStyleset>(stylesetOriginals);
    stylesetCopies.clear();
    stylesetOriginals.clear();

    for (int type : new int[] { CellRenderer.SIMPLE_STYLE, CellRenderer.MAIN_STYLE,
        CellRenderer.ADDITIONAL_STYLE }) {
      stylesetCopies.put(CellRenderer.STYLE_NAMES[type], oldCopies
          .get(CellRenderer.STYLE_NAMES[type]));
      stylesetOriginals.put(CellRenderer.STYLE_NAMES[type], oldOriginals
          .get(CellRenderer.STYLE_NAMES[type]));
    }

    if ((stylesetNames != null) && (stylesetNames.size() > 0)) {
      for (String name : stylesetNames) {

        if (oldCopies.containsKey(name)) {
          stylesetOriginals.put(name, oldOriginals.get(name));
          stylesetCopies.put(name, oldCopies.get(name));
        } else {
          stylesetCopies.put(name, CellRenderer.getStylesets().get(name).clone());
          stylesetOriginals.put(name, CellRenderer.getStylesets().get(name));
        }

      }
    }

    openTree(newRoot, new TreePath(treeModel.getPathToRoot(firstNode)));
  }

  /**
   * 
   */
  protected void openTree(TreeNode root, TreePath first) {
    // expand all nodes
    if (root.getChildCount() > 0) {
      for (int i = 0, max = root.getChildCount(); i < max; i++) {
        openNode(root.getChildAt(i));
      }
    }

    stylesets.setSelectionPath(first);
  }

  /**
   * 
   */
  protected void openNode(TreeNode node) {
    stylesets.expandPath(new TreePath(treeModel.getPathToRoot(node)));

    if (node.getChildCount() > 0) {
      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        openNode(node.getChildAt(i));
      }
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() != removeButton) {
      addStyleset();

      return;
    }

    removeStyleset();
  }

  /**
   * 
   */
  protected void addStyleset() {
    String name =
        JOptionPane.showInputDialog(this, Resources.get("tree.iconadapter.styles.add.text"));

    if (name == null)
      return;

    name = name.trim();

    if (name.equals(""))
      return;

    if (stylesetCopies.containsKey(name))
      return;

    if (!stylesets.isSelectionEmpty()) {
      TreeObject obj =
          (TreeObject) ((DefaultMutableTreeNode) stylesets.getSelectionPath()
              .getLastPathComponent()).getUserObject();
      name = obj.name + "." + name;
    }

    GraphicsStyleset set = new GraphicsStyleset(name);
    CellRenderer.addStyleset(set);
    stylesetCopies.put(name, set.clone());
    stylesetOriginals.put(name, set);

    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeObject(name));
    MutableTreeNode root = (MutableTreeNode) treeModel.getRoot();

    if (name.indexOf('.') > 0) {
      String parent = name.substring(0, name.lastIndexOf('.'));

      if (nodeMap.containsKey(parent)) {
        root = (MutableTreeNode) nodeMap.get(parent);
        treeModel.insertNodeInto(node, root, root.getChildCount());
      } else {
        treeModel.insertNodeInto(node, root, root.getChildCount());
      }
    } else {
      treeModel.insertNodeInto(node, root, root.getChildCount());
    }

    stylesets.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));

    return;
  }

  /**
   * 
   */
  protected void removeStyleset() {
    if (!stylesets.isSelectionEmpty()) {
      DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) stylesets.getSelectionPath().getLastPathComponent();
      TreeObject obj = (TreeObject) node.getUserObject();

      stylesetCopies.remove(obj.name);
      stylesetOriginals.remove(obj.name);

      CellRenderer.removeStyleset(obj.name);
      treeModel.removeNodeFromParent(node);
    }
  }

  /**
   * Updates stylePanel and buttons after a new styleset has been selected in the tree.
   * 
   * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
   */
  public void valueChanged(TreeSelectionEvent e) {
    if (stylesets.isSelectionEmpty()) {
      removeButton.setEnabled(false);
    } else {
      TreeObject obj =
          (TreeObject) ((DefaultMutableTreeNode) e.getPath().getLastPathComponent())
              .getUserObject();

      if (obj != null) {
        apply(obj.name);

        boolean removeEnabled = true;

        if (obj.name.equals(CellRenderer.STYLE_NAMES[CellRenderer.SIMPLE_STYLE])
            || obj.name.equals(CellRenderer.STYLE_NAMES[CellRenderer.MAIN_STYLE])
            || obj.name.equals(CellRenderer.STYLE_NAMES[CellRenderer.ADDITIONAL_STYLE])) {
          removeEnabled = false;
        }

        removeButton.setEnabled(removeEnabled);
      }
    }
  }

  private void apply(String name) {
    styleSetPanel.show(stylesetCopies.get(name));
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    updatePreferences();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {

    GraphicsStyleset shownSet = styleSetPanel.set;
    styleSetPanel.setVisible(false);
    for (String name : stylesetCopies.keySet()) {
      styleSetPanel.show(stylesetCopies.get(name));
      styleSetPanel.apply(stylesetOriginals.get(name));
    }
    styleSetPanel.setVisible(true);
    styleSetPanel.show(shownSet);

    CellRenderer.saveStylesets();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("tree.iconadapter.styles.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @author ...
   * @version 1.0, 16.02.2008
   */
  protected static class StylesetPanel extends JPanel {
    protected GraphicsStyleset set;
    protected DirectionPanel direction;
    protected FontPanel font;
    protected ColorPanel colors;
    protected JCheckBox fontEnabled;

    static class DirectionPanel extends JPanel implements ActionListener {
      protected AbstractButton buttons[];
      protected AbstractButton last;
      protected int horiz;
      protected int vertic;

      /**
       * Creates a new DirectionPanel object.
       */
      public DirectionPanel() {
        setLayout(new GridLayout(3, 3));
        buttons = new AbstractButton[9];

        ButtonGroup gr = new ButtonGroup();

        for (int j = 0; j < 3; j++) {
          int jv = 0;

          switch (j) {
          case 0:
            jv = SwingConstants.TOP;

            break;

          case 1:
            jv = SwingConstants.CENTER;

            break;

          case 2:
            jv = SwingConstants.BOTTOM;

            break;
          }

          for (int i = 0; i < 3; i++) {
            int iv = 0;

            switch (i) {
            case 0:
              iv = SwingConstants.LEFT;

              break;

            case 1:
              iv = SwingConstants.CENTER;

              break;

            case 2:
              iv = SwingConstants.RIGHT;

              break;
            }

            JToggleButton button = new JToggleButton();
            button.addActionListener(this);
            button.setActionCommand(String.valueOf(iv) + ";" + String.valueOf(jv));
            this.add(button);
            buttons[(j * 3) + i] = button;
            gr.add(button);
          }
        }

        buttons[4].setText(Resources.get("tree.iconadapter.icontext.position.icon"));
        last = buttons[5];
        buttons[5].setSelected(true);
        buttons[5].setText(Resources.get("tree.iconadapter.icontext.position.t"));
      }

      /**
       * Applies the properties getVerticalPos(), getHorizontalPos() to this panel.
       */
      public void setStyleset(GraphicsStyleset set) {
        int j = 0;

        if (set.getVerticalPos() == SwingConstants.CENTER) {
          j = 1;
        }

        if (set.getVerticalPos() == SwingConstants.BOTTOM) {
          j = 2;
        }

        int i = 0;

        if (set.getHorizontalPos() == SwingConstants.CENTER) {
          i = 1;
        }

        if (set.getHorizontalPos() == SwingConstants.RIGHT) {
          i = 2;
        }

        buttons[(j * 3) + i].doClick(1);
        horiz = set.getHorizontalPos();
        vertic = set.getVerticalPos();
      }

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
        if (last != null) {
          if (last == buttons[4]) {
            last.setText(Resources.get("tree.iconadapter.icontext.position.icon"));
          } else {
            last.setText(null);
          }
        }

        last = (AbstractButton) e.getSource();

        if (last == buttons[4]) {
          last.setText(Resources.get("tree.iconadapter.icontext.position.icon&text"));
        } else {
          last.setText(Resources.get("tree.iconadapter.icontext.position.t"));
        }

        StringTokenizer st = new StringTokenizer(e.getActionCommand(), ";");
        horiz = Integer.parseInt(st.nextToken());
        vertic = Integer.parseInt(st.nextToken());
      }

      /**
       * Applies the settings of this panel to <code>resultSet</code>.
       */
      public void apply(GraphicsStyleset resultSet) {
        // work-around for corner-bug
        int i = vertic;

        if ((last == buttons[0]) || (last == buttons[2]) || (last == buttons[6])
            || (last == buttons[8])) {
          if (vertic == SwingConstants.TOP) {
            i = SwingConstants.BOTTOM;
          } else {
            i = SwingConstants.TOP;
          }
        }

        resultSet.setHorizontalPos(horiz);
        resultSet.setVerticalPos(i);
      }
    }

    /**
     * Displays a panel which shows selections for a font.
     */
    class FontPanel extends JPanel {
      private final Logger log = Logger.getInstance(FontPanel.class);
      protected JComboBox fonts;
      protected JCheckBox styles[];
      protected JComboBox size;

      /**
       * Creates a new FontPanel object.
       */
      public FontPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints con = new GridBagConstraints();
        con.gridwidth = 1;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.anchor = GridBagConstraints.CENTER;
        con.weightx = 1;

        // Font-Family box
        String fontNa[] = null;

        try {
          fontNa = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        } catch (NullPointerException e) {
          // FIXME(pavkovic) 2003.03.17: This is bad!
          log.error(
              "Probably your are running jdk1.4.1 on Apple. Perhaps we can keep Magellan running. But don't count on it!");
          fontNa = new String[0];
        }

        fonts = new JComboBox(fontNa);
        this.add(fonts, con);
        con.fill = GridBagConstraints.NONE;
        con.gridx = 1;

        // Style checkboxes
        styles = new JCheckBox[2];
        styles[0] = new JCheckBox(Resources.get("tree.iconadapter.icontext.text.font.bold"));
        styles[0].setFont(styles[0].getFont().deriveFont(Font.BOLD));
        styles[1] = new JCheckBox(Resources.get("tree.iconadapter.icontext.text.font.italic"));
        styles[1].setFont(styles[1].getFont().deriveFont(Font.ITALIC));

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(2, 1));
        p2.add(styles[0]);
        p2.add(styles[1]);
        this.add(p2, con);

        // Font-size box
        String sizes[] = new String[11];

        for (int i = 0; i < 11; i++) // values 8-18
        {
          sizes[i] = String.valueOf(i + 8);
        }

        size = new JComboBox(sizes);
        con.gridx = 2;
        this.add(size, con);
      }

      /**
       * Applies the font properties of <code>set</code> to this panel.
       */
      public void setStyleset(GraphicsStyleset set) {
        if (set.getFont() != null) {
          Font f = set.getFont();
          fonts.setSelectedItem(f.getFamily());
          styles[0].setSelected(f.isBold());
          styles[1].setSelected(f.isItalic());
          size.setSelectedItem(String.valueOf(f.getSize()));
        } else {
          // we could set everything back to default here but maybe the user doesn't want this.
        }
      }

      /**
       * Returns a new Font according to the settings of this panel.
       */
      public Font getSelectedFont() {
        int style = 0;

        if (styles[0].isSelected()) {
          style |= Font.BOLD;
        }

        if (styles[1].isSelected()) {
          style |= Font.ITALIC;
        }

        int sizeI = Integer.parseInt((String) size.getSelectedItem());

        return new Font((String) fonts.getSelectedItem(), style, sizeI);
      }
    }

    /**
     * @author ...
     * @version 1.0, 16.02.2008
     */
    protected static class ColorPanel extends JPanel implements ActionListener {
      protected JCheckBox boxes[];
      protected JButton buttons[];
      private Color neutralColor;

      /**
       * Creates a new ColorPanel object.
       */
      public ColorPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.NONE;
        con.anchor = GridBagConstraints.NORTHWEST;
        con.gridwidth = 1;
        con.gridheight = 1;
        boxes = new JCheckBox[4];
        buttons = new JButton[4];

        for (int i = 0; i < 4; i++) {
          con.gridx = 0;
          con.gridy = i;
          this.add(boxes[i] =
              new JCheckBox(Resources.get("tree.iconadapter.styles.color." + String.valueOf(i))),
              con);
          con.gridx = 1;
          this.add(buttons[i] = new JButton(" "), con);
          buttons[i].addActionListener(this);
          buttons[i].setFocusPainted(false);
        }
        neutralColor = buttons[0].getBackground();
      }

      /**
       * Applies the foreground and background properties of <code>set</code> to this panel.
       */
      public void setStyleset(GraphicsStyleset set) {
        if (set.getForeground() != null) {
          boxes[0].setSelected(true);
          buttons[0].setBackground(set.getForeground());
        } else {
          boxes[0].setSelected(false);
          buttons[0].setBackground(neutralColor);
        }

        if (set.getBackground() != null) {
          boxes[1].setSelected(true);
          buttons[1].setBackground(set.getBackground());
        } else {
          boxes[1].setSelected(false);
          buttons[1].setBackground(neutralColor);
        }

        if (set.getSelectedForeground() != null) {
          boxes[2].setSelected(true);
          buttons[2].setBackground(set.getSelectedForeground());
        } else {
          boxes[2].setSelected(false);
          buttons[2].setBackground(neutralColor);
        }

        if (set.getSelectedBackground() != null) {
          boxes[3].setSelected(true);
          buttons[3].setBackground(set.getSelectedBackground());
        } else {
          boxes[3].setSelected(false);
          buttons[3].setBackground(neutralColor);
        }
      }

      /**
       * Applies the settings of this panel to <code>resultSet</code>.
       */
      public void apply(GraphicsStyleset resultSet) {
        if (boxes[0].isSelected()) {
          resultSet.setForeground(buttons[0].getBackground());
        } else {
          resultSet.setForeground(null);
        }

        if (boxes[1].isSelected()) {
          resultSet.setBackground(buttons[1].getBackground());
        } else {
          resultSet.setBackground(null);
        }

        if (boxes[2].isSelected()) {
          resultSet.setSelectedForeground(buttons[2].getBackground());
        } else {
          resultSet.setSelectedForeground(null);
        }

        if (boxes[3].isSelected()) {
          resultSet.setSelectedBackground(buttons[3].getBackground());
        } else {
          resultSet.setSelectedBackground(null);
        }
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

          int index = 0;

          while (source != buttons[index]) {
            index++;
          }

          boxes[index].setSelected(true);
        }
      }
    }

    /**
     * Creates a new StylesetPanel object.
     */
    public StylesetPanel(GraphicsStyleset set) {
      this.set = set;
      setLayout(new GridBagLayout());

      GridBagConstraints con = new GridBagConstraints();
      con.fill = GridBagConstraints.BOTH;
      con.anchor = GridBagConstraints.CENTER;

      Border border = BorderFactory.createEtchedBorder();

      // font
      JPanel p = new JPanel(new GridBagLayout());
      fontEnabled =
          new JCheckBox(Resources.get("tree.iconadapter.icontext.text.font"), set.getFont() != null);
      con.fill = GridBagConstraints.NONE;
      con.gridx = 0;
      con.gridwidth = 1;
      con.gridheight = 1;
      p.add(fontEnabled, con);
      con.gridy = 1;
      con.gridwidth = 3;
      font = new FontPanel();
      font.setStyleset(set);
      p.add(font, con);
      con.fill = GridBagConstraints.BOTH;
      con.weightx = 1;
      con.gridx = 0;
      con.gridy = 1;
      con.gridheight = 1;

      p.setBorder(border);
      con.gridwidth = 2;
      con.anchor = GridBagConstraints.WEST;
      this.add(p, con);

      // direction
      direction = new DirectionPanel();
      direction.setStyleset(set);
      direction.setBorder(border);
      con.gridx = 0;
      con.gridy = 0;
      con.gridwidth = 1;
      this.add(direction, con);

      // colors
      colors = new ColorPanel();
      colors.setStyleset(set);
      con.gridx = 1;
      colors.setBorder(border);
      this.add(colors, con);
    }

    /**
     * Sets <code>this.set</code> and updates panel.
     * 
     * @param newSet
     */
    public void show(GraphicsStyleset newSet) {
      apply(set);
      set = newSet;

      fontEnabled.setSelected(newSet.getFont() != null);

      font.setStyleset(newSet);

      direction.setStyleset(newSet);

      colors.setStyleset(newSet);
    }

    /**
     * Applies the settings of the panel to <code>this.set</code>.
     */
    public void apply() {
      apply(set);

    }

    /**
     * Applies the settings of the panel to <code>resultSet</code>.
     */
    public void apply(GraphicsStyleset resultSet) {
      if (fontEnabled.isSelected()) {
        resultSet.setFont(font.getSelectedFont());
      } else {
        resultSet.setFont(null);
      }

      direction.apply(resultSet);
      colors.apply(resultSet);
    }
  }

  /**
   * @author ...
   * @version 1.0, 16.02.2008
   */
  protected static class TreeObject {
    protected String name;
    protected String sName;

    /**
     * Creates a new TreeObject object.
     */
    public TreeObject(String name) {
      this.name = name;

      if (name.indexOf('.') > 0) {
        sName = name.substring(name.lastIndexOf('.') + 1);
      } else {
        sName = name;
      }
    }

    /**
     * Creates a new TreeObject object.
     */
    public TreeObject(String name, String sName) {
      this.name = name;
      this.sName = sName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return sName;
    }
  }

}
