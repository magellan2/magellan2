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

package magellan.client.actions.edit;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.FindDialog;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class FindAction extends MenuAction implements SelectionListener, GameDataListener {
	private Collection<Region> selectedRegions = new LinkedList<Region>();

	/**
	 * Creates a new FindAction object.
	 *
	 * @param client
	 */
	public FindAction(Client client) {
    super(client);
    client.getDispatcher().addGameDataListener(this);
		client.getDispatcher().addSelectionListener(this);
	}

	/**
	 * 
	 */
	@Override
  public void menuActionPerformed(ActionEvent e) {
		FindDialog f = new FindDialog(client, false, client.getDispatcher(), client.getData(), client.getProperties(), selectedRegions);
		f.setVisible(true);
	}

	/**
	 * 
	 */
	public void selectionChanged(SelectionEvent s) {
		if(s.getSelectionType() == SelectionEvent.ST_REGIONS) {
			selectedRegions.clear();

			if(s.getSelectedObjects() != null) {
				s.addSelectedObjects(selectedRegions, Region.class);
			}
		}
	}

	/**
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		selectedRegions.clear();
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.findaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.findaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.findaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.findaction.tooltip",false);
  }

}
