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

package magellan.client.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import magellan.client.MagellanContext;
import magellan.client.utils.Colors;
import magellan.library.utils.JVMUtilities;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Umlaut;

/**
 * TreeCellRenderer with stylesets.
 * 
 * @author Sebastian
 * @version 1.0
 */
public class CellRenderer implements TreeCellRenderer {
  // private static final Logger log = Logger.getInstance(CellRenderer.class);

  public static final String SKILL_CHANGE_STYLE_PREFIX = "Talent";

  /** Names (for preferences etc.) of styles */
  public static final String STYLE_NAMES[] = new String[] { "SIMPLE", "MAIN", "ADDITIONAL",
      "Talent>.Talent", "Talent>.Talent1", "Talent>.Talent2", "Talent>.Talent3", "Talent<.Talent",
      "Talent<.Talent-1", "Talent<.Talent-2", "Talent<.Talent3", "Talent?", "DEFAULT" };

  /** SIMPLE STYLE */
  public static final int SIMPLE_STYLE = GraphicsElement.SIMPLE;
  /** MAIN PART STYLE */
  public static final int MAIN_STYLE = GraphicsElement.MAIN;
  /** ADDITIONAL PART */
  public static final int ADDITIONAL_STYLE = GraphicsElement.ADDITIONAL;
  /** SKILL INCREASED TYPE */
  public static final int TALENT_INC_STYLE = 3;
  /** SKILL DECREASE TYPE */
  public static final int TALENT_DEC_STYLE = 7;
  /** SKILL UNKNOWN */
  public static final int TALENT_UNKNOWN_STYLE = 11;
  /** DEFAULT TYPE */
  public static final int DEFAULT_STYLE = 12;

  private DefaultTreeCellRenderer defaultRenderer = null;
  private Border focusedBorder = null;
  private Border selectedBorder = null;
  private Border plainBorder = null;
  private Border emptyBorder = null;
  private Icon missingIcon = null;
  private JLabel label = null;
  private JLabel iconLabels[] = new JLabel[10];
  private static Map<Object, Icon> mapIcons = new HashMap<Object, Icon>();
  private static Map<Font, Font> boldFonts = new HashMap<Font, Font>();
  private static Map<String, GraphicsStyleset> stylesets = null;

  /** DOCUMENT-ME */
  public static Map<String, Color> colorMap;

  /** DOCUMENT-ME */
  private static boolean showTooltips;
  private boolean initialized = false;
  private CellObject cellObj = null;
  private CellObject2 cellObj2 = null;

  // default stylesets for GraphicsElement types + fallback styleset(last in array)
  protected static GraphicsStyleset typeSets[] = null;

  // stores joined data that comes out of getStyleset
  private GraphicsStyleset styleset = new GraphicsStyleset("swap");
  private static Properties settings;

  /** DOCUMENT-ME */
  public static int emphasizeStyleChange = 0;

  /** DOCUMENT-ME */
  public static Color emphasizeColor = null;

  private MagellanContext context;

  private JPanel component;

  protected static class CRPanel extends JPanel {
    /**
     * Overrides JComponent.getToolTipText to return the tooltip of the underlying label or null, if
     * no label found.
     */
    @Override
    public String getToolTipText(MouseEvent e) {
      if (e != null) {
        // reprocess layout to have the sizes that were displayed
        doLayout();

        Point p = e.getPoint();
        Rectangle rect = new Rectangle();

        for (int i = 0; i < getComponentCount(); i++) {
          Component c = getComponent(i);
          rect = c.getBounds(rect);

          if (rect.contains(p.x, p.y)) {
            if (c instanceof JComponent)
              return ((JComponent) c).getToolTipText();

            return null;
          }
        }
      }

      return null;
    }
  }

