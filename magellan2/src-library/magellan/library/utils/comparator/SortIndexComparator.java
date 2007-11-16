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

import magellan.library.utils.Sorted;


/**
 * A comparator imposing an ordering on objects implementing the Sorted interface by comparing
 * their sorting indices.
 * 
 * <p>
 * Note: this comparator can impose orderings that are inconsistent with equals.
 * </p>
 * 
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared objects have the
 * same sorting index and they would be regarded as equal by this comparator, instead of 0 the
 * result of the sub-comparator's comparison is returned.
 * </p>
 */
public class SortIndexComparator<E> implements Comparator<Sorted> {
	protected Comparator<E> sameIndexSubCmp = null;

	/**
	 * Creates a new SortIndexComparator object.
	 *
	 * @param sameIndexSubComparator if two objects with the same sort index are compared, the
	 * 		  given sub-comparator is applied (if not <tt>null</tt>).
	 */
	public SortIndexComparator(Comparator<E> sameIndexSubComparator) {
		sameIndexSubCmp = sameIndexSubComparator;
	}

	/**
	 * Compares its two arguments for order according to their sort indices.
	 *
	 * @return the numerical difference of <tt>o1</tt>'s and <tt>o2</tt>'s sort indices. If the
	 * 		   sort indices are equal and a sub-comparator was specified, the result of that
	 * 		   sub-comparator's comparison is returned.
	 */
	public int compare(Sorted o1, Sorted o2) {
		int s1 = o1.getSortIndex();
		int s2 = o2.getSortIndex();

		if((s1 == s2) && (sameIndexSubCmp != null)) {
			return sameIndexSubCmp.compare((E)o1, (E)o2);
		} else {
			return s1 < s2 ? -1 : 1;
		}
	}

}
