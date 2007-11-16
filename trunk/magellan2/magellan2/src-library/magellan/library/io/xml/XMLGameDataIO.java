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

package magellan.library.io.xml;

import java.io.IOException;
import java.io.Reader;

import magellan.library.GameData;
import magellan.library.io.GameDataIO;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class XMLGameDataIO implements GameDataIO {
	/** DOCUMENT-ME */
	public static final Logger log = Logger.getInstance(XMLGameDataIO.class);

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
	public GameData read(Reader in, GameData world) throws IOException {
		throw new IOException("Implementation incomplete");

		// c) use corresponding gamebinding object (or eressea gamebinding object if 
		//    no special implementation found) to read the cr/xml
		// 
		//return new XMLGameDataReader(file).readGameData();
	}
}
