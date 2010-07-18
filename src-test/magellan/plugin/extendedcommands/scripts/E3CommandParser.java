// Author : stm
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
package magellan.plugin.extendedcommands.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Resources;
import magellan.plugin.extendedcommands.ExtendedCommandsHelper;

/**
 * Call from the script of your faction with<br />
 * "(new E3CommandParser(world, helper)).execute(container);" or
 * "(new E3CommandParser(world, helper)).execute(helper.getUnit("
 * ekp6").getFaction(), (Region) container);".
 * 
 * @author stm
 */
public class E3CommandParser {

  /**
   * Creates and initializes the parser.
   * 
   * @param world
   * @param helper
   */
  public E3CommandParser(GameData world, ExtendedCommandsHelper helper) {
    E3CommandParser.world = world;
    E3CommandParser.helper = helper;
  }

  static GameData world;
  static ExtendedCommandsHelper helper;

  /** Unit limit, used to warn if we get too many units. */
  public static final int EINHEITENLIMIT = 200;

  /** All script commands begin with this text. */
  public static final String scriptMarker = "$cript";
  /** The GIVE order */
  public static String GIVEOrder = "GIB";
  /** The RESERVE order */
  public static String RESERVEOrder = "RESERVIERE";
  /** The EACH order parameter */
  public static String EACHOrder = "JE";
  /** The ALL order parameter */
  public static String ALLOrder = "ALLES";
  /** The persistent comment order */
  public static String PCOMMENTOrder = "//";
  /** The persistent comment order */
  public static String COMMENTOrder = ";";
  /** The LEARN order */
  public static String LEARNOrder = "LERNE";
  /** The TEACH order */
  public static String TEACHOrder = "LEHRE";

  // warning constants
  /** The NEVER warning type token */
  public static String W_NEVER = "nie";
  /** The SKILL warning type token */
  public static String W_SKILL = "Talent";
  /** The WEAPON warning type token */
  public static String W_WEAPON = "Waffe";
  /** The SHIELD warning type token */
  public static String W_SHIELD = "Schild";
  /** The ARMOR warning type token */
  public static String W_ARMOR = "Rüstung";
  /** The UNIT warning type token */
  public static final String W_UNIT = "Einheit";
  /** The AMOUNT warning type token */
  public static final String W_AMOUNT = "Menge";
  /** The ALWAYS warning type token */
  public static final Object W_ALWAYS = "immer";
  /** The BEST token (for soldier) */
  public static String BEST = "best";
  /** The NULL token (for soldier) */
  public static String NULL = "null";
  /** The NOT token (for auto and others) */
  public static String NOT = "nicht";

  private Unit someUnit;

  private Faction currentFaction;
  private Region currentRegion;
  private Unit currentUnit;
  private ArrayList<String> oldOrders;
  private ArrayList<String> newOrders;
  private String currentOrder;
  private int line;
  private int error;
  private String errMsg;
  private boolean changedOrders;

  /**
   * The item/unit/need map. Stores all needed items.
   */
  protected Map<String, Map<Unit, Need>> needMap;

  /**
   * The item/unit/supply map. Stores all available items.
   */
  protected Map<String, Map<Unit, Supply>> supplyMap;

