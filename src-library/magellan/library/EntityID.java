/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library;

import java.util.HashMap;
import java.util.Map;

import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.logging.Logger;

/**
 * A class used to uniquely identify such objects as regions, ships or buildings by an integer. The
 * representation of the integer depends on the system default defined in the IDBaseConverter class.
 */
public class EntityID extends IntegerID {
  private static final Logger log = Logger.getInstance(EntityID.class);

  protected final int radix;

  /** a static cache to use this class as flyweight factory */
  private static Map<Integer, EntityID> idMap = new HashMap<Integer, EntityID>();

  /**
   * Constructs a new entity id based on a new Integer object created from the specified int.
   * 
   * @param i id as integer form
   * @param radix the base
   */
  protected EntityID(int i, int radix) {
    super(i);
    this.radix = radix;
  }

  /**
   * Returns a (possibly) new EntityID object.
   * 
   * @param value id as integer form
   * @param radix the base
   * @return An EntityID object matching the given value and radix
   */
  public static EntityID createEntityID(int value, int radix) {
    EntityID id = EntityID.idMap.get(value);

    if (id == null || id.radix != radix) {
      if (id != null) {
        EntityID.log.warn("changing radix of id " + id);
      }

      id = new EntityID(value, radix);
      EntityID.idMap.put(value, id);
    }

    return id;
  }

  /**
   * Constructs a new entity id parsing the specified string for an integer using the specified
   * radix. Effectively the same as calling createEntityID(s, radix, radix).
   */
  public static EntityID createEntityID(String s, int radix) {
    return EntityID.createEntityID(s, radix, radix);
  }

  /**
   * Constructs a new entity id parsing the specified string for an integer using the specified
   * radix.
   * 
   * @param s unit id as String
   * @param inputRadix base for transforming string to int
   * @param outputRadix base for the return value
   * @return EntityID of the given string
   * @throws NumberFormatException if unit id is not parseable
   */
  public static EntityID createEntityID(String s, int inputRadix, int outputRadix) {
    return EntityID.createEntityID(IDBaseConverter.parse(s, inputRadix), outputRadix);
  }

  /**
   * Returns the radix which was used to create this ID.
   */
  public int getRadix() {
    return radix;
  }

  /**
   * Returns a string representation of this id which depends on the output of the IDBaseConverter
   * class.
   */
  @Override
  public String toString() {
    return IDBaseConverter.toString(intValue(), radix);
  }

  // (stm) equals and compareTo already have been overridden by IntegerID. Overriding it here is
  // unnecessary (and dangerous).
  // /**
  // * Returns whether some other object is equal to this integer id.
  // *
  // * @return true, if o is an instance of class EntityID and the numerical values of this object
  // and
  // * o match.
  // */
  // @Override
  // public boolean equals(Object o) {
  // if (o == this)
  // return true;
  // if (o instanceof EntityID)
  // return id == ((EntityID) o).id;
  // return false;
  // }
  //
  // /**
  // * Imposes a natural ordering on EntityID objects which is based on the natural ordering of the
  // * integers they are constructed from.
  // */
  // @Override
  // public int compareTo(Object o) {
  // int thisInt = intValue();
  // int oInt = ((EntityID) o).intValue();
  // return thisInt > oInt ? 1 : thisInt == oInt ? 0 : -1;
  // }

  /**
   * Returns a copy of this EntityID object.
   */
  @Override
  public EntityID clone() {
    // pavkovic 2003.07.08: we dont really clone this object as IntegerID is unchangeable after
    // creation
    return this;
  }
}
