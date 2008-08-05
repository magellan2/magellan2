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
import java.lang.reflect.Method;

import magellan.library.Region;
import magellan.library.impl.MagellanRegionImpl;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author unknown
 * @version 1.0rn */
public class RegionFieldReplacer extends AbstractRegionReplacer {
	/** DOCUMENT-ME */
	public static final int MODE_ALL = 0;

	/** DOCUMENT-ME */
	public static final int MODE_NON_NEGATIVE = 1;

	/** DOCUMENT-ME */
	public static final int MODE_POSITIVE = 2;
	protected Field field;
	protected int mode;
  
  // Fiete 20080805
  // made the fields private, now going to use the methods!
  protected Method method;
  

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
			this.field = MagellanRegionImpl.class.getField(field);
		} catch(Exception exc) {
			// throw new RuntimeException("Error retrieving region field " + field);
      this.field=null;
		}
    if (this.field==null){
      try {
        String normalizedField = field.substring(0,1).toUpperCase() + field.substring(1);
        this.method = MagellanRegionImpl.class.getMethod("get" + normalizedField,null);
      } catch(Exception exc) {
        // throw new RuntimeException("Error retrieving region field " + field);
        this.method=null;
      }
    }
    if (this.field==null && this.method==null){
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
	@Override
  public Object getRegionReplacement(Region r) {
    if (this.field==null && this.method==null){
      return null;
    }
		try {
			Object o = null;
      if (this.field!=null) {o = field.get(r);}
      if (this.method!=null) {o = this.method.invoke(r, null);}

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
