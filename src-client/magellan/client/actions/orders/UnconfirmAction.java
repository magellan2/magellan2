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
 * @version 1.0
 */
public class UnconfirmAction extends MenuAction {
	private EMapOverviewPanel target;

	/**
	 * Creates a new UnconfirmAction object.
	 *
	 * 
	 * 
	 */
	public UnconfirmAction(Client client, EMapOverviewPanel e) {
        super(client);
		target = e;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		target.shortCut_N();
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.unconfirmaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.unconfirmaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.unconfirmaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.unconfirmaction.tooltip",false);
  }
}
