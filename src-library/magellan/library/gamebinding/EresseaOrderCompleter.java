/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Alliance;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillType;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.Units;

/**
 * A class for offering possible completions on incomplete orders. This class relies on the
 * <tt>OrderParser</tt> for reading input which calls the cmpltX methods of this class when it
 * encounters an incomplete order and has a <tt>OrderCompleter</tt> object registered. A
 * <tt>OrderCompleter</tt> wraps itself around a <tt>OrderParser</tt> so you do not get involved
 * with any of the cmpltX methods. They are solely called by the internal <tt>OrderParser</tt>.
 */
public class EresseaOrderCompleter extends AbstractOrderCompleter {

  /**
   * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   *
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public EresseaOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    super(gd, ac);
  }

  // begin of completion methods invoked by OrderParser
  @Override
  protected void cmplt() {
    super.cmplt();
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_WORK)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ATTACK), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_BANNER), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));
    if (!isLimitCompletions() || unit.getFaction() != null
        && unit.getFaction().getItems().size() > 0) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CLAIM), " "));
    }
    if (!isLimitCompletions() || !unit.isHero()) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PROMOTION)));
    }

    if (hasSkill(unit, EresseaConstants.S_TARNUNG)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_STEAL), " "));
    }

    if (!region.buildings().isEmpty()) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SIEGE), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NAME), " "));

    if (unit.getItems().size() > 0) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_USE), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_DESCRIBE), " "));

    if (!region.buildings().isEmpty() || !region.ships().isEmpty()) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ENTER), " "));
    }

    if (!isLimitCompletions() || unit.getGuard() == 0) {

      // special request for myself (Darcduck)
      // if an unit should guard the region it must have a combat state better than FLIEHE (5)
      // of a combat order (KÄMPFE) after all attack orders
      if ((unit.getCombatStatus() > EresseaConstants.CS_NOT)
          && (unit.getModifiedCombatStatus() > EresseaConstants.CS_NOT)) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GUARD) + "...",
            getOrderTranslation(EresseaConstants.OC_GUARD) + "\n"
                + getOrderTranslation(EresseaConstants.OC_COMBAT), " ", 5, 0));
      } else {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GUARD)));
      }
    } else {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GUARD) + " "
          + getOrderTranslation(EresseaConstants.OC_NOT)));
    }

    // the if clause is not always correct, but should usually be okay
    if (!isLimitCompletions()) {
      Building building = getUnit().getModifiedBuilding();
      if (building == null) {
        building = getUnit().getBuilding();
      }
      Unit owner = building == null ? null : building.getOwnerUnit();
      if (getUnit().equals(owner)) {
        addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_PAY) + " "
            + getOrderTranslation(EresseaConstants.OC_NOT) + " "));
      }
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MESSAGE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_DEFAULT),
        getOrderTranslation(EresseaConstants.OC_DEFAULT) + " " + oneQuote, "",
        Completion.DEFAULT_PRIORITY, 0));
    completions
        .add(new Completion(getOrderTranslation(EresseaConstants.OC_EMAIL),
            getOrderTranslation(EresseaConstants.OC_EMAIL), spaceQuotes,
            Completion.DEFAULT_PRIORITY, 1));
    // we focus auf our temp generation dialog FF
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_END)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_RIDE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FOLLOW), " "));

    if (hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 7)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_RESEARCH) + " "
          + getOrderTranslation(EresseaConstants.OC_HERBS)));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GIVE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GROUP), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HELP), " "));

    if (hasSkill(unit, EresseaConstants.S_MAGIE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBATSPELL), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CONTACT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT), " "));
    if (hasSkills(unit, 2)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_TEACH), " "));
    }
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_LEARN), " "));
    // removed: FF SUPPLY is not supported anymore...in eressea
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SUPPLY),
    // " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MAKE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MOVE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NUMBER), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_OPTION), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PASSWORD), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));

    if (hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 6)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PLANT), " "));
    }

    if (!isLimitCompletions()) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PIRACY), " "));
    } else if (unit.getShip() != null) {
      final Unit owner = unit.getShip().getModifiedOwnerUnit();

      if (!isLimitCompletions() || (owner != null && owner.equals(unit))) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PIRACY), " "));
      }
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PREFIX),
        getOrderTranslation(EresseaConstants.OC_PREFIX), " ", Completion.DEFAULT_PRIORITY, 0));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_RECRUIT), " "));

    if (!(unit instanceof TempUnit)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_RESERVE), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ROUTE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SORT), " "));

    if (hasSkill(unit, EresseaConstants.S_SPIONAGE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SPY), " "));
    }

    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_STIRB),
    // " ")); // don't blame me...
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HIDE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CARRY), " "));

    if (hasSkill(unit, EresseaConstants.S_STEUEREINTREIBEN)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_TAX), " "));
    }

    if (hasSkill(unit, EresseaConstants.S_UNTERHALTUNG)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ENTERTAIN), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ORIGIN), " "));

    if ((unit.getSkills() != null) && (unit.getSkills().size() > 0)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FORGET), " ",
          Completion.DEFAULT_PRIORITY + 1));
    }

    if (hasSkill(unit, EresseaConstants.S_HANDELN) && (region.maxLuxuries() > 0)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_BUY), " "));
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SELL), " "));
    }

    if (!isLimitCompletions() || (unit.getBuilding() != null) || (unit.getShip() != null)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_LEAVE)));
    }

    if (hasSkill(unit, EresseaConstants.S_MAGIE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CAST), " "));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHOW), " "));

    // TODO dontknow if we should use modified owner here (GIB and ZERSTÖRE have same priority!)
    // units destroying their own building or ship or...
    if (!isLimitCompletions()
        || ((unit.getBuilding() != null) && (unit.getBuilding().getOwnerUnit() != null) && (unit
            .getBuilding().getOwnerUnit().equals(unit)))
        || ((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null) && (unit.getShip()
            .getOwnerUnit().equals(unit)))
        ||
        // ... vicious warriors destroying other peoples buildings or ships
        (unit.getModifiedBuilding() != null && unit.getModifiedBuilding().getOwnerUnit() != null
            && unit
                .getFaction() != unit.getModifiedBuilding().getOwnerUnit().getFaction())
        || (unit.getModifiedShip() != null && (unit.getModifiedShip().getOwnerUnit() == null || unit
            .getFaction() != unit.getModifiedShip().getOwnerUnit().getFaction()))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_DESTROY)));
    } else {
      if (hasSkill(unit, EresseaConstants.S_STRASSENBAU) && (region != null)
          && !region.borders().isEmpty()) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_DESTROY), " "));
      }
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_GROW), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SABOTAGE) + " "
        + getOrderTranslation(EresseaConstants.OC_SHIP)));
  }

  /** Add completions for command Attack. */
  public void cmpltAttack() {
    // special request for myself (Darcduck)
    // if an attacking unit has the wrong combat state issue the start
    // of a combat order (KÄMPFE) after all attack orders
    String battleStateOrder = "";
    if ((unit.getCombatStatus() > EresseaConstants.CS_DEFENSIVE)
        && (unit.getModifiedCombatStatus() > EresseaConstants.CS_DEFENSIVE)) {
      battleStateOrder = "\n" + getOrderTranslation(EresseaConstants.OC_COMBAT) + " ";
    }

    // collects spy-units to create a set of attack-orders against all spies later
    final List<Unit> spies = new LinkedList<Unit>();

    // collects enemy units
    // maps faction ids to a List of unit ids
    // to create a set of attack-orders against total factions later
    final Map<ID, List<Unit>> unitList = new Hashtable<ID, List<Unit>>();

    for (Unit curUnit : unit.getRegion().units()) {
      if (curUnit.isSpy()) {
        spies.add(curUnit);
        addUnit(curUnit, battleStateOrder);
      } else {
        final Faction f = curUnit.getFaction();

        if ((f != null) && (f.getTrustLevel() <= Faction.TL_DEFAULT)) {
          List<Unit> v = unitList.get(f.getID());

          if (v == null) {
            v = new LinkedList<Unit>();
            unitList.put(f.getID(), v);
          }

          v.add(curUnit);
          addUnit(curUnit, battleStateOrder);
        }
      }
    }

    if (spies.size() > 0) {
      StringBuilder enemyUnits = getAttackOrders(spies);

      enemyUnits.append(battleStateOrder);
      completions.add(new Completion(
          getTranslation("gamebinding.eressea.eresseaordercompleter.spies"), enemyUnits.toString(),
          "", 5, 0));
    }

    for (ID fID : unitList.keySet()) {
      StringBuilder enemyUnits = getAttackOrders(unitList.get(fID));
      enemyUnits.append(battleStateOrder);
      completions.add(new Completion(getData().getFaction(fID).getName() + " (" + fID.toString()
          + ")",
          enemyUnits.toString(), "", 6, 0));
      completions.add(new Completion(fID.toString() + " (" + getData().getFaction(fID).getName()
          + ")",
          enemyUnits.toString(), "", 7, 0));
    }
  }

