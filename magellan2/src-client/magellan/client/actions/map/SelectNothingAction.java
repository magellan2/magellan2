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
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster
 */
public class SelectNothingAction extends MenuAction implements SelectionListener, GameDataListener {
	private Map<CoordinateID,Region> selectedRegions = new Hashtable<CoordinateID, Region>();

	/**
	 * Creates a new SelectNothingAction object.
	 *
	 * @param client
	 */
	public SelectNothingAction(Client client) {
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
	public void menuActionPerformed(ActionEvent e) {
		for(Iterator iter = selectedRegions.keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();

			if(c.z == client.getLevel()) {
				iter.remove();
			}
		}

		client.getData().setSelectedRegionCoordinates(selectedRegions);
		client.getDispatcher().fire(new SelectionEvent(this, selectedRegions.values(), null,
													   SelectionEvent.ST_REGIONS));
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
			defaultTranslations.put("name", "Select none");
			defaultTranslations.put("mnemonic", "n");
			defaultTranslations.put("accelerator", "");
			defaultTranslations.put("tooltip", "");
		}

		return defaultTranslations;
	}
  


  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.selectnothingaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.selectnothingaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.selectnothingaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.selectnothingaction.tooltip",false);
  }
}
