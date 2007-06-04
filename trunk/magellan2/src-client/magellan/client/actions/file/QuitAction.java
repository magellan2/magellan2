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

package magellan.client.actions.file;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class QuitAction extends MenuAction {

	/**
	 * Creates new OpenCRAction
	 *
	 * @param client
	 */
	public QuitAction(Client client) {
        super(client);
	}

	/**
	 * Called when the file->open menu is selected in order to open a certain cr file. Displays a
	 * file chooser and loads the selected cr file.
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		client.quit(true);
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("name", "Exit");
			defaultTranslations.put("mnemonic", "x");
			defaultTranslations.put("accelerator", "");
			defaultTranslations.put("tooltip", "");
		}

		return defaultTranslations;
	}
  

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("magellan.actions.quitaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("magellan.actions.quitaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("magellan.actions.quitaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("magellan.actions.quitaction.tooltip",false);
  }

}
