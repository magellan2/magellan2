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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
 public class DesktopShortCutPreferences extends JPanel implements PreferencesAdapter, ActionListener {
  private MagellanDesktop desktop;
  private Client client;
  private Properties settings;
  
  protected JTable table;
  protected DefaultTableModel model;
  protected Collator collator;
  protected Set<KeyStroke> ownShortcuts;
  protected Set<KeyStroke> otherShortcuts;

  /**
   * Creates a new ShortcutList object.
   */
  public DesktopShortCutPreferences(MagellanDesktop desktop, Client client, Properties settings) {
    this.desktop = desktop;
    this.client = client;
    this.settings = settings;
    
    try {
      collator = Collator.getInstance(magellan.library.utils.Locales.getGUILocale());
    } catch(IllegalStateException exc) {
      collator = Collator.getInstance();
    }

    if(desktop.getShortCutListeners() != null) {
      Object columns[] = {
                   Resources.get("desktop.magellandesktop.prefs.shortcuts.header1"),
                   Resources.get("desktop.magellandesktop.prefs.shortcuts.header2")
                 };

      Map<Object,List<KeyStroke>> listeners = new HashMap<Object, List<KeyStroke>>();
      Iterator it = desktop.getShortCutListeners().entrySet().iterator();

      while(it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();
        Object value = entry.getValue();

        if(!listeners.containsKey(value)) {
          listeners.put(value, new LinkedList<KeyStroke>());
        }

        // try to find a translation
        KeyStroke oldStroke = (KeyStroke) entry.getKey();
        KeyStroke newStroke = desktop.findTranslation(oldStroke);

        if(newStroke != null) {
          listeners.get(value).add(newStroke);
        } else {
          listeners.get(value).add(oldStroke);
        }
      }

      Object data[][] = new Object[desktop.getShortCutListeners().size() + listeners.size()][2];

      List<Object> list2 = new LinkedList<Object>(listeners.keySet());

      Collections.sort(list2, new ListenerComparator());

      it = list2.iterator();

      int i = 0;

      while(it.hasNext()) {
        Object key = it.next();
        ShortcutListener sl = null;

        if(key instanceof ShortcutListener) {
          sl = (ShortcutListener) key;
          data[i][0] = sl.getListenerDescription();

          if(data[i][0] == null) {
            data[i][0] = sl;
          }
        } else {
          data[i][0] = key;
        }

        data[i][1] = null;

        i++;

        List<KeyStroke> list = listeners.get(key);

        Collections.sort(list, new KeyStrokeComparator());

        Iterator it2 = list.iterator();

        while(it2.hasNext()) {
          Object obj = it2.next();
          data[i][0] = obj;

          if(sl != null) {
            if(desktop.getShortCutTranslations().containsKey(obj)) {
              obj = desktop.getTranslation((KeyStroke) obj);
            }

            try {
              data[i][1] = sl.getShortcutDescription(obj);

              if(data[i][1] == null) {
                data[i][1] = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
              }
            } catch(RuntimeException re) {
              data[i][1] = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
            }
          } else {
            data[i][1] = key;
          }

          i++;
        }
      }

      model = new DefaultTableModel(data, columns);

      StrokeRenderer sr = new StrokeRenderer();
      DefaultTableColumnModel tcm = new DefaultTableColumnModel();
      TableColumn column = new TableColumn();
      column.setHeaderValue(columns[0]);
      column.setCellRenderer(sr);
      column.setCellEditor(null);
      tcm.addColumn(column);
      column = new TableColumn(1);
      column.setHeaderValue(columns[1]);
      column.setCellRenderer(sr);
      column.setCellEditor(null);
      tcm.addColumn(column);

      table = new JTable(model, tcm);
      this.setLayout(new BorderLayout());
      this.add(new JScrollPane(table), BorderLayout.CENTER);
      table.addMouseListener(getMousePressedMouseListener());
    }

    // find all java keystrokes
    Set<KeyStroke> set = new HashSet<KeyStroke>();
    Collection<Frame> desk = new LinkedList<Frame>();
    desk.add(client);

    Iterator<Frame> it1 = desk.iterator();

    while(it1.hasNext()) {
      addKeyStrokes(it1.next(), set);
    }

    Set<KeyStroke> set2 = new HashSet<KeyStroke>(desktop.getShortCutListeners().keySet());
    Iterator<KeyStroke> it2 = desktop.getShortCutTranslations().keySet().iterator();

    while(it2.hasNext()) {
      set2.remove(desktop.getShortCutTranslations().get(it2.next()));
    }

    ownShortcuts = set2;
    ownShortcuts.addAll(desktop.getShortCutTranslations().keySet());
    set.removeAll(set2);
    set.removeAll(desktop.getShortCutTranslations().keySet());
    otherShortcuts = set;

//    JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
//    JButton help = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.help"));
//    help.addActionListener(this);
//    south.add(help);
//    this.add(south, BorderLayout.SOUTH);
  }

  protected void addKeyStrokes(Component c, Set<KeyStroke> set) {
    if(c instanceof JComponent) {
      KeyStroke str[] = ((JComponent) c).getRegisteredKeyStrokes();

      if((str != null) && (str.length > 0)) {
        for(int i = 0; i < str.length; i++) {
          set.add(str[i]);
        }
      }
    }

    if(c instanceof Container) {
      Container con = (Container) c;

      if(con.getComponentCount() > 0) {
        for(int i = 0; i < con.getComponentCount(); i++) {
          addKeyStrokes(con.getComponent(i), set);
        }
      }
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   * @deprecated TODO: implement it!
   */
  public void initPreferences() {
      // TODO: implement it
  }
        
  /**
   * 
   */
  public void applyPreferences() {
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

      if(o1 instanceof ShortcutListener) {
        s1 = ((ShortcutListener) o1).getListenerDescription();
      }

      if(s1 == null) {
        s1 = o1.toString();
      }

      if(o2 instanceof ShortcutListener) {
        s2 = ((ShortcutListener) o2).getListenerDescription();
      }

      if(s2 == null) {
        s2 = o2.toString();
      }

      if((s1 != null) && (s2 != null)) {
        return collator.compare(s1, s2);
      } else if((s1 == null) && (s2 != null)) {
        return -1;
      } else if((s1 != null) && (s2 == null)) {
        return 1;
      }

      return 0;
    }
  }

  protected class KeyStrokeComparator implements Comparator<KeyStroke> {
    /**
     * 
     */
    public int compare(KeyStroke o1, KeyStroke o2) {
      KeyStroke k1 = (KeyStroke) o1;
      KeyStroke k2 = (KeyStroke) o2;

      if(k1.getModifiers() != k2.getModifiers()) {
        int i1 = k1.getModifiers();
        int i2 = 0;
        int j1 = k2.getModifiers();
        int j2 = 0;

        while(i1 != 0) {
          if((i1 % 2) == 1) {
            i2++;
          }

          i1 /= 2;
        }

        while(j1 != 0) {
          if((j1 % 2) == 1) {
            j2++;
          }

          j1 /= 2;
        }

        if(i2 != j2) {
          return i2 - j2;
        }

        return k1.getModifiers() - k2.getModifiers();
      }

      return k1.getKeyCode() - k2.getKeyCode();
    }
  }

  protected class ListTableModel extends DefaultTableModel {
    /**
     * Creates a new ListTableModel object.
     */
    public ListTableModel(Object data[][], Object columns[]) {
      super(data, columns);
    }

    /**
     * 
     */
    public boolean isCellEditable(int r, int c) {
      return false;
    }
  }

  protected String getKeyStroke(KeyStroke stroke) {
    String s = null;

    if(stroke.getModifiers() != 0) {
      s = KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + " +
        KeyEvent.getKeyText(stroke.getKeyCode());
    } else {
      s = KeyEvent.getKeyText(stroke.getKeyCode());
    }

    return s;
  }

  protected MouseListener getMousePressedMouseListener() {
    return new MouseAdapter() {
        public void mousePressed(MouseEvent mouseEvent) {
          if(mouseEvent.getClickCount() == 2) {
            Point p = mouseEvent.getPoint();

            if((table.columnAtPoint(p) == 0) && (table.rowAtPoint(p) >= 0)) {
              int row = table.rowAtPoint(p);
              Object value = table.getValueAt(row, 0);

              if(value instanceof KeyStroke) {
                editStroke((KeyStroke) value, row);
              }
            }
          }
        }
      };
  }

  protected void editStroke(KeyStroke stroke, int row) {
    Component top = this.getTopLevelAncestor();
    TranslateStroke td = null;

    if(top instanceof Frame) {
      td = new TranslateStroke((Frame) top);
    } else if(top instanceof Dialog) {
      td = new TranslateStroke((Dialog) top);
    }

    td.setVisible(true);

    KeyStroke newStroke = td.getStroke();

    if((newStroke != null) && !newStroke.equals(stroke)) {
      if(ownShortcuts.contains(newStroke)) {
        JOptionPane.showMessageDialog(this, Resources.get("desktop.magellandesktop.prefs.shortcuts.error"));
      } else {
        boolean doIt = true;

        if(otherShortcuts.contains(newStroke)) {
          int res = JOptionPane.showConfirmDialog(this,
                              Resources.get("desktop.magellandesktop.prefs.shortcuts.warning"),
                              Resources.get("desktop.magellandesktop.prefs.shortcuts.warningtitle"),
                              JOptionPane.YES_NO_OPTION);
          doIt = (res == JOptionPane.YES_OPTION);
        }

        if(doIt) {
          if(desktop.getShortCutTranslations().containsKey(stroke)) {
            KeyStroke oldStroke = (KeyStroke) desktop.getShortCutTranslations().get(stroke);
            desktop.removeTranslation(stroke);
            stroke = oldStroke;
          }

          if(desktop.getShortCutListeners().containsKey(stroke) &&
               (desktop.getShortCutListeners().get(stroke) instanceof Action)) {
            ((Action) desktop.getShortCutListeners().get(stroke)).putValue("accelerator",
                                      newStroke);
          }

          if(!newStroke.equals(stroke)) {
            desktop.registerTranslation(newStroke, stroke);
          }

          model.setValueAt(newStroke, row, 0);
        }
      }
    }
  }

  /**
   * 
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
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

      Object args[] = { new Integer(otherShortcuts.size()) };
      buf.append(MessageFormat.format(Resources.get("desktop.magellandesktop.prefs.shortcuts.others"), args));
      buf.append('\n');
      buf.append('\n');

      Iterator it = otherShortcuts.iterator();

      while(it.hasNext()) {
        buf.append(getKeyStroke((KeyStroke) it.next()));

        if(it.hasNext()) {
          buf.append(", ");
        }
      }

      JTextArea java = new JTextArea(buf.toString());
      java.setEditable(false);
      java.setLineWrap(true);
      java.setWrapStyleWord(true);
      con.add(new JScrollPane(java), BorderLayout.SOUTH);

      JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JButton ok = new JButton("prefs.shortcuts.dialog.ok");
      ok.addActionListener(this);
      button.add(ok);
      con.add(button, BorderLayout.SOUTH);
      this.setContentPane(con);

      this.pack();
      this.setLocationRelativeTo(this.getParent());
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      this.setVisible(false);
    }
  }

  protected class TranslateStroke extends JDialog implements ActionListener {
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
      con.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.label")),
          BorderLayout.NORTH);
      text = new KeyTextField();
      con.add(text, BorderLayout.CENTER);

      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JButton ok = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.ok"));
      buttons.add(ok);
      ok.addActionListener(this);
      cancel = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.cancel"));
      buttons.add(cancel);
      cancel.addActionListener(this);
      con.add(buttons, BorderLayout.SOUTH);
      this.setContentPane(con);
      this.pack();
      this.setSize(this.getWidth() + 5, this.getHeight() + 5);
      this.setLocationRelativeTo(this.getParent());
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      if(actionEvent.getSource() != cancel) {
        if(text.getKeyCode() != 0) {
          stroke = KeyStroke.getKeyStroke(text.getKeyCode(), text.getModifiers());
        }
      }

      this.setVisible(false);
    }

    /**
     * 
     */
    public KeyStroke getStroke() {
      return stroke;
    }

    private class KeyTextField extends JTextField implements KeyListener {
      protected int modifiers = 0;
      protected int key = 0;

      /**
       * Creates a new KeyTextField object.
       */
      public KeyTextField() {
        super(20);
        this.addKeyListener(this);
      }

      /**
       * 
       */
      public void init(int modifiers, int key) {
        this.key = key;
        this.modifiers = modifiers;

        String s = KeyEvent.getKeyModifiersText(modifiers);

        if((s != null) && (s.length() > 0)) {
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
        modifiers = p1.getModifiers();
        key = p1.getKeyCode();

        // avoid double string
        if((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) ||
             (key == KeyEvent.VK_ALT) || (key == KeyEvent.VK_ALT_GRAPH)) {
          int xored = 0;

          switch(key) {
          case KeyEvent.VK_SHIFT:
            xored = KeyEvent.SHIFT_MASK;

            break;

          case KeyEvent.VK_CONTROL:
            xored = KeyEvent.CTRL_MASK;

            break;

          case KeyEvent.VK_ALT:
            xored = KeyEvent.ALT_MASK;

            break;

          case KeyEvent.VK_ALT_GRAPH:
            xored = KeyEvent.ALT_GRAPH_MASK;

            break;
          }

          modifiers ^= xored;
        }

        String s = KeyEvent.getKeyModifiersText(modifiers);

        if((s != null) && (s.length() > 0)) {
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
      }

      /** 
       * To allow "tab" as a key.
       */
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
    }
  }

  protected class StrokeRenderer extends DefaultTableCellRenderer {
    protected Font bold;
    protected Font norm;

    /**
     * Creates a new StrokeRenderer object.
     */
    public StrokeRenderer() {
      norm = this.getFont();
      bold = this.getFont().deriveFont(Font.BOLD);
    }

    /**
     * 
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                             boolean isSelected,
                             boolean hasFocus, int row, int column) {
      this.setFont(norm);
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

      if(value instanceof KeyStroke) {
        this.setText(getKeyStroke((KeyStroke) value));
      } else if(column == 0) {
        this.setFont(bold);
      }

      return this;
    }
  }
}
