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

package magellan.library.io.json;

import java.io.IOException;

import magellan.library.io.GameNameIO;
import magellan.library.io.GameNameReader;
import magellan.library.io.file.FileType;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 260 $
 */
public class JSONGameNameIO implements GameNameIO {
  private static final Logger log = Logger.getInstance(GameNameReader.class);

  public JSONGameNameIO() {
    // TODO IMPLEMENT-ME
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  public String getGameName(FileType filetype) throws IOException {
    // TODO try to detect if the file is a JSON report and return game name
    log.warn("JSON support not implemented.");
    return null;
  }
}
