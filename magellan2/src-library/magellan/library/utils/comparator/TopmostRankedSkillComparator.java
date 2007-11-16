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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.Skill;


/**
 * A comparator imposing an ordering on collections of Skill objects by comparing the highest
 * ranked skill available in each set with a SkillComparator. In case of equality the second
 * highest ranked skills are compared and so on and so on. In case of total equality (e.g. if
 * there is only one skill oject in both maps and the skilltype and value is the same) the
 * sub-comparator is used for comparison. Note: Skilltype rankings can be defined in the
 * preferences and are available through SkillTypeRankComparator.
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
public class TopmostRankedSkillComparator<E> implements Comparator<Map> {
	private Comparator<Object> rankCmp;
	private Comparator<E> subCmp;

	/**
	 * Creates a new TopmostRankedSkillComparator object.
	 *
	 * 
	 * 
	 */
	public TopmostRankedSkillComparator(Comparator<E> subComparator, Properties settings) {
		rankCmp = new SkillTypeRankComparator<E>(null, settings);
		this.subCmp = subComparator;
	}

	/**
	 * Compares its two arguments for order according to their skills.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public int compare(Map o1, Map o2) {
		int retVal = 0;
		int rank = 0;
		Map map1 = o1;
		Map map2 = o2;

		// sort maps according to skill type ranking
		List list1 = new LinkedList(map1.values());
		List list2 = new LinkedList(map2.values());
		Collections.sort(list1, rankCmp);
		Collections.sort(list2, rankCmp);

		while(true) {
			Skill s1 = null;

			if(list1.size() > rank) {
				s1 = (Skill) list1.get(rank);
			}

			Skill s2 = null;

			if(list2.size() > rank) {
				s2 = (Skill) list2.get(rank);
			}

			if((s1 == null) && (s2 != null)) {
				return Integer.MAX_VALUE;
			}

			if((s1 != null) && (s2 == null)) {
				return Integer.MIN_VALUE;
			}

			if((s1 == null) && (s2 == null)) {
				if(subCmp != null) {
					return subCmp.compare((E)o1, (E)o2);
				} else {
					return 0;
				}
			}

			retVal = rankCmp.compare(s1.getSkillType(), s2.getSkillType());

			if(retVal != 0) {
				return retVal;
			} else {
				retVal = SkillComparator.skillCmp.compare(s1, s2);

				if(retVal != 0) {
					return retVal;
				} else {
					// test if there are more skills available in both sets
					if((map1.size() > (rank + 1)) || (map2.size() > (rank + 1))) {
						rank++;
					} else {
						if(subCmp != null) {
							return subCmp.compare((E)s1, (E)s2);
						} else {
							return 0;
						}
					}
				}
			}
		}
	}

}
