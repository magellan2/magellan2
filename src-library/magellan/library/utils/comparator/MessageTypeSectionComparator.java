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

import magellan.library.rules.MessageType;

/**
 * A comparator imposing an ordering on MessageType objects by comparing the sections (categories)
 * they belong to.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared messages belong to
 * the same section and they would be regarded as equal by this comparator, instead of 0 the result
 * of the sub-comparator's comparison is returned.
 * </p>
 */
public class MessageTypeSectionComparator implements Comparator<MessageType> {
  protected Comparator<MessageType> sameSectionSubCmp = null;

  /**
   * Creates a new MessageTypeSectionComparator object.
   * 
   * @param sameSectionSubComparator if two messages belonging to the same section are compared,
   *          this sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public MessageTypeSectionComparator(Comparator<MessageType> sameSectionSubComparator) {
    sameSectionSubCmp = sameSectionSubComparator;
  }

  /**
   * Compares its two arguments for order according to the sections they belong to.
   * 
   * @return the result of the String.compareTo() method applied to <kbd>o1</kbd>'s and <kbd>o2</kbd>.
   *         If both belong to the same section and a sub-comparator was specified, the result that
   *         sub-comparator's comparison is returned. Undefined values are evaluated as
   *         <code>&gt; 0</code>.
   */
  public int compare(MessageType o1, MessageType o2) {
    String s1 = o1.getSection();
    String s2 = o2.getSection();

    if (s1 == null)
      return (s2 == null) ? 0 : 1;
    else {
      if (s2 == null)
        return -1;
      else {
        int retVal = s1.compareTo(s2);

        return ((retVal == 0) && (sameSectionSubCmp != null)) ? sameSectionSubCmp.compare(o1, o2)
            : retVal;
      }
    }
  }
}
