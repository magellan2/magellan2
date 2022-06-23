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

package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.SwingUtils;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.CollectionFactory;
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
 * @author stm
 * @version 1.2
 */
public class AdvancedTextCellRenderer extends TextCellRenderer implements GameDataListener,
    ContextChangeable, ActionListener {

  protected static final String BLANK = "";
  protected boolean breakLines; // Line break style
  protected float lastScale = -1; // last scale factor, for broken lines cache
  protected ATRSet currentSet;
  protected ATRPreferencesAdapter adapter;
  protected JMenu contextMenu;
  protected ContextObserver obs;
  private Map<String, ATRSet> atrSets;
  private boolean deferred;

  /**
   * Creates new AdvancedTextCellRenderer
   */
  public AdvancedTextCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    context.getEventDispatcher().addGameDataListener(this);

    ATRPreferences atrPref = new ATRPreferences(settings);
    atrSets = atrPref.loadSets();
    currentSet = atrSets.get(atrPref.getCurrentSet());

    try {
      breakLines = atrPref.getBreakLines();
    } catch (Exception exc2) {
      breakLines = false;
    }

    try {
      setHAlign(atrPref.getAlign());
    } catch (Exception exc) {
      setHAlign(AbstractTextCellRenderer.CENTER);
    }
  }

  /**
   * @see magellan.client.swing.map.AbstractTextCellRenderer#setHAlign(int)
   */
  @Override
  public void setHAlign(int h) {
    super.setHAlign(h);
    invalidate();
  }

  /**
   * @see magellan.client.swing.map.AbstractTextCellRenderer#init(magellan.library.GameData,
   *      java.awt.Graphics, java.awt.Rectangle)
   */
  @Override
  public void init(GameData data, Graphics g, Rectangle offset) {
    if (cellGeo.getScaleFactor() != lastScale) {
      if (currentSet != null) {
        currentSet.clearCache();
      }
      lastScale = cellGeo.getScaleFactor();
    }

    if (this.data != data) {
      if (currentSet != null) {
        currentSet.clearCache();
      }
    }

    super.init(data, g, offset);
  }

  /**
   * Never returns a string.
   *
   * @see magellan.client.swing.map.TextCellRenderer#getSingleString(magellan.library.Region,
   *      java.awt.Rectangle)
   */
  @Override
  public String getSingleString(Region r, Rectangle rect) {
    return null;
  }

  /**
   * Applies replacers and returns result.
   * 
   * @see magellan.client.swing.map.TextCellRenderer#getText(magellan.library.Region,
   *      java.awt.Rectangle)
   */
  @Override
  public String[] getText(Region r, Rectangle rect) {
    if (currentSet == null)
      return null;
    return currentSet.getReplacement(r, getCellGeometry(), this);
  }

  /**
   * @see magellan.client.swing.map.TextCellRenderer#getName()
   */
  @Override
  public String getName() {
    return "ATR";
  }

  /**
   * Changes the automatic line break property.
   */
  public void setBreakLines(boolean b) {
    if (breakLines != b) {
      if (currentSet != null) {
        currentSet.clearCache();
      }
      breakLines = b;
      invalidate();
    }
  }

  /**
   * Returns a view of all possible set definitions with names as keys.
   */
  public Map<String, ATRSet> getAllSets() {
    return Collections.unmodifiableMap(atrSets);
  }

  protected void setAllSets(Map<String, ATRSet> atrSets2) {
    atrSets = copySets(atrSets2);
    currentSet = null;
  }

  /**
   * Returns a list of all possible set names.
   */
  public List<String> getAllSetNames() {
    return new ArrayList<String>(atrSets.keySet());
  }

  /**
   * Returns the currently used set definition.
   */
  public String getCurrentSet() {
    if (currentSet == null)
      return null;
    return currentSet.getName();
  }

  protected static Map<String, ATRSet> copySets(Map<String, ATRSet> map) {
    Map<String, ATRSet> setCopy = CollectionFactory.createOrderedMap(map.size());
    for (Entry<String, ATRSet> entry : map.entrySet()) {
      setCopy.put(entry.getKey(), new ATRSet(entry.getValue()));
    }
    return setCopy;
  }

  /**
   * Selects and activates the given set definition.
   */
  public void loadSet(String setName) {
    currentSet = atrSets.get(setName);
    invalidate();
  }

  protected void setDeferUpdate(boolean defer) {
    deferred = defer;
    invalidate();
  }

  private void invalidate() {
    if (!deferred) {
      Mapper.setRenderContextChanged(true);
      DesktopEnvironment.repaintComponent(MagellanDesktop.MAP_IDENTIFIER);
    }
  }

  /**
   * @see magellan.client.swing.map.TextCellRenderer#getPreferencesAdapter()
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new ATRPreferencesAdapter(this);
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    if (currentSet != null) {
      currentSet.clearCache();
    }
  }

  // ////////////////
  // Context menu //
  // ////////////////
  /**
   * @see magellan.client.swing.context.ContextChangeable#getContextAdapter()
   */
  public JMenuItem getContextAdapter() {
    if (contextMenu == null) {
      contextMenu = new JMenu(Resources.get("map.advancedtextcellrenderer.context.title"));
      fillDefItems();
    }

    return contextMenu;
  }

  protected void fillDefItems() {
    contextMenu.removeAll();

    for (String name : atrSets.keySet()) {
      JMenuItem item = new JMenuItem(name);
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
   * Reacts to context menu actions.
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

  ArrayList<String> parts = new ArrayList<String>();

  protected String[] breakString(String s, CellGeometry geo) {
    int maxWidth = (int) (geo.getCellSize().width * 0.9);

    // create "defined" line breaks
    String[] lines = s.split(" *[\\n\\r]");

    int i = 0;
    parts.clear();

    boolean changed = false;
    for (String line : lines) {
      if (getWidth(line) <= maxWidth) {
        parts.add(line);
      } else {
        changed = true;
        if (breakLines) {
          char chr[] = line.stripTrailing().toCharArray();
          for (int pos = chr.length, start = 0; start < chr.length;) {
            if (getWidth(chr, start, pos) <= maxWidth) {
              if (pos == start)
                // part does not fit, delete all
                return new String[0];

              parts.add(new String(chr, start, pos - start));
              start = pos;
              while (start < chr.length && chr[start] == ' ') {
                ++start;
              }
              pos = chr.length;
            } else {
              --pos;
              while (pos > start && chr[pos - 1] == ' ') {
                --pos;
              }
            }
          }

        } else {
          if (line.length() <= 3) {
            int len = 3;
            for (len = line.length();; --len) {
              String part = line.substring(0, len);
              if (getWidth(part) <= maxWidth) {
                parts.add(part);
                break;
              }
            }
          } else {
            char chr[] = line.toCharArray();
            chr[chr.length - 1] = '.';
            chr[chr.length - 2] = '.';
            chr[chr.length - 3] = '.';
            int len = chr.length;
            while (getWidth(chr, 0, len) > maxWidth) {
              --len;
              if (len > 3) {
                chr[len - 3] = '.';
              }
            }
            parts.add(new String(chr, 0, len));
          }
        }
      }
    }
    String[] display;
    if (!changed) {
      display = lines;
    } else {
      display = parts.toArray(lines);
    }
    int h = getMaxHeight(display);
    if (h * display.length > geo.getCellSize().height) {
      double r = h * display.length / (double) geo.getCellSize().height;
      int l = (int) (display.length / r);
      if (l < 1) {
        l = 1;
      }
      display = Arrays.copyOf(display, l);
    }
    return display;
  }

  private void breakStringImpl(String part, int mw, ListIterator<String> it) {
    char chr[] = part.toCharArray();
    int len = chr.length;

    if (breakLines) {
      while (getWidth(chr, 0, len) > mw) {
        --len;
      }

      if (len == 0) {
        it.set(new String(chr, 0, 1));
        it.add(new String(chr, 1, chr.length - 1).trim());
      } else if (chr.length > len) {
        String first = new String(chr, 0, len).stripTrailing();
        String rest = new String(chr, len, chr.length - len).strip();
        if (first.isEmpty()) {
          it.set(rest);
        } else {
          it.set(first);
          if (!rest.isEmpty()) {
            it.add(rest);
          }
        }
      }
    } else {
      if (chr.length <= 3) {
        while (getWidth(chr, 0, len) > mw) {
          --len;
        }
      } else {
        chr[chr.length - 1] = '.';
        chr[chr.length - 2] = '.';
        chr[chr.length - 3] = '.';
        while (getWidth(chr, 0, len) > mw) {
          --len;
          if (len > 3) {
            chr[len - 3] = '.';
          }
        }
      }

      part = new String(chr, 0, len);
      it.set(part);
    }
  }

  /**
   * Encapsulates one setting containing a name, a definition string and a cache object.
   */
  protected static class ATRSet {
    protected String name;
    protected String def;
    protected String unknown = "-?-";
    protected ReplacerSystem replacer = null;
    protected Map<Region, String[]> cache;

    /**
     * Creates a new ATRSet object.
     *
     * @throws InvalidNameException if name does not pass {@link ATRPreferences#isValidName(String)}
     */
    public ATRSet(String name) throws InvalidNameException {
      if (!ATRPreferences.isValidName(name))
        throw new InvalidNameException(name);

      this.name = name;
    }

    public ATRSet(ATRSet value) {
      name = value.name;
      def = value.def;
      unknown = value.unknown;
    }

    /**
     * invalidates cached replacement
     */
    private void clearCache() {
      if (cache != null) {
        cache.clear();
      }
    }

    /**
     * Returns replacement text for a region.
     */
    public String[] getReplacement(Region r, CellGeometry geo, AdvancedTextCellRenderer atr) {
      if (cache == null) {
        cache = new HashMap<Region, String[]>();
        replacer = ReplacerHelp.createReplacer(def, unknown);
      }

      if (cache.containsKey(r))
        return cache.get(r);

      String buf[] = atr.breakString(atr.createString(replacer, r), geo);
      cache.put(r, buf);

      return buf;
    }

    /**
     * Changes the definition.
     */
    public void setDef(String def) {
      this.def = def;
      clearCache();
    }

    /**
     */
    public String getDef() {
      return def;
    }

    /**
     */
    public String getName() {
      return name;
    }

    /**
     * @throws InvalidNameException
     */
    public void setName(String name) throws InvalidNameException {
      if (!ATRPreferences.isValidName(name))
        throw new InvalidNameException(name);

      this.name = name;
    }

    /**
     */
    public String getUnknown() {
      return unknown;
    }

    /**
     */
    public void setUnknown(String unknown) {
      clearCache();
      this.unknown = unknown;

      if (unknown == null) {
        this.unknown = "";
      }

      clearCache();
    }

    /**
     */
    public void load(Properties settings) {
      String key = "ATR." + name + ".";
      def = settings.getProperty(key + "Def", "rname");
      setUnknown(settings.getProperty(key + "Unknown", "-?-"));
      clearCache();
    }

    /**
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

    public void remove(Properties settings) {
      String key = "ATR." + name + ".";
      settings.remove(key + "Def");
      settings.remove(key + "Unknown");
    }

  }

  /**
   * Thrown if a set name does not correspond to the naming conventions.
   */
  public static class InvalidNameException extends Exception {

    /**
     */
    public InvalidNameException(String name) {
      super("Invalid name \"" + name + "\"");
    }

  }

  protected static class ATRPreferences {

    private Properties settings;

    public ATRPreferences(Properties settings) {
      this.settings = settings;
    }

    public Map<String, ATRSet> loadSets() {
      List<String> names = getSets();
      Map<String, ATRSet> sets = CollectionFactory.createOrderedMap(names.size() + 1);
      for (String name : names) {
        try {
          if (isValidName(name)) {
            ATRSet set = new ATRSet(name);
            set.load(settings);
            sets.put(name, set);
          }
        } catch (InvalidNameException e) {
          // skip
        }
      }

      return sets;
    }

    public static boolean isValidName(String name) {
      return name != null && name.trim().length() > 0 && !name.matches(".*[;,]");
    }

    public String getCurrentSet() {
      return settings.getProperty(PropertiesHelper.ATR_CURRENT_SET, "Standard");
    }

    public void setCurrentSet(String current) {
      if (isValidName(current)) {
        if (current == null || current.length() == 0) {
          current = "Standard";
        }
        settings.setProperty(PropertiesHelper.ATR_CURRENT_SET, current);
      }
    }

    public boolean getBreakLines() {
      return settings.getProperty("ATR.breakLines", "false").equals("true");
    }

    public void setBreakLines(boolean b) {
      settings.setProperty("ATR.breakLines", b ? "true" : "false");
    }

    public int getAlign() {
      return Integer.parseInt(settings.getProperty(PropertiesHelper.ATR_HORIZONTAL_ALIGN, "0"));
    }

    public void setAlign(int h) {
      settings.setProperty(PropertiesHelper.ATR_HORIZONTAL_ALIGN, String.valueOf(h));
    }

    /**
     * Saves the current set to the settings.
     */
    public void saveSet(ATRSet set) {
      set.save(settings);

      if (!getSets().contains(set.getName())) {
        settings.setProperty(PropertiesHelper.ATR_SETS, settings.getProperty(
            PropertiesHelper.ATR_SETS, "") + ";" + set.getName());
      }
    }

    private List<String> getSets() {
      return Arrays.asList(settings.getProperty(PropertiesHelper.ATR_SETS, "Standard").split(";"));
    }

    /**
     * Returns true if the a definition for the given name exists.
     */
    public boolean exists(String name) {
      return getSets().contains(name);
    }

    /**
     * Remove the definition with the given name.
     */
    public void removeSet(ATRSet set) {
      set.remove(settings);

      List<String> sets = getSets();
      sets.remove(set.getName());
      saveSets(sets);
    }

    private void saveSets(Collection<String> sets) {
      String st = String.join(";", sets);
      settings.setProperty(PropertiesHelper.ATR_SETS, st);
    }

  }

  protected static class ATRPreferencesAdapter extends JPanel implements ActionListener,
      PreferencesAdapter, ListSelectionListener {
    protected AdvancedTextCellRenderer atrRenderer;
    protected ATRPreferences atrPreferences;

    // global properties
    protected JCheckBox linebreak;
    protected JRadioButton align[];
    protected Map<String, ATRSet> atrSets;
    protected String currentSet;

    // set-local properties
    protected JTextField replace;
    protected JTextArea def;

    // ui things
    protected JList<String> nameList;
    protected DefaultListModel<String> nameListModel;
    protected AbstractButton add;
    protected AbstractButton remove;
    protected AbstractButton rename;
    protected AbstractButton importB;
    protected AbstractButton export;

    /**
     * Creates a new ATRPreferences object.
     */
    public ATRPreferencesAdapter(AdvancedTextCellRenderer s) {
      atrRenderer = s;
      atrPreferences = new ATRPreferences(atrRenderer.settings);
      setLayout(new GridBagLayout());

      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.FIRST_LINE_START,
              GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridy++;
      this.add(new JSeparator(SwingConstants.HORIZONTAL), c);

      c.gridwidth = 1;
      c.fill = GridBagConstraints.NONE;
      c.gridy++;

      linebreak =
          new JCheckBox(Resources.get("map.advancedtextcellrenderer.prefs.breakline"));
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

      nameListModel = new DefaultListModel<String>();
      nameList = new JList<String>(nameListModel);
      nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      JButton showInfo =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.showinfo"));
      showInfo.setActionCommand("SHOW INFO");
      showInfo.addActionListener(this);

      JScrollPane scrollPane = new JScrollPane(nameList);
      Dimension dim = SwingUtils.getDimension(10, 8, true);
      dim.width = rename.getPreferredSize().width;
      scrollPane.setPreferredSize(dim);
      nameList.addListSelectionListener(this);

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
      p.add(scrollPane, c);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridy++;
      p.add(showInfo, c);

      return p;
    }

    protected Component createSetPanel() {
      JPanel setPanel = new JPanel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);

      JLabel rLabel = new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.unknown"));
      setPanel.add(rLabel, gbc);
      gbc.gridx++;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      gbc.weighty = .1;
      setPanel.add(replace = new JTextField(5), gbc);
      rLabel.setLabelFor(replace);
      replace.setToolTipText(Resources.get("map.advancedtextcellrenderer.prefs.unknown.tooltip"));

      def = new JTextArea(5, 30);
      def.setWrapStyleWord(false);
      def.setLineWrap(true);

      JLabel dLabel;
      gbc.gridx = 0;
      gbc.gridy++;
      gbc.weightx = 0;
      gbc.weighty = 0;
      gbc.fill = GridBagConstraints.NONE;
      setPanel.add(dLabel = new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.def")),
          gbc);
      JScrollPane defPane;
      gbc.gridx++;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.gridheight = 2;
      gbc.fill = GridBagConstraints.BOTH;
      setPanel.add(defPane = new JScrollPane(def), gbc);
      dLabel.setLabelFor(defPane);

      setPanel.setBorder(new EtchedBorder());
      return setPanel;
    }

    protected Component createAlignPanel() {
      Box box = new Box(BoxLayout.X_AXIS);
      JLabel boxLabel = new JLabel(Resources.get("map.advancedtextcellrenderer.prefs.aligntext"));
      boxLabel.setLabelFor(box);
      box.add(boxLabel);
      box.add(Box.createHorizontalStrut(5));

      ButtonGroup group = new ButtonGroup();
      align = new JRadioButton[3];

      for (int i = 0; i < 3; i++) {
        align[i] =
            new JRadioButton(Resources.get("map.advancedtextcellrenderer.prefs.align"
                + String.valueOf(i)), i == atrRenderer.getHAlign());
        group.add(align[i]);
        box.add(align[i]);
      }

      return box;
    }

    protected String getCurrent() {
      return currentSet;
    }

    protected ATRSet getCurrentSet() {
      return atrSets.get(currentSet);
    }

    /**
     * Changes the active set.
     * 
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      if (nameList.getSelectedValue() != null) {
        updateCurrent(); // update old set
        currentSet = nameList.getSelectedValue();
        updateSet();
      }
    }

    private void initSets() {
      atrSets = copySets(atrRenderer.getAllSets());
      nameListModel.clear();
      for (String key : atrSets.keySet()) {
        nameListModel.addElement(key);
      }
      currentSet = atrRenderer.getCurrentSet();
    }

    private void updateSet() {
      if (getCurrentSet() != null) {
        replace.setText(getCurrentSet().getUnknown());
        def.setText(getCurrentSet().getDef());
        nameList.setSelectedValue(getCurrentSet().getName(), true);
      } else {
        replace.setText(null);
        def.setText(null);
        nameList.setSelectedValue(null, true);
      }
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     */
    public void initPreferences() {
      initSets();
      updateSet();
      linebreak.setSelected(atrRenderer.breakLines);
      align[atrRenderer.getHAlign()].setSelected(true);
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      int newAlign = 0;
      try {
        atrRenderer.setDeferUpdate(true);
        updateCurrent();
        atrRenderer.setBreakLines(linebreak.isSelected());

        for (int i = 0; i < 3; i++) {
          if (align[i].isSelected()) {
            newAlign = i;
            atrRenderer.setHAlign(i);
            break;
          }
        }

        atrRenderer.setAllSets(atrSets);
        atrRenderer.loadSet(getCurrent());
      } finally {
        atrRenderer.setDeferUpdate(false);
      }

      atrPreferences.saveSets(Collections.<String> emptyList());
      for (ATRSet set : atrSets.values()) {
        atrPreferences.saveSet(set);
      }
      atrPreferences.setAlign(newAlign);
      atrPreferences.setBreakLines(linebreak.isSelected());
      atrPreferences.setCurrentSet(getCurrent());
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
      return atrRenderer.getName();
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
      } else if (e.getSource() instanceof JButton) {
        JButton button = (JButton) e.getSource();
        if (button.getActionCommand().equals("SHOW INFO")) {
          ToolTipReplacersInfo.showInfoDialog(this, Resources.get(
              "map.advancedtextcellrenderer.description"));
        }
      }
    }

    protected void updateCurrent() {
      ATRSet set = atrSets.get(getCurrent());
      if (set != null) {
        set.setDef(def.getText());
        set.setUnknown(replace.getText());
      }
    }

    protected void addSet() {
      String name =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.add.text"));

      if (name != null) {
        if (atrSets.containsKey(name) || !ATRPreferences.isValidName(name)) {
          JOptionPane.showMessageDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.nameexists"));
        } else {
          try {
            ATRSet set = new ATRSet(name);
            set.def = name;
            addSet(set);
            nameList.setSelectedValue(name, true);
          } catch (InvalidNameException e) {
            // already checked
          }
        }
      }
    }

    protected void addSet(ATRSet set) {
      atrSets.put(set.getName(), set);
      int index = nameList.getSelectedIndex() + 1;
      nameListModel.add(index >= 0 ? index : nameListModel.size(), set.getName());
    }

    protected void renameSet() {
      if (nameList.getSelectedValue() == null)
        return;
      String newName =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.rename.text"), getCurrent());

      if ((newName != null) && !newName.trim().equals("") && !newName.equals(getCurrent())) {
        if (atrSets.containsKey(newName) || !ATRPreferences.isValidName(newName)) {
          JOptionPane.showMessageDialog(this, Resources
              .get("map.advancedtextcellrenderer.prefs.nameexists"));
        } else {
          ATRSet set = getCurrentSet();

          nameListModel.set(nameList.getSelectedIndex(), newName);
          Map<String, ATRSet> copy = copySets(atrSets);
          atrSets.clear();
          for (String name : copy.keySet()) {
            if (name.equals(set.getName())) {
              try {
                set.setName(newName);
              } catch (InvalidNameException e) {
                // already checked
              }
              atrSets.put(newName, set);
            } else {
              atrSets.put(name, copy.get(name));
            }
          }

          nameList.setSelectedValue(newName, false);

        }
      }
    }

    protected void removeSet() {
      String name = nameList.getSelectedValue();

      if (name != null) {
        removeSet(name);
      }
    }

    protected void removeSet(String name) {
      int i = nameListModel.indexOf(name);
      if (i >= 0) {
        atrSets.remove(name);
        nameListModel.remove(i);
        nameList.setSelectedIndex(i >= nameListModel.getSize() ? 0 : i);

        updateSet();
      }
    }

    protected void importSet() {
      JFileChooser jfc = new JFileChooser(Client.getResourceDirectory());
      int ret = jfc.showOpenDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        java.io.File f = jfc.getSelectedFile();
        String lastName = getCurrentSet().getName();

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

        nameList.setSelectedValue(lastName, true);
      }
    }

    protected String importImpl(String name, Properties prop) {
      String oldName = name;

      while ((name != null) && (atrSets.containsKey(name) || ATRPreferences.isValidName(name))) {
        name =
            JOptionPane.showInputDialog(this, Resources.get(
                "map.advancedtextcellrenderer.prefs.nameexists2", name));
      }

      if (name == null)
        return null;

      ATRSet set;
      try {
        set = new ATRSet(oldName);
        set.load(prop);
        set.setName(name);
        addSet(set);
      } catch (InvalidNameException e) {
        // already checked or (in case of oldName ignored
      }

      return name;
    }

    protected void exportSet() {
      ATRSet set = getCurrentSet();
      if (set == null) {
        JOptionPane.showMessageDialog(this, Resources.get(
            "map.advancedtextcellrenderer.prefs.noselection"));
        return;
      }
      JFileChooser jfc = new JFileChooser(Client.getResourceDirectory());
      int ret = jfc.showSaveDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        try {
          java.io.File f = jfc.getSelectedFile();
          FileOutputStream out = new FileOutputStream(f);
          Properties prop = new Properties();

          set.save(prop);
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
