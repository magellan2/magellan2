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
 * ContextChangeable.java
 *
 * Created on 1. März 2002, 15:32
 */
package magellan.client.swing.context;

import javax.swing.JMenuItem;

/**
 * Marks renderers that should be included in a context menu.
 * 
 * @author Andreas
 * @version 1.0
 */
public interface ContextChangeable {
  /**
   * Returns an item for this renderer.
   */
  public JMenuItem getContextAdapter();

  /**
   * Sets an observer that should be notified if the state of this renderer changes.
   */
  public void setContextObserver(ContextObserver co);
}