  /**
   * Creates new CellRenderer
   */
  public CellRenderer(MagellanContext context) {
    CellRenderer.settings = context.getProperties();
    this.context = context;
    component = new CRPanel();

    if (!initialized) {
      emptyBorder = new EmptyBorder(0, 0, 0, 1);

      loadTypesets(); // creates the array
      CellRenderer.loadStylesets(); // create custom stylesets

      loadAdditionalValueProperties(); // loads type set "ADDITIONAL"
      CellRenderer.loadEmphasizeData();
      initialized = true;
    }

    applyUIDefaults(); // loads type set "DEFAULT"

    // initialize icon labels array
    for (int i = 0; i < iconLabels.length; i++) {
      iconLabels[i] = new JLabel();
      iconLabels[i].setOpaque(false);
      iconLabels[i].setIconTextGap(1);
      iconLabels[i].setBorder(emptyBorder);
    }

    // load missing icon
    // pavkovic 2003.09.11: only initialize once
    if (missingIcon == null) {
      missingIcon = context.getImageFactory().loadImageIcon("missing");
    }

    javax.swing.UIManager.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        component.removeAll();
        applyUIDefaults();
      }
    });
  }

  protected void loadTypesets() {
    CellRenderer.typeSets = new GraphicsStyleset[DEFAULT_STYLE + 1];

    // load the stylesets
    CellRenderer.loadStyleset(STYLE_NAMES[SIMPLE_STYLE]);
    CellRenderer.loadStyleset(STYLE_NAMES[MAIN_STYLE]);
    CellRenderer.loadStyleset(STYLE_NAMES[ADDITIONAL_STYLE]);
    CellRenderer.loadStyleset(STYLE_NAMES[DEFAULT_STYLE]);

    // load predefined "custom" sets
    CellRenderer.loadStyleset(STYLE_NAMES[TALENT_INC_STYLE]);
    for (int i = 1; i <= 3; ++i) {
      CellRenderer.loadStyleset(STYLE_NAMES[TALENT_INC_STYLE] + i);
    }
    CellRenderer.loadStyleset(STYLE_NAMES[TALENT_DEC_STYLE]);
    for (int i = -1; i >= -3; --i) {
      CellRenderer.loadStyleset(STYLE_NAMES[TALENT_DEC_STYLE] + i);
    }

    CellRenderer.loadStyleset(STYLE_NAMES[TALENT_UNKNOWN_STYLE]);

    CellRenderer.typeSets[SIMPLE_STYLE] = CellRenderer.stylesets.get(STYLE_NAMES[SIMPLE_STYLE]);
    CellRenderer.typeSets[MAIN_STYLE] = CellRenderer.stylesets.get(STYLE_NAMES[MAIN_STYLE]);
    CellRenderer.typeSets[ADDITIONAL_STYLE] =
        CellRenderer.stylesets.get(STYLE_NAMES[ADDITIONAL_STYLE]);
    CellRenderer.typeSets[TALENT_INC_STYLE] =
        CellRenderer.stylesets.get(STYLE_NAMES[TALENT_INC_STYLE]);
    for (int i = 1; i <= 3; ++i) {
      CellRenderer.typeSets[3 + i] = CellRenderer.stylesets.get(STYLE_NAMES[TALENT_INC_STYLE] + i);
    }
    CellRenderer.typeSets[TALENT_DEC_STYLE] =
        CellRenderer.stylesets.get(STYLE_NAMES[TALENT_DEC_STYLE]);
    for (int i = -1; i >= -3; --i) {
      CellRenderer.typeSets[TALENT_DEC_STYLE - i] =
          CellRenderer.stylesets.get(STYLE_NAMES[TALENT_INC_STYLE] + i);
    }
    CellRenderer.typeSets[TALENT_UNKNOWN_STYLE] =
        CellRenderer.stylesets.get(STYLE_NAMES[TALENT_UNKNOWN_STYLE]);

    CellRenderer.typeSets[DEFAULT_STYLE] = CellRenderer.stylesets.get(STYLE_NAMES[DEFAULT_STYLE]);

    CellRenderer.typeSets[SIMPLE_STYLE].setParent(STYLE_NAMES[DEFAULT_STYLE]);
    CellRenderer.typeSets[MAIN_STYLE].setParent(STYLE_NAMES[DEFAULT_STYLE]);
    CellRenderer.typeSets[ADDITIONAL_STYLE].setParent(STYLE_NAMES[DEFAULT_STYLE]);
  }

  /**
   * Loads the display values out of the settings.
   */
  protected void loadAdditionalValueProperties() {
    Map<String, Color> cMap = null;
    boolean tTip = true;

    // Text -> color mapping
    String cMapS =
        CellRenderer.settings.getProperty(PropertiesHelper.CELLRENDERER_SKILL_ICON_TEXT_COLOR_MAP);

    try {
      StringTokenizer st = new StringTokenizer(cMapS, ";");
      int c = st.countTokens() / 4;
      cMap = new HashMap<String, Color>();

      for (int i = 0; i < c; i++) {
        String value = st.nextToken();
        String redS = st.nextToken();
        String greenS = st.nextToken();
        String blueS = st.nextToken();

        try {
          cMap.put(value, Colors.decode(redS + "," + greenS + "," + blueS));
        } catch (Exception inner) {
        }
      }
    } catch (Exception exc) {
    }

    // Show Tooltips
    String tTipS = CellRenderer.settings.getProperty(PropertiesHelper.CELLRENDERER_SHOW_TOOLTIPS);
    tTip = ((tTipS != null) && tTipS.equals("true"));

    // now give the renderer our values
    CellRenderer.colorMap = cMap;
    CellRenderer.setShowTooltips(tTip);
  }

  protected static void loadEmphasizeData() {
    CellRenderer.emphasizeStyleChange = 0;

    try {
      CellRenderer.emphasizeStyleChange =
          Integer.parseInt(CellRenderer.settings
              .getProperty(PropertiesHelper.CELLRENDERER_EMPHASIZE_STYLE));
    } catch (Exception exc) {
      CellRenderer.emphasizeStyleChange = Font.BOLD;
    }

    CellRenderer.emphasizeColor = null;

    try {
      CellRenderer.emphasizeColor =
          Color.decode(CellRenderer.settings.getProperty("CellRenderer.Emphasize.Color"));
    } catch (Exception exc) {
    }
  }

  /**
   * DOCUMENT-ME
   */
  public static void setEmphasizeData(int sChange, Color sColor) {
    if ((CellRenderer.emphasizeStyleChange != sChange) && (CellRenderer.boldFonts != null)) {
      CellRenderer.boldFonts.clear();
    }

    CellRenderer.emphasizeStyleChange = sChange;

    if (sChange == 0) {
      CellRenderer.settings.remove(PropertiesHelper.CELLRENDERER_EMPHASIZE_STYLE);
    } else {
      CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_EMPHASIZE_STYLE, String
          .valueOf(sChange));
    }

    CellRenderer.emphasizeColor = sColor;

    if (sColor == null) {
      CellRenderer.settings.remove("CellRenderer.Emphasize.Color");
    } else {
      CellRenderer.settings.setProperty("CellRenderer.Emphasize.Color", CellRenderer
          .encodeColor(sColor));
    }
  }

  /**
   * Sets the display values and saves them in the settings
   */
  public static void setAdditionalValueProperties(Map<String, Color> colorM, boolean sTip) {
    CellRenderer.setShowTooltips(sTip);
    CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_SHOW_TOOLTIPS, sTip ? "true"
        : "false");

    CellRenderer.setColorMap(colorM);
  }

  /**
   * Sets the value of showTooltips.
   * 
   * @param showTooltips The value for showTooltips.
   */
  public static void setShowTooltips(boolean showTooltips) {
    CellRenderer.showTooltips = showTooltips;
  }

  /**
   * Returns the value of showTooltips.
   * 
   * @return Returns showTooltips.
   */
  public static boolean isShowTooltips() {
    return CellRenderer.showTooltips;
  }

  /**
   * Sets the current color mapping and stores it in the settings.
   */
  public static void setColorMap(Map<String, Color> colorM) {
    if (((CellRenderer.colorMap != null) && (colorM == null))
        || ((CellRenderer.colorMap != null) && !CellRenderer.colorMap.equals(colorM))
        || ((CellRenderer.colorMap == null) && (colorM != null))) {
      CellRenderer.colorMap = colorM;

      if (CellRenderer.colorMap == null) {
        CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_SKILL_ICON_TEXT_COLOR_MAP,
            "none");
      } else {
        StringBuffer str = new StringBuffer();
        for (String value : CellRenderer.colorMap.keySet()) {
          if (str.length() > 0) {
            str.append(';');
          }

          str.append(value);
          str.append(';');

          Color col = CellRenderer.colorMap.get(value);
          str.append(Colors.encode(col).replace(',', ';'));
        }

        if (str.length() > 0) {
          CellRenderer.settings.setProperty(
              PropertiesHelper.CELLRENDERER_SKILL_ICON_TEXT_COLOR_MAP, str.toString());
        } else {
          CellRenderer.settings.setProperty(
              PropertiesHelper.CELLRENDERER_SKILL_ICON_TEXT_COLOR_MAP, "none");
        }
      }
    }
  }

  /**
   * Applies the default values to type set 4 (DEFAULT).
   */
  protected void applyUIDefaults() {
    defaultRenderer = new DefaultTreeCellRenderer();

    CellRenderer.typeSets[DEFAULT_STYLE].setForeground((Color) UIManager.getDefaults().get(
        "Tree.textForeground"));
    CellRenderer.typeSets[DEFAULT_STYLE].setBackground((Color) UIManager.getDefaults().get(
        "Tree.textBackground"));
    CellRenderer.typeSets[DEFAULT_STYLE].setSelectedForeground((Color) UIManager.getDefaults().get(
        "Tree.selectionForeground"));
    CellRenderer.typeSets[DEFAULT_STYLE].setSelectedBackground((Color) UIManager.getDefaults().get(
        "Tree.selectionBackground"));

    // pavkovic 2003.10.17: prevent jvm 1.4.2_01 bug
    focusedBorder = new MatteBorder(1, 1, 1, 1, JVMUtilities.getTreeSelectionBorderColor());
    selectedBorder =
        new MatteBorder(1, 1, 1, 1, CellRenderer.typeSets[TALENT_INC_STYLE].getSelectedBackground());
    plainBorder = new EmptyBorder(1, 1, 1, 1);

    component.setOpaque(false);
    component.setLayout(new SameHeightBoxLayout());
    component.setBackground(CellRenderer.typeSets[DEFAULT_STYLE].getBackground());
    component.setForeground(CellRenderer.typeSets[DEFAULT_STYLE].getBackground());

    label = new JLabel();
    label.setOpaque(false);

    component.removeAll();
    component.add(label);

    Font plainFont = label.getFont().deriveFont(Font.PLAIN);
    CellRenderer.typeSets[DEFAULT_STYLE].setFont(plainFont);

    GraphicsStyleset set = getStyleset(MAIN_STYLE);
    defaultRenderer.setFont(set.getFont());
  }

  /**
   * DOCUMENT-ME
   */
  public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    cellObj = null;
    cellObj2 = null;

    if (value instanceof DefaultMutableTreeNode) {
      Object object = ((DefaultMutableTreeNode) value).getUserObject();

      if (object instanceof CellObject2) {
        cellObj2 = (CellObject2) object;
      } else if (object instanceof CellObject) {
        cellObj = (CellObject) object;
      }
    }

    if (cellObj2 != null) {
      layoutComponent2(selected, hasFocus);

      return component;
    }

    if (cellObj != null) {
      layoutComponent(selected, hasFocus);

      return component;
    } else
      return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf,
          row, hasFocus);
  }

  protected void layoutComponent2(boolean isSelected, boolean hasFocus) {
    List<GraphicsElement> iconNames = cellObj2.getGraphicsElements();

    // if necessary, increase the size of the iconLabels array
    resizeLabels(iconNames.size());
    label = iconLabels[cellObj2.getLabelPosition()];
    {
      int i = 0, j = 0;
      for (GraphicsElement ge : iconNames) {
        GraphicsStyleset set = getStyleset(ge);
        if (i++ == cellObj2.getLabelPosition()) {
          formatLabel(iconLabels[j], set, isSelected, hasFocus);
          fillLabel(iconLabels[j++], ge);
        } else {
          formatLabel(iconLabels[j], set, false, false);
          fillLabel(iconLabels[j++], ge);
        }
      }

    }
  }

  private void layoutComponent(boolean isSelected, boolean hasFocus) {
    Collection<String> iconNames = cellObj.getIconNames();

    // we have to use a "full" styleset
    GraphicsStyleset set = getStyleset(MAIN_STYLE);

    resizeLabels(iconNames == null ? 1 : iconNames.size() + 1);
    label = iconLabels[iconLabels.length - 1];

    formatLabel(label, set, isSelected, hasFocus);

    if (cellObj.emphasized()) {
      label.setFont(getBoldFont(set.getFont()));

      if (CellRenderer.emphasizeColor != null) {
        label.setForeground(CellRenderer.emphasizeColor);
      }
    }

    label.setText(cellObj.toString());

    // if necessary, increase the size of the iconLabels array

    // load icons and put them into the icon labels
    if (iconNames != null) {
      int i = 0;

      for (String iconName : iconNames) {
        Icon icon = getIcon(iconName);

        // typeSets[3] is always full, so we don't need to
        // call getStyleset()
        formatLabel(iconLabels[i], set, false, false);
        iconLabels[i].setIcon(icon);

        if (CellRenderer.isShowTooltips()) {
          iconLabels[i].setToolTipText(iconName);
        } else {
          iconLabels[i].setToolTipText(null);
        }
        i++;
      }
    }
  }

  /** if necessary, increase the size of the iconLabels array */
  private void resizeLabels(int newSize) {
    for (int i = 0; i < iconLabels.length; i++) {
      iconLabels[i] = null;
    }

    iconLabels = new JLabel[newSize];

    for (int i = 0; i < iconLabels.length; i++) {
      iconLabels[i] = new JLabel();
      iconLabels[i].setOpaque(false);
      iconLabels[i].setIconTextGap(1);
      iconLabels[i].setBorder(emptyBorder);
    }

    component.removeAll();
    for (JLabel iconLabel : iconLabels) {
      component.add(iconLabel);
    }

  }

  /**
   * Fills the given label with the information out of the given element.
   */
  protected void fillLabel(JLabel l, GraphicsElement ge) {
    // object
    if (ge.getObject() != null) {
      l.setText(ge.getObject().toString());
    }

    String text = l.getText();

    if ((text != null) && (CellRenderer.colorMap != null)
        && CellRenderer.colorMap.containsKey(text)) {
      l.setForeground(CellRenderer.colorMap.get(text));
    }

    // icon
    if (ge.getIcon() != null) {
      l.setIcon(ge.getIcon());
    } else if (ge.getImage() != null) {
      l.setIcon(getIcon(ge.getImage()));
    } else if (ge.getImageName() != null) {
      l.setIcon(getIcon(ge.getImageName()));
    }

    // tooltip
    if (ge.getTooltip() != null) {
      l.setToolTipText(ge.getTooltip());
    } else {
      l.setToolTipText(null);
    }

    // emphasize
    if (ge.isEmphasized()) {
      l.setFont(getBoldFont(l.getFont()));

      if (CellRenderer.emphasizeColor != null) {
        l.setForeground(CellRenderer.emphasizeColor);
      }
    }
  }

  /**
   * Returns an icon constructed out of the given information.
   * <p>
   * The following parsing is done:
   * </p>
   * <ol>
   * <li>If given object is an icon, return it.</li>
   * <li>If given object is an image, construct an ImageIcon and return.</li>
   * <li>If given object is a String, search an image with that name and construct an ImageIcon.</li>
   * </ol>
   * All icons are cached(except (1)). Non-found images of (3) are replaced with missingIcon. All
   * unparseable objects return missingIcon.
   */
  protected Icon getIcon(Object icon) {
    if (icon instanceof Icon)
      return (Icon) icon;

    if (CellRenderer.mapIcons.containsKey(icon))
      return CellRenderer.mapIcons.get(icon);

    if (icon instanceof Image) {
      ImageIcon ii = new ImageIcon((Image) icon);
      CellRenderer.mapIcons.put(icon, ii);

      return ii;
    }

    if (icon instanceof String) {
      String iconName = (String) icon;
      String normalizedIconName = Umlaut.convertUmlauts(iconName).toLowerCase();

      Icon ic = context.getImageFactory().loadImageIcon(normalizedIconName);

      if (ic == null) {
        ic = missingIcon;
      }

      CellRenderer.mapIcons.put(icon, ic);

      return ic;
    }

    return missingIcon;
  }

  /**
   * Returns the bold font of the given font. These fonts are stored in boldFonts. If the given font
   * is bold it's returned directly. If there's no bold font yet, a new one is created and placed
   * into boldFonts.
   * <p>
   * In this implementation all other style features(only italic yet) are save.
   * </p>
   */
  protected Font getBoldFont(Font f) {
    if ((CellRenderer.emphasizeStyleChange == 0)
        || ((f.getStyle() & CellRenderer.emphasizeStyleChange) != 0))
      return f;

    if (!CellRenderer.boldFonts.containsKey(f)) {
      CellRenderer.boldFonts.put(f, f.deriveFont(f.getStyle() | CellRenderer.emphasizeStyleChange));
    }

    return CellRenderer.boldFonts.get(f);
  }

  /**
   * Formats a label using the given styleset. <b>The styleset must be complete!</b> Try using
   * getStyleset() to assure a complete set.
   * <p>
   * The label will also be resetted meaning tooltip, icon text are cleared.
   * </p>
   */
  protected void formatLabel(JLabel l, GraphicsStyleset set, boolean isSelected, boolean hasFocus) {
    l.setToolTipText(null);
    l.setIcon(null);
    l.setText(null);

    if (isSelected) {
      l.setBackground(set.getSelectedBackground());
      l.setForeground(set.getSelectedForeground());
      l.setOpaque(true);
    } else {
      if (Color.white.equals(set.getBackground()) || (set.getBackground() == null)) {
        l.setOpaque(false);
      } else {
        l.setOpaque(true);
      }

      l.setBackground(set.getBackground());
      l.setForeground(set.getForeground());
    }

    if (hasFocus) {
      l.setBorder(focusedBorder);
    } else {
      if (isSelected) {
        l.setBorder(selectedBorder);
      } else {
        l.setBorder(plainBorder);
      }
    }

    l.setFont(set.getFont());
    l.setHorizontalTextPosition(set.getHorizontalPos());
    l.setVerticalTextPosition(set.getVerticalPos());
  }

  /**
   * Returns a styleset that supplies all variables. This is done with a union of the given styleset
   * of the element (if given) and the two fallbacks(type set and default set).
   */
  protected GraphicsStyleset getStyleset(GraphicsElement ge) {
    GraphicsStyleset fallback = null;

    if (ge.hasStyleset()) {
      if ((CellRenderer.stylesets == null) || !CellRenderer.stylesets.containsKey(ge.getStyleset())) {
        CellRenderer.loadStyleset(ge.getStyleset());
      }

      fallback = CellRenderer.stylesets.get(ge.getStyleset());
    } else {
      fallback = CellRenderer.typeSets[ge.getType()];
    }

    fallbackCode(fallback, ge.getType());

    return styleset;
  }

  /**
   * Returns a styleset that supplies all variables. This is done with a union of the given styleset
   * and the two fallbacks(type set[given via parameter] and default set).
   */
  protected GraphicsStyleset getStyleset(GraphicsStyleset set, int type) {
    fallbackCode(set, type);

    return styleset;
  }

  /**
   * Returns a full styleset created out of the given styleset.
   */
  protected GraphicsStyleset getStyleset(int type) {
    fallbackCode(CellRenderer.typeSets[type], type);

    return styleset;
  }

  private void fallbackCode(GraphicsStyleset fallback, int type) {
    styleset.setBackground(null);
    styleset.setFont(null);
    styleset.setForeground(null);
    styleset.setSelectedBackground(null);
    styleset.setSelectedForeground(null);

    // always use first supplied position
    styleset.setHorizontalPos(fallback.getHorizontalPos());
    styleset.setVerticalPos(fallback.getVerticalPos());

    boolean allFound;

    do {
      allFound = true;

      if (styleset.getBackground() == null) {
        if (fallback.getBackground() == null) {
          allFound = false;
        } else {
          styleset.setBackground(fallback.getBackground());
        }
      }

      if (styleset.getFont() == null) {
        if (fallback.getFont() == null) {
          allFound = false;
        } else {
          styleset.setFont(fallback.getFont());
        }
      }

      if (styleset.getForeground() == null) {
        if (fallback.getForeground() == null) {
          allFound = false;
        } else {
          styleset.setForeground(fallback.getForeground());
        }
      }

      if (styleset.getFont() == null) {
        if (fallback.getFont() == null) {
          allFound = false;
        } else {
          styleset.setFont(fallback.getFont());
        }
      }

      if (styleset.getSelectedBackground() == null) {
        if (fallback.getSelectedBackground() == null) {
          allFound = false;
        } else {
          styleset.setSelectedBackground(fallback.getSelectedBackground());
        }
      }

      if (styleset.getSelectedForeground() == null) {
        if (fallback.getSelectedForeground() == null) {
          allFound = false;
        } else {
          styleset.setSelectedForeground(fallback.getSelectedForeground());
        }
      }

      if (fallback.getParent() != null) {
        if (!CellRenderer.stylesets.containsKey(fallback.getParent())) {
          CellRenderer.loadStyleset(fallback.getParent());
        }

        fallback = CellRenderer.stylesets.get(fallback.getParent());
      } else {
        fallback = CellRenderer.typeSets[type];
      }
    } while (!allFound);
  }

  /**
   * Adds the given styleset to the styleset table. The name of the set is used as a key.
   */
  public static void addStyleset(GraphicsStyleset set) {
    if (CellRenderer.stylesets == null) {
      CellRenderer.stylesets = new HashMap<String, GraphicsStyleset>();
    }

    CellRenderer.stylesets.put(set.getName(), set);
    CellRenderer.saveStyleset(set.getName());
  }

  /**
   * Loads a styleset out of the property PropertiesHelper.CELLRENDERER_STYLESETS+name
   */
  protected static void loadStyleset(String name) {
    if (CellRenderer.stylesets == null) {
      CellRenderer.stylesets = new HashMap<String, GraphicsStyleset>();
    }

    if (!CellRenderer.stylesets.containsKey(name)) {
      GraphicsStyleset set = new GraphicsStyleset(name);
      String propName = PropertiesHelper.CELLRENDERER_STYLESETS + name;

      if (CellRenderer.settings.containsKey(propName)) {
        String def = CellRenderer.settings.getProperty(propName);
        StringTokenizer st = new StringTokenizer(def, ",;");

        while (st.hasMoreTokens()) {
          String defpart = st.nextToken();
          int index = defpart.indexOf('=');

          if (index == -1) {
            index = defpart.indexOf(':');
          }

          if (index == -1) {
            continue;
          }

          String partName = defpart.substring(0, index);
          String partValue = defpart.substring(index + 1);

          if (partName.equalsIgnoreCase("foreground")) {
            try {
              set.setForeground(Color.decode(partValue));
            } catch (NumberFormatException nfe) {
            }
          }

          if (partName.equalsIgnoreCase("background")) {
            try {
              set.setBackground(Color.decode(partValue));
            } catch (NumberFormatException nfe) {
            }
          }

          if (partName.equalsIgnoreCase("selectedforeground")) {
            try {
              set.setSelectedForeground(Color.decode(partValue));
            } catch (NumberFormatException nfe) {
            }
          }

          if (partName.equalsIgnoreCase("selectedbackground")) {
            try {
              set.setSelectedBackground(Color.decode(partValue));
            } catch (NumberFormatException nfe) {
            }
          }

          if (partName.equalsIgnoreCase("font")) {
            set.setFont(Font.decode(partValue));
          }

          if (partName.equalsIgnoreCase("horizontaltextposition")) {
            if (partValue.equalsIgnoreCase("LEFT")) {
              set.setHorizontalPos(SwingConstants.LEFT);
            }

            if (partValue.equalsIgnoreCase("CENTER")) {
              set.setHorizontalPos(SwingConstants.CENTER);
            }

            if (partValue.equalsIgnoreCase("RIGHT")) {
              set.setHorizontalPos(SwingConstants.RIGHT);
            }
          }

          if (partName.equalsIgnoreCase("verticaltextposition")) {
            if (partValue.equalsIgnoreCase("TOP")) {
              set.setVerticalPos(SwingConstants.TOP);
            }

            if (partValue.equalsIgnoreCase("CENTER")) {
              set.setVerticalPos(SwingConstants.CENTER);
            }

            if (partValue.equalsIgnoreCase("BOTTOM")) {
              set.setVerticalPos(SwingConstants.BOTTOM);
            }
          }

          if (partName.equalsIgnoreCase("parent")) {
            set.setParent(partValue);
          }
        }
      } else { // Make a random styleset to attract user interest

        if (!name.equals(STYLE_NAMES[SIMPLE_STYLE]) && !name.equals(STYLE_NAMES[MAIN_STYLE])
            && !name.equals(STYLE_NAMES[ADDITIONAL_STYLE])
            && !name.equals(STYLE_NAMES[DEFAULT_STYLE])) {
          // Note: With extended Stylesets which have parents do not do this.
          if (set.getParent() == null) {
            if (name.equals(STYLE_NAMES[TALENT_UNKNOWN_STYLE])) {
              set.setForeground(Color.BLACK);
              set.setBackground(Color.LIGHT_GRAY);
            } else {
              set.setForeground(Color.BLACK);
              set.setBackground(Color.RED);
            }
          }

          CellRenderer.stylesets.put(set.getName(), set);
          CellRenderer.saveStyleset(set.getName());
        }
      }

      CellRenderer.stylesets.put(name, set);
    }
  }

  /**
   * Returns the styleset map.
   */
  public static Map<String, GraphicsStyleset> getStylesets() {
    return CellRenderer.stylesets;
  }

  /**
   * Returns the styleset of a certain type.
   */
  public static GraphicsStyleset getTypeset(int i) {
    return CellRenderer.typeSets[i];
  }

  /**
   * Loads all custom stylesets. Checks the property PropertiesHelper.CELLRENDERER_CUSTOM_STYLESETS
   * for names of stylesets and searches the given sets.
   */
  public static void loadStylesets() {
    String custom =
        CellRenderer.settings.getProperty(PropertiesHelper.CELLRENDERER_CUSTOM_STYLESETS);

    if (custom != null) {
      StringTokenizer st = new StringTokenizer(custom, ";");

      while (st.hasMoreElements()) {
        CellRenderer.loadStyleset(st.nextToken());
      }
    }
  }

  /**
   * Saves a single styleset.
   */
  protected static void saveStyleset(String name) {
    if ((CellRenderer.stylesets != null) && CellRenderer.stylesets.containsKey(name)) {
      GraphicsStyleset set = CellRenderer.stylesets.get(name);
      String custom =
          CellRenderer.settings.getProperty(PropertiesHelper.CELLRENDERER_CUSTOM_STYLESETS, "");

      if (custom.indexOf(name) == -1) {
        if (custom.length() == 0) {
          custom = name;
        } else {
          custom += (";" + name);
        }

        CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_CUSTOM_STYLESETS, custom);
      }

      String def = CellRenderer.createDefinitionString(set);
      CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_STYLESETS + name, def);
    }
  }

  /**
   * Stores all stylesets(custom and type) into the settings.
   */
  public static void saveStylesets() {
    // custom sets
    if (CellRenderer.stylesets != null) {
      StringBuffer custom = new StringBuffer();
      for (String name : CellRenderer.stylesets.keySet()) {
        GraphicsStyleset set = CellRenderer.stylesets.get(name);
        String def = CellRenderer.createDefinitionString(set);
        CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_STYLESETS + name, def);

        if (custom.length() > 0) {
          custom.append(';');
        }

        custom.append(name);
      }

      CellRenderer.settings.setProperty(PropertiesHelper.CELLRENDERER_CUSTOM_STYLESETS, custom
          .toString());
    }
  }

  /**
   * Deletes a styleset out of the stylesets map.
   */
  public static void removeStyleset(String styleset) {
    if (CellRenderer.stylesets != null) {
      CellRenderer.stylesets.remove(styleset);
      CellRenderer.settings.remove(PropertiesHelper.CELLRENDERER_STYLESETS + styleset);
    }
  }

  protected static String encodeColor(Color c) {
    int i = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();

    return '#' + Integer.toHexString(i);
  }

  protected static String encodeFont(Font f) {
    StringBuffer buf = new StringBuffer();
    buf.append(f.getFamily());
    buf.append('-');

    if (f.isPlain()) {
      buf.append("plain-");
    } else {
      if (f.isBold()) {
        buf.append("bold-");
      }

      if (f.isItalic()) {
        buf.append("italic-");
      }
    }

    buf.append(f.getSize());

    return buf.toString();
  }

  protected static String createDefinitionString(GraphicsStyleset set) {
    StringBuffer buf = new StringBuffer();

    if (set.getForeground() != null) {
      buf.append("foreground=");
      buf.append(CellRenderer.encodeColor(set.getForeground()));
    }

    if (set.getBackground() != null) {
      if (buf.length() > 0) {
        buf.append(';');
      }

      buf.append("background=");
      buf.append(CellRenderer.encodeColor(set.getBackground()));
    }

    if (set.getSelectedForeground() != null) {
      if (buf.length() > 0) {
        buf.append(';');
      }

      buf.append("selectedforeground=");
      buf.append(CellRenderer.encodeColor(set.getSelectedForeground()));
    }

    if (set.getSelectedBackground() != null) {
      if (buf.length() > 0) {
        buf.append(';');
      }

      buf.append("selectedbackground=");
      buf.append(CellRenderer.encodeColor(set.getSelectedBackground()));
    }

    if (set.getFont() != null) {
      if (buf.length() > 0) {
        buf.append(';');
      }

      buf.append("font=");
      buf.append(CellRenderer.encodeFont(set.getFont()));
    }

    if (buf.length() > 0) {
      buf.append(';');
    }

    buf.append("horizontaltextposition=");

    int i = set.getHorizontalPos();

    if (i == SwingConstants.LEFT) {
      buf.append("LEFT");
    } else if (i == SwingConstants.CENTER) {
      buf.append("CENTER");
    } else if (i == SwingConstants.RIGHT) {
      buf.append("RIGHT");
    } else {
      buf.append("unknown");
    }

    buf.append(';');

    buf.append("verticaltextposition=");
    i = set.getVerticalPos();

    if (i == SwingConstants.TOP) {
      buf.append("TOP");
    } else if (i == SwingConstants.CENTER) {
      buf.append("CENTER");
    } else if (i == SwingConstants.BOTTOM) {
      buf.append("BOTTOM");
    } else {
      buf.append("unknown");
    }

    if (!set.isExtractedParent() && (set.getParent() != null)) {
      buf.append(";parent=");
      buf.append(set.getParent());
    }

    return buf.toString();
  }

  /**
   * A box layout assuring that all components have the same height.
   */
  protected static class SameHeightBoxLayout implements LayoutManager {
    /**
     * DOCUMENT-ME
     */
    public void addLayoutComponent(String s, Component c) {
    }

    /**
     * DOCUMENT-ME
     */
    public void removeLayoutComponent(Component c) {
    }

    /**
     * DOCUMENT-ME
     */
    public Dimension minimumLayoutSize(Container target) {
      return preferredLayoutSize(target);
    }

    /**
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container target) {
      Dimension dim = new Dimension();

      if (target.getComponentCount() > 0) {
        for (int i = 0; i < target.getComponentCount(); i++) {
          Dimension dim2 = target.getComponent(i).getPreferredSize();
          dim.width += dim2.width;

          if (dim2.height > dim.height) {
            dim.height = dim2.height;
          }
        }

        dim.height++;
      }

      return dim;
    }

    /**
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container target) {
      if (target.getComponentCount() > 0) {
        int x = 0;
        int height = target.getHeight();

        if (height <= 0) {
          height = preferredLayoutSize(target).height;
        }

        for (int i = 0; i < target.getComponentCount(); i++) {
          Component c = target.getComponent(i);
          Dimension dim = c.getPreferredSize();
          c.setBounds(x, 0, dim.width, height);
          x += dim.width;
        }
      }
    }
  }
}
