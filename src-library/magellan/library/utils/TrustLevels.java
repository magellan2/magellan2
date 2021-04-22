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
import magellan.library.TrustLevel;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.FactionType;

/**
 * A class providing useful methods on handling factions' trustlevels.
 * 
 * @author Ulrich Küster
 */
public class TrustLevels {
  /**
   * recalculates the default-trustlevel based on the alliances of all privileged factions in the
   * given GameData-Object.
   */
  @SuppressWarnings("deprecation")
  public static void recalculateTrustLevels(GameData data) {
    if (data.getFactions() != null) {
      // first reset all trustlevel, that were not set by the user
      // but by Magellan itself to TL_DEFAULT
      for (Faction f : data.getFactions()) {
        if (!f.isTrustLevelSetByUser()) {
          f.setTrustLevel(TrustLevel.TL_DEFAULT);
        }

        f.setHasGiveAlliance(false);
      }

      for (Faction f : data.getFactions()) {
        if ((f.getPassword() != null) && !f.isTrustLevelSetByUser()) { // password set
          f.setTrustLevel(TrustLevel.TL_PRIVILEGED);
        }

        if (isMonsterFaction(f)) { // monster new
          if (!f.isTrustLevelSetByUser()) {
            f.setTrustLevel(TrustLevel.TL_MONSTROUS);
          }
        } else if (f.getID().equals(EntityID.createEntityID(-1, data.base))) { // disguised
          if (!f.isTrustLevelSetByUser()) {
            f.setTrustLevel(TrustLevel.TL_DEFAULT - TrustLevel.TL_PRIVILEGED / 5);
          }
        } else if (f.getID().equals(EntityID.createEntityID(0, data.base))) { // faction disguised FIXME ??
          if (!f.isTrustLevelSetByUser()) {
            f.setTrustLevel(TrustLevel.TL_DEFAULT - TrustLevel.TL_PRIVILEGED);
          }
        } else if (isPrivileged(f)) {
          if (f.getAllies() != null) { // privileged

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
        if (isPrivileged(f) && f.getAlliance() != null) {
          for (magellan.library.ID allyID : f.getAlliance().getFactions()) {
            Faction ally = data.getFaction(allyID);
            if (ally != null && ally != f && !ally.isTrustLevelSetByUser()
                && ally.getPassword() == null) {
              ally.setTrustLevel(TrustLevel.TL_DEFAULT + (TrustLevel.TL_PRIVILEGED - TrustLevel.TL_DEFAULT) * 9 / 10);
            }
          }
        }
      }
    }

    data.postProcessAfterTrustlevelChange();
  }

  private static boolean isMonsterFaction(Faction f) {
    for (FactionType fType : f.getData().getRules().getFactions())
      if (fType.getNumber() == f.getID().intValue())
        return true;
    return false;
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

  public static boolean isPrivileged(Faction faction) {
    return TrustLevel.PRIVILEGED.le(faction.getTrustLevel());
  }

  public static boolean isHostile(Faction faction) {
    return !TrustLevel.DEFAULT.le(faction.getTrustLevel());
  }

  public static boolean isAlly(Faction faction) {
    return !TrustLevel.DEFAULT.ge(faction.getTrustLevel());
  }

  public static boolean isMonstrous(Faction faction) {
    return faction.getID().intValue() == -1 || TrustLevel.MONSTROUS.ge(faction.getTrustLevel());
  }

  public static String getTrustLevelLabel(int level) {
    return Resources.get("factionstatspanel.node.trust." + TrustLevel.getLevel(level).name().toLowerCase());
  }

}
