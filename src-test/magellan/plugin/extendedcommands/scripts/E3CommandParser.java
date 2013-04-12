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
import java.lang.reflect.InvocationTargetException;
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

import magellan.client.extern.MagellanPlugIn;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
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
 * "<code>(new E3CommandParser(world, helper)).execute(container);</code>" or from the script of a
 * region with<br />
 * "
 * <code>(new E3CommandParser(world, helper)).execute(helper.getFaction("drac"), (Region) container);</code>
 * " (for just this region).
 * 
 * @author stm
 */
public class E3CommandParser {

  /** A standard soldier's endurance skill should be this fraction of his (first row) weapon skill */
  public static float ENDURANCERATIO_FRONT = .6f;
  /** A standard soldier's endurance skill should be this fraction of his (second row) weapon skill */
  public static float ENDURANCERATIO_BACK = .35f;

  /** Unit limit, used to warn if we get too many units. */
  public static int UNIT_LIMIT = 250;

  /** If this is true, some more hints will be added to the orders if expected units are missing */
  public static boolean ADD_NOT_THERE_INFO = false;

  /**
   * If this is > 0, all units are suppliers, otherwise suppliers must be set with Versorge (the
   * default)
   */
  public static int DEFAULT_SUPPLY_PRIORITY = 0;

  /** default need priority */
  private static final int DEFAULT_PRIORITY = 100;
  /** need priority for GibWenn command */
  private static final int GIB_WENN_PRIORITY = DEFAULT_PRIORITY;
  /** need priority for Depot command and silver */
  private static final int DEPOT_SILVER_PRIORITY = 150;
  /** need priority for earn command */
  private static final int DEFAULT_EARN_PRIORITY = 200;
  /** need priority for Depot command and other items */
  private static final int DEPOT_PRIORITY = -1;
  /** need priority for traders */
  private static final int TRADE_PRIORITY = DEFAULT_PRIORITY;

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
  /** The LUXUS order parameter */
  public static String TRANKOrder = "TRANK";
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
  /** The NACH order */
  private static String MOVEOrder = "NACH";
  /** The ROUTE order */
  private static String ROUTEOrder = "ROUTE";
  /** The PAUSE order */
  private static String PAUSEOrder = "PAUSE";
  /** The RESEARCH order */
  private static String RESEARCHOrder = "FORSCHE";
  /** The RECRUIT order */
  private static String RECRUITOrder = "REKRUTIERE";

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
  public static String NOT = "NICHT";

  /** The LONG token (for clear) */
  public static String LONG = "$lang";
  /** The SHORT token (for clear) */
  public static String SHORT = "$kurz";
  /** The COMMENT token (for clear) */
  public static String COMMENT = "$kommentar";

  private static String S_ENDURANCE = EresseaConstants.S_AUSDAUER.toString();

  /** warning constants */
  protected static final int C_ALWAYS = 0, C_AMOUNT = 1, C_UNIT = 2, C_HIDDEN = 3, C_NEVER = 4;

  private OrderParser parser;
  private Logger log;

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
    log = Logger.getInstance("E3CommandParser");
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
  protected Map<String, Map<Integer, Map<Unit, Need>>> needMap;

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
   * Current state for the Loesche command
   */
  protected String clear = null;
  /** Current prefix for the Loesche command */
  protected String clearPrefix = "";

  private int progress = -1;

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

    executeFrom(faction, region, region);
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands. Ignore regions before first (in the report order).
   * 
   * @param faction
   * @param region only commands of unit in this region are executed, may be <code>null</code> to
   *          execute for all regions.
   * @param first First region to execute, may be <code>null</code> to not ignore any regions
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void executeFrom(Faction faction, Region region, Region first) {
    if (faction == null)
      throw new NullPointerException();

    currentFaction = faction;

    initLocales();

    // comment out the following two lines if you don't have the newest nighthly build of Magellan
    helper.getUI().setMaximum(world.getRegions().size() + 4);
    helper.getUI().setProgress("preprocessing", ++progress);

    // sometimes we need an arbitrary unit. This is a shorthand for it.
    someUnit = faction.units().iterator().next();
    if (someUnit == null)
      throw new RuntimeException("No units in report!");

    if (faction.units().size() >= UNIT_LIMIT) {
      addWarning(someUnit, "Einheitenlimit erreicht (" + faction.units().size() + "/" + UNIT_LIMIT
          + ")! ");
    } else if (faction.units().size() * 1.1 > UNIT_LIMIT) {
      addWarning(someUnit, "Einheitenlimit fast erreicht (" + faction.units().size() + "/"
          + UNIT_LIMIT + ")! ");
    }

    collectStats();

    // Parses the orders of the units for commands of the form "// $cript ..." and
    // tries to execute them.
    if (region == null) {
      boolean go = first == null;
      for (Region r : world.getRegions()) {
        if (r == first) {
          go = true;
        }
        if (go) {
          execute(r);
        }
      }
    } else {
      execute(region);
    }

    // comment out the following line if you don't have the newest nightly build of Magellan
    helper.getUI().setProgress("postprocessing", ++progress);

    for (Unit u : faction.units()) {
      if ("1".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(true);
        // u.addOrder("; autoconfirmed");
      } else if ("0".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(false);
      }
      notifyMagellan(u);
    }
    // comment out the following line if you don't have the newest nightly build of Magellan
    helper.getUI().setProgress("ready", ++progress);
  }

