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

import magellan.client.Client;


/**
 * Adds a previously saved selection file to the current selection.
 *
 * @author Ilja Pavkovic
 */
public class AddSelectionAction extends OpenSelectionAction {

  /**
	 * Creates a new AddSelectionAction object.
	 * 
	 */
	public AddSelectionAction(Client client) {
		super(client);
	}

	/**
	 * Does nothing.
	 * 
	 * @see magellan.client.actions.map.OpenSelectionAction#preSetCleanSelection()
	 */
	@Override
  protected void preSetCleanSelection() {
		// adding does not clean selectedRegion
		// System.out.println("do not clean selection");
	}

	/**
	 * @see magellan.client.actions.map.OpenSelectionAction#getPropertyName()
	 */
	@Override
  protected String getPropertyName() {
		return "Client.lastSELAdded";
	}

}
