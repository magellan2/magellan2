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

import java.io.File;
import java.io.IOException;

import magellan.library.Rules;
import magellan.library.io.cr.CRParser;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.rules.GenericRules;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class RulesReader {
  private static final Logger log = Logger.getInstance(RulesReader.class);

  /**
   * Creates a new RulesReader object.
   */
  public RulesReader() {
    // TODO if this class has no state, why isn't it static or singleton?
  }

  /**
   * Reads the rules of the given gamedata. Right now it first tries to read it from an xml. If this
   * fails it possibly reads the cr.
   * 
   * @return The rules for the given name. If all else fails, an empty rules object is returned.
   */
  public Rules readRules(String name) {
    try {
      return loadRules(name);
    } catch (IOException e) {
      RulesReader.log.warn("The ruleset " + name + " could not be found, falling back to Eressea.");
      try {
        return loadRules("eressea");
      } catch (IOException e2) {
        /* This is bad. We don't even have the default rules. */
        RulesReader.log.error(
            "The default ruleset couldn't be found! Operating with an empty ruleset.", e2);
      }
    }
    return new GenericRules();
  }

  /**
   * Looks for the ruleset in etc/rules and reads it.
   * 
   * @throws IOException If an I/O error occurs
   */
  private Rules loadRules(String name) throws IOException {
    String ending = new File("XML").exists() ? ".xml" : ".cr";

    if (name != null) {
      name = name.toLowerCase();
    }
    ending = ending.toLowerCase();

    RulesReader.log.fine("loading rules for \"" + name + "\" (ending: " + ending + ")");

    File ruleFile = new File(Resources.getResourceDirectory(), "etc/rules/" + name + ending);
    // workaround for working with eclipse...
    if (!ruleFile.exists()) {
      RulesReader.log.warn("Rule file '" + ruleFile.getAbsolutePath()
          + "' could not be found. Switching to local.");
      ruleFile = new File("etc/rules/" + name + ending);

      if (!ruleFile.exists()) {
        RulesReader.log.error("Cannot find rule files in '" + ruleFile.getAbsolutePath()
            + "'...that might be a problem...");
      }
    }

    FileType filetype = FileTypeFactory.singleton().createInputStreamSourceFileType(ruleFile);

    Rules rules = new CRParser(null).readRules(filetype);
    rules.setGameName(name);
    return rules;
  }
}
