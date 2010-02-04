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

package magellan.client;

import java.util.Properties;

import magellan.client.event.EventDispatcher;

/**
 * This class keeps all anchors to global resources e.g. EventDispatcher, Properties, perhaps
 * different stuff<br>
 */
public interface MagellanEnvironment {

  /**
   * Returns the properties of Magellan.
   */
  public Properties getProperties();

  /**
   * Returns the EventDispatcher of Magellan.
   */
  public EventDispatcher getEventDispatcher();

  /**
   * Initializes global resources.
   */
  public void init();
}
