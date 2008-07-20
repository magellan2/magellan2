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
import magellan.library.utils.Islands;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster
 */
public class SelectIslandsAction extends MenuAction implements GameDataListener, SelectionListener {
	private Map<CoordinateID,Region> selectedRegions = new Hashtable<CoordinateID,Region>();

	/**
	 * Creates a new SelectIslandsAction object.
	 *
	 * @param client
	 */
	public SelectIslandsAction(Client client) {
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
	@Override
  public void menuActionPerformed(ActionEvent e) {
		Map<CoordinateID,Region> newSelectedRegions = new Hashtable<CoordinateID, Region>();

		// add all regions, that were selected before and don't belong to the active level
		// or that belong to the active level region _and_ that belong to an island
		// that contained at least one selected region before
		for(Iterator iter = selectedRegions.keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();

			if(c.z != client.getLevel()) {
				newSelectedRegions.put(c, client.getData().regions().get(c));
			} else if(!newSelectedRegions.containsKey(c)) {
				newSelectedRegions.putAll(Islands.getIsland(client.getData().rules,
															client.getData().regions(),
															client.getData().regions().get(c)));
			}
		}

		selectedRegions = newSelectedRegions;
		client.getData().setSelectedRegionCoordinates(selectedRegions);
		client.getDispatcher().fire(new SelectionEvent<Region>(this, selectedRegions.values(), null,
													   SelectionEvent.ST_REGIONS));
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.selectislandsaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.selectislandsaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.selectislandsaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.selectislandsaction.tooltip",false);
  }
}
