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

import magellan.library.ID;
import magellan.library.Skill;
import magellan.library.rules.SkillType;


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
public class BestSkillComparator<E> implements Comparator<Map<ID,Skill>> {
	private final Comparator<Skill> bestCmp;
	private final Comparator<SkillType> skillTypeCmp;
	private final Comparator<E> subCmp;

	/**
	 * Creates a new BestSkillComparator object.
	 *
	 * @param bestComparator used to determine the best skill in each of the two collections of
	 * 		  skills to be compared.
	 * @param skillComparator used to compare the two best skills.
	 * @param subComparator applied when the best skills are equal or cannot be determined.
	 */
	public BestSkillComparator(Comparator<Skill> bestComparator, Comparator<SkillType> skillTypeComparator,
							   Comparator<E> subComparator) {
		this.bestCmp = bestComparator;
		this.skillTypeCmp = skillTypeComparator;
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
	public int compare(Map<ID,Skill> o1, Map<ID,Skill> o2) {
		int retVal = 0;
		Skill s1 = getBestSkill(o1);
		Skill s2 = getBestSkill(o2);
		
    E e1 = (E)o1;
    E e2 = (E)o2;

		if((s1 == null) && (s2 != null)) {
			retVal = Integer.MIN_VALUE;
		} else if((s1 != null) && (s2 == null)) {
			retVal = Integer.MAX_VALUE;
		} else if((s1 == null) && (s2 == null)) {
			if(subCmp != null) {
				retVal = subCmp.compare(e1, e2);
			} else {
				retVal = 0;
			}
		} else {
			retVal = skillTypeCmp.compare(s1.getSkillType(), s2.getSkillType());

			if((retVal == 0) && (subCmp != null)) {
				retVal = subCmp.compare(e1, e2);
			}
		}

		return retVal;
	}

	private Skill getBestSkill(Map<ID,Skill> skills) {
		if((skills == null) || (skills.size() == 0)) {
			return null;
		}

		Iterator<Skill> iter = skills.values().iterator();
		Skill bestSkill = iter.next();

		if(skills.size() > 1) {
			while(iter.hasNext()) {
				Skill curSkill = iter.next();

				if(bestCmp.compare(curSkill, bestSkill) < 0) {
					bestSkill = curSkill;
				}
			}
		}

		return bestSkill;
	}

}
