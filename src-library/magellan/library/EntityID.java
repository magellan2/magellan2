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

package magellan.library;

import java.util.HashMap;
import java.util.Map;

import magellan.library.utils.IDBaseConverter;


/**
 * A class used to uniquely identify such objects as regions, ships or buildings by an integer. The
 * representation of the integer  depends on the system default defined in the IDBaseConverter
 * class.
 */
public class EntityID extends IntegerID {
    protected int radix = 10;
    
	/**
	 * Constructs a new entity id based on the specified integer object.
	 *
	 * 
	 */
	protected EntityID(Integer i, int radix) {
		super(i);
    this.radix = radix;
	}

	/**
	 * Constructs a new entity id based on a new Integer object created from the specified int.
	 *
	 * 
	 */
	protected EntityID(int i, int radix) {
		super(i);
        this.radix = radix;
	}

	/**
	 * Constructs a new entity id parsing the specified string for an integer using the specified
	 * radix.
	 *
	 * 
	 * 
	 */
	protected EntityID(String s, int radix) {
		super(IDBaseConverter.parse(s, radix));
        this.radix = radix;
	}

	/** a static cache to use this class as flyweight factory */
	private static Map<Integer,EntityID> idMap = new HashMap<Integer, EntityID>();

	/**
	 * Returns a (possibly) new EntityID object.
	 *
	 * 
	 *
	 * 
	 *
	 * @throws NullPointerException DOCUMENT-ME
	 */
	public static EntityID createEntityID(Integer o, int radix) {
		if(o == null) {
			throw new NullPointerException();
		}
        
		EntityID id = EntityID.idMap.get(o);

		if(id == null) {
			id = new EntityID(o, radix);
			EntityID.idMap.put(o, id);
		}

		return id;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static EntityID createEntityID(int i, int radix) {
		return EntityID.createEntityID(new Integer(i),radix);
	}

	/**
	 * Constructs a new entity id parsing the specified string for an integer using the default
	 * radix of the IDBaseConverter class.
	 *
	 * 
	 *
	 * 
	 */
	public static EntityID createEntityID(String s, int radix) {
		return EntityID.createEntityID(IDBaseConverter.parse(s,radix),radix);
	}

	/**
	 * Returns a string representation of this id which depends on the output of the
	 * IDBaseConverter class.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return IDBaseConverter.toString(this.intValue(),radix);
	}

	/**
	 * Returns whether some other object is equal to this integer id.
	 *
	 * 
	 *
	 * @return true, if o is an instance of class EntityID and the numerical values of this object
	 * 		   and o match.
	 */
	@Override
  public boolean equals(Object o) {
	  if (o==null) {
      return false;
    }
		try {
			return (this == o) || id == ((EntityID) o).id;
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Imposes a natural ordering on EntityID objects which is based on the natural ordering of the
	 * integers they are constructed from.
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public int compareTo(Object o) {
        int thisInt = intValue();
        int oInt = ((EntityID) o).intValue();
        return thisInt > oInt ? 1 : thisInt == oInt ? 0 : -1;
	}
}
