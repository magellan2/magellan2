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
 * 
 * <p>
 * Three possible constructors:
 * 
 * <ul>
 * <li>
 * RegionType object -> direct test
 * </li>
 * <li>
 * StringID -> compares given region's type ID
 * </li>
 * <li>
 * String -> creates a StringID
 * </li>
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
	 *
	 * 
	 */
	public RegionTypeSwitch(String type) {
		this(StringID.create(type));
	}

	/**
	 * Creates a RegionType Switch out of a String ID describing a Region Type.
	 *
	 * 
	 */
	public RegionTypeSwitch(StringID type) {
		id = type;
	}

	/**
	 * Creates a RegionType Switch out of a Region Type.
	 *
	 * 
	 */
	public RegionTypeSwitch(RegionType type) {
		this.type = type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public RegionType getRegionType() {
		return type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public StringID getStringID() {
		return id;
	}

	/**
	 * Compares the region's type with the ID given in the constructor.
	 *
	 * 
	 *
	 * 
	 */
	public boolean isSwitchingRegion(Region r) {
		boolean res = false;

		if(type != null) {
			res = type.equals(r.getType());
		} else if(id != null) {
			res = id.equals(r.getType().getID());
		}

		return res;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
    String s = Resources.get("util.replacers.regiontypereplacer.description");

		if(s == null) {
			if(type != null) {
				return type.getName();
			}

			return id.toString();
		} else {
			if(type != null) {
				return s + type.getName();
			}

			return s + id.toString();
		}
	}
}
