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
import java.util.Hashtable;
import java.util.Properties;

import magellan.library.ID;
import magellan.library.rules.SkillType;

/**
 * A comparator imposing an ordering on SkillType objects by comparing their user modifiable
 * ranking.
 * <p>
 * Note: this comparator can impose orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared objects have the
 * same rank and they would be regarded as equal by this comparator, instead of 0 the result of the
 * sub-comparator's comparison is returned.
 * </p>
 * 
 * @author Ulrich Küster
 */
public class SkillTypeRankComparator implements Comparator<SkillType> {
  private final Comparator<? super SkillType> subCmp;
  private final Properties settings;

  /**
   * To reduces memory consumption the ranks of the various skills are cached. Keys are
   * SkillTypeIDs, values are Integers.
   */
  private Hashtable<ID, Integer> skillRanks = new Hashtable<ID, Integer>();

  /**
   * Creates a new SkillTypeRankComparator object.
   */
  public SkillTypeRankComparator(Comparator<? super SkillType> subComparator, Properties settings) {
    subCmp = subComparator;

    if (settings == null) {
      this.settings = new Properties();
    } else {
      this.settings = settings;
    }
  }

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(SkillType s1, SkillType s2) {

    int retVal = getValue(s1) - getValue(s2);

    if ((retVal == 0) && (subCmp != null)) {
      retVal = subCmp.compare(s1, s2);
    }

    return retVal;
  }

  private int getValue(SkillType s) {
    Integer retVal = skillRanks.get(s.getID());

    if (retVal == null) {
      String prop = settings.getProperty("ClientPreferences.compareValue." + s.getID(), "-1");
      retVal = new Integer(Integer.parseInt(prop));
      skillRanks.put(s.getID(), retVal);
    }

    return retVal.intValue();
  }
}
