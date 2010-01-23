/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import java.util.List;

import magellan.library.GameData;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderCompleter;
import magellan.library.rules.Race;
import magellan.library.utils.Resources;

/**
 * 
 */
public class E3AOrderCompleter extends EresseaOrderCompleter {

  /**
   * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   * 
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public E3AOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    super(gd, ac);
    setParser(new E3AOrderParser(gd, this));
  }

  /**
   * @see magellan.library.gamebinding.EresseaOrderCompleter#getCompletions(magellan.library.Unit,
   *      java.lang.String, java.util.List) FIXME implement E2K9 subtleties!
   */
  public List<Completion> getCompletions(Unit u, String line, List<Completion> old) {
    return super.getCompletions(u, line, old);
  }

  @Override
  public void cmplt() {
    // add completions, that were defined by the user in the option pane
    // and can be accessed by CompleterSettingsProvider.getSelfDefinedCompletions()
    for (Completion selfDef : getCompleterSettingsProvider().getSelfDefinedCompletions())
      addCompletion(selfDef);
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_WORK)));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ATTACK), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BANNER), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    if (getUnit().getFaction().getItems().size() > 0) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CLAIM), " "));
    }
    if (!getUnit().isHero()) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION)));
    }

    if (!getRegion().buildings().isEmpty()) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SIEGE), " "));
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NAME), " "));

    if (getUnit().getItems().size() > 0) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_USE), " "));
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE), " "));

    if (!getRegion().buildings().isEmpty() || !getRegion().ships().isEmpty()) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ENTER), " "));
    }

    if (getUnit().getGuard() == 0) {

      // special request for myself (Darcduck)
      // if an unit should guard the region it must have a combat state better than FLIEHE (5)
      // of a combat order (KÄMPFE) after all attack orders
      if ((getUnit().getCombatStatus() > 4) && (getUnit().getModifiedCombatStatus() > 4)) {
        addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD)
            + "...", Resources.getOrderTranslation(EresseaConstants.O_GUARD) + "\n"
            + Resources.getOrderTranslation(EresseaConstants.O_COMBAT), " ", 5, 0));
      } else {
        addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD)));
      }
    } else {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_NOT)));
    }
    
    // the if clause is not always correct, but should usually be okay
    if (getUnit().getModifiedBuilding() != null
        || (getUnit().getBuilding() != null && getUnit().getBuilding().getOwnerUnit().equals(
            getUnit()))) {
      addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_PAY) + " "
          + Resources.getOrderTranslation(E3AConstants.O_NOT)));
    }
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT),
        Resources.getOrderTranslation(EresseaConstants.O_DEFAULT) + " '", "",
        Completion.DEFAULT_PRIORITY, 0));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EMAIL), Resources
        .getOrderTranslation(EresseaConstants.O_EMAIL), " \"\"", Completion.DEFAULT_PRIORITY, 1));
    // we focus auf our temp generation dialog FF
    // addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_END)));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RIDE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW), " "));

// if (hasSkill(getUnit(), EresseaConstants.S_KRAEUTERKUNDE, 7)) {
// addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH) + " "
// + Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
// }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GIVE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROUP), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HELP), " "));

    if (hasSkill(getUnit(), EresseaConstants.S_MAGIE)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL),
          " "));
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CONTACT), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEARN), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MAKE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MOVE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NUMBER), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_OPTION), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD), " "));

    if (hasSkill(getUnit(), EresseaConstants.S_KRAEUTERKUNDE, 6)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PLANT), " "));
    }

    if (getUnit().getShip() != null) {
      Unit owner = getUnit().getShip().getModifiedOwnerUnit();

      if (owner != null) {
        if (owner.equals(getUnit())) {
          addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PIRACY),
              " "));
        }
      }
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PREFIX),
        Resources.getOrderTranslation(EresseaConstants.O_PREFIX), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT), " "));

    if (!(getUnit() instanceof TempUnit)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESERVE), " "));
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROUTE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SORT), " "));

    // addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STIRB),
    // " ")); // don't blame me...
    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CARRY), " "));

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN), " "));

    if ((getUnit().getSkills() != null) && (getUnit().getSkills().size() > 0)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FORGET), " ",
          Completion.DEFAULT_PRIORITY + 1));
    }

    if ((getUnit().getBuilding() != null) || (getUnit().getShip() != null)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEAVE)));
    }

    if (hasSkill(getUnit(), EresseaConstants.S_MAGIE)) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CAST), " "));
    }

    addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHOW), " "));

    // TODO dontknow if we should use modified owner here (GIB and ZERSTÖRE have same priority!)
    // units destroying their own building or ship or...
    if (((getUnit().getBuilding() != null) && (getUnit().getBuilding().getOwnerUnit() != null) && (getUnit()
        .getBuilding().getOwnerUnit().equals(getUnit())))
        || ((getUnit().getShip() != null) && (getUnit().getShip().getOwnerUnit() != null) && (getUnit()
            .getShip().getOwnerUnit().equals(getUnit())))
        ||
        // ... vicious warriors destroying other peoples buildings or ships
        (getUnit().getModifiedBuilding() != null
            && getUnit().getModifiedBuilding().getOwnerUnit() != null && getUnit().getFaction() != getUnit()
            .getModifiedBuilding().getOwnerUnit().getFaction())
        || (getUnit().getModifiedShip() != null && (getUnit().getModifiedShip().getOwnerUnit() == null || getUnit()
            .getFaction() != getUnit().getModifiedShip().getOwnerUnit().getFaction()))) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY)));
    } else {
      if (hasSkill(getUnit(), EresseaConstants.S_STRASSENBAU) && (getRegion() != null)
          && !getRegion().borders().isEmpty()) {
        addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY), " "));
      }
    }

// addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROW), " "));
  }

  @Override
  public void cmpltBenenne() {
    super.cmpltBenenne();
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE), " "));
  }

  public void cmpltBezahle() {
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_NOT)));
  }

  @Override
  public void cmpltMache() {
    super.cmpltMache();
    if (!getCompleterSettingsProvider().getLimitMakeCompletion()
        || (getRegion().getItem(getData().rules.getItemType(StringID.create("Holz"))) != null)) {
      addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_WATCH),
          " "));
    }
  }
  
  @Override
  public void cmpltMacheAmount() {
    super.cmpltMacheAmount();
    if (!getCompleterSettingsProvider().getLimitMakeCompletion()
        || (getRegion().getItem(getData().rules.getItemType(StringID.create("Holz"))) != null)) {
      addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_WATCH),
          " "));
    }
  }
  
  public void cmpltRekrutiere() {
    super.cmpltRekrutiere();
  }

  public void cmpltRekrutiereAmount() {
    for (Race r : getData().rules.getRaces()) {
      if (r.getRecruitmentName() != null)
        addCompletion(new Completion(r.getRecruitmentName()));
    }
// addCompletion(new Completion(Resources.get("gamebinding.e3a.e3aordercompleter.race"), "", ""));
  }

  public void cmpltAllianz() {
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_COMMAND),
        " "));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_INVITE), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_JOIN), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_KICK), " "));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_LEAVE), ""));
    addCompletion(new Completion(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_NEW), " "));
  }

}
