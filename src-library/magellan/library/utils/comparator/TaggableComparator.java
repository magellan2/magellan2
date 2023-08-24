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

import magellan.library.utils.Taggable;

/**
 * FIXME: Wrong description!!! A comparator imposing an ordering on Unit objects by comparing the
 * factions they belong to.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared units belong to
 * the same faction and they would be regarded as equal by this comparator, instead of 0 the result
 * of the sub-comparator's comparison is returned.
 * </p>
 */
public class TaggableComparator implements Comparator<Taggable> {

  public final static TaggableComparator DEFAULT_COMPARATOR = new TaggableComparator(null);

  protected Comparator<? super Taggable> subCmp = null;
  protected String tagToCompare = null;

  /**
   * Creates a new TagBasedComparatorComparator object with the default tag
   * "ejcTagBasedComparatorTag"
   * 
   * @param subComparator the comparator used to compare the Tagged tags if this one thinks they are
   *          equal
   */
  public TaggableComparator(Comparator<? super Taggable> subComparator) {
    this("ejcTaggableComparator", subComparator);
  }

  /**
   * Creates a new TagBasedComparatorComparator object with the default tag
   * "ejcTagBasedComparatorTag"
   * 
   * @param subComparator the comparator used to compare the Tagged tags if this one thinks they are
   *          equal
   * @param tag to compare the two Tagged objects
   */
  public TaggableComparator(String tag, Comparator<? super Taggable> subComparator) {
    subCmp = subComparator;
    tagToCompare = tag;
  }

  /**
   * Compares its two arguments for order according to the factions they belong to.
   * 
   * @return the result of the faction comparator's comparison of <kbd>o1</kbd>'s and <kbd>o2</kbd>. If
   *         both belong to the same faction and a sub-comparator was specified, the result that
   *         sub-comparator's comparison is returned. Undefined values are evaluated as
   *         <code>&gt; 0</code>.
   */
  public int compare(Taggable o1, Taggable o2) {
    String t1 = o1.getTag(tagToCompare);
    String t2 = o2.getTag(tagToCompare);

    int retVal = 0;
    if (t1 == null) {
      // retVal (t2 == null) ? 0 : t2.compareTo(t1);
      retVal = (t2 == null) ? 0 : 1;
    } else {
      retVal = (t2 == null) ? -1 : t1.compareTo(t2);
    }
    return (retVal == 0 && subCmp != null) ? subCmp.compare(o1, o2) : retVal;
  }
}
