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
import magellan.client.swing.MapSaverUI;
import magellan.client.swing.MapperPanel;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class MapSaveAction extends MenuAction {
	private MapperPanel map;

	/**
	 * Creates a new MapSaveAction object.
	 *
	 * @param client
	 * 
	 */
	public MapSaveAction(Client client, MapperPanel m) {
    super(client);
		map = m;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		new MapSaverUI(client, true, map.getView()).setVisible(true);
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.mapsaveaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.mapsaveaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.mapsaveaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.mapsaveaction.tooltip",false);
  }
}
