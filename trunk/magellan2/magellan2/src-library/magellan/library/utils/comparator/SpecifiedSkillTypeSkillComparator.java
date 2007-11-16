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
import java.util.Map;

import magellan.library.ID;
import magellan.library.Skill;
import magellan.library.rules.SkillType;


/**
 * A comparator imposing an ordering on collections of Skill objects by comparing the skills of the
 * given SkillType available in each set.
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
public class SpecifiedSkillTypeSkillComparator implements Comparator {
	private final Comparator skillCmp;
	private final Comparator subCmp;
	private ID skillTypeID;

	/**
	 * Creates a new BestSkillComparator object.
	 *
	 * @param skillType used to determine the best skill in each of the two collections of
	 * 		  skills to be compared.
	 * @param skillComparator used to compare the two best skills.
	 * @param subComparator applied when the best skills are equal or cannot be determined.
	 */
	public SpecifiedSkillTypeSkillComparator(SkillType skillType, Comparator skillComparator,
											 Comparator subComparator) {
		this.skillTypeID = skillType.getID();
		this.skillCmp = skillComparator;
		this.subCmp = subComparator;
	}

	/**
	 * Compares its two arguments for order according to their skills.
	 *
	 * 
	 * 
	 *
	 * @return the result of the skill comparator applied to the - according to the given skilltype
	 * 		   - smallest skills in o1 and o2.
	 */
	public int compare(Object o1, Object o2) {
		int retVal = 0;
		Skill s1 = (Skill) ((Map) o1).get(skillTypeID);
		Skill s2 = (Skill) ((Map) o2).get(skillTypeID);

		if((s1 == null) && (s2 != null)) {
			retVal = Integer.MIN_VALUE;
		} else if((s1 != null) && (s2 == null)) {
			retVal = Integer.MAX_VALUE;
		} else if((s1 == null) && (s2 == null)) {
			if(subCmp != null) {
				retVal = subCmp.compare(o1, o2);
			}
		} else {
			retVal = skillCmp.compare(s1, s2);

			if((retVal == 0) && (subCmp != null)) {
				retVal = subCmp.compare(o1, o2);
			}
		}

		return retVal;
	}
}