  protected void execute(Region region) {
    try {
      currentRegion = region;
      initSupply();

      for (Unit u : region.units()) {
        if (u.getFaction().equals(currentFaction)) {
          currentUnit = u;
          // comment out the following line if you don't have the newest nightly build of Magellan
          helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

          try {
            parseScripts();
          } catch (RuntimeException e) {
            addWarning(u, "error " + e.getClass().getSimpleName());
            log.error(e.getClass().getSimpleName() + " script error for " + u);
            throw new RuntimeException("script error for " + u, e);
          }
        }
      }
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setProgress(region.toString() + " - postprocessing", ++progress);

      satisfyNeeds();
    } catch (RuntimeException e) {
      e.printStackTrace();
      // log.error(e.getClass().getSimpleName() + " script error for " + region);
      throw new RuntimeException("script error for " + region, e);
    }
    // refresh relations, just in case
    region.refreshUnitRelations(true);
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
    // comment out the following lines if you don't have the newest nightly build of Magellan
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
    // u.addOrder(COMMENTOrder + " hat Skript", false);
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
        + " region scripts", true);
  }

  /**
   * Parses the orders of the unit u for commands of the form "// $cript ..." and tries to execute
   * them. Known commands:<br />
   * <tt>// $cript +X text</tt> -- If X<=1 then a warning containing text is added to the unit's
   * orders. Otherwise X is decreased by one.<br />
   * <code>// $cript [rest [period [length]] text</code> -- Adds text (or commands) to the orders<br />
   * <code>// $cript auto [NICHT]|[length [period]]</code> -- autoconfirm orders<br />
   * <code>// $cript Loeschen [$kurz] [<prefix>]</code> -- clears orders except comments<br />
   * <code>// $cript GibWenn receiver [JE] amount item [warning]</code> -- add give order (if
   * possible)<br />
   * <code>// $cript Benoetige minAmount [maxAmount] item [priority]</code><br />
   * <code>// $cript Benoetige JE amount item [priority]</code><br />
   * <code>// $cript Benoetige ALLES [item] [priority]</code> -- acquire things from other units<br />
   * <code>// $cript BenoetigeFremd unit [JE] minAmount [maxAmount] item [priority]</code><br />
   * <code>// $cript Versorge [[item]...] priority</code> -- set supply priority.<br />
   * <code>// $cript BerufDepotVerwalter [Zusatzbetrag]</code> Collects all free items in the
   * region, Versorge 100, calls Ueberwache<br />
   * <code>// $cript Soldat [Talent [Waffe [Schild [Rüstung]]]] [nie|Talent|Waffe|Schild|Rüstung]</code>
   * -- learn skill and reserve equipment<br />
   * <code>// $cript Lerne Talent1 Stufe1 [[Talent2 Stufe2]...]</code> -- learn skills in given
   * ratio <br />
   * <code>// $cript BerufBotschafter [Talent]</code> -- earn money if necessary, otherwise learn
   * skill<br />
   * <code>// $cript Ueberwache</code> -- look out for unknown units<br />
   * <code>// $cript Erlaube faction unit [unit...]</code> -- allow units for Ueberwache<br />
   * <code>// $cript Verlange faction unit [unit...]</code> -- allow and require units for
   * Ueberwache<br />
   * <code>// $cript Ernaehre [amount]</code> -- earn money<br />
   * <code>// $cript Handel amount [ALLES | good...]</code> -- trade luxuries<br />
   * <code>// $cript Steuermann minSilver maxSilver</code> -- be responsible for ship<br />
   * <code>// $cript Mannschaft skill</code> -- be crew and learn<br />
   * <code>// $cript Quartiermeister [[amount item]...]</code> -- be lookout<br />
   * <code>// $cript Sammler [interval]</code> -- collect and research HERBS<br />
   * <code>// $cript KrautKontrolle [route]</code> -- FORSCHE KRÄUTER in several regions<br />
   * <code>// $cript RekrutiereMax</code> -- recruit as much as possible<br />
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
    for (Order o : currentUnit.getOrders2()) {
      ++line;
      currentOrder = o.getText();
      String[] tokens = detectScriptCommand(currentOrder);
      if (tokens == null) {
        // add order if
        if (shallClear(currentOrder)) {
          addNewOrder(COMMENTOrder + " " + currentOrder, true);
        } else {
          addNewOrder(currentOrder, false);
        }
        currentOrder = null;
      } else {
        try {
          Integer.parseInt(tokens[0]);
          currentOrder = commandRepeat(tokens);
          if (currentOrder == null) {
            tokens = null;
          } else {
            tokens = detectScriptCommand(currentOrder);
            if (tokens == null) {
              addNewOrder(currentOrder, true);
              currentOrder = null;
            }
          }
        } catch (NumberFormatException e) {
          // not a repeating order
        }
        if (tokens != null) {
          String command = tokens[0];
          if (command.startsWith("+")) {
            commandWarning(tokens);
            changedOrders = true;
          } else if (command.equals("KrautKontrolle")) {
            commandControl(tokens);
            changedOrders = true;
          } else if (command.equals("auto")) {
            commandAuto(tokens);
          } else {
            // order remains
            addNewOrder(currentOrder, false);

            if (command.equals("Loeschen")) {
              commandClear(tokens);
            } else if (command.equals("GibWenn")) {
              commandGiveIf(tokens);
            } else if (command.equals("Benoetige") || command.equals("BenoetigeFremd")) {
              commandNeed(tokens);
            } else if (command.equals("Versorge")) {
              commandSupply(tokens);
            } else if (command.equals("BerufDepotVerwalter")) {
              commandDepot(tokens);
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
                addNewError("zu wenige Argumente");
              } else {
                commandNeed(new String[] { "Benoetige", tokens[1], tokens[2], "Silber",
                    String.valueOf(DEFAULT_PRIORITY + 1) });
                setConfirm(currentUnit, false);
              }
            } else if (command.equals("Mannschaft")) {
              if (tokens.length < 3) {
                addNewError("zu wenige Argumente");
              } else {
                commandLearn(new String[] { "Lerne", tokens[1], tokens[2] });
                setConfirm(currentUnit, true);
              }
            } else if (command.equals("Quartiermeister")) {
              commandQuartermaster(tokens);
            } else if (command.equals("Sammler")) {
              commandCollector(tokens);
            } else if (command.equals("RekrutiereMax")) {
              commandRecruit(tokens);
            } else {
              addNewError("unbekannter Befehl: " + command);
            }
          }
          currentOrder = null;

        }
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
      if (part.equals(PCOMMENTOrder) || part.equals(COMMENTOrder)) {
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
   * <code>// $cript Loeschen [$kurz] [<prefix>]</code><br />
   * Remove orders except comments from here on. If "$kurz", remove all orders, otherwise only long
   * and permanent (@) orders. If prefix is set, remove only orders starting with that prefix.
   * 
   * @param tokens
   */
  protected void commandClear(String[] tokens) {
    clearPrefix = "";
    if (tokens.length == 1) {
      clear = LONG;
    } else {
      if (tokens[1].equalsIgnoreCase(SHORT)) {
        clear = ALLOrder;
        clearPrefix =
            currentOrder.substring(Math.min(currentOrder.length(), currentOrder.indexOf(SHORT)
                + SHORT.length() + 1));
      } else {
        clear = LONG;
        clearPrefix =
            currentOrder.substring(Math.min(currentOrder.length(), currentOrder.indexOf("Loeschen")
                + "Loeschen".length() + 1));
      }
    }
    for (int i = 0; i < newOrders.size(); i++) {
      String order = newOrders.get(i);
      if (shallClear(order)) {
        changedOrders = true;
        newOrders.set(i, COMMENTOrder + " " + order);
      }
    }
  }

  protected boolean shallClear(String order) {
    if (clear == null)
      return false;
    String trimmed = order.trim();

    return !trimmed.startsWith(PCOMMENTOrder)
        && !trimmed.startsWith(COMMENTOrder)
        && (clear == ALLOrder || (clear == LONG && world.getGameSpecificStuff().getOrderChanger()
            .isLongOrder(order)))
        && (clearPrefix == null || clearPrefix.length() == 0 || trimmed.startsWith(clearPrefix));
  }

  /**
   * <code>// $cript [rest [period [length]]] text</code><br />
   * Adds text (or commands) to the orders after rest rounds. If <code>rest==1</code>, text is added
   * to the unit's orders after the current order. If text is a script order itself, it will be
   * executed. If period is set, rest will be reset to period and the modified order added instead
   * of the current order. If <code>rest>1</code>, it is decreased and the modified order added
   * instead of the current one. If length is given, the whole order will be removed after length
   * rounds.
   * 
   * @param tokens
   * @return <code>text</code>, if <code>rest==1</code>, otherwise <code>null</code>
   */
  protected String commandRepeat(String[] tokens) {
    StringBuilder result = null;
    try {
      int rest = Integer.parseInt(tokens[0]);
      int period = 0;
      int length = Integer.MAX_VALUE;
      int textIndex = 1;
      if (tokens.length >= 2) {
        try {
          period = Integer.parseInt(tokens[1]);
          textIndex = 2;
          if (tokens.length >= 3) {
            try {
              length = Integer.parseInt(tokens[2]);
              textIndex = 3;
            } catch (NumberFormatException nfe) {
              // third argument not a number
              length = Integer.MAX_VALUE;
              textIndex = 2;
            }
          }
        } catch (NumberFormatException nfe) {
          // second argument not a number
          period = 0;
          textIndex = 1;
        }
      }
      if (rest == 1) {
        result = new StringBuilder();
        if (period > 0) {
          rest = period + 1;
        }
        if (tokens[textIndex].equals(scriptMarker)) {
          result.append(COMMENTOrder).append(" ");
        }
        for (int i = textIndex; i < tokens.length; ++i) {
          if (i > textIndex) {
            result.append(" ");
          }
          result.append(tokens[i]);
        }
      } else if (length == 0) {
        // return empty string to signal success
        result = new StringBuilder();
      }
      if ((rest > 1 || period > 0) && length > 0) {
        StringBuilder newOrder = new StringBuilder();
        newOrder.append(PCOMMENTOrder).append(" ").append(scriptMarker);
        newOrder.append(" ").append(rest - 1);
        if (period > 0) {
          newOrder.append(" ").append(period);
        }
        if (length != Integer.MAX_VALUE) {
          newOrder.append(" ").append(length - 1);
        }
        for (int i = textIndex; i < tokens.length; ++i) {
          newOrder.append(" ").append(tokens[i]);
        }
        addNewOrder(newOrder.toString(), true);
      }
    } catch (NumberFormatException e) {
      addNewOrder(currentOrder, false);
      addNewError("Zahl erwartet");
      result = null;
    }

    return result != null ? result.toString() : null;
  }

  /**
   * <code>// $cript auto [NICHT]|[length [period]]</code><br />
   * Autoconfirms a unit (or prevents autoconfirmation if NICHT). If length is given, the unit is
   * autoconfirmed for length rounds (length is decreased each round). If period is given, the unit
   * is <em>not</em> confirmed every period rounds, otherwise confirmed.
   * 
   * @param tokens
   */
  protected void commandAuto(String[] tokens) {
    if (tokens.length > 4) {
      addNewError("zu viele Argumente");
      return;
    }
    if (tokens.length == 1) {
      setConfirm(currentUnit, true);
      addNewOrder(currentOrder, false);
    } else if (NOT.equalsIgnoreCase(tokens[1])) {
      setConfirm(currentUnit, false);
      addNewOrder(currentOrder, false);
    } else {
      int length = 0, period = 0;
      try {
        length = Integer.parseInt(tokens[1]);
        if (tokens.length > 2) {
          period = Integer.parseInt(tokens[2]);
        }
        if (length > 0) {
          setConfirm(currentUnit, true);
        } else {
          setConfirm(currentUnit, false);
          if (period > 0) {
            length = period;
          }
        }
        StringBuilder newOrder = new StringBuilder();
        newOrder.append(PCOMMENTOrder).append(" ").append(scriptMarker).append(" ").append(
            tokens[0]);
        newOrder.append(" ").append(length - 1);
        if (period > 0) {
          newOrder.append(" ").append(period);
        }
        addNewOrder(newOrder.toString(), true);
      } catch (NumberFormatException e) {
        addNewOrder(currentOrder, false);
        addNewError("Zahl erwartet");
        return;
      }
    }
  }

  /**
   * <code>// $cript GibWenn receiver [[JE] amount|ALLES|KRAUT|LUXUS|TRANK] [item] [warning]</code><br />
   * Adds a GIB order to the unit. Warning may be one of "immer", "Menge", "Einheit", "versteckt"
   * "nie". "versteckt" informiert nur, wenn die Einheit nicht da ist, übergibt aber trotzdem.
   */
  protected void commandGiveIf(String[] tokens) {
    if (tokens.length < 3 || tokens.length > 6) {
      addNewError("falsche Anzahl Argumente");
      return;
    }

    Unit target = helper.getUnit(tokens[1]);

    // test if EACH is present
    int je = 0;
    if (EACHOrder.equals(tokens[2])) {
      je = 1;
      if (ALLOrder.equals(tokens[3]) || KRAUTOrder.equals(tokens[3])
          || LUXUSOrder.equals(tokens[3]) || TRANKOrder.equals(tokens[3])) {
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
          if (item.getAmount() > 0) {
            addNewOrder(getGiveOrder(currentUnit, tokens[1], item.getOrderName(),
                Integer.MAX_VALUE, false), true);
          } else if (ADD_NOT_THERE_INFO) {
            addNewMessage("we have no " + item);
          }
        }
        return;
      }
    }

    if (KRAUTOrder.equalsIgnoreCase(tokens[2]) || LUXUSOrder.equalsIgnoreCase(tokens[2])
        || TRANKOrder.equalsIgnoreCase(tokens[2])) {
      // handle GIB xyz KRAUT
      if (KRAUTOrder.equalsIgnoreCase(tokens[2])) {
        if (world.rules.getItemCategory("herbs") == null) {
          addNewError("Spiel kennt keine Kräuter");
        } else {
          giveAll(tokens, target, warning, new Filter() {

            public boolean approve(Item item) {
              return world.rules.getItemCategory("herbs").equals(item.getItemType().getCategory());
            }
          });
        }
      }

      // handle GIB xyz LUXUS
      if (LUXUSOrder.equalsIgnoreCase(tokens[2])) {
        if (world.rules.getItemCategory("luxuries") == null) {
          addNewError("Spiel kennt keine Luxusgüter");
        } else {
          giveAll(tokens, target, warning, new Filter() {

            public boolean approve(Item item) {
              return world.rules.getItemCategory("luxuries").equals(
                  item.getItemType().getCategory());
            }
          });
        }
      }

      // handle GIB xyz TRANK
      if (TRANKOrder.equalsIgnoreCase(tokens[2])) {
        if (world.rules.getItemCategory("potions") == null) {
          addNewError("Spiel kennt keine Tränke");
        } else {
          giveAll(tokens, target, warning, new Filter() {
            public boolean approve(Item item) {
              return world.rules.getItemCategory("potions")
                  .equals(item.getItemType().getCategory());
            }
          });
        }
      }

      return;
    }

    if (tokens.length != 4 + je + (warning == -1 ? 0 : 1)) {
      addNewError("zu viele Parameter");
      return;
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
        addNewWarning("supply 0 " + item);
        return;
      }
      supply.reduceAmount(amount);
      if (target != null) {
        addNeed(item, target, -amount, -amount, GIB_WENN_PRIORITY);
      }
    }

  }

  interface Filter {

    boolean approve(Item item);

  }

  private void giveAll(String[] tokens, Unit target, int warning, Filter filter) {
    if (testUnit(tokens[1], target, warning)) {
      if (tokens.length > (warning == -1 ? 3 : 4)) {
        addNewError("zu viele Parameter");
      }

      for (Item item : currentUnit.getItems())
        if (filter.approve(item)) {
          addNewOrder(getGiveOrder(currentUnit, tokens[1], item.getOrderName(), Integer.MAX_VALUE,
              false), true);
          Supply supply = getSupply(item.getOrderName(), currentUnit);
          if (supply == null) {
            addNewWarning("supply 0 " + item);
          } else {
            int amount = item.getAmount();
            supply.reduceAmount(amount);
            if (target != null) {
              addNeed(item.getOrderName(), target, -amount, -amount, GIB_WENN_PRIORITY);
            }
          }
        }

    }
  }

  private boolean testUnit(String sOther, Unit other, int warning) {
    if (other == null || other.getRegion() != currentUnit.getRegion()) {
      if (warning == C_AMOUNT || warning == C_HIDDEN || warning == C_NEVER) {
        if (ADD_NOT_THERE_INFO) {
          addNewOrder("; " + sOther + " nicht da", true);
        }
      } else {
        addNewWarning(sOther + " nicht da");
      }
      return warning == C_HIDDEN;
    }
    return true;
  }

  /**
   * <code>// $cript Benoetige minAmount [maxAmount] item [priority]</code><br />
   * <code>// $cript Benoetige JE amount item [priority]</code><br />
   * <code>// $cript Benoetige ALLES [item] [priority]</code><br />
   * Tries to transfer the maxAmount of item from other units to this unit. Issues warning if
   * minAmount cannot be supplied. <code>Benoetige JE</code> tries to reserve amount of item for
   * every person in the unit. Fractional amounts are possible and rounded up.
   * <code>Benoetige ALLES item</code> is equivalent to <code>Benoetige 0 infinity item</code>,
   * <code>Benoetige ALLES</code> is equivalent to <code>Benoetige ALLES</code> for every itemtype
   * in the region.<br/>
   * <code>Benoetige KRAUT</code> is the same for every herb type in the region.<br/>
   * <code>BenoetigeFremd unit (JE amount)|(minAmount [maxAmount]) item</code><br />
   * <code>BenoetigeFremd</code> does the same, but for the given unit instead of the current unit.
   * Needs with higher priority are satisfied first. If no priority is given,
   * {@link #DEFAULT_PRIORITY} is used.
   */
  protected void commandNeed(String[] tokens) {
    Unit unit = currentUnit;
    if (tokens[0].equals("BenoetigeFremd")) {
      unit = helper.getUnit(tokens[1]);
      if (unit == null || unit.getRegion() != currentUnit.getRegion()) {
        addNewMessage("Ziel ist nicht in unserer Region");
        return;
      }
      // erase unit token for easier processing afterwards
      tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
      tokens[0] = "BenoetigeFremd";
    }

    int priority;
    try {
      priority = Integer.parseInt(tokens[tokens.length - 1]);
      tokens = Arrays.copyOf(tokens, tokens.length - 1);
    } catch (NumberFormatException e) {
      priority = DEFAULT_PRIORITY;
    }

    if (tokens.length < 2 || tokens.length > 5) {
      addNewError("falsche Anzahl Argumente");
      return;
    }

    try {
      if (ALLOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNeed(tokens[2], unit, 0, Integer.MAX_VALUE, priority);
        } else {
          for (String item : supplyMap.keySet()) {
            addNeed(item, unit, 0, Integer.MAX_VALUE, priority);
          }
        }
      } else if (EACHOrder.equals(tokens[1])) {
        if (tokens.length != 4) {
          addNewError("ungültige Argumente für Benoetige JE x Ding");
        } else {
          int amount = (int) Math.ceil(unit.getPersons() * Double.parseDouble(tokens[2]));
          String item = tokens[3];
          addNeed(item, unit, amount, amount, priority);
        }
      } else if (KRAUTOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNewError("zu viele Parameter");
        }
        if (world.rules.getItemCategory("herbs") == null) {
          addNewError("Spiel kennt keine Kräuter");
        } else {
          for (Item item : currentUnit.getItems()) {
            if (world.rules.getItemCategory("herbs").equals(item.getItemType().getCategory())) {
              addNeed(item.getOrderName(), unit, 0, Integer.MAX_VALUE, priority);
            }
          }
        }
      } else if (tokens.length > 4) {
        addNewError("zu viele Parameter");
      } else {
        int minAmount = Integer.parseInt(tokens[1]);
        int maxAmount = tokens.length == 3 ? minAmount : Integer.parseInt(tokens[2]);
        String item = tokens[tokens.length - 1];
        addNeed(item, unit, minAmount, maxAmount, priority);
      }
    } catch (NumberFormatException exc) {
      addNewError("Ungültige Zahl in Benoetige");
    }
  }

  /**
   * <code>// $cript BerufDepotVerwalter [[ZusatzMin] ZusatzMax]</code><br />
   * Collects all free items in the region, Versorge 100, calls Ueberwache
   */
  protected void commandDepot(String[] tokens) {
    if (tokens.length > 3) {
      addNewError("zu viele Argumente");
    }
    commandMonitor(new String[] { "Ueberwache" });

    int costs = 0;
    for (Unit u : currentRegion.units()) {
      if (u.getFaction() == currentFaction) {
        costs += u.getRace().getMaintenance() * u.getPersons();
      }
    }
    int zusatz1 = 0, zusatz2 = 0;
    if (tokens.length > 1) {
      try {
        zusatz1 = Integer.parseInt(tokens[1]);
        if (tokens.length > 2) {
          zusatz2 = Integer.parseInt(tokens[2]);
        }
      } catch (NumberFormatException e) {
        addNewError("Zahl erwartet");
      }
    }
    addNeed("Silber", currentUnit, costs + zusatz1, costs + zusatz2, DEPOT_SILVER_PRIORITY);

    commandNeed(new String[] { "Benoetige ", ALLOrder, String.valueOf(DEPOT_PRIORITY) });
    commandSupply(new String[] { "Versorge", "100" });
  }

  /**
   * <code>Versorge [[item1]...] priority</code> -- set supply priority. Units with negative
   * priority only deliver for minimum needs. Needs are satisfied in descending order of priority.
   * If no items are given, the priority is adjusted for alle the unit's items.
   */
  protected void commandSupply(String[] tokens) {
    int priority = 0;
    if (tokens.length < 2) {
      addNewError("zu wenig Argumente");
    }
    try {
      priority = Integer.parseInt(tokens[tokens.length - 1]);
    } catch (NumberFormatException e) {
      addNewError("Zahl erwartet");
      return;
    }
    if (tokens.length == 2) {
      for (Item item : currentUnit.getItems()) {
        Supply supply = getSupply(item.getOrderName(), currentUnit);
        if (supply != null) {
          supply.priority = priority;
        }
      }
    } else {
      for (int i = 1; i < tokens.length - 1; ++i) {
        Supply supply = getSupply(tokens[i], currentUnit);
        if (supply != null) {
          supply.priority = priority;
        }
      }
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
      StringBuilder warning = new StringBuilder();
      String foo = currentOrder.substring(currentOrder.indexOf("+"));
      warning.append(foo.indexOf(" ") >= 0 ? foo.substring(foo.indexOf(" ") + 1) : "");
      addNewWarning(warning.toString());
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
    if (tokens.length < 3 || tokens.length % 2 != 1) {
      addNewError("falsche Anzahl Argumente");
      return;
    }
    List<Skill> targetSkills = new LinkedList<Skill>();

    for (int i = 1; i < tokens.length; i++) {
      try {
        if (i < tokens.length - 1) {
          SkillType skill = world.rules.getSkillType(tokens[i++]);
          int level = Integer.parseInt(tokens[i]);
          if (skill == null) {
            addNewError("unbekanntes Talent " + tokens[i - 1]);
          }
          targetSkills.add(new Skill(skill, 0, level, 1, true));
        } else {
          addNewError("unerwartetes Token " + tokens[i]);
        }
      } catch (NumberFormatException e) {
        addNewError("ungültige Stufe " + tokens[i]);
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

  /**
   * <code>// $cript BerufBotschafter [Talent]</code> -- earn money if necessary, otherwise learn
   * skill<br />
   */
  protected void commandEmbassador(String[] tokens) {
    Skill skill = null;
    if (tokens.length > 1) {
      skill = getSkill(tokens[1], 10);
    } else {
      skill = getSkill(EresseaConstants.S_WAHRNEHMUNG.toString(), 10);
      if (skill == null) {
        skill = getSkill(EresseaConstants.S_AUSDAUER.toString(), 10);
      }
    }

    commandClear(new String[] { "Loeschen" });

    if (helper.getSilver(currentUnit) < 100) {
      if (hasEntertain() && currentUnit.getSkill(EresseaConstants.S_UNTERHALTUNG) != null
          && currentUnit.getSkill(EresseaConstants.S_UNTERHALTUNG).getLevel() > 0) {
        addNewOrder(ENTERTAINOrder, true);
      } else if (hasWork()) {
        addNewOrder(WORKOrder, true);
      } else {
        addNewWarning("Einheit verhungert");
      }
    } else if (skill == null) {
      StringBuilder order = new StringBuilder();
      if (tokens.length > 1) {
        order.append(tokens[1]);
      }
      for (int i = 2; i < tokens.length; ++i) {
        order.append(" ").append(tokens[i]);
      }
      addNewOrder(order.toString(), true);
    } else {
      learn(currentUnit, Collections.singleton(skill));
      if (tokens.length > 2) {
        addNewError("zu viele Argumente");
      }
    }
  }

  protected void commandMonitor(String[] tokens) {
    if (tokens.length > 1) {
      addNewError("zu viele Argumente");
    }

    // check if region units are allowed
    Map<Faction, List<Unit>> warnings = new HashMap<Faction, List<Unit>>();
    for (Unit u : currentRegion.units()) {
      if (u.getFaction() != currentUnit.getFaction()) {
        if (!(allowedUnits.containsKey(u.getFaction()) && (allowedUnits.get(u.getFaction())
            .contains(u) || allowedUnits.get(u.getFaction()).contains(currentRegion.getZeroUnit())))) {
          if (!(requiredUnits.containsKey(u.getFaction()) && requiredUnits.get(u.getFaction())
              .contains(u))) {
            List<Unit> list = warnings.get(u.getFaction());
            if (list == null) {
              list = new LinkedList<Unit>();
              warnings.put(u.getFaction(), list);
            }
            list.add(u);
          }
        }
      }
    }

    for (Entry<Faction, List<Unit>> entry : warnings.entrySet()) {
      StringBuilder sb = new StringBuilder();
      sb.append(entry.getKey()).append(" hat unerlaubte Einheiten:");
      int i = 0;
      for (Unit u : entry.getValue()) {
        if (++i < 4) {
          sb.append(" ");
          sb.append(u.toString());
        }
      }
      entry.getValue().clear();
      addNewWarning(sb.toString());
    }

    // check if required units are present
    for (Faction f : requiredUnits.keySet()) {
      for (Unit u : requiredUnits.get(f)) {
        if (u.getRegion() != currentUnit.getRegion()) {
          addNewWarning("Einheit " + u + " der Partei " + f + " nicht mehr da.");
        }
      }
    }
  }

  protected void commandAllow(String[] tokens) {
    if (tokens.length < 3) {
      addNewError("zu wenige Argumente");
      return;
    }

    Faction faction = helper.getFaction(tokens[1]);
    Map<Faction, Set<Unit>> map;
    if (tokens[0].equals("Erlaube")) {
      map = allowedUnits;
    } else {
      map = requiredUnits;
    }
    if (faction == null) {
      addNewError("unbekannte Partei");
    } else {
      Set<Unit> set = map.get(faction);
      if (set == null) {
        set = new HashSet<Unit>();
        map.put(faction, set);
      }
      if (ALLOrder.equals(tokens[2])) {
        set.add(currentRegion.getZeroUnit());
        if (tokens.length > 3) {
          addNewError("zu viele Argumente");
        }
      } else {
        for (int i = 2; i < tokens.length; ++i) {
          Unit u = helper.getUnit(tokens[i]);
          if (u != null) {
            set.add(u);
          }
        }
      }
    }
  }

  /**
   * <code>// $cript Ernaehre [amount]</code> -- Earn as much money as possible (or the specified
   * amount), Versorge {@value #DEFAULT_EARN_PRIORITY}
   */
  protected void commandEarn(String[] tokens) {
    int amount = -1;
    if (tokens.length > 1) {
      try {
        amount = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        addNewError("ungültige Zahl " + tokens[1]);
        return;
      }
      if (tokens.length > 2) {
        addNewError("zu viele Argumente");
      }
    }

    // Ernaehre includes Versorge
    commandSupply(new String[] { "Versorge", String.valueOf(DEFAULT_EARN_PRIORITY) });

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
    int entertain = 0, entertain2 = 0, tax = 0, tax2 = 0;
    if (entertaining != null && hasEntertain()) {
      entertain2 = 20 * entertaining.getLevel() * currentUnit.getPersons();
      entertain = Math.max(0, Math.min(currentRegion.maxEntertain(), entertain2));
    }
    if (taxing != null && isSoldier(currentUnit)) {
      tax2 = 20 * taxing.getLevel() * currentUnit.getPersons();
      tax = Math.min(currentRegion.getSilver(), tax2);
    }

    if (tax > entertain) {
      addNewOrder(TAXOrder + " " + (amount > 0 ? amount : "") + COMMENTOrder + " " + tax + ">"
          + entertain, true);
      if (tax >= currentRegion.getSilver() + workers * 10 - currentRegion.getPeasants() * 10) {
        addNewWarning("Bauern verhungern");
      }
      if (tax2 > tax * 2) {
        addNewWarning("Treiber unterbeschäftigt " + tax2 + ">" + tax);
        entertain = currentRegion.maxEntertain();
      }
    } else if (entertain > 0) {
      addNewOrder(ENTERTAINOrder + " " + (amount > 0 ? amount : "") + COMMENTOrder + " "
          + entertain + ">" + tax, true);
      if (amount > currentRegion.maxEntertain()) {
        addNewWarning("zu viele Arbeiter");
      } else if (entertain2 > entertain * 2) {
        addNewWarning("Unterhalter unterbeschäftigt " + entertain2 + ">" + entertain);
        entertain = currentRegion.maxEntertain();
      }
    } else {
      addNewOrder(WORKOrder + " " + (amount > 0 ? amount : ""), true);
      if ((maxWorkers - workers) * 10 < Math.min(amount, currentUnit.getModifiedPersons() * 10)) {
        addNewWarning("zu viele Arbeiter");
      }
    }
    setConfirm(currentUnit, true);
  }

  /**
   * <code>// $cript Handel Menge [ALLES | Verkaufsgut...] Warnung</code>: trade luxuries, Versorge
   * {@value #DEFAULT_EARN_PRIORITY}. Warnung can be "Talent", "Menge", or "nie"<br />
   */
  protected void commandTrade(String[] tokens) {
    if (tokens.length < 2) {
      addNewError("zu wenige Argumente");
    }

    commandSupply(new String[] { "Versorge", String.valueOf(DEFAULT_EARN_PRIORITY) });

    String warning = null;
    if (W_SKILL.equals(tokens[tokens.length - 1]) || W_AMOUNT.equals(tokens[tokens.length - 1])
        || W_NEVER.equals(tokens[tokens.length - 1])) {
      warning = tokens[tokens.length - 1];
    }

    int amount = -1;
    try {
      amount = Integer.parseInt(tokens[1]);
    } catch (NumberFormatException e) {
      addNewError("ungültige Zahl " + tokens[1]);
      return;
    }

    Skill buySkill =
        currentUnit.getModifiedSkill(world.rules.getSkillType(EresseaConstants.S_HANDELN));
    if (buySkill == null) {
      addNewError("kein Handelstalent");
      return;
    }

    if (currentRegion.getPrices() == null) {
      addNewError("kein Handel möglich");
      return;
    }

    LuxuryPrice buyGood = null;
    for (Entry<StringID, LuxuryPrice> entry : currentRegion.getPrices().entrySet()) {
      LuxuryPrice price = entry.getValue();
      if (price.getPrice() < 0) {
        buyGood = price;
      } else {
        if (currentRegion.getOldPrices() != null
            && currentRegion.getOldPrices().get(entry.getKey()) != null
            && price.getPrice() < currentRegion.getOldPrices().get(entry.getKey()).getPrice()) {
          addNewWarning("Preis gesunken: " + price.getItemType());
        }
      }
    }

    int volume = currentRegion.maxLuxuries();

    removeOrdersLike(SELLOrder + ".*", true);
    removeOrdersLike(BUYOrder + ".*", true);

    // Gueterzahl intitialisieren
    int guetersumme = 0;

    if (amount > 0 && (volume <= 0 || buyGood == null)) {
      addNewError("Kein Handel möglich");
    }

    // Soll eingekauft werden?
    if (volume > 0 && amount > 0 && buyGood != null) {
      // Berechne noetige Geldmenge fuer Einkauf (einfacher: Modulorechnung, aber wegen
      // Rundungsfehler nicht umsetzbar)
      int hilfNochUebrig = amount;
      int hilfFaktor = 1;
      int geldNoetig = 0;
      while (hilfNochUebrig > 0) {
        if (hilfNochUebrig > volume) {
          hilfNochUebrig -= volume;
          geldNoetig -= (volume * hilfFaktor * buyGood.getPrice()); // price is negative
          hilfFaktor++;
        } else {
          geldNoetig -= (hilfNochUebrig * hilfFaktor * buyGood.getPrice());
          hilfNochUebrig = 0;
        }
      }

      addNeed("Silber", currentUnit, geldNoetig, geldNoetig, TRADE_PRIORITY);

      // Einkaufsbefehl setzen, wenn notwendig
      if (amount > 0) {
        addNewOrder(BUYOrder + " " + amount + " " + buyGood.getItemType().getOrderName(), true);
        guetersumme += amount;
      }
    }

    if (volume > 0 && buyGood != null) {
      List<String> goods = new LinkedList<String>();
      // Verkaufsbefehl setzen, wenn notwendig
      if (tokens.length > 2 && ALLOrder.equals(tokens[2])) {
        if (world.rules.getItemCategory("luxuries") == null) {
          addNewError("Spiel kennt keine Luxusgüter");
        } else {
          for (ItemType luxury : world.getRules().getItemTypes()) {
            if (!luxury.equals(buyGood.getItemType())
                && world.rules.getItemCategory("luxuries").equals(luxury.getCategory())) {
              goods.add(luxury.getOrderName());
            }
          }
        }
      } else {
        for (int i = 2; i < (warning == null ? tokens.length : tokens.length - 1); ++i) {
          goods.add(tokens[i]);
        }
      }

      int maxAmount = buySkill.getLevel() * currentUnit.getPersons() * 10;

      boolean skillWarning = false;
      for (String luxury : goods) {
        int goodAmount = Math.min(volume, maxAmount - guetersumme);
        if (W_NEVER.equals(warning)) {
          if (goodAmount > 0) {
            addNeed(luxury, currentUnit, 0, goodAmount, TRADE_PRIORITY);
          }
        } else if (W_SKILL.equals(warning)) {
          goodAmount = getSupply(luxury);
          goodAmount = Math.min(goodAmount, volume);
          if (goodAmount > maxAmount - guetersumme) {
            goodAmount = Math.min(goodAmount, maxAmount - guetersumme);
            skillWarning = true;
          }
          if (goodAmount > 0) {
            addNeed(luxury, currentUnit, 0, goodAmount, TRADE_PRIORITY);
          }
        } else {
          if (goodAmount > maxAmount - guetersumme) {
            goodAmount = Math.min(goodAmount, maxAmount - guetersumme);
            skillWarning = true;
          }
          if (goodAmount > 0) {
            addNeed(luxury, currentUnit, goodAmount, goodAmount, TRADE_PRIORITY);
          }
        }
        if (goodAmount > 0) {
          if (goodAmount == volume || W_NEVER.equals(warning)) {
            addNewOrder(SELLOrder + " " + ALLOrder + " " + luxury, true);
          } else {
            addNewOrder(SELLOrder + " " + goodAmount + " " + luxury, true);
          }
        }
        guetersumme += goodAmount;
      }

      // Einheit gut genug?
      if (skillWarning && (W_SKILL.equals(warning) || warning == null)) {
        addNewError("Einheit hat zu wenig Handelstalent!");
      }
    }

    setConfirm(currentUnit, true);
  }

  /**
   * <code>// $cript Quartiermeister [[Menge1 Gut 1]...]</code>: learn perception, allow listed
   * amount of goods. If other goods are detected, do not confirm orders.
   */
  protected void commandQuartermaster(String[] tokens) {
    learn(currentUnit, Collections.singleton(new Skill(world.rules
        .getSkillType(EresseaConstants.S_WAHRNEHMUNG), 30, 10, 1, true)));
    try {
      for (Item item : currentUnit.getItems()) {
        boolean okay = false;
        if (item.getItemType().getID().equals(EresseaConstants.I_USILVER)
            && item.getAmount() < 1000) {
          okay = true;
        }
        for (int i = 1; !okay && i < tokens.length - 1; i += 2) {
          if (item.getName().equals(tokens[i + 1])) {
            if (item.getAmount() <= Integer.parseInt(tokens[i])) {
              okay = true;
              break;
            }
          }
        }
        setConfirm(currentUnit, okay);
      }
    } catch (NumberFormatException e) {
      addNewError("ungültige Zahl ");
    }
    setConfirm(currentUnit, true);
  }

  /**
   * <code>// $cript Sammler [frequenz]</code>: collect herbs if there are at least "viele",
   * research herbs every frequenz rounds.
   */
  protected void commandCollector(String[] tokens) {
    if (tokens.length > 2) {
      addNewError("zu viele Argumente");
    }
    int modulo = Integer.MAX_VALUE;
    if (tokens.length > 1) {
      try {
        modulo = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        addNewError("ungültige Zahl " + tokens[1]);
        return;
      }
    }

    if (currentRegion.getRegionType().isOcean()) {
      addNewError("Sammeln nicht möglich!");
      return;
    }
    removeOrdersLike(MAKEOrder + " " + "[^T].*", true);
    removeOrdersLike(getResearchOrder() + ".*", true);
    if (modulo != Integer.MAX_VALUE
        && (world.getDate().getDate() % modulo == 0 || (!currentRegion.getHerbAmount().equals(
            "viele") && !currentRegion.getHerbAmount().equals("sehr viele")))) {
      addNewOrder(getResearchOrder(), true);
    } else {
      addNewOrder(MAKEOrder + " " + getLocalizedOrder(EresseaConstants.O_HERBS, "KRÄUTER"), true);
    }
  }

  /**
   * <code>KrautKontrolle [[[direction...] PAUSE]...]</code> move until the next PAUSE, if pause is
   * reached, research herbs.
   */
  protected void commandControl(String[] tokens) {
    for (Order o : currentUnit.getOrders2()) {
      String order = o.getText();
      if (order.startsWith(ROUTEOrder)) {
        if (order.substring(order.indexOf(" ")).trim().startsWith(PAUSEOrder)) {
          // end of route, FORSCHE
          addNewOrder(currentOrder, false);
          addNewOrder(getResearchOrder(), true);
          removeOrdersLike(ROUTEOrder + ".*", true);
        } else {
          // continue on route
          addNewOrder(currentOrder, false);
        }
        return;
      }
    }
    // no route order -- create new one
    removeOrdersLike(getResearchOrder() + ".*", true);
    StringBuilder newOrder = new StringBuilder();
    newOrder.append(PCOMMENTOrder).append(" ").append(scriptMarker).append(" ").append(tokens[0]);
    StringBuilder moveOrder = new StringBuilder(ROUTEOrder);
    boolean pause = false;
    for (int i = 1; i < tokens.length; ++i) {
      if (!pause) {
        // add move order until first PAUSE
        moveOrder.append(" ").append(tokens[i]);
      } else {
        // add to $cript order after first PAUSE
        newOrder.append(" ").append(tokens[i]);
      }
      if (PAUSEOrder.equalsIgnoreCase(tokens[i])) {
        pause = true;
      }
    }
    moveOrder.append(" ").append(PAUSEOrder);
    pause = false;
    for (int i = 1; i < tokens.length; ++i) {
      if (!pause) {
        // append movement until first PAUSE to back of $cript order
        newOrder.append(" ").append(tokens[i]);
      }
      if (PAUSEOrder.equalsIgnoreCase(tokens[i])) {
        pause = true;
      }
    }
    addNewOrder(newOrder.toString(), true);
    addNewOrder(moveOrder.toString(), true);
  }

  /**
   * <code>// $cript RekrutiereMax [min [max]] [race]</code>: recruit as much as possible; warn if
   * less than min are possible
   */
  protected void commandRecruit(String[] tokens) {
    if (tokens.length > 4) {
      addNewError("zu viele Argumente");
    }
    int min = 1;
    String race = null;
    int max = Integer.MAX_VALUE;
    if (tokens.length > 1) {
      try {
        min = Integer.parseInt(tokens[1]);
        if (tokens.length > 2) {
          try {
            max = Integer.parseInt(tokens[2]);
            if (tokens.length > 3) {
              race = tokens[3];
            }
          } catch (NumberFormatException e) {
            max = Integer.MAX_VALUE;
            race = tokens[2];
            if (tokens.length > 3) {
              addNewError("zu viele Argumente");
            }
          }
        }
      } catch (NumberFormatException e) {
        min = 1;
        race = tokens[1];
        if (tokens.length > 2) {
          addNewError("zu viele Argumente");
        }
      }
    }

    Race effRace = race == null ? currentUnit.getRace() : helper.getRace(race);
    if (effRace == null) {
      addNewError("Unbekannte Rasse");
      return;
    }

    if (currentUnit.getPersons() != 0 && !effRace.equals(currentUnit.getRace())) {
      addNewWarning("race != unit race");
    }

    int amount = world.getGameSpecificRules().getRecruitmentLimit(currentUnit, effRace);

    if (currentUnit.getPersons() + amount >= max) {
      addNewWarning("Rekrutierungslimit erreicht");
      amount = Math.min(max - currentUnit.getPersons(), amount);
    }

    if (amount < min) {
      addNewError("Nicht genug Rekruten");
    }

    if (effRace.getRecruitmentCosts() > 0) {
      int costs = amount * effRace.getRecruitmentCosts();
      addNeed("Silber", currentUnit, costs, costs, DEFAULT_PRIORITY);
    } else {
      addNewWarning("Rekrutierungskosten unbekannt");
    }
    if (amount > 0) {
      getRecruitOrder(amount, effRace);
      addNewOrder(getRecruitOrder(amount, effRace != currentUnit.getRace() ? effRace : null), true);
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
      needMap = new LinkedHashMap<String, Map<Integer, Map<Unit, Need>>>();
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
          putSupply(item.getOrderName(), u, item.getAmount());
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
  protected Supply putSupply(String item, Unit unit, int amount) {
    Map<Unit, Supply> itemSupplyMap = supplyMap.get(item);
    if (itemSupplyMap == null) {
      itemSupplyMap = new LinkedHashMap<Unit, Supply>();
      supplyMap.put(item, itemSupplyMap);
    }
    Supply result = new Supply(unit, item, getItemCount(unit, item));
    itemSupplyMap.put(unit, result);
    return result;
  }

  /**
   * Tries to satisfy all needs in the current needMap by adding GIVE orders to supplyers.
   */
  protected void satisfyNeeds() {
    for (String item : needMap.keySet()) {
      // sort supplies by priority
      Map<Unit, Supply> itemSupply = supplyMap.get(item);
      if (itemSupply == null) {
        itemSupply = Collections.emptyMap();
      } else {
        Supply[] sorted = null;
        sorted = itemSupply.values().toArray(new Supply[0]);

        // this causes problems in BeansShell; I don't know why
        // Arrays.sort(sorted);
        // doing insertion sort instead
        for (int j = 1; j < sorted.length; ++j) {
          for (int i = 0; i < j; ++i) {
            if (sorted[j].compareTo(sorted[i]) < 0) {
              // if (current.priority > sorted[i].priority) {
              Supply temp = sorted[i];
              sorted[i] = sorted[j];
              sorted[j] = temp;
            }
          }
        }

        itemSupply.clear();
        for (Supply supply : sorted) {
          itemSupply.put(supply.unit, supply);
        }
      }

      Map<Unit, Integer> reserves = new HashMap<Unit, Integer>();

      Map<Integer, Map<Unit, Need>> pMap = needMap.get(item);

      List<Integer> prios = new ArrayList<Integer>();
      for (Integer key : pMap.keySet()) {
        prios.add(key);
      }
      Collections.sort(prios);
      Collections.reverse(prios);

      for (Integer prio : prios) {
        Map<Unit, Need> nMap = pMap.get(prio);
        // try to satisfy minimum need by own items
        for (Need need : nMap.values()) {
          reserveNeed(need, true, reserves);
        }

        // try to satisfy minimum needs with GIVE
        for (Need need : nMap.values()) {
          giveNeed(need, true);
        }

        // add warnings for unsatisfied needs
        for (Need need : nMap.values()) {
          if (need.getMinAmount() > 0) {
            addWarning(need.getUnit(), "braucht " + need.getMinAmount() + " mehr " + need.getItem());
          }
        }

        // try to satisfy max needs, ignore infinite needs first
        for (Need need : nMap.values()) {
          if (need.getAmount() != Integer.MAX_VALUE) {
            reserveNeed(need, false, reserves);
          }
        }

        for (Need need : nMap.values()) {
          if (need.getAmount() != Integer.MAX_VALUE) {
            giveNeed(need, false);
          }
        }

        // now, finally, satisfy infinite needs
        for (Need need : nMap.values()) {
          if (need.getAmount() == Integer.MAX_VALUE) {
            reserveNeed(need, false, reserves);
          }
        }

        for (Need need : nMap.values()) {
          if (need.getAmount() == Integer.MAX_VALUE) {
            giveNeed(need, false);
          }
        }

        // add messages for unsatisfied needs
        for (Need need : nMap.values()) {
          if (need.getMinAmount() <= 0 && need.getMaxAmount() > 0
              && need.getMaxAmount() != Integer.MAX_VALUE) {
            need.getUnit().addOrder("; braucht " + need.getMaxAmount() + " mehr " + need.getItem(),
                false);
          }
        }
      }
      for (Unit u : reserves.keySet()) {

        int amount = reserves.get(u);
        if (amount > 0) {
          if (amount == u.getPersons()) {
            u.addOrder(getReserveOrder(u, item // + COMMENTOrder + need.toString()
                , 1, true), false);
          } else {
            u.addOrder(getReserveOrder(u, item, amount, false), false);
          }
        }
      }
    }

  }

  /**
   * Tries to satisfy (minimum) need by a RESERVE order
   * 
   * @param need
   * @param min
   * @param reserves
   */
  protected void reserveNeed(Need need, boolean min, Map<Unit, Integer> reserves) {
    int amount = min ? need.getMinAmount() : need.getAmount();
    Supply supply = getSupply(need.getItem(), need.getUnit());
    if (supply == null)
      return;

    // only suppliers with positive priority serve maximum needs
    amount = Math.min(amount, supply.getAmount());
    if (amount > 0) {
      need.reduceAmount(amount);
      need.reduceMinAmount(amount);
      supply.reduceAmount(amount);
      if (min) {
        if (reserves.containsKey(need.getUnit())) {
          amount += reserves.get(need.getUnit());
        }
        reserves.put(need.getUnit(), amount);
      }
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
        if (supply.getUnit() != need.getUnit() && (min || supply.priority > 0)) {
          int giveAmount = Math.min(amount, supply.getAmount());
          if (giveAmount > 0) {
            supply.getUnit().addOrder(
                getGiveOrder(supply.getUnit(), need.getUnit().getID().toString(), need.getItem(),
                    giveAmount, false)
                    + COMMENTOrder + need.toString(), false);
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
   * Returns the total supply for an item.
   * 
   * @param item Order name of the supplied item
   * @return The supply or 0, if none has been registered.
   */
  protected int getSupply(String item) {
    Map<Unit, Supply> map = supplyMap.get(item);
    int goodAmount = 0;
    if (map != null) {
      for (Supply s : map.values()) {
        goodAmount += s.getAmount();
      }
    }
    return goodAmount;
  }

  /**
   * Returns a supply of a unit for an item.
   * 
   * @param item Order name of the supplied item
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
   * @param item Order name of the required item
   * @param unit
   * @param minAmount
   * @param maxAmount
   */
  protected void addNeed(String item, Unit unit, int minAmount, int maxAmount, int priority) {
    Map<Integer, Map<Unit, Need>> map = needMap.get(item);
    if (map == null) {
      map = new LinkedHashMap<Integer, Map<Unit, Need>>();
      needMap.put(item, map);
    }
    Map<Unit, Need> pMap = map.get(priority);
    if (pMap == null) {
      pMap = new LinkedHashMap<Unit, Need>();
      map.put(priority, pMap);
    }
    Need need = pMap.get(unit);
    if (need == null) {
      need = new Need(unit, item, 0, 0);
      pMap.put(unit, need);
    }

    if (need.getAmount() != Integer.MAX_VALUE) {
      if (maxAmount == Integer.MAX_VALUE) {
        need.setAmount(maxAmount);
      } else {
        need.reduceAmount(-maxAmount);
      }
    }
    if (need.getMinAmount() != Integer.MAX_VALUE) {
      if (minAmount == Integer.MAX_VALUE) {
        need.setMinAmount(minAmount);
      } else {
        need.reduceMinAmount(-minAmount);
      }
    }
  }

  /**
   * Returns a need of a unit for an item.
   * 
   * @param item Order name of the required item
   * @param unit
   * @return The need or <code>null</code> if none has been registered.
   */
  protected Need getNeed(String item, Unit unit, int priority) {
    Map<Integer, Map<Unit, Need>> map = needMap.get(item);
    if (map == null)
      return null;
    Map<Unit, Need> pMap = map.get(priority);
    if (pMap == null)
      return null;

    return pMap.get(unit);
  }

  /**
   * Marks the unit as soldier. Learns its best weapon skill. Reserves suitable weapon, armor, and
   * shield if available.
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
        addNewWarning("kein Kampftalent");
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
      if (weapons.isEmpty()) {
        addNewError("keine passenden Waffen bekannt für " + weaponSkill);
      }
    } else {
      if (u.getItem(weapon) != null) {
        weapons.add(u.getItem(weapon));
      } else {
        addNewError("keine " + weapon);
      }
    }

    // note that "shield" is a subcategory of "armour"
    ArrayList<Item> shields = findItems(shield, u, "shield");
    if (shields.isEmpty()) {
      addNewError("keine Schilde bekannt");
    }

    ArrayList<Item> armors = findItems(armor, u, "armour");
    if (armors.isEmpty()) {
      addNewError("keine Rüstungn bekannt");
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
      addNewError("konnte Waffe nicht reservieren");
    }
    if (!NULL.equals(sShield)
        && !reserveEquipment(shield, shields, W_ARMOR.equals(warning) || W_SHIELD.equals(warning))) {
      addNewError("konnte Schilde nicht reservieren");
    }
    if (!NULL.equals(sArmor) && !reserveEquipment(armor, armors, W_ARMOR.equals(warning))) {
      addNewError("konnte Rüstung nicht reservieren");
    }
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
      addNewError("kein Kampftalent");
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
        if (itemCategory.equals(item.getItemType().getCategory())) {
          items.add(item);
        }
      }
    } else {
      if (u.getItem(itemType) != null) {
        items.add(u.getItem(itemType));
      }
    }
    if (items.isEmpty()) {
      for (Object o : itemCategory.getInstances()) {
        ItemType type = (ItemType) o;
        items.add(new Item(type, 0));
        break;
      }
    }
    return items;
  }

  protected boolean reserveEquipment(ItemType preferred, List<Item> ownStuff, boolean warn) {
    if (preferred != null) {
      // reserve requested weapon
      commandNeed(new String[] { "Benoetige", EACHOrder, "1", preferred.getOrderName(),
          String.valueOf(DEFAULT_PRIORITY) });
    } else if (!ownStuff.isEmpty()) {
      int supply = 0;
      for (Item w : ownStuff) {
        // reserve all matching weapons, except first one
        if (supply > 0) {
          commandNeed(new String[] { "Benoetige",
              Integer.toString(Math.min(currentUnit.getPersons() - supply, w.getAmount())),
              w.getOrderName(), String.valueOf(DEFAULT_PRIORITY) });
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
      commandNeed(new String[] { "Benoetige", min, max, w.getOrderName(),
          String.valueOf(DEFAULT_PRIORITY) });
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
    MOVEOrder = getLocalizedOrder(EresseaConstants.O_MOVE, MOVEOrder);
    ROUTEOrder = getLocalizedOrder(EresseaConstants.O_ROUTE, ROUTEOrder);
    PAUSEOrder = getLocalizedOrder(EresseaConstants.O_PAUSE, PAUSEOrder);
    RESEARCHOrder = getLocalizedOrder(EresseaConstants.O_RESEARCH, RESEARCHOrder);
    RECRUITOrder = getLocalizedOrder(EresseaConstants.O_RECRUIT, RECRUITOrder);

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
   * Registers a pattern. All lines matching this regular expression (case sensitive!) will be
   * removed from here on. If retroActively, also orders that are already in {@link #newOrders}.
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
   * Adds an error line.
   * 
   * @param line The current order line
   * @param hint
   */
  protected void addNewError(String hint) {
    error = line;
    // errMsg = hint;
    addNewOrder(COMMENTOrder + " TODO: " + hint + " (Fehler in Zeile " + error + ")", true);
    setConfirm(currentUnit, false);
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
    addNewOrder(COMMENTOrder + " TODO: " + hint + " (Warnung in Zeile " + error + ")", true);
    setConfirm(currentUnit, false);
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
   * Returns a <code>RESEARCH HERBS</code> order.
   */
  protected String getResearchOrder() {
    return RESEARCHOrder + " " + getLocalizedOrder(EresseaConstants.O_HERBS, "KRÄUTER");
  }

  /**
   * Returns a <code>RECRUIT amount race</code> order.
   */
  protected String getRecruitOrder(int amount, Race race) {
    return RECRUITOrder + " " + amount
        + (race != null ? (" " + getLocalizedOrder("race." + race.getID(), race.getName())) : "");
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
      if (weapons.isInstance(item.getItemType())) {
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
      for (Order order : u.getOrders2()) {
        String line = order.getText();
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
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setMaximum(world.getRegions().size());

      // call self for all regions
      for (Region r : world.getRegions()) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(r.toString(), ++progress);

        convertVorlage(faction, r);
      }
      return;
    }

    // loop for all units of faction
    final String scriptStart = PCOMMENTOrder + " " + scriptMarker + " ";
    for (Unit u : region.units())
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        currentUnit = u;
        newOrders = new ArrayList<String>();
        removedOrderPatterns = new ArrayList<String>();
        changedOrders = false;
        error = -1;
        StringBuilder lerneOrder = null;

        // loop over orders
        line = 0;
        for (Order o : u.getOrders2()) {
          ++line;
          currentOrder = o.getText();

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
                int number = Integer.MIN_VALUE;
                if (tokens.length > 1) {
                  try {
                    number = Integer.parseInt(tokens[1]);
                  } catch (Exception e) {
                    number = Integer.MIN_VALUE;
                  }
                }
                addNewOrder(scriptStart + "BerufDepotVerwalter " + (number > 0 ? number : ""), true);
              } else if (command.equals("BerufWahrnehmer")) {
                addNewOrder(scriptStart
                    + "Lerne "
                    + world.getRules().getSkillType(EresseaConstants.S_WAHRNEHMUNG.toString())
                        .getName() + " 10", true);
              } else if (command.equals("Depot")) {
                addNewOrder(scriptStart + "Benoetige " + ALLOrder, true);
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
                  if (order.length() > 0) {
                    order.append(" ");
                  }
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
                if (tokens[tokens.length - 1].equals("nie")) {
                  order.append(" nie");
                }
                addNewOrder(order.toString(), true);
                if (command.equals("Steuermann") && tokens.length != 3) {
                  addNewError("unzulässige Argumente");
                }
              } else if (command.equals("VersorgeFremd") || command.equals("BenoetigeFremd")) {
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
                  if (order.length() > scriptStart.length()) {
                    order.append(" ");
                  }
                  order.append(token);
                }
                addNewOrder(order.toString(), true);
                if (command.equals("Steuermann") && tokens.length != 3) {
                  addNewError("unzulässige Argumente");
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
              log.fine("unknown order " + currentOrder);
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
   * @param u
   * @param orderFilter
   */
  protected boolean changeOrders(Unit u, OrderFilter orderFilter) {
    List<String> newOrders2 = new ArrayList<String>();
    boolean changedOrders2 = false;

    // loop over orders
    for (Order command : u.getOrders2()) {
      if (orderFilter.changeOrder(command)) {
        changedOrders2 = true;
        if (orderFilter.changedOrder() != null) {
          newOrders2.add(orderFilter.changedOrder());
        }
      } else {
        newOrders2.add(command.getText());
      }
    }
    if (changedOrders2) {
      u.setOrders(newOrders2);
    }
    return changedOrders2;
  }

  /**
   * Private utility script written to convert some of my faction's orders. Not interesting for the
   * general public.
   * 
   * @param faction
   * @param region
   */
  public void cleanShortOrders(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();
    currentFaction = faction;

    initLocales();

    if (region == null) {
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setMaximum(world.getRegions().size());

      // call self for all regions
      for (Region r : world.getRegions()) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(r.toString(), ++progress);

        cleanShortOrders(faction, r);
      }
      return;
    }

    // loop for all units of faction
    for (Unit u : region.units()) {
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        if (changeOrders(u, new ShortOrderFilter(world))) {
          notifyMagellan(u);
        }
      }
    }
  }

  /**
   * Private utility script written to convert some of my faction's orders. Not interesting for the
   * general public.
   * 
   * @param faction
   * @param region
   */
  public void cleanScripts(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();
    currentFaction = faction;

    initLocales();

    if (region == null) {
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setMaximum(world.getRegions().size());

      // call self for all regions
      for (Region r : world.getRegions()) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(r.toString(), ++progress);

        cleanScripts(faction, r);
      }
      return;
    }

    // loop for all units of faction
    for (Unit u : region.units()) {
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        currentUnit = u;
        newOrders = new ArrayList<String>();
        removedOrderPatterns = new ArrayList<String>();
        changedOrders = false;
        error = -1;

        // loop over orders
        line = 0;
        for (Order command : u.getOrders2()) {
          currentOrder = command.getText();
          if (currentOrder.startsWith("@RESERVIEREN")) {
            addNewOrder("// $cript Benoetige " + currentOrder.substring(currentOrder.indexOf(" ")),
                true);
            addNewOrder(currentOrder.substring(1), true);
          } else {
            if (command.isLong() || command.isPersistent() || command.getText().startsWith("//")) {
              addNewOrder(command.getText(), false);
            } else {
              // omit
              changedOrders = true;
            }
          }
        }
        if (changedOrders) {
          currentUnit.setOrders(newOrders);
        }
        notifyMagellan(currentUnit);

        newOrders = null;
      }
    }

  }

  /**
   * Private utility script written to handle unwanted orders created by the TeachPlugin. Not
   * interesting for the general public.
   * 
   * @param faction
   * @param region
   */
  public void undoTeaching(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();
    currentFaction = faction;

    initLocales();

    if (region == null) {
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setMaximum(world.getRegions().size());

      // call self for all regions
      for (Region r : world.getRegions()) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(r.toString(), ++progress);

        undoTeaching(faction, r);
      }
      return;
    }

    // loop for all units of faction
    for (Unit u : region.units()) {
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        currentUnit = u;
        newOrders = new ArrayList<String>();
        removedOrderPatterns = new ArrayList<String>();
        changedOrders = false;
        error = -1;

        // loop over orders
        line = 0;
        for (Order command : u.getOrders2()) {
          currentOrder = command.getText();
          if (currentOrder.startsWith("; $$$ LE")) {
            addNewOrder(currentOrder.substring(currentOrder.indexOf("$$$") + 4) + " ; restored",
                true);
            changedOrders = true;
          } else if (changedOrders
              && (currentOrder.startsWith("LERNE") || currentOrder.startsWith("LEHRE"))) {
            // skip
          } else {
            addNewOrder(command.getText(), false);
          }
        }
        if (changedOrders) {
          currentUnit.setOrders(newOrders);
        }
        notifyMagellan(currentUnit);

        newOrders = null;
      }
    }

  }

  /**
   * Again, a very specialized procedure
   */
  protected void markTRound(int round) {
    if (world.getDate().getDate() == round) {
      for (Unit u : world.getUnits()) {
        changeOrders(u, new TeachRoundOrderFilter());
        u.addOrder("// $$L" + (round + 1));
      }
    }
  }

  private void setNamespaces(MagellanPlugIn plugin, String namespaces) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    ExtendedCommandsHelper.invoke(plugin, "setNamespaces", new Class[] { String.class },
        new Object[] { namespaces });
  }

  private String getNamespaces(MagellanPlugIn plugin) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    return (String) ExtendedCommandsHelper.invoke(plugin, "getNamespacesString", new Class[] {},
        new Object[] {});
  }

  protected void teachRegion(Collection<Region> regions, String namespaces) {
    ArrayList<Region> regions2 = new ArrayList<Region>();
    for (Region r : regions) {
      if (r != null) {
        regions2.add(r);
      }
    }
    if (regions2.isEmpty())
      return;

    log.info("trying to call TeachPlugin method..");
    MagellanPlugIn plugin = helper.getPlugin("magellan.plugin.teacher.TeachPlugin");
    if (plugin != null) {
      try {
        String oldNameSpaces = null;
        if (namespaces != null) {
          oldNameSpaces = getNamespaces(plugin);
          setNamespaces(plugin, namespaces);
        }
        ExtendedCommandsHelper.invoke(plugin, "doTeachUnits", new Class[] { Collection.class },
            new Collection[] { regions2 });
        if (oldNameSpaces != null) {
          setNamespaces(plugin, oldNameSpaces);
        }
      } catch (ClassCastException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (SecurityException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (NoSuchMethodException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (IllegalArgumentException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (IllegalAccessException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (InvocationTargetException e) {
        log.warn("error calling TeachPlugin", e);
      }
    } else {
      log.warn("TeachPlugin not found");
    }
  }

  protected void teachAll(String namespaces) {
    log.info("trying to call TeachPlugin method..");
    MagellanPlugIn plugin = helper.getPlugin("magellan.plugin.teacher.TeachPlugin");
    if (plugin != null) {
      try {
        String oldNamespaces = null;
        if (namespaces != null) {
          oldNamespaces = getNamespaces(plugin);
          setNamespaces(plugin, namespaces);
        }
        ExtendedCommandsHelper.invoke(plugin, "execute", new Class[] { String.class },
            new Object[] { "EXECUTE_ALL" });
        if (oldNamespaces != null) {
          setNamespaces(plugin, oldNamespaces);
        }
      } catch (ClassCastException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (SecurityException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (NoSuchMethodException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (IllegalArgumentException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (IllegalAccessException e) {
        log.warn("error calling TeachPlugin", e);
      } catch (InvocationTargetException e) {
        log.warn("error calling TeachPlugin", e);
      }
    } else {
      log.warn("TeachPlugin not found");
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

class Supply implements Comparable<Supply> {

  Unit unit;
  /** The order name of the item. */
  String item;
  int amount;
  int priority;

  public Supply(Unit unit, String item, int amount) {
    super();
    if (unit == null)
      throw new NullPointerException();
    this.unit = unit;
    this.item = item;
    this.amount = amount;
    priority = E3CommandParser.DEFAULT_SUPPLY_PRIORITY;
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

  public int compareTo(Supply o) {
    return o.priority - priority;
  }

}

interface OrderFilter {
  /**
   * @return true if the order should be changed or deleted
   */
  public boolean changeOrder(String order);

  /**
   * @return true if the order should be changed or deleted
   */
  public boolean changeOrder(Order order);

  /**
   * @return the string that should replace order, <code>null</code> if the order should be be
   *         removed. If changeOrder returns <code>false</code>, the return value is undefined!
   */
  public String changedOrder();
}

class TeachRoundOrderFilter implements OrderFilter {
  public boolean changeOrder(Order order) {
    return changeOrder(order.getText());
  }

  public boolean changeOrder(String order) {
    if (order.startsWith("// $$L"))
      return true;
    return false;
  }

  public String changedOrder() {
    return null;
  }
}

class ShortOrderFilter implements OrderFilter {

  private String result;
  private GameData world;

  public ShortOrderFilter(GameData world) {
    this.world = world;
  }

  public boolean changeOrder(String command) {
    if (world.getGameSpecificStuff().getOrderChanger().isLongOrder(command))
      return false;
    else {
      if (command.startsWith("@") || command.startsWith("//"))
        return false;
      else {
        if (command.startsWith(";")) {
          result = null;
          return true;
        } else {
          result = ";" + command;
          return true;
        }
      }
    }
  }

  public boolean changeOrder(Order command) {
    if (world.getGameSpecificStuff().getOrderChanger().isLongOrder(command))
      return false;
    else {
      if (command.isLong() || command.isPersistent() || command.getText().startsWith("//"))
        return false;
      else {
        if (command.getText().startsWith(";")) {
          result = null;
          return true;
        } else {
          result = ";" + command.getText();
          return true;
        }
      }
    }
  }

  public String changedOrder() {
    return result;
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
    if (minAmount == Integer.MAX_VALUE || minAmount == Integer.MIN_VALUE)
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
