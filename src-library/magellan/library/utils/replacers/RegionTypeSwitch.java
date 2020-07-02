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

import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.rules.RegionType;
import magellan.library.utils.Resources;

/**
 * A switch that reacts if the given region is of a certain type.
 * <p>
 * Three possible constructors:
 * <ul>
 * <li>RegionType object &rarr; direct test</li>
 * <li>StringID -> compares given region's type ID</li>
 * <li>String -> creates a StringID</li>
 * </ul>
 * </p>
 * 
 * @author Andreas
 * @version 1.0
 */
public class RegionTypeSwitch extends AbstractRegionSwitch {
  /** For use if constructed with Region Type. */
  protected RegionType type = null;

  /** For use if constructed with String or String ID. */
  protected StringID id = null;

  /**
   * Creates a RegionType Switch out of a String describing a Region Type.
   */
  public RegionTypeSwitch(String type) {
    this(StringID.create(type));
  }

  /**
   * Creates a RegionType Switch out of a String ID describing a Region Type.
   */
  public RegionTypeSwitch(StringID type) {
    id = type;
  }

  /**
   * Creates a RegionType Switch out of a Region Type.
   */
  public RegionTypeSwitch(RegionType type) {
    this.type = type;
  }

  /**
   * Returns the defining region type.
   */
  public RegionType getRegionType() {
    return type;
  }

  /**
   * Returns the defining region type ID.
   */
  public StringID getStringID() {
    return id;
  }

  /**
   * Compares the region's with the type or type ID given in the constructor.
   */
  @Override
  public boolean isSwitchingRegion(Region r) {
    boolean res = false;

    if (type != null) {
      res = type.equals(r.getType());
    } else if (id != null) {
      res = id.equals(r.getType().getID());
    }

    return res;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    Object arg[] = new Object[1];
    if (type != null) {
      arg[0] = type.getName();
    } else {
      arg[0] = id.toString();
    }

    return Resources.get("util.replacers.regiontypereplacer.description", arg);
  }
}
