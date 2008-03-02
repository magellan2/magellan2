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

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.SetOriginDialog;
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
