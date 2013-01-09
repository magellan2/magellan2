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
 * Returns a field of an object of class {@link Region}.
 * 
 * @author unknown
 * @version 1.0rn
 */
public class RegionFieldReplacer extends AbstractRegionReplacer {
  /** Identifier for "any value mode" */
  public static final int MODE_ALL = 0;

  /** Identifier for "non-negative mode" */
  public static final int MODE_NON_NEGATIVE = 1;

  /** Identifier for "positive mode" */
  public static final int MODE_POSITIVE = 2;
  protected Field field;
  protected int mode;

  // Fiete 20080805
  // made the fields private, now going to use the methods!
  protected Method method;

  /**
   * Creates a new RegionFieldReplacer object.
   * 
   * @param field A field name of Region
   * @param mode Defines what is returned for negative values.
   * @throws RuntimeException if the given field is not accessible
   */
  public RegionFieldReplacer(String field, int mode) {
    try {
      this.field = MagellanRegionImpl.class.getField(field);
    } catch (Exception exc) {
      // throw new RuntimeException("Error retrieving region field " + field);
      this.field = null;
    }
    if (this.field == null) {
      try {
        String normalizedField = field.substring(0, 1).toUpperCase() + field.substring(1);
        method = MagellanRegionImpl.class.getMethod("get" + normalizedField, (Class[]) null);
      } catch (Exception exc) {
        // throw new RuntimeException("Error retrieving region field " + field);
        method = null;
      }
    }
    if (this.field == null && method == null)
      throw new RuntimeException("Error retrieving region field " + field);

    this.mode = mode;
  }

  protected void setMethod(Method method) {
    this.method = method;
  }

  protected void setMode(int mode) {
    this.mode = mode;
  }

  protected RegionFieldReplacer() {
    //
  }

  /**
   * Returns the value of the property for the region. If {@link #MODE_ALL} is set, returns any
   * number, if {@link #MODE_NON_NEGATIVE} is set, returns null on negatives, if
   * {@link #MODE_POSITIVE} is set, return <code>null</code> on non-positives.
   * 
   * @return the value or null
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region r) {
    if (field == null && method == null)
      return null;
    try {
      Object o = null;
      if (field != null) {
        o = field.get(r);
      }
      if (method != null) {
        o = method.invoke(r, (Object[]) null);
      }

      if (o != null) {
        if (!(o instanceof Number))
          return o;

        Number n = (Number) o;

        switch (mode) {
        case MODE_ALL:
          return o;

        case MODE_NON_NEGATIVE:

          if (n.doubleValue() >= 0)
            return o;

          break;

        case MODE_POSITIVE:

          if (n.doubleValue() > 0)
            return o;

          break;

        default:
          break;
        }
      }
    } catch (Exception exc) {
      // return null on error
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.regionfieldreplacer."
        + (field != null ? field.getName() : method != null ? method.getName() : "")
        + ".description");
  }

}
