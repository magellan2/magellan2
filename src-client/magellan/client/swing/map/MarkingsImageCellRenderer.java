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
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;

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
public class MarkingsImageCellRenderer extends ImageCellRenderer {
  private static final Logger log = Logger.getInstance(MarkingsImageCellRenderer.class);

  /** DOCUMENT-ME */
  public static final String ICON_TAG = "regionicon";
  private StringBuffer buf;

  /**
   * Creates a new MarkingsImageCellRenderer object.
   */
  public MarkingsImageCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    buf = new StringBuffer();
  }

  private Collection<String> markingRenderImagesNotFound = new HashSet<String>();

  /**
   * DOCUMENT-ME
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region && ((Region) obj).hasTags()) {
      Region r = (Region) obj;
      CoordinateID c = r.getCoordinate();

      Rectangle rect = cellGeo.getImageRect(c.getX(), c.getY());
      rect.translate(-offset.x, -offset.y);

      int i = 1;

      buf.setLength(0);
      buf.append(MarkingsImageCellRenderer.ICON_TAG);

      String key = null;

      do {
        key = buf.toString();

        if (r.containsTag(key)) {
          StringTokenizer st = new StringTokenizer(r.getTag(key), " ");

          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            Image img = getImage(token);

            if (img != null) {
              graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
            } else {
              if (token != null && !markingRenderImagesNotFound.contains(token)) {
                MarkingsImageCellRenderer.log
                    .warn("MarkingsImageCellRenderer.render(): marking image \"" + token
                        + "\" not found!");
                markingRenderImagesNotFound.add(token);
              }
            }
          }
        }

        if (i > 1) {
          buf.setLength(buf.length() - 1);
        }

        buf.append(i);
        i++;
      } while (i <= 10);
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_MARKINGS;
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.markingsimagecellrenderer.name");
  }

}
