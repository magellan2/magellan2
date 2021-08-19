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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

/**
 * This is a container for all preferences regarding Shortcuts in Magellan.
 * 
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class DesktopShortCutPreferences extends JPanel implements PreferencesAdapter {
  private MagellanDesktop desktop;

  protected JTable table;
  protected DefaultTableModel model;
  protected Collator collator;
  protected HashMap<KeyStroke, Object> ownShortcuts;
  protected Set<KeyStroke> otherShortcuts;

  /**
   * Creates a new ShortcutList object.
   */
  public DesktopShortCutPreferences(MagellanDesktop desktop, Client client) {
    this.desktop = desktop;

    try {
      collator = Collator.getInstance(magellan.library.utils.Locales.getGUILocale());
    } catch (IllegalStateException exc) {
      collator = Collator.getInstance();
    }

    if (desktop.getShortCutListeners() != null) {
      table = getShortCutTable();

      JTextField filter = getFilter(table);
      table.addMouseListener(getMousePressedMouseListener());

      setLayout(new GridBagLayout());
      GridBagConstraints con = new GridBagConstraints();
      con.gridx = 0;
      con.gridy = 0;
      con.fill = GridBagConstraints.BOTH;
      con.weightx = 1;
      con.weighty = 1;
      con.gridwidth = 2;
      this.add(new JScrollPane(table), con);
      con.fill = GridBagConstraints.NONE;
      con.weightx = .5;
      con.weighty = 0;
      con.gridwidth = 1;
      con.gridy++;
      con.anchor = GridBagConstraints.WEST;
      JPanel searchPanel = new JPanel();
      searchPanel.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.shortcuts.search")));
      searchPanel.add(filter);
      // con.gridx++;
      con.weightx = 1;
      this.add(searchPanel, con);

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
    // find all java keystrokes

    if (desktop.getShortCutListeners() != null) {
      ownShortcuts = new HashMap<KeyStroke, Object>(desktop.getShortCutListeners());
    } else {
      ownShortcuts = new HashMap<KeyStroke, Object>();
    }

    otherShortcuts = new HashSet<KeyStroke>();
    addKeyStrokes(client, otherShortcuts);
    otherShortcuts.removeAll(ownShortcuts.keySet());
    otherShortcuts.removeAll(desktop.getShortCutTranslations().keySet());
  }

  private JTable getShortCutTable() {
    Vector<String> columns = new Vector<String>(3);
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header1"));
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header2"));
    columns.add(Resources.get("desktop.magellandesktop.prefs.shortcuts.header3"));

    model = new DefaultTableModel(getShortCutMap(), columns);

    StrokeRenderer sr = new StrokeRenderer();
    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    TableColumn column = new TableColumn();
    column.setHeaderValue(columns.get(0));
    column.setCellRenderer(sr);
    column.setCellEditor(new DefaultCellEditor(new JTextField()) {
      @Override
      public boolean isCellEditable(EventObject anEvent) {
        return false;
      }
    });
    tcm.addColumn(column);
    column = new TableColumn(1);
    column.setHeaderValue(columns.get(1));
    column.setCellRenderer(sr);
    column.setCellEditor(new DefaultCellEditor(new JTextField()) {
      @Override
      public boolean isCellEditable(EventObject anEvent) {
        return false;
      }
    });
    tcm.addColumn(column);

    column = new TableColumn(2);
    column.setHeaderValue(columns.get(2));
    column.setCellRenderer(sr);
    column.setCellEditor(new DefaultCellEditor(new JTextField()) {
      @Override
      public boolean isCellEditable(EventObject anEvent) {
        return false;
      }
    });
    tcm.addColumn(column);

    return new JTable(model, tcm);
  }

  private JTextField getFilter(JTable table) {
    RowSorter<? extends TableModel> rs = table.getRowSorter();
    if (rs == null) {
      table.setAutoCreateRowSorter(true);
      rs = table.getRowSorter();
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

  private Vector<Vector<?>> getShortCutMap() {
    Map<Object, List<KeyStroke>> listeners = new HashMap<Object, List<KeyStroke>>();
    Iterator<Entry<KeyStroke, Object>> entryIterator =
        desktop.getShortCutListeners().entrySet().iterator();

    while (entryIterator.hasNext()) {
      Entry<KeyStroke, Object> entry = entryIterator.next();
      Object value = entry.getValue();

      if (!listeners.containsKey(value)) {
        listeners.put(value, new LinkedList<KeyStroke>());
      }

      // try to find a translation
      KeyStroke oldStroke = entry.getKey();
      KeyStroke newStroke = desktop.findTranslation(oldStroke);

      if (newStroke != null) {
        listeners.get(value).add(newStroke);
      } else {
        listeners.get(value).add(oldStroke);
      }
    }

    Vector<Vector<?>> data = new Vector<Vector<?>>();

    List<Object> listenerList = new LinkedList<Object>(listeners.keySet());

    Collections.sort(listenerList, new ListenerComparator());

    for (Object key : listenerList) {
      ShortcutListener sl = null;
      if (key instanceof ShortcutListener) {
        sl = (ShortcutListener) key;
      }

      Object category = getShortCutCategory(key);

      List<KeyStroke> keyStrokeList = listeners.get(key);

      Collections.sort(keyStrokeList, new KeyStrokeComparator());

      for (KeyStroke obj : keyStrokeList) {
        Object description = getShortCutDescription(obj, sl);

        Vector<Object> row = new Vector<Object>(3);
        row.add(obj);
        row.add(description);
        row.add(category);
        data.add(row);
      }
    }

    return data;
  }

  private Object getShortCutCategory(Object key) {
    Object category = null;
    if (key == null)
      return "null";
    if (key instanceof ShortcutListener) {
      ShortcutListener sl = (ShortcutListener) key;
      category = sl.getListenerDescription();

      if (category == null) {
        category = sl;
      }
    } else if (key instanceof Action) {
      Action action = (Action) key;
      category = action.getValue(Action.SHORT_DESCRIPTION);
      if (category == null) {
        category = action.getValue("tooltip");
      }
    }
    if (category == null) {
      category = key.toString();
    }
    return category.toString();
  }

  private String getShortCutDescription(KeyStroke key, ShortcutListener sl) {
    if (sl == null)
      return key.toString();

    if (desktop.getShortCutTranslations().containsKey(key)) {
      key = desktop.getTranslation(key);
    }

    String description;
    try {
      description = sl.getShortcutDescription(key);

      if (description == null) {
        description = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
      }
    } catch (RuntimeException re) {
      description = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
    }
    return description;
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
    // TODO
    // as long as shortcuts cannot be changed from outside the preferences, this is safe to not
    // implement.
  }

  /**
   * 
   */
  public void applyPreferences() {
    // shortcuts are changed immediately on change
  }

  /**
   * 
   */
  public Component getComponent() {
    return this;
  }

  /**
   * 
   */
  public String getTitle() {
    return Resources.get("desktop.magellandesktop.prefs.shortcuts.title");
  }

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

  protected static class ListTableModel extends DefaultTableModel {
    /**
     * Creates a new ListTableModel object.
     */
    public ListTableModel(Object data[][], Object columns[]) {
      super(data, columns);
    }

    /**
     * 
     */
    @Override
    public boolean isCellEditable(int r, int c) {
      return false;
    }
  }

  protected String getKeyStroke(KeyStroke stroke) {
    String s = null;

    if (stroke.getModifiers() != 0) {
      s =
          InputEvent.getModifiersExText(stroke.getModifiers()) + " + "
              + KeyEvent.getKeyText(stroke.getKeyCode());
    } else {
      s = KeyEvent.getKeyText(stroke.getKeyCode());
    }

    return s;
  }

  protected MouseListener getMousePressedMouseListener() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() > 0) {
          Point p = mouseEvent.getPoint();

          if ((table.columnAtPoint(p) == 0) && (table.rowAtPoint(p) >= 0)) {
            int row = table.rowAtPoint(p);
            Object value = table.getValueAt(row, 0);

            if (value instanceof KeyStroke) {
              editStroke((KeyStroke) value, row);
            }
          }
        }
      }
    };
  }

  protected void editStroke(KeyStroke stroke, int row) {
    Component top = getTopLevelAncestor();
    TranslateStroke td = null;

    if (table.getValueAt(row, 0) instanceof KeyStroke) {
      if (top instanceof Frame) {
        td = new TranslateStroke((Frame) top);
      } else if (top instanceof Dialog) {
        td = new TranslateStroke((Dialog) top);
      } else
        throw new RuntimeException("top level ancestor is neither frame nor dialog.");

      td.setVisible(true);

      KeyStroke newStroke = td.getStroke();

      if (newStroke != null) {
        changeStroke(newStroke, stroke, row);
      }
    }
  }

  private boolean changeStroke(KeyStroke newStroke, KeyStroke stroke, int row) {
    if ((newStroke != null) && !newStroke.equals(stroke)) {
      if (ownShortcuts.get(newStroke) != null) {
        KeyStroke translation = desktop.getTranslation(newStroke);
        if (translation != null) {
          ;//
        }
        Object listener = desktop.getShortCutListeners().get(newStroke);
        String description;
        if (listener instanceof ShortcutListener || listener == null) {
          description = getShortCutDescription(newStroke, (ShortcutListener) listener);
        } else {
          description = listener.toString();
        }

        JOptionPane.showMessageDialog(this, Resources.get(
            "desktop.magellandesktop.prefs.shortcuts.error", description, getShortCutCategory(listener)));
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
          ownShortcuts.put(newStroke, ownShortcuts.remove(stroke));
          if (desktop.getShortCutTranslations().containsKey(stroke)) {
            KeyStroke oldStroke = desktop.getShortCutTranslations().get(stroke);
            desktop.removeTranslation(stroke);
            stroke = oldStroke;
          }

          if (desktop.getShortCutListeners().containsKey(stroke)
              && (desktop.getShortCutListeners().get(stroke) instanceof Action)) {
            ((Action) desktop.getShortCutListeners().get(stroke))
                .putValue("accelerator", newStroke);
          }

          if (!newStroke.equals(stroke)) {
            desktop.registerTranslation(newStroke, stroke);
          }

          if (row >= 0) {
            table.setValueAt(newStroke, row, 0);
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
    protected KeyStroke stroke = null;

    /**
     * Creates a new TranslateStroke object.
     */
    public TranslateStroke(Frame parent) {
      super(parent, true);
      init();
    }

    /**
     * Creates a new TranslateStroke object.
     */
    public TranslateStroke(Dialog parent) {
      super(parent, true);
      init();
    }

    protected void init() {
      JPanel con = new JPanel(new BorderLayout());
      JLabel label = new JLabel(
          Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.label"));
      con.add(label, BorderLayout.NORTH);
      text = new KeyTextField();
      label.setLabelFor(text);
      con.add(text, BorderLayout.CENTER);

      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JButton ok = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.ok"));
      buttons.add(ok);
      ok.addActionListener(this);
      cancel = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.cancel"));
      buttons.add(cancel);
      cancel.addActionListener(this);
      con.add(buttons, BorderLayout.SOUTH);
      setContentPane(con);
      pack();
      this.setSize(getWidth() + 5, getHeight() + 5);
      setLocationRelativeTo(getParent());
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      if (actionEvent.getSource() != cancel) {
        if (text.getKeyCode() != 0) {
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

    /**
     * 
     */
    public void init(int modifiers, int key) {
      this.key = key;
      this.modifiers = modifiers;

      String s = InputEvent.getModifiersExText(modifiers);

      if ((s != null) && (s.length() > 0)) {
        s += ('+' + KeyEvent.getKeyText(key));
      } else {
        s = KeyEvent.getKeyText(key);
      }

      setText(s);
    }

    /**
     * 
     */
    public void keyReleased(KeyEvent p1) {
      // maybe should delete any input if there's no "stable"(non-modifying) key
    }

    /**
     * 
     */
    public void keyPressed(KeyEvent p1) {
      modifiers = p1.getModifiersEx();
      key = p1.getKeyCode();

      // avoid double string
      if ((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) || (key == KeyEvent.VK_ALT)
          || (key == KeyEvent.VK_ALT_GRAPH)) {
        int xored = 0;

        switch (key) {
        case KeyEvent.VK_SHIFT:
          xored = InputEvent.SHIFT_DOWN_MASK;

          break;

        case KeyEvent.VK_CONTROL:
          xored = InputEvent.CTRL_DOWN_MASK;

          break;

        case KeyEvent.VK_ALT:
          xored = InputEvent.ALT_DOWN_MASK;

          break;

        case KeyEvent.VK_ALT_GRAPH:
          xored = InputEvent.ALT_GRAPH_DOWN_MASK;

          break;
        }

        modifiers ^= xored;
      }

      String s = InputEvent.getModifiersExText(modifiers);

      if ((s != null) && (s.length() > 0)) {
        s += ('+' + KeyEvent.getKeyText(key));
      } else {
        s = KeyEvent.getKeyText(key);
      }

      setText(s);
      p1.consume();
    }

    /**
     * 
     */
    public void keyTyped(KeyEvent p1) {
      // keyPressed used
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
        textField.init(((KeyStroke) oldValue).getModifiers(), ((KeyStroke) oldValue).getKeyCode());
      }
      super.cancelCellEditing();
    }

    @Override
    public boolean stopCellEditing() {
      if (oldValue instanceof KeyStroke)
        if (!changeStroke((KeyStroke) getCellEditorValue(), (KeyStroke) oldValue, -1)) {
          textField
              .init(((KeyStroke) oldValue).getModifiers(), ((KeyStroke) oldValue).getKeyCode());
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
        textField.init(((KeyStroke) value).getModifiers(), ((KeyStroke) value).getKeyCode());
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

      if (value instanceof KeyStroke && column == 0) {
        if (otherShortcuts.contains(value)) {
          setFont(bold);
          setForeground(Color.RED);
        } else {
          setFont(norm);
          setForeground(Color.BLACK);
        }
        setText(getKeyStroke((KeyStroke) value));
      } else if (column == 0) {
        setFont(bold);
      } else if (column == 2) {
        column = 2;
      }

      return this;
    }
  }
}
