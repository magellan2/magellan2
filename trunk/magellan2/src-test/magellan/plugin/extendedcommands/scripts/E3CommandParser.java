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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import magellan.plugin.extendedcommands.ExtendedCommandsHelper;

// ---start uncomment for BeanShell
// import magellan.library.*;
// import java.util.*;
// ---stop uncomment for BeanShell

/**
 * Call from the script of your faction with<br />
 * "(new E3CommandParser(world, helper)).execute(container);" or
 * "(new E3CommandParser(world, helper)).execute(helper.getFaction("drac"), (Region) container);".
 * 
 * @author stm
 */
public class E3CommandParser {

  /** A standard soldier's endurance skill should be this fraction of his (first row) weapon skill */
  public static final float ENDURANCERATIO_FRONT = .6f;
  /** A standard soldier's endurance skill should be this fraction of his (second row) weapon skill */
  public static final float ENDURANCERATIO_BACK = .35f;

  /** Unit limit, used to warn if we get too many units. */
  public static final int EINHEITENLIMIT = 250;

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
  /** The KRÄUTER order parameter */
  public static String KRAUTOrder = "KRAUT";
  /** The LUXUS order parameter */
  public static String LUXUSOrder = "LUXUS";
  /** The persistent comment order */
  public static String PCOMMENTOrder = EresseaConstants.O_PCOMMENT;
  /** The persistent comment order */
  public static String COMMENTOrder = EresseaConstants.O_COMMENT;
  /** The LEARN order */
  public static String LEARNOrder = "LERNE";
  /** The TEACH order */
  public static String TEACHOrder = "LEHRE";
  /** The ENTERTAIN order */
  private static String ENTERTAINOrder = "UNTERHALTE";
  /** The TAX order */
  private static String TAXOrder = "TREIBE";
  /** The WORK order */
  private static String WORKOrder = "ARBEITE";
  /** The BUY order */
  private static String BUYOrder = "KAUFE";
  /** The SELL order */
  private static String SELLOrder = "VERKAUFE";
  /** The MAKE order */
  private static String MAKEOrder = "MACHE";
  /** The RESEARCH order */
  private static String RESEARCHOrder = "FORSCHE";

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
  /** The HIDDEN warning type token */
  public static String W_HIDDEN = "versteckt";
  /** The ALWAYS warning type token */
  public static final String W_ALWAYS = "immer";
  /** The BEST token (for soldier) */
  public static String BEST = "best";
  /** The NULL token (for soldier) */
  public static String NULL = "null";
  /** The NOT token (for auto and others) */
  public static String NOT = "nicht";
  /** The LONG token (for clear) */
  public static String LONG = "lang";

  /** warning constants */
  protected static final int C_ALWAYS = 0, C_AMOUNT = 1, C_UNIT = 2, C_HIDDEN = 3, C_NEVER = 4;

  private static final String S_ENDURANCE = EresseaConstants.S_AUSDAUER.toString();

  private OrderParser parser;

  /**
   * Creates and initializes the parser.
   * 
   * @param world
   * @param helper
   */
  public E3CommandParser(GameData world, ExtendedCommandsHelper helper) {
    E3CommandParser.world = world;
    E3CommandParser.helper = helper;
    parser = world.getGameSpecificStuff().getOrderParser(world);
  }

  protected OrderParser getParser() {
    return parser;
  }

  // variables available in scripts; this is here mainly to be able to test this class outside
  // BeanShell
  static GameData world;
  static ExtendedCommandsHelper helper;

  private Unit someUnit;

  // variables for current script state
  private Faction currentFaction;
  private Region currentRegion;
  private Unit currentUnit;
  private ArrayList<String> newOrders;
  private String currentOrder;
  private int line;
  private int error;
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
   * Lists of allowed units for Erlaube/Ueberwache
   */
  protected Map<Faction, Set<Unit>> allowedUnits = new HashMap<Faction, Set<Unit>>();
  /**
   * Lists of required units for Verlange/Ueberwache
   */
  protected Map<Faction, Set<Unit>> requiredUnits = new HashMap<Faction, Set<Unit>>();

  /**
   * Current state for the Lösche command
   */
  protected String clear = null;

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

    if (faction.units().size() >= EINHEITENLIMIT) {
      addWarning(someUnit, "Einheitenlimit erreicht (" + faction.units().size() + "/"
          + EINHEITENLIMIT + ")! ");
    }

