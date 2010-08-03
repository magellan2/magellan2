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
 * ContextObserver.java
 *
 * Created on 1. März 2002, 16:03
 */
package magellan.client.swing.context;

/**
 * Marks a class that should be notified of change by a context menu
 * 
 * @author Andreas
 * @version 1.0
 */
public interface ContextObserver {
  /**
   * Called if the state has changed to notify the observer that it should be updated.
   */
  public void contextDataChanged();
}
