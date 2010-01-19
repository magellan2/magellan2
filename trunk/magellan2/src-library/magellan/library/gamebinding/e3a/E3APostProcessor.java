/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.StringID;
import magellan.library.gamebinding.EresseaPostProcessor;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: stm$
 */
public class E3APostProcessor extends EresseaPostProcessor {
  private static final Logger log = Logger.getInstance(E3APostProcessor.class);
  private static final E3APostProcessor singleton = new E3APostProcessor();

  protected E3APostProcessor() {
  }

  /**
   * Returns an instance.
   */
  public static E3APostProcessor getSingleton() {
    return E3APostProcessor.singleton;
  }

  /**
   * Additionally set COMBAT status according to new AllianceGroups.
   * 
   * @see magellan.library.gamebinding.EresseaPostProcessor#postProcess(magellan.library.GameData)
   */
  @Override
  public void postProcess(GameData data) {
    super.postProcess(data);

    int fightState = 2;
    try {
      fightState = data.rules.getAllianceCategory(StringID.create("KÄMPFE")).getBitMask();
    } catch (NullPointerException e) {
      // FIXME(stm) fix for bug I did not find..., maybe has to do with English reports
      log.error("postProcess " + e);
      e.printStackTrace();
      fightState = 2;
    }

    // for every pair of factions in the AllianceGroup set the "help fight" state
    for (AllianceGroup allianceGroup : data.getAllianceGroups()) {
      for (ID id1 : allianceGroup.getFactions()) {
        Faction faction1 = data.getFaction(id1);
        for (ID id2 : allianceGroup.getFactions()) {
          Faction faction2 = data.getFaction(id2);
          if (faction1 != faction2) {
            boolean found = false;
            if (faction1.getAllies()!=null){
              for (Alliance alliance : faction1.getAllies().values()) {
                if (alliance.getFaction().equals(faction2)) {
                  alliance.addState(fightState);
                  found = true;
                }
              }
            }
            if (!found) {
              if (faction1.getAllies()==null)
                faction1.setAllies(new OrderedHashtable<EntityID, Alliance>());
              faction1.getAllies().put(faction2.getID(), new Alliance(faction2, fightState));
            }
          }
        }
      }
    }
  }
}
