/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing.map;

import java.awt.Graphics;
import java.awt.Rectangle;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.GameData;

/**
 * The interface required of a class used as a map renderer.
 */
public interface MapCellRenderer {

  /**
   * Renders the supplied object. The <code>active</code> and <code>selected</code> parameters
   * influence how the object is drawn.
   * 
   * @param obj This object is going to be displayed (typically a region).
   * @param active Indicates if the object is the currently active object.
   * @param selected Indicates if the object is the currently selected.
   */
  public void render(Object obj, boolean active, boolean selected);

  /**
   * Returns a name for this renderer.
   */
  public String getName();

  /**
   * Returns the index of the default rendering plane of this renderer. See the constants specified
   * in com.eressea.swing.map.Mapper for possible values.
   */
  public int getPlaneIndex();

  /**
   * Returns the CellGeometry object this object uses for rendering.
   */
  public CellGeometry getCellGeometry();

  /**
   * Sets the CellGeometry object this object uses for rendering.
   */
  public void setCellGeometry(CellGeometry geo);

  /**
   * Initializes the renderer for one rendering pass. All supplied informations stays constant
   * during a pass and is therefore not transferred with each render() call.
   */
  public void init(GameData data, Graphics g, Rectangle offset);

  /**
   * Tells the renderer that it should re-adjust the scale factor it uses for rendering.
   * 
   * @param scaleFactor The new factor. Must be &gt; 0.
   * @throws IllegalArgumentException if scaleFactor &le; 0.
   */
  public void scale(float scaleFactor);

  /**
   * Returns a component that allows to modify the preferences available for this renderer.
   */
  public PreferencesAdapter getPreferencesAdapter();
}
