/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Direction;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.Units;
import magellan.library.utils.logging.Logger;

/**
 * A class for offering possible completions on incomplete orders. This class relies on the
 * <tt>OrderParser</tt> for reading input which calls the cmpltX methods of this class when it
 * encounters an incomplete order and has a <tt>OrderCompleter</tt> object registered. A
 * <tt>OrderCompleter</tt> wraps itself around a <tt>OrderParser</tt> so you do not get involved
 * with any of the cmpltX methods. They are solely called by the internal <tt>OrderParser</tt>.
 */
public class EresseaOrderCompleter implements Completer {
  private static final Logger log = Logger.getInstance(EresseaOrderCompleter.class);
  private static final Comparator<Completion> prioComp = new PrioComp();
  private OrderParser parser = null;
  private List<Completion> completions = null;
  private GameData data = null;
  private Region region = null;
  private Unit unit = null;
  private CompleterSettingsProvider completerSettingsProvider = null;

  /**
   * Returns the value of completerSettingsProvider.
   * 
   * @return Returns completerSettingsProvider.
   */
  protected CompleterSettingsProvider getCompleterSettingsProvider() {
    return completerSettingsProvider;
  }

  /**
   * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   * 
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public EresseaOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    this.completerSettingsProvider = ac;
    this.completions = new LinkedList<Completion>();
    this.data = gd;

    if (data != null) {
      parser = new EresseaOrderParser(this.data, this);
    } else {
      parser = new EresseaOrderParser(null, this);
    }
  }

  /**
   * Parses the String cmd with Unit u as context and returns possible completions if the cmd is an
   * incomplete order.
   * 
   * @param u a <tt>Unit</tt> object taken as context information for the completion decisions.
   * @param cmd a <tt>String</tt> containing the (possibly incomplete) order to parse.
   * @return a <tt>List</tt> with possible completions of the given order. If there are no proposed
   *         completions this list is empty.
   */
  public List<Completion> getCompletions(Unit u, String cmd) {
    unit = u;
    region = unit.getRegion();
    completions = new LinkedList<Completion>();
    getParser().read(new StringReader(cmd));

    List<OrderToken> tokens = getParser().getTokens();

    if ((tokens.size() > 1) && (tokens.get(tokens.size() - 2).ttype == OrderToken.TT_COMMENT)) {
      return Collections.emptyList();
    } else {
      return crop(completions, tokens);
    }
  }

  public OrderParser getParser() {
    return parser;
  }

  protected void setParser(OrderParser parser) {
    this.parser = parser;
  }

  /**
   * Filters all Completion objects from list, that do not match the last word in txt, usually the
   * order entered so far.
   */
  public List<Completion> crop(List<Completion> list, List<OrderToken> tokens) {
    List<Completion> ret = new LinkedList<Completion>();
    int start = 0;
    String stub = getStub(tokens);

    if (stub.length() > 0) {
      // filter list
      Collections.sort(list, new IgnrCsComp());
      start = Collections.binarySearch(list, stub, new IgnrCsComp());

      if (start == (-list.size() - 1)) {
        return ret;
      } else {
        if (start < 0) {
          start = Math.abs(start) - 1;
        }

        Iterator<Completion> it = list.listIterator(start);

        while (it.hasNext()) {
          Completion elem = it.next();
          String val = elem.getName();
          int len = Math.min(stub.length(), val.length());

          if (val.substring(0, len).equalsIgnoreCase(stub)) {
            ret.add(elem);
          } else {
            break;
          }
        }
      }

    } else {
      // stub.length <= 0
      ret = list;
    }
    Collections.sort(ret, EresseaOrderCompleter.prioComp);

    Completion last = null;
    for (Iterator<Completion> it = ret.iterator(); it.hasNext();) {
      Completion current = it.next();
      if (current.equals(last))
        it.remove();
      last = current;
    }

    return ret;
  }

  public void addCompletion(Completion completion) {
    completions.add(completion);
  }

