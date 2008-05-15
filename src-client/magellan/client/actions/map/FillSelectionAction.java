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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster
 */
public class FillSelectionAction extends MenuAction implements SelectionListener, GameDataListener {
	private Map<CoordinateID,Region> selectedRegions = new Hashtable<CoordinateID, Region>();

	/**
	 * Creates a new FillSelectionAction object.
	 *
	 * @param client
	 */
	public FillSelectionAction(Client client) {
        super(client);
		client.getDispatcher().addSelectionListener(this);
		client.getDispatcher().addGameDataListener(this);
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
					selectedRegions.put((CoordinateID)r.getID(), r);
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		selectedRegions.clear();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(java.awt.event.ActionEvent e) {
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		for(Iterator iter = selectedRegions.keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();

			if(c.z == client.getLevel()) {
				if(c.x > maxX) {
					maxX = c.x;
				}

				if(c.y > maxY) {
					maxY = c.y;
				}

				if(c.x < minX) {
					minX = c.x;
				}

				if(c.y < minY) {
					minY = c.y;
				}
			}
		}

		for(Iterator iter = client.getData().regions().keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();

			if((c.z == client.getLevel()) && (c.x <= maxX) && (c.x >= minX) && (c.y <= maxY) &&
				   (c.y >= minY)) {
				selectedRegions.put(c, client.getData().regions().get(c));
			}
		}

		client.getData().setSelectedRegionCoordinates(selectedRegions);
		client.getDispatcher().fire(new SelectionEvent<Region>(this, selectedRegions.values(), null,
													   SelectionEvent.ST_REGIONS));
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.fillsaveasaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.fillselectionaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.fillselectionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.fillselectionaction.tooltip",false);
  }

}
