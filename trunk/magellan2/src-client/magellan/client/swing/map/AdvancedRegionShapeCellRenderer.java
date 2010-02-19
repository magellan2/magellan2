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
import java.awt.Window;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
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
import javax.swing.event.ListSelectionListener;

import magellan.client.MagellanContext;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Colors;
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
  class ARRSet {
    protected String name;
    protected ColorTable cTable;
    protected ValueMapping vMapping;
    protected String minDef;
    protected String curDef;
    protected String maxDef;

    protected String unknownString;

    protected String mapperTooltip;

    public final String zero = (new Float(0)).toString();
    public final String pointFive = (new Float(0.5)).toString();
    public final String one = (new Float(1)).toString();

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

    ARRSet(Properties settings, String set) {
      if (set == null)
        throw new NullPointerException("empty name");
      name = set;
      cTable = new ColorTable();
      vMapping = new ValueMapping();

      StringTokenizer st =
          new StringTokenizer(settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
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
        }
      }

      st =
          new StringTokenizer(settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
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
        }
      }

      minDef =
          settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
              + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM, zero);
      curDef =
          settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
              + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT, pointFive);
      maxDef =
          settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
              + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM, one);

      unknownString =
          settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
              + PropertiesHelper.ADVANCEDSHAPERENDERER_UNKNOWN, "?");

      mapperTooltip =
          settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + set
              + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP);

    }

    protected void save(Properties settings) {
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

        settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
            + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS, buf.toString());
      } else {
        settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + name
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

        settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
            + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES, buf.toString());
      } else {
        settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + name
            + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES);
      }

      settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM, minDef);
      settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM, maxDef);
      settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
          + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT, curDef);

      if (mapperTooltip == null) {
        settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + name
            + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP);
      } else {
        settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + name
            + PropertiesHelper.ADVANCEDSHAPERENDERER_TOOLTIP, mapperTooltip);
      }

      // look if this set is registered
      StringTokenizer tokens =
          new StringTokenizer(
              settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, ""), ",");
      boolean found = false;

      while (!found && tokens.hasMoreTokens()) {
        found = (name.equals(tokens.nextToken()));
      }

      if (!found) {
        settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, settings.getProperty(
            PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, "")
            + "," + name);
      }

    }

    /**
     * DOCUMENT-ME
     */
    public void setMinDef(String s) {
      minDef = s;
    }

    /**
     * DOCUMENT-ME
     */
    public void setMaxDef(String s) {
      maxDef = s;
    }

    /**
     * DOCUMENT-ME
     */
    public void setCurDef(String s) {
      curDef = s;
    }

    /**
     * DOCUMENT-ME
     */
    public void setUnknown(String s) {
      unknownString = s;
    }

    @Override
    public String toString() {
      return name;
    }

    public String getName() {
      return toString();
    }

    public void setName(String newName) {
      if (newName == null)
        throw new NullPointerException();
      name = newName;
    }

    @Override
    public ARRSet clone() throws CloneNotSupportedException {
      ARRSet result = new ARRSet(getName());
      result.cTable = cTable;
      result.vMapping = vMapping;
      result.minDef = minDef;
      result.maxDef = maxDef;
      result.unknownString = unknownString;
      result.mapperTooltip = mapperTooltip;
      return result;
    }
  }

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
  protected String lastMapperDef = null;

  private Preferences preferences;

  /**
   * Creates new AdvancedRegionShapeCellRenderer
   */
  public AdvancedRegionShapeCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);

    contextMenu = new JMenu(getName());

    context.getEventDispatcher().addGameDataListener(this);

    loadCurrentSet();
  }

  protected void loadCurrentSet() {
    loadSet(settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT_SET, "Standard"));
  }

  protected void loadSet(String set) {
    currentSett = new ARRSet(settings, set);
    applySet(currentSett);

    // TODO (stm)
    // if (!settings.containsKey(PropertiesHelper.ADVANCEDSHAPERENDERER + currentSet +
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
          if (log.isDebugEnabled()) {
            AdvancedRegionShapeCellRenderer.log.debug("cur " + r.getName() + " " + curF);
          }
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
  protected Color[] getColor(Region r) {
    return null;
  }

  /**
   * Invoked when the current game data object becomes invalid.
   */
  public void gameDataChanged(GameDataEvent e) {
    applySet(currentSett);
  }

  protected void applySet(ARRSet set) {
    lastMapperDef = getMapperTooltip();
    if (set.mapperTooltip != null) {
      setMapperTooltip(set.mapperTooltip);
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
  protected class ValueMapping {
    protected SortedMap<Float, Float> values;

    /**
     * Creates a new ValueMapping object.
     */
    public ValueMapping() {
      values = new TreeMap<Float, Float>();
    }

    /**
     * DOCUMENT-ME
     */
    public float addEntry(float val, float mval) {
      Float fval = new Float(val);
      Float old = values.put(fval, new Float(mval));

      if (old != null)
        return old.floatValue();

      return -1f;
    }

    /**
     * DOCUMENT-ME
     */
    public void removeEntry(float val) {
      values.remove(new Float(val));
    }

    /**
     * DOCUMENT-ME
     */
    public void removeAll() {
      values.clear();
    }

    /**
     * DOCUMENT-ME
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
     * DOCUMENT-ME
     */
    public Collection<Float> getValues() {
      return values.values();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Float> getMappedValues() {
      return values.keySet();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Map.Entry<Float, Float>> getEntries() {
      return values.entrySet();
    }

    /**
     * DOCUMENT-ME
     */
    public boolean hasValue(float f) {
      return values.containsKey(new Float(f));
    }

    /**
     * DOCUMENT-ME
     */
    public float getValueAt(float f) {
      return (values.get(new Float(f))).floatValue();
    }

    /**
     * DOCUMENT-ME
     */
    public float interpolate(float f) {
      Float fl = new Float(f);
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
  protected class ColorTable {
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
      Float fval = new Float(val);
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
      Object old = colors.remove(new Float(val));

      if (old != null) {
        clearBuffer();
      }
    }

    /**
     * DOCUMENT-ME
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
     * DOCUMENT-ME
     */
    public Collection<Color> getColors() {
      return colors.values();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Float> getValues() {
      return colors.keySet();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Map.Entry<Float, Color>> getEntries() {
      return colors.entrySet();
    }

    /**
     * DOCUMENT-ME
     */
    public boolean hasValue(float f) {
      return colors.containsKey(new Float(f));
    }

    /**
     * DOCUMENT-ME
     */
    public Color getColorAt(float f) {
      return colors.get(new Float(f));
    }

    /**
     * DOCUMENT-ME
     */
    public Color interpolate(float f) {
      Float fl = new Float(f);
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
      Integer integer = new Integer(((red & 255) << 16) | ((green & 255) << 8) | (blue & 255));

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
    preferences = new Preferences(currentSett);
    return preferences;
  }

  protected void reprocessMenu() {
    for (int i = 0; i < contextMenu.getItemCount(); ++i) {
      JMenuItem item = contextMenu.getItem(i);
      if (item != null) {
        item.removeActionListener(this);
      }
    }
    contextMenu.removeAll();

    StringTokenizer s =
        new StringTokenizer(settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, ""),
            ",");

    while (s.hasMoreTokens()) {
      String str = s.nextToken();
      JMenuItem item = new JMenuItem(str);
      item.addActionListener(this);

      if (str.equals(currentSett.getName())) {
        item.setEnabled(false);
      }

      contextMenu.add(item);
    }
  }

  /**
   * DOCUMENT-ME
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
   * DOCUMENT-ME
   */
  public JMenuItem getContextAdapter() {
    return contextMenu;
  }

  /**
   * DOCUMENT-ME
   */
  public void setContextObserver(ContextObserver co) {
    obs = co;
  }

  // MAPPER things
  /**
   * DOCUMENT-ME
   */
  public String getMapperTooltip() {
    if (mapper != null)
      return mapper.getTooltipDefinition();

    return null;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean setMapperTooltip(String tooltip) {
    if (mapper != null) {
      if (tooltip != null) {
        mapper.setTooltipDefinition(tooltip);
      }

      return true;
    }

    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public void setMapper(Mapper mapper) {
    this.mapper = mapper;
  }

  protected class Preferences extends JPanel implements PreferencesAdapter, ActionListener,
      ListSelectionListener {
    private final Logger log = Logger.getInstance(Preferences.class);

    protected MappingPanel mPanel;
    protected ColorShowPanel cShowPanel;
    protected ColorPanel cPanel;
    protected JTextField minText;
    protected JTextField maxText;
    protected JTextField curText;
    protected JTextField uText;

    protected javax.swing.border.Border selBorder;
    protected javax.swing.border.Border nonSelBorder;

    protected JList list;
    protected DefaultListModel model;
    protected AbstractButton addSet;
    protected AbstractButton removeSet;
    protected AbstractButton renameSet;
    protected AbstractButton importSet;
    protected AbstractButton exportSet;
    protected AbstractButton setTooltip;
    protected AbstractButton showInfo;
    protected JLabel tooltipLabel;

    ARRSet prefSet;
    String currentSelection;

    private ToolTipReplacersInfo infoDialog;

    /**
     * Creates a new Preferences object.
     */
    public Preferences(ARRSet currentSet) {
      prefSet = currentSet;
      currentSelection = currentSet.getName();

      JPanel mainPanel = new JPanel(new GridBagLayout());

      // the panel is lay out in a 3x9 grid
      // 0 1 2
      // -------------------------------
      // 0 | menu | texts |
      // - ---------------------
      // 1 | | separ
      // - ---------------------
      // 2 | | color | info |
      // - ------------ -
      // 3 | | separ | |
      // - ------------ -
      // 4 | | mapping | |
      // ---------- - -
      // 5 | list | | |
      // - ---------------------
      // 6 | | separ
      // - ---------------------
      // 7 | | result
      // -------------------------------

      GridBagConstraints con =
          new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTH,
              GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0);

      selBorder =
          BorderFactory.createCompoundBorder(BorderFactory
              .createBevelBorder(javax.swing.border.BevelBorder.LOWERED), BorderFactory
              .createEmptyBorder(2, 2, 2, 2));
      nonSelBorder =
          BorderFactory.createCompoundBorder(BorderFactory
              .createBevelBorder(javax.swing.border.BevelBorder.RAISED), BorderFactory
              .createEmptyBorder(2, 2, 2, 2));

      JPanel infoPanel = new JPanel(new GridBagLayout());
      GridBagConstraints infoPanelConstraint =
          new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
              GridBagConstraints.BOTH, new Insets(2, 0, 2, 0), 0, 0);
      JTextArea info = null;
      try {
        info =
            new JTextArea(Resources.get("map.advancedregionshapecellrenderer.prefs.help"), 10, 10);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
        info.setSelectionColor(mainPanel.getBackground());
        info.setSelectedTextColor(mainPanel.getForeground());
        info.setRequestFocusEnabled(false);
        info.setBackground(mainPanel.getBackground());
        info.setSelectionColor(mainPanel.getBackground());
        info.setSelectedTextColor(mainPanel.getForeground());
        info.setFont(new JLabel().getFont());
        infoPanel.add(new JScrollPane(info), infoPanelConstraint);
      } catch (Exception exc) {
        info = null;
      }

      infoPanelConstraint.gridy++;
      infoPanelConstraint.weighty = 0;
      infoPanelConstraint.fill = GridBagConstraints.NONE;
      infoPanelConstraint.anchor = GridBagConstraints.CENTER;
      showInfo =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.showinfo"));
      showInfo.addActionListener(this);
      infoPanel.add(showInfo, infoPanelConstraint);

      Insets oldInsets = con.insets;

      if (oldInsets != null) {
        con.insets =
            new Insets(oldInsets.top, Math.max(oldInsets.left, 5), oldInsets.bottom,
                oldInsets.right);
      } else {
        con.insets = new Insets(1, 5, 1, 1);
      }

      con.insets = oldInsets;

      con.gridx = 1;
      con.gridy = 0;
      con.gridheight = 1;
      con.gridwidth = 2;
      con.weightx = .5;
      con.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(createTexts(), con);

      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.weightx = 0;
      mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      cPanel = new ColorPanel();
      cPanel.load(prefSet);

      con.fill = GridBagConstraints.NONE;
      con.gridwidth = 1;
      con.gridy++;
      con.weightx = 0.2;
      mainPanel.add(cPanel, con);

      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      mPanel = new MappingPanel();
      mPanel.load(prefSet);

      con.fill = GridBagConstraints.BOTH;
      con.gridy++;
      con.gridheight = 2;
      mainPanel.add(mPanel, con);

      con.gridx++;
      con.gridy -= 2;
      con.fill = GridBagConstraints.BOTH;
      con.gridwidth = 1;
      con.gridheight = 4;
      con.weightx = .8;
      mainPanel.add(infoPanel, con);

      con.gridheight = 1;
      con.gridwidth = 2;
      con.gridx--;
      con.gridy += 4;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.weightx = 0;
      mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), con);

      JPanel resultPanel = new JPanel(new FlowLayout());
      resultPanel
          .add(new JLabel(Resources.get("map.advancedregionshapecellrenderer.prefs.result")));

      cShowPanel = new ColorShowPanel();

      Dimension dim = new Dimension(mPanel.getPreferredSize());
      dim.width += infoPanel.getPreferredSize().width / 2;
      dim.height = 30;
      cShowPanel.setPreferredSize(dim);
      cShowPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
      resultPanel.add(cShowPanel);

      con.fill = GridBagConstraints.HORIZONTAL;
      con.gridwidth = 2;
      con.gridy++;
      con.weightx = 0.5;
      mainPanel.add(resultPanel, con);

      con.fill = GridBagConstraints.NONE;
      con.anchor = GridBagConstraints.CENTER;
      con.gridx = 0;
      con.gridy = 0;
      con.gridwidth = 1;
      con.gridheight = 5;
      con.insets = new Insets(1, 1, 1, 10);
      con.weightx = 0;
      mainPanel.add(createMenu(), con);

      con.gridy = 5;
      con.gridheight = 3;
      con.fill = GridBagConstraints.BOTH;
      mainPanel.add(createList(), con);

      setLayout(new BorderLayout());
      this.add(mainPanel, BorderLayout.CENTER);

      // this.add(left, BorderLayout.WEST);
    }

    protected Component createList() {
      model = new DefaultListModel();
      reprocessSets();
      list = new JList(model);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setSelectedValue(currentSelection, true);
      list.addListSelectionListener(this);

      return new JScrollPane(list);
    }

    protected void reprocessSets() {
      Object old = null;

      if (list != null) {
        old = list.getSelectedValue();
      }

      reprocessSets(old);
    }

    /**
     * Add all known sets to list model.
     * 
     * @param oldSet
     */
    protected void reprocessSets(Object oldSet) {
      List<String> sets = new LinkedList<String>();
      StringTokenizer s =
          new StringTokenizer(
              settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, ""), ",");

      while (s.hasMoreTokens()) {
        sets.add(s.nextToken());
      }

      model.clear();

      Iterator<String> it = sets.iterator();

      while (it.hasNext()) {
        model.addElement(it.next());
      }

      if ((oldSet != null) && model.contains(oldSet)) {
        list.setSelectedValue(oldSet, true);
      } else {
        log.debug("old set unknown");
      }
    }

    protected Component createMenu() {
      addSet = new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.add"));
      addSet.addActionListener(this);
      removeSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.remove"));
      removeSet.addActionListener(this);
      renameSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.rename"));
      renameSet.addActionListener(this);
      importSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.import"));
      importSet.addActionListener(this);
      exportSet =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.export"));
      exportSet.addActionListener(this);
      setTooltip =
          new JButton(Resources.get("map.advancedregionshapecellrenderer.prefs.menu.tooltip"));
      setTooltip.addActionListener(this);
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

      for (int i = 0; i < 4; i++) {
        con.gridx = (i % 2) * 2;
        con.gridy = (i / 2);
        panel.add(new JLabel(Resources.get("map.advancedregionshapecellrenderer.prefs.texts."
            + String.valueOf(i))), con);
      }

      con.gridx = 1;
      con.gridy = 0;
      minText = new JTextField(prefSet.minDef, 10);
      panel.add(minText, con);

      curText = new JTextField(prefSet.curDef, 10);
      con.gridx = 3;
      panel.add(curText, con);
      maxText = new JTextField(prefSet.maxDef, 10);
      con.gridx = 1;
      con.gridy = 1;
      panel.add(maxText, con);
      uText = new JTextField(prefSet.unknownString, 10);
      con.gridx = 3;
      panel.add(uText, con);

      return panel;
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      apply(currentSelection);

      loadGeomColors();
      loadSet(currentSelection);
      reprocessMenu();
    }

    private void apply(String currentSelection) {
      prefSet.setMinDef(minText.getText());
      prefSet.setMaxDef(maxText.getText());
      prefSet.setCurDef(curText.getText());
      prefSet.setUnknown(uText.getText());
      // currentSettt.setMapping(myMapping);
      // currentSettt.setTable(myTable);
      prefSet.save(settings);
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
      return Resources.get("map.advancedregionshapecellrenderer.prefs.title");
    }

    /**
     * DOCUMENT-ME
     */
    public void dataChanged() {
      cShowPanel.repaint();
      mPanel.repaint();
      cPanel.repaint();
    }

    /**
     * DOCUMENT-ME
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
        if (infoDialog == null) {
          Window w = SwingUtilities.getWindowAncestor(this);
          if (SwingUtilities.getWindowAncestor(this) instanceof JDialog) {
            infoDialog =
                new ToolTipReplacersInfo((JDialog) w, Resources
                    .get("map.mapperpreferences.tooltipdialog.tooltipinfo.title"));
          } else {
            infoDialog =
                new ToolTipReplacersInfo(Resources
                    .get("map.mapperpreferences.tooltipdialog.tooltipinfo.title"));
          }
        }

        if (!infoDialog.isVisible()) {
          infoDialog.showDialog();
        }

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
      prefSet.save(settings);
      reprocessSets(newSet.name);
    }

    protected boolean checkName(String name) {
      StringTokenizer s =
          new StringTokenizer(
              settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, ""), ",");

      while (s.hasMoreTokens()) {
        if (name.equals(s.nextToken()))
          return false;
      }

      return true;
    }

    protected void removeSet(String removedSet) {
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS);
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES);
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM);
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM);
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT);
      settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER + removedSet
          + PropertiesHelper.ADVANCEDSHAPERENDERER_UNKNOWN);

      String set = settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS);

      if (set != null) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer s = new StringTokenizer(set, ",");

        while (s.hasMoreTokens()) {
          String token = s.nextToken();

          if (!removedSet.equals(token)) {
            if (buf.length() > 0) {
              buf.append(',');
            }

            buf.append(token);
          }
        }

        if (buf.length() > 0) {
          settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, buf.toString());
        } else {
          settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS);
        }
      }

      StringTokenizer s =
          new StringTokenizer(
              settings.getProperty(PropertiesHelper.ADVANCEDSHAPERENDERER_SETS, ""), ",");

      if (s.hasMoreTokens()) {
        currentSelection = s.nextToken();
        prefSet = new ARRSet(settings, currentSelection);
      } else {
        settings.remove(PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT_SET);
        // TODO !!!
        // loadDefaultSet();
      }

      updateTables();
      list.setSelectedIndex(-1);
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
          // oldSet.save(settings);
          // reprocessSets(newName);
          // currentSelection = newName;
          // prefSet = oldSet;
          // prefSet.setName(newName);
          // prefSet.save(settings);
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
        ARRSet newSet = new ARRSet(prop, prop.getProperty("Set.Name"));
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
          prefSet.save(prop);
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
      if (mapper != null) {
        List<String> l = mapper.getAllTooltipDefinitions();
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
                  .get("map.advancedregionshapecellrenderer.prefs.menu.tooltip.input"));

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
     * DOCUMENT-ME
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
      if (list.getSelectedValue() != null) {
        if (list.getSelectedValue() != currentSelection) {
          apply(currentSelection);
          setSet((String) list.getSelectedValue(), false);
        }
      }
    }

    private void setSet(String newSet) {
      setSet(newSet, true);
    }

    private void setSet(String newSet, boolean setList) {
      currentSelection = newSet;
      prefSet = new ARRSet(settings, currentSelection);
      updateTables();
      updateTooltipLabel();
      if (setList) {
        list.setSelectedValue(currentSelection, true);
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
       * DOCUMENT-ME
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
       * DOCUMENT-ME
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
        myShow.setPreferredSize(new Dimension(200, 20));
        mapPainter = new ColorPainter();
        mapPainter.setPreferredSize(new Dimension(200, 30));
        mapPainter.addMouseListener(this);
        mapPainter.addMouseMotionListener(this);

        JPanel help = new JPanel(new BorderLayout());
        help.add(myShow, BorderLayout.CENTER);
        help.add(mapPainter, BorderLayout.SOUTH);
        help.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        help.setMinimumSize(new Dimension(250, 50));
        help.setPreferredSize(new Dimension(250, 50));
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
        addItem.addActionListener(this);
        removeItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup.remove"));
        removeItem.addActionListener(this);
        modifyItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup.modify"));
        modifyItem.addActionListener(this);
      }

      protected void fillPopup(int type) {
        popup.removeAll();

        if (type == -1) {
          popup.add(addItem);
        } else {
          popup.add(modifyItem);

          if ((type != 0) && (type != (value.size() - 1))) {
            popup.add(removeItem);
          }
        }
      }

      /**
       * DOCUMENT-ME
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
          for (int i = 0; i < value.size(); i++) {
            if (newValue == (value.get(i)).floatValue())
              return false;
          }
        }

        value.set(index, new Float(newValue));
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

            value.set(moveIndex, new Float(newHoriz));
            setText(moveIndex);
            save();
            dataChanged();
          }
        }
      }

      /**
       * DOCUMENT-ME
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

        searchMoving(mouseEvent);
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
          moveIndex = -1;
          mapPainter.setCursor(gripCursor);
        }
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
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
            value.set(lastMoveIndex, new Float(fl));
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
          value.add(index, new Float(((float) x) / ((float) width)));
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
       * DOCUMENT-ME
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
       * DOCUMENT-ME
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
          java.text.NumberFormat number = null;

          try {
            number =
                java.text.NumberFormat.getInstance(magellan.library.utils.Locales.getGUILocale());
          } catch (IllegalStateException ise) {
            number = java.text.NumberFormat.getInstance();
          }

          number.setMaximumFractionDigits(1);
          number.setMinimumFractionDigits(0);

          int x[] = new int[5];
          int y[] = new int[5];
          poly = new Polygon(x, y, 5);
          numbers = new String[11];

          for (int i = 0; i < numbers.length; i++) {
            numbers[i] = number.format(((double) i) / ((double) 10));
          }
        }

        /**
         * DOCUMENT-ME
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
            y = Math.max(0, size.height - fm.getAscent() - 1);

            for (int i = 0; i < value.size(); i++) {
              int x = (int) (size.width * value.get(i).floatValue());
              paintIcon(g, x, y, mapping.get(i));
            }
          }
        }

        protected void paintIcon(Graphics g, int x, int h, Color col) {
          g.setColor(col);
          poly.xpoints[0] = x;
          poly.ypoints[0] = 2;

          poly.xpoints[4] = x + 4;
          poly.ypoints[4] = h / 3;

          poly.xpoints[3] = x + 4;
          poly.ypoints[3] = h - 1;

          poly.xpoints[2] = x - 4;
          poly.ypoints[2] = h - 1;

          poly.xpoints[1] = x - 4;
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
        setPreferredSize(new Dimension(150, 100));
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
        addItem.addActionListener(this);
        removeItem =
            new JMenuItem(Resources.get("map.advancedregionshapecellrenderer.prefs.popup2.remove"));
        removeItem.addActionListener(this);
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
       * DOCUMENT-ME
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
                mapping.set(moveIndex, new Float(newVert));
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

                value.set(moveIndex, new Float(newHoriz));
              }

              if (newVert != oldY) {
                mapping.set(moveIndex, new Float(newVert));
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

        value.add(index, new Float(((float) x) / ((float) width)));
        mapping.add(index, new Float(((float) (mapPainter.getHeight() - y))
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
       * DOCUMENT-ME
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
       * DOCUMENT-ME
       */
      public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        if (moveIndex != -1) {
          moved(mouseEvent);
          moveIndex = -1;
          mapPainter.setCursor(gripCursor);
        }
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
       */
      public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
      }

      /**
       * DOCUMENT-ME
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
       * DOCUMENT-ME
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
       * DOCUMENT-ME
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
         * DOCUMENT-ME
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
}
