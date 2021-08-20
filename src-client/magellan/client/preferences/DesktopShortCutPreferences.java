// class magellan.client.preferences.DesktopShortCutPreferences
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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.preferences.DesktopShortCutPreferences.ShortcutModel.StrokeInfo;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This is a container for all preferences regarding Shortcuts in Magellan.
 * 
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class DesktopShortCutPreferences extends JPanel implements PreferencesAdapter {
  private static Logger log = Logger.getInstance(DesktopShortCutPreferences.class);

  private static final boolean SHOW_SYSTEM_KEYS = false;

  /**
   * Table model for shortcuts
   */
  public static class ShortcutModel extends AbstractTableModel {

    /**
     * records relevant info about a KeyStroke
     */
    public static class StrokeInfo implements Comparable<StrokeInfo> {

      private KeyStroke stroke;
      private KeyStroke originalStroke;
      private KeyStroke strokeId;
      private String category;
      private String description;

      protected StrokeInfo(KeyStroke stroke, KeyStroke strokeId, ShortcutListener sl) {
        this.stroke = stroke;
        originalStroke = stroke;
        this.strokeId = strokeId;

        category = sl.getListenerDescription();

        if (category == null) {
          category = sl.toString();
        }
        try {
          description = sl.getShortcutDescription(stroke);
          if (description == null) {
            description = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
          }
        } catch (RuntimeException re) {
          description = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
        }
      }

      protected StrokeInfo(KeyStroke key, KeyStroke translation, Action action) {
        stroke = key;
        originalStroke = key;
        strokeId = translation;

        description = (String) action.getValue(Action.SHORT_DESCRIPTION);
        if (description == null) {
          description = (String) action.getValue("tooltip");
        }
        if (description == null) {
          description = (String) action.getValue(Action.NAME);
        }

        if (description == null) {
          description = action.toString();
        }
        category = "";
      }

      KeyStrokeComparator strokeCmp = new KeyStrokeComparator();

      public int compareTo(StrokeInfo o) {
        if (o == null)
          return -1;
        int cmp = category.compareTo(o.category);
        if (cmp != 0)
          return cmp;
        cmp = description.compareTo(o.description);
        if (cmp != 0)
          return cmp;
        return strokeCmp.compare(stroke, o.stroke);
      }

    }

    private ArrayList<StrokeInfo> shortcuts;

    protected ShortcutModel() {
      shortcuts = new ArrayList<StrokeInfo>();
      // initialized later
    }

    protected void init(MagellanDesktop desktop) {
      shortcuts.clear();

      for (Entry<KeyStroke, Object> entry : desktop.getShortCutListeners().entrySet()) {

        KeyStroke keyId = entry.getKey();
        KeyStroke currentKey = desktop.findTranslation(keyId);
        if (currentKey == null) {
          currentKey = keyId;
        }

        if (entry.getValue() instanceof ShortcutListener) {
          shortcuts.add(new StrokeInfo(currentKey, keyId, (ShortcutListener) entry.getValue()));
        } else if (entry.getValue() instanceof Action) {
          shortcuts.add(new StrokeInfo(currentKey, keyId, (Action) entry.getValue()));
        } else {
          log.error("unknown shortcut listener for " + keyId);
        }
      }
      Collections.sort(shortcuts);
      fireTableDataChanged();
    }

    public int getRowCount() {
      return shortcuts.size();
    }

    public int getColumnCount() {
      return 3;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      StrokeInfo shortcut = shortcuts.get(rowIndex);
      switch (columnIndex) {
      case 0:
        return shortcut.stroke;
      case 1:
        return shortcut.description;
      case 2:
        return shortcut.category;
      // case 3:
      // return shortcut.strokeId;
      default:
        throw new IndexOutOfBoundsException(columnIndex);
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      StrokeInfo shortcut = shortcuts.get(rowIndex);
      switch (columnIndex) {
      case 0:
        shortcut.stroke = (KeyStroke) aValue;
        break;
      default:
        throw new IndexOutOfBoundsException(columnIndex);
      }
      fireTableCellUpdated(rowIndex, columnIndex);
    }

    private StrokeInfo getStrokeInfo(int row) {
      return shortcuts.get(row);
    }

    protected void setDefaults() {
      for (StrokeInfo info : shortcuts) {
        info.stroke = info.strokeId;
      }
      fireTableDataChanged();
    }
  }

  private MagellanDesktop desktop;

  protected JTable table;
  protected ShortcutModel model;
  protected Collator collator;
  protected HashMap<KeyStroke, Integer> ownShortcuts = new HashMap<KeyStroke, Integer>();
  protected Set<KeyStroke> otherShortcuts = new HashSet<KeyStroke>();

  private Client client;

  /**
   * Creates a new ShortcutList object.
   */
  public DesktopShortCutPreferences(MagellanDesktop desktop, Client client) {
    this.desktop = desktop;
    this.client = client;

    try {
      collator = Collator.getInstance(magellan.library.utils.Locales.getGUILocale());
    } catch (IllegalStateException exc) {
      collator = Collator.getInstance();
    }

    table = getShortCutTable();

    JTextField filter = getFilter(table);
    filter.setMaximumSize(filter.getPreferredSize());
    table.addMouseListener(getMousePressedMouseListener());

    Box searchPanel = Box.createHorizontalBox();
    // searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));
    searchPanel.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.shortcuts.search")));
    searchPanel.add(filter);
    searchPanel.add(Box.createHorizontalGlue());
    searchPanel.add(new JButton(new AbstractAction(Resources.get(
        "desktop.magellandesktop.prefs.shortcuts.dialog.defaults")) {
      public void actionPerformed(ActionEvent e) {
        setDefaults();
      }
    }));

    setLayout(new GridBagLayout());
    GridBagConstraints con = new GridBagConstraints();
    con.gridx = 0;
    con.gridy = 0;
    con.fill = GridBagConstraints.BOTH;
    con.weightx = 1;
    con.weighty = 1;
    con.gridwidth = 2;
    this.add(new JScrollPane(table), con);
    con.fill = GridBagConstraints.HORIZONTAL;
    con.weightx = .5;
    con.weighty = 0;
    con.gridwidth = 1;
    con.gridy++;
    con.anchor = GridBagConstraints.WEST;
    // con.gridx++;
    con.weightx = 1;
    this.add(searchPanel, con);

    if (SHOW_SYSTEM_KEYS) {
      con.gridy++;
      con.gridx = 0;
      con.weightx = 0;
      con.fill = GridBagConstraints.NONE;
      this.add(new JButton(new AbstractAction(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.action")) {
        public void actionPerformed(ActionEvent e) {
          new InformDialog(client).setVisible(true);
        }
      }), con);
    }
  }

  protected void setDefaults() {
    model.setDefaults();
  }

  private JTable getShortCutTable() {
    Vector<String> columns = new Vector<String>(3);
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header1"));
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header2"));
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header3"));
    // columns.add("translation");

    model = new ShortcutModel();

    StrokeRenderer sr = new StrokeRenderer();
    DefaultTableColumnModel tcm = new DefaultTableColumnModel();

    for (int i = 0; i < columns.size(); ++i) {
      TableColumn column = new TableColumn(i);
      column.setHeaderValue(columns.get(i));
      column.setCellRenderer(sr);
      column.setCellEditor(new DefaultCellEditor(new JTextField()) {
        @Override
        public boolean isCellEditable(EventObject anEvent) {
          return false;
        }
      });
      tcm.addColumn(column);
    }

    return new JTable(model, tcm);
  }

  private JTextField getFilter(JTable aTable) {
    RowSorter<? extends TableModel> rs = aTable.getRowSorter();
    if (rs == null) {
      aTable.setAutoCreateRowSorter(true);
      rs = aTable.getRowSorter();
    }

    TableRowSorter<? extends TableModel> rowSorter =
        (rs instanceof TableRowSorter) ? (TableRowSorter<? extends TableModel>) rs : null;

    if (rowSorter == null)
      throw new RuntimeException("Cannot find appropriate rowSorter: " + rs);

    final JTextField tf = new JTextField(15);

    tf.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update(e);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        update(e);
      }

      private void update(DocumentEvent e) {
        String text = tf.getText().trim();
        if (text.length() == 0) {
          rowSorter.setRowFilter(null);
        } else {
          Matcher matcher = Pattern.compile("(?i)" + text).matcher("");

          rowSorter.setRowFilter(new RowFilter<TableModel, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
              if (entry.getValue(1) == null)
                return true;

              matcher.reset(entry.getStringValue(0));
              if (matcher.find())
                return true;
              matcher.reset(entry.getStringValue(1));
              if (matcher.find())
                return true;
              return false;
            }
          });
        }
      }
    });

    return tf;
  }

  protected void addKeyStrokes(Component c, Set<KeyStroke> set) {
    if (c instanceof JComponent) {
      KeyStroke str[] = ((JComponent) c).getRegisteredKeyStrokes();

      if ((str != null) && (str.length > 0)) {
        for (KeyStroke element : str) {
          set.add(element);
        }
      }
    }

    if (c instanceof Container) {
      Container con = (Container) c;

      if (con.getComponentCount() > 0) {
        for (int i = 0; i < con.getComponentCount(); i++) {
          addKeyStrokes(con.getComponent(i), set);
        }
      }
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    model.init(desktop);

    addShortcuts();
  }

  private void addShortcuts() {
    // find all java keystrokes
    ownShortcuts.clear();

    otherShortcuts.clear();
    addKeyStrokes(client, otherShortcuts);

    for (int i = 0; i < model.getRowCount(); ++i) {
      KeyStroke stroke = (KeyStroke) model.getValueAt(i, 0);
      ownShortcuts.put(stroke, i);
      otherShortcuts.remove(stroke);
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    for (int row = 0; row < model.getRowCount(); ++row) {
      StrokeInfo strokeInfo = model.getStrokeInfo(row);
      if (strokeInfo.originalStroke != strokeInfo.stroke) {
        desktop.changeTranslation(strokeInfo.strokeId, strokeInfo.originalStroke, strokeInfo.stroke);
      }
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("desktop.magellandesktop.prefs.shortcuts.title");
  }

  /**
   * compares listener by description
   */
  protected class ListenerComparator implements Comparator<Object> {
    /**
     * 
     */
    public int compare(Object o1, Object o2) {
      String s1 = null;
      String s2 = null;

      if (o1 instanceof ShortcutListener) {
        s1 = ((ShortcutListener) o1).getListenerDescription();
      }

      if (s1 == null) {
        s1 = o1.toString();
      }

      if (o2 instanceof ShortcutListener) {
        s2 = ((ShortcutListener) o2).getListenerDescription();
      }

      if (s2 == null) {
        s2 = o2.toString();
      }

      if ((s1 != null) && (s2 != null))
        return collator.compare(s1, s2);
      else if ((s1 == null) && (s2 != null))
        return -1;
      else if ((s1 != null) && (s2 == null))
        return 1;

      return 0;
    }
  }

  /**
   * compare KeyStrokes by number of modifiers / modifiers / keycode
   */
  protected static class KeyStrokeComparator implements Comparator<KeyStroke> {
    /**
     * 
     */
    public int compare(KeyStroke o1, KeyStroke o2) {
      KeyStroke k1 = o1;
      KeyStroke k2 = o2;

      if (k1.getModifiers() != k2.getModifiers()) {
        int i1 = k1.getModifiers();
        int i2 = 0;
        int j1 = k2.getModifiers();
        int j2 = 0;

        while (i1 != 0) {
          if ((i1 % 2) != 0) {
            i2++;
          }

          i1 /= 2;
        }

        while (j1 != 0) {
          if ((j1 % 2) != 0) {
            j2++;
          }

          j1 /= 2;
        }

        if (i2 != j2)
          return i2 - j2;

        return k1.getModifiers() - k2.getModifiers();
      }

      return k1.getKeyCode() - k2.getKeyCode();
    }
  }

  protected static String getKeyStroke(KeyStroke stroke) {
    return getKeyStroke(stroke.getModifiers(), stroke.getKeyCode());
  }

  protected static String getKeyStroke(int modifiers, int keyCode) {

    if (keyCode == KeyEvent.VK_UNDEFINED || isModifier(keyCode))
      return InputEvent.getModifiersExText(modifiers);
    else if (modifiers != 0)
      return InputEvent.getModifiersExText(modifiers) + " + "
          + KeyEvent.getKeyText(keyCode);
    else
      return KeyEvent.getKeyText(keyCode);
  }

  private static boolean isModifier(int key) {
    return ((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) || (key == KeyEvent.VK_ALT)
        || (key == KeyEvent.VK_ALT_GRAPH));
  }

  protected MouseListener getMousePressedMouseListener() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() > 0) {
          Point p = mouseEvent.getPoint();
          int col = table.columnAtPoint(p);
          int modelCol = table.convertColumnIndexToModel(col);
          if (modelCol == 0 && table.rowAtPoint(p) >= 0) {
            int row = table.rowAtPoint(p);
            Object value = table.getValueAt(row, col);

            if (value instanceof KeyStroke) {
              editStroke((KeyStroke) value, table.convertRowIndexToModel(row));
            }
          }
        }
      }
    };
  }

  protected void editStroke(KeyStroke stroke, int modelRow) {
    Component top = getTopLevelAncestor();
    TranslateStroke td = null;

    if (model.getValueAt(modelRow, 0) instanceof KeyStroke) {
      if (top instanceof Frame) {
        td = new TranslateStroke((Frame) top, stroke);
      } else if (top instanceof Dialog) {
        td = new TranslateStroke((Dialog) top, stroke);
      } else
        throw new RuntimeException("top level ancestor is neither frame nor dialog.");
      td.setDefault(model.getStrokeInfo(modelRow).strokeId);

      KeyStroke newStroke = stroke;
      while (newStroke != null) {
        td.setVisible(true);
        newStroke = td.getStroke();

        if (newStroke != null) {
          if (changeStroke(newStroke, stroke, modelRow)) {
            newStroke = null;
          }
        }
      }
    }
  }

  private boolean changeStroke(KeyStroke newStroke, KeyStroke stroke, int modelRow) {
    if ((newStroke != null) && !newStroke.equals(stroke)) {
      if (ownShortcuts.get(newStroke) != null) {
        JOptionPane.showMessageDialog(this, Resources.get(
            "desktop.magellandesktop.prefs.shortcuts.error",
            model.getValueAt(ownShortcuts.get(newStroke), 1),
            table.getValueAt(ownShortcuts.get(newStroke), 2)));
        return false;
      } else {
        boolean doIt = true;

        if (otherShortcuts.contains(newStroke)) {
          int res =
              JOptionPane.showConfirmDialog(this, Resources
                  .get("desktop.magellandesktop.prefs.shortcuts.warning"), Resources
                      .get("desktop.magellandesktop.prefs.shortcuts.warningtitle"),
                  JOptionPane.YES_NO_OPTION);
          doIt = (res == JOptionPane.YES_OPTION);
        }

        if (doIt) {
          ownShortcuts.remove(model.getValueAt(modelRow, 0));
          ownShortcuts.put(newStroke, modelRow);

          if (modelRow >= 0) {
            model.setValueAt(newStroke, modelRow, 0);
          }
        }
        return doIt;
      }
    }
    return false;
  }

  protected class InformDialog extends JDialog implements ActionListener {
    /**
     * Creates a new InformDialog object.
     */
    public InformDialog(Frame parent) {
      super(parent, true);
      init();
    }

    /**
     * Creates a new InformDialog object.
     */
    public InformDialog(Dialog parent) {
      super(parent, true);
      init();
    }

    protected void init() {
      JPanel con = new JPanel(new BorderLayout());

      StringBuffer buf = new StringBuffer();

      Object args[] = { Integer.valueOf(otherShortcuts.size()) };
      buf.append(MessageFormat.format(Resources
          .get("desktop.magellandesktop.prefs.shortcuts.others"), args));
      buf.append('\n');
      buf.append('\n');

      Iterator<KeyStroke> it = otherShortcuts.iterator();

      while (it.hasNext()) {
        buf.append(getKeyStroke(it.next()));

        if (it.hasNext()) {
          buf.append(", ");
        }
      }

      JTextArea shortcutInfo = new JTextArea(buf.toString());
      shortcutInfo.setColumns(80);
      shortcutInfo.setRows(20);
      shortcutInfo.setEditable(false);
      shortcutInfo.setLineWrap(true);
      shortcutInfo.setWrapStyleWord(true);
      con.add(new JScrollPane(shortcutInfo), BorderLayout.CENTER);

      JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JButton ok = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.ok"));
      ok.addActionListener(this);
      button.add(ok);
      con.add(button, BorderLayout.SOUTH);
      setContentPane(con);

      pack();
      setLocationRelativeTo(getParent());
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      setVisible(false);
    }
  }

  protected static class TranslateStroke extends JDialog implements ActionListener {
    protected KeyTextField text;
    protected JButton cancel;
    private JButton reset;
    protected KeyStroke stroke;
    private KeyStroke defaultStroke;

    /**
     * Creates a new TranslateStroke object.
     */
    public TranslateStroke(Frame parent, KeyStroke stroke) {
      super(parent, true);
      init(stroke);
    }

    public void setDefault(KeyStroke stroke) {
      defaultStroke = stroke;
      reset.setEnabled(stroke != null);
    }

    /**
     * Creates a new TranslateStroke object.
     */
    public TranslateStroke(Dialog parent, KeyStroke stroke) {
      super(parent, true);
      init(stroke);
    }

    protected void init(KeyStroke stroke) {
      JPanel con = new JPanel(new BorderLayout());
      JLabel label = new JLabel(
          Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.label"));
      con.add(label, BorderLayout.NORTH);
      text = new KeyTextField();
      text.setText(stroke);
      text.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getModifiersEx() == 0 &&
              (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            terminate(e.getKeyCode() == KeyEvent.VK_ENTER);
          }
        }
      });
      label.setLabelFor(text);
      con.add(text, BorderLayout.CENTER);

      Box buttons = Box.createHorizontalBox();
      JButton ok = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.ok"));
      buttons.add(ok);
      ok.addActionListener(this);
      cancel = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.cancel"));
      buttons.add(cancel);
      cancel.addActionListener(this);
      buttons.add(Box.createHorizontalGlue());
      reset = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.default"));
      reset.setEnabled(defaultStroke != null);
      buttons.add(reset);
      reset.addActionListener(this);
      con.add(buttons, BorderLayout.SOUTH);
      setContentPane(con);
      pack();
      this.setSize(getWidth() + 5, getHeight() + 5);
      setLocationRelativeTo(getParent());
    }

    protected void terminate(boolean success) {
      if (success) {
        if (text.getKeyCode() != KeyEvent.VK_UNDEFINED) {
          stroke = KeyStroke.getKeyStroke(text.getKeyCode(), text.getModifiers());
        }
      } else {
        stroke = null;
      }

      setVisible(false);
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      if (actionEvent.getSource() == reset) {
        text.init(defaultStroke);
      } else {
        terminate(actionEvent.getSource() != cancel);
      }
    }

    /**
     * Returns the user input.
     */
    public KeyStroke getStroke() {
      return stroke;
    }

  }

  private static class KeyTextField extends JTextField implements KeyListener {
    protected int modifiers = 0;
    protected int key = 0;

    /**
     * Creates a new KeyTextField object.
     */
    public KeyTextField() {
      super(20);
      addKeyListener(this);
    }

    public void setText(KeyStroke stroke) {
      setText(stroke.getModifiers(), stroke.getKeyCode());
    }

    /**
     * 
     */
    public void init(KeyStroke stroke) {
      key = stroke.getKeyCode();
      modifiers = stroke.getModifiers();

      setText(modifiers, key);
    }

    private void setText(int modifiers, int key) {
      String s = DesktopShortCutPreferences.getKeyStroke(modifiers, key);
      setText(s);
    }

    /**
     * 
     */
    public void keyReleased(KeyEvent p1) {
      // delete any input if there's no "stable"(non-modifying) key
      if (isModifier(key)) {
        modifiers = KeyEvent.VK_UNDEFINED;
        key = KeyEvent.VK_UNDEFINED;
        setText(modifiers, key);
      }
    }

    /**
     * 
     */
    public void keyPressed(KeyEvent p1) {
      if (p1.getModifiersEx() == 0 &&
          (p1.getKeyCode() == KeyEvent.VK_ENTER || p1.getKeyCode() == KeyEvent.VK_ESCAPE))
        // ignore ENTER
        return;
      modifiers = p1.getModifiersEx();
      key = p1.getKeyCode();

      setText(modifiers, key);
      p1.consume();
    }

    /**
     * 
     */
    public void keyTyped(KeyEvent p1) {
      p1.consume();
    }

    /**
     * To allow "tab" as a key.
     */
    @Override
    public boolean isManagingFocus() {
      return true;
    }

    /**
     * 
     */
    public int getKeyCode() {
      return key;
    }

    /**
     * 
     */
    public int getModifiers() {
      return modifiers;
    }

    public KeyStroke getKeyStroke() {
      return KeyStroke.getKeyStroke(getKeyCode(), getModifiers());
    }
  }

  /**
   * A CellEditor component for editing key strokes in JTable cells.
   */
  protected class KeyStrokeCellEditor extends AbstractCellEditor implements TableCellEditor {

    private KeyTextField textField;
    private Object oldValue;
    private JTextField dummy = new JTextField();

    public KeyStrokeCellEditor() {
      textField = new KeyTextField();
    }

    @Override
    public void cancelCellEditing() {
      if (oldValue instanceof KeyStroke) {
        textField.init((KeyStroke) oldValue);
      }
      super.cancelCellEditing();
    }

    @Override
    public boolean stopCellEditing() {
      if (oldValue instanceof KeyStroke)
        if (!changeStroke((KeyStroke) getCellEditorValue(), (KeyStroke) oldValue, -1)) {
          textField.init((KeyStroke) oldValue);
        }

      return super.stopCellEditing();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
      return super.isCellEditable(anEvent);
    }

    /**
     * @see javax.swing.DefaultCellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() {
      if (oldValue instanceof KeyStroke)
        return textField.getKeyStroke();
      else
        return oldValue;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
      if (value instanceof KeyStroke) {
        oldValue = value;
        textField.init((KeyStroke) value);
        return textField;
      } else {
        oldValue = value;
        dummy.setEditable(false);
        dummy.setText(oldValue.toString());
        return dummy;
      }
    }

  }

  protected class StrokeRenderer extends DefaultTableCellRenderer {
    protected Font bold;
    protected Font norm;

    /**
     * Creates a new StrokeRenderer object.
     */
    public StrokeRenderer() {
      norm = getFont();
      bold = getFont().deriveFont(Font.BOLD);
    }

    /**
     * 
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      setFont(norm);
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      int modelColumn = table.convertColumnIndexToModel(column);

      if (value instanceof KeyStroke && modelColumn == 0) {
        if (otherShortcuts.contains(value)) {
          setFont(bold);
          setForeground(Color.RED);
        } else {
          setForeground(Color.BLACK);
        }
        setText(getKeyStroke((KeyStroke) value));
      } else if (modelColumn == 0) {
        setFont(bold);
      }

      return this;
    }
  }
}
