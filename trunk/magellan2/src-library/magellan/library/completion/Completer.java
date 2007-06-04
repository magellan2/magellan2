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

package magellan.library.completion;

import java.util.List;

import magellan.library.Unit;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public interface Completer {
	/**
	 * DOCUMENT-ME
	 */
	public List<Completion> getCompletions(Unit u, String line, List<Completion> old);
}
