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

import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.gamebinding.EresseaOrderCompleter;


/**
 * 
 */
public class E3AOrderCompleter extends EresseaOrderCompleter {

	/**
	 * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
	 * <tt>GameData</tt> object.
	 *
	 * @param gd The <tt>GameData</tt> this completer uses as context.
	 * 
	 */
	public E3AOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
	  super(gd, ac);
	}

  /**
   * @see magellan.library.gamebinding.EresseaOrderCompleter#getCompletions(magellan.library.Unit, java.lang.String, java.util.List)
   * FIXME implement E2K9 subtleties!
   */
  public List<Completion> getCompletions(Unit u, String line, List<Completion> old) {
    return super.getCompletions(u, line, old);
  }

}
