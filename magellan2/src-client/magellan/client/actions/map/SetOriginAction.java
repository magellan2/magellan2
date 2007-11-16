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

package magellan.client.actions.map;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;

import magellan.client.Client;
import magellan.client.SetOriginDialog;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class SetOriginAction extends MenuAction {

	/**
	 * Creates a new SetOriginAction object.
	 *
	 * @param client
	 */
	public SetOriginAction(Client client) {
        super(client);
	}

	/**
	 * Opens the SetOriginDialog, waits for user input
	 * if approved, then setOrigin of class Client is called
	 * sets the new Origin
	 *
	 * @param e 
	 */
	public void menuActionPerformed(ActionEvent e) {
		SetOriginDialog dialog = new SetOriginDialog(client, client.getDispatcher(), client.getData());
		dialog.setVisible(true);
		if (dialog.approved()){
			client.setOrigin(dialog.getNewOrigin());
		}
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
			defaultTranslations.put("name", "Set origin...");
			defaultTranslations.put("mnemonic", "o");
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
    return Resources.get("actions.setoriginaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.setoriginaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.setoriginaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.setoriginaction.tooltip",false);
  }
}
