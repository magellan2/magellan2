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

package magellan.client.actions.orders;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;

import magellan.client.Client;
import magellan.client.EMapOverviewPanel;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class FindPreviousUnconfirmedAction extends MenuAction {
	private EMapOverviewPanel target;

	/**
	 * Creates a new FindPreviousUnconfirmedAction object.
	 *
	 * 
	 * 
	 */
	public FindPreviousUnconfirmedAction(Client client, EMapOverviewPanel e) {
        super(client);
		target = e;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		target.shortCut_Reverse_N();
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
			defaultTranslations.put("name", "Previous unconfirmed");
			defaultTranslations.put("mnemonic", "p");
			defaultTranslations.put("accelerator", "ctrl shift N");
			defaultTranslations.put("tooltip", "");
		}

		return defaultTranslations;
	}
  

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("magellan.actions.findpreviousunconfirmedaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("magellan.actions.findpreviousunconfirmedaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("magellan.actions.findpreviousunconfirmedaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("magellan.actions.findpreviousunconfirmedaction.tooltip",false);
  }
}
