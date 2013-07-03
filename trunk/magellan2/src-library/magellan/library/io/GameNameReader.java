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

import magellan.library.io.cr.CRGameNameIO;
import magellan.library.io.file.FileType;
import magellan.library.io.json.JSONGameNameIO;
import magellan.library.io.nr.NRGameNameIO;
import magellan.library.io.xml.XMLGameNameIO;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class GameNameReader {
  private static final Logger log = Logger.getInstance(GameNameReader.class);

  /**
   * Tries to read the game name from the file.
   * 
   * @param filetype A report or rules file
   * @return The name of the game or <code>null</code> if it couldn't be found.
   */
  public static String getGameName(FileType filetype) {
    try {
      String gameName = new CRGameNameIO().getGameName(filetype);

      if (gameName != null)
        return gameName;

      gameName = new XMLGameNameIO().getGameName(filetype);
      if (gameName != null)
        return gameName;

      gameName = new JSONGameNameIO().getGameName(filetype);
      if (gameName != null)
        return gameName;

      gameName = new NRGameNameIO().getGameName(filetype);
      if (gameName != null)
        return gameName;

      return null;
    } catch (IOException e) {
      GameNameReader.log.error(e);
      return null;
    }
  }
}
