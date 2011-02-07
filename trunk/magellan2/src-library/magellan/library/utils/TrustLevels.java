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
import magellan.library.gamebinding.EresseaConstants;

/**
 * DOCUMENT ME!
 * 
 * @author Ulrich Küster A class providing useful methods on handling factions' trustlevels
 */
public class TrustLevels {
  /**
   * recalculates the default-trustlevel based on the alliances of all privileged factions in the
   * given GameData-Object.
   */
  public static void recalculateTrustLevels(GameData data) {
    if (data.getFactions() != null) {
      // first reset all trustlevel, that were not set by the user
      // but by Magellan itself to TL_DEFAULT
      for (Faction f : data.getFactions()) {
        if (!f.isTrustLevelSetByUser()) {
          f.setTrustLevel(Faction.TL_DEFAULT);
        }

        f.setHasGiveAlliance(false);
      }

      for (Faction f : data.getFactions()) {
        if ((f.getPassword() != null) && !f.isTrustLevelSetByUser()) { // password set
          f.setTrustLevel(Faction.TL_PRIVILEGED);
        }

        if (f.getID().equals(EntityID.createEntityID(-1, data.base))) { // monster or disguised

          if (!f.isTrustLevelSetByUser()) {
            f.setTrustLevel(-20);
          }
        } else if (f.getID().equals(EntityID.createEntityID(0, data.base))) { // faction disguised

          if (!f.isTrustLevelSetByUser()) {
            f.setTrustLevel(-100);
          }
        } else if (f.isPrivileged() && (f.getAllies() != null)) { // privileged

          Iterator<Map.Entry<EntityID, Alliance>> iter = f.getAllies().entrySet().iterator();

          while (iter.hasNext()) {
            Alliance alliance = (iter.next()).getValue();

            // update the trustlevel of the allied factions if their
            // trustlevels were not set by the user
            Faction ally = alliance.getFaction();

            if (!ally.isTrustLevelSetByUser()) {
              ally.setTrustLevel(Math.max(ally.getTrustLevel(), alliance.getTrustLevel()));
            }
            if (alliance.getState(EresseaConstants.A_GIVE)) {
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
   */
  public static boolean containsTrustLevelsSetByUser(GameData data) {
    for (Faction faction : data.getFactions()) {
      if (faction.isTrustLevelSetByUser())
        return true;
    }

    return false;
  }
}
