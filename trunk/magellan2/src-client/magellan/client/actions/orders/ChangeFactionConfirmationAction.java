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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.OrderConfirmEvent;
import magellan.library.Faction;
import magellan.library.Unit;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ilja Pavkovic
 */
public class ChangeFactionConfirmationAction extends MenuAction {
	/** DOCUMENT-ME */
	public static final int SETCONFIRMATION = 0;

	/** DOCUMENT-ME */
	public static final int REMOVECONFIRMATION = 1;

	/** DOCUMENT-ME */
	public static final int INVERTCONFIRMATION = 2;
	private Faction faction;
	private int confirmation; // one of the values above, should be selfexplaining
	private boolean selectedRegionsOnly; // only change confirmation in selected regions

	/**
	 * Creates a new ChangeFactionConfirmationAction object.
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public ChangeFactionConfirmationAction(Client client, Faction f, int conf, boolean r) {
		super(client);

		if(f != null) {
			setName(f.toString());
		}

		faction = f;

		if((conf < 0) || (conf > 2)) {
			throw new IllegalArgumentException();
		}

		confirmation = conf;
		selectedRegionsOnly = r;

		if(selectedRegionsOnly) {
			setName(getName() + " " + Resources.get("magellan.actions.changefactionconfirmationaction.name.postfix.selected"));
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(java.awt.event.ActionEvent e) {
		Collection units = null;

		if(faction == null) {
			if((client.getData() != null) && (client.getData().units() != null)) {
				units = client.getData().units().values();
			}
		} else {
			units = faction.units();
		}

		if(units != null) {
			for(Iterator iter = units.iterator(); iter.hasNext();) {
				Unit unit = (Unit) iter.next();

				if(!unit.isSpy()) {
					// this is slow but ok for this situation (normally one would iterate over the
					// regions and check the containment once per region)
					if(selectedRegionsOnly &&
						   !client.getSelectedRegions().containsKey(unit.getRegion().getID())) {
						continue;
					}

					changeConfirmation(unit);

					// (!) temp units are contained in Faction.units(),
					// but not in GameData.units() (!)
					for(Iterator temps = unit.tempUnits().iterator(); temps.hasNext();) {
						Unit temp = (Unit) temps.next();
						changeConfirmation(temp);
					}
				}
			}

			client.getDispatcher().fire(new OrderConfirmEvent(this,
															  (faction == null)
															  ? client.getData().units().values()
															  : faction.units()));
		}
	}

	private void changeConfirmation(Unit unit) {
		switch(confirmation) {
		case SETCONFIRMATION:
			unit.setOrdersConfirmed(true);

			break;

		case REMOVECONFIRMATION:
			unit.setOrdersConfirmed(false);

			break;

		case INVERTCONFIRMATION:
			unit.setOrdersConfirmed(!unit.isOrdersConfirmed());

			break;

		default:
			break;
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
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("name", "All units");
			defaultTranslations.put("mnemonic", "a");
			defaultTranslations.put("accelerator", "");
			defaultTranslations.put("tooltip", "");
			defaultTranslations.put("name.postfix.selected", "in selected regions only");
		}

		return defaultTranslations;
	}
  


  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("magellan.actions.changefactionconfirmationaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("magellan.actions.changefactionconfirmationaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("magellan.actions.changefactionconfirmationaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("magellan.actions.changefactionconfirmationaction.tooltip",false);
  }
}
