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
import java.util.Properties;

import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Resources;


/**
 * A class for identifying unit objects through an integer. This class makes equivalent assumptions
 * about the representation of the integer as the EntityID class. It also provides additional
 * convenience methods and knowledge about TEMP unit ids (which are represented as negative
 * integers).
 */
public class UnitID extends EntityID {
	/**
	 * Constructs a new UnitID object based on an Integer object created from the specified int.
	 *
	 * @param i id as integer form
	 */
	protected UnitID(int i, int radix) {
		super(i,radix);
	}

	protected UnitID(Integer i, int radix) {
		super(i,radix);
	}

	/** a static cache to use this class as flyweight factory */
	private static Map<Integer,UnitID> idMap = new HashMap<Integer,UnitID>();

	/**
	 * Constructs a new UnitID object based on the specified Integer.
	 *
	 * @param o unitid as Integer
	 *
	 * @return UnitID of the given int
	 *
	 * @throws NullPointerException if o is null
	 */
	public static UnitID createUnitID(Integer o,int radix) {
		if(o == null) {
			throw new NullPointerException();
		}

		UnitID id = UnitID.idMap.get(o);

		if(id == null) {
			id = new UnitID(o, radix);
			UnitID.idMap.put(o, id);
		}

		return id;
	}

	/**
	 * Constructs a new UnitID object based on the specified Integer.
	 *
	 * @param i unitid as Integer
	 *
	 * @return UnitID of the given int
	 */
	public static UnitID createUnitID(int i, int radix) {
		return UnitID.createUnitID(new Integer(i), radix);
	}

    /**
	 * Constructs a new UnitID object by parsing the specified string for an integer in the default
	 * representation of class IDBaseConverter.
	 *
	 * @param s unitid as String
	 * @param radix radix as base for transforming string to int
	 *
	 * @return UnitID of the given string
	 */
	public static UnitID createUnitID(String s, int radix) {
		return UnitID.createUnitID(UnitID.valueOf(s, radix),radix);
	}

	/**
	 * Creates a temp id.
	 *
	 * @param data The current GameData object
	 * @param settings The active settings
	 * @param parentUnit The parent unit of the temp unit (maybe null)
	 *
	 * @return the new temp id. This is always negative as Magellan expects temp unit ids to be negative.
	 */
	public static UnitID createTempID(GameData data, Properties settings, Unit parentUnit) {
		if(data.getCurTempID() == -1) {
			// uninitialized
			String s = settings.getProperty("ClientPreferences.TempIDsInitialValue", "");
			data.setCurTempID(s);
		}

		if((data.getCurTempID() == 0) && (parentUnit != null)) {
			// use old system: same id as parent unit

			int i = ((UnitID) parentUnit.getID()).intValue();

			while(data.tempUnits().get(UnitID.createUnitID(-i,data.base)) != null) {
				i = UnitID.getNextDecimalID(i, data.base, true);
			}

			return UnitID.createUnitID(-i,data.base);
		} else {            
			int i = data.getCurTempID();
			UnitID checkID = UnitID.createUnitID(-i,data.base);

			while(data.tempUnits().get(checkID) != null) {
				boolean ascending = settings.getProperty("ClientPreferences.ascendingOrder", "true")
											.equalsIgnoreCase("true");

				if(settings.getProperty("ClientPreferences.countDecimal", "true").equalsIgnoreCase("true")) {
					i = UnitID.getNextDecimalID(i, data.base, ascending);
				} else {
					if(ascending) {
						i++;
					} else {
						i--;
					}
				}

				if(ascending) {
					if(i > IDBaseConverter.getMaxId(data.base)) {
						i = 1;
					}
				} else {
					if(i <= 0) {
						i = IDBaseConverter.getMaxId(data.base);
					}
				}

				checkID = UnitID.createUnitID(-i,data.base);
			}

			data.setCurTempID(i);
            return checkID;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param i
	 * @param ascending
	 *
	 * @return the next int, that is bigger than the given one but consists only out of decimal
	 * 		   digits (interpreted in the current base) if the given int did so also.
	 */
	private static int getNextDecimalID(int i, int radix, boolean ascending) {
		int base = radix;

		if(ascending) {
			i++;

			if((i % base) == 10) {
				i += (base - 10);
			}

			if((i % (base * base)) == (base * 10)) {
				i += ((base - 10) * base * base);
			}

			if((i % (base * base * base)) == (base * base * 10)) {
				i += ((base - 10) * base * base * base);
			}

			if(i > IDBaseConverter.getMaxId(base)) {
				i = 1;
			}
		} else {
			if(i == 0) {
				i = (base * base * base * 10) + 10;
			}

			if((i % (base * base * base)) == 0) {
				i = i - (base * base * base) + (base * base * 10);
			}

			if((i % (base * base)) == 0) {
				i = i - (base * base) + (base * 10);
			}

			if((i % base) == 0) {
				i = i - base + 10;
			}

			i--;

			if(i <= 0) {
				i = IDBaseConverter.getMaxId(base);
			}
		}

		return i;
	}

	/**
	 * Returns a String representation of this UnitID. The radix of the output depends on the
	 * default set in the IDBaseConverter class. This method is not TEMP id aware, i.e. negative
	 * ids are returned as the string representation of the absolute value but without a 'TEMP'
	 * prefix.
	 *
	 * @return String representation of this UnitID
	 */
	@Override
  public String toString() {
		return IDBaseConverter.toString(Math.abs(this.intValue()),radix);
	}

	/**
	 * Indicates that this UnitID is equal to some other object.
	 *
	 * @param o object to compare
	 *
	 * @return true, if o is an instance of UnitID and the integer values of this and the specfied
	 * 		   object o are equal.
	 */
	@Override
  public boolean equals(Object o) {
		try {
			return this == o || id == ((EntityID) o).id;
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Imposes a natural ordering on UnitID objects based on the natural ordering of the absolute
	 * values of the underlying integers.
	 *
	 * @param o object to compare
	 *
	 * @return int based on comparability
	 */
	@Override
  public int compareTo(Object o) {
		return Math.abs(this.intValue()) - Math.abs(((EntityID) o).intValue());
	}

	/**
	 * Returns the integer contained in the specified string with the specified radix. This method
	 * is TEMP id aware, i.e. the string "TEMP 909" would return an Integer object with the
	 * numerical value -909.
	 *
	 * @param s string represenation of the unit id
	 * @param radix radix to parse integer
	 *
	 * @return integer representation of the given string based on given radix
	 *
	 * @throws NumberFormatException if unit id is not parseable
	 */
	private static Integer valueOf(String s, int radix) {
		s = s.trim().replace('\t', ' ');

		int blankPos = s.indexOf(" ");

		if(blankPos == -1) {
			return Integer.valueOf(s, radix);
		} else {
			String part1 = s.substring(0, blankPos);

			if(part1.equalsIgnoreCase(Resources.getOrderTranslation(EresseaConstants.O_TEMP))) {
				return new Integer(-1 * Integer.parseInt(s.substring(blankPos).trim(), radix));
			} else {
				throw new NumberFormatException("UnitID: unable to parse id " + s);
			}
		}
	}
}