  private StringBuilder getAttackOrders(Collection<Unit> spies) {
    StringBuilder enemyUnits = new StringBuilder(); // curUnit.getID().toString()).append("
                                                    // ;").append(curUnit.getName());

    for (Unit curUnit : spies) {
      if (enemyUnits.length() > 0) {
        enemyUnits.append("\n").append(getOrderTranslation(EresseaConstants.OC_ATTACK)).append(" ");
      }
      enemyUnits.append(curUnit.getID().toString()).append(" ;").append(curUnit.getName());
    }
    return enemyUnits;
  }

  /** Add completions for command Beklaue. */
  public void cmpltBeklaue() {
    addEnemyUnits("");
  }

  /** Add completions for command Belagere. */
  public void cmpltBelagere() {
    if ((getData() != null) && (unit != null) && (region != null)) {
      final Faction ownerFaction = unit.getFaction();
      final Iterator<Building> buildings = region.buildings().iterator();

      while (buildings.hasNext()) {
        final Building b = buildings.next();

        if (getData().getGameSpecificRules().isCastle(b.getType())
            && (b.getModifiedOwnerUnit() == null || b.getModifiedOwnerUnit().getFaction().equals(
                ownerFaction) == false)) {
          addNamed(b, "", 0, true);
        }
      }
    }
  }

