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

import java.lang.reflect.Method;

import magellan.library.Region;


/**
 * DOCUMENT ME!
 *
 * @author unknown
 * @version 1.0
 */
public class RegionMethodReplacer extends AbstractRegionReplacer {
	/** DOCUMENT-ME */
	public static final int MODE_ALL = 0;

	/** DOCUMENT-ME */
	public static final int MODE_NON_NEGATIVE = 1;

	/** DOCUMENT-ME */
	public static final int MODE_POSITIVE = 2;
	protected Method method;
	protected int mode;

	/**
	 * Creates a new RegionMethodReplacer object.
	 *
	 * 
	 * 
	 *
	 * @throws RuntimeException DOCUMENT-ME
	 */
	public RegionMethodReplacer(String method, int mode) {
		try {
			this.method = Class.forName("magellan.library.impl.MagellanRegionImpl").getMethod(method);
		} catch(Exception exc) {
			throw new RuntimeException("Error retrieving region method " + method);
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
	@Override
  public Object getRegionReplacement(Region r) {
		try {
			Object o = method.invoke(r, (Object[])null);

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
		if(method != null) {
			return method.getName();
		}

    return "no desc";
	}
}