  // begin of completion methods invoked by OrderParser
  protected void cmplt() {
    // add completions, that were defined by the user in the option pane
    // and can be accessed by CompleterSettingsProvider.getSelfDefinedCompletions()
    completions.addAll(completerSettingsProvider.getSelfDefinedCompletions());
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_WORK)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ATTACK), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BANNER),
        " \"\"", Completion.DEFAULT_PRIORITY, 1));
    if (unit.getFaction().getItems().size() > 0) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CLAIM), " "));
    }
    if (!unit.isHero()) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION)));
    }

    if (hasSkill(unit, EresseaConstants.S_TARNUNG)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STEAL), " "));
    }

    if (!region.buildings().isEmpty()) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SIEGE), " "));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NAME), " "));

    if (unit.getItems().size() > 0) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_USE), " "));
    }

    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE), " "));

    if (!region.buildings().isEmpty() || !region.ships().isEmpty()) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ENTER), " "));
    }

    if (unit.getGuard() == 0) {

      // special request for myself (Darcduck)
      // if an unit should guard the region it must have a combat state better than FLIEHE (5)
      // of a combat order (KÄMPFE) after all attack orders
      if ((unit.getCombatStatus() > 4) && (unit.getModifiedCombatStatus() > 4)) {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD)
            + "...", Resources.getOrderTranslation(EresseaConstants.O_GUARD) + "\n"
            + Resources.getOrderTranslation(EresseaConstants.O_COMBAT), " ", 5, 0));
      } else {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD)));
      }
    } else {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_NOT)));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT),
        Resources.getOrderTranslation(EresseaConstants.O_DEFAULT) + " '", "",
        Completion.DEFAULT_PRIORITY, 0));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EMAIL),
        Resources.getOrderTranslation(EresseaConstants.O_EMAIL), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    // we focus auf our temp generation dialog FF
    // completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_END)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RIDE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW), " "));

    if (hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 7)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH)
          + " " + Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GIVE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROUP), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HELP), " "));

    if (hasSkill(unit, EresseaConstants.S_MAGIE)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL),
          " "));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CONTACT), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT), " "));
    if (hasSkills(unit, 2)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEACH), " "));
    }
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEARN), " "));
// removed: FF SUPPLY is not supported anymore...in eressea
    // completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY),
    // " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MAKE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MOVE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NUMBER), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_OPTION), " "));
    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD), " "));

    if (hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 6)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PLANT), " "));
    }

    if (unit.getShip() != null) {
      Unit owner = unit.getShip().getModifiedOwnerUnit();

      if (owner != null) {
        if (owner.equals(unit)) {
          completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PIRACY),
              " "));
        }
      }
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PREFIX),
        Resources.getOrderTranslation(EresseaConstants.O_PREFIX), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT), " "));

    if (!(unit instanceof TempUnit)) {
      completions
          .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESERVE), " "));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROUTE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SORT), " "));

    if (hasSkill(unit, EresseaConstants.S_SPIONAGE)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SPY), " "));
    }

    // completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STIRB),
    // " ")); // don't blame me...
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HIDE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CARRY), " "));

    if (hasSkill(unit, EresseaConstants.S_STEUEREINTREIBEN)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TAX), " "));
    }

    if (hasSkill(unit, EresseaConstants.S_UNTERHALTUNG)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN),
          " "));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN), " "));

    if ((unit.getSkills() != null) && (unit.getSkills().size() > 0)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FORGET), " ",
          Completion.DEFAULT_PRIORITY + 1));
    }

    if (hasSkill(unit, EresseaConstants.S_HANDELN) && (region.maxLuxuries() > 0)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BUY), " "));
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SELL), " "));
    }

    if ((unit.getBuilding() != null) || (unit.getShip() != null)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEAVE)));
    }

    if (hasSkill(unit, EresseaConstants.S_MAGIE)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CAST), " "));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHOW), " "));

    // TODO dontknow if we should use modified owner here (GIB and ZERSTÖRE have same priority!)
    // units destroying their own building or ship or...
    if (((unit.getBuilding() != null) && (unit.getBuilding().getOwnerUnit() != null) && (unit
        .getBuilding().getOwnerUnit().equals(unit)))
        || ((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null) && (unit.getShip()
            .getOwnerUnit().equals(unit)))
        ||
        // ... vicious warriors destroying other peoples buildings or ships
        (unit.getModifiedBuilding() != null && unit.getModifiedBuilding().getOwnerUnit() != null && unit
            .getFaction() != unit.getModifiedBuilding().getOwnerUnit().getFaction())
        || (unit.getModifiedShip() != null && (unit.getModifiedShip().getOwnerUnit() == null || unit
            .getFaction() != unit.getModifiedShip().getOwnerUnit().getFaction()))) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY)));
    } else {
      if (hasSkill(unit, EresseaConstants.S_STRASSENBAU) && (region != null)
          && !region.borders().isEmpty()) {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY),
            " "));
      }
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROW), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SABOTAGE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_SHIP)));
  }

  public void cmpltAttack() {
    // special request for myself (Darcduck)
    // if an attacking unit has the wrong combat state issue the start
    // of a combat order (KÄMPFE) after all attack orders
    String battleStateOrder = "";
    if ((unit.getCombatStatus() > 3) && (unit.getModifiedCombatStatus() > 3)) {
      battleStateOrder = "\n" + Resources.getOrderTranslation(EresseaConstants.O_COMBAT) + " ";
    }

    // collects spy-units to create a set of attack-orders against all spies later
    List<Unit> spies = new LinkedList<Unit>();

    // collects enemy units
    // maps faction ids to a List of unit ids
    // to create a set of attack-orders against total factions later
    Map<ID, List<Unit>> unitList = new Hashtable<ID, List<Unit>>();

    for (Iterator<Unit> iter = unit.getRegion().units().iterator(); iter.hasNext();) {
      Unit curUnit = iter.next();

      if (curUnit.isSpy()) {
        spies.add(curUnit);
        addUnit(curUnit, battleStateOrder);
      } else {
        Faction f = curUnit.getFaction();

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
      Iterator<Unit> i = spies.iterator();
      Unit curUnit = i.next();
      String enemyUnits = curUnit.getID().toString() + " ;" + curUnit.getName();

      while (i.hasNext()) {
        curUnit = i.next();
        enemyUnits +=
            ("\n" + Resources.getOrderTranslation(EresseaConstants.O_ATTACK) + " "
                + curUnit.getID().toString() + " ;" + curUnit.getName());
      }
      enemyUnits += battleStateOrder;
      completions.add(new Completion(Resources
          .get("gamebinding.eressea.eresseaordercompleter.spies"), enemyUnits, "", 5, 0));
    }

    for (Iterator<ID> iter = unitList.keySet().iterator(); iter.hasNext();) {
      ID fID = iter.next();
      Iterator<Unit> i = (unitList.get(fID)).iterator();
      Unit curUnit = i.next();
      String enemyUnits = curUnit.getID().toString() + " ;" + curUnit.getName();

      while (i.hasNext()) {
        curUnit = i.next();
        enemyUnits +=
            ("\n" + Resources.getOrderTranslation(EresseaConstants.O_ATTACK) + " "
                + curUnit.getID().toString() + " ;" + curUnit.getName());
      }
      enemyUnits += battleStateOrder;
      completions.add(new Completion(data.getFaction(fID).getName() + " (" + fID.toString() + ")",
          enemyUnits, "", 6, 0));
      completions.add(new Completion(fID.toString() + " (" + data.getFaction(fID).getName() + ")",
          enemyUnits, "", 7, 0));
    }
  }

  public void cmpltBeklaue() {
    addEnemyUnits("");
  }

  public void cmpltBelagere() {
    if ((data != null) && (unit != null) && (region != null)) {
      Faction ownerFaction = unit.getFaction();
      Iterator buildings = region.buildings().iterator();

      while (buildings.hasNext()) {
        Building b = (Building) buildings.next();

        if (data.rules.getGameSpecificStuff().getGameSpecificRules().isCastle(b.getType())
            && (b.getModifiedOwnerUnit() == null || b.getModifiedOwnerUnit().getFaction().equals(
                ownerFaction) == false)) {
          addNamed(b, "", 0, true);
        }
      }
    }
  }

  public void cmpltBenenne() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(Resources
        .getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING), " "));
    completions.add(new Completion(
        Resources.getOrderTranslation(EresseaConstants.O_FOREIGNFACTION), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNSHIP),
        " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNUNIT),
        " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
        " \"\"", Completion.DEFAULT_PRIORITY, 1));

    // use old owner unit (BENENNE before GIB)
    if ((unit.getBuilding() != null) && unit.getBuilding().getOwnerUnit().equals(unit)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
    }

    if ((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null)
        && unit.getShip().getOwnerUnit().equals(unit)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
    }
  }

  public void cmpltBenenneFremdes(OrderToken token) {
    if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNUNIT)))
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " "));
    if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING)))
      completions
          .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE), " "));
    if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNFACTION)))
      completions
          .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION), " "));
    if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNSHIP)))
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
  }

  public void cmpltBenenneFremdeEinheit() {
    if ((data != null) && (unit != null) && (region != null)) {
      Faction ownerFaction = unit.getFaction();
      Iterator units = region.units().iterator();

      while (units.hasNext()) {
        Unit u = (Unit) units.next();

        if (u.getFaction().equals(ownerFaction) == false) {
          addUnit(u, " \"\"", 1);
        }
      }
    }
  }

  public void cmpltBenenneFremdesGebaeude() {
    if ((data != null) && (unit != null) && (region != null)) {
      Faction ownerFaction = unit.getFaction();
      Iterator buildings = region.buildings().iterator();

      while (buildings.hasNext()) {
        Building b = (Building) buildings.next();

        // use old owner unit (BENENNE before GIB)
        if ((b.getOwnerUnit() != null)
            && (b.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
          addNamed(b, " \"\"", 1, false);
        }
      }
    }
  }

  public void cmpltBenenneFremdePartei() {
    if ((data != null) && (data.factions() != null) && (unit != null)) {
      Faction ownerFaction = unit.getFaction();
      Iterator factions = data.factions().values().iterator();

      while (factions.hasNext()) {
        Faction f = (Faction) factions.next();

        if (f.equals(ownerFaction) == false) {
          addNamed(f, " \"\"", 1, false);
        }
      }
    }
  }

  public void cmpltBenenneFremdesSchiff() {
    if ((data != null) && (unit != null) && (region != null)) {
      Faction ownerFaction = unit.getFaction();
      Iterator ships = region.ships().iterator();

      while (ships.hasNext()) {
        Ship s = (Ship) ships.next();

        // use old owner unit (BENENNE before GIB)
        if ((s.getOwnerUnit() != null)
            && (s.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
          String id = s.getID().toString();
          String name = s.getName();
          completions.add(new Completion(s.getType().getName() + " " + name + " (" + id + ")", id,
              " \"\"", Completion.DEFAULT_PRIORITY - 1, 1));
          completions.add(new Completion(id + " (" + s.getType().getName() + " " + name + ")", id,
              " \"\"", Completion.DEFAULT_PRIORITY, 1));
        }
      }
    }
  }

  public void cmpltDescription() {
    completions.add(new Completion("\"\"", "\"\"", " ", Completion.DEFAULT_PRIORITY, 2));
  }

  /**
   * Ergänzt alle Items der Faction in der Region, deren Anzahl größer als amount ist
   * 
   * @param amount
   */
  public void cmpltBenutze(int amount) {
    // addUnitItems("");
    // addFactionItems("");
    addRegionItemsFaction("", amount);
  }

  public void cmpltBeanspruche() {
    for (Iterator<Item> iter = unit.getFaction().getItems().iterator(); iter.hasNext();) {
      Item actItem = iter.next();
      completions.add(new Completion(actItem.getName()));
    }
  }

  public void cmpltBeschreibe() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " \"\"",
        Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PRIVATE),
        " \"\"", Completion.DEFAULT_PRIORITY, 1));

    // use old owner unit (BENENNE before GIB)
    if ((unit.getBuilding() != null) && unit.getBuilding().getOwnerUnit().equals(unit)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
    }

    if ((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null)
        && unit.getShip().getOwnerUnit().equals(unit)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
          " \"\"", Completion.DEFAULT_PRIORITY, 1));
    }
  }

  public void cmpltBetrete() {
    if (region.buildings().size() > 0) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE), " ",
          7));
    }
    addRegionBuildings(Resources.getOrderTranslation(EresseaConstants.O_CASTLE) + " ", " ", unit
        .getBuilding(), true);

    if (region.ships().size() > 0) {
      completions
          .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " ", 7));
    }
    // FIXME doesn't work as intended?
    addRegionShips(Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " ", " ", unit
        .getShip(), true);
  }

  public void cmpltBetreteBurg() {
    for (Iterator iter = region.buildings().iterator(); iter.hasNext();) {
      UnitContainer uc = (UnitContainer) iter.next();

      if (!uc.equals(unit.getBuilding())) {
        addNamed(uc, "", 0, true);
      }
    }
  }

  public void cmpltBetreteSchiff() {
    for (Iterator iter = region.ships().iterator(); iter.hasNext();) {
      UnitContainer uc = (UnitContainer) iter.next();

      if (!uc.equals(unit.getShip())) {
        addNamed(uc, "", 0, true);
      }
    }
  }

  public void cmpltBotschaft() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
        " \"\"", Completion.DEFAULT_PRIORITY, 1));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
  }

  public void cmpltBotschaftEinheit(boolean tempToken) {
    addRegionUnits(" \"\"", 1, tempToken);
  }

  public void cmpltBotschaftPartei() {
    addOtherFactions(" \"\"", 1, false);
  }

  public void cmpltBotschaftGebaeude() {
    for (Building uc : region.buildings()) {
      addNamed(uc, " \"\"", 1, false);
    }
  }

  public void cmpltBotschaftGebaeudeID() {
    completions.add(new Completion(" \"\"", " \"\"", "", Completion.DEFAULT_PRIORITY, 1));
  }

  public void cmpltBotschaftSchiff() {
    for (Ship s : region.ships()) {
      addNamed(s, " \"\"", 1, false);
    }
  }

  public void cmpltBotschaftSchiffID() {
    completions.add(new Completion(" \"\"", " \"\"", "", Completion.DEFAULT_PRIORITY, 1));
  }

  public void cmpltFahre(boolean tempToken) {
    addRegionUnits("", tempToken);
  }

  public void fixWhitespace() {
    List<Completion> oldList = new LinkedList<Completion>(completions);
    completions.clear();
    for (Completion c : oldList) {
      completions.add(new Completion(c.getName().replaceAll(" ", "~"), c.getValue().replaceAll(" ",
          "~"), c.getPostfix(), c.getPriority(), c.getCursorOffset()));
    }
  }

  public void fixQuotes(OrderToken openingToken, OrderToken contentToken, OrderToken closingToken,
      boolean preferQuotes, boolean forceQuotes, boolean doClose, char preferredQuote) {
    List<Completion> oldList = new ArrayList<Completion>(completions);
    completions.clear();
    for (Completion c : oldList) {
      OrderTokenizer nameTokenizer = new OrderTokenizer(new StringReader(c.getName()));
      String newName = c.getName();
      if (openingToken != null || forceQuotes)
        newName =
            fixQuotes(nameTokenizer, openingToken, contentToken, closingToken, preferQuotes,
                forceQuotes, doClose, preferredQuote);
      OrderTokenizer valueTokenizer = new OrderTokenizer(new StringReader(c.getValue()));
      String newValue =
          fixQuotes(valueTokenizer, openingToken, contentToken, closingToken, preferQuotes,
              forceQuotes, doClose, preferredQuote);
      completions.add(new Completion(newName, newValue, c.getPostfix(), c.getPriority(), c
          .getCursorOffset()));
    }
  }

  private String fixQuotes(OrderTokenizer innerTokenizer, OrderToken openingToken,
      OrderToken contentToken, OrderToken closingToken, boolean preferQuotes, boolean forceQuotes,
      boolean doClose, char preferredQuote) {

    StringBuffer result = new StringBuffer();

    // see if first inner token is a quote, if not, add it to inner tokens
    List<OrderToken> innerTokens = new LinkedList<OrderToken>();
    OrderToken innerQuote = innerTokenizer.getNextToken();
    if (innerQuote.ttype != OrderToken.TT_OPENING_QUOTE) {
      innerTokens.add(innerQuote);
      innerQuote = null;
    }

    // assign which quote is used
    String insertedQuote;
    if (openingToken != null)
      insertedQuote = openingToken.getText();
    else
      insertedQuote = "" + preferredQuote;

    // add rest of inner tokens
    for (OrderToken currentToken = innerTokenizer.getNextToken(); currentToken.ttype != OrderToken.TT_EOC
        && (currentToken.ttype != OrderToken.TT_EOC || (currentToken.ttype == OrderToken.TT_EOC && !currentToken
            .getText().equals(insertedQuote))); currentToken = innerTokenizer.getNextToken()) {
      if (currentToken.ttype != OrderToken.TT_CLOSING_QUOTE
          || (currentToken.ttype == OrderToken.TT_CLOSING_QUOTE && !currentToken.getText().equals(
              insertedQuote)))
        innerTokens.add(currentToken);
    }

    // append opening quote if needed
    int lastLength = 0;
    if (openingToken != null || innerQuote != null
        || ((innerTokens.size() > 1 && preferQuotes) || forceQuotes)) {
      result.append(insertedQuote);
      lastLength = insertedQuote.length();
    }

    // append content
    for (OrderToken t : innerTokens) {
      if (t.ttype != OrderToken.TT_EOC) {
        for (int i =
            result.length() - lastLength + (innerQuote == null ? 0 : innerQuote.getText().length()); i < t
            .getStart(); ++i) {
          if (preferQuotes) {
            result.append(" ");
          } else {
            result.append('~');
          }
        }
        result.append(t.getText());
      }
    }

    // append closing quote if needed
    if (doClose
        && (openingToken != null || innerQuote != null || (innerTokens.size() > 2 && preferQuotes) || forceQuotes)) {
      result.append(openingToken != null ? openingToken.getText() : preferredQuote);
    }

    return result.toString();
  }

  public void cmplFinalQuote(char quote) {
    List<Completion> oldList = new ArrayList<Completion>(completions);
    completions.clear();
    for (Completion c : oldList) {
      if (c.getValue().length() > 1 && c.getValue().charAt(c.getValue().length() - 1) == quote
          && c.getValue().charAt(c.getValue().length() - 2) != '\\')
        completions.add(c);
      else
        completions.add(new Completion(c.getName() + quote, c.getValue().trim() + quote, c
            .getPostfix(), c.getPriority(), c.getCursorOffset()));
    }
  }

  public void cmplOpeningQuote(char quote, boolean cmplName) {
    List<Completion> oldList = new LinkedList<Completion>(completions);
    completions.clear();
    for (Completion c : oldList) {
      completions.add(new Completion(cmplName ? quote + c.getName() : c.getName(), quote
          + c.getValue(), c.getPostfix(), c.getPriority(), c.getCursorOffset()));
    }
  }

  public void clear() {
    completions.clear();
  }

  public void cmpltFolge() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
  }

  public void cmpltFolgeEinheit(boolean tempToken) {
    addRegionUnits("", tempToken);
  }

  public void cmpltFolgeSchiff() {
    if (region != null) {
      Iterator i = region.ships().iterator();

      while (i.hasNext()) {
        Ship s = (Ship) i.next();

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
        for (Iterator<Message> messages = region.getTravelThruShips().iterator(); messages
            .hasNext();) {
          String text = messages.next().getText();

          // try to match a ship id in the text
          // TODO: use message type
          String number = "\\w+";
          Matcher matcher = Pattern.compile("\\((" + number + ")\\)").matcher(text);
          while (matcher.find()) {
            if (1 <= matcher.groupCount()) {
              String id = matcher.group(1);
              completions.add(new Completion(text, id, " ", Completion.DEFAULT_PRIORITY - 1));
              completions.add(new Completion(id + " (" + text + ")", id, " "));
            }
          }
        }
      }
    }
  }

  public void cmpltForsche() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
  }

  public void cmpltGruppe() {
    if ((unit != null) && (unit.getFaction() != null) && (unit.getFaction().getGroups() != null)) {
      for (Iterator iter = unit.getFaction().getGroups().values().iterator(); iter.hasNext();) {
        Group g = (Group) iter.next();
        completions.add(new Completion(g.getName(), ""));
      }
    }
  }

  public void cmpltGib() {
    addRegionUnits(" ", false);
    addRegionShipCommanders(" ");
    addRegionBuildingOwners(" ");
  }

  public void cmpltGibUID(boolean tempToken) {
    if (tempToken) {
      addRegionUnits(" ", tempToken);
      return;
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT)));

    /*
     * if (unit.getBuilding() != null && unit.equals(unit.getBuilding().getOwnerUnit()) ||
     * unit.getShip() != null && unit.equals(unit.getShip().getOwnerUnit())) {
     */
    // if we do not move into or stay in a ship or building we can't give control to another unit
    if ((unit.getModifiedShip() != null) || (unit.getModifiedBuilding() != null)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CONTROL)));
    }

    // }
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EACH) + " "
        + Resources.get("gamebinding.eressea.eresseaordercompleter.amount"), Resources
        .getOrderTranslation(EresseaConstants.O_EACH), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EACH) + " 1",
        Resources.getOrderTranslation(EresseaConstants.O_EACH) + " 1", " "));
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  public void cmpltGibJe() {
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  /**
   * For multiple-line-completion like the creation of give-orders for the resources of an item it
   * is necessary to get the unit's id and the amount to be given. They are given as parameters:
   * 
   * @param uid the unit's id
   * @param i the amount
   * @param persons Whether to add "PERSONEN" or not
   */
  public void cmpltGibUIDAmount(UnitID uid, int i, boolean persons) {
    addUnitItems(i, "");

    if ((i != 0) && (uid != null)) {
      // add completions, that create multiple Give-Orders for the resources of an item
      for (Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
        ItemType iType = (ItemType) iter.next();

        if (iType.getResources() != null && iType.getResources().hasNext() // necessary resources
            // are known
            && checkForMaterials(iType.getResources(), i)) { // necessary resources are available

          boolean suggest = true;
          int loopCount = 0;
          StringBuffer order = new StringBuffer();

          for (Iterator iterator = iType.getResources(); iterator.hasNext() && suggest; loopCount++) {
            Item resource = (Item) iterator.next();

            if ((loopCount == 0) && !iterator.hasNext()) {
              // only one resource is necessary for this ItemType
              // don't create a completion to give the resource for this ItemType
              suggest = false;
            } else {

              if (order.length() == 0) {
                order.append(resource.getOrderName());
              } else {
                order.append("\n").append(Resources.getOrderTranslation(EresseaConstants.O_GIVE))
                    .append(" ").append(uid.toString()).append(" ").append(i).append(" ").append(
                        resource.getOrderName());
              }
            }
          }

          if (suggest) {
            completions.add(new Completion("R-" + iType.getOrderName(), order.toString(), "",
                Completion.DEFAULT_PRIORITY + 1));
          }
        }
      }
      /**
       * Add multiple GIVE orders for if we enter ALL i.e. assume the unit has 200 sword, shild,
       * plate and 80 horses GIVE abcd 100 [ALL] will complete to GIVE abcd 100 sword GIVE abcd 100
       * shield GIVE abcd 100 plate as we have not at least 100 horses. This is perfect to split
       * units
       */
      String order = "";
      String tounit =
          (uid.intValue() >= 0) ? uid.toString() : Resources
              .getOrderTranslation(EresseaConstants.O_TEMP)
              + " " + uid.toString();
      if (persons && (unit.getPersons() >= i)) {
        order = Resources.getOrderTranslation(EresseaConstants.O_MEN);
      }
      for (Item item : unit.getItems()) {
        if (item.getAmount() >= i) {

          if ("".equals(order)) {
            order = item.getOrderName();
          } else {
            order +=
                ("\n" + Resources.getOrderTranslation(EresseaConstants.O_GIVE) + " " + tounit + " "
                    + i + " " + item.getOrderName());
          }
        }
      }
      if (!"".equals(order)) {
        completions.add(new Completion(Resources
            .get("gamebinding.eressea.eresseaordercompleter.allall"), order, ""));
      }
    }

    if (persons) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MEN), (unit
          .getPersons() >= i) ? 0 : Completion.DEFAULT_PRIORITY + 1));
    }

  }

  public void cmpltGibUIDAmount() {
    cmpltGibUIDAmount(null, 0, true);
  }

  public void cmpltGibUIDAlles() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MEN)));
    addUnitItems("");
  }

  public void cmpltHelfe() {
    addOtherFactions(" ", 0, false);
  }

  public void cmpltHelfeFID() {
    for (Iterator it = getData().rules.getAllianceCategoryIterator(); it.hasNext();) {
      AllianceCategory all = (AllianceCategory) it.next();
      completions.add(new Completion(Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX
          + all.getName())));
    }
  }

  public void cmpltHelfeFIDModifier() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
  }

  public void cmpltKaempfe() {
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
    if ((unit == null) || ((unit.getCombatStatus() != 0) && (unit.getCombatStatus() != -1))) {
      completions.add(new Completion(Resources
          .getOrderTranslation(EresseaConstants.O_COMBAT_AGGRESSIVE), "", unit.getCombatStatus()));
    }

    if ((unit == null) || (unit.getCombatStatus() != 2)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_REAR),
          "", Math.abs(unit.getCombatStatus() - 2)));
    }

    if ((unit == null) || (unit.getCombatStatus() != 3)) {
      completions.add(new Completion(Resources
          .getOrderTranslation(EresseaConstants.O_COMBAT_DEFENSIVE), "", Math.abs(unit
          .getCombatStatus() - 3)));
    }

    if ((unit == null) || (unit.getCombatStatus() != 4)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_NOT),
          "", Math.abs(unit.getCombatStatus() - 4) + attackMalus));
    }

    if ((unit == null) || (unit.getCombatStatus() != 5)) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FLEE),
          "", Math.abs(unit.getCombatStatus() - 5) + guardMalus + attackMalus));
    }

    // ACHTUNG!!!!
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_HELP),
        " "));
  }

  public void cmpltKaempfeHelfe() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_NOT)));
  }

  public void cmpltKaufe() {
    completions.add(new Completion(region.maxLuxuries() + "", " "));
  }

  public void cmpltKaufeAmount() {
    String item = null;

    if (region.getPrices() != null) {
      for (Iterator<LuxuryPrice> iter = region.getPrices().values().iterator(); iter.hasNext();) {
        LuxuryPrice p = iter.next();

        if (p.getPrice() < 0) {
          item = p.getItemType().getName();

          break;
        }
      }
    }

    if (item == null) {
      if ((data != null) && (data.rules != null)) {
        ItemCategory luxCat = data.rules.getItemCategory(EresseaConstants.C_LUXURIES);

        if (luxCat != null) {
          for (Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
            ItemType t = (ItemType) iter.next();

            if (t.getCategory().equals(luxCat)) {
              completions.add(new Completion(t.getOrderName()));
            }
          }
        }
      }
    } else {
      completions.add(new Completion(item));
    }
  }

  public void cmpltKampfzauber(boolean modifiers, String opening, String closing) {
    if ((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
      if (modifiers) {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
            " ", Completion.DEFAULT_PRIORITY - 1));

// if ((unit.getCombatSpells() != null) && (unit.getCombatSpells().size() > 0)) {
// completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT), "",
// Completion.DEFAULT_PRIORITY - 1));
// }
      }

      addFilteredSpells(unit, false, region.getType().equals(
          data.rules.getRegionType(EresseaConstants.RT_OCEAN)), true, opening, closing);
    }
  }

  public void cmpltKampfzauberSpell() {
    // at this point it is not important if the unit has combat spells, so we don't check for it.
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT), ""));
  }

  public void cmpltKontaktiere() {
    Alliance alliance =
        new Alliance(unit.getFaction(), EresseaConstants.A_GIVE | EresseaConstants.A_GUARD);
    addNotAlliedUnits(alliance, "");
  }

  public void cmpltLehre(boolean tempToken) {
    addRegionUnits(" ", tempToken);
  }

  public void cmpltLerne() {
    if ((data != null) && (data.rules != null)) {
      for (Iterator iter = data.rules.getSkillTypeIterator(); iter.hasNext();) {
        SkillType t = (SkillType) iter.next();
        int cost = getSkillCost(t, unit);
        // add quotes if needed
        String name = t.getName().replace(' ', '~');

        if (cost > 0) {
          completions.add(new Completion(name, " " + cost));
        } else {
          completions.add(new Completion(name));
        }
      }
    }
  }

  public void cmpltLerneTalent(SkillType t) {
    if (data != null && data.rules != null && t != null) {
      int cost = getSkillCost(t, unit);

      if (cost > 0) {
        completions.add(new Completion(Integer.toString(cost)));
      }
      if (t.equals(data.rules.getSkillType(EresseaConstants.S_MAGIE))
          && (unit.getFaction() == null || unit.getFaction().getSpellSchool() == null)) {
        completions.add(new Completion("\""
            + Resources.get("gamebinding.eressea.eresseaordercompleter.magicarea") + "\"", "\"\"",
            "", Completion.DEFAULT_PRIORITY, 1));
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
  public int getSkillCost(SkillType skillType, Unit someUnit) {
    int cost = 0;
    int c2 = 0;

    Skill sk = someUnit.getSkill(skillType);
    if (sk == null)
      c2 = skillType.getCost(1);
    else
      c2 = skillType.getCost(1 + sk.getLevel() - sk.getModifier(someUnit));

    if (skillType.getID().equals(EresseaConstants.S_TAKTIK)
        || skillType.getID().equals(EresseaConstants.S_KRAEUTERKUNDE)
        || skillType.getID().equals(EresseaConstants.S_ALCHEMIE)) {
      cost = 200;
    } else if (skillType.getID().equals(EresseaConstants.S_SPIONAGE)) {
      cost = 100;
    } else if (skillType.getID().equals(EresseaConstants.S_MAGIE)) {
      // get magiclevel without modifier
      int level = 0;
      Skill skill = (someUnit != null) ? someUnit.getSkill(skillType) : null;

      if (skill != null && someUnit != null) {
        if (skill.noSkillPoints()) {
          level = skill.getLevel() - skill.getModifier(someUnit);
        } else {
          int days = someUnit.getSkill(skillType).getPointsPerPerson();
          level = (int) Math.floor(Math.sqrt((days / 15.0) + 0.25) - 0.5);
        }
      }

      int nextLevel = level + 1;
      cost = (int) (50 + ((50 * (1 + nextLevel) * (nextLevel)) / 2.0));
    }

    if (someUnit != null) {
      if ((someUnit.getModifiedBuilding() != null)
          && someUnit.getModifiedBuilding().getType().equals(
              data.rules.getBuildingType(EresseaConstants.B_ACADEMY))) {
        if (cost == 0) {
          cost = 50;
          c2 = 50;
        } else {
          cost *= 2;
          c2 *= 2;
        }
      }

      cost *= Math.max(1, someUnit.getModifiedPersons());
      c2 *= Math.max(1, someUnit.getModifiedPersons());
    }

    if (c2 != cost && data.getGameSpecificStuff().getName().equalsIgnoreCase("eressea")) {
      log.error("assertion error getSkillCost()");
    }
    return c2;
  }

  public void cmpltLiefere() {
    cmpltGib();
  }

  public void cmpltLocale() {
    completions.add(new Completion("deutsch", "\"de\"", ""));
    completions.add(new Completion("english", "\"en\"", ""));
  }

  public void cmpltMache() {
    // we focus on our temp creation dialog
    // completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEMP),
    // " "));
    cmpltMacheAmount();
  }

  public void cmpltMacheAmount() {
    // buildings
    if (hasSkill(unit, EresseaConstants.S_BURGENBAU)) {
      if ((data != null) && (data.rules != null)) {
        for (Iterator iter = data.rules.getBuildingTypeIterator(); iter.hasNext();) {
          BuildingType t = (BuildingType) iter.next();

          if ((t instanceof CastleType == false)
              && t.containsRegionType(region.getRegionType())
              && hasSkill(unit, EresseaConstants.S_BURGENBAU, t.getBuildSkillLevel())
              && (!completerSettingsProvider.getLimitMakeCompletion() || checkForMaterials(t
                  .getRawMaterials().iterator()))) {
            completions.add(new Completion(t.getName(), " "));
          }
        }
      }

      if (!completerSettingsProvider.getLimitMakeCompletion()
          || (Units.getContainerPrivilegedUnitItem(region, data.rules
              .getItemType(EresseaConstants.I_USTONE)) != null)) {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
            " "));
      }
    }

    // ships
    if (hasSkill(unit, EresseaConstants.S_SCHIFFBAU)
        && (!completerSettingsProvider.getLimitMakeCompletion() || (Units
            .getContainerPrivilegedUnitItem(region, data.rules.getItemType(EresseaConstants.I_WOOD)) != null))) {
      if ((data != null) && (data.rules != null)) {
        for (Iterator iter = data.rules.getShipTypeIterator(); iter.hasNext();) {
          ShipType t = (ShipType) iter.next();

          if (hasSkill(unit, EresseaConstants.S_SCHIFFBAU, t.getBuildSkillLevel())) {
            completions.add(new Completion(t.getName(), " "));
          }
        }
      }

      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
    }

    // streets
    // check, if there is the necessary roadsupportbuilding
    BuildingType b = region.getRegionType().getRoadSupportBuilding();
    boolean canMake = false;

    if (b == null) {
      canMake = true;
    } else {
      for (Iterator iter = region.buildings().iterator(); iter.hasNext() && !canMake;) {
        if (((Building) iter.next()).getBuildingType().equals(b)) {
          canMake = true;
        }
      }
    }

    if (hasSkill(unit, EresseaConstants.S_STRASSENBAU)
        && (!completerSettingsProvider.getLimitMakeCompletion() || (Units
            .getContainerPrivilegedUnitItem(region, data.rules
                .getItemType(EresseaConstants.I_USTONE)) != null)) && canMake) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROAD), " "));
    }

    // items
    for (Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
      ItemType itemType = (ItemType) iter.next();
      canMake = true;

      if (itemType.getMakeSkill() == null) {
        // some items can not be made like dragonblood or magic artefacts
        canMake = false;
      } else if (!hasSkill(unit, itemType.getMakeSkill().getSkillType().getID(), itemType
          .getMakeSkill().getLevel())) {
        canMake = false;
      } else if (completerSettingsProvider.getLimitMakeCompletion()
          && !checkForMaterials(itemType.getResources())) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_UIRON))
          && (region.getIron() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_ULAEN))
          && (region.getLaen() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_WOOD)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
          // also take care of mallorn flag
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_UMALLORN)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || !region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_UHORSE)) 
          && (region.getHorses() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.rules.getItemType(EresseaConstants.I_USTONE))
          && (region.getStones() <= 0)) {
        canMake = false;
      }

      if (canMake) {
        addItem(itemType, "");
      }
    }
  }

  public void cmpltMacheTemp() {
  }

  public void cmpltMacheTempID() {
    completions.add(new Completion("\"\""));
  }

  public void cmpltMacheBurg() {
    Iterator i = region.buildings().iterator();

    while ((i != null) && i.hasNext()) {
      Building b = (Building) i.next();
      BuildingType type = b.getBuildingType();

      if (type instanceof CastleType || (type.getMaxSize() != b.getSize())) {
        addNamed(b, "", 0, true);
      }
    }
  }

  public void cmpltMacheBuilding(String typeName) {
    // TODO(pavkovic): korrigieren!!! Hier soll eigentlich das Gebäude über den
    // übersetzten Namen gefunden werden!!!
    // BuildingType type = ((Eressea) data.rules).getBuildingType(typeName);
    BuildingType type = data.rules.getBuildingType(StringID.create(typeName));

    if (type != null) {
      Iterator i = region.buildings().iterator();

      while ((i != null) && i.hasNext()) {
        UnitContainer uc = (UnitContainer) i.next();

        if (uc.getType().equals(type)) {
          addNamed(uc, "", 0, true);
        }
      }
    }
  }

  public void cmpltMacheSchiff() {
    Faction ownerFaction = unit.getFaction();
    Iterator i = region.ships().iterator();

    while ((i != null) && i.hasNext()) {
      Ship s = (Ship) i.next();

      if ((s.getModifiedOwnerUnit() != null)
          && ownerFaction.equals(s.getModifiedOwnerUnit().getFaction())) {
        addNamed(s, "", Completion.DEFAULT_PRIORITY - 2, 0, true);
      } else {
        addNamed(s, "", Completion.DEFAULT_PRIORITY, 0, true);
      }
    }
  }

  public void cmpltMacheStrasse() {
    addDirections("");
  }

  public void cmpltNach() {
    addDirections(" ");
    addSurroundingRegions(getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(unit, true), " ");
  }

  public void cmpltNeustart() {
    completions.add(new Completion(Resources.get("gamebinding.eressea.eresseaordercompleter.race"),
        "", ""));
  }

  public void cmpltNummer() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE), " "));
  }

  public void cmpltOption() {
    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ADDRESSES), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REPORT), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BZIP2), " "));
    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMPUTER), " "));
    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ITEMPOOL), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SCORE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SILVERPOOL),
        " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STATISTICS),
        " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ZIPPED), " "));
    completions
        .add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEMPLATE), " "));
  }

  public void cmpltOptionOption() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
  }

  public void cmpltPflanze() {
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  public void cmpltPflanze(int minAmount) {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TREES)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SEED)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MALLORNSEED)));
  }

  public void cmpltPiraterie() {
    addOtherFactions(" ", 0, false);
  }

  public void cmpltPiraterieFID() {
    cmpltPiraterie();
  }

  public void cmpltPraefix() {
    completions.add(new Completion("\"\"", "\"\"", "", Completion.DEFAULT_PRIORITY, 1));
  }

  public void cmpltRekrutiere() {
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  public void cmpltReserviere() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EACH), " "));
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));

    // reserve all items that the unit has
    for (Item item : unit.getItems()) {
      completions.add(new Completion(item.getName() + " "
          + Resources.get("gamebinding.eressea.eresseaordercompleter.allamount"), item.getAmount()
          + " " + item.getName(), ""));
    }

    addMaxReserve(unit);

  }

  /**
   * Reserve as much of the item as the unit can carry.
   * 
   * @param otherUnit
   */
  private void addMaxReserve(Unit otherUnit) {
    int modLoad = getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(otherUnit);
    ItemType carts = data.rules.getItemType(EresseaConstants.I_CART);
    int maxOnFoot = getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(otherUnit);
    int maxOnHorse = getGameSpecificStuff().getMovementEvaluator().getPayloadOnHorse(otherUnit);

    for (Iterator iter = Units.getContainerAllUnitItems(otherUnit.getRegion()).iterator(); iter
        .hasNext();) {
      Item item = (Item) iter.next();
      ItemType type = item.getItemType();

      if ((type.getWeight() > 0.0) && !type.isHorse() && !type.equals(carts)) {
        int weight = (int) (type.getWeight() * 100);
        if (weight > 0) {
          if ((maxOnFoot - modLoad) > 0) {
            completions.add(new Completion(type.getName() + " "
                + Resources.get("gamebinding.eressea.eresseaordercompleter.maxfootamount"),
                (maxOnFoot - modLoad) / weight + " " + type.getOrderName(), ""));
          }
          if ((maxOnHorse - modLoad) > 0) {
            completions.add(new Completion(type.getName() + " "
                + Resources.get("gamebinding.eressea.eresseaordercompleter.maxhorseamount"),
                (maxOnHorse - modLoad) / weight + " " + type.getOrderName(), ""));
          }
        }
      }
    }
  }

  public void cmpltReserviereJe() {
    completions.add(new Completion(Resources
        .get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
  }

  public void cmpltReserviereAmount() {
    Faction f = unit.getFaction();
    boolean silverPool = false;
    boolean materialPool = false;
    if (f.getOptions() != null) {
      silverPool = f.getOptions().isActive(StringID.create(EresseaConstants.O_SILVERPOOL));
      materialPool = f.getOptions().isActive(StringID.create(EresseaConstants.O_ITEMPOOL));
    }

    if (!silverPool && !materialPool) {
      addUnitItems("");
    } else if (silverPool && !materialPool) {
      addUnitItems("");

      // if unit doesn't have silver, but poolsilver is available
      if ((unit.getItem(data.rules.getItemType(EresseaConstants.I_USILVER)) == null)
          && (region.getItem(data.rules.getItemType(EresseaConstants.I_USILVER)) != null)) {
        completions.add(new Completion(data.rules.getItemType(EresseaConstants.I_USILVER)
            .getOrderName()));
      }
    } else if (!silverPool && materialPool) {
      for (Iterator<Item> iter = region.items().iterator(); iter.hasNext();) {
        Item item = iter.next();

        if (silverPool
            || (item.getItemType() != data.rules.getItemType(EresseaConstants.I_USILVER))
            || (unit.getItem(data.rules.getItemType(EresseaConstants.I_USILVER)) != null)) {
          String name = item.getName();
          String quotedName = name;

          if ((name.indexOf(" ") > -1)) {
            quotedName = "\"" + name + "\"";
          }

          completions.add(new Completion(name, quotedName, ""));
        }
      }
    } else {
      for (Iterator<Item> iter = region.items().iterator(); iter.hasNext();) {
        Item item = iter.next();

        // silver only if silverpool activated or unit has silver
        if (silverPool
            || (item.getItemType() != data.rules.getItemType(EresseaConstants.I_USILVER))
            || (unit.getItem(data.rules.getItemType(EresseaConstants.I_USILVER)) != null)) {
          String name = item.getName();
          String quotedName = name;

          if ((name.indexOf(" ") > -1)) {
            quotedName = "\"" + name + "\"";
          }

          completions.add(new Completion(name, quotedName, ""));
        }
      }
    }
  }

  public void cmpltRoute() {
    addDirections(" ");
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PAUSE), " "));
  }

  public void cmpltSabotiere() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP)));
  }

  public void cmpltSortiere() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BEFORE), " "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AFTER), " "));
  }

  public void cmpltSortiereVor() {
    if (unit.getBuilding() != null) {
      addSortiereUnits(unit, unit.getBuilding(), false);
    } else if (unit.getShip() != null) {
      addSortiereUnits(unit, unit.getShip(), false);
    } else {
      for (Iterator iter = region.units().iterator(); iter.hasNext();) {
        Unit u = (Unit) iter.next();

        if (unit.getFaction().equals(u.getFaction()) && (u.getBuilding() == null)
            && (u.getShip() == null)) {
          if (!u.equals(unit)) {
            addUnit(u, "");
          }
        }
      }
    }
  }

  public void cmpltSortiereHinter() {
    if (unit.getBuilding() != null) {
      addSortiereUnits(unit, unit.getBuilding(), true);
    } else if (unit.getShip() != null) {
      addSortiereUnits(unit, unit.getShip(), true);
    } else {
      for (Iterator iter = region.units().iterator(); iter.hasNext();) {
        Unit u = (Unit) iter.next();

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
    for (Unit currentUnit : c.units()) {
      if (u.getFaction().equals(currentUnit.getFaction())
          && (c.equals(currentUnit.getBuilding()) || c.equals(currentUnit.getShip()))) {
        if (!u.equals(currentUnit) && (addOwner || !currentUnit.equals(c.getModifiedOwnerUnit()))) {
          addUnit(currentUnit, "");
        }
      }
    }
  }

  public void cmpltSpioniere() {
    addEnemyUnits("");
  }

  public void cmpltStirb() {
    if (Units.isPrivilegedAndNoSpy(unit)) {
      completions.add(new Completion('"' + Resources
          .get("gamebinding.eressea.eresseaordercompleter.password") + '"', "\"\"", ""));
    }
  }

  public void cmpltTarne(boolean quoted) {
    if (!quoted) {
      if (unit.isHideFaction()) {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION)
            + " " + Resources.getOrderTranslation(EresseaConstants.O_NOT)));
      } else {
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
            " "));
      }
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
      completions.add(new Completion("0"));
    }

    if ((data != null) && (data.rules != null)) {
      Race demons = data.rules.getRace(EresseaConstants.R_DAEMONEN);

      if ((demons == null) || (unit.getRace().equals(demons))) {
        for (Iterator iter = data.rules.getRaceIterator(); iter.hasNext();) {
          Race r = (Race) iter.next();
          completions.add(new Completion(r.getName(), Completion.DEFAULT_PRIORITY + 1));
        }
      }
    }

  }

  public void cmpltTarnePartei() {
    if (unit.isHideFaction()) {
      completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
    }

    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NUMBER), " "));
  }

  public void cmpltTarneParteiNummer() {
    addFactions("");
  }

  public void cmpltTransportiere(boolean tempToken) {
    addRegionUnits("", tempToken);
  }

  public void cmpltVergesse() {
    for (Iterator i = unit.getSkills().iterator(); i.hasNext();) {
      completions.add(new Completion(((Skill) i.next()).getName(), ""));
    }
  }

  public void cmpltVerkaufe() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
  }

  public void cmpltVerkaufeAmount() {
    addUnitLuxuries("");
  }

  public void cmpltVerkaufeAlles() {
    addUnitLuxuries("");
  }

  public void cmpltZaubere(boolean far, boolean combat, boolean addRegion, boolean addLevel,
      String opening, String closing) {
    // this is the check for magicans & familars with own spells:
    if ((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
      if (addRegion)
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
            " ", Completion.DEFAULT_PRIORITY - 1));
      if (addLevel)
        completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
            " ", Completion.DEFAULT_PRIORITY - 1));
      addFilteredSpells(unit, far, region.getType().equals(
          data.rules.getRegionType(EresseaConstants.RT_OCEAN)), combat, opening, closing);
    }

    // here we go for spells spoken through the familar
    if (unit.getFamiliarmageID() != null) {
      Unit mage = data.getUnit(unit.getFamiliarmageID());
      if ((mage != null) && (mage.getSpells() != null) && (mage.getSpells().size() > 0)) {
        if (addRegion)
          completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
              " ", Completion.DEFAULT_PRIORITY - 1));
        if (addLevel)
          completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
              " ", Completion.DEFAULT_PRIORITY - 1));
        addFamilarSpells(mage, unit, opening, closing);
      }
    }
  }

  public void cmpltZaubereStufe() {
    Skill magic = unit.getSkill(data.rules.getSkillType(EresseaConstants.S_MAGIE));
    int level = 1;
    if (magic != null)
      level = magic.getLevel();
    completions
        .add(new Completion(Resources.get("gamebinding.eressea.eresseaordercompleter.level"),
            String.valueOf(level), ""));
  }

  public void cmpltZaubereRegion() {
    Map<CoordinateID, Region> regions1 =
        Regions.getAllNeighbours(data.regions(), region.getID(), 1, null);
    Map<CoordinateID, Region> regions2 =
        Regions.getAllNeighbours(data.regions(), region.getID(), 2, null);

    CoordinateID trans =
        data.getCoordinateTranslation(unit.getFaction().getID(), region.getCoordinate().z);
    if (trans != null) {
      trans = (new CoordinateID(0, 0, trans.z)).subtract(trans);
    }

    // first add all regions within a radius of 1 and remove them from Map regions2
    for (CoordinateID c : regions1.keySet()) {

      if (!c.equals(region.getCoordinate())) {
        Region r = regions1.get(c);
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

    for (CoordinateID c : regions2.keySet()) {
      Region r = regions2.get(c);
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

  public void cmpltZaubereRegionStufe() {
    cmpltZaubereStufe();
    /*
     * if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
     * addFilteredSpells(unit.getSpells().values(), true,
     * region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)), false); }
     */
  }

  /*
   * Enno in e-client about the syntax: 'c' = Zeichenkette 'k' =
   * REGION|EINHEIT|STUFE|SCHIFF|GEBAEUDE 'i' = Zahl 's' = Schiffsnummer 'b' = Gebaeudenummer 'r' =
   * Regionskoordinaten (x, y) 'u' = Einheit '+' = Wiederholung des vorangehenden Parameters '?' =
   * vorangegangener Parameter
   */
  public void cmpltZaubereSpruch(Spell spell) {
    if (spell == null || spell.getSyntax() == null)
      return;
    if (spell.getSyntax().contains("k")) {
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION), " "));
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT), " "));
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL), " "));
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP), " "));
      addCompletion(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE), " "));
    }
    if (spell.getSyntax().contains("u"))
      addRegionUnits(" ", 0, false);
    if (spell.getSyntax().contains("b"))
      addRegionBuildings("", " ", null, false);
    if (spell.getSyntax().contains("s"))
      addRegionShips("", " ", null, false);
  }

  /**
   * adds the given spells if combat, only adds combat-spells and so on
   * 
   * @param closing
   * @param opening
   */
  private void addFilteredSpells(Unit u, boolean far, boolean ocean, boolean combat,
      String opening, String closing) {
    Collection spells = u.getSpells().values();
    for (Iterator iter = spells.iterator(); iter.hasNext();) {
      Spell spell = (Spell) iter.next();

      // FF 20080412: ocean = true if unit is MM !

      if ((spell.getDescription() == null) // indicates that no information is available about this
          // spell
          || ((spell.getIsFar() || !far)
              && (spell.getOnOcean() || !ocean || u.getRace().equals(
                  data.rules.getRace(EresseaConstants.R_MEERMENSCHEN))) && (!combat ^ (spell
              .getType().toLowerCase().indexOf("combat") > -1)))) {
        String spellName = this.data.getTranslation(spell);

        completions.add(new Completion(opening + spellName + closing));
      }
    }
  }

  private void addFamilarSpells(Unit mage, Unit familar, String opening, String closing) {

    Skill magic = mage.getSkill(data.rules.getSkillType(EresseaConstants.S_MAGIE));
    if ((magic != null)
        && (Regions.getRegionDist(mage.getRegion().getCoordinate(), familar.getRegion()
            .getCoordinate()) <= magic.getLevel())) {
      // familar is in range
      int maxlevel = magic.getLevel() / 2;
      magic = familar.getSkill(data.rules.getSkillType(EresseaConstants.S_MAGIE));
      if (magic != null) {
        // maximum possible spelllevel:
        maxlevel = Math.min(maxlevel, magic.getLevel());

        for (Spell spell : mage.getSpells().values()) {
          if ((spell.getDescription() == null) // indicates that no information is available about
              // this spell
              || (spell.getIsFamiliar() && (spell.getLevel() <= maxlevel))) {
            // seems to be a spell usable by a familar
            String spellName = this.data.getTranslation(spell);

            completions.add(new Completion(spellName, opening + spellName + closing, " "));
          }
        }
      }
    } else {
      completions.add(new Completion("=== Magier nicht in Reichweite ===", "", ""));
    }
  }

  public void cmpltZeige() {
    addUnitItems("");
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " ",
        Completion.DEFAULT_PRIORITY - 1));
  }

  public void cmpltZeigeAlle() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_POTIONS)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SPELLS)));
  }

  public void cmpltZerstoere() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROAD), " "));
  }

  public void cmpltZerstoereStrasse() {
    if (region != null) {
      for (Iterator iter = region.borders().iterator(); iter.hasNext();) {
        Border b = (Border) iter.next();

        if (Umlaut.convertUmlauts(b.getType()).equalsIgnoreCase(
            Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
          completions.add(new Completion(Direction.toString(b.getDirection()), ""));
        }
      }
    } else {
      addDirections("");
    }
  }

  public void cmpltZuechte() {
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HORSES)));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
  }

  public void cmpltFactions(String postfix) {
    addFactions(postfix);
  }

  /**
   * adds all units in this region whose faction has a trustlevel not greater than zero (TL_DEFAULT)
   */
  protected void addEnemyUnits(String postfix) {
    if ((data != null) && (unit != null) && (region != null)) {
      Iterator units = region.units().iterator();

      while (units.hasNext() == true) {
        Unit u = (Unit) units.next();

        if ((u.getFaction().getTrustLevel() <= Faction.TL_DEFAULT) || u.isSpy()) {
          addUnit(u, postfix);
        }
      }
    }
  }

  /**
   * adds all units in this region, whose faction does not fit all of the alliances in the given
   * Alliance-Object. Example: Given Alliance contains help and give: units are added if they are
   * not allied both: help AND give. The reference-object is the faction of the current unit
   */
  protected void addNotAlliedUnits(Alliance alliance, String postfix) {
    for (Iterator<Unit> iter = region.units().iterator(); iter.hasNext();) {
      Unit curUnit = iter.next();
      Faction f = curUnit.getFaction();

      // search for alliances
      if (f == null) {
        addUnit(curUnit, postfix);
      } else if (!f.equals(unit.getFaction())) {
        Alliance testAlliance = null;
        if (unit.getGroup() != null) {
          Map<EntityID, Alliance> allies = unit.getGroup().allies();
          if (allies != null)
            testAlliance = unit.getGroup().allies().get(f.getID());
        } else {
          Map<EntityID, Alliance> allies = unit.getFaction().getAllies();
          if (allies != null)
            testAlliance = unit.getFaction().getAllies().get(f.getID());
        }
        if (testAlliance == null) {
          // curUnit is not allied
          addUnit(curUnit, postfix);
        } else {
          if ((testAlliance.getState() & alliance.getState()) != alliance.getState()) {
            // curUnit doesn't fit all alliance-states and is therefor added
            addUnit(curUnit, postfix);
          }
        }
      }
    }
  }

  /**
   * Adds all units in the region to the completions.
   */
  protected void addRegionUnits(String postfix, boolean tempOnly) {
    addRegionUnits(postfix, 0, tempOnly);
  }

  /**
   * Adds all units in the region to the completions.
   */
  protected void addRegionUnits(String postfix, int cursorOffset, boolean tempOnly) {
    if (region != null) {
      for (Unit u : region.units()) {
        if (((unit == null) || !u.equals(unit)) && (!tempOnly || u instanceof TempUnit)) {
          addUnit(u, postfix, cursorOffset, tempOnly);
        }
      }
    }
  }

  /**
   * Add all the ships in the current region except <code>exclude</code> (which may be
   * <code>null</code>). Prefix them with <code>prefix</code>.
   */
  protected void addRegionShips(String prefix, String postfix, Ship exclude, boolean comment) {
    Iterator<Ship> iter2 = region.ships().iterator();
    for (; iter2.hasNext();) {
      UnitContainer uc = iter2.next();

      if (!uc.equals(exclude)) {
        completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY-1));
        completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY));
      }
    }
  }

  /**
   * Add all the buildings in the current region except <code>exclude</code> (which may be
   * <code>null</code>). Prefix them with <code>prefix</code>.
   * 
   * @param postfix
   */
  void addRegionBuildings(String prefix, String postfix, Building exclude, boolean comment) {
    Iterator<Building> iter1 = region.buildings().iterator();
    for (; iter1.hasNext();) {
      UnitContainer uc = iter1.next();

      if (!uc.equals(exclude)) {
        completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY-1));
        completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY));
      }
    }
  }

  protected void addRegionItemsFaction(String postfix, int minAmount) {
    if (region != null) {
      Map<ItemType, Integer> items = new HashMap<ItemType, Integer>();
      for (Unit actUnit : region.units()) {
        if (actUnit.getFaction() != null && actUnit.getFaction().equals(unit.getFaction())) {
          for (Item actUnitItem : actUnit.getItems()) {
            ItemType actItemType = actUnitItem.getItemType();
            if (items.containsKey(actItemType)) {
              // our List contains the ItemType already
              items.put(actItemType, new Integer((items.get(actItemType)).intValue()
                  + actUnitItem.getAmount()));
            } else {
              // new ItemType on our List
              items.put(actItemType, new Integer(actUnitItem.getAmount()));
            }
          }
        }
      }
      if (items.size() > 0) {
        for (ItemType itemType : items.keySet()) {
          int amount = items.get(itemType).intValue();
          if (amount >= minAmount) {
            completions.add(new Completion(itemType.getName() + " (" + amount + ")", itemType
                .getOrderName(), postfix));
          }
        }
      }
    }
  }

  protected void addRegionShipCommanders(String postfix) {
    addRegionShipCommanders(postfix, 0);
  }

  protected void addRegionShipCommanders(String postfix, int cursorOffset) {
    if (region != null) {
      Iterator<Ship> ships = region.ships().iterator();
      while (ships.hasNext() == true) {
        Ship s = ships.next();
        if (s != null) {
          Unit u = s.getModifiedOwnerUnit();
          if (u != null) {
            if ((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(s, u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }

  protected void addUnitContainerOwner(UnitContainer s, Unit u, String postfix, int cursorOffset) {
    String id = u.getID().toString();

    completions.add(new Completion(s.toString() + " (" + s.getID() + ")", id, postfix,
        Completion.DEFAULT_PRIORITY + 1, cursorOffset));
    completions.add(new Completion(s.getID() + " (" + s.toString() + ")", id, postfix,
        Completion.DEFAULT_PRIORITY + 2, cursorOffset));
  }

  protected void addRegionBuildingOwners(String postfix) {
    addRegionBuildingOwners(postfix, 0);
  }

  protected void addRegionBuildingOwners(String postfix, int cursorOffset) {
    if (region != null) {
      Iterator<Building> buildings = region.buildings().iterator();
      while (buildings.hasNext() == true) {
        Building b = buildings.next();
        if (b != null) {
          Unit u = b.getModifiedOwnerUnit();
          if (u != null) {
            if ((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(b, u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }

  protected void addUnitItems(String postfix) {
    addUnitItems(0, postfix);
  }

  protected void addUnitItems(int amount, String postfix) {
    for (Item i : unit.getItems()) {
      completions
          .add(new Completion(i.getOrderName(), i.getOrderName(), postfix,
              (i.getAmount() >= amount) ? Completion.DEFAULT_PRIORITY
                  : Completion.DEFAULT_PRIORITY + 1));
    }
  }

  protected void addFactionItems(String postfix) {
    addFactionItems(0, postfix);
  }

  protected void addFactionItems(int amount, String postfix) {
    for (Item i : unit.getFaction().getItems()) {
      completions
          .add(new Completion(i.getOrderName(), i.getOrderName(), postfix,
              (i.getAmount() >= amount) ? Completion.DEFAULT_PRIORITY
                  : Completion.DEFAULT_PRIORITY + 1));
    }
  }

  /**
   * Adds all known factions to the completions with comment
   * 
   * @param postfix
   */
  protected void addFactions(String postfix) {
    if (data != null) {
      for (Iterator<Faction> iter = data.factions().values().iterator(); iter.hasNext();) {
        Faction f = iter.next();
        addNamed(f, postfix, 0, true);
      }
    }
  }

  /**
   * Adds all factions except the current unit's.
   * 
   * @param postfix appended to Completion
   * @param cursorOffset used for Completion
   * @param addComment if <code>true</code>, the faction name is appended as a comment. Does not mix
   *          well with <code>cursorOffset!=0</code>.
   */
  protected void addOtherFactions(String postfix, int cursorOffset, boolean addComment) {
    Faction ownerFaction = unit.getFaction();
    Iterator<Faction> factions = data.factions().values().iterator();

    while ((factions != null) && factions.hasNext()) {
      Faction f = factions.next();

      if ((ownerFaction == null) || (f.equals(ownerFaction) == false)) {
        addNamed(f, postfix, cursorOffset, addComment);
      }
    }
  }

  protected void addSurroundingRegions(int radius, String postfix) {
    if (radius < 1) {
      radius = 1;
    }

    Map<ID, RegionType> excludedRegionTypes = Regions.getOceanRegionTypes(getData().rules);
    // no need to exclude oceans, oceans have no name anyway and it'll break getPath(...)
    
    Map<CoordinateID, Region> neighbours =
        Regions.getAllNeighbours(data.regions(), region.getID(), radius, excludedRegionTypes);

    // do not include the region the unit stays in
    neighbours.remove(region.getID());

    for (Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
      Region r = iter.next();

      if (r.getName() != null && region != null && !region.equals(r)) {
        // get a path from the current region to neighbouring
        // translate the path of regions into a string of
        // directions to take
        String directions =
            Regions.getDirections(data, region.getID(), r.getID(), excludedRegionTypes, radius);

        if (directions != null) {
          completions.add(new Completion(r.getName(), directions, postfix,
              Completion.DEFAULT_PRIORITY - 1));
        }
      }
    }
  }

  protected void addDirections(String postfix) {
    for (Iterator iter = Direction.getShortNames().iterator(); iter.hasNext();) {
      String dir = (String) iter.next();
      completions.add(new Completion(dir, dir, postfix));
    }

    for (Iterator iter = Direction.getLongNames().iterator(); iter.hasNext();) {
      String dir = (String) iter.next();
      completions.add(new Completion(dir, dir, postfix));
    }
  }

  protected void addUnitLuxuries(String postfix) {
    ItemCategory cat = null;

    if ((data != null) && (data.rules != null)) {
      cat = data.rules.getItemCategory(EresseaConstants.C_LUXURIES);
    }

    if ((cat != null) && (unit != null)) {
      for (Item i : unit.getModifiedItems()) {

        if ((i.getItemType().getCategory() != null) && i.getItemType().getCategory().equals(cat)) {
          LuxuryPrice lp = unit.getRegion().getPrices().get(i.getItemType().getID());
          if (lp != null && lp.getPrice() > 0) {
            completions.add(new Completion(i.getOrderName(), i.getOrderName(), postfix));
          }
        }
      }
    }
  }

  protected void addUnit(Unit u, String postfix) {
    addUnit(u, postfix, 0);
  }

  /**
   * Adds a unit to the completion in a standard manner without comment.
   */
  protected void addUnit(Unit u, String postfix, int cursorOffset) {
    addUnit(u, postfix, cursorOffset, false);
  }

  /**
   * Adds a unit to the completions in a standard manner without comments.
   */
  protected void addUnit(Unit u, String postfix, int cursorOffset, boolean tempOnly) {
    String id = u.getID().toString();

    if (u instanceof TempUnit) {
      completions.add(new Completion((tempOnly ? "" : Resources
          .getOrderTranslation(EresseaConstants.O_TEMP)
          + " ")
          + id, postfix, Completion.DEFAULT_PRIORITY - 1, cursorOffset));
    } else {
      addNamed(u, postfix, cursorOffset, false);
    }
  }

  /**
   * Adds a thing to the completions, optionally with comments.
   * 
   * @param named This object's name and ID are displayed
   * @param postfix Completion postfix
   * @param offset Completion offset (probably doesn't make sense with comment)
   * @param addComment If this is <code>true</code>, the name is inserted as a comment after the id
   */
  protected void addNamed(Named named, String postfix, int offset, boolean addComment) {
    addNamed(named, postfix, Completion.DEFAULT_PRIORITY, offset, addComment);
  }

  /**
   * Adds a thing to the completions, optionally with comments.
   * 
   * @param named This object's name and ID are displayed
   * @param postfix Completion postfix
   * @param prio This prio and <code>prio-1</code> are used
   * @param offset Completion offset (probably doesn't make sense with comment)
   * @param addComment If this is <code>true</code>, the name is inserted as a comment after the id
   */
  protected void addNamed(Named named, String postfix, int prio, int offset, boolean addComment) {
    String name = named.getName();
    String id = named.getID().toString();
    if (name != null) {
      name.replaceAll(" ", "~");
      completions.add(new Completion(name + " (" + id + ")",
          id + (addComment ? ("; " + name) : ""), postfix, prio - 1, offset));
      completions.add(new Completion(id + " (" + name + ")",
          id + (addComment ? ("; " + name) : ""), postfix, prio, offset));
    } else {
      completions.add(new Completion(id, id, postfix, prio, offset));
    }
  }

  /**
   * Check for the necessary materials to produce an item considering all privileged factions in the
   * current region
   * 
   * @param iter An Iterator over the necessary materials (Items)
   * @return true, if the necessary materials are available, false otherwise
   */
  protected boolean checkForMaterials(Iterator iter) {
    return checkForMaterials(iter, 1);
  }

  /**
   * Check for the necessary materials to produce an item considering all privileged factions in the
   * current region
   * 
   * @param iter An Iterator over the necessary materials (Items)
   * @param amount A multiplicator
   * @return true, if the necessary materials are available, false otherwise
   */
  protected boolean checkForMaterials(Iterator iter, int amount) {
    boolean canMake = true;

    while (iter != null && iter.hasNext() && canMake) {
      Item ingredient = (Item) iter.next();

      // be careful, units cannot own peasants although one is required for the potion "Bauernblut"
      if (ingredient.getItemType() != null) {
        int availableAmount = 0;

        if (ingredient.getItemType().equals(data.rules.getItemType(EresseaConstants.I_PEASANTS))) {
          availableAmount = region.getPeasants();
        } else {
          Item available = region.getItem(ingredient.getItemType());

          if (available != null) {
            availableAmount = available.getAmount();
          }
        }

        if (availableAmount < (ingredient.getAmount() * amount)) {
          canMake = false;
        }
      }
    }

    return canMake;
  }

  /**
   * Returns the last word in the list of tokens.
   */
  public static String getStub(List<OrderToken> tokens) {
    if (tokens.size() == 0)
      throw new IllegalArgumentException();
    if (tokens.size() == 1)
      return "";
    else {
      OrderToken lastWord = tokens.get(tokens.size() - 2);
      if (lastWord.followedBySpace() || lastWord.ttype == OrderToken.TT_PERSIST)
        return "";
      else if (lastWord.ttype == OrderToken.TT_CLOSING_QUOTE)
        return tokens.get(tokens.size() - 4).getText() + tokens.get(tokens.size() - 3).getText()
            + lastWord.getText();
      else if (tokens.size() > 2
          && tokens.get(tokens.size() - 3).ttype == OrderToken.TT_OPENING_QUOTE)
        return tokens.get(tokens.size() - 3).getText() + lastWord.getText();
      else
        return lastWord.getText();
    }
  }

  /**
   * Returns the last word immediately at the end of the String txt. It is preferable to use
   * {@link #getStub(List)}. <br/>
   * FIXME (stm) this is identical to
   * <code>magellan.client.completion.AutoCompletion.getStub(String)</code> but we don't want to
   * reference src-client here...
   */
  public static String getStub(String txt) {
    StringBuffer retVal = new StringBuffer();

    for (int i = txt.length() - 1; i >= 0; i--) {
      char c = txt.charAt(i);

// if((c == '-') || (c == '_') || (c == '~') || (c == '.') || (Character.isLetterOrDigit(c) ==
      // true)) {
      if ((!Character.isWhitespace(c) && c != '\'' && c != '"' && c != '@')) {
// if ((!Character.isWhitespace(c))) {
        retVal.append(c);
      } else {
        break;
      }
    }
    return retVal.reverse().toString();
  }

  /**
   * Determines whether the unit has any skill of at least the given level Used i.e. for determining
   * if someone can teach
   */
  protected boolean hasSkills(Unit u, int level) {
    for (Skill s : u.getModifiedSkills()) {
      if (s.getLevel() >= level) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether the specified unit has a skill.
   */
  protected boolean hasSkill(Unit u, StringID id) {
    return hasSkill(u, id, 1);
  }

  /**
   * Determines whether the specified unit has a skill at a minimum level. Returns also true, if the
   * specified skill is unknown. FF: changed to reflect modified skill
   */
  protected boolean hasSkill(Unit u, StringID id, int level) {
    boolean retVal = false;
    SkillType skillType = data.rules.getSkillType(id);

    if (skillType != null) {
      // Skill e = u.getSkill(skillType);
      Skill e = u.getModifiedSkill(skillType);

      if ((e != null) && (e.getLevel() >= level)) {
        retVal = true;
      }
    } else {
      retVal = true;
    }

    return retVal;
  }

  /**
   * Adds an item by type
   */
  protected void addItem(ItemType iType, String postfix) {
    completions.add(new Completion(iType.getOrderName(), iType.getOrderName(), postfix));
  }

  /**
   * Case-insensitive comparator for String and/or Completion objects
   */
  protected class IgnrCsComp implements Comparator<Object> {
    /**
     * Compares Strings or completions case insensitively.
     */
    public int compare(Object o1, Object o2) {
      if (o1 instanceof String && o2 instanceof String) {
        return ((String) o1).compareToIgnoreCase((String) o2);
      } else if (o1 instanceof Completion && o2 instanceof Completion) {
        Completion c1 = (Completion) o1;
        Completion c2 = (Completion) o2;

        if (c1.getName() == null) {
          return (c2.getName() == null) ? 0 : 1;
        } else {
          return (c2.getName() == null) ? (-1) : c1.getName().compareToIgnoreCase(c2.getName());
        }
      } else if (o1 instanceof Completion && o2 instanceof String) {
        String s1 = ((Completion) o1).getName();
        String s2 = (String) o2;
        if (s1 == null) {
          return 0;
        } else {
          return s1.compareToIgnoreCase(s2);
        }
      } else if (o1 instanceof String && o2 instanceof Completion) {
        String s1 = (String) o1;
        String s2 = ((Completion) o2).getName();
        if (s2 == null) {
          return 0;
        } else {
          return s2.compareToIgnoreCase(s1);
        }
      }

      return 0;
    }

  }

  /**
   * Priority comparator for Completion objects
   */
  protected static class PrioComp implements Comparator<Completion> {
    /**
     * Compares to Completions by priority.
     */
    public int compare(Completion o1, Completion o2) {
      int retVal = 0;

      if (o1.getPriority() != o2.getPriority()) {
        retVal = o1.getPriority() - o2.getPriority();
      } else {
        retVal = o1.getName().compareToIgnoreCase(o2.getName());
      }

      return retVal;
    }

  }

  /**
   * @see magellan.library.completion.Completer#getCompletions(magellan.library.Unit,
   *      java.lang.String, java.util.List)
   */
  public List<Completion> getCompletions(Unit u, String line, List<Completion> old) {
    if (true || (old == null) || (old.size() == 0)) {
      return this.getCompletions(u, line);
    } else {
      getParser().read(new StringReader(line));
      List<OrderToken> tokens = getParser().getTokens();
      return this.crop(old, tokens);
    }
  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  public GameData getData() {
    return data;
  }

  protected GameSpecificStuff getGameSpecificStuff() {
    return data.rules.getGameSpecificStuff();
  }


  /**
   * Returns the value of completions.
   * 
   * @return Returns completions.
   */
  public List<Completion> getCompletions() {
    return completions;
  }

  /**
   * Returns the value of region.
   * 
   * @return Returns region.
   */
  public Region getRegion() {
    return region;
  }

  /**
   * Returns the value of unit.
   * 
   * @return Returns unit.
   */
  public Unit getUnit() {
    return unit;
  }

}
