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

package magellan.library.utils;

/**
 * An interface to be implemented by all classes that want to be notified about cache clean-up.
 */
public interface CacheHandler {
  /**
   * Called if a cache is cleared.
   * 
   * @param c The cache that is going to be cleared.
   */
  public void clearCache(Cache c);
}
