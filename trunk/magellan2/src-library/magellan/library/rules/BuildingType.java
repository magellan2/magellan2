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

package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import magellan.library.ID;


/**
 * Stores attributes for a type of building.
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class BuildingType extends ConstructibleType {
	private Map<ID,Integer> skillBonuses = null;
	private Map<ID,RegionType> regionTypes = null;

	/**
	 * Creates a new BuildingType object.
	 *
	 * 
	 */
	public BuildingType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public int getSkillBonus(SkillType skillType) {
		int bonus = 0;

		if(skillBonuses != null) {
			Integer i = skillBonuses.get(skillType.getID());

			if(i != null) {
				bonus = i.intValue();
			}
		}

		return bonus;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setSkillBonus(SkillType skillType, int bonus) {
		if(skillBonuses == null) {
			skillBonuses = new Hashtable<ID, Integer>();
		}

		skillBonuses.put(skillType.getID(), new Integer(bonus));
	}

	/**
	 *  Registers type as allowed region type for this building.
	 *
	 * @param type
	 */
	public void addRegionType(RegionType type) {
		if(regionTypes == null) {
			regionTypes = new Hashtable<ID, RegionType>();
		}

		regionTypes.put(type.getID(), type);
	}

	/**
	 * Returns <code>true</code> if either no RegionType has been registered by {@link #addRegionType(RegionType)} or if at least one RegionType has been registered and type is one of the allowed types.  
	 *
	 * @param type
	 */
	public boolean containsRegionType(RegionType type) {
		return (regionTypes != null) && regionTypes.containsKey(type.getID());
	}

	/**
	 * Returns a list of allowed RegionTypes. An empty list indicates that any type is allowed!
	 */
	public Collection<RegionType> regionTypes() {
    if (regionTypes != null && regionTypes.values() != null) {
      return Collections.unmodifiableCollection(regionTypes.values());
    }
    return Collections.emptyList();
	}
}
