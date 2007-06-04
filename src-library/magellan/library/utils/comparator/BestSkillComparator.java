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
import java.util.Iterator;
import java.util.Map;

import magellan.library.Skill;


/**
 * A comparator imposing an ordering on collections of Skill objects by comparing the best skills
 * available in each set.
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * 
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality.
 * </p>
 */
public class BestSkillComparator implements Comparator {
	private final Comparator bestCmp;
	private final Comparator skillCmp;
	private final Comparator subCmp;

	/**
	 * Creates a new BestSkillComparator object.
	 *
	 * @param bestComparator used to determine the best skill in each of the two collections of
	 * 		  skills to be compared.
	 * @param skillComparator used to compare the two best skills.
	 * @param subComparator applied when the best skills are equal or cannot be determined.
	 */
	public BestSkillComparator(Comparator bestComparator, Comparator skillComparator,
							   Comparator subComparator) {
		this.bestCmp = bestComparator;
		this.skillCmp = skillComparator;
		this.subCmp = subComparator;
	}

	/**
	 * Compares its two arguments for order according to their skills.
	 *
	 * 
	 * 
	 *
	 * @return the result of the skill comparator applied to the - according to the best comparator
	 * 		   - smallest skills in o1 and o2.
	 */
	public int compare(Object o1, Object o2) {
		int retVal = 0;
		Skill s1 = getBestSkill((Map) o1);
		Skill s2 = getBestSkill((Map) o2);

		if((s1 == null) && (s2 != null)) {
			retVal = Integer.MIN_VALUE;
		} else if((s1 != null) && (s2 == null)) {
			retVal = Integer.MAX_VALUE;
		} else if((s1 == null) && (s2 == null)) {
			if(subCmp != null) {
				retVal = subCmp.compare(o1, o2);
			} else {
				retVal = 0;
			}
		} else {
			retVal = skillCmp.compare(s1, s2);

			if((retVal == 0) && (subCmp != null)) {
				retVal = subCmp.compare(o1, o2);
			}
		}

		return retVal;
	}

	private Skill getBestSkill(Map skills) {
		if((skills == null) || (skills.size() == 0)) {
			return null;
		}

		Iterator iter = skills.values().iterator();
		Skill bestSkill = (Skill) iter.next();

		if(skills.size() > 1) {
			while(iter.hasNext()) {
				Skill curSkill = (Skill) iter.next();

				if(bestCmp.compare(curSkill, bestSkill) < 0) {
					bestSkill = curSkill;
				}
			}
		}

		return bestSkill;
	}

}
