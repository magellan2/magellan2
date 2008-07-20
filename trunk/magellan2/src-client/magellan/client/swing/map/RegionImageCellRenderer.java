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
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.rules.Date;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * This renderer writes an image per region onto the screen.
 *
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class RegionImageCellRenderer extends ImageCellRenderer implements ContextChangeable {
	/** DOCUMENT-ME */
	public static final String MAP_TAG = "mapicon";
	private static final Logger log = Logger.getInstance(RegionImageCellRenderer.class);
	private boolean fogOfWar = true;
	protected JCheckBoxMenuItem item = null;
	protected ContextObserver obs = null;

	/**
	 * Creates a new RegionImageCellRenderer object.
	 */
	public RegionImageCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
		fogOfWar = (Boolean.valueOf(settings.getProperty("RegionImageCellRenderer.fogOfWar",Boolean.TRUE.toString()))).booleanValue();
		item = new JCheckBoxMenuItem(Resources.get("map.regionimagecellrenderer.chk.showfow.caption"), fogOfWar);
		item.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					fogOfWar = item.isSelected();
					getSettings().setProperty("RegionImageCellRenderer.fogOfWar",String.valueOf(fogOfWar));

					if(obs != null) {
						obs.contextDataChanged();
					}
				}
			});
	}

  /**
   * 
   */
	protected Properties getSettings() {
		return settings;
	}

	/**
	 * 
	 */
	@Override
  public void render(Object obj, boolean active, boolean selected) {
    RegionImageCellRenderer.log.debug("render()");
		if(obj instanceof Region) {
			Region r = (Region) obj;
			CoordinateID c = r.getCoordinate();

			Rectangle rect = cellGeo.getImageRect(c.x, c.y);
			rect.translate(-offset.x, -offset.y);

			if(r.containsTag(RegionImageCellRenderer.MAP_TAG)) {
				Image img = getImage(r.getTag(RegionImageCellRenderer.MAP_TAG));
        
				if(img != null) {
					drawImage(r, img, rect);

					return;
				}
			}

			UnitContainerType type = r.getType();

			if(type != null) {
			  String imageName = type.getID().toString();
        
        // first try a season specific icon
			  // Fiete 20080518: check, if seasonal images wanted...
			  if ((r.getData().getDate() != null) && (Boolean.valueOf(settings.getProperty(PropertiesHelper.BORDERCELLRENDERER_USE_SEASON_IMAGES, "false")))) {
			    switch (r.getData().getDate().getSeason()) {
            case Date.SPRING: imageName+="_spring"; break;
            case Date.SUMMER: imageName+="_summer"; break;
            case Date.AUTUMN: imageName+="_autumn"; break;
            case Date.WINTER: imageName+="_winter"; break;
			    }
			  }
        
				Image img = getImage(imageName,false);
				
				// if we cannot find it, try a default icon.
				if (img == null) {
	        imageName = type.getID().toString(); 
				  img = getImage(imageName);
				}

				if(img != null) {
					drawImage(r, img, rect);
				} else {
          img = getImage("notype",true);
          if (img!=null){
            drawImage(r, img, rect);
            RegionImageCellRenderer.log.warnOnce("RegionImageCellRenderer.render(): using predifined notype-image for unknown type: " + imageName);
          } else {
            RegionImageCellRenderer.log.warnOnce("RegionImageCellRenderer.render(): predefined image not found: notype");
          }
				}
			} else {
				RegionImageCellRenderer.log.warnOnce("RegionImageCellRenderer.render(): Couldn't determine region type for region: " + r.toString());
			}
		}
	}

  /**
   * 
   */
	protected void drawImage(Region r, Image img, Rectangle rect) {
		if(img != null) {
			graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
		} else {
			RegionImageCellRenderer.log.warn("RegionImageCellRenderer.render(): image is null");
		}

		if(fogOfWar) {
			Image fogImg = getImage("Nebel");

			if((fogImg != null) && r.fogOfWar()) {
				graphics.drawImage(fogImg, rect.x, rect.y, rect.width, rect.height, null);
			}
		}
	}

	/**
	 * 
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_REGION;
	}

	/**
	 * 
	 */
	public boolean getFogOfWar() {
		return fogOfWar;
	}

	/**
	 * 
	 */
	public void setFogOfWar(boolean bool) {
		fogOfWar = bool;
		settings.setProperty("RegionImageCellRenderer.fogOfWar", String.valueOf(fogOfWar));
		item.setSelected(fogOfWar);
	}

	/**
	 * 
	 */
	@Override
  public PreferencesAdapter getPreferencesAdapter() {
		return new Preferences(this);
	}

	/**
	 * 
	 */
	public JMenuItem getContextAdapter() {
		return item;
	}

	/**
	 * 
	 */
	public void setContextObserver(ContextObserver co) {
		obs = co;
	}
  

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.regionimagecellrenderer.name");
  }

  /**
   *
   * @author ...
   * @version 1.0, 11.11.2007
   */
	private class Preferences extends JPanel implements PreferencesAdapter {
		// The source component to configure
		private RegionImageCellRenderer source = null;

		// GUI elements
		private JCheckBox chkFogOfWar = null;

		/**
		 * Creates a new Preferences object.
		 *
		 * 
		 */
		public Preferences(RegionImageCellRenderer r) {
			this.source = r;
			init();
		}

		private void init() {
			chkFogOfWar = new JCheckBox(Resources.get("map.regionimagecellrenderer.chk.showfow.caption"), source.getFogOfWar());
			this.add(chkFogOfWar);
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			source.setFogOfWar(chkFogOfWar.isSelected());
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Component getComponent() {
			return this;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getTitle() {
			return source.getName();
		}
	}
}
