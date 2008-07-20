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
import magellan.library.Item;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class BuildingType extends UnitContainerType {
	private int minSkillLevel = -1;
	private int maxSize = -1;
	private Map<ID,Item> rawMaterials = null;
	private Map<ID,Item> maintenance = null;
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
	 */
	public void addRawMaterial(Item i) {
		if(rawMaterials == null) {
			rawMaterials = new Hashtable<ID, Item>();
		}

		rawMaterials.put(i.getItemType().getID(), i);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Collection<Item> getRawMaterials() {
    if (rawMaterials != null && rawMaterials.values() != null) {
      return Collections.unmodifiableCollection(rawMaterials.values());
    }
		return Collections.emptyList();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Item getRawMaterial(ID id) {
		if(rawMaterials != null) {
			return rawMaterials.get(id);
		} else {
			return null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addMaintenance(Item i) {
		if(maintenance == null) {
			maintenance = new Hashtable<ID, Item>();
		}

		maintenance.put(i.getItemType().getID(), i);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Collection<Item> getMaintenanceItems() {
    if (maintenance != null && maintenance.values() != null) {
      return Collections.unmodifiableCollection(maintenance.values());
    }
    return Collections.emptyList();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Item getMaintenance(ID id) {
		if(maintenance != null) {
			return maintenance.get(id);
		} else {
			return null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMinSkillLevel(int l) {
		minSkillLevel = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMinSkillLevel() {
		return minSkillLevel;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMaxSize(int m) {
		maxSize = m;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMaxSize() {
		return maxSize;
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addRegionType(RegionType type) {
		if(regionTypes == null) {
			regionTypes = new Hashtable<ID, RegionType>();
		}

		regionTypes.put(type.getID(), type);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsRegionType(RegionType t) {
		return (regionTypes != null) && regionTypes.containsKey(t.getID());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Collection<RegionType> regionTypes() {
    if (regionTypes != null && regionTypes.values() != null) {
      return Collections.unmodifiableCollection(regionTypes.values());
    }
    return Collections.emptyList();
	}
}
