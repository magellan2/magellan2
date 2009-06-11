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

package magellan.library.gamebinding.e3a;

import magellan.library.gamebinding.EresseaRelationFactory;


/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class E3ARelationFactory extends EresseaRelationFactory {
  private static final E3ARelationFactory singleton = new E3ARelationFactory();

  protected E3ARelationFactory() {
  }


  /**
   * 
   */
  public static E3ARelationFactory getSingleton() {
    return E3ARelationFactory.singleton;
  }
}
