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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Resources;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class HighlightShapeCellRenderer extends HexCellRenderer {
  private static final int ALPHALEVEL = 100;
  private Color selectedColor = Color.white;
  private Color activeColor = Color.red;
  private boolean drawFilled = true;

  /**
   * Creates a new HighlightShapeCellRenderer object.
   */
  public HighlightShapeCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);

    if (settings != null) {
      try {
        selectedColor =
            Colors.decode(settings.getProperty("HighlightShapeCellRenderer.selectedColor", Colors
                .encode(selectedColor)));
      } catch (NumberFormatException e) {
      }

      try {
        activeColor =
            Colors.decode(settings.getProperty("HighlightShapeCellRenderer.activeColor", Colors
                .encode(activeColor)));
      } catch (NumberFormatException e) {
      }

      drawFilled =
          Boolean.valueOf(settings.getProperty("HighlightShapeCellRenderer.drawfilled", "true"))
              .booleanValue();
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region) {
      Region r = (Region) obj;

      if (selected) {
        drawAndPossiblyFillPolygon(r, selectedColor);
      }

      if (active) {
        drawAndPossiblyFillPolygon(r, activeColor);
      }
    }
  }

  private void drawAndPossiblyFillPolygon(Region r, Color col) {
    CoordinateID c = r.getCoordinate();

    Rectangle rect = cellGeo.getCellRect(c.getX(), c.getY());
    rect.translate(-offset.x, -offset.y);

    Polygon p = cellGeo.getScaledPolygon();

    // make a copy of the polygon
    p = new Polygon(p.xpoints, p.ypoints, p.npoints);
    p.translate(rect.x, rect.y);

    if (drawFilled) {
      Color newCol =
          new Color(col.getRed(), col.getGreen(), col.getBlue(),
              HighlightShapeCellRenderer.ALPHALEVEL);
      graphics.setColor(newCol);
      graphics.fillPolygon(p);
    }

    graphics.setColor(col);

    graphics.setStroke(HighlightShapeCellRenderer.getDefaultStroke());

    graphics.drawPolygon(p);

    // if (1 == 2) {
    // p.translate(1, 0);
    // graphics.drawPolygon(p);
    // p.translate(0, 1);
    // graphics.drawPolygon(p);
    // p.translate(-1, 0);
    // graphics.drawPolygon(p);
    // }
  }

  // use this as singleton to this object
  private static BasicStroke defaultStroke = new BasicStroke(2.0f);

  private static BasicStroke getDefaultStroke() {
    return HighlightShapeCellRenderer.defaultStroke;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_HIGHLIGHT;
  }

  private boolean getDrawFilled() {
    return drawFilled;
  }

  private void setDrawFilled(boolean bool) {
    drawFilled = bool;

    if (settings != null) {
      settings.setProperty("HighlightShapeCellRenderer.drawfilled", String.valueOf(bool));
    }
  }

  private Color getActiveColor() {
    return activeColor;
  }

  private void setActiveColor(Color c) {
    activeColor = c;

    if (settings != null) {
      settings.setProperty("HighlightShapeCellRenderer.activeColor", Colors.encode(activeColor));
    }
  }

  private Color getSelectedColor() {
    return selectedColor;
  }

  private void setSelectedColor(Color c) {
    selectedColor = c;

    if (settings != null) {
      settings
          .setProperty("HighlightShapeCellRenderer.selectedColor", Colors.encode(selectedColor));
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new Preferences(this);
  }

  protected static class Preferences extends JPanel implements PreferencesAdapter {
    // The source component to configure
    protected HighlightShapeCellRenderer source = null;

    // GUI elements
    private JPanel pnlSelectedColor = null;
    private JPanel pnlActiveColor = null;
    private JCheckBox chkDrawFilled = null;

    /**
     * Creates a new Preferences object.
     */
    public Preferences(HighlightShapeCellRenderer r) {
      source = r;
      init();
    }

    private void init() {
      pnlSelectedColor = new JPanel();
      SwingUtils.setPreferredSize(pnlSelectedColor, 1.5, 1.5, false);
      pnlSelectedColor.setBackground(source.getSelectedColor());
      pnlSelectedColor.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          Color newColor =
              JColorChooser.showDialog(pnlSelectedColor.getTopLevelAncestor(), Resources
                  .get("map.highlightshapecellrenderer.textcolor"), pnlSelectedColor
                      .getBackground());

          if (newColor != null) {
            pnlSelectedColor.setBackground(newColor);
          }
        }
      });

      JLabel lblSelectedColor =
          new JLabel(Resources.get("map.highlightshapecellrenderer.lblselectedcolor"));
      lblSelectedColor.setLabelFor(pnlSelectedColor);

      pnlActiveColor = new JPanel();
      SwingUtils.setPreferredSize(pnlActiveColor, 1.5, 1.5, false);
      pnlActiveColor.setBackground(source.getActiveColor());
      pnlActiveColor.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          Color newColor =
              JColorChooser.showDialog(pnlActiveColor.getTopLevelAncestor(), Resources
                  .get("map.highlightshapecellrenderer.textcolor"), pnlActiveColor.getBackground());

          if (newColor != null) {
            pnlActiveColor.setBackground(newColor);
          }
        }
      });

      JLabel lblActiveColor =
          new JLabel(Resources.get("map.highlightshapecellrenderer.lblactivecolor"));
      lblActiveColor.setLabelFor(pnlActiveColor);

      chkDrawFilled =
          new JCheckBox(Resources.get("map.highlightshapecellrenderer.drawfilled"), source
              .getDrawFilled());

      setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
          GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);
      c.anchor = GridBagConstraints.WEST;
      c.gridx = 0;
      c.gridy = 0;
      this.add(lblSelectedColor, c);
      c.gridx = 1;
      c.gridy = 0;
      this.add(pnlSelectedColor, c);
      c.gridx = 0;
      c.gridy = 1;
      this.add(lblActiveColor, c);
      c.gridx = 1;
      c.gridy = 1;
      this.add(pnlActiveColor, c);
      c.gridx = 0;
      c.gridy = 2;
      this.add(chkDrawFilled, c);
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      source.setSelectedColor(pnlSelectedColor.getBackground());
      source.setActiveColor(pnlActiveColor.getBackground());
      source.setDrawFilled(chkDrawFilled.isSelected());
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
      return source.getName();
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.highlightshapecellrenderer.name");
  }

}
