/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.io.json;

import java.io.IOException;
import java.io.Reader;

import magellan.library.GameData;
import magellan.library.Rules;
import magellan.library.io.GameDataIO;
import magellan.library.io.ReportParser;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * Parser for nr-files.
 */
public class JSONParser implements RulesIO, GameDataIO, ReportParser { // extends
                                                                       // AbstractReportParser
  protected static final Logger log = Logger.getInstance(JSONParser.class);
  private UserInterface ui;
  private ReportTransformer transformer;

  /**
   * Creates a new parser.
   * 
   * @param ui The UserInterface for the progress. Can be NULL. Then no operation is displayed.
   */
  public JSONParser(UserInterface ui) {
    this(ui, new IdentityTransformer());
  }

  /**
   * Creates a new parser. This new parser translates coordinates according to newOrigin. All
   * coordinates which are read from the report are translated by newOrigin. That is, if a
   * coordinate read and its level (the z coordinate) equals the new origins level, its x and y
   * coordinates are decreased by origin.x and origin.y, respectively. That means, that the reports
   * origin is transferred to newOrigin.
   * 
   * @param translator The coordinates (relative to the origin of the report) of the new origin.
   */
  public JSONParser(UserInterface ui, ReportTransformer translator) {
    if (ui == null) {
      this.ui = new NullUserInterface();
    } else {
      this.ui = ui;
    }
    transformer = translator;

  }

  public GameData read(Reader in, GameData world) throws IOException {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

  public GameData read(FileType aFileType, Rules rules) throws IOException {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

  public Rules readRules(FileType filetype) throws IOException {
    log.error("something went wrong, there are no JSON rule files.");
    return null;
  }

  public void setUI(UserInterface ui) {
    this.ui = ui;
  }

  public void setTransformer(ReportTransformer coordinateTransformer) {
    transformer = coordinateTransformer;
  }

}