  /** Add completions for command Benenne. */
  public void cmpltBenenne() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FOREIGNBUILDING), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FOREIGNFACTION), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FOREIGNSHIP), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_FOREIGNUNIT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION),
        spaceQuotes, Completion.DEFAULT_PRIORITY, 1));

    // use old owner unit (BENENNE before GIB)
    if (!isLimitCompletions()
        || (unit.getBuilding() != null && unit.equals(unit.getBuilding().getOwnerUnit()))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
    }

    if (!isLimitCompletions()
        || (unit.getShip() != null && (unit.getShip().getOwnerUnit() != null) && unit.getShip()
            .getOwnerUnit().equals(unit))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
    }
  }

  /** Add completions for command BenenneFremdes. */
  public void cmpltBenenneFremdes(OrderToken token) {
    if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNUNIT))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), " "));
    }
    if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNBUILDING))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " "));
    }
    if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNFACTION))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION),
          " "));
    }
    if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNSHIP))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
    }
  }

  /** Add completions for command BenenneFremdeEinheit. */
  public void cmpltBenenneFremdeEinheit() {
    if ((getData() != null) && (unit != null) && (region != null)) {
      final Faction ownerFaction = unit.getFaction();
      final Iterator<Unit> units = region.units().iterator();

      while (units.hasNext()) {
        final Unit u = units.next();

        if (u.getFaction().equals(ownerFaction) == false) {
          addUnit(u, spaceQuotes, 1);
        }
      }
    }
  }

  /** Add completions for command BenenneFremdesGebaeude. */
  public void cmpltBenenneFremdesGebaeude() {
    if ((getData() != null) && (unit != null) && (region != null)) {
      final Faction ownerFaction = unit.getFaction();
      final Iterator<Building> buildings = region.buildings().iterator();

      while (buildings.hasNext()) {
        final Building b = buildings.next();

        // use old owner unit (BENENNE before GIB)
        if ((b.getOwnerUnit() != null)
            && (b.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
          addNamed(b, spaceQuotes, 1, false);
        }
      }
    }
  }

  /** Add completions for command BenenneFremdePartei. */
  public void cmpltBenenneFremdePartei() {
    if ((getData() != null) && (getData().getFactions() != null) && (unit != null)) {
      final Faction ownerFaction = unit.getFaction();

      for (Faction f : getData().getFactions()) {

        if (f.equals(ownerFaction) == false) {
          addNamed(f, spaceQuotes, 1, false);
        }
      }
    }
  }

  /** Add completions for command BenenneFremdesSchiff. */
  public void cmpltBenenneFremdesSchiff() {
    if ((getData() != null) && (unit != null) && (region != null)) {
      final Faction ownerFaction = unit.getFaction();
      final Iterator<Ship> ships = region.ships().iterator();

      while (ships.hasNext()) {
        final Ship s = ships.next();

        // use old owner unit (BENENNE before GIB)
        if ((s.getOwnerUnit() != null)
            && (s.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
          final String id = s.getID().toString();
          final String name = s.getName();
          completions.add(new Completion(s.getType().getName() + " " + name + " (" + id + ")", id,
              spaceQuotes, Completion.DEFAULT_PRIORITY - 1, 1));
          completions.add(new Completion(id + " (" + s.getType().getName() + " " + name + ")", id,
              spaceQuotes, Completion.DEFAULT_PRIORITY, 1));
        }
      }
    }
  }

  /** Add completions for command BEWACHE. */
  public void cmpltBewache() {
    if (!isLimitCompletions() || unit.getGuard() != 0) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT)));
    }
  }

  /**
   * Ergänzt alle Items der Faction in der Region, deren Anzahl größer als amount ist
   *
   * @param amount
   */
  /** Add completions for command Benutze. */
  public void cmpltBenutze(int amount) {
    // addUnitItems("");
    // addFactionItems("");
    addRegionItemsFaction("", amount);
  }

  /** Add completions for command Beanspruche. */
  public void cmpltBeanspruche() {
    for (Item actItem : unit.getFaction().getItems()) {
      completions.add(new Completion(actItem.getName()));
    }
  }

  /** Add completions for command Beschreibe. */
  public void cmpltBeschreibe() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PRIVATE), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));

    // use old owner unit (BENENNE before GIB)
    if (!isLimitCompletions()
        || (unit.getBuilding() != null && unit.getBuilding().getOwnerUnit().equals(unit))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
    }

    if (!isLimitCompletions()
        || (unit.getShip() != null && unit.getShip().getOwnerUnit() != null && unit.getShip()
            .getOwnerUnit().equals(unit))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), spaceQuotes,
          Completion.DEFAULT_PRIORITY, 1));
    }
  }

  /** Add completions for command Betrete. */
  public void cmpltBetrete() {
    if (region.buildings().size() > 0) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " ", 7));
    }
    addRegionBuildings(getOrderTranslation(EresseaConstants.OC_CASTLE) + " ", " ", unit
        .getBuilding(), true);

    if (region.ships().size() > 0) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " ", 7));
    }
    addRegionShips(getOrderTranslation(EresseaConstants.OC_SHIP) + " ", " ", unit.getShip(), true);
  }

  /** Add completions for command BetreteBurg. */
  public void cmpltBetreteBurg() {
    for (Building building : region.buildings()) {
      final UnitContainer uc = building;

      if (!uc.equals(unit.getBuilding())) {
        addNamed(uc, "", 0, true);
      }
    }
  }

  /** Add completions for command BetreteSchiff. */
  public void cmpltBetreteSchiff() {
    for (Ship ship : region.ships()) {
      final UnitContainer uc = ship;

      if (!uc.equals(unit.getShip())) {
        addNamed(uc, "", 0, true);
      }
    }
  }

  public void cmpltBezahle() {
    addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_NOT), " "));
  }

  public void cmpltBezahleNicht() {
    addRegionBuildings("", "", unit.getBuilding(), true);
  }

  /** Add completions for command Botschaft. */
  public void cmpltBotschaft() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " "));
    completions
        .add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), spaceQuotes,
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
  }

  /** Add completions for command BotschaftEinheit. */
  public void cmpltBotschaftEinheit(boolean omitTemp) {
    addRegionUnits(spaceQuotes, 1, omitTemp);
  }

  /** Add completions for command BotschaftPartei. */
  public void cmpltBotschaftPartei() {
    addOtherFactions(spaceQuotes, 1, false);
  }

  /** Add completions for command BotschaftGebaeude. */
  public void cmpltBotschaftGebaeude() {
    for (final Building uc : region.buildings()) {
      addNamed(uc, spaceQuotes, 1, false);
    }
  }

  /** Add completions for command BotschaftGebaeudeID. */
  public void cmpltBotschaftGebaeudeID() {
    completions.add(new Completion(spaceQuotes, spaceQuotes, "", Completion.DEFAULT_PRIORITY, 1));
  }

  /** Add completions for command BotschaftSchiff. */
  public void cmpltBotschaftSchiff() {
    for (final Ship s : region.ships()) {
      addNamed(s, spaceQuotes, 1, false);
    }
  }

  /** Add completions for command BotschaftSchiffID. */
  public void cmpltBotschaftSchiffID() {
    completions.add(new Completion(spaceQuotes, spaceQuotes, "", Completion.DEFAULT_PRIORITY, 1));
  }

  /** Add completions for command Fahre. */
  public void cmpltFahre(boolean omitTemp) {
    addRegionUnits("", omitTemp);
  }

  /** Add completions for command Folge. */
  public void cmpltFolge() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
  }

  /** Add completions for command FolgeEinheit. */
  public void cmpltFolgeEinheit(boolean omitTemp) {
    addRegionUnits("", omitTemp);
  }

  /** Add completions for command FolgeSchiff. */
  public void cmpltFolgeSchiff() {
    if (region != null) {
      final Iterator<Ship> i = region.ships().iterator();

      while (i.hasNext()) {
        final Ship s = i.next();

        int prio = 0;
        // stm 2007-03-11: follow ships, no matter who's the owner
        if ((s.getModifiedOwnerUnit() != null)
            && (unit.getFaction().equals(s.getModifiedOwnerUnit().getFaction()))) {
          prio = 16;
        }

        addNamed(s, " ", prio, 0, false);
      }

      // add ships from DURCHSCHIFFUNG
      if (region.getTravelThruShips() != null) {
        for (Message message : region.getTravelThruShips()) {
          final String text = message.getText();

          // try to match a ship id in the text
          // TODO: use message type
          final String number = "\\w+";
          final Matcher matcher = Pattern.compile("\\((" + number + ")\\)").matcher(text);
          while (matcher.find()) {
            if (1 <= matcher.groupCount()) {
              final String id = matcher.group(1);
              completions.add(new Completion(text, id, " ", Completion.DEFAULT_PRIORITY - 1));
              completions.add(new Completion(id + " (" + text + ")", id, " "));
            }
          }
        }
      }
    }
  }

  /** Add completions for command Forsche. */
  public void cmpltForsche() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HERBS)));
  }

  /** Add completions for command Gruppe. */
  public void cmpltGruppe() {
    if ((unit != null) && (unit.getFaction() != null) && (unit.getFaction().getGroups() != null)) {
      for (Group g : unit.getFaction().getGroups().values()) {
        if (!g.getName().contains(" ")) {
          completions.add(new Completion(g.getName(), ""));
        }
        completions.add(new Completion("\"" + g.getName() + "\"", ""));
      }
    }
  }

  /** Add completions for command Gib. */
  public void cmpltGib() {
    addRegionUnits(" ", false);
    addRegionShipCommanders(" ");
    addRegionBuildingOwners(" ");
  }

  /** Add completions for command GibUID. */
  public void cmpltGibUID(boolean omitTemp) {
    if (omitTemp) {
      addRegionUnits(" ", omitTemp);
      return;
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ALL), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT)));

    /*
     * if (unit.getBuilding() != null && unit.equals(unit.getBuilding().getOwnerUnit()) ||
     * unit.getShip() != null && unit.equals(unit.getShip().getOwnerUnit())) {
     */
    // if we do not move into or stay in a ship or building we can't give control to another unit
    if (unit.getShip() != null || unit.getModifiedShip() != null || unit.getBuilding() != null
        || unit
            .getModifiedBuilding() != null) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CONTROL)));
    }

    // }
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HERBS)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_EACH) + " "
        + getTranslation("gamebinding.eressea.eresseaordercompleter.amount"),
        getOrderTranslation(EresseaConstants.OC_EACH), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_EACH) + " 1",
        getOrderTranslation(EresseaConstants.OC_EACH) + " 1", " "));
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /** Add completions for command GibJe. */
  public void cmpltGibJe() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /**
   * For multiple-line-completion like the creation of give-orders for the resources of an item it
   * is necessary to get the unit's id and the amount to be given. They are given as parameters:
   *
   * @param uid the unit's id
   * @param i the amount
   * @param each Whether amount contained "JE"
   */
  /** Add completions for command GibUIDAmount. */
  public void cmpltGibUIDAmount(UnitID uid, int i, boolean each) {
    addUnitItems(i, "");

    if ((i != 0) && (uid != null)) {
      // add completions, that create multiple Give-Orders for the resources of an item
      for (final Iterator<ItemType> iter = getData().getRules().getItemTypeIterator(); iter
          .hasNext();) {
        final ItemType iType = iter.next();

        if (iType.getResources() != null && iType.getResources().hasNext() // necessary resources
        // are known
            && checkForMaterials(iType.getResources(), i)) { // necessary resources are available

          addMulti(uid, i, iType.getOrderName(), iType.getResources());
        }
      }
      for (Spell spell : getData().getSpells()) {
        List<Item> comps = new LinkedList<Item>();
        for (Spell.Component sComp : spell.getParsedComponents()) {
          if (sComp.getItem() != null) {
            comps.add(new Item(sComp.getItem(), sComp.getAmount()));
          }
        }

        if (!comps.isEmpty()) {
          addMulti(uid, i, spell.getName(), comps.iterator());
        }
      }
      /**
       * Add multiple GIVE orders for if we enter ALL i.e. assume the unit has 200 sword, shild,
       * plate and 80 horses GIVE abcd 100 [ALL] will complete to GIVE abcd 100 sword GIVE abcd 100
       * shield GIVE abcd 100 plate as we have not at least 100 horses. This is perfect to split
       * units
       */
      String order = "";
      String tounit;
      try {
        tounit =
            getData().getGameSpecificStuff().getOrderChanger().getTokenLocalized(getLocale(), uid);
      } catch (RulesException e) {
        tounit = "TEMP " + uid;
      }
      if (!each && (unit.getPersons() >= i)) {
        order = getOrderTranslation(EresseaConstants.OC_MEN);
      }
      for (final Item item : unit.getItems()) {
        if (item.getAmount() >= i) {

          if ("".equals(order)) {
            order = item.getOrderName();
          } else {
            order +=
                ("\n" + getOrderTranslation(EresseaConstants.OC_GIVE) + " " + tounit + " " + i
                    + " " + item.getOrderName());
          }
        }
      }
      if (!"".equals(order)) {
        completions.add(new Completion(
            getTranslation("gamebinding.eressea.eresseaordercompleter.allall"), order, ""));
      }
    }

    if (!each) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MEN), (unit
          .getPersons() >= i) ? 0 : Completion.DEFAULT_PRIORITY + 1));
    }

    //
    if (!each && unit.getModifiedShip() != null) {
      Ship s = unit.getModifiedShip();
      if (s.getModifiedOwnerUnit().equals(unit)) {
        // is the amount <= number of ships in the fleet?
        if (i <= s.getModifiedAmount()) {
          // valid targets are 0, or the captain of another ship or a unit on the same ship or a
          // unit without ship
          Unit target = getData().getUnit(uid);
          if (uid.intValue() == 0 || (target != null && unit.getFaction().equals(target
              .getFaction()))) {
            Ship targetShip = target == null ? null : target.getModifiedShip();
            if (targetShip == null || targetShip.equals(s) || target == null || target.equals(
                targetShip.getModifiedOwnerUnit()))

              // same shipType
              if (targetShip == null || targetShip.getShipType().equals(s.getShipType())) {
                completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP),
                    Completion.DEFAULT_PRIORITY + 1));
              }

          }
        }
      }
    }

  }

  private void addMulti(UnitID uid, int i, String name, Iterator<Item> resources) {
    boolean suggest = true;
    int loopCount = 0;
    final StringBuffer order = new StringBuffer();

    for (final Iterator<Item> iterator = resources; iterator.hasNext() && suggest; loopCount++) {
      final Item resource = iterator.next();

      if ((loopCount == 0) && !iterator.hasNext()) {
        // only one resource is necessary for this ItemType
        // don't create a completion to give the resource for this ItemType
        suggest = false;
      } else {

        if (order.length() == 0) {
          order.append(resource.getOrderName());
        } else {
          order.append("\n").append(getOrderTranslation(EresseaConstants.OC_GIVE)).append(" ")
              .append(uid.toString()).append(" ").append(i * resource.getAmount()).append(" ")
              .append(resource.getOrderName());
        }
        order.append("; ").append(name);
      }
    }

    if (suggest) {
      completions.add(new Completion("R-" + name, order.toString(), "",
          Completion.DEFAULT_PRIORITY + 1));
    }
  }

  /** Add completions for command GibUIDAmount. */
  public void cmpltGibUIDAmount() {
    cmpltGibUIDAmount(null, 0, true);
  }

  /** Add completions for command GibUIDAlles. */
  public void cmpltGibUIDAlles() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MEN)));
    addUnitItems("");
  }

  /** Add completions for command Helfe. */
  public void cmpltHelfe() {
    addOtherFactions(" ", 0, false);
  }

  /** Add completions for command HelfeFID. */
  public void cmpltHelfeFID() {
    for (final Iterator<AllianceCategory> it = getData().getRules()
        .getAllianceCategoryIterator(); it
            .hasNext();) {
      final AllianceCategory all = it.next();
      completions.add(new Completion(getOrderTranslation(StringID
          .create(GameConstants.ORDER_KEY_PREFIX + all.getName()))));
    }
  }

  /** Add completions for command HelfeFIDModifier. */
  public void cmpltHelfeFIDModifier() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT)));
  }

  /** Add completions for command Kaempfe. */
  public void cmpltKaempfe() {
    // adjust order of completions by guard and attack status
    int guardMalus = 0;
    int attackMalus = 0;
    if (unit != null) {
      if (unit.getModifiedGuard() > 0) {
        guardMalus = 10;
      }
      if ((unit.getAttackVictims() != null) && (unit.getAttackVictims().size() > 0)) {
        attackMalus = 20;
      }
    }

    if (!isLimitCompletions()
        || (unit == null)
        || ((unit.getCombatStatus() != EresseaConstants.CS_AGGRESSIVE) && (unit
            .getCombatStatus() != -1))) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_AGGRESSIVE),
          "", unit.getCombatStatus()));
    }

    // do not propose "VORNE"
    // if ((unit == null) || (unit.getCombatStatus() != EresseaConstants.CS_FRONT)) {
    // completions.add(new
    // Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_FRONT),
    // "", Math.abs(unit.getCombatStatus() - 2)));
    // }

    if (!isLimitCompletions() || (unit == null)
        || (unit.getCombatStatus() != EresseaConstants.CS_REAR)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_REAR), "", Math
          .abs(unit.getCombatStatus() - 3)));
    }

    if (!isLimitCompletions() || (unit == null)
        || (unit.getCombatStatus() != EresseaConstants.CS_DEFENSIVE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_DEFENSIVE), "",
          Math.abs(unit.getCombatStatus() - 3)));
    }

    if (!isLimitCompletions() || (unit == null)
        || (unit.getCombatStatus() != EresseaConstants.CS_NOT)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_NOT), "", Math
          .abs(unit.getCombatStatus() - 4)
          + attackMalus));
    }

    if (!isLimitCompletions() || (unit == null)
        || (unit.getCombatStatus() != EresseaConstants.CS_FLEE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_FLEE), "", Math
          .abs(unit.getCombatStatus() - 5)
          + guardMalus + attackMalus));
    }

    // ACHTUNG!!!!
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_HELP), " "));
  }

  /** Add completions for command KaempfeHelfe. */
  public void cmpltKaempfeHelfe() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMBAT_NOT)));
  }

  /** Add completions for command Kaufe. */
  public void cmpltKaufe() {
    completions.add(new Completion(region.maxLuxuries() + "", " "));
  }

  /** Add completions for command KaufeAmount. */
  public void cmpltKaufeAmount() {
    String item = null;

    if (region.getPrices() != null) {
      for (LuxuryPrice p : region.getPrices().values()) {
        if (p.getPrice() < 0) {
          item = p.getItemType().getName();

          break;
        }
      }
    }

    if (item == null) {
      if ((getData() != null) && (getData().getRules() != null)) {
        final ItemCategory luxCat = getData().getRules().getItemCategory(
            EresseaConstants.C_LUXURIES);

        if (luxCat != null) {
          for (final Iterator<ItemType> iter = getData().getRules().getItemTypeIterator(); iter
              .hasNext();) {
            final ItemType t = iter.next();

            if (luxCat.equals(t.getCategory())) {
              completions.add(new Completion(t.getOrderName()));
            }
          }
        }
      }
    } else {
      completions.add(new Completion(item));
    }
  }

  /** Add completions for command Kampfzauber. */
  public void cmpltKampfzauber(boolean modifiers, String opening, String closing) {
    if ((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
      if (modifiers) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_LEVEL), " ",
            Completion.DEFAULT_PRIORITY - 1));

        // if ((unit.getCombatSpells() != null) && (unit.getCombatSpells().size() > 0)) {
        // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT), "",
        // Completion.DEFAULT_PRIORITY - 1));
        // }
      }

      addFilteredSpells(unit, false, region.getType().equals(
          getData().getRules().getRegionType(EresseaConstants.RT_OCEAN)), true, opening, closing);
    }
  }

  /** Add completions for command KampfzauberSpell. */
  public void cmpltKampfzauberSpell() {
    // at this point it is not important if the unit has combat spells, so we don't check for it.
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT), ""));
  }

  /** Add completions for command Kontaktiere. */
  public void cmpltKontaktiere() {
    final Alliance alliance =
        new Alliance(unit.getFaction(), EresseaConstants.A_GIVE | EresseaConstants.A_GUARD);
    addNotAlliedUnits(alliance, "");
  }

  /** Add completions for command Lehre. */
  public void cmpltLehre(boolean omitTemp) {
    addRegionUnits(" ", omitTemp);
  }

  /** Add completions for command Lerne. */
  public void cmpltLerne() {
    addSkills();
  }

  /** Add completions for command LerneTalent. */
  public void cmpltLerneTalent(SkillType t) {
    if (getData() != null && getData().getRules() != null && t != null) {
      final int cost = getSkillCost(t, unit);

      if (cost > 0) {
        completions.add(new Completion(Integer.toString(cost)));
      }
      if (t.equals(getData().getRules().getSkillType(EresseaConstants.S_MAGIE))
          && (unit.getFaction() == null || unit.getFaction().getSpellSchool() == null)) {
        completions.add(new Completion(oneQuote
            + getTranslation("gamebinding.eressea.eresseaordercompleter.magicarea") + oneQuote,
            twoQuotes, "", Completion.DEFAULT_PRIORITY, 1));
      }
    }
  }

  /**
   * Returns the learn cost for a specific skill.
   *
   * @param skillType the skill to be learned
   * @return the cost to learn a skill for the given unit. If the unit has no persons the cost for
   *         one person is returned.
   */
  @Override
  public int getSkillCost(SkillType skillType, Unit someUnit) {
    // int cost = 0;
    int c2 = 0;

    final Skill sk = someUnit.getSkill(skillType);
    if (sk == null) {
      c2 = skillType.getCost(1);
    } else {
      c2 = skillType.getCost(1 + sk.getLevel() - sk.getModifier(someUnit));
    }

    // if (skillType.getID().equals(EresseaConstants.S_TAKTIK)
    // || skillType.getID().equals(EresseaConstants.S_KRAEUTERKUNDE)
    // || skillType.getID().equals(EresseaConstants.S_ALCHEMIE)) {
    // cost = 200;
    // } else if (skillType.getID().equals(EresseaConstants.S_SPIONAGE)) {
    // cost = 100;
    // } else if (skillType.getID().equals(EresseaConstants.S_MAGIE)) {
    // // get magiclevel without modifier
    // int level = 0;
    // final Skill skill = (someUnit != null) ? someUnit.getSkill(skillType) : null;
    //
    // if (skill != null && someUnit != null) {
    // if (skill.noSkillPoints()) {
    // level = skill.getLevel() - skill.getModifier(someUnit);
    // } else {
    // final int days = someUnit.getSkill(skillType).getPointsPerPerson();
    // level = (int) Math.floor(Math.sqrt((days / 15.0) + 0.25) - 0.5);
    // }
    // }
    //
    // final int nextLevel = level + 1;
    // cost = (int) (50 + ((50 * (1 + nextLevel) * (nextLevel)) / 2.0));
    // }

    if ((someUnit.getModifiedBuilding() != null)
        && someUnit.getModifiedBuilding().getType().equals(
            getData().getRules().getBuildingType(EresseaConstants.B_ACADEMY))) {
      if (c2 == 0) {
        // cost = 50;
        c2 = 50;
      } else {
        // cost *= 2;
        c2 *= 2;
      }
    }

    // cost *= Math.max(1, someUnit.getModifiedPersons());
    c2 *= Math.max(1, someUnit.getModifiedPersons());

    // if (c2 != cost && getData().getGameSpecificStuff().getName().equalsIgnoreCase("eressea")) {
    // EresseaOrderCompleter.log.error("assertion error getSkillCost()");
    // }
    return c2;
  }

  /** Add completions for command Liefere. */
  public void cmpltLiefere() {
    cmpltGib();
  }

  /** Add completions for command Locale. */
  public void cmpltLocale() {
    completions.add(new Completion("deutsch", oneQuote + "de" + oneQuote, ""));
    completions.add(new Completion("english", oneQuote + "en" + oneQuote, ""));
  }

  /** Add completions for command Mache. */
  public void cmpltMache() {
    // we focus on our temp creation dialog
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_TEMP),
    // " "));
    cmpltMacheAmount();
  }

  /** Add completions for command MacheAmount. */
  public void cmpltMacheAmount() {
    // buildings
    if (hasSkill(unit, EresseaConstants.S_BURGENBAU)) {
      if ((getData() != null) && (getData().getRules() != null)) {
        for (final Iterator<BuildingType> iter = getData().getRules()
            .getBuildingTypeIterator(); iter
                .hasNext();) {
          final BuildingType t = iter.next();

          if (!isLimitCompletions()
              || ((t instanceof CastleType) && t.containsRegionType(region.getRegionType())
                  && hasSkill(unit, EresseaConstants.S_BURGENBAU, t.getBuildSkillLevel())
                  && (!isLimitCompletions() || checkForMaterials(t.getRawMaterials()
                      .iterator())))) {
            String name =
                Resources.getRuleItemTranslation("building." + t.getID().toString(), getLocale());
            if (name.startsWith("rules.skill")) {
              name = t.getName();
            }
            name = name.replace(' ', '~');

            completions.add(new Completion(name, " "));
          }
        }
      }

      if (hasSkill(unit, EresseaConstants.S_BURGENBAU)
          && !isLimitCompletions()
          || (Units.getContainerPrivilegedUnitItem(region, getData().getRules().getItemType(
              EresseaConstants.I_USTONE)) != null)) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " "));
      }
    }

    // ships
    if (hasSkill(unit, EresseaConstants.S_SCHIFFBAU)
        && (!isLimitCompletions() || (Units
            .getContainerPrivilegedUnitItem(region, getData().getRules().getItemType(
                EresseaConstants.I_WOOD)) != null))) {
      if ((getData() != null) && (getData().getRules() != null)) {
        for (final Iterator<ShipType> iter = getData().getRules().getShipTypeIterator(); iter
            .hasNext();) {
          final ShipType t = iter.next();

          if (hasSkill(unit, EresseaConstants.S_SCHIFFBAU, t.getBuildSkillLevel())) {
            completions.add(new Completion(t.getName(), " "));
          }
        }
      }

      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
    }

    // streets
    // check, if there is the necessary roadsupportbuilding
    final BuildingType b = region.getRegionType().getRoadSupportBuilding();
    boolean canMake = false;

    if (b == null) {
      canMake = true;
    } else {
      for (final Iterator<Building> iter = region.buildings().iterator(); iter.hasNext()
          && !canMake;) {
        if ((iter.next()).getBuildingType().equals(b)) {
          canMake = true;
        }
      }
    }

    if (hasSkill(unit, EresseaConstants.S_STRASSENBAU)
        && (!isLimitCompletions() || (Units
            .getContainerPrivilegedUnitItem(region, getData().getRules().getItemType(
                EresseaConstants.I_USTONE)) != null)) && canMake) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ROAD), " "));
    }

    if (hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE)) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HERBS), " "));
    }

    // items
    for (final Iterator<ItemType> iter = getData().getRules().getItemTypeIterator(); iter
        .hasNext();) {
      final ItemType itemType = iter.next();
      canMake = true;

      if (itemType.getMakeSkill() == null) {
        // some items can not be made like dragonblood or magic artefacts
        canMake = false;
      } else if (!hasSkill(unit, itemType.getMakeSkill().getSkillType().getID(), itemType
          .getMakeSkill().getLevel())) {
        canMake = false;
      } else if (isLimitCompletions()
          && !checkForMaterials(itemType.getResources())) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_UIRON))
          && (region.getIron() <= 0)) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_ULAEN))
          && (region.getLaen() <= 0)) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_WOOD)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
      // also take care of mallorn flag
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_UMALLORN)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || !region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_UHORSE))
          && (region.getHorses() <= 0)) {
        canMake = false;
      } else if (itemType.equals(getData().getRules().getItemType(EresseaConstants.I_USTONE))
          && (region.getStones() <= 0)) {
        canMake = false;
      }

      if (!isLimitCompletions() || canMake) {
        addItem(itemType, "");
      }
    }
  }

  /** Add completions for command MacheTemp. */
  public void cmpltMacheTemp() {
    // we could offer an ID here but not for now...
  }

  /** Add completions for command MacheTempID. */
  public void cmpltMacheTempID() {
    completions.add(new Completion(twoQuotes));
  }

  /** Add completions for command MacheBurg. */
  public void cmpltMacheBurg() {
    final Iterator<Building> i = region.buildings().iterator();

    while ((i != null) && i.hasNext()) {
      final Building b = i.next();
      final BuildingType type = b.getBuildingType();

      if (type instanceof CastleType || (type.getMaxSize() != b.getSize())) {
        addNamed(b, "", 0, true);
      }
    }
  }

  /** Add completions for command MacheBuilding. */
  public void cmpltMacheBuilding(String typeName) {
    // TODO(pavkovic): korrigieren!!! Hier soll eigentlich das Gebäude über den
    // übersetzten Namen gefunden werden!!!
    // BuildingType type = ((Eressea) getData().rules).getBuildingType(typeName);
    final BuildingType type = getData().getRules().getBuildingType(StringID.create(typeName));

    if (type != null) {
      final Iterator<Building> i = region.buildings().iterator();

      while ((i != null) && i.hasNext()) {
        final UnitContainer uc = i.next();

        if (uc.getType().equals(type)) {
          addNamed(uc, "", 0, true);
        }
      }
    }
  }

  /** Add completions for command MacheSchiff. */
  public void cmpltMacheSchiff() {
    final Faction ownerFaction = unit.getFaction();
    final Iterator<Ship> i = region.ships().iterator();

    while ((i != null) && i.hasNext()) {
      final Ship s = i.next();

      if ((s.getModifiedOwnerUnit() != null)
          && ownerFaction.equals(s.getModifiedOwnerUnit().getFaction())) {
        addNamed(s, "", Completion.DEFAULT_PRIORITY - 2, 0, true);
      } else {
        addNamed(s, "", Completion.DEFAULT_PRIORITY, 0, true);
      }
    }
  }

  /** Add completions for command MacheStrasse. */
  public void cmpltMacheStrasse() {
    addDirections("");
  }

  /** Add completions for command Nach. */
  public void cmpltNach() {
    addDirections(" ");
    addSurroundingRegions(getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(unit,
        true), " ");
  }

  /** Add completions for command NachDirection. */
  public void cmpltNachDirection() {
    addDirections(" ");
  }

  /** Add completions for command Neustart. */
  public void cmpltNeustart() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.race"), "", ""));
  }

  /** Add completions for command Nummer. */
  public void cmpltNummer() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
    completions
        .add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " "));
  }

  /** add completions for command NUMMER EINHEIT/BUILDING/SHIP ... */
  public void cmpltNummerId() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.newid"), "1", " "));
  }

  /** Add completions for command Option. */
  public void cmpltOption() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ADDRESSES), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REPORT), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_BZIP2), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_COMPUTER), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ITEMPOOL), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SCORE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SILVERPOOL), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_STATISTICS), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ZIPPED), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_TEMPLATE), " "));
  }

  /** Add completions for command OptionOption. */
  public void cmpltOptionOption() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT)));
  }

  /** Add completions for command Pflanze. */
  public void cmpltPflanze() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /** Add completions for command Pflanze. */
  public void cmpltPflanze(int minAmount) {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HERBS)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_TREES)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SEED)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_MALLORNSEED)));
  }

  /** Add completions for command Piraterie. */
  public void cmpltPiraterie() {
    addOtherFactions(" ", 0, false);
  }

  /** Add completions for command PiraterieFID. */
  public void cmpltPiraterieFID() {
    cmpltPiraterie();
  }

  /** Add completions for command Praefix. */
  public void cmpltPraefix() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.prefix"), "", ""));
  }

  /** Add completions for command Rekrutiere. */
  public void cmpltRekrutiere() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /** Add completions for command Reserviere. */
  public void cmpltReserviere() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_EACH), " "));
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));

    // reserve all items that the unit has
    StringBuilder all = new StringBuilder();
    for (final Item item : unit.getItems()) {
      int newAmount = item.getAmount();
      // reserve new amount if it has decreased
      for (final Item newItem : unit.getModifiedItems()) {
        if (item.getItemType() == newItem.getItemType()) {
          newAmount = Math.min(newItem.getAmount(), newAmount);
        }
      }
      if (newAmount > 0) {
        if (all.length() > 0) {
          all.append("\n").append(getOrderTranslation(EresseaConstants.OC_RESERVE)).append(" ");
        }
        if (newAmount == unit.getModifiedPersons()) {
          all.append(getOrderTranslation(EresseaConstants.OC_EACH)).append(" 1 ");
        } else {
          all.append(newAmount).append(" ");
        }
        all.append(item.getOrderName());
      }

      // RESERVE <Item (all)>
      completions.add(new Completion(item.getName() + " "
          + getTranslation("gamebinding.eressea.eresseaordercompleter.allamount"), item.getAmount()
              + " " + item.getOrderName(), ""));
    }

    // RESERVE <(all remaining items)>
    if (all.length() > 0) {
      completions.add(new Completion(
          getTranslation("gamebinding.eressea.eresseaordercompleter.allremaining"), all.toString(),
          ""));
    }

    addMaxReserve(unit);

  }

  /**
   * Reserve as much of the item as the unit can carry.
   *
   * @param otherUnit
   */
  private void addMaxReserve(Unit otherUnit) {
    final int modLoad = getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(otherUnit);
    final ItemType carts = getData().getRules().getItemType(EresseaConstants.I_CART);
    final int maxOnFoot = getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(otherUnit);
    final int maxOnHorse =
        getGameSpecificStuff().getMovementEvaluator().getPayloadOnHorse(otherUnit);
    final int maxOnShip =
        otherUnit.getModifiedShip() == null ? 0 : otherUnit.getModifiedShip().getCapacity()
            - otherUnit.getModifiedShip().getModifiedLoad();

    for (Units.StatItem item : Units.getContainerAllUnitItems(otherUnit.getRegion())) {
      final ItemType type = item.getItemType();

      if ((type.getWeight() > 0.0) && !type.isHorse() && !type.equals(carts)) {
        final int weight = (int) (type.getWeight() * 100);
        int existingWeight =
            otherUnit.getModifiedItem(type) != null ? otherUnit.getModifiedItem(type).getAmount()
                * weight : 0;
        if (weight > 0) {
          if (maxOnFoot > 0 && (maxOnFoot - modLoad) >= weight) {
            completions.add(new Completion(type.getName() + " "
                + getTranslation("gamebinding.eressea.eresseaordercompleter.maxfootamount"),
                (maxOnFoot - modLoad + existingWeight) / weight + " " + type.getOrderName(), ""));
          }
          // maxOnHorse could be MIN_INT, so check > 0!
          if (maxOnHorse > 0 && (maxOnHorse - modLoad) >= weight) {
            completions.add(new Completion(type.getName() + " "
                + getTranslation("gamebinding.eressea.eresseaordercompleter.maxhorseamount"),
                (maxOnHorse - modLoad + existingWeight) / weight + " " + type.getOrderName(), ""));
          }
          // maxOnHorse could be MIN_INT, so check > 0!
          if (maxOnShip >= weight && maxOnShip > 0) {
            completions.add(new Completion(type.getName() + " "
                + getTranslation("gamebinding.eressea.eresseaordercompleter.maxshipamount"),
                ((maxOnShip + existingWeight) / weight) + " " + type.getOrderName(), ""));
          }
        }
      }
    }
  }

  /** Add completions for command ReserviereJe. */
  public void cmpltReserviereJe() {
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /** Add completions for command ReserviereAmount. */
  public void cmpltReserviereAmount() {
    addUnitItems("");
    if (getSilverPool()) {
      // if unit doesn't have silver, but silver pool is available
      // if ((unit.getItem(getData().rules.getItemType(EresseaConstants.I_USILVER)) == null)
      // && (Units.getContainerPrivilegedUnitItem(region, getData().rules
      // .getItemType(EresseaConstants.I_USILVER)) != null)) {
      completions.add(new Completion(getData().getRules().getItemType(EresseaConstants.I_USILVER)
          .getOrderName(), Completion.DEFAULT_PRIORITY - 1));
      // }
    }
    if (getMaterialPool()) {
      for (Units.StatItem item : Units.getContainerAllUnitItems(region)) {
        if (unit.getItem(item.getItemType()) == null) {
          // silver only if silver pool activated or unit has silver
          if ((item.getItemType() != getData().getRules().getItemType(
              EresseaConstants.I_USILVER))) {
            final String name = item.getName();
            String quotedName = name;

            if ((name.indexOf(" ") > -1)) {
              quotedName = oneQuote + name + oneQuote;
            }

            completions.add(new Completion(name, quotedName, "", Completion.DEFAULT_PRIORITY + 1));
          }
        }
      }
    }
  }

  protected boolean getMaterialPool() {
    // if (f.getOptions() != null) {
    // silverPool = f.getOptions().isActive(StringID.create(EresseaConstants.OC_SILVERPOOL));
    // materialPool = f.getOptions().isActive(StringID.create(EresseaConstants.OC_ITEMPOOL));
    // }

    boolean materialPool =
        getGameSpecificStuff().getGameSpecificRules().isPooled(unit, (StringID) null);
    return materialPool;
  }

  protected boolean getSilverPool() {
    boolean silverPool =
        getGameSpecificStuff().getGameSpecificRules().isPooled(unit,
            getData().getRules().getItemType(EresseaConstants.I_USILVER));
    return silverPool;
  }

  /** Add completions for command Route. */
  public void cmpltRoute() {
    addDirections(" ");
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PAUSE), " "));
    addSurroundingRegions(getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(unit,
        true), " ");
  }

  /** Add completions for command RouteDirection. */
  public void cmpltRouteDirection() {
    addDirections(" ");
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PAUSE), " "));
  }

  /** Add completions for command Sabotiere. */
  public void cmpltSabotiere() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP)));
  }

  /** Add completions for command Sortiere. */
  public void cmpltSortiere() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_BEFORE), " "));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_AFTER), " "));
  }

  /** Add completions for command SortiereVor. */
  public void cmpltSortiereVor() {
    if (unit.getBuilding() != null) {
      addSortiereUnits(unit, unit.getBuilding(), false);
    } else if (unit.getShip() != null) {
      addSortiereUnits(unit, unit.getShip(), false);
    } else {
      for (Unit u : region.units()) {
        if (unit.getFaction().equals(u.getFaction()) && (u.getBuilding() == null)
            && (u.getShip() == null)) {
          if (!u.equals(unit)) {
            addUnit(u, "");
          }
        }
      }
    }
  }

  /** Add completions for command SortiereHinter. */
  public void cmpltSortiereHinter() {
    if (unit.getBuilding() != null) {
      addSortiereUnits(unit, unit.getBuilding(), true);
    } else if (unit.getShip() != null) {
      addSortiereUnits(unit, unit.getShip(), true);
    } else {
      for (Unit u : region.units()) {
        if (unit.getFaction().equals(u.getFaction()) && (u.getBuilding() == null)
            && (u.getShip() == null)) {
          if (!u.equals(unit)) {
            addUnit(u, "");
          }
        }
      }
    }
  }

  /**
   * Adds completions for all units in the container <code>c</code> if they're in the same buildg as
   * <code>u</code>.
   *
   * @param u
   * @param c
   * @param addOwner If true, the container's owner is included if applicable.
   */
  private void addSortiereUnits(Unit u, UnitContainer c, boolean addOwner) {
    for (final Unit currentUnit : c.units()) {
      if (u.getFaction().equals(currentUnit.getFaction())
          && (c.equals(currentUnit.getBuilding()) || c.equals(currentUnit.getShip()))) {
        if (!u.equals(currentUnit) && (addOwner || !currentUnit.equals(c.getModifiedOwnerUnit()))) {
          addUnit(currentUnit, "");
        }
      }
    }
  }

  /** Add completions for command Spioniere. */
  public void cmpltSpioniere() {
    addEnemyUnits("");
  }

  /** Add completions for command Stirb. */
  public void cmpltStirb() {
    if (Units.isPrivilegedAndNoSpy(unit)) {
      completions.add(new Completion(oneQuote
          + getTranslation("gamebinding.eressea.eresseaordercompleter.password") + oneQuote,
          twoQuotes, ""));
    }
  }

  /** Add completions for command Tarne. */
  public void cmpltTarne(boolean quoted) {
    if (!quoted) {
      if (unit.isHideFaction()) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION)
            + " " + getOrderTranslation(EresseaConstants.OC_NOT),
            getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION), " "
                + getOrderTranslation(EresseaConstants.OC_NOT)));
      } else {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION),
            " "));
      }
      // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT)));
      completions.add(new Completion("0"));
    }

    if ((getData() != null) && (getData().getRules() != null)) {
      final Race demons = getData().getRules().getRace(EresseaConstants.R_DAEMONEN);

      if ((demons == null) || (unit.getRace().equals(demons))) {
        for (final Iterator<Race> iter = getData().getRules().getRaceIterator(); iter.hasNext();) {
          final Race r = iter.next();
          completions.add(new Completion(r.getName(), Completion.DEFAULT_PRIORITY + 1));
        }
      }
    }

  }

  /** Add completions for command TarnePartei. */
  public void cmpltTarnePartei() {
    if (unit.isHideFaction()) {
      completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_NOT)));
    }

    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_STEALTH_NUMBER), " "));
  }

  /** Add completions for command TarneParteiNummer. */
  public void cmpltTarneParteiNummer() {
    addFactions("");
  }

  /** Add completions for command Transportiere. */
  public void cmpltTransportiere(boolean omitTemp) {
    addRegionUnits("", omitTemp);
  }

  /** Add completions for command Vergesse. */
  public void cmpltVergesse() {
    for (Skill skill : unit.getSkills()) {
      completions.add(new Completion(skill.getName(), ""));
    }
  }

  /** Add completions for command Verkaufe. */
  public void cmpltVerkaufe() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ALL), " "));
  }

  /** Add completions for command VerkaufeAmount. */
  public void cmpltVerkaufeAmount() {
    addUnitLuxuries("");
  }

  /** Add completions for command VerkaufeAlles. */
  public void cmpltVerkaufeAlles() {
    addUnitLuxuries("");
  }

  /** Add completions for command Zaubere. */
  public void cmpltZaubere(boolean far, boolean combat, boolean addRegion, boolean addLevel,
      String opening, String closing) {
    // this is the check for magicans & familars with own spells:
    if (!isLimitCompletions() || (unit.getSpells() != null && unit.getSpells().size() > 0)) {
      if (addRegion) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), " ",
            Completion.DEFAULT_PRIORITY - 1));
      }
      if (addLevel) {
        completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_LEVEL), " ",
            Completion.DEFAULT_PRIORITY - 1));
      }
      if (unit.getSpells() != null) {
        addFilteredSpells(unit, far, region.getType().equals(
            getData().getRules().getRegionType(EresseaConstants.RT_OCEAN)), combat, opening,
            closing);
      }
    }

    // here we go for spells spoken through the familar
    if (unit.getFamiliarmageID() != null) {
      final Unit mage = getData().getUnit(unit.getFamiliarmageID());
      if ((mage != null) && (mage.getSpells() != null) && (mage.getSpells().size() > 0)) {
        if (addRegion) {
          completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), " ",
              Completion.DEFAULT_PRIORITY - 1));
        }
        if (addLevel) {
          completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_LEVEL), " ",
              Completion.DEFAULT_PRIORITY - 1));
        }
        addFamilarSpells(mage, unit, opening, closing);
      }
    }
  }

  /** Add completions for command ZaubereStufe. */
  public void cmpltZaubereStufe() {
    final Skill magic = unit.getSkill(getData().getRules().getSkillType(EresseaConstants.S_MAGIE));
    int level = 1;
    if (magic != null) {
      level = magic.getLevel();
    }
    completions.add(new Completion(
        getTranslation("gamebinding.eressea.eresseaordercompleter.level"), String.valueOf(level),
        ""));
  }

  /** Add completions for command ZaubereRegion. */
  public void cmpltZaubereRegion() {
    final Map<CoordinateID, Region> regions1 =
        Regions.getAllNeighbours(getData().regions(), region.getID(), 1, null);
    final Map<CoordinateID, Region> regions2 =
        Regions.getAllNeighbours(getData().regions(), region.getID(), 2, null);

    CoordinateID trans =
        getData().getCoordinateTranslation(unit.getFaction().getID(), region.getCoordinate()
            .getZ());
    if (trans != null) {
      trans = CoordinateID.create(0 - trans.getX(), 0 - trans.getY(), trans.getZ() - trans.getZ());
    }

    // first add all regions within a radius of 1 and remove them from Map regions2
    for (final CoordinateID c : regions1.keySet()) {

      if (!c.equals(region.getCoordinate())) {
        final Region r = regions1.get(c);
        String name = r.getName();
        int prio = Completion.DEFAULT_PRIORITY - 2;

        if (name == null) {
          name = c.toString();
          prio = Completion.DEFAULT_PRIORITY - 1;
        }

        if (trans != null) {
          completions.add(new Completion(name, trans.createDistanceCoordinate(c).toString(" "),
              " ", prio));
        } else {
          completions.add(new Completion(name, c.toString(" "), " ", prio));
        }
      }

      regions2.remove(c);
    }

    for (final CoordinateID c : regions2.keySet()) {
      final Region r = regions2.get(c);
      String name = r.getName();
      int prio = Completion.DEFAULT_PRIORITY;

      if (name == null) {
        name = c.toString(" ");
        prio += 1;
      }

      if (trans != null) {
        completions.add(new Completion(name, trans.createDistanceCoordinate(c).toString(" "), " ",
            prio));
      } else {
        completions.add(new Completion(name, c.toString(" "), " ", prio));
      }
    }
  }

  /** Add completions for command ZaubereRegionStufe. */
  public void cmpltZaubereRegionStufe() {
    cmpltZaubereStufe();
    /*
     * if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
     * addFilteredSpells(unit.getSpells().values(), true,
     * region.getType().equals(getData().rules.getRegionType(EresseaConstants.RT_OCEAN)), false); }
     */
  }

  /*
   * Enno in e-client about the syntax: 'c' = Zeichenkette 'k' =
   * REGION|EINHEIT|STUFE|SCHIFF|GEBAEUDE 'i' = Zahl 's' = Schiffsnummer 'b' = Gebaeudenummer 'r' =
   * Regionskoordinaten (x, y) 'u' = Einheit '+' = Wiederholung des vorangehenden Parameters '?' =
   * vorangegangener Parameter
   */
  /** Add completions for command ZaubereSpruch. */
  public void cmpltZaubereSpruch(Spell spell) {
    if (spell == null || spell.getSyntax() == null)
      return;
    if (spell.getSyntax().contains("k")) {
      addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_REGION), " "));
      addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_UNIT), " "));
      addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_LEVEL), " "));
      addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_SHIP), " "));
      addCompletion(new Completion(getOrderTranslation(EresseaConstants.OC_CASTLE), " "));
    }
    if (spell.getSyntax().contains("u")) {
      addRegionUnits(" ", 0, false);
    }
    if (spell.getSyntax().contains("b")) {
      addRegionBuildings("", " ", null, false);
    }
    if (spell.getSyntax().contains("s")) {
      addRegionShips("", " ", null, false);
    }
  }

  /** Add completions for command Zeige. */
  public void cmpltZeige() {
    addUnitItems("");
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ALL), " ",
        Completion.DEFAULT_PRIORITY - 1));
  }

  /** Add completions for command ZeigeAlle. */
  public void cmpltZeigeAlle() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_POTIONS)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_SPELLS)));
  }

  /** Add completions for command Zerstoere. */
  public void cmpltZerstoere() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ROAD), " "));
  }

  /** Add completions for command ZerstoereStrasse. */
  public void cmpltZerstoereStrasse() {
    if (region != null) {
      for (Border b : region.borders()) {
        if (Umlaut.convertUmlauts(b.getType()).equalsIgnoreCase(
            getOrderTranslation(EresseaConstants.OC_ROAD))) {
          addDirection("", b.getDirection());
        }
      }
    } else {
      addDirections("");
    }
  }

  /** Add completions for command Zuechte. */
  public void cmpltZuechte() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HORSES)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_HERBS)));
  }

  @Override
  protected String getTemp() {
    return getOrderTranslation(EresseaConstants.OC_TEMP);
  }

  protected boolean isLimitCompletions() {
    return getCompleterSettingsProvider().isLimitCompletions();
  }

}
