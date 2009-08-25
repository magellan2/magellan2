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
import java.awt.Rectangle;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Resources;
import magellan.library.utils.mapping.LevelRelation;

/**
 * Renders the real space regions of level 0 behind the astral space regions of level 1.
 *
 * @author Ralf Duckstein
 * @version 1.0, 13.12.2007
 */
public class EresseaSchemesCellRenderer extends ImageCellRenderer {
  private static final int REAL_LAYER = 0;
  private static final int ASTRAL_LAYER = 1;
  
  private LevelRelation relation = null;
  /**
   * @param geo
   * @param context
   */
  public EresseaSchemesCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    // construct new geometry
    this.setCellGeometry(geo);
  }

  @Override
  public void setCellGeometry(CellGeometry geo) {
    CellGeometry newGeo = new CellGeometry();
    newGeo.setGeometry(geo.getPolygon().xpoints, geo.getPolygon().ypoints);
    newGeo.setImageOffset(geo.getImageOffset().x-96, geo.getImageOffset().y-96);
    newGeo.setImageSize(geo.getImageSize().width, geo.getImageSize().height);
    newGeo.setScaleFactor(geo.getScaleFactor() * (float)0.25);
    super.setCellGeometry(newGeo);
  }
  
  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.eresseaschemescellrenderer.name");
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_BEHIND;
  }

  @Override
  public void init(GameData data, Graphics g, Rectangle offset) {
    this.relation = data.getLevelRelation(EresseaSchemesCellRenderer.ASTRAL_LAYER, EresseaSchemesCellRenderer.REAL_LAYER);
    super.init(data, g, offset);
  }
  
  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (relation == null) {
      return;
    }
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
            
              Rectangle rect = cellGeo.getImageRect(cr.x-relation.x, cr.y-relation.y);
              rect.translate(-offset.x, -offset.y);
              graphics.drawImage(getImage(imageName), rect.x, rect.y, rect.width, rect.height, null);
            }
          }
        }
      }
    }
  }

  /**
   * @see magellan.client.swing.map.ImageCellRenderer#scale(float)
   */
  @Override
  public void scale(float scaleFactor) {
    scaleFactor *= 0.25f;
    super.scale(scaleFactor);
  }
  
}
