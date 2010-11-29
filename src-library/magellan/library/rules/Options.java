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

package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Rules;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 301 $
 */
public class Options {
  private static final Logger log = Logger.getInstance(Options.class);
  private Map<ID, OptionCategory> options = null;
  Rules rules;

  /**
   * Creates a new Options object.
   */
  public Options(Rules rules) {
    this.rules = rules;
    initOptions(rules);
  }

  /**
   * copy constructor
   */
  public Options(Options orig) {
    this(orig.rules);
    setValues(orig.getBitMap());
  }

  /**
   * DOCUMENT-ME
   */
  public int getBitMap() {
    int bitMap = 0;

    for (OptionCategory o : options.values()) {
      if (o.isActive()) {
        bitMap = bitMap | o.getBitMask();
      }
    }

    return bitMap;
  }

  /**
   * DOCUMENT-ME
   */
  public void setValues(int bitMap) {
    // log.info("CR BitMap: " + Integer.toBinaryString(bitMap));
    for (OptionCategory o : options.values()) {
      int test = bitMap & o.getBitMask();
      // log.info("Option: " + o.getName() + " Bitmask:(" + o.getBitMask() + "):" +
      // Integer.toBinaryString(o.getBitMask()));
      // log.info("test: " + test);
      o.setActive(test != 0);
    }

    if (bitMap != getBitMap()) {
      Options.log.info("Options.setValues(): invalid value computed! (" + bitMap + "<>"
          + getBitMap() + ")");
      // log.info("CR BitMap: " + Integer.toBinaryString(bitMap));
      // log.info("calculated BitMap: " + Integer.toBinaryString(getBitMap()));
    }
  }

  /**
   * DOCUMENT-ME
   */
  public Collection<OptionCategory> options() {
    if (options == null) {
      initOptions(rules);
    }

    if (options != null && options.values() != null)
      return Collections.unmodifiableCollection(options.values());
    return Collections.emptyList();
  }

  private void initOptions(Rules rules) {
    options = CollectionFactory.<ID, OptionCategory> createSyncOrderedMap();

    for (Iterator<OptionCategory> iter = rules.getOptionCategoryIterator(); iter.hasNext();) {
      OptionCategory orig = iter.next();
      options.put(orig.getID(), new OptionCategory(orig));
    }
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isActive(ID id) {
    OptionCategory o = options.get(id);

    return (o != null) && o.isActive();
  }

  /**
   * DOCUMENT-ME
   */
  public void setActive(ID id, boolean active) {
    OptionCategory o = options.get(id);

    if (o != null) {
      o.setActive(active);
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    for (Iterator<OptionCategory> iter = options.values().iterator(); iter.hasNext();) {
      OptionCategory o = iter.next();
      sb.append(o.getID() + ": " + o.isActive());

      if (iter.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }
}
