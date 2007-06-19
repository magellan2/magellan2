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

package magellan.library.utils.replacers;

import java.lang.reflect.Field;

import magellan.library.Region;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author unknown
 * @version
 */
public class RegionFieldReplacer extends AbstractRegionReplacer {
	/** DOCUMENT-ME */
	public static final int MODE_ALL = 0;

	/** DOCUMENT-ME */
	public static final int MODE_NON_NEGATIVE = 1;

	/** DOCUMENT-ME */
	public static final int MODE_POSITIVE = 2;
	protected Field field;
	protected int mode;

	/**
	 * Creates a new RegionFieldReplacer object.
	 *
	 * 
	 * 
	 *
	 * @throws RuntimeException DOCUMENT-ME
	 */
	public RegionFieldReplacer(String field, int mode) {
		try {
			this.field = Region.class.getField(field);
		} catch(Exception exc) {
			throw new RuntimeException("Error retrieving region field " + field);
		}

		this.mode = mode;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getRegionReplacement(Region r) {
		try {
			Object o = field.get(r);

			if(o != null) {
				if(!(o instanceof Number)) {
					return o;
				}

				Number n = (Number) o;

				switch(mode) {
				case MODE_ALL:
					return o;

				case MODE_NON_NEGATIVE:

					if(n.doubleValue() >= 0) {
						return o;
					}

					break;

				case MODE_POSITIVE:

					if(n.doubleValue() > 0) {
						return o;
					}

					break;

				default:
					break;
				}
			}
		} catch(Exception exc) {
		}

		return null;
	}
  

  public String getDescription() {
    return Resources.get("util.replacers.regionfieldreplacer.description");
  }  

}
