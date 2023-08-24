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

package magellan.library.utils.comparator.tree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This comparator glues two comparators together.
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared units belong to
 * the same faction and they would be regarded as equal by this comparator, instead of 0 the result
 * of the sub-comparator's comparison is returned.
 * </p>
 */
public class GroupingComparator<T> implements Comparator<T> {

  protected Comparator<? super T> main = null;
  protected GroupingComparator<? super T> sub = null;

  /**
   * Creates a new <kbd>GroupingComparator</kbd> object.
   * 
   * @param mainComparator the comparator used to compare the given objects
   * @param subComparator the comparator used to compare the given objects if mainComparator
   *          delivers 0.
   */
  public GroupingComparator(Comparator<? super T> mainComparator,
      GroupingComparator<? super T> subComparator) {
    if (mainComparator == null)
      throw new NullPointerException();
    main = mainComparator;
    sub = subComparator;
  }

  public GroupingComparator(Comparator<? super T> mainComparator,
      Comparator<? super T> subComparator) {
    this(mainComparator, new GroupingComparator<T>(subComparator, null));
  }

  /**
   * Compares its two arguments. Also it returns powers of 2 to return the depth of the underlying
   * comparators
   */
  public int compare(T o1, T o2) {
    int ret = main.compare(o1, o2);
    return ret == 0 && sub != null ? sub.compare(o1, o2) : ret;

  }

  public static <S> GroupingComparator<S> buildFromList(Comparator<S>[] comparators) {
    return GroupingComparator.buildFromList(Arrays.asList(comparators));
  }

  private static <S> GroupingComparator<S> buildFromList(List<Comparator<S>> comparators) {
    if (comparators == null || comparators.isEmpty())
      return null;

    return new GroupingComparator<S>(comparators.remove(0), GroupingComparator
        .buildFromList(comparators));
  }
}
