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
import java.awt.Rectangle;
import java.util.Iterator;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.ID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class ShipCellRenderer extends ImageCellRenderer {
	/**
	 * Creates a new ShipCellRenderer object.
	 *
	 * 
	 * 
	 */
	public ShipCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
	}

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

			Iterator iter = r.ships().iterator();

			if(iter.hasNext()) {
				CoordinateID c = r.getCoordinate();
				Point pos = new Point(cellGeo.getImagePosition(c.x, c.y));
				pos.translate(-offset.x, -offset.y);

				Dimension size = cellGeo.getImageSize();

				// grep ships in region
				// The ship with the maximum capacity will be drawn only
				// Directions 0-6
				ShipInformation shipInformations[] = new ShipInformation[7];

				boolean multipleTypes = false;
        // find ships with max capacity
        while (iter.hasNext()) {
          Ship s = (Ship) iter.next();

          if (shipInformations[s.getShoreId() + 1] == null) {
            shipInformations[s.getShoreId() + 1] =
                new ShipInformation(s.getShipType().getCapacity(), s.getType().getName());
          }

          ShipInformation actShip = shipInformations[s.getShoreId() + 1];

          if (actShip.capacity != s.getShipType().getCapacity()) {
            multipleTypes = true;
            if (actShip.capacity < s.getShipType().getCapacity()) {
              actShip.capacity = s.getDeprecatedCapacity();
              actShip.typeName = s.getType().getName();
            }
          }
        }

				// 2. draw ships in region
				for(int shore = 0; shore < shipInformations.length; shore++) {
					ShipInformation actShip = shipInformations[shore];

					if(actShip == null) {
						continue;
					}

					Image img = getImage(actShip.typeName + shore);

					if(img == null) {
						// special image not found, use generic image
						img = getImage("Schiff" + shore);
					}

					if(img != null) {
						graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
					}
	        if (multipleTypes)
	          renderMultiple(r, shore);
				}
			}
			renderTravelThrough(r);
		}
	}

	private void renderMultiple(Region region, int shore) {
	  Image img = getImage("viele_schiffe"+shore);
	  if(img != null) {
	    CoordinateID c = region.getCoordinate();
	    Rectangle rect = cellGeo.getImageRect(c.x, c.y);    
	    rect.translate(-offset.x, -offset.y);
	    graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
	  }
	}

	/** 
	 * renders shipthrou informations here. May be smarter elsewhere.
	 */
	private void renderTravelThrough(Region region) {
		if(!region.getRegionType().isOcean()) {
			return;
		}
		boolean foundEnemy=false;
		boolean foundAllied=false;
		if(region.getTravelThruShips() != null) {
			for(Iterator<Message> iter = region.getTravelThruShips().iterator(); iter.hasNext(); ) {
				// Messages like "Wogenspalter (64ch)"
				String msg = iter.next().toString();
				int from = msg.lastIndexOf("(");
				int to   = msg.indexOf(")",from);
				if(from>-1 && to <msg.toString().length()) {
					ID sid = EntityID.createEntityID(msg.substring(from+1,to),region.getData().base);
					Ship ship = region.getData().getShip(sid);
					if(ship != null && ship.getOwnerUnit() != null &&
							ship.getOwnerUnit().getFaction() != null && 
							ship.getOwnerUnit().getFaction().getTrustLevel() >=FactionTrustComparator.ALLIED) {
						foundAllied=true;
					} else {
						foundEnemy=true;
					}
				}
			}
		}

		for (Ship ship : region.ships()) {
      if(ship != null && ship.getOwnerUnit() != null &&
          ship.getOwnerUnit().getFaction() != null && 
          ship.getOwnerUnit().getFaction().getTrustLevel() >=FactionTrustComparator.ALLIED) {
        
      } else {
        foundEnemy=true;
      }
		}
		
		if(foundAllied) {
			Image img = getImage("durchschiffung_alliiert");
			if(img != null) {
				CoordinateID c = region.getCoordinate();
				Rectangle rect = cellGeo.getImageRect(c.x, c.y);		
				rect.translate(-offset.x, -offset.y);
				graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
			}
		}		
		if(foundEnemy) {
			Image img = getImage("durchschiffung_feindlich");
			if(img != null) {
				CoordinateID c = region.getCoordinate();
				Rectangle rect = cellGeo.getImageRect(c.x, c.y);		
				rect.translate(-offset.x, -offset.y);
				graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
			}
		}		
	}
	
	private static class ShipInformation {
		/** DOCUMENT-ME */
		public int capacity = -1;

		/** DOCUMENT-ME */
		public String typeName = null;

		ShipInformation(int aCap, String aName) {
			capacity = aCap;
			typeName = aName;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_SHIP;
	}


  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.shipcellrenderer.name");
  }

}
