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

package magellan.library.utils.replacers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ReplacerEnvironment {
  /** DOCUMENT-ME */
  public static final String OPERATION_PART = "Op";

  /** DOCUMENT-ME */
  public static final String UNITSELECTION_PART = "Unit";
  private Map<String, EnvironmentPart> parts;

  /**
   * Creates new ReplacerEnvironment
   */
  public ReplacerEnvironment() {
    parts = new HashMap<String, EnvironmentPart>();

    // put some default parts
    parts.put(ReplacerEnvironment.OPERATION_PART, new OperationMode());
    parts.put(ReplacerEnvironment.UNITSELECTION_PART, new UnitSelection());
  }

  /**
   * DOCUMENT-ME
   */
  public EnvironmentPart getPart(String part) {
    return parts.get(part);
  }

  /**
   * DOCUMENT-ME
   */
  public void setPart(String name, EnvironmentPart part) {
    parts.put(name, part);
  }

  /**
   * DOCUMENT-ME
   */
  public void reset() {
    Iterator<EnvironmentPart> it = parts.values().iterator();

    while (it.hasNext()) {
      it.next().reset();
    }
  }
}
