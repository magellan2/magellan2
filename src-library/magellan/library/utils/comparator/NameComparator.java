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

import magellan.library.Named;

/**
 * A comparator imposing an ordering on named objects by comparing their names alphabetically.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared objects have the
 * same name and they would be regarded as equal by this comparator, instead of 0 the result of the
 * sub-comparator's comparison is returned.
 * </p>
 */
public class NameComparator implements Comparator<Named> {
  protected Comparator<? super Named> sameNameSubCmp = null;

  /** The default NameComparator without a subComparator */
  public static final Comparator<Named> DEFAULT = new NameComparator(null);

  /**
   * Creates a new NameComparator object.
   * 
   * @param sameNameSubComparator if two objects with the same name are compared, this
   *          sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public NameComparator(Comparator<? super Named> sameNameSubComparator) {
    sameNameSubCmp = sameNameSubComparator;
  }

  /**
   * Compares its two arguments for order according to their names.
   * 
   * @param o1 an instance of interface Named.
   * @param o2 an instance of interface Named.
   * @return the lexical difference of <kbd>o1</kbd>'s and <kbd>o2</kbd>'s names as returned by
   *         String.compareTo(). If the names are equal and a sub-comparator was specified, the
   *         result of that sub-comparator's comparison is returned. Undefined values are evaluated
   *         as <code>&gt; 0</code>.
   */
  public int compare(Named o1, Named o2) {
    String n1 = o1.getName();
    String n2 = o2.getName();

    int retVal = 0;
    if (n1 != null) {
      retVal = n2 == null ? -1 : n1.compareToIgnoreCase(n2);
    } else {
      // n1 == null
      retVal = n2 == null ? 0 : 1;
    }

    return retVal == 0 && (sameNameSubCmp != null) ? sameNameSubCmp.compare(o1, o2) : retVal;
  }
}
