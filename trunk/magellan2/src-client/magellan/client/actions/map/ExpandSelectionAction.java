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
import java.util.Collection;
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
 * Expanding a Selection about 1 region in each direction
 * (all neighbours, feature request)
 *
 * @author Fiete
 */
public class ExpandSelectionAction extends MenuAction implements GameDataListener,SelectionListener
{
	private Map<CoordinateID,Region> selectedRegions = new Hashtable<CoordinateID, Region>();

	/**
	 * Creates a new InvertSelectionAction object.
	 *
	 * @param client
	 */
	public ExpandSelectionAction(Client client) {
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

		// append the neighboring regions to the already selected regions
		if (client.getData().getSelectedRegionCoordinates()==null){
			return;
		}
		// add all the current selected region in one move
		newSelectedRegions.putAll(client.getData().getSelectedRegionCoordinates());
		for(Iterator iter = client.getData().getSelectedRegionCoordinates().keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();
			Region region = client.getData().regions().get(c);
			// get neighbors
			Collection neighbours = region.getNeighbours(); 
			if (neighbours!=null){
				for (Iterator it = neighbours.iterator();it.hasNext();){
					CoordinateID checkRegionID = (CoordinateID) it.next();
					if (!newSelectedRegions.containsKey(checkRegionID)){
						newSelectedRegions.put(checkRegionID, client.getData().regions().get(checkRegionID));
					}
				}
			}
		}

		selectedRegions = newSelectedRegions;
		client.getData().setSelectedRegionCoordinates(selectedRegions);
		client.getDispatcher().fire(new SelectionEvent(this, selectedRegions.values(), null,
													   SelectionEvent.ST_REGIONS));
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.expandselectionaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.expandselectionaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.expandselectionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.expandselectionaction.tooltip",false);
  }
}
