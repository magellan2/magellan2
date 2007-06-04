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
import java.util.Hashtable;
import java.util.Map;
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
	 *
	 * 
	 * 
	 */
	public MarkingsImageCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
		buf = new StringBuffer();
	}

    private Collection<String> markingRenderImagesNotFound = new HashSet<String>();
    
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region && ((Region) obj).hasTags()) {
			Region r = (Region) obj;
			CoordinateID c = r.getCoordinate();

			Rectangle rect = cellGeo.getImageRect(c.x, c.y);
			rect.translate(-offset.x, -offset.y);

			int i = 1;

			buf.setLength(0);
			buf.append(ICON_TAG);

			String key = null;

			do {
				key = buf.toString();

				if(r.containsTag(key)) {
					StringTokenizer st = new StringTokenizer(r.getTag(key), " ");

					while(st.hasMoreTokens()) {
                        String token = st.nextToken();
						Image img = getImage(token);

						if(img != null) {
							graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
						} else {
                            if(token != null && !markingRenderImagesNotFound.contains(token)) {
                                log.warn("MarkingsImageCellRenderer.render(): marking image \""+token+"\" not found!");
                                markingRenderImagesNotFound.add(token);
                            }
						}
					}
				}

				if(i > 1) {
					buf.setLength(buf.length() - 1);
				}

				buf.append(i);
				i++;
			} while(i <= 10);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPlaneIndex() {
		return Mapper.PLANE_MARKINGS;
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("name", "Additional icons");
		}

		return defaultTranslations;
	}


  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("magellan.map.markingsimagecellrenderer.name");
  }

}
