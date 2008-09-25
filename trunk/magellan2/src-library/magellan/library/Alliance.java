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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import magellan.library.rules.AllianceCategory;
import magellan.library.utils.Resources;


/**
 * A class representing an alliance status between two factions. The faction having this alliance
 * is implicit, the target faction is an explicite field of this class.
 */
public class Alliance {
	private final Faction faction;
	private int state = 0;

	/**
	 * Create a new Alliance object for an alliance with the specified faction and without any
	 * alliance status set.
	 *
	 * @param faction the faction to establish an alliance with.
	 */
	public Alliance(Faction faction) {
		this(faction, 0);
	}

	/**
	 * Create a new Alliance object for an alliance with the specified faction and the specified
	 * status.
	 *
	 * @param faction the faction to establish an alliance with
	 * @param state the alliance status, must be one of constants SILVER, FIGHT, GIVE, GUARD, GUISE
	 * 		  or ALL.
	 *
	 * @throws NullPointerException if the faction parameter is null.
	 */
	public Alliance(Faction faction, int state) {
		if(faction == null) {
			throw new NullPointerException();
		}

		this.faction = faction;
		this.state = state;
	}

	private AllianceCategory getMaxAllianceCategory() {
		Iterator<AllianceCategory> iter = faction.getData().rules.getAllianceCategoryIterator();

		if(iter.hasNext()) {
			AllianceCategory ret = iter.next();

			while(iter.hasNext()) {
				AllianceCategory ac = iter.next();

				if(ac.compareTo(ret) > 0) {
					ret = ac;
				}
			}

			return ret;
		}

		return null;
	}

	/**
	 * Returns the faction this alliance refers to. The return value is never null.
	 *
	 * @return the faction of this alliance
	 */
	public Faction getFaction() {
		return faction;
	}

	/**
	 * Get the state bit-field of this alliance.
	 *
	 * @return the state bitfield.
	 */
	public int getState() {
		return state;
	}

	/**
	 * Determine whether a specific state of this alliance is set.
	 *
	 * @param selector specifying one of the constants in this class.
	 *
	 * @return true if specific state is set, false if not which state should be evaluated.
	 */
	public boolean getState(int selector) {
		return ((state & selector) == selector);
	}

	/**
	 * Get a string representation of the alliance state.
	 *
	 * @return the alliance state as string.
	 */
	public String stateToString() {
		AllianceCategory maxAC = getMaxAllianceCategory();

		if(maxAC == null) {
			return "";
		}

		if(getState(maxAC.getBitMask())) {
			return Resources.getOrderTranslation(maxAC.getName());
		}

		StringBuffer ret = new StringBuffer();

		// connect all state strings separated by spaces
		for(Iterator<AllianceCategory> iter = faction.getData().rules.getAllianceCategoryIterator(); iter.hasNext();) {
			AllianceCategory ac = iter.next();

			if(!ac.equals(maxAC) && getState(ac.getBitMask())) {
				ret.append(Resources.getOrderTranslation(ac.getName()));

				if(iter.hasNext()) {
					ret.append(" ");
				}
			}
		}

		return ret.toString();
	}
	
	/**
	 * Returns all alliance categories assigned to this alliance
	 */
	public List<AllianceCategory> getAllianceCategories() {
	  List<AllianceCategory> categories = new ArrayList<AllianceCategory>();
	  
    AllianceCategory maxAC = getMaxAllianceCategory();
    if(maxAC == null) return categories; 

    if(getState(maxAC.getBitMask())) {
      categories.add(maxAC);
      return categories;
    }
	  
	  
    for(Iterator<AllianceCategory> iter = faction.getData().rules.getAllianceCategoryIterator(); iter.hasNext();) {
      AllianceCategory ac = iter.next();

      if(!ac.equals(maxAC) && getState(ac.getBitMask())) {
        categories.add(ac);
      }
    }
    
    return categories;
	}

	/**
	 * Return a string representation of this alliance object.
	 *
	 * @return the alliance object as string.
	 */
	@Override
  public String toString() {
		return faction.toString() + ": " + stateToString();
	}

	/**
	 * A method to convert an alliance into a trustlevel. This method should be uses when Magellan
	 * calculates trust levels on its own.
	 *
	 * @return the trustlevel of this alliance
	 */
	public int getTrustLevel() {
		int ret = 0;

		// connect all state strings separated by spaces
		for(Iterator iter = faction.getData().rules.getAllianceCategoryIterator(); iter.hasNext();) {
			AllianceCategory ac = (AllianceCategory) iter.next();

			if(getState(ac.getBitMask())) {
				ret += 10;
			}
		}

		return ret;
	}
}