    if (faction.units().size() * 1.1 > EINHEITENLIMIT) {
      addWarning(someUnit, "Einheitenlimit fast erreicht (" + faction.units().size() + "/"
          + EINHEITENLIMIT + ")! ");
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
        u.addOrder("; autoconfirmed");
      } else if ("0".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(false);
      }
      notifyMagellan(u);
    }
  }

  protected void execute(Region region) {
    currentRegion = region;
    initSupply();

    for (Unit u : region.units()) {
      if (u.getFaction().equals(currentFaction)) {
        currentUnit = u;
        // uncomment the following lines if you have the newest nighthly build of Magellan
        // ---start uncomment for BeanShell
        // helper.getUI().setProgress(region.toString() + " - " + u.toString(), 0);
        // ---stop uncomment for BeanShell
        parseScripts();
      }
    }
    // uncomment the following lines if you have the newest nighthly build of Magellan
    // ---start uncomment for BeanShell
    // helper.getUI().setProgress(region.toString() + " - postprocessing ", 0);
    // ---stop uncomment for BeanShell
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
    // uncomment the following lines if you have the newest nighthly build of Magellan
    // ---start uncomment for BeanShell
    for (Building b : world.getBuildings()) {
      if (helper.hasScript(b)) {
        buildingScripts++;
      }
    }
    for (Ship s : world.getShips()) {
      if (helper.hasScript(s)) {
        shipScripts++;
      }
    }
    for (Unit u : world.getUnits()) {
      if (helper.hasScript(u)) {
        u.addOrder(COMMENTOrder + " has script");
        unitScripts++;
      }
    }
    for (Region r : world.getRegions()) {
      if (helper.hasScript(r)) {
        regionScripts++;
      }
    }
    // ---stop uncomment for BeanShell
    someUnit.addOrderAt(0, "; " + unitScripts + " unit scripts, " + buildingScripts
        + " building scripts, " + shipScripts + " ship scripts, " + regionScripts
        + " region scripts", true);
  }

  /**
   * Parses the orders of the unit u for commands of the form "// $cript ..." and tries to execute
   * them. Known commands:<br />
   * <tt>// $cript +X text</tt> -- If X<=1 then a warning containing text is added to the unit's
   * orders. Otherwise X is decreased by one.<br />
   * <tt>// $cript auto [nicht]</tt> -- autoconfirm orders<br />
   * <code>// $cript Loeschen [lang]</code> -- clears all orders except comments<br />
   * <code>// $cript GibWenn receiver [JE] amount item [warning]</code> -- add give order (if
   * possible)<br />
   * <code>// $cript Benoetige minAmount [maxAmount] item</code><br />
   * <code>// $cript Benoetige JE amount item</code><br />
   * <code>// $cript Benoetige ALLES [item]</code> -- acquire things from other units<br />
   * <code>// $cript BenoetigeFremd unit [JE] minAmount [maxAmount] item</code><br />
   * <code>// $cript Soldat [Talent [Waffe [Schild [Rüstung]]]] [nie|Talent|Waffe|Schild|Rüstung]</code>
   * -- learn skill and reserve equipment<br />
   * <code>// $cript Lerne Talent1 Stufe1 [[Talent2 Stufe2]...]</code> -- learn skills in given
   * ratio <br />
   * <code>// $cript BerufBotschter [Talent]</code> -- earn money if necessary, otherwise learn
   * skill<br />
   * <code>// $cript Ueberwache</code> -- look out for unknown units<br />
   * <code>// $cript Erlaube faction unit [unit...]</code> -- allow units for Ueberwache<br />
   * <code>// $cript Verlange faction unit [unit...]</code> -- allow and require units for
   * Ueberwache<br />
   * <code>// $cript Ernaehre [amount]</code> -- earn money<br />
   * <code>// $cript Handel []</code> -- trade luxuries<br />
   * <code>// $cript Steuermann minSilver maxSilver</code> -- be responsible for ship<br />
   * <code>// $cript Mannschaft skill</code> -- be crew and learn<br />
   * <code>// $cript Quartiermeister [[amount item]...]</code> -- be lookout<br />
   * <code>// $cript Sammler [interval]</code> -- collect and research HERBS<br />
   */
  protected void parseScripts() {
    newOrders = new ArrayList<String>();
    removedOrderPatterns = new ArrayList<String>();
    changedOrders = false;
    error = -1;
    // errMsg = null;
    line = 0;
    allowedUnits.clear();
    requiredUnits.clear();
    clear = null;

    // NOTE: must not change currentUnit's orders directly! Always change newOrders!
    for (Object o : currentUnit.getOrders()) {
      ++line;
      currentOrder = (String) o;
      String[] tokens = detectScriptCommand(currentOrder);
      if (tokens == null) {
        // add order if
        if (clear == null
            || currentOrder.trim().startsWith(EresseaConstants.O_PCOMMENT)
            || currentOrder.trim().startsWith(EresseaConstants.O_COMMENT)
            || (clear == LONG && (world.getGameSpecificStuff().getOrderChanger().isLongOrder(
                currentOrder) || currentOrder.trim().startsWith(EresseaConstants.O_PERSISTENT)))) {
          addNewOrder(currentOrder, false);
        } else {
          changedOrders = true;
        }
      } else {
        String command = tokens[0];
        if (command.startsWith("+")) {
          commandWarning(tokens);
          changedOrders = true;
        } else {
          // order remains
          addNewOrder(currentOrder, false);

          if (command.equals("auto")) {
            if (tokens.length == 2 && NOT.equals(tokens[1])) {
              setConfirm(currentUnit, false);
            } else {
              setConfirm(currentUnit, true);
            }
          } else if (command.equals("Loeschen")) {
            commandClear(tokens);
          } else if (command.equals("GibWenn")) {
            commandGibWenn(tokens);
          } else if (command.equals("Benoetige")) {
            commandBenoetige(tokens);
            // } else if (command.equals("Versorge")) {
            // commandVersorge(tokens);
          } else if (command.equals("Soldat")) {
            commandSoldier(tokens);
          } else if (command.equals("Lerne")) {
            commandLearn(tokens);
          } else if (command.equals("BerufBotschafter")) {
            commandEmbassador(tokens);
          } else if (command.equals("Ueberwache")) {
            commandMonitor(tokens);
          } else if (command.equals("Erlaube") || command.equals("Verlange")) {
            commandAllow(tokens);
          } else if (command.equals("Ernaehre")) {
            commandEarn(tokens);
          } else if (command.equals("Handel")) {
            commandTrade(tokens);
          } else if (command.equals("Steuermann")) {
            if (tokens.length < 3) {
              addNewError("too few parameters");
            } else {
              commandBenoetige(new String[] { "Benoetige", tokens[1], tokens[2], "Silber" });
            }
          } else if (command.equals("Mannschaft")) {
            commandSoldier(new String[] { "Soldat", tokens[1], "Talent" });
            setConfirm(currentUnit, true);
          } else if (command.equals("Quartiermeister")) {
            commandQuartermaster(tokens);
          } else if (command.equals("Sammler")) {
            commandCollector(tokens);
          } else {
            addNewOrder(currentOrder, false);
            addNewError("unbekannter Befehl: " + command);
          }
        }

        currentOrder = null;
      }
    }
    if (changedOrders) {
      currentUnit.setOrders(newOrders);
    }
    notifyMagellan(currentUnit);

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
   * <code>// $cript Loeschen [lang]</code><br />
   * Remove orders except comments from here on. If lang=={@value #LONG}, remove only long and
   * permanent (@) orders, otherwise remove all orders.
   * 
   * @param tokens
   */
  protected void commandClear(String[] tokens) {
    if (tokens.length == 1) {
      clear = ALLOrder;
    } else if (tokens.length == 2 && tokens[1].equals(LONG)) {
      clear = LONG;
    } else {
      addNewError("too many parameters");
    }
  }

  /**
   * <code>// $cript GibWenn receiver [[JE] amount|ALLES|KRAUT|LUXUS] [item] [warning]</code><br />
   * Adds a GIB order to the unit. Warning may be one of "immer", "Menge", "Einheit", "versteckt"
   * "nie". "versteckt" informiert nur, wenn die Einheit nicht da ist, übergibt aber trotzdem.
   */
  protected void commandGibWenn(String[] tokens) {

    if (tokens.length < 3 && tokens.length > 6) {
      addNewError("Fehler in GibWenn");
      return;
    }

    Unit target = helper.getUnit(tokens[1]);

    // test if EACH is present
    int je = 0;
    if (EACHOrder.equals(tokens[2])) {
      je = 1;
      if (ALLOrder.equals(tokens[3]) || KRAUTOrder.equals(tokens[3])
          || LUXUSOrder.equals(tokens[3])) {
        addNewError("JE " + tokens[3] + " geht nicht");
        return;
      }
    }

    // get warning type
    int warning = -1;
    if (W_ALWAYS.equals(tokens[tokens.length - 1])) {
      warning = C_ALWAYS;
    } else if (W_AMOUNT.equals(tokens[tokens.length - 1])) {
      warning = C_AMOUNT;
    } else if (W_UNIT.equals(tokens[tokens.length - 1])) {
      warning = C_UNIT;
    } else if (W_NEVER.equals(tokens[tokens.length - 1])) {
      warning = C_NEVER;
    } else if (W_HIDDEN.equals(tokens[tokens.length - 1])) {
      warning = C_HIDDEN;
    }

    // handle GIB xyz ALLES
    if (ALLOrder.equalsIgnoreCase(tokens[2])) {
      if (!testUnit(tokens[1], target, warning))
        return;
      if (tokens.length == (warning == -1 ? 3 : 4)) {
        for (Item item : currentUnit.getItems()) {
          addNewOrder(getGiveOrder(currentUnit, tokens[1], item.getOrderName(), Integer.MAX_VALUE,
              false), true);
        }
        return;
      }
    }

    // handle GIB xyz KRAUT
    if (KRAUTOrder.equalsIgnoreCase(tokens[2])) {
      if (testUnit(tokens[1], target, warning)) {
        if (tokens.length > (warning == -1 ? 3 : 4)) {
          addNewError("zu viele Parameter");
        }
        for (Item item : currentUnit.getItems())
          if (item.getItemType().getCategory().equals(world.rules.getItemCategory("herbs"))) {
            addNewOrder(getGiveOrder(currentUnit, tokens[1], item.getOrderName(),
                Integer.MAX_VALUE, false), true);
          }
      }
      return;
    }

    // handle GIB xyz LUXUS
    if (LUXUSOrder.equalsIgnoreCase(tokens[2])) {
      if (testUnit(tokens[1], target, warning)) {
        if (tokens.length > (warning == -1 ? 3 : 4)) {
          addNewError("zu viele Parameter");
        }
        for (Item item : currentUnit.getItems())
          if (item.getItemType().getCategory().equals(world.rules.getItemCategory("luxuries"))) {
            addNewOrder(getGiveOrder(currentUnit, tokens[1], item.getOrderName(),
                Integer.MAX_VALUE, false), true);
          }
      }
      return;
    }

    if (tokens.length != 4 + je + (warning == -1 ? 0 : 1)) {
      addNewError("zu viele Parameter");
    }

    if (warning == -1) {
      warning = C_ALWAYS;
    }

    // get amount
    String item = tokens[3 + je];
    int amount = 0;
    if (ALLOrder.equalsIgnoreCase(tokens[2 + je])) {
      if (KRAUTOrder.equals(item) || LUXUSOrder.equals(item)) {
        addNewError("GIB xyz ALLES " + item + " statt GIB xyz " + item);
      }
      amount = getItemCount(currentUnit, item);
    } else {
      try {
        amount = Integer.parseInt(tokens[2 + je]);
      } catch (NumberFormatException e) {
        amount = 0;
        addNewError("Zahl oder ALLES erwartet");
        return;
      }
    }

    if (!testUnit(tokens[1], target, warning))
      return;

    // get full amount (=amount * persons)
    int fullAmount = amount;
    if (je == 1) {
      if (target == null) {
        addNewMessage("Einheit nicht gefunden; kann Menge nicht überprüfen");
      } else {
        fullAmount = target.getModifiedPersons() * amount;
      }
    }

    // check availibility
    if (getItemCount(currentUnit, item) < fullAmount) {
      if (warning == C_ALWAYS || warning == C_AMOUNT || warning == C_HIDDEN) {
        addNewWarning("zu wenig " + item);
      } else {
        addNewMessage("zu wenig " + item);
      }
      amount = getItemCount(currentUnit, item);
      je = 0;
    }

    // make GIVE order
    if (amount > 0) {
      if (amount == getItemCount(currentUnit, item) && je != 1) {
        addNewOrder(getGiveOrder(currentUnit, tokens[1], item, Integer.MAX_VALUE, je == 1), true);
      } else {
        addNewOrder(getGiveOrder(currentUnit, tokens[1], item, amount, je == 1), true);
      }
      Supply supply = getSupply(item, currentUnit);
      if (supply == null) {
        addNewWarning("supply 0");
        return;
      }
      supply.reduceAmount(fullAmount);
      if (target != null) {
        addNeed(item, target, -amount, -amount);
      }
    }

  }

  private boolean testUnit(String sOther, Unit other, int warning) {
    if (other == null || other.getRegion() != currentUnit.getRegion()) {
      if (warning == C_AMOUNT || warning == C_HIDDEN || warning == C_NEVER) {
        addNewOrder("; " + sOther + " nicht da", true);
      } else {
        addNewWarning(sOther + " nicht da");
      }
      return warning == C_HIDDEN;
    }
    return true;
  }

  /**
   * <code>// $cript Benoetige minAmount [maxAmount] item</code><br />
   * <code>// $cript Benoetige JE amount item</code><br />
   * <code>// $cript Benoetige ALLES [item]</code><br />
   * <code>// $cript BenoetigeFremd unit (JE amount)|(minAmount [maxAmount]) item</code><br />
   * Tries to transfer the maxAmount of item from other units to this unit. Issues warning if
   * minAmount cannot be supplied. <code>Benoetige JE</code> tries to reserve 1 item for every
   * person in the unit. <code>Benoetige ALLES item</code> is equivalent to
   * <code>Benoetige 0 infinity item</code>, <code>Benoetige ALLES</code> is equivalent to
   * <code>Benoetige ALLES</code> for every itemtype in the region.<br/>
   * <code>BenoetigeFremd</code> does the same, but for the given unit instead of the current unit.
   */
  protected void commandBenoetige(String[] tokens) {
    Unit unit = currentUnit;
    if (tokens[0].equals("BenoetigeFremd")) {
      unit = helper.getUnit(tokens[1]);
      if (unit.getRegion() != currentUnit.getRegion()) {
        addNewMessage("target unit not in same region");
        return;
      }
      // erase unit token for easier processing afterwards
      tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
      tokens[0] = "BenoetigeFremd";
    }

    if (tokens.length < 2 || tokens.length > 5) {
      addNewError("Fehler in Benoetige");
      return;
    }

    try {
      if (ALLOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNeed(tokens[2], unit, 0, Integer.MAX_VALUE);
        } else {
          for (String item : supplyMap.keySet()) {
            addNeed(item, unit, 0, Integer.MAX_VALUE);
          }
        }
      } else if (EACHOrder.equals(tokens[1])) {
        if (tokens.length != 4) {
          addNewError("Ungültige Argumente für Benoetige JE x Ding");
        } else {
          int amount = unit.getPersons() * Integer.parseInt(tokens[2]);
          String item = tokens[3];
          addNeed(item, unit, amount, amount);
        }
      } else {
        int minAmount = Integer.parseInt(tokens[1]);
        int maxAmount = tokens.length == 3 ? minAmount : Integer.parseInt(tokens[2]);
        String item = tokens[tokens.length - 1];
        addNeed(item, unit, minAmount, maxAmount);
      }
    } catch (NumberFormatException exc) {
      addNewError("Ungültige Zahl in Benoetige");
    }
  }

  /**
   * <code>// $script +x [Arguments...]</code><br />
   * If x<=1, the rest of the line is added as a warning to this unit. Otherwise x is decreased by
   * one. If x==1, the order is removed.
   */
  protected void commandWarning(String[] tokens) {
    int delay = -1;
    try {
      delay = Integer.parseInt(tokens[0].substring(1));
    } catch (NumberFormatException e) {
      addNewError("Zahl erwartet");
      return;
    }
    if (delay <= 1) {
      StringBuilder warning = new StringBuilder("; TODO: ");
      String foo = currentOrder.substring(currentOrder.indexOf("+"));
      warning.append(foo.indexOf(" ") >= 0 ? foo.substring(foo.indexOf(" ") + 1) : "");
      addNewOrder(warning.toString(), true);
    }
    if (delay != 1) {
      StringBuilder newCommand =
          new StringBuilder(PCOMMENTOrder).append(" ").append(scriptMarker).append(" +");
      newCommand.append(Math.max(0, delay - 1));
      for (int i = 1; i < tokens.length; ++i) { // skip "+x"
        newCommand.append(" ").append(tokens[i]);
      }
      addNewOrder(newCommand.toString(), true);
    }
  }

  /**
   * <code>// $cript Lerne Talent1 Stufe1 [[Talent2 Stufe2]...]</code><br />
   * Tries to learn skills in given ratio. For example,
   * <code>// $cript Lerne Hiebwaffen 10 Ausdauer 5</code> tries to learn to (Hiebwaffen 2, Ausdauer
   * 1), (Hiebwaffen 4, Ausdauer 2), and so forth.
   */
  protected void commandLearn(String[] tokens) {
    List<Skill> targetSkills = new LinkedList<Skill>();

    for (int i = 1; i < tokens.length; i++) {
      try {
        if (i < tokens.length - 1) {
          SkillType skill = world.rules.getSkillType(tokens[i++]);
          int level = Integer.parseInt(tokens[i]);
          if (skill == null) {
            addNewError("invalid skill " + tokens[i - 1]);
          }
          targetSkills.add(new Skill(skill, 0, level, 1, true));
        } else {
          addNewError("stray token " + tokens[i]);
        }
      } catch (NumberFormatException e) {
        addNewError("invalid skill level " + tokens[i]);
      }
    }

    learn(currentUnit, targetSkills);
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

  protected void commandEmbassador(String[] tokens) {
    if (helper.getSilver(currentUnit) < 100) {
      if (hasEntertain()) {
        addNewOrder(ENTERTAINOrder, true);
      } else if (hasWork()) {
        addNewOrder(WORKOrder, true);
      } else {
        addNewWarning("unit is going to starve");
      }
    } else if (tokens.length > 1) {
      learn(currentUnit, Collections.singleton(getSkill(tokens[1], 10)));
      if (tokens.length > 2) {
        addNewError("too many parameters");
      }
    } else {
      learn(currentUnit, Collections.singleton(getSkill(EresseaConstants.S_WAHRNEHMUNG.toString(),
          10)));
    }
  }

  protected void commandMonitor(String[] tokens) {
    if (tokens.length > 1) {
      addNewError("too many parameters");
    }

    // check if region units are allowed
    Map<Faction, Unit> warnings = new HashMap<Faction, Unit>();
    for (Unit u : currentRegion.units()) {
      if (u.getFaction() != currentUnit.getFaction()) {
        if (!allowedUnits.containsKey(u.getFaction())
            || !allowedUnits.get(u.getFaction()).contains(u)) {
          if (!requiredUnits.containsKey(u.getFaction())
              || !requiredUnits.get(u.getFaction()).contains(u)) {
            warnings.put(u.getFaction(), u);
          }
        }
      }
    }

    for (Entry<Faction, Unit> entry : warnings.entrySet()) {
      addNewWarning("faction " + entry.getKey() + " has unauthorized units: " + entry.getValue());
    }

    // check if required units are present
    for (Faction f : requiredUnits.keySet()) {
      for (Unit u : requiredUnits.get(f)) {
        if (u.getRegion() != currentUnit.getRegion()) {
          addNewWarning("unit " + u + " of faction " + f + " is not here any more.");
        }
      }
    }
  }

  protected void commandAllow(String[] tokens) {
    if (tokens.length < 3) {
      addNewError("too few parameters");
    }

    Faction faction = helper.getFaction(tokens[1]);
    Map<Faction, Set<Unit>> map;
    if (tokens[0].equals("Erlaube")) {
      map = allowedUnits;
    } else {
      map = requiredUnits;
    }
    if (faction != null) {
      Set<Unit> set = map.get(faction);
      if (set == null) {
        set = new HashSet<Unit>();
        map.put(faction, set);
      }
      for (int i = 2; i < tokens.length; ++i) {
        Unit u = helper.getUnit(tokens[i]);
        if (u != null) {
          set.add(u);
        }
      }
    }
  }

  protected void commandEarn(String[] tokens) {
    int amount = -1;
    if (tokens.length > 1) {
      try {
        amount = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        addNewError("invalid number " + tokens[1]);
        return;
      }
      if (tokens.length > 2) {
        addNewError("too many parameters");
      }
    }

    // remove previous orders
    removeOrdersLike(TAXOrder + ".*", true);
    removeOrdersLike(ENTERTAINOrder + ".*", true);
    removeOrdersLike(WORKOrder + ".*", true);

    int maxWorkers =
        Utils.getIntValue(world.getGameSpecificRules().getMaxWorkers(currentRegion), 0);
    int workers = Math.min(maxWorkers, currentRegion.getPeasants());
    Skill entertaining =
        currentUnit.getModifiedSkill(world.rules.getSkillType(EresseaConstants.S_UNTERHALTUNG));
    Skill taxing =
        currentUnit.getModifiedSkill(world.rules.getSkillType(EresseaConstants.S_STEUEREINTREIBEN));
    int entertain = 0, tax = 0;
    if (entertaining != null) {
      entertain = Math.min(currentRegion.maxEntertain(), 20 * entertaining.getLevel());
    }
    if (taxing != null && isSoldier(currentUnit)) {
      tax = Math.min(currentRegion.getSilver(), 20 * taxing.getLevel());
    }

    if (tax > entertain) {
      addNewOrder(TAXOrder + " " + (amount > 0 ? amount : "") + COMMENTOrder + " " + tax, true);
      if (tax > currentRegion.getSilver() + workers * 10 - currentRegion.getPeasants() * 10) {
        addNewWarning("peasants will starve");
      }
    } else if (entertain > 0) {
      addNewOrder(ENTERTAINOrder + " " + (amount > 0 ? amount : "") + COMMENTOrder + " " + tax,
          true);
      if (amount > currentRegion.maxEntertain()) {
        addNewWarning("too many workers");
      }
    } else {
      addNewOrder(WORKOrder + " " + (amount > 0 ? amount : ""), true);
      if ((maxWorkers - workers) * 10 < Math.min(amount, currentUnit.getModifiedPersons() * 10)) {
        addNewWarning("too many workers");
      }
    }
    setConfirm(currentUnit, true);
  }

  protected void commandTrade(String[] tokens) {
    if (tokens.length < 2) {
      addNewError("too few parameters");
    }

    int amount = -1;
    try {
      amount = Integer.parseInt(tokens[1]);
    } catch (NumberFormatException e) {
      addNewError("invalid number " + tokens[1]);
      return;
    }

    Skill buySkill =
        currentUnit.getModifiedSkill(world.rules.getSkillType(EresseaConstants.S_HANDELN));
    if (buySkill == null) {
      addNewError("no skill");
      return;
    }

    LuxuryPrice buyGood = null;
    for (LuxuryPrice price : currentRegion.getPrices().values()) {
      if (price.getPrice() < 0) {
        buyGood = price;
      }
    }

    int volume = currentRegion.maxLuxuries();

    // Soll eingekauft werden?
    if (volume > 0 && amount > 0 && buyGood != null) {
      removeOrdersLike("VERKAUFE.*", true);
      removeOrdersLike("KAUFE.*", true);

      // Berechne noetige Geldmenge fuer Einkauf (einfacher: Modulorechnung, aber wegen
      // Rundungsfehler nicht umsetzbar)
      int hilfNochUEbrig = amount;
      int hilfFaktor = 1;
      int geldNoetig = 0;
      while (hilfNochUEbrig > 0) {
        if (hilfNochUEbrig > volume) {
          hilfNochUEbrig -= volume;
          geldNoetig += (volume * hilfFaktor * buyGood.getPrice());
          hilfFaktor++;
        } else {
          geldNoetig += (hilfNochUEbrig * hilfFaktor * buyGood.getPrice());
          hilfNochUEbrig = 0;
        }
      }

      addNeed("Silber", currentUnit, geldNoetig, geldNoetig);

      // Zu verkaufende Handelsgueter verarbeiten
      for (int i = 2; i < tokens.length; ++i) {
        addNeed(tokens[i], currentUnit, amount, amount);
      }

      // Gueterzahl intitialisieren
      int guetersumme = 0;

      // Einkaufsbefehl setzen, wenn notwendig
      if (amount > 0) {
        addNewOrder(BUYOrder + " " + amount + " " + buyGood.getItemType().getOrderName(), true);
        guetersumme += amount;
      }

      // Verkaufsbefehl setzen, wenn notwendig
      for (int i = 2; i < tokens.length; ++i) {
        addNewOrder(SELLOrder + " " + volume + " " + tokens[i], true);
        guetersumme += volume;
      }

      // Einheit gut genug?
      if (guetersumme > buySkill.getLevel() * currentUnit.getPersons() * 10) {
        addNewWarning("Einheit hat zuwenig Handelstalent!");
      }
    } else {
      addNewError("nothing to trade");
    }
  }

  protected void commandQuartermaster(String[] tokens) {
    learn(currentUnit, Collections.singleton(new Skill(world.rules
        .getSkillType(EresseaConstants.S_WAHRNEHMUNG), 30, 10, 1, true)));
    for (Item item : currentUnit.getItems()) {
      boolean okay = false;
      if (item.getItemType().getID().equals(EresseaConstants.I_USILVER) && item.getAmount() < 1000) {
        okay = true;
      }
      for (int i = 1; !okay && i < tokens.length; i += 2) {
        if (item.getName().equals(tokens[i + 1]) && item.getAmount() < Integer.parseInt(tokens[i])) {
          okay = true;
          break;
        }
      }
      setConfirm(currentUnit, okay);
    }
  }

  protected void commandCollector(String[] tokens) {
    if (tokens.length > 2) {
      addNewError("too many arguments");
    }
    int modulo = Integer.MAX_VALUE;
    if (tokens.length > 1) {
      try {
        modulo = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        addNewError("invalid number " + tokens[1]);
        return;
      }
    }

    removeOrdersLike(MAKEOrder + " " + "[^T].*", true);
    removeOrdersLike(RESEARCHOrder + ".*", true);
    if (modulo != Integer.MAX_VALUE
        && (world.getDate().getDate() % modulo == 0 || !currentRegion.getHerbAmount().contains(
            "viele"))) {
      addNewOrder(RESEARCHOrder + " " + getLocalizedOrder(EresseaConstants.O_HERBS, "KRÄUTER"),
          true);
    } else {
      addNewOrder(MAKEOrder + " " + getLocalizedOrder(EresseaConstants.O_HERBS, "KRÄUTER"), true);
    }
  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  protected boolean hasEntertain() {
    return world.getGameSpecificStuff().getOrderChanger().isLongOrder(
        getLocalizedOrder(EresseaConstants.O_ENTERTAIN, ENTERTAINOrder));
  }

  protected boolean hasWork() {
    return world.getGameSpecificStuff().getOrderChanger().isLongOrder(
        getLocalizedOrder(EresseaConstants.O_WORK, WORKOrder));
  }

  protected void initSupply() {
    if (needMap == null) {
      needMap = new LinkedHashMap<String, Map<Unit, Need>>();
    } else {
      needMap.clear();
    }
    if (supplyMap == null) {
      supplyMap = new LinkedHashMap<String, Map<Unit, Supply>>();
    } else {
      supplyMap.clear();
    }

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
      if (amount == need.getUnit().getPersons()) {
        addReserveOrder(need.getUnit(), need.getItem(), 1, true);
      } else {
        addReserveOrder(need.getUnit(), need.getItem(), amount, false);
      }
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
          int giveAmount = Math.min(amount, supply.getAmount());
          if (giveAmount > 0) {
            addGiveOrder(supply.getUnit(), need.getUnit(), need.getItem(), giveAmount, false);
            need.reduceAmount(giveAmount);
            need.reduceMinAmount(giveAmount);
            supply.reduceAmount(giveAmount);
            amount -= giveAmount;
          }
        }
        if (amount <= 0) {
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
      map = new LinkedHashMap<Unit, Need>();
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
   * shield if available. Confirms unit if there's no problem.
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

    if (weaponSkill == null || BEST.equals(sWeaponSkill)) {

      int max = 0;
      for (Skill skill : u.getSkills()) {
        if (isWeaponSkill(skill) && skill.getLevel() > max) {
          max = skill.getLevel();
          weaponSkill = skill.getSkillType();
        }
      }
      if (weaponSkill == null && !W_NEVER.equals(warning)) {
        addNewWarning("no weapon skill");
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
        for (ItemType type : rules.getItemTypes()) {
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
      addNewError("no known matching weapon types");
    }

    ArrayList<Item> shields = findItems(shield, u, "shield");
    if (shields.isEmpty()) {
      addNewError("no known shield types");
    }

    ArrayList<Item> armors = findItems(armor, u, "armour");
    // FIXME: shield is subcategory of armour!
    if (armors.isEmpty()) {
      addNewError("no known armour types");
    }

    if (weaponSkill != null) {
      List<Skill> targetSkills = new LinkedList<Skill>();
      targetSkills.add(u.getSkill(weaponSkill));
      if (weaponSkill.getName().equals("Hiebwaffen")
          || weaponSkill.getName().equals("Stangenwafen")) {
        targetSkills.add(getSkill(S_ENDURANCE, Math.round(ENDURANCERATIO_FRONT
            * getSkillLevel(u, weaponSkill))));
      } else {
        targetSkills.add(getSkill(S_ENDURANCE, Math.round(ENDURANCERATIO_BACK
            * getSkillLevel(u, weaponSkill))));
      }
      learn(u, targetSkills);
    }

    if (!NULL.equals(sWeapon)
        && !reserveEquipment(weapon, weapons, !W_NEVER.equals(warning) && !W_SKILL.equals(warning))) {
      addNewError("could not reserve weapons");
    }
    if (!NULL.equals(sShield)
        && !reserveEquipment(shield, shields, W_ARMOR.equals(warning) || W_SHIELD.equals(warning))) {
      addNewError("could not reserve shields");
    }
    if (!NULL.equals(sArmor) && !reserveEquipment(armor, armors, W_ARMOR.equals(warning))) {
      addNewError("could not reserve armour");
    }

    setConfirm(u, true);
    notifyMagellan(u);
  }

  /**
   * Tries to learn the skills at the ratio reflected in the skills argument.
   * 
   * @param u
   * @param targetSkills
   */
  protected void learn(Unit u, Collection<Skill> targetSkills) {

    // find skill with maximum priority
    double maxWeight = 0;
    Skill maxSkill = null;
    StringBuilder comment = new StringBuilder(";");
    for (Skill skill : targetSkills) {
      double weight = calcSkillWeight(u, skill, targetSkills);
      comment.append(" ").append(skill.toString()).append(" ").append(weight);
      if (weight > maxWeight) {
        maxWeight = weight;
        maxSkill = skill;
      }
    }

    addNewOrder(comment.toString(), true);

    if (maxSkill == null) {
      addNewError("no weapon skill");
    } else {
      removeOrdersLike(LEARNOrder + ".*", true);
      removeOrdersLike(TEACHOrder + ".*", true);
      addNewOrder(LEARNOrder + " " + maxSkill.getName(), true);
    }
  }

  /**
   * Returns a weight ("importance") of a skill according to target values and the unit's current
   * skill levels.
   */
  protected static double calcSkillWeight(Unit u, Skill learningSkill,
      Collection<Skill> targetSkills) {
    if (learningSkill == null)
      return 0;
    double prio = 0.5;

    // calc max mult
    Skill learningTarget = null;
    double maxMult = 0;

    for (Skill skill2 : targetSkills) {
      if (skill2.getSkillType().equals(learningSkill.getSkillType())) {
        learningTarget = skill2;
      }
      int level = Math.max(1, getSkillLevel(u, skill2.getSkillType()));
      // if (lev < getMax(skill2)) {
      double mult = level / (double) skill2.getLevel();
      if (maxMult < mult) {
        maxMult = mult;
      }
      // }
    }

    if (learningTarget == null)
      // learned skill is not in target skills
      return 0.01 * learningSkill.getLevel();

    if (maxMult > 0) {
      // calc max normalized learning weeks
      double maxWeeks = 0;
      for (Skill skill2 : targetSkills) {
        int currentLevel = Math.max(1, getSkillLevel(u, skill2.getSkillType()));
        // if (lev < getMax(skill2)) {
        double weeks = skill2.getLevel() - currentLevel / maxMult + 2;
        if (maxWeeks < weeks) {
          maxWeeks = weeks;
        }
        // }
      }
      int level = Math.max(1, getSkillLevel(u, learningSkill.getSkillType()));
      // if (level >= getMax(skill))
      // prio = 0d;
      // else
      // prio should be between .4 and 1
      prio = .4 + .6 * (learningTarget.getLevel() - level / maxMult + 2) / maxWeeks;
      if (prio < 0) {
        prio = prio * 1.000001;
      }
    }
    return prio;
  }

  protected static int getSkillLevel(Unit u, SkillType skill) {
    Skill uskill = u.getSkill(skill);
    return uskill == null ? 0 : uskill.getLevel();
  }

  /**
   * If <code>itemType==null</code> return all the unit's items of the given category. If no item is
   * found, at least one item (with amount 0) is returned.
   */
  protected static ArrayList<Item> findItems(ItemType itemType, Unit u, String category) {
    ArrayList<Item> items = new ArrayList<Item>(1);
    ItemCategory itemCategory = world.rules.getItemCategory(StringID.create(category));
    if (itemType == null) {
      for (Item item : u.getItems()) {
        if (item.getItemType().getCategory().equals(itemCategory)) {
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
        if (type.getCategory().equals(itemCategory)) {
          items.add(new Item(type, 0));
          break;
        }
      }
    }
    return items;
  }

  protected boolean reserveEquipment(ItemType preferred, List<Item> ownStuff, boolean warn) {
    if (preferred != null) {
      // reserve requested weapon
      commandBenoetige(new String[] { "Benoetige", EACHOrder, "1", preferred.getOrderName() });
    } else if (!ownStuff.isEmpty()) {
      int supply = 0;
      for (Item w : ownStuff) {
        // reserve all matching weapons, except first one
        if (supply > 0) {
          commandBenoetige(new String[] { "Benoetige",
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
      commandBenoetige(new String[] { "Benoetige", min, max, w.getOrderName() });
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
    ENTERTAINOrder = getLocalizedOrder(EresseaConstants.O_ENTERTAIN, ENTERTAINOrder);
    TAXOrder = getLocalizedOrder(EresseaConstants.O_TAX, TAXOrder);
    WORKOrder = getLocalizedOrder(EresseaConstants.O_WORK, WORKOrder);
    BUYOrder = getLocalizedOrder(EresseaConstants.O_BUY, BUYOrder);
    SELLOrder = getLocalizedOrder(EresseaConstants.O_SELL, SELLOrder);
    MAKEOrder = getLocalizedOrder(EresseaConstants.O_MAKE, MAKEOrder);
    RESEARCHOrder = getLocalizedOrder(EresseaConstants.O_RESEARCH, RESEARCHOrder);

    if (currentFaction.getLocale().getLanguage() != "de") {
      // warning constants
      W_NEVER = "never";
      W_SKILL = "skill";
      W_WEAPON = "weapon";
      W_SHIELD = "shield";
      W_ARMOR = "armor";
      BEST = "best";
      NULL = "null";
      LUXUSOrder = "LUXURY";
      KRAUTOrder = "HERBS";
    }
  }

  /**
   * Tries to translate the given order to the current locale.
   */
  protected String getLocalizedOrder(String orderKey, String fallBack) {
    String translation = Resources.getOrderTranslation(orderKey, currentFaction.getLocale());
    if (translation == orderKey)
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
      // remove old orders matching a removed order
      for (String pattern : removedOrderPatterns)
        if (order.matches(pattern))
          return;
    }

    newOrders.add(getParser().parse(order, currentUnit.getLocale()).getText());
  }

  /**
   * Registers a pattern. All lines matching this regular expression will be removed from here on.
   * If retroActively, also orders that are already in {@link #newOrders}.
   * 
   * @param regEx
   * @param retroActively
   */
  protected void removeOrdersLike(String regEx, boolean retroActively) {
    if (retroActively) {
      for (Iterator<String> it = newOrders.iterator(); it.hasNext();) {
        String line2 = it.next();
        if (line2.matches(regEx)) {
          it.remove();
        }
      }
    }
    removedOrderPatterns.add(regEx);
  }

  /**
   * Adds an error line
   * 
   * @param line The current order line
   * @param hint
   */
  protected void addNewError(String hint) {
    error = line;
    // errMsg = hint;
    addNewOrder(COMMENTOrder + " TODO: Fehler im Skript in Zeile " + error + ": " + hint, true);
  }

  /**
   * Adds a warning message (with a to do tag) to the new orders.
   * 
   * @param text
   */
  protected void addNewMessage(String text) {
    addNewOrder(COMMENTOrder + " ----- " + text + " -----", true);
  }

  /**
   * Adds an error line to new orders.
   * 
   * @param line The current order line
   * @param hint
   */
  protected void addNewWarning(String hint) {
    error = line;
    // errMsg = hint;
    addNewOrder(COMMENTOrder + " TODO: Fehler im Skript in Zeile " + error + ": " + hint, true);
  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  /**
   * Adds a warning message (with a to do tag) directly to the unit's orders.
   * 
   * @param unit
   * @param text
   */
  public static void addWarning(Unit unit, String text) {
    // helper.addOrder(unit, "; -------------------------------------");
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
   * @param name A skill name, like "Ausdauer"
   * @return The SkillType corresponding to name or <code>null</code> if this SkillType does not
   *         exist.
   */
  public static SkillType getSkillType(String name) {
    return world.rules.getSkillType(name);
  }

  /**
   * Returns a skill with the given name and level.
   * 
   * @param name
   * @param level
   * @return A skill with the given level or <code>null</code> if this SkillType does not exist.
   */
  public static Skill getSkill(String name, int level) {
    if (getSkillType(name) != null)
      return new Skill(getSkillType(name), 0, level, 1, true);
    else
      return null;
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
   * This method tries to find out, if the unit has a weapon and a skill to use this weapon.
   */
  public static boolean isSoldier(Unit unit) {
    Collection<Item> items = unit.getItems();
    ItemCategory weapons = world.rules.getItemCategory(StringID.create("weapons"));
    for (Item item : items) {
      ItemCategory itemCategory = item.getItemType().getCategory();
      if (weapons.equals(itemCategory)) {
        // ah, a weapon...
        Skill useSkill = item.getItemType().getUseSkill();
        if (useSkill != null) {
          // okay, has the unit the skill?
          for (Skill skill : unit.getSkills()) {
            if (useSkill.getSkillType().equals(skill.getSkillType()))
              return true;
          }
        }
      }
    }
    return false;
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
      for (Order line : u.getOrders2()) {
        java.util.regex.Pattern p = Pattern.compile(".*[$]([^$]*)[$]verlassen.*");
        java.util.regex.Matcher m = p.matcher(line.getText());
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
   * Converts some Vorlage commands to $cript commands. Call from the script of your faction with<br />
   * <code>(new E3CommandParser(world, helper)).convertVorlage((Faction) container, null);</code>
   * (all regions) or from a region with<br />
   * <code>(new E3CommandParser(world, helper)).convertVorlage(helper.getFaction("1wpy"), 
   * (Region) container);</code>.
   * 
   * @param faction
   * @param region
   */
  public void convertVorlage(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();
    currentFaction = faction;

    initLocales();

    if (region == null) {
      // call self for all regions
      for (Region r : world.regions().values()) {
        convertVorlage(faction, r);
      }
      return;
    }

    // loop for all units of faction
    final String scriptStart = PCOMMENTOrder + " " + scriptMarker + " ";
    for (Unit u : region.units())
      if (faction.getID().equals(u.getFaction().getID())) {
        // uncomment the following lines if you have the newest nighthly build of Magellan
        // ---start uncomment for BeanShell
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), 0);
        // ---stop uncomment for BeanShell
        currentUnit = u;
        newOrders = new ArrayList<String>();
        removedOrderPatterns = new ArrayList<String>();
        changedOrders = false;
        error = -1;
        StringBuilder lerneOrder = null;

        // loop over orders
        line = 0;
        for (Object o : u.getOrders()) {
          ++line;
          currentOrder = (String) o;
          if (currentOrder.startsWith("// #")) {
            String[] tokens = detectVorlageCommand(currentOrder);
            if (tokens != null) {
              String command = tokens[0];

              // add vorlage as comment
              addNewOrder(COMMENTOrder + " " + currentOrder, true);

              // detect commands
              if (command.equals("Ausruestung")) {
                for (int i = 1; i < tokens.length; ++i) {
                  if (tokens[i].equals("Ausruestung")) {
                    continue;
                  }
                  try {
                    int number = Integer.parseInt(tokens[i]);
                    addNewOrder(scriptStart + "Benoetige " + number + " " + tokens[++i], true);
                  } catch (Exception e) {
                    addNewOrder(scriptStart + "Benoetige JE 1 " + tokens[i], true);
                  }
                }
              } else if (command.equals("AutoBestaetigen")) {
                addNewOrder(scriptStart + "auto " + (tokens[1].equals("an") ? "" : NOT), true);
              } else if (command.equals("Benoetige")) {
                if (tokens.length > 3 && tokens[3].equals("aus")) {
                  addNewOrder(scriptStart + "Benoetige 0 " + tokens[1] + " " + tokens[2], true);
                } else {
                  addNewOrder(scriptStart + "Benoetige " + tokens[1] + " " + tokens[2], true);
                }
              } else if (command.startsWith("BerufBotschafter")) { // also BerufBotschafterSTM
                addNewOrder(scriptStart + " BerufBotschafter "
                    + (tokens.length > 1 ? tokens[1] : ""), true);
              } else if (command.equals("BerufDepotVerwalter")) {
                if (tokens.length > 1) {
                  try {
                    int number = Integer.parseInt(tokens[1]);
                    addNewOrder(scriptStart
                        + "Benoetige "
                        + number
                        + " "
                        + Resources.getRuleItemTranslation(EresseaConstants.I_USILVER.toString(),
                            faction.getLocale()), true);
                  } catch (Exception e) {
                    // no number -- ignore
                  }
                }
                addNewOrder(scriptStart + "Ueberwache", true);
              } else if (command.equals("BerufWahrnehmer")) {
                addNewOrder(scriptStart
                    + "Lerne "
                    + world.getRules().getSkillType(EresseaConstants.S_WAHRNEHMUNG.toString())
                        .getName(), true);
              } else if (command.equals("Depot")) {
                addNewOrder(COMMENTOrder + " Depot ignored", true);
              } else if (command.equals("Versorge")) {
                addNewOrder(COMMENTOrder + " Versorge ignored", true);
              } else if (command.equals("Erlerne")) {
                // prepare Lerne order, but write it later to include several consecutive Erlerne
                // orders
                if (lerneOrder == null) {
                  lerneOrder = new StringBuilder();
                  lerneOrder.append(scriptStart).append("Lerne");
                }
                lerneOrder.append(" ").append(tokens[1]).append(" ").append(
                    tokens.length > 2 ? tokens[2] : "10");
                // ignore warning argument "an/aus"
              } else if (command.equals("Ernaehre")) {
                StringBuilder order = new StringBuilder(scriptStart);
                for (String token : tokens) {
                  order.append(token);
                }
                addNewOrder(order.toString(), true);
              } else if (command.equals("GibKraeuter")) {
                addNewOrder(scriptStart + "GibWenn " + tokens[1] + " KRAUT", true);
              } else if (command.equals("GibWenn")) {
                StringBuilder order = new StringBuilder(scriptStart);
                for (int i = 0; i < tokens.length - 1; ++i) {
                  if (order.length() > 0) {
                    order.append(" ");
                  }
                  order.append(tokens[i]);
                }
                if (tokens[tokens.length - 1].equals("aus")) {
                  order.append(" Menge");
                }
                addNewOrder(order.toString(), true);
                if (command.equals("Steuermann") && tokens.length != 3) {
                  addNewError("unsupported arguments");
                }
              } else if (command.equals("VersorgeFremd")) {
                if (tokens.length > 3 && tokens[3].equals("aus")) {
                  addNewOrder(scriptStart + "BenoetigeFremd " + tokens[1] + " 0 " + tokens[2] + " "
                      + tokens[3], true);
                } else {
                  addNewOrder(scriptStart + "BenoetigeFremd " + tokens[1] + " " + tokens[2] + " "
                      + tokens[3], true);
                }
              } else if (command.equals("Warnung")) {
                StringBuilder order = new StringBuilder(scriptStart);
                order.append("+0 ");
                for (int i = 1; i < tokens.length; ++i) {
                  order.append(tokens[i]);
                }
                addNewOrder(order.toString(), true);
              } else if (command.equals("Warnung")) {
                // ignore
              } else if (command.equals("Erlaube") || command.equals("Verlange")
                  || command.equals("Handel") || command.equals("Mannschaft")
                  || command.equals("Quartiermeister") || command.equals("Steuermann")
                  || command.equals("Ueberwache")) {
                // simply copy order
                StringBuilder order = new StringBuilder(scriptStart);
                for (String token : tokens) {
                  if (order.length() > 0) {
                    order.append(" ");
                  }
                  order.append(token);
                }
                addNewOrder(order.toString(), true);
                if (command.equals("Steuermann") && tokens.length != 3) {
                  addNewError("unsupported arguments");
                }

              } else {
                addNewOrder(currentOrder, false);
                addNewError("unbekannter Befehl: " + command);
              }
            } else if (currentOrder.startsWith("// #forever { ")) {
              addNewOrder(COMMENTOrder + " " + currentOrder, true);
              addNewOrder("@" + currentOrder.substring(14, currentOrder.lastIndexOf("}") - 1), true);
            } else if (currentOrder.startsWith("// #default")) {
              // ignore
              addNewOrder(COMMENTOrder + " " + currentOrder, true);
            } else {
              addNewOrder(currentOrder, false);
              addNewError("unbekannter Befehl: " + currentOrder);
              Logger.getInstance("E3CommandParser").fine("unknown order " + currentOrder);
            }
          } else {
            addNewOrder(currentOrder, false);
          }
          currentOrder = null;

        }
        if (lerneOrder != null) { // unwritten Lerne order
          addNewOrder(lerneOrder.toString(), true);
          lerneOrder = null;
        }

        if (changedOrders) {
          currentUnit.setOrders(newOrders);
        }
        notifyMagellan(currentUnit);

        newOrders = null;
      }
  }

  /**
   * If order is a vorlage command ("// #call Name ..."), returns a List of the tokens. Otherwise
   * returns <code>null</code>. The first in the list is the first token after the "// #call".
   */
  protected static String[] detectVorlageCommand(String order) {
    StringTokenizer tokenizer = new StringTokenizer(order, " ");
    if (tokenizer.hasMoreTokens()) {
      String part = tokenizer.nextToken();
      if (part.equals(PCOMMENTOrder)) {
        if (tokenizer.hasMoreTokens()) {
          part = tokenizer.nextToken();
          if (part.equals("#call")) {
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
   * strip it from generics and other advanced stuff that BeanShell doesn't understand.
   * 
   * @param args this is ignored
   */
  public static void main(String[] args) {
    File file =
        new File("./src-test/magellan/plugin/extendedcommands/scripts/E3CommandParser.java");
    try {
      LineNumberReader reader = new LineNumberReader(new FileReader(file));

      System.out.println("// created by E3CommandParser.main() at "
          + Calendar.getInstance().getTime());
      System.out.println();

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
          newLine = newLine.replaceFirst(" +@.*", "// $0");
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
// void soldier(Unit unit) {
// E3CommandParser.soldier(unit);
// }
//
// void cleanOcean() {
// for (Region r : world.regions().values()) {
// if (r.getName() != null && r.getType().toString().contains("Ozean")
// && r.getName().contains("Leere")) {
// r.setName(null);
// r.setDescription(null);
// }
// }
// }
// ---stop uncomment for BeanShell
