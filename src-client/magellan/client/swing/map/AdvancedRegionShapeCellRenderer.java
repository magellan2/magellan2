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
 * AdvancedRegionShapeCellRenderer.java
 *
 * Created on 17. Mai 2002, 12:40
 */
package magellan.client.swing.map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import magellan.client.MagellanContext;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.client.utils.SwingUtils;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Locales;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.library.utils.replacers.ReplacerSystem;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class AdvancedRegionShapeCellRenderer extends AbstractRegionShapeCellRenderer implements
    GameDataListener, ContextChangeable, ActionListener, MapperAware {
  private static final Logger log = Logger.getInstance(AdvancedRegionShapeCellRenderer.class);

  // current set information
  static class ARRSet {
    protected String name;
    protected ColorTable cTable;
    protected ValueMapping vMapping;
    protected String minDef;
    protected String curDef;
    protected String maxDef;

    protected String unknownString;

    protected String mapperTooltip;

    public final String zero = (Float.valueOf(0f)).toString();
    public final String pointFive = (Float.valueOf(0.5f)).toString();
    public final String one = (Float.valueOf(1f)).toString();

    ARRSet(String set) {
      if (set == null)
        throw new NullPointerException();
      name = set;
      cTable = new ColorTable();
      vMapping = new ValueMapping();
      minDef = zero;
      maxDef = one;
      curDef = pointFive;
    }

    ARRSet(Properties localSettings, String name, String propKey) {
      if (name == null)
        throw new NullPointerException("empty name");
      this.name = name;
      cTable = new ColorTable();
      vMapping = new ValueMapping();
      String propKey2 = propKey + "." + name;

      StringTokenizer st =
          new StringTokenizer(localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS, zero + ";0,0,0;" + one
                  + ";255,255,255"), ";");
      cTable.removeAll();

      while (st.hasMoreTokens()) {
        try {
          String key = st.nextToken();
          String value = st.nextToken();
          float fl = Float.parseFloat(key);
          cTable.addEntry(fl, Colors.decode(value));
        } catch (RuntimeException exc) {
          log.warn("error while reading set " + name);
        }
      }

      st =
          new StringTokenizer(localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES, zero + ";0.0;" + one + ";" + one),
              ";");
      vMapping.removeAll();

      while (st.hasMoreTokens()) {
        try {
          String key = st.nextToken();
          String value = st.nextToken();
          float f1 = Float.parseFloat(key);
          float f2 = Float.parseFloat(value);
          vMapping.addEntry(f1, f2);
        } catch (RuntimeException exc) {
          // log.warn("error while reading set "+set);
        }
      }

      minDef =
          localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM, zero);
      curDef =
          localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT, pointFive);
      maxDef =
          localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM, one);

      unknownString =
          localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_UNKNOWN, "?");

      mapperTooltip =
          localSettings.getProperty(propKey2
              + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP);

    }

    protected void save(Properties localSettings, String propKey) {
      String propKey2 = propKey + "." + name;

      Collection<Entry<Float, Color>> cEntries = cTable.getEntries();
      StringBuffer buf = new StringBuffer();

      if (cEntries.size() > 0) {
        Iterator<Entry<Float, Color>> it = cEntries.iterator();

        while (it.hasNext()) {
          Entry<Float, Color> entry = it.next();
          buf.append((entry.getKey()).floatValue());
          buf.append(';');
          buf.append(Colors.encode(entry.getValue()));

          if (it.hasNext()) {
            buf.append(';');
          }
        }

        localSettings.setProperty(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS, buf.toString());
      } else {
        localSettings.remove(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS);
      }

      Collection<Entry<Float, Float>> vEntries = vMapping.getEntries();

      if (vEntries.size() > 0) {
        Iterator<Entry<Float, Float>> it = vEntries.iterator();
        buf.setLength(0);

        while (it.hasNext()) {
          Entry<Float, Float> entry = it.next();
          buf.append((entry.getKey()).floatValue());
          buf.append(';');
          buf.append((entry.getValue()).floatValue());

          if (it.hasNext()) {
            buf.append(';');
          }
        }

        localSettings.setProperty(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES, buf.toString());
      } else {
        localSettings.remove(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES);
      }

      localSettings.setProperty(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM, minDef);
      localSettings.setProperty(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM, maxDef);
      localSettings.setProperty(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT, curDef);
      localSettings.setProperty(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_UNKNOWN, unknownString);

      if (mapperTooltip == null) {
        localSettings.remove(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP);
      } else {
        localSettings.setProperty(propKey2
            + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP, mapperTooltip);
      }

      // look if this set is registered
      boolean found = false;
      for (String aName : getAllSetNames(localSettings, propKey)) {
        if (name.equals(aName)) {
          found = true;
          break;
        }
      }

      if (!found) {
        localSettings.setProperty(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS,
            localSettings.getProperty(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS, "")
                + "," + name);
      }
    }

    public static void deleteSet(String removedSet, Properties properties, String propKey) {
      String propKey2 = propKey + "." + removedSet;
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP);
      properties.remove(propKey2
          + PropertiesHelper.ADVANCEDSHAPERENDERER_UNKNOWN);

      List<String> sets = getAllSetNames(properties, propKey);

      if (sets.size() > 0) {
        StringBuffer buf = new StringBuffer();
        for (String token : sets) {

          if (!removedSet.equals(token)) {
            if (buf.length() > 0) {
              buf.append(',');
            }

            buf.append(token);
          }
        }

        if (buf.length() > 0) {
          properties.setProperty(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS, buf
              .toString());
        } else {
          properties.remove(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS);
        }
      }
    }

    /**
     */
    public void setMinDef(String s) {
      minDef = s;
    }

    /**
     */
    public void setMaxDef(String s) {
      maxDef = s;
    }

    /**
     */
    public void setCurDef(String s) {
      curDef = s;
    }

    /**
     */
    public void setUnknown(String s) {
      unknownString = s;
    }

    @Override
    public String toString() {
      return name;
    }

    /**
     */
    public String getName() {
      return toString();
    }

    /**
     */
    public void setName(String newName) {
      if (newName == null)
        throw new NullPointerException();
      name = newName;
    }

    @Override
    public ARRSet clone() throws CloneNotSupportedException {
      Properties tempSettings = new Properties();
      save(tempSettings, "fookey");
      ARRSet result = new ARRSet(tempSettings, getName(), "fookey");
      return result;
    }

  }

  // public static class ARRPreferences {
  //
  // private Properties localSettings;
  //
  // public ARRPreferences(Properties localSettings) {
  // this.localSettings = localSettings;
  // }
  //
  // }

  ARRSet currentSett;

  protected ReplacerSystem minList;
  protected ReplacerSystem curList;
  protected ReplacerSystem maxList;

  protected boolean minEvalfed = false;
  protected boolean maxEvalfed = false;
  protected float minEvalf = 0f;
  protected float maxEvalf = 1f;

  protected Color oceanColor;
  protected Color unknownColor;

  protected JMenu contextMenu;
  protected ContextObserver obs = null;
  protected Mapper mapper = null;

  private Preferences preferences;

  private String propKey;

  /**
   * Creates new AdvancedRegionShapeCellRenderer
   */
  public AdvancedRegionShapeCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);

    contextMenu = new JMenu(getName());

    context.getEventDispatcher().addGameDataListener(this);

    setKeys(null);

  }

  protected void loadCurrentSet() {
    loadSet(settings.getProperty(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_CURRENT_SET,
        "Standard"));
  }

  public void loadSet(String set) {
    currentSett = new ARRSet(settings, set, propKey);
    applySet(currentSett);

    // TODO (stm)
    // if (!localSettings.containsKey(propKey + currentSet +
    // PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS)) {
    // saveSettings();
    // }
  }

  protected void loadGeomColors() {
    oceanColor =
        Colors.decode(settings.getProperty("GeomRenderer.OceanColor", "128,128,255").replace(';',
            ','));
    unknownColor =
        Colors.decode(settings.getProperty("GeomRenderer.UnknownColor", "128,128,128").replace(';',
            ','));
  }

  @Override
  protected Color getSingleColor(Region r) {
    boolean nonFound = false;
    float minF = 0f;

    if (minEvalfed) {
      minF = minEvalf;
    } else if (minList != null) {
      try {
        minF = parseFloat(minList.getReplacement(r));
        // log.info("min " + r.getName()+" "+minF);
      } catch (Exception e) {
        // log.info("min " + r.getName()+" --- ");
        nonFound = true;
      }
    }

    float maxF = 1f;

    if (!nonFound) {
      if (maxEvalfed) {
        maxF = maxEvalf;
      } else if (maxList != null) {
        try {
          maxF = parseFloat(maxList.getReplacement(r));
          // log.info("max " + r.getName()+" "+maxF);
        } catch (Exception e) {
          // log.info("max " + r.getName()+" --- ");
          nonFound = true;
        }
      }
    }

    float curF = 0.5f;

    if (!nonFound) {
      if (curList != null) {
        try {
          curF = parseFloat(curList.getReplacement(r));
          // if (log.isDebugEnabled()) {
          // AdvancedRegionShapeCellRenderer.log.debug("cur " + r.getName() + " " + curF);
          // }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            AdvancedRegionShapeCellRenderer.log.debug("cur " + r.getName() + " --- ");
          }
          nonFound = true;
        }
      }
    }

    if (nonFound) {
      if (r.getRegionType().isOcean())
        return oceanColor;

      return unknownColor;
    }

    float percent = (curF - minF) / (maxF - minF);

    if (percent < 0f) {
      percent = 0f;
    } else if (percent > 1f) {
      percent = 1f;
    }

    return currentSett.cTable.interpolate(currentSett.vMapping.interpolate(percent));
  }

  // we only have single colors
  @Override
  protected void getColor(Vector<Color> colors, Region r) {
    colors.clear();
  }

  /**
   * Invoked when the current game data object becomes invalid.
   */
  public void gameDataChanged(GameDataEvent e) {
    applySet(currentSett);
  }

  /**
   * Returns a list of all set names.
   */
  public List<String> getAllSetNames() {
    return getAllSetNames(settings, propKey);
  }

  protected static List<String> getAllSetNames(Properties localSettings, String propKey) {
    List<String> allSets = new ArrayList<String>();
    StringTokenizer s =
        new StringTokenizer(
            localSettings.getProperty(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS, ""),
            ",");

    while (s.hasMoreTokens()) {
      allSets.add(s.nextToken());
    }

    return allSets;
  }

  public String getCurrentSet() {
    return currentSett.getName();
  }

  protected void applySet(ARRSet set) {
    if (set.mapperTooltip != null) {
      setMapperTooltip(null, set.mapperTooltip);
    }

    // load ocean and unknown color from geom. renderer
    loadGeomColors();

    // reload replacers from Mapper
    minList = ReplacerHelp.createReplacer(set.minDef, set.unknownString);
    curList = ReplacerHelp.createReplacer(set.curDef, set.unknownString);
    maxList = ReplacerHelp.createReplacer(set.maxDef, set.unknownString);

    minEvalf = 0f;
    minEvalfed = true;

    if (minList != null) {
      try {
        minEvalf = parseFloat(minList.getReplacement(null));
        // log.info("min: "+minEvalf);
        minEvalfed = true;
      } catch (Exception exc) {
        minEvalfed = false;
      }
    }

    maxEvalf = 1f;
    maxEvalfed = true;

    if (maxList != null) {
      try {
        maxEvalf = parseFloat(maxList.getReplacement(null));
        // log.info("max: "+maxEvalf);
        maxEvalfed = true;
      } catch (Exception exc) {
        maxEvalfed = false;
      }
    }

    reprocessMenu();
  }

  private float parseFloat(Object replacement) throws ParseException {
    if (replacement instanceof Number)
      return ((Number) replacement).floatValue();
    NumberFormat f = NumberFormat.getInstance(Locales.getGUILocale());
    if (f instanceof DecimalFormat) {
      DecimalFormat df = (DecimalFormat) f;
      df.setParseBigDecimal(true);

      return df.parse(replacement.toString()).floatValue();
    }
    return f.parse(replacement.toString()).floatValue();
  }

  /**
   * Implementation via SortedMap, maybe inefficient for small number of entries
   */
  protected static class ValueMapping {
    protected SortedMap<Float, Float> values;

    /**
     * Creates a new ValueMapping object.
     */
    public ValueMapping() {
      values = new TreeMap<Float, Float>();
    }

    /**
     */
    public float addEntry(float val, float mval) {
      Float old = values.put(Float.valueOf(val), Float.valueOf(mval));

      if (old != null)
        return old.floatValue();

      return -1f;
    }

    /**
     */
    public void removeEntry(float val) {
      values.remove(Float.valueOf(val));
    }

    /**
     */
    public void removeAll() {
      values.clear();
    }

    /**
     */
    public void set(ValueMapping valueMapping) {
      removeAll();

      Iterator<Map.Entry<Float, Float>> it = valueMapping.getEntries().iterator();

      while (it.hasNext()) {
        Map.Entry<Float, Float> entry = it.next();
        values.put(entry.getKey(), entry.getValue());
      }
    }

    /**
     */
    public Collection<Float> getValues() {
      return values.values();
    }

    /**
     */
    public Collection<Float> getMappedValues() {
      return values.keySet();
    }

    /**
     */
    public Collection<Map.Entry<Float, Float>> getEntries() {
      return values.entrySet();
    }

    /**
     */
    public boolean hasValue(float f) {
      return values.containsKey(Float.valueOf(f));
    }

    /**
     */
    public float getValueAt(float f) {
      return (values.get(Float.valueOf(f))).floatValue();
    }

    /**
     * DOCUMENT-ME
     */
    public float interpolate(float f) {
      Float fl = Float.valueOf(f);
      SortedMap<Float, Float> m1 = values.headMap(fl);

      if ((m1 == null) || (m1.size() == 0))
        return (values.get(values.firstKey())).floatValue();

      SortedMap<Float, Float> m2 = values.tailMap(fl);

      if ((m2 == null) || (m2.size() == 0)) {
        float last = (values.get(values.lastKey())).floatValue();

        return last;
      }

      if (m2.firstKey().equals(fl))
        return m2.get(fl).floatValue();

      Float left = m1.lastKey();
      Float right = m2.firstKey();
      Float leftMapping = m1.get(left);
      Float rightMapping = m2.get(right);

      float percent = (f - left.floatValue()) / (right.floatValue() - left.floatValue());
      float ret =
          ((leftMapping.floatValue() * (1 - percent)) + (rightMapping.floatValue() * percent));

      return ret;
    }
  }

  /**
   * Implementation via SortedMap, maybe inefficient for small number of entries
   */
  protected static class ColorTable {
    protected SortedMap<Float, Color> colors;
    protected Map<Integer, Color> interpolBuf;
    protected List<Integer> interpolLookup;
    private int MAX_BUF = 256;

    /**
     * Creates a new ColorTable object.
     */
    public ColorTable() {
      colors = new TreeMap<Float, Color>();
      interpolBuf = new HashMap<Integer, Color>();
      interpolLookup = new LinkedList<Integer>();
    }

    /**
     * DOCUMENT-ME
     */
    public Color addEntry(float val, Color col) {
      Float fval = Float.valueOf(val);
      Color old = colors.put(fval, col);

      if (old != null) { // clear interpolation buffer
        clearBuffer();
      }

      return old;
    }

    /**
     * DOCUMENT-ME
     */
    public void removeEntry(float val) {
      Object old = colors.remove(Float.valueOf(val));

      if (old != null) {
        clearBuffer();
      }
    }

    /**
     */
    public void removeAll() {
      colors.clear();
      clearBuffer();
    }

    /**
     * DOCUMENT-ME
     */
    public void set(ColorTable colorTable) {
      removeAll();

      Iterator<Map.Entry<Float, Color>> it = colorTable.getEntries().iterator();

      while (it.hasNext()) {
        Map.Entry<Float, Color> entry = it.next();
        colors.put(entry.getKey(), entry.getValue());
      }
    }

    protected void clearBuffer() {
      interpolBuf.clear();
      interpolLookup.clear();
    }

    /**
     */
    public Collection<Color> getColors() {
      return colors.values();
    }

    /**
     */
    public Collection<Float> getValues() {
      return colors.keySet();
    }

    /**
     */
    public Collection<Map.Entry<Float, Color>> getEntries() {
      return colors.entrySet();
    }

    /**
     */
    public boolean hasValue(float f) {
      return colors.containsKey(Float.valueOf(f));
    }

    /**
     */
    public Color getColorAt(float f) {
      return colors.get(Float.valueOf(f));
    }

    /**
     * DOCUMENT-ME
     */
    public Color interpolate(float f) {
      Float fl = Float.valueOf(f);
      SortedMap<Float, Color> m1 = colors.headMap(fl);

      if ((m1 == null) || (m1.size() == 0))
        return colors.get(colors.firstKey());

      SortedMap<Float, Color> m2 = colors.tailMap(fl);

      if ((m2 == null) || (m2.size() == 0))
        return colors.get(colors.lastKey());

      if (m2.firstKey().equals(fl))
        return m2.get(fl);

      Float left = m1.lastKey();
      Float right = m2.firstKey();
      Color leftColor = m1.get(left);
      Color rightColor = m2.get(right);
      float percent = (f - left.floatValue()) / (right.floatValue() - left.floatValue());
      int red =
          (int) (((leftColor.getRed()) * (1 - percent)) + ((rightColor.getRed()) * (percent)));
      int green =
          (int) (((leftColor.getGreen()) * (1 - percent)) + ((rightColor.getGreen()) * (percent)));
      int blue =
          (int) (((leftColor.getBlue()) * (1 - percent)) + ((rightColor.getBlue()) * (percent)));
      Integer integer = Integer.valueOf(((red & 255) << 16) | ((green & 255) << 8) | (blue & 255));

      if (interpolBuf.containsKey(integer)) {
        interpolLookup.remove(integer);
        interpolLookup.add(0, integer);

        return interpolBuf.get(integer);
      }

      Color newColor = new Color(red, green, blue);

      if (interpolLookup.size() == MAX_BUF) {
        Object last = interpolLookup.remove(MAX_BUF - 1);
        interpolBuf.remove(last);
      }

      interpolBuf.put(integer, newColor);
      interpolLookup.add(0, integer);

      return newColor;
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPreferencesAdapter()
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    AdvancedRegionShapeCellRenderer.log.debug("new pref " + currentSett);
    preferences = new Preferences(this, currentSett);
    return preferences;
  }

  protected void reprocessMenu() {
    for (int i = 0; i < contextMenu.getItemCount(); ++i) {
      JMenuItem item = contextMenu.getItem(i);
      if (item != null) {
        item.removeActionListener(AdvancedRegionShapeCellRenderer.this);
      }
    }
    contextMenu.removeAll();

    for (String str : getAllSetNames()) {
      JMenuItem item = new JMenuItem(str);
      item.addActionListener(AdvancedRegionShapeCellRenderer.this);

      if (str.equals(currentSett.getName())) {
        item.setEnabled(false);
      }

      contextMenu.add(item);
    }
  }

  /**
   * Activate a set when it is selected.
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent actionEvent) {
    loadSet(actionEvent.getActionCommand());
    if (preferences != null) {
      preferences.setSet(actionEvent.getActionCommand());
    }

    if (obs != null) {
      obs.contextDataChanged();
    }
  }

  /**
   * @see magellan.client.swing.context.ContextChangeable#getContextAdapter()
   */
  public JMenuItem getContextAdapter() {
    return contextMenu;
  }

  /**
   * @see magellan.client.swing.context.ContextChangeable#setContextObserver(magellan.client.swing.context.ContextObserver)
   */
  public void setContextObserver(ContextObserver co) {
    obs = co;
  }

  // MAPPER things
  /**
   * Returns the tool tip definition of the responsible mapper.
   */
  protected String getMapperTooltip() {
    if (mapper != null)
      return mapper.getTooltipDefinition()[1];

    return null;
  }

  protected List<String> getAllMapperTooltips() {
    return mapper.getAllTooltipDefinitions();
  }

  /**
   * Changes the responsible mapper's tool tip definition
   */
  public boolean setMapperTooltip(String name, String tooltip) {
    if (mapper != null) {
      if (tooltip != null) {
        mapper.setTooltipDefinition(name, tooltip);
      }

      return true;
    }

    return false;
  }

  /**
   * @see magellan.client.swing.map.MapperAware#setMapper(magellan.client.swing.map.Mapper)
   */
  public void setMapper(Mapper mapper) {
    this.mapper = mapper;
    setKeys(mapper);
  }

  private void setKeys(Mapper mapper) {
    if (mapper != null && mapper.getID() != null) {
      propKey = PropertiesHelper.ADVANCEDSHAPERENDERER + "." + mapper.getID();
      if (getAllSetNames(settings, propKey).isEmpty()) {
        String baseKey = PropertiesHelper.ADVANCEDSHAPERENDERER;
        for (String name : getAllSetNames(settings, baseKey)) {
          ARRSet set = new ARRSet(settings, name, baseKey);
          set.save(settings, propKey);
        }
        // transferSetting(PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS, baseKey, propKey);
        transferSetting(PropertiesHelper.ADVANCEDSHAPERENDERER_S_CURRENT_SET, baseKey, propKey);
      }
    } else {
      propKey = PropertiesHelper.ADVANCEDSHAPERENDERER;
    }
    loadCurrentSet();
  }

  private void transferSetting(String subKey, String propKey1,
      String propKey2) {
    String old = settings.getProperty(propKey1 + subKey);
    if (old != null) {
      settings.setProperty(propKey2 + subKey, old);
    }
  }

  protected static class Preferences extends JPanel implements PreferencesAdapter, ActionListener,
      ListSelectionListener {
    @SuppressWarnings("hiding")
    private final Logger log = Logger.getInstance(Preferences.class);

    protected MappingPanel mPanel;
    protected ColorShowPanel cShowPanel;
    protected ColorPanel cPanel;
    protected JTextField minText;
    protected JTextField maxText;
    protected JTextArea curText;
    protected JTextField uText;

    protected JList<String> setsList;
    protected DefaultListModel<String> model;
    protected AbstractButton addSet;
    protected AbstractButton removeSet;
    protected AbstractButton renameSet;
    protected AbstractButton importSet;
    protected AbstractButton exportSet;
    protected AbstractButton setTooltip;
    protected AbstractButton showInfo;
    protected JLabel tooltipLabel;

    protected ARRSet prefSet;
    protected String currentSelection;

    // FIXME
    private Properties localSettings;
    private Properties globalSettings;
    private AdvancedRegionShapeCellRenderer arr;

    private String tooltip;
    private List<String> tooltips;

    private String propKey;

    /**
     * Creates a new Preferences object.
     */
    public Preferences(AdvancedRegionShapeCellRenderer arr, ARRSet currentSet) {
      try {
        prefSet = currentSet.clone();
      } catch (CloneNotSupportedException e) {
        log.error("illegal state", e);
      }
      currentSelection = currentSet.getName();
      localSettings = new Properties();
      this.arr = arr;
      globalSettings = arr.settings;
      propKey = arr.getPropertiesKey();

      JPanel defPanel = new JPanel(new GridBagLayout());

      // the panel is lay out in a grid
      // 0 1 2
      // -------------------------------
      // 0 | menu+list | texts |
      // - ---------------------
      // 1 | | separ |
      // - ---------------------
      // 2 | | color |
      // - ------------ -
      // 3 | | mapping |
      // - ---------------------
      // 4 | | result
      // -------------------------------

      GridBagConstraints con =
          new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH,
              GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0);

      con.gridx = 0;
      con.gridy = 0;
      con.gridheight = 1;
      con.gridwidth = 1;
      con.weightx = 1;
      con.fill = GridBagConstraints.HORIZONTAL;
      defPanel.add(createTexts(), con);

      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.weightx = 0;
      defPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      cPanel = new ColorPanel();
      cPanel.load(prefSet);

      con.fill = GridBagConstraints.HORIZONTAL;
      con.gridy++;
      con.weightx = 1;
      defPanel.add(cPanel, con);

      // con.gridy++;
      // con.fill = GridBagConstraints.HORIZONTAL;
      // con.weightx = 0;
      // mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      mPanel = new MappingPanel();
      mPanel.load(prefSet);

      con.fill = GridBagConstraints.BOTH;
      con.gridy++;
      con.gridheight = 2;
      con.weighty = 1;
      defPanel.add(mPanel, con);
      con.weighty = 0;

      con.gridheight = 1;
      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.weightx = 0;
      defPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      JPanel resultPanel = new JPanel();
      resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
      resultPanel.add(Box.createHorizontalStrut(10));

      resultPanel.add(new JLabel(Resources.get(
          "map.advancedregionshapecellrenderer.prefs.result")));

      cShowPanel = new ColorShowPanel();

      Dimension dim = new Dimension(mPanel.getPreferredSize());
      Dimension dim2 = SwingUtils.getDimension(1, 3, false);
      dim.height = dim2.height;
      cShowPanel.setPreferredSize(dim);
      cShowPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

      resultPanel.add(cShowPanel);

      con.gridy++;
      con.gridheight = 1;
      con.fill = GridBagConstraints.HORIZONTAL;
      defPanel.add(resultPanel, con);

      showInfo =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.showinfo"));
      showInfo.addActionListener(Preferences.this);

      JPanel lPanel = new JPanel();
      lPanel.setLayout(new GridBagLayout());
      GridBagConstraints con2 = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTH,
          GridBagConstraints.BOTH, con.insets, con.ipadx, con.ipady);

      lPanel.add(createMenu(), con2);
      con2.gridy++;
      con2.weighty = 1;
      lPanel.add(createList(), con2);
      con2.gridy++;
      con2.weighty = 0;
      lPanel.add(showInfo, con2);

      // con.gridwidth = 1;
      // con.gridheight = con.gridy + 1;
      // con.gridx = con.gridy = 0;
      // con.weightx = .1;
      // con.fill = GridBagConstraints.HORIZONTAL;
      // con.anchor = GridBagConstraints.CENTER;
      // con.insets = new Insets(2, 0, 2, 8);
      // defPanel.add(lPanel, con);

      setLayout(new BorderLayout(4, 4));
      defPanel.setBorder(new EtchedBorder());
      this.add(defPanel, BorderLayout.CENTER);
      this.add(lPanel, BorderLayout.WEST);
    }

    protected Component createList() {
      model = new DefaultListModel<String>();
      reprocessSets();
      setsList = new JList<String>(model);
      setsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setsList.setSelectedValue(currentSelection, true);
      setsList.addListSelectionListener(this);

      return new JScrollPane(setsList);
    }

    protected void reprocessSets() {
      String old = null;

      if (setsList != null) {
        old = setsList.getSelectedValue();
      }

      reprocessSets(old);
    }

    /**
     * Add all known sets to list model.
     * 
     * @param newSelection
     */
    protected void reprocessSets(String newSelection) {
      List<String> sets = getAllSetNames(localSettings, propKey);

      model.clear();
      for (String element : sets) {
        model.addElement(element);
      }

      if ((newSelection != null) && model.contains(newSelection)) {
        setsList.setSelectedValue(newSelection, true);
      } else {
        log.debug("old set unknown");
      }
    }

    protected Component createMenu() {
      addSet = new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.add"));
      addSet.addActionListener(Preferences.this);
      removeSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.remove"));
      removeSet.addActionListener(Preferences.this);
      renameSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.rename"));
      renameSet.addActionListener(Preferences.this);
      importSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.import"));
      importSet.addActionListener(Preferences.this);
      exportSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.export"));
      exportSet.addActionListener(Preferences.this);
      setTooltip =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.tooltip"));
      setTooltip.addActionListener(Preferences.this);
      tooltipLabel = new JLabel();
      tooltipLabel.setHorizontalAlignment(SwingConstants.CENTER);
      updateTooltipLabel();

      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints con =
          new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
              GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0);

      panel.add(addSet, con);
      con.gridy++;
      panel.add(renameSet, con);
      // con.gridy++;
      // panel.add(new JSeparator(SwingConstants.HORIZONTAL), con);
      con.gridy++;
      panel.add(removeSet, con);
      // con.gridy++;
      // panel.add(new JSeparator(SwingConstants.HORIZONTAL), con);
      con.gridy++;
      panel.add(importSet, con);
      con.gridy++;
      panel.add(exportSet, con);
      // con.gridy++;
      // panel.add(new JSeparator(SwingConstants.HORIZONTAL), con);
      con.gridy++;
      panel.add(setTooltip, con);
      con.gridy++;
      panel.add(tooltipLabel, con);

      return panel;
    }

    protected Container createTexts() {
      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints con =
          new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
              GridBagConstraints.NONE, new Insets(1, 1, 0, 0), 0, 0);

      panel.add(new JLabel(Resources.get("map.advancedregionshapecellrenderer.prefs.texts.min")),
          con);
      con.gridx++;
      minText = new JTextField(prefSet.minDef, 10);
      panel.add(minText, con);

      con.gridx++;
      panel.add(new JLabel(Resources.get("map.advancedregionshapecellrenderer.prefs.texts.value")),
          con);
      con.gridx++;
      con.weightx = 1;
      con.weighty = 1;
      con.gridheight = 3;
      con.fill = GridBagConstraints.BOTH;
      JPanel help = new JPanel(new BorderLayout());
      curText = new JTextArea(prefSet.curDef, 3, 10);
      curText.setWrapStyleWord(false);
      curText.setLineWrap(true);
      help.add(new JScrollPane(curText), BorderLayout.CENTER);
      panel.add(help, con);
      con.gridheight = 1;

      con.gridy++;
      con.weightx = 0;
      con.gridx = 0;
      con.fill = GridBagConstraints.NONE;
      panel.add(new JLabel(Resources.get("map.advancedregionshapecellrenderer.prefs.texts.max")),
          con);
      con.gridx++;
      maxText = new JTextField(prefSet.maxDef, 10);
      panel.add(maxText, con);

      con.weightx = 0;
      con.gridx = 0;
      con.gridy++;
      con.fill = GridBagConstraints.NONE;
      panel.add(new JLabel(Resources.get(
          "map.advancedregionshapecellrenderer.prefs.texts.unknown")),
          con);
      con.gridx++;
      con.weightx = 0;
      con.fill = GridBagConstraints.HORIZONTAL;
      uText = new JTextField(prefSet.unknownString, 10);
      uText.setToolTipText(Resources.get(
          "map.advancedregionshapecellrenderer.prefs.texts.unknown.tooltip"));
      panel.add(uText, con);

      return panel;
    }

    public void initPreferences() {
      tooltip = arr.getMapperTooltip();
      tooltips = arr.getAllMapperTooltips();
      localSettings = new Properties();
      for (String name : arr.getAllSetNames()) {
        new ARRSet(globalSettings, name, propKey).save(localSettings, propKey);
      }
      currentSelection = null;
      prefSet = new ARRSet("dummy");
      reprocessSets(arr.getCurrentSet());

    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      apply(currentSelection);

      for (String name : arr.getAllSetNames()) {
        ARRSet.deleteSet(name, globalSettings, propKey);
      }

      for (String key : localSettings.stringPropertyNames()) {
        globalSettings.setProperty(key, localSettings.getProperty(key));
      }

      arr.loadGeomColors();
      arr.loadSet(currentSelection);
      arr.reprocessMenu();
    }

    private void apply(String currentSelection) {
      if (currentSelection != null) {
        prefSet.setMinDef(minText.getText());
        prefSet.setMaxDef(maxText.getText());
        prefSet.setCurDef(curText.getText());
        prefSet.setUnknown(uText.getText());
        // currentSettt.setMapping(myMapping);
        // currentSettt.setTable(myTable);
        prefSet.save(localSettings, propKey);
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
      return Resources.get("map.advancedregionshapecellrenderer.prefs.title");
    }

    /**
     * Update controls.
     */
    public void dataChanged() {
      cShowPanel.repaint();
      mPanel.repaint();
      cPanel.repaint();
    }

    /**
     * React to buttons.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      if (actionEvent.getSource() == addSet) {
        addSet();
      } else if (actionEvent.getSource() == removeSet) {
        removeSet(currentSelection);
      } else if (actionEvent.getSource() == renameSet) {
        renameSet();
      } else if (actionEvent.getSource() == importSet) {
        importSet();
      } else if (actionEvent.getSource() == exportSet) {
        exportSet();
      } else if (actionEvent.getSource() == setTooltip) {
        setTooltip();
      } else if (actionEvent.getSource() == showInfo) {
        ToolTipReplacersInfo.showInfoDialog(this, Resources.get(
            "map.advancedregionshapecellrenderer.prefs.help"));
      }
    }

    protected void addSet() {
      String name =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedregionshapecellrenderer.prefs.newset"));

      if ((name != null) && !name.trim().equals("")) {
        addSet(name);
      }
    }

    protected void addSet(String name) {
      if (!checkName(name)) {
        JOptionPane.showMessageDialog(this, Resources
            .get("map.advancedregionshapecellrenderer.prefs.error.already"));

        return;
      }

      ARRSet newSet = null;
      try {
        newSet = prefSet.clone();
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return;
      }
      newSet.setName(name);
      addSet(newSet);
    }

    protected void addSet(ARRSet newSet) {
      apply(currentSelection);
      currentSelection = newSet.name;
      prefSet = newSet;
      prefSet.save(localSettings, propKey);
      updateTables();
      updateTooltipLabel();
      reprocessSets(newSet.name);
    }

    protected boolean checkName(String name) {
      for (String aName : getAllSetNames(localSettings, propKey)) {
        if (name.equals(aName))
          return false;
      }

      return true;
    }

    protected void removeSet(String removedSet) {
      ARRSet.deleteSet(removedSet, localSettings, propKey);

      List<String> sets = getAllSetNames(localSettings, propKey);

      if (sets.size() > 0) {
        currentSelection = sets.get(0);
        prefSet = new ARRSet(localSettings, currentSelection, propKey);
      } else {
        localSettings.remove(propKey + PropertiesHelper.ADVANCEDSHAPERENDERER_S_CURRENT_SET);
      }

      // TODO !!!
      // loadDefaultSet();

      updateTables();
      updateTooltipLabel();
      setsList.setSelectedIndex(-1);
      reprocessSets(currentSelection);
    }

    protected void renameSet() {
      String newName =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedregionshapecellrenderer.prefs.renameset"));

      if ((newName != null) && !newName.trim().equals("")) {
        if (!checkName(newName)) {
          JOptionPane.showMessageDialog(this, Resources
              .get("map.advancedregionshapecellrenderer.prefs.error.already"));

          return;
        }

        if (!currentSelection.equals(newName)) {
          ARRSet oldSet = prefSet;
          removeSet(currentSelection);
          oldSet.setName(newName);
          addSet(oldSet);
          // oldSet.save(localSettings);
          // reprocessSets(newName);
          // currentSelection = newName;
          // prefSet = oldSet;
          // prefSet.setName(newName);
          // prefSet.save(localSettings);
        }
      }
    }

    protected void importSet() {
      JFileChooser jfc = new JFileChooser();
      int ret = jfc.showOpenDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        File files[] = jfc.getSelectedFiles();

        if ((files == null) || (files.length == 0)) {
          importSet(jfc.getSelectedFile());
        } else {
          for (File file : files) {
            importSet(file);
          }
        }

        reprocessSets();
      }
    }

    protected void importSet(File file) {
      try {
        Properties prop = new Properties();
        FileInputStream in = new FileInputStream(file);
        prop.load(in);
        in.close();
        ARRSet newSet = new ARRSet(prop, prop.getProperty("Set.Name"),
            PropertiesHelper.ADVANCEDSHAPERENDERER);
        addSet(newSet);
      } catch (Exception ioe) {
        showIOError(ioe);
      }
    }

    protected void exportSet() {
      JFileChooser jfc = new JFileChooser();
      int ret = jfc.showSaveDialog(this);

      if (ret == JFileChooser.APPROVE_OPTION) {
        try {
          File file = jfc.getSelectedFile();

          if (file.exists()) {
            file.delete();
          }

          Properties prop = new Properties();
          prefSet.save(prop, PropertiesHelper.ADVANCEDSHAPERENDERER);
          prop.setProperty("Set.Name", prefSet.getName());

          FileOutputStream out = new FileOutputStream(file);
          prop.store(out, null);
          out.close();
        } catch (IOException ioe) {
          showIOError(ioe);
        }
      }
    }

    protected void setTooltip() {
      // make a list of all available tooltips plus "none" and "current"
      List<Object> list = new LinkedList<Object>();
      int selIndex = 2;

      // add "none"
      list.add(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.none"));

      if (getMapperTooltip() == null) {
        selIndex = 0;
      }

      // add "current"
      list.add(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.current"));

      if ((getMapperTooltip() != null) && getMapperTooltip().equals(curText.getText())) {
        selIndex = 1;
      }

      // add "manual input"
      list.add(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.manual"));

      // add all mapper tool tips
      List<String> l = getMapperTooltipDefinitions();

      Iterator<String> it = l.iterator();
      int i = 3;

      while (it.hasNext()) {
        String name = it.next();
        String tip = it.next();

        if ((prefSet.mapperTooltip != null) && tip.equals(prefSet.mapperTooltip)) {
          selIndex = i;
        }

        list.add(new StringPair(name, tip));
        i++;
      }

      Object o[] = list.toArray();
      Object ret =
          JOptionPane.showInputDialog(this, Resources
              .get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.choose"), Resources
                  .get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.title"),
              JOptionPane.QUESTION_MESSAGE, null, o, o[selIndex]);

      if (ret != null) {
        if (ret == o[0]) {
          // no tool tip
          prefSet.mapperTooltip = null;
        } else if (ret == o[1]) {
          // current tool tip
          prefSet.mapperTooltip = curText.getText();
        } else if (ret == o[2]) {
          String tip =
              JOptionPane.showInputDialog(this, Resources
                  .get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.input"),
                  prefSet.mapperTooltip);

          if (tip != null) {
            prefSet.mapperTooltip = tip;
          }
        } else {
          StringPair pair = (StringPair) ret;
          prefSet.mapperTooltip = pair.s2;
        }
        updateTooltipLabel();
      }
    }

    protected String getMapperTooltip() {
      return tooltip;
    }

    protected List<String> getMapperTooltipDefinitions() {
      return tooltips;
    }

    protected void showIOError(Exception ioe) {
      log.error(ioe);
      JOptionPane.showMessageDialog(this, ioe);
    }

    protected void updateTooltipLabel() {
      if (prefSet.mapperTooltip == null) {
        tooltipLabel.setText(Resources
            .get("map.advancedregionshapecellrenderer.prefs.menu.tlabel.none"));
      } else {
        String s = prefSet.mapperTooltip;

        if (s.length() > 20) {
          s = s.substring(0, 18) + "...";
        }

        tooltipLabel.setText(s);
      }
    }

    protected void updateTables() {
      cPanel.load(prefSet);
      mPanel.load(prefSet);

      minText.setText(prefSet.minDef);
      curText.setText(prefSet.curDef);
      maxText.setText(prefSet.maxDef);
      uText.setText(prefSet.unknownString);
      dataChanged();
    }

    /**
     * React to selection of sets in the list.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
      if (setsList.getSelectedValue() != null) {
        if (!setsList.getSelectedValue().equals(currentSelection)) {
          apply(currentSelection);
          setSet(setsList.getSelectedValue(), false);
        }
      }
    }

    private void setSet(String newSet) {
      setSet(newSet, true);
    }

    private void setSet(String newSet, boolean setList) {
      currentSelection = newSet;
      prefSet = new ARRSet(localSettings, currentSelection, propKey);
      updateTables();
      updateTooltipLabel();
      if (setList) {
        setsList.setSelectedValue(currentSelection, true);
      }
    }

    protected class StringPair {
      String s1;
      String s2;
      String s3;

      /**
       * Creates a new StringPair object.
       */
      public StringPair(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        s3 = s1 + ": " + s2;

        if (s3.length() > 50) {
          s3 = s3.substring(0, 28) + "...";
        }
      }

      /**
       */
      @Override
      public String toString() {
        return s3;
      }
    }

    protected class ColorShowPanel extends JPanel {
      protected boolean showMapping = true;

      /**
       * Creates a new ColorShowPanel object.
       */
      public ColorShowPanel() {
      }

      /**
       * Creates a new ColorShowPanel object.
       */
      public ColorShowPanel(boolean show) {
        showMapping = show;
      }

      /**
       * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
       */
      @Override
      public void paintComponent(Graphics g) {
        Dimension size = this.getSize();
        float percent = 0;

        for (int i = 0; i < size.width; i++) {
          percent = ((float) i) / ((float) size.width);

          if (showMapping) {
            percent = prefSet.vMapping.interpolate(percent);
          }

          g.setColor(prefSet.cTable.interpolate(percent));
          g.drawLine(i, 0, i, size.height);
        }
      }
    }

    protected class ColorPanel extends JPanel implements MouseListener, MouseMotionListener,
        ActionListener {
      protected int moveIndex = -1;
      protected int lastMoveIndex = -1;
      protected List<Float> value;
      protected List<Color> mapping;
      protected Cursor normCursor;
      protected Cursor gripCursor;
      protected Cursor moveCursor;
      protected Cursor handCursor;
      protected ColorShowPanel myShow;
      protected ColorPainter mapPainter;
      protected JPopupMenu popup;
      protected JMenuItem addItem;
      protected JMenuItem removeItem;
      protected JMenuItem modifyItem;
      protected int addX = -1;
      protected JTextField textValue;
      protected java.text.NumberFormat number;

      /**
       * Creates a new ColorPanel object.
       */
      public ColorPanel() {
        try {
          number = java.text.NumberFormat.getInstance(Locales.getGUILocale());
        } catch (IllegalStateException ise) {
          number = java.text.NumberFormat.getInstance();
        }

        number.setMaximumFractionDigits(3);
        number.setMinimumFractionDigits(0);

        setLayout(new BorderLayout());

        myShow = new ColorShowPanel(false);
        SwingUtils.setPreferredSize(myShow, 15, 3, false);
        mapPainter = new ColorPainter();
        SwingUtils.setPreferredSize(mapPainter, 15, 3, false);
        mapPainter.addMouseListener(this);
        mapPainter.addMouseMotionListener(this);
        myShow.addMouseListener(this);

        JPanel help = new JPanel(new BorderLayout());
        help.add(myShow, BorderLayout.CENTER);
        help.add(mapPainter, BorderLayout.SOUTH);
        help.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Dimension dim = SwingUtils.getDimension(20, 6, false);
        help.setMinimumSize(dim);
        help.setPreferredSize(dim);
        this.add(help, BorderLayout.CENTER);

        textValue = new JTextField(5);
        textValue.addActionListener(this);
        textValue.setEditable(false);
        textValue.setEnabled(true);
        help = new JPanel(new FlowLayout(FlowLayout.CENTER));
        help.add(textValue);
        this.add(help, BorderLayout.SOUTH);

        normCursor = getCursor();
        gripCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        handCursor = new Cursor(Cursor.HAND_CURSOR);

        value = new LinkedList<Float>();
        mapping = new LinkedList<Color>();

        createPopup();
      }

      protected void createPopup() {
        popup = new JPopupMenu();

        addItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup.add"));
        addItem.addActionListener(ColorPanel.this);
        removeItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup.remove"));
        removeItem.addActionListener(ColorPanel.this);
        modifyItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup.modify"));
        modifyItem.addActionListener(ColorPanel.this);
        JMenuItem removeAllItem = new JMenuItem(Resources.get(
            "map.advancedregionshapecellrenderer.prefs.popup.removeall"));
        removeAllItem.setActionCommand("removeall");
        removeAllItem.addActionListener(ColorPanel.this);
        popup.add(removeAllItem);
      }

      protected void fillPopup(int type) {
        popup.remove(addItem);
        popup.remove(modifyItem);
        popup.remove(removeItem);

        if (type == -1) {
          popup.add(addItem, 0);
        } else {
          popup.add(modifyItem, 0);

          if ((type != 0) && (type != (value.size() - 1))) {
            popup.add(removeItem, 1);
          }
        }
      }

      /**
       *
       */
      public void clear() {
        value.clear();
        mapping.clear();
      }

      protected void load(ARRSet set) {
        clear();
        Collection<Map.Entry<Float, Color>> col = set.cTable.getEntries();
        Iterator<Map.Entry<Float, Color>> it = col.iterator();

        while (it.hasNext()) {
          Map.Entry<Float, Color> entry = it.next();
          value.add(entry.getKey());
          mapping.add(entry.getValue());
        }
      }

      protected void save() {
        prefSet.cTable.removeAll();

        if (value.size() > 0) {
          for (int i = 0; i < value.size(); i++) {
            prefSet.cTable.addEntry((value.get(i)).floatValue(), mapping.get(i));
          }
        }
      }

      protected int find(int x, int y, int maxDiff) {
        if (value.size() > 0) {
          int width = mapPainter.getWidth();

          for (int i = 0; i < value.size(); i++) {
            Float next = value.get(i);
            int j = (int) (next.floatValue() * width);

            if (Math.abs(x - j) <= maxDiff)
              return i;
          }
        }

        return -1;
      }

      protected void setText(int index) {
        lastMoveIndex = index;

        if (index == -1) {
          textValue.setText(null);
        } else {
          textValue.setText(number.format(value.get(index)));
        }
      }

      protected void searchMoving(MouseEvent e) {
        moveIndex = find(e.getX(), e.getY(), 5);

        // never move endings
        if ((moveIndex == 0) || (moveIndex == (value.size() - 1))) {
          moveIndex = -1;
        }

        if (moveIndex >= 0) {
          mapPainter.setCursor(moveCursor);
          setText(moveIndex);
        }
      }

      protected boolean moveTo(int index, float newValue) {
        if ((newValue == 1f) || (newValue == 0f))
          return false;

        if (value.size() > 0) {
          for (Float element : value) {
            if (newValue == (element).floatValue())
              return false;
          }
        }

        value.set(index, Float.valueOf(newValue));
        setText(index);
        save();
        dataChanged();

        return true;
      }

      protected void moved(MouseEvent e) {
        if (moveIndex >= 0) {
          float width = mapPainter.getWidth();
          float oldX = value.get(moveIndex).floatValue();

          float newHoriz = e.getX() / width;

          if (newHoriz < 0) {
            newHoriz = 0f;
          }

          if (newHoriz > 1) {
            newHoriz = 1f;
          }

          if (newHoriz != oldX) {
            // don't move endings horizontally
            // check sides
            float leftX = value.get(moveIndex - 1).floatValue();

            if (newHoriz <= leftX) {
              int lx = (int) (leftX * width);
              newHoriz = (lx + 1) / width;
            }

            float rightX = value.get(moveIndex + 1).floatValue();

            if (rightX <= newHoriz) {
              int rx = (int) (rightX * width);
              newHoriz = (rx - 1) / width;
            }

            value.set(moveIndex, Float.valueOf(newHoriz));
            setText(moveIndex);
            save();
            dataChanged();
          }
        }
      }

      /**
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        if (SwingUtilities.isRightMouseButton(mouseEvent) && (moveIndex == -1)) {
          addX = find(mouseEvent.getX(), mouseEvent.getY(), 5);
          fillPopup(addX);

          if (addX == -1) {
            addX = mouseEvent.getX();
          }

          popup.show(mapPainter, mouseEvent.getX(), mouseEvent.getY());

          return;
        }
        if (mouseEvent.getSource() == mapPainter) {
          searchMoving(mouseEvent);
        }
      }

      /**
       * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
       */
      public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       */
      public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
          moveIndex = -1;
          mapPainter.setCursor(gripCursor);
        }
      }

      /**
       * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
       */
      public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
       */
      public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * React to user actions.
       *
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if (actionEvent.getSource() == addItem) {
          addColor();

          return;
        } else if (actionEvent.getSource() == removeItem) {
          removeColor();

          return;
        } else if (actionEvent.getSource() == modifyItem) {
          changeColor();

          return;
        } else if (actionEvent.getSource() == textValue) {
          textChange();

          return;
        } else if (actionEvent.getActionCommand().equals("removeall")) {
          if (JOptionPane.showConfirmDialog(this, Resources
              .get("map.advancedregionshapecellrenderer.prefs.removeall.confirm"), null,
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            removeAllColors();
          }

          return;
        }
      }

      protected void textChange() {
        if (lastMoveIndex != -1) {
          try {
            Number nf = number.parse(textValue.getText());
            float fl = nf.floatValue();

            if (fl <= 0) {
              fl = 0.001f;
            } else if (fl >= 1) {
              fl = 0.999f;
            }

            textValue.setText(number.format(fl));
            value.set(lastMoveIndex, Float.valueOf(fl));
            save();
            dataChanged();
          } catch (java.text.ParseException exc) {
            setText(lastMoveIndex);
          }
        }
      }

      protected void addColor() {
        int x = addX;
        int width = mapPainter.getWidth();
        int index = -1;
        int ox = -1;

        if (value.size() > 0) {
          Iterator<Float> it = value.iterator();

          while ((ox < x) && it.hasNext()) {
            float next = it.next().floatValue();
            ox = (int) (next * width);

            if (ox == x)
              return;

            index++;
          }
        }

        if (ox < x) {
          index++;
        }

        Color col =
            JColorChooser.showDialog(this, Resources
                .get("map.advancedregionshapecellrenderer.prefs.newcolor"), Color.white);

        if (col != null) {
          value.add(index, Float.valueOf(((float) x) / ((float) width)));
          mapping.add(index, col);
          setText(index);
          save();
          dataChanged();
        }
      }

      protected void removeColor() {
        setText(-1);
        value.remove(addX);
        mapping.remove(addX);
        save();
        dataChanged();
      }

      protected void removeAllColors() {
        setText(-1);
        while (value.size() > 2) {
          value.remove(1);
          mapping.remove(1);
        }
        save();
        dataChanged();
      }

      protected void changeColor() {
        Color col =
            JColorChooser.showDialog(this, Resources
                .get("map.advancedregionshapecellrenderer.prefs.changecolor"), mapping.get(addX));

        if (col != null) {
          mapping.set(addX, col);
          setText(addX);
          save();
          dataChanged();
        }
      }

      /**
       * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
       */
      public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
        int index = find(mouseEvent.getX(), mouseEvent.getY(), 5);

        if (index > 0) {
          if ((index > 0) && (index < (value.size() - 1))) {
            mapPainter.setCursor(gripCursor);
          } else {
            mapPainter.setCursor(normCursor);
          }
        } else {
          mapPainter.setCursor(handCursor);
        }
      }

      /**
       * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
       */
      public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
        }
      }

      protected class ColorPainter extends JPanel {
        protected Polygon poly;
        protected String numbers[];

        /**
         * Creates a new ColorPainter object.
         */
        public ColorPainter() {
          java.text.NumberFormat format = null;

          try {
            format =
                java.text.NumberFormat.getInstance(magellan.library.utils.Locales.getGUILocale());
          } catch (IllegalStateException ise) {
            format = java.text.NumberFormat.getInstance();
          }

          format.setMaximumFractionDigits(1);
          format.setMinimumFractionDigits(0);

          int x[] = new int[5];
          int y[] = new int[5];
          poly = new Polygon(x, y, 5);
          numbers = new String[11];

          for (int i = 0; i < numbers.length; i++) {
            numbers[i] = format.format(((double) i) / ((double) 10));
          }
        }

        /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        public void paintComponent(Graphics g) {
          Dimension size = this.getSize();
          g.setColor(getBackground());
          g.fillRect(0, 0, size.width, size.height);
          g.setColor(Color.black);

          FontMetrics fm = g.getFontMetrics();
          int y = size.height - 1;
          int height = y - fm.getAscent() - 1;

          for (int i = 0; i <= 10; i++) {
            int x = ((i * size.width) / 10);

            if (x == size.width) {
              x--;
            }

            g.drawLine(x, 0, x, height - 1);

            if (i == 0) {
              g.drawString(numbers[0], x, y);
            } else if (i == 10) {
              g.drawString(numbers[10], x - fm.stringWidth(numbers[10]), y);
            } else {
              g.drawString(numbers[i], x - (fm.stringWidth(numbers[i]) / 2), y);
            }
          }

          if (value.size() > 0) {
            int h = Math.max(0, size.height - fm.getAscent() - 1);
            int w = Math.min(size.height, fm.getMaxAdvance());

            for (int i = 0; i < value.size(); i++) {
              int x = (int) (size.width * value.get(i).floatValue());
              paintIcon(g, x, h, w, mapping.get(i));
            }
          }
        }

        protected void paintIcon(Graphics g, int x, int h, int w, Color col) {
          g.setColor(col);
          poly.xpoints[0] = x;
          poly.ypoints[0] = 2;

          poly.xpoints[4] = x + w / 2;
          poly.ypoints[4] = h / 3;

          poly.xpoints[3] = x + w / 2;
          poly.ypoints[3] = h - 1;

          poly.xpoints[2] = x - w / 2;
          poly.ypoints[2] = h - 1;

          poly.xpoints[1] = x - w / 2;
          poly.ypoints[1] = h / 3;
          g.fillPolygon(poly);
          g.setColor(Color.black);
          g.drawPolygon(poly);
        }
      }
    }

    protected class MappingPanel extends JPanel implements MouseListener, MouseMotionListener,
        ActionListener {
      protected int moveIndex = -1;
      protected List<Float> value;
      protected List<Float> mapping;
      protected PaintPanel mapPainter;
      protected Cursor normCursor;
      protected Cursor gripCursor;
      protected Cursor moveCursor;
      protected JPopupMenu popup;
      protected JMenuItem addItem;
      protected JMenuItem removeItem;
      protected int addX = -1;
      protected int addY = -1;

      /**
       * Creates a new MappingPanel object.
       */
      public MappingPanel() {
        SwingUtils.setPreferredSize(this, 12, 8, true);
        setLayout(new BorderLayout());

        normCursor = getCursor();
        gripCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);

        mapPainter = new PaintPanel();
        mapPainter.addMouseListener(this);
        mapPainter.addMouseMotionListener(this);

        JPanel help = new JPanel();
        help.setLayout(new BorderLayout());
        help.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        help.add(mapPainter);
        this.add(help, BorderLayout.CENTER);

        value = new LinkedList<Float>();
        mapping = new LinkedList<Float>();

        createPopup();
      }

      protected void createPopup() {
        popup = new JPopupMenu();

        addItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup2.add"));
        addItem.addActionListener(MappingPanel.this);
        removeItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup2.remove"));
        removeItem.addActionListener(MappingPanel.this);
      }

      protected void fillPopup(int type) {
        popup.removeAll();

        if (type == -1) {
          popup.add(addItem);
        } else {
          popup.add(removeItem);
        }
      }

      /**
       *
       */
      public void clear() {
        value.clear();
        mapping.clear();
      }

      protected void load(ARRSet set) {
        clear();
        Collection<Map.Entry<Float, Float>> col = set.vMapping.getEntries();
        Iterator<Map.Entry<Float, Float>> it = col.iterator();

        while (it.hasNext()) {
          Map.Entry<Float, Float> entry = it.next();
          value.add(entry.getKey());
          mapping.add(entry.getValue());
        }
      }

      protected void save() {
        prefSet.vMapping.removeAll();

        if (value.size() > 0) {
          for (int i = 0; i < value.size(); i++) {
            prefSet.vMapping.addEntry(value.get(i).floatValue(), mapping.get(i).floatValue());
          }
        }
      }

      protected int find(int x, int y, int maxDiff) {
        if (value.size() > 0) {
          int width = mapPainter.getWidth();
          int height = mapPainter.getHeight();

          for (int i = 0; i < value.size(); i++) {
            Float next = value.get(i);
            int j = (int) (next.floatValue() * width);

            if (Math.abs(x - j) <= maxDiff) {
              Float oheight = mapping.get(i);
              int k = (int) ((1f - oheight.floatValue()) * height);

              if (Math.abs(y - k) <= maxDiff)
                return i;
            }
          }
        }

        return -1;
      }

      protected void searchMoving(MouseEvent e) {
        moveIndex = find(e.getX(), e.getY(), 5);

        if (moveIndex >= 0) {
          mapPainter.setCursor(moveCursor);
        }
      }

      protected void moved(MouseEvent e) {
        if (moveIndex >= 0) {
          float width = mapPainter.getWidth();
          float height = mapPainter.getHeight();
          float oldX = value.get(moveIndex).floatValue();
          float oldY = mapping.get(moveIndex).floatValue();
          float newVert = (height - e.getY()) / height;

          if (newVert < 0) {
            newVert = 0f;
          }

          if (newVert > 1) {
            newVert = 1f;
          }

          float newHoriz = e.getX() / width;

          if (newHoriz < 0) {
            newHoriz = 0f;
          }

          if (newHoriz > 1) {
            newHoriz = 1f;
          }

          if ((newVert != oldY) || (newHoriz != oldX)) {
            // don't move endings horizontally
            if ((moveIndex == 0) || (moveIndex == (value.size() - 1))) {
              if (newVert != oldY) {
                mapping.set(moveIndex, Float.valueOf(newVert));
              } else
                return;
            } else {
              // check sides
              if (newHoriz != oldX) {
                float leftX = value.get(moveIndex - 1).floatValue();

                if (newHoriz <= leftX) {
                  int lx = (int) (leftX * width);
                  newHoriz = (lx + 1) / width;
                }

                float rightX = value.get(moveIndex + 1).floatValue();

                if (rightX <= newHoriz) {
                  int rx = (int) (rightX * width);
                  newHoriz = (rx - 1) / width;
                }

                value.set(moveIndex, Float.valueOf(newHoriz));
              }

              if (newVert != oldY) {
                mapping.set(moveIndex, Float.valueOf(newVert));
              }
            }

            save();
            dataChanged();
          }
        }
      }

      protected void addMapping(int x, int y) {
        int width = mapPainter.getWidth();
        int index = -1;
        int ox = -1;

        if (value.size() > 0) {
          Iterator<Float> it = value.iterator();

          while ((ox < x) && it.hasNext()) {
            float next = it.next().floatValue();
            ox = (int) (next * width);

            if (ox == x)
              return;

            index++;
          }
        }

        if (ox < x) {
          index++;
        }

        value.add(index, Float.valueOf(((float) x) / ((float) width)));
        mapping.add(index, Float.valueOf(((float) (mapPainter.getHeight() - y))
            / ((float) mapPainter.getHeight())));
        save();
        dataChanged();
      }

      protected void removeMapping(int index) {
        if ((index > 0) && (index < (value.size() - 1))) {
          value.remove(index);
          mapping.remove(index);
          save();
          dataChanged();
        }
      }

      /**
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        if (SwingUtilities.isRightMouseButton(mouseEvent) && (moveIndex == -1)) {
          addX = find(mouseEvent.getX(), mouseEvent.getY(), 5);
          fillPopup(addX);

          if (addX == -1) {
            addX = mouseEvent.getX();
            addY = mouseEvent.getY();
          }

          popup.show(mapPainter, mouseEvent.getX(), mouseEvent.getY());

          return;
        }

        searchMoving(mouseEvent);
      }

      /**
       * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
       */
      public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       */
      public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
          moveIndex = -1;
          mapPainter.setCursor(gripCursor);
        }
      }

      /**
       * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
       */
      public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
       */
      public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        //
      }

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if (actionEvent.getSource() == addItem) {
          addMapping(addX, addY);

          return;
        } else if (actionEvent.getSource() == removeItem) {
          removeMapping(addX);

          return;
        }
      }

      /**
       * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
       */
      public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {
        int index = find(mouseEvent.getX(), mouseEvent.getY(), 5);

        if (index >= 0) {
          mapPainter.setCursor(gripCursor);
        } else {
          mapPainter.setCursor(normCursor);
        }
      }

      /**
       * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
       */
      public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
        }
      }

      protected class PaintPanel extends JPanel {
        protected Stroke stroke;

        /**
         * Creates a new PaintPanel object.
         */
        public PaintPanel() {
          float dashs[] = { 1f, 4f };
          stroke =
              new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.5f, dashs, 0f);
        }

        /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        public void paintComponent(Graphics g) {
          Dimension size = this.getSize();
          g.setColor(getBackground());
          g.fillRect(0, 0, size.width, size.height);

          float fWidth = size.width;
          float fHeight = size.height;
          Stroke oldStroke = null;

          if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(stroke);
          }

          g.setColor(Color.darkGray);

          for (int i = 1; i < 10; i++) {
            int x = (i * size.width) / 10;
            g.drawLine(x, 0, x, size.height);

            int y = (i * size.height) / 10;
            g.drawLine(0, y, size.width, y);
          }

          g.setColor(Color.black);

          if (oldStroke != null) {
            ((Graphics2D) g).setStroke(oldStroke);
          }

          if (value.size() > 0) {
            int lastX = -1;
            int lastY = -1;

            for (int i = 0; i < value.size(); i++) {
              int x = (int) (value.get(i).floatValue() * fWidth);
              int y = (int) ((1 - mapping.get(i).floatValue()) * fHeight);
              g.fillRect(x - 2, y - 2, 5, 5);

              if (lastX >= 0) {
                g.drawLine(lastX, lastY, x, y);
              }

              lastX = x;
              lastY = y;
            }
          }
        }
      }
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.advancedregionshapecellrenderer.name");
  }

  public String getPropertiesKey() {
    return propKey;
  }
}