  /**
   * Lines matching these patterns should be removed.
   */
  protected List<String> removedOrderPatterns;

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands.
   * 
   * @param faction
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void execute(Faction faction) {
    execute(faction, null);
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands.
   * 
   * @param faction
   * @param region only commands of unit in this region are executed
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void execute(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();

    currentFaction = faction;

    initLocales();

    // sometimes we need an arbitrary unit. This is a shorthand for it.
    someUnit = faction.units().iterator().next();
    if (someUnit == null)
      throw new RuntimeException("No units in report!");

    if (faction.units().size() > EINHEITENLIMIT - 10) {
      addWarning(someUnit, "Einheitenlimit fast erreicht!");
    }

    collectStats();

    // Parses the orders of the units for commands of the form "// $cript ..." and
    // tries to execute them.
    if (region == null) {
      for (Region r : world.regions().values()) {
        execute(r);
      }
    } else {
      execute(region);
    }

    for (Unit u : faction.units()) {
      if ("1".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(true);
      } else if ("0".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(false);
      }
      notifyMagellan(u);
    }
  }

  private void execute(Region region) {
    currentRegion = region;
    initSupply();

    needMap.clear();
    // supplyMap.clear();
    for (Unit u : region.units()) {
      if (u.getFaction().equals(currentFaction)) {
        currentUnit = u;
        parseScripts();
      }
    }
    satisfyNeeds();
  }

  /**
   * Adds some statistic information to the orders of the first unit.
   * 
   * @param region
   */
  protected void collectStats() {
    int buildingScripts = 0;
    int shipScripts = 0;
    int unitScripts = 0;
    int regionScripts = 0;
    // FIXME uncomment this if you have the newest nighthly build of Magellan
    // ---start uncomment for BeanShell
    // for (Building b : world.getBuildings()) {
    // if (helper.hasScript(b)) {
    // buildingScripts++;
    // }
    // }
    // for (Ship s : world.getShips()) {
    // if (helper.hasScript(s)) {
    // shipScripts++;
    // }
    // }
    // for (Unit u : world.getUnits()) {
    // if (helper.hasScript(u)) {
    // unitScripts++;
    // }
    // }
    // for (Region r : world.getRegions()) {
    // if (helper.hasScript(r)) {
    // regionScripts++;
    // }
    // }
    // ---stop uncomment for BeanShell
    someUnit.addOrderAt(0, "; " + unitScripts + " unit scripts, " + buildingScripts
        + " building scripts, " + shipScripts + " ship scripts, " + regionScripts
        + " region scripts");
  }

  /**
   * Parses the orders of the unit u for commands of the form "// $cript ..." and tries to execute
   * them. Known commands:<br />
   * <tt>// $cript +X text</tt> -- If X<=1 then a warning containing text is added to the unit's
   * orders. Otherwise X is decreased by one.<br />
   * <tt>// $cript auto [nicht]</tt> -- autoconfirm orders<br />
   * <tt>// $cript GibWenn unit amount item</tt> -- Adds a GIVE order if the specified unit is
   * present in u's region.<br />
   * <code>// $cript GibWenn receiver [JE] amount item [warning]</code> -- add give order (if
   * possible)<br />
   * <code>// $cript Benoetige minAmount [maxAmount] item</code><br />
   * <code>// $cript Benoetige JE amount item</code><br />
   * <code>// $cript Benoetige ALLES [item]</code> -- acquire things from other units<br />
   * <code>// $cript Soldat [Talent [Waffe [Schild [Rüstung]]]] [nie|Talent|Waffe|Schild|Rüstung]</code>
   * -- learn skill and reserve equipment<br />
   */
  protected void parseScripts() {
    oldOrders = new ArrayList<String>(currentUnit.getOrders());
    newOrders = new ArrayList<String>();
    removedOrderPatterns = new ArrayList<String>();
    changedOrders = false;
    error = -1;
    errMsg = null;
    line = 0;
    orderLoop: for (Object o : oldOrders) {
      ++line;
      currentOrder = (String) o;
      String[] tokens = detectScriptCommand(currentOrder);
      if (tokens == null) {
        addNewOrder(currentOrder, false);
        continue orderLoop;
      }

      String command = tokens[0];
      if (command.startsWith("+")) {
        commandWarning(tokens);
      } else if (command.equals("auto")) {
        addNewOrder(currentOrder, false);
        if (tokens.length == 2 && NOT.equals(tokens[1])) {
          setConfirm(currentUnit, false);
        } else {
          setConfirm(currentUnit, true);
          addNewOrder("; autoconfirmed", true);
        }
        removeOrdersLike("; autoconfirmed", true);
      } else if (command.equals("GibWenn")) {
        commandGibWenn(tokens);
      } else if (command.equals("Benoetige")) {
        commandBenoetige(tokens);
        // } else if (command.equals("Versorge")) {
        // commandVersorge(tokens);
      } else if (command.equals("Soldat")) {
        commandSoldier(tokens);
      } else {
        addError("unbekannter Befehl");
      }
      currentOrder = null;
    }
    if (changedOrders) {
      currentUnit.setOrders(newOrders);
    }
    notifyMagellan(currentUnit);

    oldOrders = null;
    newOrders = null;
  }

  /**
   * If order is a script command ("// $cript ..."), returns a List of the tokens. Otherwise returns
   * <code>null</code>. The first in the list is the first token after the "// $cript".
   */
  protected String[] detectScriptCommand(String order) {
    StringTokenizer tokenizer = new StringTokenizer(order, " ");
    if (tokenizer.hasMoreTokens()) {
      String part = tokenizer.nextToken();
      if (part.equals(PCOMMENTOrder)) {
        if (tokenizer.hasMoreTokens()) {
          part = tokenizer.nextToken();
          if (part.equals(scriptMarker)) {
            List<String> result = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
              result.add(tokenizer.nextToken());
            }
            if (result.size() == 0)
              return null;
            return result.toArray(new String[] {});
          }
        }
      }
    }
    return null;
  }

  /**
   * <code>// $cript GibWenn receiver [[JE] amount|ALLES] item [warning]</code><br />
   * Adds a GIB order to the unit. Warning may be one of "immer", "Menge", "Einheit", "nie".
   */
  protected void commandGibWenn(String[] tokens) {
    final int IMMER = 0, MENGE = 1, EINHEIT = 2, NIE = 3;

    addNewOrder(currentOrder, false);
    if (tokens.length >= 4 && tokens.length <= 6) {
      Unit other = helper.getUnit(tokens[1]);
      int je = 0;
      if (EACHOrder.equals(tokens[2])) {
        je = 1;
      }
      int warning = IMMER;
      if (tokens.length == 5 + je) {
        if (W_ALWAYS.equals(tokens[4 + je])) {
          warning = IMMER;
        } else if (W_AMOUNT.equals(tokens[4 + je])) {
          warning = MENGE;
        } else if (W_UNIT.equals(tokens[4 + je])) {
          warning = EINHEIT;
        } else if (W_NEVER.equals(tokens[4 + je])) {
          warning = NIE;
        } else {
          addError("unbekannter Warnungstyp " + tokens[4 + je]
              + "; \"immer\", \"Menge\", \"Einheit\" oder \"nie\" erlaubt.");
        }
      }
      String item = tokens[3 + je];
      int amount = 0;
      if (ALLOrder.equalsIgnoreCase(tokens[2 + je])) {
        if (je > 0) {
          addError("JE ALLES geht nicht");
          return;
        }
        amount = getItemCount(currentUnit, item);
      } else {
        try {
          amount = Integer.parseInt(tokens[2 + je]);
        } catch (NumberFormatException e) {
          amount = 0;
          addError("Zahl oder ALLES erwartet");
        }
      }
      int fullAmount = amount;
      if (je == 1) {
        if (other == null) {
          addNewOrder("; Einheit nicht gefunden; kann Menge nicht überprüfen", true);
        } else {
          fullAmount = other.getModifiedPersons() * amount;
        }
      }
      if (getItemCount(currentUnit, item) < fullAmount) {
        if (warning == IMMER || warning == MENGE) {
          addError("zu wenig " + item);
        } else {
          addNewOrder("; zu wenig " + item, true);
        }
        amount = getItemCount(currentUnit, item);
        je = 0;
      }

      if (other == null || other.getRegion() != currentUnit.getRegion()) {
        if (warning == IMMER || warning == EINHEIT) {
          addError(tokens[1] + " nicht da.");
        } else {
          addNewOrder("; " + tokens[1] + " nicht da.", true);
        }
      }

      if (amount > 0) {
        if (amount == getItemCount(currentUnit, item) && je != 1) {
          addNewOrder(getGiveOrder(currentUnit, tokens[1], item, Integer.MAX_VALUE, je == 1), true);
        } else {
          addNewOrder(getGiveOrder(currentUnit, tokens[1], item, amount, je == 1), true);
        }
        Supply supply = getSupply(item, currentUnit);
        if (supply == null) {
          addError("Fehler im script: supply 0");
        }
        supply.reduceAmount(fullAmount);
        if (other != null) {
          addNeed(item, other, -amount, -amount);
        }
      }
    } else {
      addError("Fehler in GibWenn");
    }
  }

