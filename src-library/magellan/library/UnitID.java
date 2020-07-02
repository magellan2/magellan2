/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.atlantis.AtlantisConstants;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.logging.Logger;

/**
 * A class for identifying unit objects through an integer. This class makes equivalent assumptions
 * about the representation of the integer as the EntityID class. It also provides additional
 * convenience methods and knowledge about TEMP unit ids (which are represented as negative
 * integers).
 * <p>
 * FIXME (stm-2010) It would be better if UnitID wasn't a sub-type of EntityID. This can result in
 * unit IDs being compared to building IDs and found equal...
 * </p>
 */
public class UnitID extends EntityID {
  private static final Logger log = Logger.getInstance(UnitID.class);

  /** a static cache to use this class as flyweight factory */
  private static Map<Integer, UnitID> idMap = new HashMap<Integer, UnitID>();

  /**
   * Constructs a new UnitID object based on an Integer object created from the specified int.
   * 
   * @param i id as integer form
   * @param radix the base
   */
  protected UnitID(int i, int radix) {
    super(i, radix);
  }

  /**
   * Constructs a new UnitID object based on the specified Integer and specified radix.
   * 
   * @param o unit id as int
   * @param radix base for the UnitID
   * @return UnitID of the given int
   */
  public static UnitID createUnitID(int o, int radix) {
    UnitID id = UnitID.idMap.get(o);

    if (id == null || id.radix != radix) {
      if (id != null) {
        UnitID.log.warn("changing radix of id " + id);
      }
      id = new UnitID(o, radix);
      UnitID.idMap.put(o, id);
    }

    return id;
  }

  /**
   * Constructs a new UnitID object by parsing the specified string. Effectively the same as calling
   * createUnitID(s, radix, radix).
   * 
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  public static UnitID createUnitID(String s, int radix) {
    return UnitID.createUnitID(s, radix, radix);
  }

  /**
   * Constructs a new UnitID object by parsing the specified string.
   * 
   * @param s unit id as String
   * @param inputRadix base for transforming string to int
   * @param outputRadix base for the return value
   * @return UnitID of the given string
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */

  public static UnitID createUnitID(String s, int inputRadix, int outputRadix) {
    return UnitID.createUnitID(UnitID.valueOf(s, inputRadix).intValue(), outputRadix);
  }

  /**
   * Creates a temp id.
   * 
   * @param data The current GameData object
   * @param settings The active settings
   * @param parentUnit The parent unit of the temp unit (maybe null)
   * @return the new temp id. This is always negative as Magellan expects temp unit ids to be
   *         negative.
   */
  public static UnitID createTempID(GameData data, Properties settings, Unit parentUnit) {
    if (data.getCurTempID() == -1) {
      // uninitialized
      String s = "1";
      if (settings != null) {
        s = settings.getProperty("ClientPreferences.TempIDsInitialValue", "");
      }
      data.setCurTempID(s);
    }

    if ((data.getCurTempID() == 0) && (parentUnit != null)) {
      // use old system: same id as parent unit

      int i = (parentUnit.getID()).intValue();

      while (data.getTempUnit(UnitID.createUnitID(-i, data.base)) != null) {
        // does not make much sense, does it?
        // i = UnitID.getNextDecimalID(i, data.base, true);
        i++;
      }

      return UnitID.createUnitID(-i, data.base);
    } else {
      // find next free ID based off getCurTempID()
      int i = data.getCurTempID();
      UnitID checkID = UnitID.createUnitID(-i, data.base);

      while (data.getTempUnit(checkID) != null) {
        boolean ascending = true;
        if (settings != null) {
          ascending =
              settings.getProperty("ClientPreferences.ascendingOrder", "true").equalsIgnoreCase(
                  "true");
        }

        if (settings == null
            || settings.getProperty("ClientPreferences.countDecimal", "true").equalsIgnoreCase(
                "true")) {
          i = UnitID.getNextDecimalID(i, data.base, ascending);
        } else {
          if (ascending) {
            i++;
          } else {
            i--;
          }
        }

        if (ascending) {
          if (i > IDBaseConverter.getMaxId(data.base)) {
            i = 1;
          }
        } else {
          if (i <= 0) {
            i = IDBaseConverter.getMaxId(data.base);
          }
        }

        checkID = UnitID.createUnitID(-i, data.base);
      }

      data.setCurTempID(i);
      return checkID;
    }
  }

