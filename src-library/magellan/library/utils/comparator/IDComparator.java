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

import magellan.library.Unique;

/**
 * A comparator imposing a total ordering on identifiable objects by comparing their ids.
 */
public class IDComparator implements Comparator<Unique> {
  private boolean inverse;

  /**
   * Creates a new IDComparator object.
   */
  private IDComparator(boolean inverted) {
    // private -- singleton
    inverse = inverted;
  }

  /** The default IDComparator. We only has extrinsic state so we can use singleton here. */
  public static final Comparator<Unique> DEFAULT = new IDComparator(false);

  /** The default IDComparator, but with inverted order. */
  public static final Comparator<Unique> INVERSE = new IDComparator(true);

  /**
   * Compares its two arguments for order according to their ids.
   * 
   * @return the natural ordering of <kbd>o1</kbd>'s id and <kbd>o2</kbd>'s id.
   */
  public int compare(Unique o1, Unique o2) {
    if (o1.getID().getClass() != o2.getID().getClass())
      throw new AssertionError("comparing different ID types");
    int result = o1.getID().compareTo(o2.getID());
    if (inverse)
      return -result;
    else
      return result;
  }

}
