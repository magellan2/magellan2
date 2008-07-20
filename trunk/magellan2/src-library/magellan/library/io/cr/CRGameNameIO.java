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

package magellan.library.io.cr;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import magellan.library.io.GameNameIO;
import magellan.library.io.file.FileType;
import magellan.library.utils.logging.Logger;


/**
 * The purpose of this class is to try to read the game name from a cr report file.
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class CRGameNameIO implements GameNameIO {
	private static final Logger log = Logger.getInstance(CRGameNameIO.class);


	/**
	 * Tries to determine the game name from a report.
	 * 
	 * It tries to get it from the "Spiel" tag and returns "Eressea" if no such tag is found.
	 *
	 * @param filetype 
	 *
	 * @return A String representing the name of the game.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public String getGameName(FileType filetype) throws IOException {
		Reader report = filetype.createReader();

		try {
			Map headerMap = (new CRParser(null)).readHeader(report);

			if(headerMap.containsKey("Spiel")) {
				return (String) headerMap.get("Spiel");
			}
		} catch(IOException e) {
			CRGameNameIO.log.error("Loader.getGameName(): unable to determine game's name of report " + report, e);
		} finally {
			report.close();
		}

		CRGameNameIO.log.warn("Loader.getGameName(): report header does not contain 'Spiel' tag!");

		return "Eressea";
	}
}
