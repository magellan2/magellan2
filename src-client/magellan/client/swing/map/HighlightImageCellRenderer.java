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

import java.awt.Image;
import java.awt.Rectangle;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class HighlightImageCellRenderer extends ImageCellRenderer {
  private static final Logger log = Logger.getInstance(HighlightImageCellRenderer.class);

  /**
   * Creates a new HighlightImageCellRenderer object.
   */
  public HighlightImageCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region) {
      Region r = (Region) obj;

      if (selected) {
        renderIt(r, "Selected");
      }

      if (active) {
        renderIt(r, "Active");
      }
    }
  }

  private void renderIt(Region r, String imgName) {
    CoordinateID c = r.getCoordinate();

    Rectangle rect = cellGeo.getImageRect(c.x, c.y);
    rect.translate(-offset.x, -offset.y);

    Image img = getImage(imgName);

    if (img != null) {
      graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
    } else {
      HighlightImageCellRenderer.log.warn("HighlightImageCellRenderer.render(): image " + imgName
          + " is null!");
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_HIGHLIGHT;
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.highlightimagecellrenderer.name");
  }

}
