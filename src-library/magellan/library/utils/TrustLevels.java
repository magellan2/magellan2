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

package magellan.library.utils;

import java.util.Iterator;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster A class providing useful methods on handling factions' trustlevels
 */
public class TrustLevels {
	/**
	 * recalculates the default-trustlevel based on the alliances of all privileged factions in the
	 * given GameData-Object.
	 *
	 * 
	 */
	public static void recalculateTrustLevels(GameData data) {
		if(data.factions() != null) {
			// first reset all trustlevel, that were not set by the user
			// but by Magellan itself to TL_DEFAULT
			for(Iterator iter = data.factions().values().iterator(); iter.hasNext();) {
				Faction f = (Faction) iter.next();

				if(!f.isTrustLevelSetByUser()) {
					f.setTrustLevel(Faction.TL_DEFAULT);
				}
				
				f.setHasGiveAlliance(false);
			}

			for(Iterator factions = data.factions().values().iterator(); factions.hasNext();) {
				Faction f = (Faction) factions.next();

				if((f.getPassword() != null) && !f.isTrustLevelSetByUser()) { // password set
					f.setTrustLevel(Faction.TL_PRIVILEGED);
				}

				if(f.getID().equals(EntityID.createEntityID(-1,data.base))) { // monster

					if(!f.isTrustLevelSetByUser()) {
						f.setTrustLevel(-20);
					}
				} else if(f.getID().equals(EntityID.createEntityID(0,data.base))) { // faction disguised

					if(!f.isTrustLevelSetByUser()) {
						f.setTrustLevel(-100);
					}
				} else if(f.isPrivileged() && (f.getAllies() != null)) { // privileged

					Iterator iter = f.getAllies().entrySet().iterator();

					while(iter.hasNext()) {
						Alliance alliance = (Alliance) ((Map.Entry) iter.next()).getValue();

						// update the trustlevel of the allied factions if their
						// trustlevels were not set by the user
						Faction ally = alliance.getFaction();

						if(!ally.isTrustLevelSetByUser()) {
							ally.setTrustLevel(Math.max(ally.getTrustLevel(), alliance.getTrustLevel()));
						}
						/**
						 * not really fine..but bitmask 8 means "GIVE"
						 * Fiete
						 */
						if (alliance.getState(8)){
							ally.setHasGiveAlliance(true);
						}
					}
				}
			}
		}

		data.postProcessAfterTrustlevelChange();
	}

	/**
	 * determines if the specified gamedata contains trust levels, that were set by the user
	 * explicitly or read from CR (which means the same)
	 *
	 * 
	 *
	 * 
	 */
	public static boolean containsTrustLevelsSetByUser(GameData data) {
		for(Iterator iter = data.factions().values().iterator(); iter.hasNext();) {
			if(((Faction) iter.next()).isTrustLevelSetByUser()) {
				return true;
			}
		}

		return false;
	}
}
