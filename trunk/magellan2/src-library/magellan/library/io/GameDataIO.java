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

package magellan.library.io;

import java.io.IOException;
import java.io.Reader;

import magellan.library.GameData;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface GameDataIO {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public GameData read(Reader in, GameData world) throws IOException;
}
