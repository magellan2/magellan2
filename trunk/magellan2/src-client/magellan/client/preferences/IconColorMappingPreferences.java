// class magellan.client.preferences.IconColorMappingPreferences
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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tree.CellRenderer;
import magellan.library.utils.Resources;

/**
 * @author ...
 * @version 1.0, 16.02.2008
 */
public class IconColorMappingPreferences extends JPanel implements ActionListener,
    PreferencesAdapter, ListSelectionListener, KeyListener {

  protected JList list;
  protected DefaultListModel listModel;
  protected JButton buttons[];
  protected HelpDialog dialog;

  /**
   * Creates a new ColorMapping object.
   */
  public IconColorMappingPreferences() {
    setLayout(new GridBagLayout());
    setBorder(new TitledBorder(Resources.get("tree.iconadapter.colormap.title")));

    GridBagConstraints con = new GridBagConstraints();
    con.weighty = 1;
    con.fill = GridBagConstraints.BOTH;
    con.gridy = 0;
    con.gridx = 0;
    con.weightx = 1;
    con.anchor = GridBagConstraints.CENTER;
    con.gridwidth = 1;
    con.gridheight = 3;
    con.insets = new Insets(1, 3, 1, 3);

    listModel = createListModel();
    list = new JList(listModel);
    list.setCellRenderer(new ColorMappingListCellRenderer());
    list.addListSelectionListener(this);
    list.addKeyListener(this);
    this.add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), con);

    JPanel bBox = new JPanel(new GridLayout(0, 1));

    buttons = new JButton[5];

    // Border border = BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED);

    buttons[0] = new JButton(Resources.get("tree.iconadapter.colormap.button0"));
    buttons[0].addActionListener(this);
    // buttons[0].setBorder(border);
    bBox.add(buttons[0]);

    buttons[1] = new JButton(Resources.get("tree.iconadapter.colormap.button1"));
    buttons[1].addActionListener(this);
    buttons[1].setEnabled(false);
    // buttons[1].setBorder(border);
    bBox.add(buttons[1]);

    buttons[2] = new JButton(Resources.get("tree.iconadapter.colormap.button2"));
    buttons[2].addActionListener(this);
    buttons[2].setEnabled(false);
    // buttons[2].setBorder(border);
    bBox.add(buttons[2]);

    buttons[3] = new JButton(Resources.get("tree.iconadapter.colormap.button3"));
    buttons[3].addActionListener(this);
    // buttons[3].setBorder(border);
    bBox.add(Box.createVerticalStrut(20));
    bBox.add(buttons[3]);

    buttons[4] = new JButton(Resources.get("tree.iconadapter.colormap.button4"));
    buttons[4].addActionListener(this);
    // buttons[4].setBorder(border);
    bBox.add(Box.createVerticalStrut(20));
    bBox.add(buttons[4]);

    con.gridwidth = 1;
    con.gridx = 1;
    con.gridy = 2;
    con.fill = GridBagConstraints.NONE;
    con.weightx = 0;
    con.weighty = 0;
    con.gridheight = 1;
    this.add(bBox, con);

    dialog =
        new HelpDialog((Frame) getTopLevelAncestor(), Resources
            .get("tree.iconadapter.colormap.help.text"), Resources
            .get("tree.iconadapter.colormap.help.button"));
  }

  /**
   * 
   */
  protected DefaultListModel createListModel() {
    Map<String, Color> m = CellRenderer.colorMap;
    DefaultListModel dlm = new DefaultListModel();

    if ((m == null) || (m.size() == 0))
      return dlm;

    Set<String> s = m.keySet();
    List<String> l = new ArrayList<String>(s.size());
    l.addAll(s);
    Collections.sort(l);

    Iterator<String> it = l.iterator();

    while (it.hasNext()) {
      MapElement mapElem = new MapElement();
      mapElem.value = it.next();
      mapElem.color = m.get(mapElem.value);
      dlm.addElement(mapElem);
    }

    return dlm;
  }

  /**
   * presumes that the value is not used
   */
  protected void addPair(String value, Color col) {
    MapElement mapElem = new MapElement();
    mapElem.value = value;
    mapElem.color = col;
    listModel.addElement(mapElem);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent p1) {
    if (p1.getSource() == buttons[0]) {
      createPair();
    }

    if (p1.getSource() == buttons[1]) {
      changePair();
    }

    if (p1.getSource() == buttons[2]) {
      deletePairs();
    }

    if (p1.getSource() == buttons[3]) {
      createDefaultPairs();
    }

    if (p1.getSource() == buttons[4]) {
      dialog.setVisible(true);
    }
  }

  /**
   * 
   */
  protected boolean valueExists(String s) {
    for (int i = 0, max = listModel.size(); i < max; i++) {
      if (((MapElement) listModel.get(i)).value.equals(s))
        return true;
    }

    return false;
  }

  /**
   * 
   */
  protected void createPair() {
    String value = null;
    boolean error = false;

    do {
      value =
          JOptionPane
              .showInputDialog(this, Resources.get("tree.iconadapter.colormap.create.value"));

      if (value == null)
        return;

      error = value.equals("") || valueExists(value);

      if (error) {
        JOptionPane.showMessageDialog(this, Resources
            .get("tree.iconadapter.colormap.create.valueExisting"));
      }
    } while (error);

    Color col =
        JColorChooser.showDialog(this, Resources.get("tree.iconadapter.colormap.create.color"),
            CellRenderer.getTypeset(3).getForeground());

    if (col != null) {
      addPair(value, col);
    }
  }

  /**
   * 
   */
  protected void changePair() {
    MapElement elem = (MapElement) list.getSelectedValue();
    int index = list.getSelectedIndex();
    String newValue =
        JOptionPane.showInputDialog(this, Resources.get("tree.iconadapter.colormap.change.value"));

    if (newValue == null)
      return;

    if (!newValue.equals("") && valueExists(newValue)) {
      JOptionPane.showMessageDialog(this, Resources
          .get("tree.iconadapter.colormap.change.valueExisting"));

      return;
    }

    if (newValue.equals("")) {
      newValue = elem.value;
    }

    Color col =
        JColorChooser.showDialog(this, Resources.get("tree.iconadapter.colormap.change.color"),
            elem.color);

    if (col == null)
      return;

    elem.color = col;
    elem.value = newValue;
    listModel.set(index, elem);
  }

  /**
   * 
   */
  protected void deletePairs() {
    int indices[] = list.getSelectedIndices();

    for (int i = 0; i < indices.length; i++) {
      listModel.remove(indices[i] - i);
    }
  }

  /**
   * 
   */
  protected void createDefaultPairs() {
    listModel.clear();

    for (int i = 1; i < 31; i++) {
      addPair(String.valueOf(i), CellRenderer.getTypeset(3).getForeground());
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    Map<String, Color> newMap = new HashMap<String, Color>();

    if (listModel.size() > 0) {
      for (int i = 0, max = listModel.size(); i < max; i++) {
        MapElement mapElem = (MapElement) listModel.get(i);
        newMap.put(mapElem.value, mapElem.color);
      }
    }

    CellRenderer.setColorMap(newMap);
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * DOCUMENT-ME
   */
  public String getTitle() {
    return Resources.get("tree.iconadapter.colormap.title");
  }

  /**
   * DOCUMENT-ME
   */
  public void valueChanged(ListSelectionEvent e) {
    int indices[] = list.getSelectedIndices();

    if ((indices == null) || (indices.length == 0)) {
      buttons[1].setEnabled(false);
      buttons[2].setEnabled(false);

      return;
    }

    buttons[1].setEnabled(false);

    if (indices.length == 1) {
      buttons[1].setEnabled(true);
    }

    if (indices.length >= 1) {
      buttons[2].setEnabled(true);
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void keyPressed(KeyEvent e) {
    if (e.getModifiers() != 0)
      return;

    switch (e.getKeyCode()) {
    case KeyEvent.VK_ENTER:

      if (buttons[1].isEnabled()) {
        changePair();
      }

      break;

    case KeyEvent.VK_DELETE:

      if (buttons[2].isEnabled()) {
        deletePairs();
      }

      break;

    default:
      break;
    }
  }

  /**
   * Does nothing.
   */
  public void keyReleased(KeyEvent e) {
  }

  /**
   * Does nothing.
   */
  public void keyTyped(KeyEvent e) {
  }

  /**
   */
  protected class MapElement {
    protected String value;
    protected Color color;
  }

  /**
   *
   */
  protected class ColorMappingListCellRenderer extends JLabel implements ListCellRenderer {
    protected class RoundRectIcon implements Icon {
      protected Color color;
      protected boolean selected;
      protected int width;
      protected int height;

      /**
       * Creates a new RoundRectIcon object.
       */
      public RoundRectIcon(int w, int h) {
        width = w;
        height = h;
      }

      /**
       * @see javax.swing.Icon#getIconHeight()
       */
      public int getIconHeight() {
        return height;
      }

      /**
       * @see javax.swing.Icon#getIconWidth()
       */
      public int getIconWidth() {
        return width;
      }

      /**
       * DOCUMENT-ME
       */
      public void setIconWidth(int w) {
        width = w;
      }

      /**
       * DOCUMENT-ME
       */
      public void setColor(Color c) {
        color = c;
      }

      /**
       * DOCUMENT-ME
       */
      public void setSelected(boolean s) {
        selected = s;
      }

      /**
       * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
       */
      public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRoundRect(x, y, width, height, 10, 10);

        if (selected) {
          g.setColor(Color.white);
          g.drawRoundRect(x, y, width, height, 10, 10);
        }
      }
    }

    protected RoundRectIcon icon;
    protected Border border1;
    protected Border border2;

    /**
     * Creates a new ColorMappingListCellRenderer object.
     */
    public ColorMappingListCellRenderer() {
      setOpaque(false);
      icon = new RoundRectIcon(32, 24);
      setIcon(icon);
      setHorizontalTextPosition(SwingConstants.CENTER);
      setFont(getFont().deriveFont(18.0f));
      setBorder(border1 = BorderFactory.createLineBorder(Color.white, 3));
      border2 = BorderFactory.createLineBorder(Color.blue, 3);
    }

    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList l, Object value, int index, boolean sel,
        boolean foc) {
      if (value instanceof MapElement) {
        setText(((MapElement) value).value);

        Color color = ((MapElement) value).color;
        icon.setColor(color);
        setForeground(new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()));
        icon.setSelected(sel);
        icon.setIconWidth(Math.abs(l.getSize().width - 6));

        if (sel) {
          setOpaque(true);
          setBackground(Color.blue);
          setBorder(border2);
        } else {
          setOpaque(false);
          setBorder(border1);
        }
      } else {
        setText("Error!");
        setIcon(null);
      }

      return this;
    }
  }

  /**
   * @author ...
   * @version 1.0, 16.02.2008
   */
  protected class HelpDialog extends JDialog implements ActionListener {
    /**
     * Creates a new HelpDialog object.
     */
    public HelpDialog(Frame owner, String text, String buttonText) {
      super(owner, true);

      Container con = getContentPane();

      con.setLayout(new BorderLayout());

      JTextArea expl = new JTextArea(text);
      expl.setBackground(con.getBackground());
      expl.setEditable(false);
      expl.setBorder(null);
      expl.setLineWrap(true);
      expl.setWrapStyleWord(true);
      expl.setRequestFocusEnabled(false);
      expl.setFont(new JLabel().getFont());
      expl.setSelectionColor(expl.getBackground());
      expl.setSelectedTextColor(expl.getForeground());

      JScrollPane tScroll = new JScrollPane(expl);
      tScroll.setBorder(null);
      con.add(tScroll, BorderLayout.CENTER);

      JButton button = new JButton(buttonText);
      button.addActionListener(this);

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      panel.add(button);
      con.add(panel, BorderLayout.SOUTH);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      dim.width = 400;
      dim.height = 300;
      this.setSize(dim);
      setLocationRelativeTo(owner);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

}
