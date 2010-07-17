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

/*
 * AdvancedTextCellRenderer.java
 *
 * Created on 12. Juli 2001, 19:19
 */
package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.library.utils.replacers.ReplacerSystem;

/**
 * A slightly improved text cell renderer using the replacer engine of Magellan to create region
 * dependent output. <i>Caches a lot to improve performance, so let the interpreter allocate more
 * memory!</i>
 * 
 * @author Andreas
 * @version 1.1
 */
public class AdvancedTextCellRenderer extends TextCellRenderer implements GameDataListener,
    ContextChangeable, ActionListener {
  protected static final String BLANK = "";
  protected boolean breakLines; // Line break style
  protected static List<String> buffer; // breaking buffer
  protected float lastScale = -1; // last scale factor, for broken lines cache
  protected ATRSet set;
  protected ATRPreferences adapter;
  // protected ShortcutListener deflistener; // Shortcutlistener at depth 1 (after STRG-A)
  protected JMenu contextMenu;
  protected ContextObserver obs;

  /**
   * Creates new AdvancedTextCellRenderer
   */
  public AdvancedTextCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    context.getEventDispatcher().addGameDataListener(this);

    if (AdvancedTextCellRenderer.buffer == null) {
      AdvancedTextCellRenderer.buffer = new LinkedList<String>();
    }

    loadSet(settings.getProperty(PropertiesHelper.ATR_CURRENT_SET, "Standard"));

    // load style settings
    try {
      breakLines = settings.getProperty("ATR.breakLines", "false").equals("true");
    } catch (Exception exc2) {
      breakLines = false;
    }

    try {
      setHAlign(Integer.parseInt(settings.getProperty(PropertiesHelper.ATR_HORIZONTAL_ALIGN, "0")));
    } catch (Exception exc) {
      setHAlign(AbstractTextCellRenderer.CENTER);
    }

    // moved to MapperPanel
    // create shortcut structure
    // DesktopEnvironment.registerShortcutListener(KeyStroke.getKeyStroke(KeyEvent.VK_T,
    // InputEvent.CTRL_MASK |
    // InputEvent.ALT_MASK), this);
    // deflistener = new DefListener();

    // create the context menu as needed
  }

  /**
   * Switches to the set with the given name. If such a set does not exist, it is created.
   */
  public void loadSet(String name) {
    loadSet(name, settings);

    if (!exists(name)) {
      saveSet();
    }
  }

  /**
   * Switches to the given set.
   */
  public void loadSet(String name, Properties settings) {
    ATRSet s = new ATRSet(name);
    s.load(settings);
    set = s;
    settings.setProperty(PropertiesHelper.ATR_CURRENT_SET, name);
  }

  /**
   * Saves the current set to the settings.
   */
  public void saveSet() {
    saveSet(settings);
  }

  /**
   * Saves the current set to the given settings.
   */
  public void saveSet(Properties settings) {
    set.save(settings);

    if (!exists(set.getName(), settings)) {
      settings.setProperty(PropertiesHelper.ATR_SETS, settings.getProperty(
          PropertiesHelper.ATR_SETS, "")
          + ";" + set.getName());
    }
  }

  /**
   * DOCUMENT-ME
   */
  public boolean exists(String name) {
    return exists(name, settings);
  }

  /**
   * DOCUMENT-ME
   */
  public boolean exists(String name, Properties settings) {
    String allSets = settings.getProperty(PropertiesHelper.ATR_SETS, "");
    int sindex = 0;
    int i = -1;

    do {
      i = allSets.indexOf(name, sindex);

      boolean check = (i >= 0);

      try {
        char first = allSets.charAt(i - 1);

        if (first != ';') {
          check = false;
        }
      } catch (IndexOutOfBoundsException ie) {
      }

      try {
        char first = allSets.charAt(i + name.length());

        if (first != ';') {
          check = false;
        }
      } catch (IndexOutOfBoundsException ie2) {
      }

      if (check)
        return true;

      sindex++;
    } while (i >= 0);

    return false;
  }

  /**
   * a collection of set names
   */
  public List<String> getAllSets() {
    List<String> c = new LinkedList<String>();
    StringTokenizer st =
        new StringTokenizer(settings.getProperty(PropertiesHelper.ATR_SETS, "Standard"), ";");

    while (st.hasMoreTokens()) {
      c.add(st.nextToken());
    }

    return c;
  }

  /**
   * DOCUMENT-ME
   */
  public void removeSet(String name) {
    String key = "ATR." + name + ".";
    settings.remove(key + "Def");
    settings.remove(key + "Unknown");

    StringTokenizer st =
        new StringTokenizer(settings.getProperty(PropertiesHelper.ATR_SETS, "Standard"), ";");
    StringBuffer b = new StringBuffer();

    while (st.hasMoreTokens()) {
      String s = st.nextToken();

      if (!s.equals(name)) {
        if (b.length() > 0) {
          b.append(';');
        }

        b.append(s);
      }
    }

    settings.setProperty(PropertiesHelper.ATR_SETS, b.toString());
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.client.swing.map.AbstractTextCellRenderer#setHAlign(int)
   */
  @Override
  public void setHAlign(int h) {
    super.setHAlign(h);
    settings.setProperty(PropertiesHelper.ATR_HORIZONTAL_ALIGN, String.valueOf(h));
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.client.swing.map.AbstractTextCellRenderer#init(magellan.library.GameData,
   *      java.awt.Graphics, java.awt.Rectangle)
   */
  @Override
  public void init(GameData data, Graphics g, Rectangle offset) {
    if (cellGeo.getScaleFactor() != lastScale) {
      set.clearCache();
      lastScale = cellGeo.getScaleFactor();
    }

    if (this.data != data) {
      set.clearCache();
      set.reprocessReplacer();
    }

    super.init(data, g, offset);
  }

  // never have single strings
  @Override
  public String getSingleString(Region r, Rectangle rect) {
    return null;
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.client.swing.map.TextCellRenderer#getText(magellan.library.Region,
   *      java.awt.Rectangle)
   */
  @Override
  public String[] getText(Region r, Rectangle rect) {
    return set.getReplacement(r, getCellGeometry());
  }

  /**
   * @see magellan.client.swing.map.TextCellRenderer#getName()
   */
  @Override
  public String getName() {
    return "ATR";
  }

  /**
   * DOCUMENT-ME
   */
  public void setBreakLines(boolean b) {
    if (breakLines != b) {
      set.clearCache();
      breakLines = b;
      settings.setProperty("ATR.breakLines", b ? "true" : "false");
      Mapper.setRenderContextChanged(true);
      DesktopEnvironment.repaintComponent("MAP");
    }
  }

  /**
   * @see magellan.client.swing.map.TextCellRenderer#getPreferencesAdapter()
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new ATRPreferences(this);
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    set.clearCache();
    set.reprocessReplacer();
  }

  // /**
  // * @see magellan.client.desktop.ShortcutListener#getShortCuts()
  // */
  // public Iterator<KeyStroke> getShortCuts() {
  // return null;
  // }
  //
  // /**
  // * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
  // */
  // public void shortCut(javax.swing.KeyStroke shortcut) {
  // changeATR();
  // }

  // /**
  // * @see
  // magellan.client.desktop.ExtendedShortcutListener#isExtendedShortcut(javax.swing.KeyStroke)
  // */
  // public boolean isExtendedShortcut(KeyStroke stroke) {
  // // (stm) removed, too obfuscating for users
  // if (true) return false;
  // return true;
  // }
  //
  // /**
  // * @see
  // magellan.client.desktop.ExtendedShortcutListener#getExtendedShortcutListener(javax.swing.KeyStroke)
  // */
  // public ShortcutListener getExtendedShortcutListener(KeyStroke stroke) {
  // return deflistener;
  // }
  //
  // /**
  // * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(java.lang.Object)
  // */
  // public String getShortcutDescription(KeyStroke stroke) {
  // return Resources.get("map.advancedtextcellrenderer.shortcuts.description");
  // }
  //
  // /**
  // * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
  // */
  // public String getListenerDescription() {
  // return Resources.get("map.advancedtextcellrenderer.shortcuts.title");
  // }

  // ////////////////
  // Context menu //
  // ////////////////
  public JMenuItem getContextAdapter() {
    if (contextMenu == null) {
      contextMenu = new JMenu(Resources.get("map.advancedtextcellrenderer.context.title"));
      fillDefItems();
    }

    return contextMenu;
  }

  protected void fillDefItems() {
    contextMenu.removeAll();

    List<String> c = getAllSets();
    Iterator<String> it = c.iterator();

    while (it.hasNext()) {
      JMenuItem item = new JMenuItem(it.next());
      item.addActionListener(this);
      contextMenu.add(item);
    }
  }

  protected String getShortText(String text, int maxLength) {
    if (text == null)
      return null;

    if (text.length() <= maxLength)
      return text;

    return (text.substring(0, 7) + "...");
  }

  // don't need to call anyone, we repaint ourself
  public void setContextObserver(ContextObserver co) {
    obs = co;
  }

  /**
   * DOCUMENT-ME
   */
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    loadSet(actionEvent.getActionCommand());

    if (obs != null) {
      obs.contextDataChanged();
    }
  }

  // //////////////
  // SET & REPLACER
  // /////////////
  protected String createString(ReplacerSystem rep, Region r) {
    if (rep != null) { // create replacement

      Object o = rep.getReplacement(r);

      if (o != null)
        return o.toString();
    }

    return AdvancedTextCellRenderer.BLANK;
  }

  protected String[] breakString(String s, CellGeometry geo) {
    int maxWidth = (int) (geo.getCellSize().width * 0.9);
    AdvancedTextCellRenderer.buffer.clear();

    // create "defined" line breaks
    StringTokenizer st = new StringTokenizer(s, "\n");

    while (st.hasMoreTokens()) {
      AdvancedTextCellRenderer.buffer.add(st.nextToken());
    }

    boolean changed = false;

    do {
      changed = false;

      Iterator<String> it = AdvancedTextCellRenderer.buffer.iterator();
      int index = 0;

      while (it.hasNext() && !changed) {
        String part = it.next();

        if (getWidth(part) > maxWidth) {
          breakStringImpl(part, AdvancedTextCellRenderer.buffer, maxWidth, index);
          changed = true;
        }

        index++;
      }
    } while (changed);

    String strbuf[] = new String[AdvancedTextCellRenderer.buffer.size()];

    for (int i = 0; i < strbuf.length; i++) {
      strbuf[i] = AdvancedTextCellRenderer.buffer.get(i);
    }

    return strbuf;
  }

  private void breakStringImpl(String part, List<String> buffer, int mw, int index) {
    char chr[] = part.toCharArray();
    int len = chr.length;

    while (getWidth(chr, 0, len) > mw) {
      len--;
    }

    if (breakLines) {
      String first = new String(chr, 0, len);
      String rest = new String(chr, len, chr.length - len);
      buffer.set(index, rest);
      buffer.add(index, first);
    } else {
      try {
        chr[len - 1] = '.';
        chr[len - 2] = '.';
        chr[len - 3] = '.';
      } catch (Exception exc) {
      }

      part = new String(chr, 0, len);
      buffer.set(index, part);
    }
  }

  /**
   * Encapsulates one setting containing a name, a definition string and a cache object.
   */
  protected class ATRSet {
    protected String name;
    protected String def;
    protected String unknown = "-?-";
    protected ReplacerSystem replacer = null;
    protected Map<Region, String[]> cache;

    /**
     * Creates a new ATRSet object.
     */
    public ATRSet(String name) {
      this.name = name;
      cache = new HashMap<Region, String[]>();
    }

    /**
     * DOCUMENT-ME
     */
    public void clearCache() {
      cache.clear();
    }

    /**
     * DOCUMENT-ME
     */
    public void reprocessReplacer() {
      replacer = ReplacerHelp.createReplacer(def, unknown);
    }

    /**
     * DOCUMENT-ME
     */
    public String[] getReplacement(Region r, CellGeometry geo) {
      if (cache.containsKey(r))
        return cache.get(r);

      String buf[] = breakString(createString(replacer, r), geo);
      cache.put(r, buf);

      return buf;
    }

    /**
     * DOCUMENT-ME
     */
    public void setDef(String def) {
      clearCache();
      this.def = def;
      reprocessReplacer();
    }

    /**
     * DOCUMENT-ME
     */
    public String getDef() {
      return def;
    }

    /**
     * DOCUMENT-ME
     */
    public String getName() {
      return name;
    }

    /**
     * DOCUMENT-ME
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * DOCUMENT-ME
     */
    public String getUnknown() {
      return unknown;
    }

    /**
     * DOCUMENT-ME
     */
    public void setUnknown(String unknown) {
      clearCache();
      this.unknown = unknown;

      if (unknown == null) {
        this.unknown = "";
      }

      reprocessReplacer();
    }

    /**
     * DOCUMENT-ME
     */
    public void load(Properties settings) {
      clearCache();

      String key = "ATR." + name + ".";
      def = settings.getProperty(key + "Def", "rname");
      setUnknown(settings.getProperty(key + "Unknown", "-?-"));
    }

    /**
     * DOCUMENT-ME
     */
    public void save(Properties settings) {
      String key = "ATR." + name + ".";
      settings.setProperty(key + "Def", def);

      if (unknown.equals("-?-")) {
        settings.remove(key + "Unknown");
      } else {
        settings.setProperty(key + "Unknown", unknown);
      }
    }
  }

  class DefListener implements ShortcutListener {
    protected List<KeyStroke> keys;

    /**
     * Creates a new DefListener object.
     */
    public DefListener() {
      keys = new ArrayList<KeyStroke>(10);

      for (int i = 1; i < 10; i++) {
        keys.add(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
      }

      keys.add(KeyStroke.getKeyStroke('0'));
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getShortCuts()
     */
    public Iterator<KeyStroke> getShortCuts() {
      return keys.iterator();
    }

    /**
     * Selects the x-th set (where shortcut is the x-th registered shortcut.
     * 
     * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
     */
    public void shortCut(javax.swing.KeyStroke shortcut) {
      int i = keys.indexOf(shortcut);

      if (i >= 0) {
        List<String> c = getAllSets();

        if (c.size() > i) {
          Iterator<String> it = c.iterator();
          int j = 0;

          while (it.hasNext()) {
            String s = it.next();

            if (i == j) {
              loadSet(s);
              DesktopEnvironment.repaintComponent("MAP");

              break;
            }

            j++;
          }
        }
      }
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
     */
    public String getShortcutDescription(KeyStroke stroke) {
      return null;
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
     */
    public String getListenerDescription() {
      return null;
    }
  }

  protected class ATRPreferences extends JPanel implements ActionListener, PreferencesAdapter,
      ListSelectionListener {
    protected AdvancedTextCellRenderer s;

    // global properties
    protected JCheckBox linebreak;
    protected JRadioButton align[];

    // set-local properties
    protected JTextField replace;
    protected JTextArea def;

    // ui things
    protected JList list;
    protected DefaultListModel listModel;
    protected AbstractButton add;
    protected AbstractButton remove;
    protected AbstractButton rename;
    protected AbstractButton importB;
    protected AbstractButton export;

    /**
     * Creates a new ATRPreferences object.
     */
    public ATRPreferences(AdvancedTextCellRenderer s) {
      this.s = s;
      setLayout(new GridBagLayout());

      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.FIRST_LINE_START,
              GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
      String helpText = Resources.get("map.advancedtextcellrenderer.description");
      JTextArea fontHelp = new JTextArea(helpText, 2, 0);// , 2, helpText.length()/2);
      fontHelp.setFont((new JLabel()).getFont());
      // fontHelp.setPreferredSize(new Dimension(500, 30));
      fontHelp.setEditable(false);
      fontHelp.setBorder(null);
      fontHelp.setBackground(getBackground());
      fontHelp.setLineWrap(true);
      fontHelp.setWrapStyleWord(true);

      c.fill = GridBagConstraints.HORIZONTAL;
      this.add(new JScrollPane(fontHelp), c);

      c.gridy++;
      this.add(new JSeparator(SwingConstants.HORIZONTAL), c);

      c.gridwidth = 1;
      c.fill = GridBagConstraints.NONE;
      c.gridy++;

      linebreak =
          new JCheckBox(Resources.get("map.advancedtextcellrenderer.prefs.breakline"), s.breakLines);
      this.add(linebreak, c);

      c.gridy++;
      this.add(createAlignPanel(), c);

      c.gridx = 0;
      c.gridy++;
      c.fill = GridBagConstraints.HORIZONTAL;
      this.add(new JSeparator(SwingConstants.HORIZONTAL), c);

      c.gridy++;
      c.fill = GridBagConstraints.BOTH;
      this.add(createFullSetPanel(), c);
    }

    protected Component createFullSetPanel() {
      JPanel help = new JPanel(new BorderLayout());

      help.add(createSetPanel(), BorderLayout.CENTER);
      help.add(createNavPanel(), BorderLayout.WEST);

      return help;
    }

    protected Component createNavPanel() {
      add = new JButton(Resources.get("map.advancedtextcellrenderer.prefs.add"));
      add.addActionListener(this);
      remove = new JButton(Resources.get("map.advancedtextcellrenderer.prefs.remove"));
      remove.addActionListener(this);
      rename = new JButton(Resources.get("map.advancedtextcellrenderer.prefs.rename"));
      rename.addActionListener(this);
      importB = new JButton(Resources.get("map.advancedtextcellrenderer.prefs.import"));
      importB.addActionListener(this);
      export = new JButton(Resources.get("map.advancedtextcellrenderer.prefs.export"));
      export.addActionListener(this);

      listModel = new DefaultListModel();
      list = new JList(listModel);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      fillList(set.getName());

      JPanel p = new JPanel(new GridBagLayout());
      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
              GridBagConstraints.HORIZONTAL, new Insets(0, 1, 1, 1), 0, 0);

      p.add(add, c);
      c.gridy++;
      p.add(rename, c);
      c.gridy++;
      p.add(remove, c);
      c.gridy++;
      p.add(importB, c);
      c.gridy++;
      p.add(export, c);
      c.gridy++;
      c.fill = GridBagConstraints.BOTH;
      JScrollPane scrollPane = new JScrollPane(list);
      scrollPane.setPreferredSize(new Dimension(rename.getPreferredSize().width, 100));
      p.add(scrollPane, c);

      list.addListSelectionListener(this);

      return p;
    }

    protected Component createSetPanel() {
      JPanel sPanel = new JPanel(new BorderLayout());

      JPanel help = new JPanel(new FlowLayout(FlowLayout.CENTER));
      help.add(new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.replace")));
      help.add(replace = new JTextField(set.getUnknown(), 5));
      sPanel.add(help, BorderLayout.NORTH);

      help = new JPanel();
      help.add(new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.def"))); // Resources.get("map.advancedtextcellrenderer.prefs.planes")),c);
      help.add(new JScrollPane(def = new JTextArea(set.getDef(), 11, 30)));
      sPanel.add(help, BorderLayout.CENTER);

      return sPanel;
    }

    protected Component createAlignPanel() {
      Box box = new Box(BoxLayout.X_AXIS);
      box.add(new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.aligntext")));
      box.add(Box.createHorizontalStrut(5));

      ButtonGroup group = new ButtonGroup();
      align = new JRadioButton[3];

      for (int i = 0; i < 3; i++) {
        align[i] =
            new JRadioButton(Resources.get("map.advancedtextcellrenderer.prefs.align"
                + String.valueOf(i)), i == s.getHAlign());
        group.add(align[i]);
        box.add(align[i]);
      }

      return box;
    }

    protected void fillList() {
      List<String> c = s.getAllSets();

      if (c.size() > 0) {
        fillList(c.iterator().next());
      } else {
        fillList(null);
      }
    }

    protected void fillList(Object select) {
      List<String> c = s.getAllSets();
      listModel.removeAllElements();

      Iterator<String> it = c.iterator();

      while (it.hasNext()) {
        listModel.addElement(it.next());
      }

      if (select != null) {
        list.setSelectedValue(select, true);
      } else {
        list.setSelectedIndex(-1);
      }
    }

    /**
     * Changes the active set.
     * 
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      if (list.getSelectedValue() != null) {
        if ((set == null) || (list.getSelectedValue() != set.getName())) {
          if (set != null) {
            saveSettings();
          }

          loadSet((String) list.getSelectedValue());
          updateSet();
        }
      }
    }

    protected void updateSet() {
      replace.setText(set.getUnknown());
      def.setText(set.getDef());
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     */
    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      saveSettings();
      s.setBreakLines(linebreak.isSelected());

      for (int i = 0; i < 3; i++) {
        if (align[i].isSelected()) {
          s.setHAlign(i);

          break;
        }
      }
    }

    /**
     * Returns <code>this</code>.
     * 
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public Component getComponent() {
      return this;
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
     */
    public String getTitle() {
      return s.getName();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == add) {
        addSet();
      } else if (e.getSource() == rename) {
        renameSet();
      } else if (e.getSource() == remove) {
        removeSet();
      } else if (e.getSource() == importB) {
        importSet();
      } else if (e.getSource() == export) {
        exportSet();
      }
    }

    protected void saveSettings() {
      set.setDef(def.getText());
      set.setUnknown(replace.getText());
      saveSet();
    }

    protected void addSet() {
      String name =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.add.text"));

      if ((name != null) && !name.trim().equals("")) {
        if (exists(name)) {
          JOptionPane.showMessageDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.nameexists"));
        } else {
          saveSettings();
          loadSet(name);
          fillList(name);
        }
      }
    }

    protected void renameSet() {
      String newName =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.rename.text"));

      if ((newName != null) && !newName.trim().equals("")) {
        if (exists(newName)) {
          JOptionPane.showMessageDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.nameexists"));
        } else {
          s.removeSet(set.getName());
          set.setName(newName);
          saveSettings();
          fillList(newName);
        }
      }
    }

    protected void removeSet() {
      String name = (String) list.getSelectedValue();

      if (name != null) {
        s.removeSet(name);

        ATRSet old = set;
        set = null;
        fillList();

        if (set == null) {
          set = old;
        }
      }
    }

    protected void importSet() {
      JFileChooser jfc = new JFileChooser(Client.getResourceDirectory());
      int ret = jfc.showOpenDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        java.io.File f = jfc.getSelectedFile();
        String lastName = set.getName();

        try {
          Properties prop = new Properties();
          prop.load(new java.io.FileInputStream(f));

          String sets = prop.getProperty(PropertiesHelper.ATR_SETS, "");
          StringTokenizer st = new StringTokenizer(sets, ";");

          while (st.hasMoreTokens()) {
            String retS = importImpl(st.nextToken(), prop);

            if (retS != null) {
              lastName = retS;
            }
          }
        } catch (Exception exc) {
          Logger.getInstance(this.getClass()).warn(
              "could not read file " + f + ", " + exc.getLocalizedMessage());
        }

        fillList(lastName);
      }
    }

    protected String importImpl(String name, Properties prop) {
      String oldName = name;

      while ((name != null) && exists(name)) {
        name =
            JOptionPane.showInputDialog(this, Resources.get(
                "map.advancedtextcellrenderer.prefs.nameexists2", name));
      }

      if (name == null)
        return null;

      saveSettings();
      s.loadSet(oldName, prop);
      set.setName(name);
      updateSet();
      saveSettings();

      return name;
    }

    protected void exportSet() {
      JFileChooser jfc = new JFileChooser(Client.getResourceDirectory());
      int ret = jfc.showSaveDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        try {
          java.io.File f = jfc.getSelectedFile();
          FileOutputStream out = new FileOutputStream(f);
          Properties prop = new Properties();
          saveSet(prop);
          prop.store(out, "ATR exported settings");
          out.close();
        } catch (IOException exc) {
          Logger.getInstance(this.getClass()).warn(
              "could not write file " + jfc.getSelectedFile() + ", " + exc.getLocalizedMessage());
        }
      }
    }
  }
}
