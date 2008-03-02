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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Resources;


/**
 * A renderer for Scheme objects. Schemes are seen from the "Astralraum".
 */
public class SchemeCellRenderer extends ImageCellRenderer {
	/**
	 * Creates a new BuildingCellRenderer object.
	 *
	 * 
	 * 
	 */
	public SchemeCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @param obj the region to be rendered
	 * @param active no use
	 * @param selected region to be rendered shall be marked as part of the schemes that are active
	 * 		  right now
	 */
	public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region) {
			Region r = (Region) obj;

			Image schemeImage = getImage("schemen");

			CoordinateID c = r.getCoordinate();
			Point pos = new Point(cellGeo.getImagePosition(c.x, c.y));
			pos.translate(-offset.x, -offset.y);

			Dimension size = cellGeo.getImageSize();

			graphics.drawImage(schemeImage, pos.x, pos.y, size.width, size.height, null);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPlaneIndex() {
		return Mapper.PLANE_SCHEMES;
	}

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.schemecellrenderer.name");
  }

}
