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

import magellan.library.ID;
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
public class TopmostRankedSkillComparator implements Comparator<Map<ID, Skill> > {
	private Comparator<Skill> rankCmp;
	private Comparator<? super Map<ID, Skill>> subCmp;

	/**
	 * Creates a new TopmostRankedSkillComparator object.
	 *
	 * 
	 * 
	 */
	public TopmostRankedSkillComparator(Comparator<? super Map<ID, Skill> > subComparator, Properties settings) {
		rankCmp = new SkillRankComparator(null, settings);
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
	public int compare(Map<ID, Skill> o1, Map<ID, Skill> o2) {
		int retVal = 0;
		int rank = 0;
		Map<ID, Skill> map1 = o1;
		Map<ID, Skill> map2 = o2;

		// sort maps according to skill type ranking
		List<Skill> list1 = new LinkedList<Skill>(map1.values());
		List<Skill> list2 = new LinkedList<Skill>(map2.values());
		Collections.sort(list1, rankCmp);
		Collections.sort(list2, rankCmp);

		while(true) {
			Skill s1 = null;

			if(list1.size() > rank) {
				s1 = list1.get(rank);
			}

			Skill s2 = null;

			if(list2.size() > rank) {
				s2 = list2.get(rank);
			}

			if((s1 == null) && (s2 != null)) {
				return Integer.MAX_VALUE;
			}

			if((s1 != null) && (s2 == null)) {
				return Integer.MIN_VALUE;
			}

			if((s1 == null) && (s2 == null)) {
				if(subCmp != null) {
					return subCmp.compare(o1, o2);
				} else {
					return 0;
				}
			}

			retVal = rankCmp.compare(s1, s2);

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
							return subCmp.compare(o1, o2);
						} else {
							return 0;
						}
					}
				}
			}
		}
	}

}
