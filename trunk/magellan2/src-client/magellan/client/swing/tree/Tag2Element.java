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

/*
 * ColorAwareCellObject.java
 *
 * Created on 30. August 2001, 16:54
 */
package magellan.client.swing.tree;

import magellan.library.Group;
import magellan.library.Unit;
import magellan.library.UnitContainer;

/**
 * This class provides helper function to remember a style and apply a style to various
 * GraphicsElements.
 * 
 * @author Andreas
 * @version 1.0
 */
public class Tag2Element {
  protected static String lastStyle = null;
  protected static final String STYLE_TAG = "magStyle";

  /**
   * remembers the style tag of the given unit
   * 
   * @param u
   */
  public static void start(Unit u) {
    Tag2Element.lastStyle = null;

    if (u.containsTag(Tag2Element.STYLE_TAG)) {
      Tag2Element.lastStyle = u.getTag(Tag2Element.STYLE_TAG);
    }
  }

  /**
   * remembers the style tag of the given UnitContainer
   * 
   * @param u
   */
  public static void start(UnitContainer u) {
    Tag2Element.lastStyle = null;

    if (u.containsTag(Tag2Element.STYLE_TAG)) {
      Tag2Element.lastStyle = u.getTag(Tag2Element.STYLE_TAG);
    }
  }

  /**
   * remembers the style tag of the given Group
   * 
   * @param g
   */
  public static void start(Group g) {
    Tag2Element.lastStyle = null;

    if (g.containsTag(Tag2Element.STYLE_TAG)) {
      Tag2Element.lastStyle = g.getTag(Tag2Element.STYLE_TAG);
    }
  }

  /**
   * apllies the remembered style to the given GraphicsElement
   * 
   * @param ge
   */
  public static void apply(GraphicsElement ge) {
    if ((ge.getStyleset() == null) || (Tag2Element.lastStyle != null)) {
      ge.setStyleset(Tag2Element.lastStyle);
    }
  }
}