  /**
   * Returns the next int, that is bigger than the given one but consists only out of decimal digits
   * (interpreted in the current base).
   * 
   * @param current the last ID, that is to be increased (or decreased)
   * @param base the base where the digits come from
   * @param ascending if <code>true</code>, the current id is increased, otherwise decreased
   * @return the next int, that is bigger than the given one (or smaller if ascending==false) but
   *         consists only of decimal digits (interpreted in the given base). The result is also
   *         &gt; 0 and &le; {@link IDBaseConverter#getMaxId(int)}
   */
  protected static int getNextDecimalID(int current, int base, boolean ascending) {
    int result = current;

    if (ascending) {
      result++;
      if (result <= 0) {
        result = 1;
      }
      for (int nextDigit = 1; nextDigit <= IDBaseConverter.getMaxId(base); nextDigit *= base)
        if ((result % (nextDigit * base)) >= 10 * nextDigit) {
          result += nextDigit * base - result % (nextDigit * base);
        }

      if (result > IDBaseConverter.getMaxId(base)) {
        result = 1;
      }
    } else {
      result--;

      if (result <= 0 || result > IDBaseConverter.getMaxId(base)) {
        result = IDBaseConverter.getMaxId(base);
      }

      int nextDigit = 1;
      while (nextDigit < IDBaseConverter.getMaxId(base)) {
        nextDigit *= base;
      }
      for (; nextDigit >= 1; nextDigit /= base)
        if ((result % (nextDigit * base)) >= 10 * nextDigit) {
          result -= result % (nextDigit * base);
          for (; nextDigit >= 1; nextDigit /= base) {
            result += 9 * nextDigit + 0;
          }

        }

      while ((result % base) >= 10) {
        int j = 9;
        while (j < result) {
          j = j * base + 9;
        }
        result = (j - 9) / base;
      }

    }

    return result;
  }

  /**
   * Returns a String representation of this UnitID. The radix of the output depends on the default
   * set in the IDBaseConverter class. This method is not TEMP id aware, i.e. negative ids are
   * returned as the string representation of the absolute value but without a 'TEMP' prefix.
   * 
   * @return String representation of this UnitID
   */
  @Override
  public String toString() {
    return IDBaseConverter.toString(Math.abs(intValue()), radix);
  }

  // /**
  // * Returns a String representation of this UnitID. The radix of the output depends on the
  // default
  // * set in the IDBaseConverter class. If <code>temp</code> is not <code>null</code>, it is
  // prefixed
  // * if this is a negative ID.
  // *
  // * @param temp
  // * @return String representation of this UnitID
  // */
  // public String toFullString(String temp) {
  // if (temp != null && intValue() < 0)
  // return temp + " " + IDBaseConverter.toString(Math.abs(intValue()), radix);
  // else
  // return IDBaseConverter.toString(Math.abs(intValue()), radix);
  // }

  // (stm) equals and compareTo already have been overridden by IntegerID. Overriding it here is
  // unnecessary (and dangerous).

  /**
   * Returns the integer contained in the specified string with the specified radix. This method is
   * TEMP id aware, i.e. the string "TEMP 909" would return an Integer object with the numerical
   * value -909.
   * 
   * @param s string representation of the unit id
   * @param radix radix to parse integer
   * @return integer representation of the given string based on given radix
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  private static Integer valueOf(String s, int radix) {
    s = s.trim().replace('\t', ' ');

    int blankPos = s.indexOf(" ");

    if (blankPos == -1)
      return Integer.valueOf(s, radix);
    else {
      String part1 = s.substring(0, blankPos);

      // FIXME hack, must be game specific
      if (part1.equalsIgnoreCase(EresseaConstants.OC_TEMP.toString())
          || part1.equalsIgnoreCase(AtlantisConstants.OC_NEW.toString()))
        return Integer.valueOf(-1 * Integer.parseInt(s.substring(blankPos).trim(), radix));
      else
        throw new NumberFormatException("UnitID: unable to parse id " + s);
    }
  }

  /**
   * @see magellan.library.EntityID#clone()
   */
  @Override
  public UnitID clone() {
    return this;
  }
}
