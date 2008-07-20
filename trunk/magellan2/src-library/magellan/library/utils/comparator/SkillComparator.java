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

package magellan.library.utils.comparator;

import java.util.Comparator;

import magellan.library.Skill;


/**
 * A comparator imposing an ordering on Skill objects by comparing the skill level and days.
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 */
public class SkillComparator implements Comparator<Skill> {
	/** A default instance */
	public static final SkillComparator skillCmp = new SkillComparator();

	/**
	 * Creates a new SkillComparator object.
	 */
	public SkillComparator() {
	}

	/**
	 * Compares its two arguments for order according to the skill levels and days.
	 *
	 * 
	 * 
	 *
	 * @return a number &lt; 0 if o1's level is larger than the one of o2 or if both levels are
	 * 		   equal and o1's days is larger than o2's. A number &gt; 0 is returned if o2's level
	 * 		   is larger than o1's or if both levels are equal and o2's days is larger than o1's.
	 * 		   0 is returned if both values are equal.
	 */
	public int compare(Skill s1, Skill s2) {
	  if (s1==null || s2==null){
	    return 0;
	  }
	  
		if((s1.getLevel() != s2.getLevel()) || (s1.noSkillPoints() && s2.noSkillPoints())) {
			return s2.getLevel() - s1.getLevel();
		} else {
			return s2.getPointsPerPerson() - s1.getPointsPerPerson();
		}
	}
}
