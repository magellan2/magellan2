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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.GameData;
import magellan.library.utils.Resources;

/**
 * A class providing implementations of several methods required by a renderer that renders
 * hexagonal cells.
 */
public abstract class HexCellRenderer implements MapCellRenderer {
  /** The cell geometry used for rendering. */
  protected CellGeometry cellGeo = null;

  /** A Properties object used to retrieve and store preferences of this renderer. */
  protected Properties settings = null;

  /** The game data this renderer may use for additional information on what and how to render. */
  protected GameData data = null;

  /** The graphics object set by init() to draw on in the render() method. */
  protected Graphics2D graphics = null;

  /**
   * The pixel offset used to compensate the difference of axis origins between the graphics object
   * and the map.
   */
  protected Rectangle offset = null;

  protected MagellanContext context;

  /**
   * A constructor assigning a cell geometry and settings.
   */
  public HexCellRenderer(CellGeometry geo, MagellanContext context) {
    cellGeo = geo;
    this.context = context;
    settings = context.getProperties();
  }

  /**
   * Initializes a rendering pass with the specified graphics object g used to paint on in
   * subsequent calls to render() and the offset where to draw region 0, 0 on the graphics object.
   */
  /*
   * public void init(GameData data, Graphics g, Point offset) { this.data = data; graphics = g;
   * this.offset = new Rectangle(offset); }
   */
  public void init(GameData data, Graphics g, Rectangle offset) {
    this.data = data;
    graphics = (Graphics2D) g;
    this.offset = offset;
  }

  /**
   * @see magellan.client.swing.map.MapCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  public abstract void render(Object obj, boolean active, boolean selected);

  /**
   * Returns a name for this renderer. By default, the key "name" is looked up in the component
   * dictionary and returned.
   */
  public abstract String getName();/*
                                    * { return getString("name"); }
                                    */

  /**
   * Returns the default rendering plane of the renderer. See the constants specified in
   * com.eressea.swing.map.Mapper for possible values.
   */
  public abstract int getPlaneIndex();

  /**
   * Returns a default preferences adapter telling the user that this renderer does not have
   * modifiable preferences.
   */
  public PreferencesAdapter getPreferencesAdapter() {
    return new DefaultRendererPreferencesAdapter(this);
  }

  /**
   * Scales the used cell geometry object.
   * 
   * @param scaleFactor The new factor. Must be &gt; 0.
   * @throws IllegalArgumentException if scaleFactor &le; 0.
   */
  public void scale(float scaleFactor) {
    if (scaleFactor <= 0)
      throw new IllegalArgumentException("factor < 0: " + scaleFactor);
    cellGeo.setScaleFactor(scaleFactor);
  }

  /**
   * Returns the used cell geometry object.
   */
  public CellGeometry getCellGeometry() {
    return cellGeo;
  }

  /**
   * Sets the used cell geometry object.
   */
  public void setCellGeometry(CellGeometry geo) {
    cellGeo = geo;
  }

  /**
   * Returns a String representation of the renderer.
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Returns a translation from the translation table for the specified key. protected abstract
   * String getString(String key);/* { return Resources.get("map.hexcellrenderer.", key); }
   */

  /**
   * The default preferences adapter telling the user that there are no modifiable preferences.
   */
  protected class DefaultRendererPreferencesAdapter implements PreferencesAdapter {
    protected MapCellRenderer source = null;

    /**
     * Creates a new DefaultRendererPreferencesAdapter object.
     */
    public DefaultRendererPreferencesAdapter(MapCellRenderer source) {
      this.source = source;
    }

    public void initPreferences() {
      // nothing to do
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
    }

    /**
     * Creates a default component with no configurable options.
     * 
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public Component getComponent() {
      return new JLabel(Resources.get("map.hexcellrenderer.lbl.nooptions.caption"));
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
     */
    public String getTitle() {
      return getName();
    }
  }

  protected static JTextArea createDescriptionPanel(String text, Component parent) {
    JTextArea description = new JTextArea(text);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    description.setEditable(false);
    description.setRequestFocusEnabled(false);
    if (parent != null) {
      description.setBackground(parent.getBackground());
      description.setSelectionColor(parent.getBackground());
      description.setSelectedTextColor(parent.getForeground());
      description.setBackground(parent.getBackground());
      description.setSelectionColor(parent.getBackground());
      description.setSelectedTextColor(parent.getForeground());
      description.setFont(new JLabel().getFont());
    }
    return description;
  }
}
