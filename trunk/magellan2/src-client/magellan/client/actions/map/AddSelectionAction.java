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

import java.util.Iterator;

import magellan.client.Client;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.Region;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ilja Pavkovic
 */
public class AddSelectionAction extends OpenSelectionAction implements SelectionListener {
	/**
	 * Creates a new AddSelectionAction object.
	 *
	 * 
	 */
	public AddSelectionAction(Client client) {
		super(client);
		client.getDispatcher().addSelectionListener(this);
	}

	@Override
  protected void preSetCleanSelection() {
		// adding does not clean selectedRegion
		// System.out.println("do not clean selection");
	}

	@Override
  protected String getPropertyName() {
		return "Client.lastSELAdded";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent e) {
		if(e.getSource() == this) {
			return;
		}

		if((e.getSelectedObjects() != null) && (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
			selectedRegions.clear();

			for(Iterator iter = e.getSelectedObjects().iterator(); iter.hasNext();) {
				Object o = iter.next();

				if(o instanceof Region) {
					Region r = (Region) o;
					selectedRegions.put(r.getID(), r);
				}
			}
		}
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.addselectionaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.addselectionaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.addselectionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.addselectionaction.tooltip");
  }
}
