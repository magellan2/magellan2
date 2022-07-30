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

package magellan.library.utils;

import java.io.File;
import java.util.Properties;

/**
 * A NameGenerator that takes names from a file.
 * 
 * @author Andreas
 * @version 1.0
 */
public class NameFileNameGenerator extends AbstractNameGenerator implements NameGenerator {

  /**
   * Initialize and load list of files, if defined.
   * 
   * @param settings The client settings
   * @param settingsDir The directory where a configuration file can be stored
   */
  public NameFileNameGenerator(Properties settings, File settingsDir) {
    super(settings, settingsDir);
  }

}
