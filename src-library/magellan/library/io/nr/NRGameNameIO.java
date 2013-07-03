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

package magellan.library.io.nr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.io.GameNameIO;
import magellan.library.io.file.FileType;
import magellan.library.utils.logging.Logger;

/**
 * Extracts a game name from a nr report.
 * 
 * @author stm
 */
public class NRGameNameIO implements GameNameIO {
  private static final Logger log = Logger.getInstance(NRGameNameIO.class);

  private static final String nameUndef = "undef";

  protected static Pattern eresseaPattern = Pattern.compile("\\s*Report f..?r ([^,]+),.*");
  protected static Pattern atlantisPattern = Pattern.compile("\\s*(.+) Turn Report.*");

  /**
   * Tries to determine the game name from a report. It tries to get it from the "Spiel" tag and
   * returns "Eressea" if no such tag is found.
   * 
   * @param filetype
   * @return A String representing the name of the game or null if the game was not recognized
   * @throws IOException If an I/O error occurs
   */
  public String getGameName(FileType filetype) throws IOException {
    BufferedReader report = new BufferedReader(filetype.createReader());

    try {
      int lnr = 0;
      String line = report.readLine();
      while (line != null && lnr++ < 10) {
        if (line.length() > 0) {
          Matcher matcher = eresseaPattern.matcher(line);
          if (matcher.matches()) {
            log.warn("Eressea NR not implemented");
            return null;
            // return matcher.group(1);
          } else {
            matcher = atlantisPattern.matcher(line);
          }
          if (matcher.matches())
            return matcher.group(1);
        }
        line = report.readLine();
      }
    } catch (IOException e) {
      NRGameNameIO.log.error("Loader.getGameName(): unable to determine game's name of report "
          + report, e);
    } finally {
      report.close();
    }

    NRGameNameIO.log
        .warn("report does not appear to be a valid Atlantis type human readable report.");
    return null;
  }
}
