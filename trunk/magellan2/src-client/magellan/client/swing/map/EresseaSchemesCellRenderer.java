// class magellan.plugin.RealSchemesCellRenderer
// created on 13.12.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client.swing.map;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.logging.Logger;

/**
 * TODO This class must be commented
 *
 * @author ...
 * @version 1.0, 13.12.2007
 */
public class EresseaSchemesCellRenderer extends ImageCellRenderer {
  private static final Logger log = Logger.getInstance(EresseaSchemesCellRenderer.class);
  private CoordinateID mapping = null;
  /**
   * @param geo
   * @param context
   */
  public EresseaSchemesCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    // construct new geometry
    this.offset = new Point();
    this.setCellGeometry(geo);
    // TODO Auto-generated constructor stub
  }

  public void setCellGeometry(CellGeometry geo) {
    CellGeometry newGeo = new CellGeometry();
    newGeo.setGeometry(geo.getPolygon().xpoints, geo.getPolygon().ypoints);
    newGeo.setImageOffset(geo.getImageOffset().x-96, geo.getImageOffset().y-96);
    newGeo.setImageSize(geo.getImageSize().width, geo.getImageSize().height);
    newGeo.setScaleFactor(geo.getScaleFactor() * (float)0.25);
//    trans.setSize((geo.getScaledPolygon().getBounds().width+1)*3/8, (geo.getScaledPolygon().getBounds().height+1)*3/8);
    super.setCellGeometry(newGeo);
  }
  
  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  public String getName() {
    // TODO localization
    return "Schemen / Schemes";
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
   */
  public int getPlaneIndex() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return Mapper.PLANE_BEHIND;
  }

  public void init(GameData data, Graphics g, Point offset) {
    this.mapping = data.getAstralMapping();
    super.init(data, g, offset);
  }
  
  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  public void render(Object obj, boolean active, boolean selected) {
    if (mapping == null) return;
    if(obj instanceof Region) {
      Region ar = (Region) obj;
      CoordinateID c = ar.getCoordinate();
      if (c.z==1) {
        for (Scheme s : ar.schemes()) {
          CoordinateID cr = s.getCoordinate();
          Region r = data.getRegion(cr);
          if (r != null) {
            UnitContainerType type = r.getType();
            if(type != null) {
              String imageName = type.getID().toString();
            
              Rectangle rect = cellGeo.getImageRect(cr.x-mapping.x, cr.y-mapping.y);
              rect.translate(-offset.x, -offset.y);
              graphics.drawImage(getImage(imageName), rect.x, rect.y, rect.width, rect.height, null);
            }
          }
        }
      }
    }
  }

  public void scale(float scaleFactor) {
    scaleFactor *= 0.25;
    super.scale(scaleFactor);
  }
  
}
