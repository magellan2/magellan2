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

import java.util.Hashtable;
import java.util.Map;

import magellan.library.ID;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class Race extends UnitContainerType {
	private int recruit = -1;
	private float weight = 0;
	private float capacity = 0;
	private Map<ID,Integer> skillBonuses = null;
  private Map<ID,Map<ID,Integer>> skillRegionBonuses = null;
	private int additiveShipBonus;
  private String recruitName;

	/**
	 * Creates a new Race object.
	 *
	 * 
	 */
	public Race(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setRecruitmentCosts(int r) {
		recruit = r;
	}

	/**
	 * Returns the cost for creating one person of this race. 
	 */
	public int getRecruitmentCosts() {
		return recruit;
	}

  /**
   * Sets the string needed for recruiting orders. 
   */
	public void setRecruitmentName(String name) {
	  this.recruitName = name;
	}
	
  /**
   * Returns the string needed for recruiting orders if applicable, otherwise <code>null</code>. 
   */
  public String getRecruitmentName() {
    return recruitName;
  }

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setWeight(float w) {
		weight = w;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setCapacity(float c) {
		capacity = c;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public float getCapacity() {
		return capacity;
	}

	/**
	 * Returns the bonus this race has on the specified skill.
	 *
	 * 
	 *
	 * @return the bonus for the specified skill or 0, if no bonus-information is available for
	 * 		   this skill.
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
	 * Sets the bonus this race has on the specified skill.
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
	 * Returns the bonus this race has in certain region terrains.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public int getSkillBonus(SkillType skillType, RegionType regionType) {
		int bonus = 0;

		if(skillRegionBonuses != null) {
			Map<ID,Integer> m = skillRegionBonuses.get(regionType.getID());

			if(m != null) {
				Integer i = m.get(skillType.getID());

				if(i != null) {
					bonus = i.intValue();
				}
			}
		}

		return bonus;
	}

	/**
	 * Sets the bonus this race has in certain region terrains.
	 *
	 * 
	 * 
	 * 
	 */
	public void setSkillBonus(SkillType skillType, RegionType regionType, int bonus) {
		if(skillRegionBonuses == null) {
      skillRegionBonuses = new Hashtable<ID, Map<ID,Integer>>();
		}

		Map<ID,Integer> m = skillRegionBonuses.get(regionType.getID());

		if(m == null) {
			m = new Hashtable<ID, Integer>();
      skillRegionBonuses.put(regionType.getID(), m);
		}

		m.put(skillType.getID(), bonus);
	}
	
	/**
	 * Returns the bonus that is added to the ship radius for this race. 
	 *
	 * @return The bonus 
	 */
	public int getAdditiveShipBonus() {
		return additiveShipBonus;
	}

	/**
 	 * Returns the bonus (or malus) that is added to the ship radius for this race. 
	 *
	 * @param bon
	 */
	public void setAdditiveShipBonus(int bon) {
		additiveShipBonus = bon;
	}

}