  /**
   * <code>// $cript Benoetige minAmount [maxAmount] item</code><br />
   * <code>// $cript Benoetige JE amount item</code><br />
   * <code>// $cript Benoetige ALLES [item]</code><br />
   * Tries to transfer the maxAmount of item from other units to this unit. Issues warning if
   * minAmount cannot be supplied. <code>Benoetige JE</code> tries to reserve 1 item for every
   * person in the unit. <code>Benoetige ALLES item</code> is equivalent to
   * <code>Benoetige 0 infinity item</code>, <code>Benoetige ALLES</code> is equivalent to
   * <code>Benoetige ALLES</code> for every itemtype in the region.
   */
  protected void commandBenoetige(String[] tokens) {
    if (tokens.length < 2 || tokens.length > 4) {
      addError("Fehler in Benoetige");
      return;
    }

    try {
      if (ALLOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNeed(tokens[2], currentUnit, 0, Integer.MAX_VALUE);
        } else {
          for (String item : needMap.keySet()) {
            addNeed(item, currentUnit, 0, Integer.MAX_VALUE);
          }
        }
      } else if (EACHOrder.equals(tokens[1])) {
        if (tokens.length != 4) {
          addError("Ungültige Argumente für Benoetige JE x Ding");
        } else {
          int amount = currentUnit.getPersons() * Integer.parseInt(tokens[2]);
          String item = tokens[3];
          addNeed(item, currentUnit, amount, amount);
        }
      } else {
        int minAmount = Integer.parseInt(tokens[1]);
        int maxAmount = tokens.length == 3 ? minAmount : Integer.parseInt(tokens[2]);
        String item = tokens[tokens.length - 1];
        addNeed(item, currentUnit, minAmount, maxAmount);
      }
    } catch (NumberFormatException exc) {
      addError("Ungültige Zahl in Benoetige");
    }
  }

  /**
   * <code>// $script +x [Arguments...]</code><br />
   * If x==0, the rest of the line is added as a warning to this unit. Otherwise x is decreased by
   * one.
   */
  protected void commandWarning(String[] tokens) {
    int delay = -1;
    try {
      delay = Integer.parseInt(tokens[0].substring(1));
    } catch (NumberFormatException e) {
      addError("Zahl erwartet");
      return;
    }
    if (delay <= 1) {
      StringBuilder warning = new StringBuilder("; TODO: ");
      String foo = currentOrder.substring(currentOrder.indexOf("+"));
      warning.append(foo.indexOf(" ") >= 0 ? foo.substring(foo.indexOf(" ") + 1) : "");
      addNewOrder(warning.toString(), true);
    } else {
      StringBuilder newCommand =
          new StringBuilder(PCOMMENTOrder).append(" ").append(scriptMarker).append(" +");
      newCommand.append(delay - 1);
      for (int i = 1; i < tokens.length; ++i) { // skip "+x"
        newCommand.append(" ").append(tokens[i]);
      }
      addNewOrder(newCommand.toString(), true);
    }
  }

  /**
   * <code>Soldat [Talent [Waffe [Schild [Rüstung]]]] [nie|Talent|Waffe|Schild|Rüstung]</code><br />
   * Tries to learn best skill and acquire equipment. If no skill is given, best weapon skill is
   * selected. If no weapon is given, best matching weapon is acquired and so on. Preference is
   * always for RESERVEing stuff the unit already has. Waffe, Schild, Rüstung can be "null" if
   * nothing should be reserved or "best" (the default behaviour).<br />
   * Warning can be:<br />
   * nie: issues no warnings at all, Talent: only warns if no skill is given and no best skill
   * exists, Waffe: additionally warn if no weapon can be acquired, Schild: additionally warn if no
   * shield, Rüstung: additionally warn if no armor. Default warning level is "Waffe".
   */
  protected void commandSoldier(String[] tokens) {
    addNewOrder(currentOrder, false);
    String warning = tokens[tokens.length - 1];
    if (!(W_NEVER.equals(warning) || W_SKILL.equals(warning) || W_WEAPON.equals(warning)
        || W_SHIELD.equals(warning) || W_ARMOR.equals(warning))) {
      warning = null;
    }
    String skill = null;
    if (tokens.length > 1 + (warning == null ? 0 : 1)) {
      skill = tokens[1];
    }
    String weapon = null;
    if (tokens.length > 2 + (warning == null ? 0 : 1)) {
      weapon = tokens[2];
    }
    String shield = null;
    if (tokens.length > 3 + (warning == null ? 0 : 1)) {
      shield = tokens[3];
    }
    String armor = null;
    if (tokens.length > 4 + (warning == null ? 0 : 1)) {
      armor = tokens[4];
    }

    if (warning == null) {
      warning = W_WEAPON;
    }

    if (BEST.equals(skill)) {
      skill = null;
    }
    if (BEST.equals(weapon)) {
      weapon = null;
    }
    if (BEST.equals(shield)) {
      shield = null;
    }
    if (BEST.equals(armor)) {
      armor = null;
    }

    soldier(currentUnit, skill, weapon, shield, armor, warning);
  }

  protected void initSupply() {
    needMap = new OrderedHashtable<String, Map<Unit, Need>>();
    supplyMap = new HashMap<String, Map<Unit, Supply>>();

    for (Unit u : currentRegion.units()) {
      if (u.getFaction() == currentFaction) {
        for (Item item : u.getItems()) {
          putSupply(item.getName(), u, item.getAmount());
        }

        // TODO take RESERVE or GIVE orders into account?

        // // subtract reserved items from supply amount of unit
        // for (ReserveRelation relation : u.getRelations(ReserveRelation.class)) {
        // Supply supply = getSupply(relation.itemType.getName(), u);
        // if (supply != null && relation.source == u) {
        // supply.setAmount(supply.getAmount() - relation.amount);
        // }
        // }
        // // subtract transferred items (by GIVE orders) from supply amount of source unit (don't
        // add
        // // to target unit)
        // for (ItemTransferRelation relation : u.getRelations(ItemTransferRelation.class)) {
        // Supply supply = getSupply(relation.itemType.getName(), u);
        // if (supply != null && relation.source == u) {
        // supply.setAmount(supply.getAmount() - relation.amount);
        // }
        // }
      }
    }
  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  /**
   * Adds a supply to the supplyMap
   * 
   * @param item
   * @param unit
   * @param amount
   */
  protected void putSupply(String item, Unit unit, int amount) {
    Map<Unit, Supply> itemSupplyMap = supplyMap.get(item);
    if (itemSupplyMap == null) {
      itemSupplyMap = new HashMap<Unit, Supply>();
      supplyMap.put(item, itemSupplyMap);
    }
    itemSupplyMap.put(unit, new Supply(unit, item, getItemCount(unit, item)));
  }

  /**
   * Tries to satisfy all needs in the current needMap by adding GIVE orders to supplyers.
   */
  protected void satisfyNeeds() {
    for (String item : needMap.keySet()) {
      // try to satisfy minimum need by own items
      for (Need need : needMap.get(item).values()) {
        reserveNeed(need, true);
      }

      // try to satisfy minimum needs with GIVE
      for (Need need : needMap.get(item).values()) {
        giveNeed(need, true);
      }

      // add warnings for unsatisfied needs
      for (Need need : needMap.get(item).values()) {
        if (need.getMinAmount() > 0) {
          addWarning(need.getUnit(), "needs " + need.getMinAmount() + " more " + need.getItem());
        }
      }

      // try to satisfy max needs, ignore infinite needs first
      for (Need need : needMap.get(item).values()) {
        if (need.getAmount() != Integer.MAX_VALUE) {
          reserveNeed(need, false);
        }
      }

      for (Need need : needMap.get(item).values()) {
        if (need.getAmount() != Integer.MAX_VALUE) {
          giveNeed(need, false);
        }
      }

      // now, finally, satisfy infinite needs
      for (Need need : needMap.get(item).values()) {
        if (need.getAmount() == Integer.MAX_VALUE) {
          reserveNeed(need, false);
        }
      }

      for (Need need : needMap.get(item).values()) {
        if (need.getAmount() == Integer.MAX_VALUE) {
          giveNeed(need, false);
        }
      }

      // add messages for unsatisfied needs
      for (Need need : needMap.get(item).values()) {
        if (need.getMinAmount() <= 0 && need.getMaxAmount() > 0
            && need.getMaxAmount() != Integer.MAX_VALUE) {
          helper.addOrder(need.getUnit(), "; needs " + need.getMaxAmount() + " more "
              + need.getItem());
        }
      }

    }
  }

  /**
   * Tries to satisfy (minimum) need by a RESERVE order
   * 
   * @param need
   * @param min
   */
  protected void reserveNeed(Need need, boolean min) {
    int amount = min ? need.getMinAmount() : need.getAmount();
    Supply supply = getSupply(need.getItem(), need.getUnit());
    if (supply == null)
      return;
    amount = Math.min(amount, supply.getAmount());
    if (amount > 0) {
      addReserveOrder(need.getUnit(), need.getItem(), amount, false);
      need.reduceAmount(amount);
      need.reduceMinAmount(amount);
      supply.reduceAmount(amount);

    }
  }

  /**
   * Tries to satisfy (minimum) need by a give order from supplyers.
   * 
   * @param need
   * @param min
   */
  protected void giveNeed(Need need, boolean min) {
    int amount = min ? need.getMinAmount() : need.getAmount();
    if (amount > 0) {
      if (!supplyMap.containsKey(need.getItem()))
        return;
      for (Supply supply : supplyMap.get(need.getItem()).values()) {
        if (supply.getUnit() != need.getUnit()) {
          amount = Math.min(amount, supply.getAmount());
          if (amount > 0) {
            addGiveOrder(supply.getUnit(), need.getUnit(), need.getItem(), amount, false);
            need.reduceAmount(amount);
            need.reduceMinAmount(amount);
            supply.reduceAmount(amount);
          }
        }
        if ((min ? need.getMinAmount() : need.getAmount()) <= 0) {
          break;
        }
      }
    }
  }

  /**
   * Returns a supply of a unit for an item.
   * 
   * @param item
   * @param unit
   * @return The supply or null, if none has been registered.
   */
  protected Supply getSupply(String item, Unit unit) {
    Map<Unit, Supply> map = supplyMap.get(item);
    if (map == null)
      return null;
    return map.get(unit);
  }

  /**
   * Adds the specified amounts to the need of a unit for an item to the needMap.
   * 
   * @param item
   * @param unit
   * @param minAmount
   * @param maxAmount
   */
  protected void addNeed(String item, Unit unit, int minAmount, int maxAmount) {
    Map<Unit, Need> map = needMap.get(item);
    if (map == null) {
      map = new OrderedHashtable<Unit, Need>();
      needMap.put(item, map);
    }
    Need need = map.get(unit);
    if (need == null) {
      need = new Need(unit, item, 0, 0);
      map.put(unit, need);
    }

    need.reduceAmount(-maxAmount);
    need.reduceMinAmount(-minAmount);
  }

  /**
   * Returns a need of a unit for an item.
   * 
   * @param item
   * @param unit
   * @return The need or <code>null</code> if none has been registered.
   */
  protected Need getNeed(String item, Unit unit) {
    Map<Unit, Need> map = needMap.get(item);
    if (map == null)
      return null;
    return map.get(unit);
  }

  /**
   * Marks the unit as soldier. Learns its best weapon skill. Reserves suitable weapon, armor, and
   * shield if available. Confirms unit if there's no problem. TODO update doc
   * 
   * @param u The unit in question
   * @param sWeaponSkill The desired skill. If <code>null</code>, the unit's best weapon skill is
   *          used. If the unit knows no weapon skill, a warning is issued.
   * @param sWeapon The desired weapon that is reserved. If this is <code>null</code>, a weapon that
   *          matches the weaponSkill is reserved.
   * @param sArmor The desired armor. If <code>null</code>, a suitable armor is reserved. If the
   *          unit has no armor at all, <em>no</em> warning is issued.
   * @param sShield The desired shield. If <code>null</code>, a suitable shield is reserved. If the
   *          unit has no shield at all, <em>no</em> warning is issued.
   * @param warnint Warnings for missing equipment are only issued if this is <code>true</code>.
   */
  protected void soldier(Unit u, String sWeaponSkill, String sWeapon, String sShield,
      String sArmor, String warning) {

    Rules rules = world.rules;

    SkillType weaponSkill =
        sWeaponSkill == null ? null : rules.getSkillType(StringID.create(sWeaponSkill));
    ItemType weapon = sWeapon == null ? null : rules.getItemType(StringID.create(sWeapon));
    ItemType armor = sArmor == null ? null : rules.getItemType(StringID.create(sArmor));
    ItemType shield = sShield == null ? null : rules.getItemType(StringID.create(sShield));

    if (weaponSkill == null || NULL.equals(weaponSkill)) {

      int max = 0;
      for (Skill skill : u.getSkills()) {
        if (isWeaponSkill(skill) && skill.getLevel() > max) {
          max = skill.getLevel();
          weaponSkill = skill.getSkillType();
        }
      }
      if (weaponSkill == null && !W_NEVER.equals(warning)) {
        addError("no weapon skill");
        return;
      }
    }

    ArrayList<Item> weapons = new ArrayList<Item>();
    if (weapon == null) {
      for (Item item : u.getItems()) {
        if (isUsable(item, weaponSkill)) {
          weapons.add(item);
        }
      }
      if (weapons.isEmpty()) {
        for (ItemType type : world.rules.getItemTypes()) {
          if (isUsable(type, weaponSkill)) {
            weapons.add(new Item(type, 0));
            break;
          }
        }
      }
    } else {
      if (u.getItem(weapon) != null) {
        weapons.add(u.getItem(weapon));
      }
    }
    if (weapons.isEmpty()) {
      addError("no known matching weapon types");
    }

    ArrayList<Item> shields = findItems(shield, u, "shield");
    if (shields.isEmpty()) {
      addError("no known shield types");
    }

    ArrayList<Item> armors = findItems(armor, u, "armour");
    if (armors.isEmpty()) {
      addError("no known armour types");
    }

    removeOrdersLike(LEARNOrder + ".*", true);
    removeOrdersLike(TEACHOrder + ".*", true);
    addNewOrder(LEARNOrder + " " + weaponSkill.getName(), true);

    if (!NULL.equals(sWeapon)
        && !reserveEquipment(weapon, weapons, !W_NEVER.equals(warning) && !W_SKILL.equals(warning))) {
      addError("could not reserve weapons");
    }
    if (!NULL.equals(sShield)
        && !reserveEquipment(shield, shields, W_ARMOR.equals(warning) || W_SHIELD.equals(warning))) {
      addError("could not reserve shields");
    }
    if (!NULL.equals(sArmor) && !reserveEquipment(armor, armors, W_ARMOR.equals(warning))) {
      addError("could not reserve armour");
    }

    setConfirm(u, true);
    notifyMagellan(u);
  }

  /**
   * If <code>itemType==null</code> return all the unit's items of the given category. If no item is
   * found, at least one item (with amount 0) is returned.
   */
  private ArrayList<Item> findItems(ItemType itemType, Unit u, String category) {
    ArrayList<Item> items = new ArrayList<Item>(1);
    ItemCategory itemCategory = world.rules.getItemCategory(StringID.create(category), false);
    if (itemType == null) {
      for (Item item : u.getItems()) {
        if (item.getItemType().getCategory().isDescendant(itemCategory)) {
          items.add(item);
        }
      }
    } else {
      if (u.getItem(itemType) != null) {
        items.add(u.getItem(itemType));
      }
    }
    if (items.isEmpty()) {
      for (ItemType type : world.rules.getItemTypes()) {
        if (type.getCategory().isDescendant(itemCategory)) {
          items.add(new Item(type, 0));
          break;
        }
      }
    }
    return items;
  }

  private boolean reserveEquipment(ItemType preferred, List<Item> ownStuff, boolean warn) {
    if (preferred != null) {
      // reserve requested weapon
      commandBenoetige(new String[] { "SOLDIER", EACHOrder, "1", preferred.getOrderName() });
    } else if (!ownStuff.isEmpty()) {
      int supply = 0;
      for (Item w : ownStuff) {
        // reserve all matching weapons, except first one
        if (supply > 0) {
          commandBenoetige(new String[] { "SOLDIER",
              Integer.toString(Math.min(currentUnit.getPersons() - supply, w.getAmount())),
              w.getOrderName() });
        }
        supply += w.getAmount();
      }

      // now reserve the first matching weapon with min=w.getAmount(), max=w.getPersons()-other
      // weapons
      Item w = ownStuff.get(0);
      String max =
          Integer.toString(Math.max(0, Math.min(currentUnit.getPersons() - supply + w.getAmount(),
              currentUnit.getPersons())));
      String min = warn ? max : Integer.toString(Math.min(currentUnit.getPersons(), w.getAmount()));
      commandBenoetige(new String[] { "SOLDIER", min, max, w.getOrderName() });
    } else
      return false;
    return true;
  }

  /**
   * Tries to init the constants according to the current faction's locale
   */
  protected void initLocales() {
    GIVEOrder = getLocalizedOrder(EresseaConstants.O_GIVE, GIVEOrder);
    RESERVEOrder = getLocalizedOrder(EresseaConstants.O_RESERVE, RESERVEOrder);
    EACHOrder = getLocalizedOrder(EresseaConstants.O_EACH, EACHOrder);
    ALLOrder = getLocalizedOrder(EresseaConstants.O_ALL, ALLOrder);
    PCOMMENTOrder = "//";
    LEARNOrder = getLocalizedOrder(EresseaConstants.O_LEARN, LEARNOrder);
    TEACHOrder = getLocalizedOrder(EresseaConstants.O_TEACH, TEACHOrder);

    if (currentFaction.getLocale().getLanguage() != "de") {
      // warning constants
      W_NEVER = "NEVER";
      W_SKILL = "SKILL";
      W_WEAPON = "WEAPON";
      W_SHIELD = "SHIELD";
      W_ARMOR = "ARMOR";
      BEST = "BEST";
      NULL = "NULL";
    }
  }

  protected String getLocalizedOrder(String key, String fallBack) {
    String translation = Resources.getOrderTranslation(key, currentFaction.getLocale());
    if (translation == key)
      return fallBack;
    else
      return translation;
  }

  /**
   * Adss an order to the current unit's new orders.
   * 
   * @param order The new order
   * @param changed Set to true if this is a change (not merely a copy of an old order)
   */
  protected void addNewOrder(String order, boolean changed) {
    changedOrders = changedOrders || changed;
    if (!changed) {
      for (String pattern : removedOrderPatterns)
        if (order.matches(pattern))
          return;
    }
    newOrders.add(order);
  }

  /**
   * Adds an error line
   * 
   * @param line The current order line
   * @param hint
   */
  protected void addError(String hint) {
    error = line;
    errMsg = hint;
    addNewOrder(COMMENTOrder + " TODO: Fehler im Skript in Zeile " + error + ": " + hint, true);
  }

  /**
   * Registers a pattern. All lines matching this pattern (except $script orders!) will be removed
   * from here on.
   * 
   * @param pattern
   */
  protected void removeOrdersLike(String pattern, boolean retroActively) {
    if (retroActively) {
      for (Iterator<String> it = newOrders.iterator(); it.hasNext();) {
        String line = it.next();
        if (line.matches(pattern)) {
          it.remove();
        }
      }
    }
    removedOrderPatterns.add(pattern);
  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  /**
   * Adds a warning message (with a to do tag to the unit's orders.
   * 
   * @param unit
   * @param text
   */
  public static void addWarning(Unit unit, String text) {
    helper.addOrder(unit, "; -------------------------------------");
    helper.addOrder(unit, "; TODO: " + text);
    setConfirm(unit, false);
  }

  /**
   * If confirm is false, mark unit as not confirmable. If confirm is true, mark it as confirmable
   * if it has not been marked as unconfirmable before (unconfirm always overrides confirm).
   * 
   * @param u
   * @param confirm
   */
  public static void setConfirm(Unit u, boolean confirm) {
    String tag = getProperty(u, "confirm");
    if (confirm) {
      if (tag.length() == 0) {
        setProperty(u, "confirm", "1");
      }
    } else if (!confirm) {
      setProperty(u, "confirm", "0");
    }
  }

  /**
   * Add a property (in form of a tag) to the unit.
   * 
   * @param u
   * @param tagName
   * @param value
   * @see #getProperty(Unit, String)
   */
  public static void setProperty(Unit u, String tagName, String value) {
    u.putTag(scriptMarker + "." + tagName, value);
    // u.addOrder("; $cript " + tagName + ":" + value, false, 0);
  }

  /**
   * Returns a property value (from a tag) from the unit.
   * 
   * @param u
   * @param tagName
   * @return The value of property <code>tagName</code> or "" if the property has not been set.
   */
  public static String getProperty(Unit u, String tagName) {
    String tag = u.getTag(scriptMarker + "." + tagName);
    return tag == null ? "" : tag;
  }

  /**
   * Returns the ItemType in the rules matching <code>name</code>.
   * 
   * @param name A item type name, like "Silber"
   * @return The ItemType corresponding to name or <code>null</code> if this ItemType does not
   *         exist.
   */
  public static ItemType getItemType(String name) {
    return world.rules.getItemType(name);
  }

  /**
   * Returns the SkillType in the rules matching <code>name</code>.
   * 
   * @param name A item type name, like "Hiebwaffen"
   * @return The SkillType corresponding to name or <code>null</code> if this SkillType does not
   *         exist.
   */
  public static SkillType getSkillType(String name) {
    return world.rules.getSkillType(name);
  }

  /**
   * Notifies the interface that the unit should be updated.
   * 
   * @param u This unit is updated in the UI
   * @deprecated I don't think this is needed any more.
   */
  @Deprecated
  public static void notifyMagellan(Unit u) {
    helper.updateUnit(u);
  }

  /**
   * Adds a GIVE order to the unit's orders, like <code>GIVE receiver [EACH] amount item</code>.
   * 
   * @param unit The order is added to this unit's orders
   * @param receiver
   * @param item
   * @param amount
   * @param each
   */
  public static void addGiveOrder(Unit unit, Unit receiver, String item, int amount, boolean each) {
    helper.addOrder(unit, getGiveOrder(unit, receiver.getID().toString(), item, amount, each));
  }

  /**
   * Returns a line like <code>GIVE receiver [EACH] amount item</code>.
   * 
   * @param unit
   * @param receiver
   * @param item
   * @param amount If <code>amount == Integer.MAX_VALUE</code>, amount is replaced by ALL
   * @param each
   * @return a line like <code>GIVE receiver [EACH] amount item</code>.
   */
  public static String getGiveOrder(Unit unit, String receiver, String item, int amount,
      boolean each) {
    return helper.getGiveOrder(unit, receiver, item, amount, each);
  }

  /**
   * Adds a RESERVE order to the unit's orders, like <code>RESERVE [EACH] amount item</code>.
   * 
   * @param unit The order is added to this unit's orders
   * @param item
   * @param amount
   * @param each
   */
  public static void addReserveOrder(Unit unit, String item, int amount, boolean each) {
    helper.addOrder(unit, getReserveOrder(unit, item, amount, each));
  }

  /**
   * Returns a line like <code>RESERVE [EACH] amount item</code>.
   * 
   * @param unit
   * @param item
   * @param amount
   * @param each
   * @return a line like <code>GIVE receiver [EACH] amount item</code>.
   */
  public static String getReserveOrder(Unit unit, String item, int amount, boolean each) {
    return helper.getReserveOrder(unit, item, amount, each);
  }

  /**
   * Return the amount of item that a unit has.
   * 
   * @param unit
   * @param item
   * @return The amount of item in the unit's items
   */
  public static int getItemCount(Unit unit, String item) {
    return helper.getItemCount(unit, item);
  }

  /**
   * Return the amount of silver of the unit.
   * 
   * @param unit
   * @return The amount of silver in this unit's items
   */
  public static int getSilber(Unit unit) {
    return Math.max(getItemCount(unit, "Silver"), getItemCount(unit, "Silber"));
  }

  /**
   * Returns true if the item is usable with weaponSkill, false otherwise.
   */
  protected static boolean isUsable(ItemType type, SkillType weaponSkill) {
    return (type.getUseSkill() != null && type.getUseSkill().getSkillType().equals(weaponSkill));
  }

  /**
   * Returns true if the item is usable with weaponSkill, false otherwise.
   */
  protected static boolean isUsable(Item item, SkillType weaponSkill) {
    return isUsable(item.getItemType(), weaponSkill);
  }

  /**
   * Returns true if skill is a weapon skill.
   */
  protected static boolean isWeaponSkill(Skill skill) {
    return skill.getSkillType().getID().equals(EresseaConstants.S_HIEBWAFFEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_STANGENWAFFEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_BOGENSCHIESSEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_ARMBRUSTSCHIESSEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_KATAPULTBEDIENUNG);
  }

  /**
   * Parses all units in the region for orders like "; $xyz$sl" and adds a tag with name
   * "ejcTaggableComparator5" and value "xyz" for them.
   * 
   * @param r
   */
  public static void parseShipLoaderTag(magellan.library.Region r) {
    for (Unit u : r.units()) {
      String name = null;
      for (String line : u.getOrders()) {
        java.util.regex.Pattern p = Pattern.compile(".*[$]([^$]*)[$]sl.*");
        java.util.regex.Matcher m = p.matcher(line);
        if (m.matches()) {
          name = m.group(1);
        }
      }
      if (name != null) {
        u.putTag("ejcTaggableComparator5", name);
      } else {
        u.removeTag("ejcTaggableComparator5");
      }
    }
  }

  /**
   * Parses all units in the region for orders like "; $xyz$verlassen" and adds a tag with name
   * "ejcTaggableComparator5" and value "xyz" for them.
   * 
   * @param r
   */
  public static void parseShipLoaderTag2(magellan.library.Region r) {
    for (Unit u : r.units()) {
      String name = null;
      for (String line : u.getOrders()) {
        java.util.regex.Pattern p = Pattern.compile(".*[$]([^$]*)[$]verlassen.*");
        java.util.regex.Matcher m = p.matcher(line);
        if (m.matches()) {
          name = m.group(1);
        }
      }
      if (name != null && name.equals("633")) {
        u.putTag("ejcTaggableComparator5", name);
      } else {
        u.removeTag("ejcTaggableComparator5");
      }
    }
  }

  /**
   * Test classes for Beanshell
   */
  static class A {

    /**
     * @return "A.b"
     */
    public static String b() {
      return "A.b";
    }
  }

  class B {

    public String b() {
      return "B.b";
    }

  }

  void testA(Unit u) {
    helper.addOrder(u, A.b());
    helper.addOrder(u, (new B()).b());
  }

  // ---start comment for BeanShell
  /**
   * Makes this code usable as input to the CommandParser. Tries to read the source of this file and
   * strip it from generics and other advanced stuff that BeanShell dosen't understand.
   * 
   * @param args this is ignored
   */
  public static void main(String[] args) {
    File file = new File("./src/magellan/plugin/extendedcommands/stm/E3CommandParser.java");
    try {
      LineNumberReader reader = new LineNumberReader(new FileReader(file));

      System.out.println("// created by E3CommandParser.main() at "
          + Calendar.getInstance().getTime());
      boolean commentMode = false;
      boolean unCommentMode = false;
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        if (line.matches(" *// ---start comment for BeanShell")) {
          commentMode = true;
          System.out.println("// ---commented for BeanShell");
          continue;
        }
        if (line.matches(" *// ---stop comment for BeanShell")) {
          commentMode = false;
          System.out.println(line);
          continue;
        }
        if (line.matches(" *// ---start uncomment for BeanShell")) {
          unCommentMode = true;
          System.out.println("// ---uncommented for BeanShell");
          continue;
        }
        if (line.matches(" *// ---stop uncomment for BeanShell")) {
          unCommentMode = false;
          System.out.println(line);
          continue;
        }
        if (commentMode) {
          System.out.print("//");
          System.out.println(line);
          continue;
        }
        if (unCommentMode) {
          System.out.println(line.substring(line.indexOf("//") + 2));
          continue;
        }
        if (line.matches(" *package [a-z.]*;")) {
          continue;
        }

        // remove generics
        String lastLine = null, newLine = line;
        boolean changed = false;
        while (newLine.matches(".*<[A-Za-z0-9_, ]*>.*") && !isJavaComment(newLine)
            && !newLine.equals(lastLine)) {
          lastLine = newLine;
          newLine = newLine.replaceFirst("<[A-Za-z0-9_, ]*>", "");
          changed = true;
        }
        // remove @ directives
        if (!isJavaComment(newLine)) {
          newLine = newLine.replaceFirst("@.*", "// $0");
        }
        if (changed) {
          System.out.print("// ");
          System.out.println(line);
        }
        System.out.println(newLine);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isJavaComment(String line) {
    return line.matches("[ ]*//.*") || line.matches("[ ]*[*].*");
  }
  // ---stop comment for BeanShell
}

class Supply {

  Unit unit;
  String item;
  int amount;

  public Supply(Unit unit, String item, int amount) {
    super();
    this.unit = unit;
    this.item = item;
    this.amount = amount;
  }

  public Unit getUnit() {
    return unit;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int i) {
    amount = i;
  }

  public String getItem() {
    return item;
  }

  public void reduceAmount(int change) {
    if (amount == Integer.MAX_VALUE || amount == Integer.MIN_VALUE)
      return;
    long newValue = (long) amount - (long) change;
    if (newValue > Integer.MAX_VALUE) {
      amount = Integer.MAX_VALUE;
    } else if (newValue < Integer.MIN_VALUE) {
      amount = Integer.MIN_VALUE;
    } else {
      amount = (int) newValue;
    }
  }

  @Override
  public String toString() {
    return unit + " has " + amount + " " + item;
  }

}

class Need extends Supply {

  private int minAmount;

  public Need(Unit unit, String item, int minAmount, int maxAmount) {
    super(unit, item, maxAmount);
    this.minAmount = minAmount;
  }

  public int getMinAmount() {
    return minAmount;
  }

  public void setMinAmount(int amount) {
    minAmount = amount;
  }

  public void reduceMinAmount(int change) {
    if (amount == Integer.MAX_VALUE || amount == Integer.MIN_VALUE)
      return;
    long newValue = (long) minAmount - (long) change;
    if (newValue > Integer.MAX_VALUE) {
      minAmount = Integer.MAX_VALUE;
    } else if (newValue < Integer.MIN_VALUE) {
      minAmount = Integer.MIN_VALUE;
    } else {
      minAmount = (int) newValue;
    }
  }

  public int getMaxAmount() {
    return getAmount();
  }

  public void setMaxAmount(int amount) {
    setAmount(amount);
  }

  public void reduceMaxAmount(int change) {
    reduceAmount(change);
  }

  @Override
  public String toString() {
    return unit + " needs " + minAmount + "/" + amount + " " + item;
  }
}

// ---start uncomment for BeanShell
// void soldier(Unit unit) { E3CommandParser.soldier(unit); }
// ---stop uncomment for BeanShell
