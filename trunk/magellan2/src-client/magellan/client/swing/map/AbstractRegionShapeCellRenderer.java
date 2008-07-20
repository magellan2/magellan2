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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.Region;


/**
 * Abstract base class for renderers that want to paint regions as a colored polygon.
 */
public abstract class AbstractRegionShapeCellRenderer extends HexCellRenderer {
	protected AbstractRegionShapeCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
	}

	protected abstract Color getSingleColor(Region r);

	protected abstract Color[] getColor(Region r);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	@Override
  public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region) {
			Region r = (Region) obj;
			CoordinateID c = r.getCoordinate();

			Point pos = cellGeo.getCellPosition(c.x, c.y);
			pos.translate(-offset.x, -offset.y);

			Polygon p = cellGeo.getScaledPolygon();
			p = new Polygon(p.xpoints, p.ypoints, p.npoints);
			p.translate(pos.x, pos.y);

			paintRegion(graphics, p, r);
		}
	}

	/**
	 * This renderer is for regions only and returns Mapper.PLANE_REGION.
	 *
	 * @return the plane index for this renderer
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_REGION;
	}

	/**
	 * Paints the specified region.
	 *
	 * 
	 * 
	 * 
	 */
	protected void paintRegion(Graphics g, Polygon p, Region r) {
		Color colors[] = getColor(r);

		if((colors == null) || (colors.length < 2)) {
			Color color = null;

			if((colors != null) && (colors.length > 0)) {
				color = colors[0];
			} else {
				color = getSingleColor(r);
			}

			g.setColor(color);
			g.fillPolygon(p);
		} else {
			Rectangle bounds = p.getBounds();
			int i;
			int j;
			int imax = bounds.x + bounds.width;
			int jmax = bounds.y + bounds.height;
			;

			for(i = bounds.x; i < imax; i++) {
				j = bounds.y;

				do {
					if(p.contains(i, j)) {
						break;
					}

					j++;
				} while(j < jmax);

				g.setColor(colors[(((i - bounds.x) * colors.length) / bounds.width)]);
				g.drawLine(i, j, i, j + (bounds.height - (2 * (j - bounds.y))));
			}
		}

		if(p.getBounds().width > 3) {
			g.setColor(Color.black);
			g.drawPolygon(p);
		}
	}
}
