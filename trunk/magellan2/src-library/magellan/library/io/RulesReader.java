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
import magellan.library.utils.PropertiesHelper;
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
	}

	/**
	 * Reads the rules of the given gamedata. Right now it first tries to read it from an xml. If
	 * this fails it  possibly reads the cr
	 */
	public Rules readRules(String name) throws IOException {
		try {
			return loadRules(name);
		} catch(IOException e) {
			/* The desired rule file doesn't exist. Fallback to default, if
			   we haven't tried that yet. */
			if(name.equalsIgnoreCase("eressea")) {
				/* This is bad. We don't even have the default rules. */
				RulesReader.log.warn("The default ruleset couldn't be found! Operating with an empty ruleset.",e);

				return new GenericRules();
			} else {
				return readRules("eressea");
			}
		}
	}

	/**
	 *
	 */
	private Rules loadRules(String name) throws IOException {
		String ending = new File("XML").exists() ? ".xml" : ".cr";
    RulesReader.log.debug("loading rules for \"" + name + "\" (ending: " + ending + ")");
    
    File rules = new File(PropertiesHelper.getSettingsDirectory(),"etc/rules/" + name + ending);
    // workaround for working with eclipse...
    if (!rules.exists()) {
      rules = new File("etc/rules/" + name + ending);
    }

		FileType filetype = FileTypeFactory.singleton().createInputStreamSourceFileType(rules);

		return new CRParser(null).readRules(filetype);
	}
}
