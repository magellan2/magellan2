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
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.CoordinateID;
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

	protected void preSetCleanSelection() {
		// adding does not clean selectedRegion
		// System.out.println("do not clean selection");
	}

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
					selectedRegions.put((CoordinateID)r.getID(), r);
				}
			}
		}
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
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("name", "Add selection...");
			defaultTranslations.put("mnemonic", "d");
			defaultTranslations.put("accelerator", "");
			defaultTranslations.put("tooltip", "");
			defaultTranslations.put("title", "add map selection file");
		}

		return defaultTranslations;
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
