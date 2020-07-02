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
import java.util.Deque;
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
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.gamebinding.RulesException;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import magellan.plugin.extendedcommands.ExtendedCommandsHelper;

// ---start uncomment for BeanShell
// import magellan.library.*;
// import java.util.*;
// ---stop uncomment for BeanShell

class Flag {
  boolean positive;
  String name;
  int value;

  public Flag(boolean positive, String name, int value) {
    this.positive = positive;
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name + "(" + positive + "," + value + ")";
  }

}

class Warning {
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
  /** The ALWAYS warning type token */
  public static final String W_FOREIGN = "fremd";

  /** warning constants */
  protected static final int C_AMOUNT = 1, C_UNIT = 1 << 1, C_HIDDEN = 1 << 2, C_FOREIGN = 1 << 3,
      C_WEAPON = 1 << 5, C_ARMOR = 1 << 6, C_SHIELD = 1 << 7, C_SKILL = 1 << 8;

  private List<Flag> flags = new ArrayList<Flag>();

  public static final Flag[] ALL_FLAGS = new Flag[8];

  private static void initFlags() {
    ALL_FLAGS[0] = new Flag(true, W_AMOUNT, C_AMOUNT);
    ALL_FLAGS[1] = new Flag(true, W_ARMOR, C_ARMOR);
    ALL_FLAGS[2] = new Flag(false, W_FOREIGN, C_FOREIGN);
    ALL_FLAGS[3] = new Flag(false, W_HIDDEN, C_HIDDEN);
    ALL_FLAGS[4] = new Flag(true, W_SHIELD, C_SHIELD);
    ALL_FLAGS[5] = new Flag(true, W_SKILL, C_SKILL);
    ALL_FLAGS[6] = new Flag(true, W_UNIT, C_UNIT);
    ALL_FLAGS[7] = new Flag(true, W_WEAPON, C_WEAPON);
  }

  public static boolean initialized = false;
  public static Map<String, Flag> NAMES;

  public Warning(boolean all) {
    this(new String[] { W_NEVER });
    if (all) {
      setAll();
    }
  }

  public Warning(String[] tokens) {
    /** Bean Shell does not know static initializers, so this is a bit awkward */
    if (!initialized) {
      initStatic();
    }
    parse(tokens);
  }

  protected void initStatic() {
    initialized = true;
    initFlags();
    NAMES = new HashMap<String, Flag>();
    for (Flag f : ALL_FLAGS) {
      NAMES.put(f.name, f);
    }
  }

  protected void setAll() {
    for (Flag f : ALL_FLAGS) {
      add(f);
    }
  }

  private void setAll(boolean positive) {
    for (Flag f : ALL_FLAGS) {
      if (f.positive == positive) {
        add(f);
      }
    }
  }

  protected String[] parse(String[] tokens) {
    flags = new ArrayList<Flag>();
    if (tokens == null)
      return null;

    if (tokens.length == 0) {
      for (Flag f : ALL_FLAGS) {
        add(f);
      }
    }

    int i = tokens.length - 1;
    boolean hasPositive = false;
    setAll(false);
    for (; i >= 0; --i)
      if (NAMES.containsKey(tokens[i])) {
        Flag f = NAMES.get(tokens[i]);
        if (f.positive) {
          hasPositive = true;
          add(f);
        } else {
          remove(f);
        }
      } else if (W_ALWAYS.equals(tokens[i])) {
        flags = new ArrayList<Flag>();
        setAll();
      } else if (W_NEVER.equals(tokens[i])) {
        flags = new ArrayList<Flag>();
        hasPositive = true;
      } else {
        break;
      }
    if (!hasPositive) {
      setAll(true);
    }

    if (i == tokens.length - 1)
      return tokens;
    return Arrays.copyOf(tokens, i + 1);
  }

  public void add(String name) {
    if (NAMES.containsKey(name)) {
      add(NAMES.get(name));
    }
  }

  public void add(Flag f) {
    if (!flags.contains(f)) {
      flags.add(f);
    }
  }

  public void remove(Flag f) {
    flags.remove(f);
  }

  public boolean contains(String w) {
    for (Flag f : flags)
      if (f.name.equals(w))
        return true;
    return false;
  }

  public boolean contains(int flag) {
    for (Flag f : flags)
      if (f.value == flag)
        return true;
    return false;
  }

  @Override
  public String toString() {
    return flags.toString();
  }

}

class SupplyMap {
  Map<String, Map<Unit, Supply>> supplyMap;
  private E3CommandParser parser;

  public SupplyMap(E3CommandParser parser) {
    this.parser = parser;
    supplyMap = new LinkedHashMap<String, Map<Unit, Supply>>();
  }

  public Collection<String> items() {
    return supplyMap.keySet();
  }

  public int getSupply(String item) {
    Map<Unit, Supply> map = supplyMap.get(item);
    int goodAmount = 0;
    if (map != null) {
      for (Supply s : map.values()) {
        if (s.hasPriority()) {
          goodAmount += s.getAmount();
        }
      }
    }
    return goodAmount;
  }

  public Supply get(String item, Unit unit) {
    Map<Unit, Supply> map = supplyMap.get(item);
    if (map == null)
      return null;
    return map.get(unit);
  }

  public Collection<Supply> get(String item) {
    Map<Unit, Supply> result = supplyMap.get(item);
    if (result == null)
      return Collections.emptyList();
    return result.values();
  }

  public void sortByPriority() {
    for (String item : supplyMap.keySet()) {
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
        sort(sorted);

        itemSupply.clear();
        for (Supply supply : sorted) {
          itemSupply.put(supply.getUnit(), supply);
        }
      }
    }
  }

  private void sort(Supply[] sorted) {
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
  }

  public Supply put(String item, Unit unit, int amount, long serial) {
    Map<Unit, Supply> itemSupplyMap = supplyMap.get(item);
    if (itemSupplyMap == null) {
      itemSupplyMap = new LinkedHashMap<Unit, Supply>();
      supplyMap.put(item, itemSupplyMap);
    }
    Supply result = new Supply(unit, item, amount, serial);
    itemSupplyMap.put(unit, result);
    return result;
  }

  public void clear() {
    supplyMap.clear();
  }
}

interface ReserveVisitor {
  public void execute(Unit u, String item, int amount);
}

class Reserves {

  Map<String, Map<Unit, Integer>> reserves = new HashMap<String, Map<Unit, Integer>>();

  public void add(String item, Unit unit, int amount) {
    E3CommandParser.increaseMultiInv(reserves, item, unit, amount);
  }

  public void execute(ReserveVisitor reserveVisitor) {
    for (String item : reserves.keySet()) {
      Map<Unit, Integer> iMap = reserves.get(item);
      for (Unit u : iMap.keySet()) {
        reserveVisitor.execute(u, item, iMap.get(u));
      }

    }
  }
}

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
class E3CommandParser {

  /**
   * A standard soldier's endurance skill should be this fraction of his (first row) weapon skill
   */
  public static float ENDURANCERATIO_FRONT = .6f;
  /**
   * A standard soldier's endurance skill should be this fraction of his (second row) weapon skill
   */
  public static float ENDURANCERATIO_BACK = .35f;

  /** Unit limit, used to warn if we get too many units. */
  public static int UNIT_LIMIT = 250;

  /** If this is true, some more hints will be added to the orders if expected units are missing */
  public static boolean ADD_NOT_THERE_INFO = false;

  /**
   * If this is &gt; 0, all units are suppliers, otherwise suppliers must be set with Versorge (the
   * default)
   */
  public static int DEFAULT_SUPPLY_PRIORITY = 0;

  /** default need priority */
  private static final int DEFAULT_PRIORITY = 100;
  /** need priority for GibWenn command */
  private static final int GIVE_IF_PRIORITY = 999999;
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
  /** The "on foot" order */
  public static String FOOTOrder = "FUSS";
  /** The "on horse" order */
  public static String HORSEOrder = "PFERD";
  /** The "on ship" order */
  public static String SHIPOrder = "SCHIFF";
  /** The item "horses" */
  public static String HORSEItem = "Pferd";
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
  private Map<Faction, Integer> currentFactions = CollectionFactory.createOrderedMap();
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
  protected List<Need> needQueue;
  /**
   * The item/unit/supply map. Stores all available items.
   */
  protected SupplyMap supplyMapp;

  protected Map<Unit, Integer> capacities;

  private Map<String, Unit> dummyUnits;
  private Map<Unit, Map<String, Integer>> transfersMap;
  private List<Transfer> transferList;

  /**
   * Lines matching these patterns should be removed.
   */
  protected List<String> removedOrderPatterns;

  /**
   * Lists of allowed units for Erlaube/Ueberwache
   */
  protected Map<Faction, Set<UnitID>> allowedUnits = new HashMap<Faction, Set<UnitID>>();
  /**
   * Lists of required units for Verlange/Ueberwache
   */
  protected Map<Faction, Set<UnitID>> requiredUnits = new HashMap<Faction, Set<UnitID>>();

  /**
   * Current state for the Loesche command
   */
  protected String clear = null;
  /** Current prefix for the Loesche command */
  protected String clearPrefix = "";

  private int progress = -1;
  private long supplySerial = 0;
  private int showStats = 1;
  private String cachedScriptCommand;

  public int getShowStats() {
    return showStats;
  }

  public void setShowStats(int showStats) {
    this.showStats = showStats;
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands.
   *
   * @param faction scripts for all units of this faction
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void execute(Faction faction) {
    executeFrom(Collections.singleton(faction), null, null);
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands.
   *
   * @param factions scripts for all units of all factions in this set
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void execute(Collection<Faction> factions) {
    executeFrom(factions, null, null);
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to execute all unit commands.
   *
   * @param faction scripts for all units of this faction are executed
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
   * @param faction scripts for all units of this faction are executed
   * @param region only commands of unit in this region are executed, may be <code>null</code> to
   *          execute for all regions.
   * @param first First region to execute, may be <code>null</code> to not ignore any regions
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void executeFrom(Faction faction, Region region, Region first) {
    executeFrom(Collections.singleton(faction), region, first);
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Ignore regions before first
   * (in the report order).
   *
   * @param factions scripts for all units of all factions in this set are executed
   * @param region only commands of unit in this region are executed, may be <code>null</code> to
   *          execute for all regions.
   * @param first First region to execute, may be <code>null</code> to not ignore any regions
   * @throws NullPointerException if <code>faction == null</code>
   */
  public void executeFrom(Collection<Faction> factions, Region region, Region first) {
    if (factions == null || factions.isEmpty())
      throw new NullPointerException();

    currentFactions = CollectionFactory.createOrderedMap(1);
    for (Faction f : factions) {
      currentFactions.put(f, null);
    }

    helper.getUI().setMaximum(world.getRegions().size() + 4);
    helper.getUI().setProgress("init", ++progress);

    // comment out the following two lines if you don't have the newest nighthly build of Magellan
    helper.getUI().setProgress("preprocessing", ++progress);

    EresseaRelationFactory relationFactory = ((EresseaRelationFactory) world.getGameSpecificStuff()
        .getRelationFactory());
    relationFactory.stopUpdating();

    findSomeUnit(currentFactions);

    initLocales();

    for (Faction faction : factions) {
      if (faction.units().size() >= UNIT_LIMIT) {
        addWarning(someUnit, "Einheitenlimit erreicht (" + faction.units().size() + "/"
            + UNIT_LIMIT + ")! ");
      } else if (faction.units().size() * 1.1 > UNIT_LIMIT) {
        addWarning(someUnit, "Einheitenlimit fast erreicht (" + faction.units().size() + "/"
            + UNIT_LIMIT + ")! ");
      }
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

    relationFactory.restartUpdating();

    for (Faction faction : factions) {
      for (Unit u : faction.units()) {
        if ("1".equals(getProperty(u, "confirm"))) {
          u.setOrdersConfirmed(true);
        } else if ("0".equals(getProperty(u, "confirm"))) {
          u.setOrdersConfirmed(false);
        }
        notifyMagellan(u);
      }
    }
    // comment out the following line if you don't have the newest nightly build of Magellan
    helper.getUI().setProgress("ready", ++progress);
  }

  protected void execute(Region region) {
    try {
      currentRegion = region;
      initSupply();

      for (Unit u : region.units()) {
        if (currentFactions.containsKey(u.getFaction())) {

          setCurrentUnit(u);
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
      setCurrentUnit(null);

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

  protected void updateCurrentOrders() {
    if (isChangedOrders()) {
      currentUnit.setOrders(newOrders);
    }
    notifyMagellan(currentUnit);
  }

  protected void setCurrentUnit(Unit u) {
    if (u != null) {
      currentUnit = u;
      newOrders = new ArrayList<String>();
      removedOrderPatterns = new ArrayList<String>();
      setChangedOrders(false);
      error = -1;
    } else {
      currentUnit = null;
      newOrders = null;
    }
  }

  protected boolean isChangedOrders() {
    return changedOrders;
  }

  protected void setChangedOrders(boolean changedOrders) {
    this.changedOrders = changedOrders;
  }

  protected Unit findUnit(String target) {
    Unit targetUnit;
    if ((targetUnit = helper.getUnit(target)) == null) {
      targetUnit = dummyUnits.get(target);
      if (targetUnit == null) {
        dummyUnits.put(target, targetUnit = MagellanFactory.createUnit(UnitID.createUnitID(target,
            world.base),
            world));
      }
    }
    return targetUnit;
  }

  private void findSomeUnit(Map<Faction, Integer> factions) {
    // sometimes we need an arbitrary unit. This is a shorthand for it.
    for (Faction faction : factions.keySet())
      if (!faction.units().isEmpty()) {
        someUnit = faction.units().iterator().next();
        break;
      }

    if (someUnit == null)
      throw new RuntimeException("No units in report!");

  }

  protected boolean testUnit(String sOther, Unit other, Warning warning) {
    return testUnit(sOther, other, warning, false);
  }

  protected boolean testUnit(String sOther, Unit other, Warning w, boolean testFaction) {
    if (other == null || other.getRegion() != currentUnit.getRegion()) {
      if (w.contains(Warning.C_UNIT) && w.contains(Warning.C_HIDDEN)) {
        addNewWarning(sOther + " nicht da");
        return false;
      } else if (ADD_NOT_THERE_INFO) {
        addNewOrder("; " + sOther + " nicht da", true);
      }
      return !w.contains(Warning.C_HIDDEN);
    } else if (testFaction && w.contains(Warning.C_FOREIGN)
        && !currentFactions.containsKey(other.getFaction())) {
      addNewWarning("Einheit " + sOther + " gehört nicht zu uns");
    }

    return true;
  }

  /**
   * Tries to init the constants according to the current faction's locale
   */
  protected void initLocales() {
    if (currentUnit == null) {
      findSomeUnit(currentFactions);
    }
    if (someUnit == null)
      throw new NullPointerException();
    setCurrentUnit(someUnit);

    GIVEOrder = getLocalizedOrder(EresseaConstants.OC_GIVE, GIVEOrder).toString();
    RESERVEOrder = getLocalizedOrder(EresseaConstants.OC_RESERVE, RESERVEOrder).toString();
    EACHOrder = getLocalizedOrder(EresseaConstants.OC_EACH, EACHOrder).toString();
    ALLOrder = getLocalizedOrder(EresseaConstants.OC_ALL, ALLOrder).toString();
    PCOMMENTOrder = "//";
    LEARNOrder = getLocalizedOrder(EresseaConstants.OC_LEARN, LEARNOrder).toString();
    TEACHOrder = getLocalizedOrder(EresseaConstants.OC_TEACH, TEACHOrder).toString();
    ENTERTAINOrder = getLocalizedOrder(EresseaConstants.OC_ENTERTAIN, ENTERTAINOrder).toString();
    TAXOrder = getLocalizedOrder(EresseaConstants.OC_TAX, TAXOrder).toString();
    WORKOrder = getLocalizedOrder(EresseaConstants.OC_WORK, WORKOrder).toString();
    BUYOrder = getLocalizedOrder(EresseaConstants.OC_BUY, BUYOrder).toString();
    SELLOrder = getLocalizedOrder(EresseaConstants.OC_SELL, SELLOrder).toString();
    MAKEOrder = getLocalizedOrder(EresseaConstants.OC_MAKE, MAKEOrder).toString();
    MOVEOrder = getLocalizedOrder(EresseaConstants.OC_MOVE, MOVEOrder).toString();
    ROUTEOrder = getLocalizedOrder(EresseaConstants.OC_ROUTE, ROUTEOrder).toString();
    PAUSEOrder = getLocalizedOrder(EresseaConstants.OC_PAUSE, PAUSEOrder).toString();
    RESEARCHOrder = getLocalizedOrder(EresseaConstants.OC_RESEARCH, RESEARCHOrder).toString();
    RECRUITOrder = getLocalizedOrder(EresseaConstants.OC_RECRUIT, RECRUITOrder).toString();

    if (currentFactions.keySet().iterator().next().getLocale().getLanguage() != "de") {
      // warning constants
      Warning.W_NEVER = "never";
      Warning.W_SKILL = "skill";
      Warning.W_WEAPON = "weapon";
      Warning.W_SHIELD = "shield";
      Warning.W_ARMOR = "armor";
      BEST = "best";
      NULL = "null";
      LUXUSOrder = "LUXURY";
      KRAUTOrder = "HERBS";
    }
    setCurrentUnit(null);
  }

  protected String getLocalizedOrder(StringID orderKey, String fallback) {
    try {
      return world.getGameSpecificStuff().getOrderChanger().getOrderO(orderKey,
          currentUnit.getLocale()).getText();
    } catch (RulesException e) {
      return fallback;
    }
  }

  /**
   * Tries to translate the given order to the current locale.
   */
  protected String getLocalizedOrder(StringID orderKey, Object[] args) {
    return world.getGameSpecificStuff().getOrderChanger().getOrderO(currentUnit.getLocale(),
        orderKey, args).getText();
  }

  /**
   * Tries to translate the given order to the current locale.
   */
  protected String getLocalizedOrder(String orderKey, String fallBack) {
    String translation = Resources.getOrderTranslation(orderKey, currentUnit.getLocale());
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
    setChangedOrders(isChangedOrders() || changed);
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
   * @param hint
   */
  protected void addNewWarning(String hint) {
    addNewWarning(hint, true);
  }

  protected void addNewWarning(String hint, boolean addLine) {
    error = line;
    // errMsg = hint;
    addNewOrder(COMMENTOrder + " TODO: " + hint
        + (addLine ? " (Warnung in Zeile " + error + ")" : ""), true);
    setConfirm(currentUnit, false);
  }

  /**
   * Adds some statistic information to the orders of the first unit.
   *
   */
  protected void collectStats() {
    int buildingScripts = 0;
    int shipScripts = 0;
    int unitScripts = 0;
    int regionScripts = 0;
    // comment out the following lines if you don't have the newest nightly build of Magellan
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
        addOrder(u, COMMENTOrder + " hat Skript", false);
        unitScripts++;
      }
    }
    for (Region r : world.getRegions()) {
      if (helper.hasScript(r)) {
        regionScripts++;
      }
    }

    if (showStats > 0) {
      someUnit.addOrderAt(0, "; " + unitScripts + " unit scripts, " + buildingScripts
          + " building scripts, " + shipScripts + " ship scripts, " + regionScripts
          + " region scripts", true);
    }
  }

  /**
   * Parses the orders of the unit u for commands of the form "// $cript ..." and tries to execute
   * them. Known commands:<br />
   * <tt>// $cript +X text</tt> -- If X<=1 then a warning containing text is added to the unit's
   * orders. Otherwise X is decreased by one.<br />
   * <code>// $cript [rest [period [length]] text</code> -- Adds text (or commands) to the
   * orders<br />
   * <code>// $cript auto [NICHT]|[length [period]]</code> -- autoconfirm orders<br />
   * <code>// $cript Loeschen [$kurz] [<prefix>]</code> -- clears orders except comments<br />
   * <code>// $cript GibWenn receiver [[JE] amount|ALLES|KRAUT|LUXUS|TRANK] [item] [warning...]</code>
   * -- add give order (if possible)<br />
   * <code>// $cript Benoetige minAmount [maxAmount] item [priority]</code><br />
   * <code>// $cript Benoetige JE amount item [priority]</code><br />
   * <code>// $cript Benoetige ALLES [item] [priority]</code> -- acquire things from other
   * units<br />
   * <code>// $cript BenoetigeFremd unit [JE] minAmount [maxAmount] item [priority] [warning]</code><br
   * />
   * <code>// $cript Versorge [[item]...] priority</code> -- set supply priority.<br />
   * <code>// $cript BerufDepotVerwalter [Zusatzbetrag]</code> Collects all free items in the
   * region, Versorge 100, calls Ueberwache<br />
   * <code>// $cript Soldat [Talent [Waffe [Schild [Rüstung]]]] [nie|Talent|Waffe|Schild|Rüstung]</code>
   * -- learn skill and reserve equipment<br />
   * <code>// $cript Lerne Talent1 Stufe1 [[Talent2 Stufe2]...]</code> -- learn skills in given
   * ratio <br />
   * <code>// $cript BerufBotschafter [minimum money] [Talent]</code> -- earn money if necessary,
   * otherwise learn skill<br />
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
   * <code>// $cript Kommentar text</code> -- add ; comment<br />
   */
  protected void parseScripts() {
    // errMsg = null;
    line = 0;
    allowedUnits.clear();
    requiredUnits.clear();
    clear = null;

    // NOTE: must not change currentUnit's orders directly! Always change newOrders!
    Deque<String> queue = new LinkedList<String>();
    for (Order o : currentUnit.getOrders2()) {
      ++line;
      queue.add(o.getText());
      while (!queue.isEmpty()) {
        currentOrder = queue.poll();
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
          // as of Java 7 the first character of an integer may be a '+' sign
          boolean repeat = true;
          while (tokens != null && !tokens[0].startsWith("+") && repeat) {
            try {
              Integer.parseInt(tokens[0]);
              String[] nextOrders = commandRepeat(tokens);
              if (nextOrders == null) {
                currentOrder = null;
                tokens = null;
              } else {
                currentOrder = nextOrders.length > 0 ? nextOrders[0] : "";
                tokens = detectScriptCommand(currentOrder);
                if (tokens == null) {
                  addNewOrder(currentOrder, true);
                  currentOrder = null;
                }
                for (int i = 1; i < nextOrders.length; ++i) {
                  queue.add(nextOrders[i]);
                }
              }
            } catch (NumberFormatException e) {
              repeat = false;
              // not a repeating order
            }
          }
          if (tokens != null) {
            // System.out.println(o);
            String command = tokens[0];
            if (command.startsWith("+")) {
              commandWarning(tokens);
              setChangedOrders(true);
            } else if (command.equals("KrautKontrolle")) {
              commandControl(tokens);
              setChangedOrders(true);
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
              } else if (command.equals("Kapazitaet")) {
                commandCapacity(tokens);
              } else if (command.equals("BerufDepotVerwalter")) {
                commandDepot(tokens);
              } else if (command.equals("Soldat")) {
                commandSoldier(tokens);
              } else if (command.equals("Lerne")) {
                commandLearn(tokens);
              } else if (command.equals("BerufBotschafter")) {
                Collection<String> commands = commandEmbassador(tokens);
                Collections.reverse((List<?>) commands);
                for (String newOrder : commands) {
                  queue.addFirst(newOrder);
                }
                setChangedOrders(true);
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
                      String.valueOf(DEFAULT_PRIORITY + 10) });
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
              } else if (command.equals("Kommentar")) {
                commandComment(tokens);
              } else {
                addNewError("unbekannter Befehl: " + command);
              }
            }
            currentOrder = null;

          }
        }
      }
    }
    updateCurrentOrders();
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
        setChangedOrders(true);
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
   * rounds. <code>text</code> may contain '\n". If this is the case it is split into lines and the
   * lines are executed or inserted after the current command. For example the line<br />
   * "// $cript 1 10 MACHE TEMP a\nLERNE Hiebwaffen\n// $cript 2 GIB a 1 Silber\nENDE"<br />
   * will be replaced with<br />
   * "// $cript 10 10 MACHE TEMP a\nLERNE Hiebwaffen\n// $cript 2 GIB a 1 Silber\nENDE",<br />
   * "MACHE TEMP a",<br />
   * "LERNE Hiebwaffen",<br />
   * "// $cript 1 GIB a 1 Silber",<br />
   * "ENDE".
   *
   * @param tokens
   * @return <code>text</code>, if <code>rest==1</code>, otherwise <code>null</code>
   */
  protected String[] commandRepeat(String[] tokens) {
    ArrayList<String> lines = null;
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
        lines = new ArrayList<String>(1);
        StringBuilder result = new StringBuilder();
        if (period > 0) {
          rest = period + 1;
        }
        if (textIndex < tokens.length && tokens[textIndex].equals(scriptMarker)) {
          result.append(COMMENTOrder).append(" ");
        }
        for (int tokenIndex = textIndex; tokenIndex < tokens.length; ++tokenIndex) {
          if (tokenIndex > textIndex) {
            result.append(" ");
          }
          String[] subTokens = tokens[tokenIndex].split("\\\\n");
          result.append(subTokens[0]);
          for (int j = 1; j < subTokens.length; ++j) {
            lines.add(result.toString());
            result.setLength(0);
            result.append(subTokens[j]);
          }
          if (tokenIndex + 1 >= tokens.length) {
            lines.add(result.toString());
          }
        }
      } else if (length == 0) {
        // return empty string to signal success
        lines = new ArrayList<String>(1);
        lines.add("");
      }
      if ((rest > 1 || period > 0) && length > 0) {
        StringBuilder newOrder = new StringBuilder();
        newOrder.append(createScriptCommand()).append(rest - 1);
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
    }

    return lines != null ? lines.toArray(new String[0]) : null;
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
        newOrder.append(createScriptCommand()).append(tokens[0]);
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

  private String createScriptCommand() {
    if (cachedScriptCommand == null) {
      cachedScriptCommand = PCOMMENTOrder + " " + scriptMarker + " ";
    }
    return cachedScriptCommand;
  }

  /**
   * <code>// $cript GibWenn receiver [[JE] amount|ALLES|KRAUT|LUXUS|TRANK] [item] [warning...]</code>
   * <br />
   * Adds a GIB order to the unit. Warning may be one of "immer" (the default), "Menge", "Einheit",
   * "nie".
   */
  protected void commandGiveIf(String[] tokens) {
    Warning w = new Warning(true);
    tokens = w.parse(tokens);

    if (tokens.length < 3 || tokens.length > 5) {
      addNewError("falsche Anzahl Argumente");
      return;
    }

    Unit target = helper.getUnit(tokens[1]);
    String targetId = tokens[1];

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

    if (!testUnit(targetId, target, w))
      return;

    // handle GIB xyz ALLES
    if (ALLOrder.equalsIgnoreCase(tokens[2])) {
      if (tokens.length == 3) {
        giveAll(targetId, w, null, null);
        return;
      }
    }

    if (KRAUTOrder.equalsIgnoreCase(tokens[2]) || LUXUSOrder.equalsIgnoreCase(tokens[2])
        || TRANKOrder.equalsIgnoreCase(tokens[2])) {
      // handle GIB xyz KRAUT
      if (KRAUTOrder.equalsIgnoreCase(tokens[2])) {
        if (world.getRules().getItemCategory("herbs") == null) {
          addNewError("Spiel kennt keine Kräuter");
        } else {
          giveAll(targetId, w, null, world.getRules().getItemCategory("herbs"));
        }
      }

      // handle GIB xyz LUXUS
      if (LUXUSOrder.equalsIgnoreCase(tokens[2])) {
        if (world.getRules().getItemCategory("luxuries") == null) {
          addNewError("Spiel kennt keine Luxusgüter");
        } else {
          giveAll(targetId, w, null, world.getRules().getItemCategory("luxuries"));
        }
      }

      // handle GIB xyz TRANK
      if (TRANKOrder.equalsIgnoreCase(tokens[2])) {
        if (world.getRules().getItemCategory("potions") == null) {
          addNewError("Spiel kennt keine Tränke");
        } else {
          giveAll(targetId, w, null, world.getRules().getItemCategory("potions"));
        }
      }

      return;
    }

    if (tokens.length != 4 + je) {
      addNewError("zu viele Parameter");
      return;
    }

    // get amount
    final String item = tokens[3 + je];
    int amount = 0;
    if (ALLOrder.equalsIgnoreCase(tokens[2 + je])) {
      if (KRAUTOrder.equals(item) || LUXUSOrder.equals(item)) {
        addNewError("GIB xyz ALLES " + item + " statt GIB xyz " + item);
      }
      giveAll(targetId, w, item, null);
      return;
    } else {
      try {
        amount = Integer.parseInt(tokens[2 + je]);
      } catch (NumberFormatException e) {
        amount = 0;
        addNewError("Zahl oder ALLES erwartet");
        return;
      }
    }

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
      if (w.contains(Warning.C_AMOUNT)) {
        addNewWarning("zu wenig " + item);
      } else {
        addNewMessage("zu wenig " + item);
      }
      fullAmount = getItemCount(currentUnit, item);
      je = 0;
    }

    // make GIVE order
    if (fullAmount > 0) {
      giveTransfer(targetId, item, fullAmount, false);
    }
  }

  private void giveAll(String targetId, Warning w, String filterItem, ItemCategory filterCategory) {
    for (Item item : getUnitItems(currentUnit, filterItem, filterCategory)) {
      String itemName = item.getOrderName();
      int amount = getSupply(itemName, currentUnit).getAmount();
      giveTransfer(targetId, itemName, amount, true);
    }
  }

  private Collection<Item> getUnitItems(Unit unit, String filterItem, ItemCategory filterCategory) {
    Collection<Item> items = new ArrayList<Item>();
    for (Item item : unit.getItems()) {
      if ((filterItem != null && filterItem.equals(
          item.getOrderName()))
          ||
          (filterCategory != null && filterCategory.equals(
              item.getItemType().getCategory()))
          || (filterItem == null && filterCategory == null)) {
        items.add(item);
      }
    }
    return items;
  }

  /**
   * <code>// $cript Benoetige minAmount [maxAmount] item [priority]</code><br />
   * <code>// $cript Benoetige JE amount item [priority]</code><br />
   * <code>// $cript Benoetige FUSS|PFERD Pferd [priority]</code><br />
   * <code>// $cript Benoetige ALLES [item] [priority]</code><br />
   * Tries to transfer the maxAmount of item from other units to this unit. Issues warning if
   * minAmount cannot be supplied. <code>Benoetige JE</code> tries to reserve amount of item for
   * every person in the unit. Fractional amounts are possible and rounded up.
   * <code>Benoetige ALLES item</code> is equivalent to <code>Benoetige 0 infinity item</code>,
   * <code>Benoetige ALLES</code> is equivalent to <code>Benoetige ALLES</code> for every itemtype
   * in the region.<br/>
   * <code>Benoetige KRAUT</code> is the same for every herb type in the region.<br/>
   * <code>BenoetigeFremd unit (JE amount)|(minAmount [maxAmount]) item [priority] [warning...]</code>
   * <br />
   * <code>BenoetigeFremd</code> does the same, but for the given unit instead of the current unit.
   * Needs with higher priority are satisfied first. If no priority is given,
   * {@link #DEFAULT_PRIORITY} is used.
   */
  protected void commandNeed(String[] tokens) {
    Unit unit = currentUnit;
    Warning w = new Warning(true);

    String sOther = "???";
    if (tokens[0].equals("BenoetigeFremd")) {
      sOther = tokens[1];
      unit = helper.getUnit(tokens[1]);

      // erase unit token for easier processing afterwards
      tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
      tokens[0] = "BenoetigeFremd";

      // only benoetigefremd can have warnings!
      tokens = w.parse(tokens);
      // foreign implies amount
      if (w.contains(Warning.C_FOREIGN)) { // FIXME test
        w.add(Warning.W_AMOUNT);
      }
    }

    int tokenCount = tokens.length;

    int priority;
    try {
      priority = Integer.parseInt(tokens[tokenCount - 1]);
      tokenCount--;
    } catch (NumberFormatException e) {
      priority = DEFAULT_PRIORITY;
    }

    tokens = Arrays.copyOf(tokens, tokenCount);

    if (tokens.length < 2 || tokens.length > 5) {
      addNewError("falsche Anzahl Argumente");
      return;
    }

    if (!testUnit(sOther, unit, w, true))
      return;
    if (unit == null) {
      if (sOther == null) {
        addNewError("need unit token");
        return;
      }
      unit = findUnit(sOther);
    }
    try {
      if (ALLOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNeed(tokens[2], unit, 0, Integer.MAX_VALUE, priority, w);
        } else {
          for (String item : supplyMapp.items()) {
            addNeed(item, unit, 0, Integer.MAX_VALUE, priority, w);
          }
        }
      } else if (EACHOrder.equals(tokens[1])) {
        if (tokens.length != 4) {
          addNewError("ungültige Argumente für Benoetige JE x Ding");
        } else {
          if (unit.getPersons() <= 0)
            if (w.contains(Warning.C_AMOUNT)) {
              addNewWarning("Benoetige JE für leere Einheit");
            } else {
              addNewMessage("Benoetige JE für leere Einheit");
            }
          int amount = (int) Math.ceil(unit.getPersons() * Double.parseDouble(tokens[2]));
          String item = tokens[3];
          addNeed(item, unit, amount, amount, priority, w);
        }
      } else if (KRAUTOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNewError("zu viele Parameter");
        }
        if (world.getRules().getItemCategory("herbs") == null) {
          addNewError("Spiel kennt keine Kräuter");
        } else {
          for (ItemType itemType : world.getRules().getItemTypes()) { // currentUnit.getItems()
            if (world.getRules().getItemCategory("herbs").equals(itemType.getCategory())) {
              addNeed(itemType.getOrderName(), unit, 0, Integer.MAX_VALUE, priority, w);
            }
          }
        }
      } else if (LUXUSOrder.equals(tokens[1])) {
        if (tokens.length > 2) {
          addNewError("zu viele Parameter");
        }
        if (world.getRules().getItemCategory("luxuries") == null) {
          addNewError("Spiel kennt keine Luxusgüter");
        } else {
          for (ItemType itemType : world.getRules().getItemTypes()) { // currentUnit.getItems()
            if (world.getRules().getItemCategory("luxuries").equals(itemType.getCategory())) {
              addNeed(itemType.getOrderName(), unit, 0, Integer.MAX_VALUE, priority, w);
            }
          }
        }
      } else if (tokens.length > 4 || tokens.length < 3) {
        addNewError("falsche Anzahl Argumente");
      } else {
        String item = tokens[tokens.length - 1];
        int minAmount = getAmountWithHorse(unit, tokens[1], item);
        int maxAmount = tokens.length == 3 ? minAmount : getAmountWithHorse(unit, tokens[2], item);
        addNeed(item, unit, minAmount, maxAmount, priority, w);
      }
    } catch (NumberFormatException exc) {
      addNewError("Ungültige Zahl in Benoetige: " + exc.getMessage());
    }
  }

  private int getAmountWithHorse(Unit unit, String amount, String item) {
    if (isHorse(item))
      if (HORSEOrder.equals(amount))
        return world.getGameSpecificRules().getMaxHorsesRiding(unit);
      else if (FOOTOrder.equals(amount))
        return world.getGameSpecificRules().getMaxHorsesWalking(unit);
    return Integer.parseInt(amount);
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
      if (currentFactions.containsKey(u.getFaction())) {
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
    addNeed("Silber", currentUnit, costs + zusatz1, costs + zusatz1 + zusatz2,
        DEPOT_SILVER_PRIORITY);

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
          supply.setPriority(priority);
        }
      }
    } else {
      for (int i = 1; i < tokens.length - 1; ++i) {
        setSupplyPriority(currentUnit, tokens[i], priority);
      }
    }
  }

  private void setSupplyPriority(Unit unit, String itemToken, int priority) {
    Collection<Item> items = null;
    if (KRAUTOrder.equalsIgnoreCase(itemToken)) {
      items = getCategoryItems(unit, "herbs", "Kräuter");
    } else if (LUXUSOrder.equalsIgnoreCase(itemToken)) {
      items = getCategoryItems(unit, "luxury", "Luxusgüter");
    } else if (TRANKOrder.equalsIgnoreCase(itemToken)) {
      items = getCategoryItems(unit, "potion", "Tränke");
    } else {
      items = getUnitItems(unit, itemToken, null);
    }

    if (items != null) {
      for (Item item : items) {
        Supply supply = getSupply(item.getOrderName(), currentUnit);
        if (supply != null) {
          supply.setPriority(priority);
        }
      }
    }
  }

  private Collection<Item> getCategoryItems(Unit unit, String category, String categoryName) {
    ItemCategory itemCategory = world.getRules().getItemCategory(StringID.create(category));
    if (itemCategory == null) {
      addNewError("Spiel kennt keine " + categoryName);
      return Collections.emptyList();
    }
    return getUnitItems(unit, null, itemCategory);
  }

  /**
   * <code>Kapazitaet FUSS|PFERD|SCHIFF|amount</code> -- ensure that a unit's capacity is not
   * exceeded
   */
  protected void commandCapacity(String[] tokens) {
    int capacity = 0;
    int slack = 0;
    if (tokens.length < 2) {
      addNewError("zu wenig Argumente");
      return;
    } else if (tokens.length > 2) {
      if (tokens.length == 4 && "-".equals(tokens[2])) {
        try {
          slack = Integer.parseInt(tokens[3]);
        } catch (NumberFormatException e) {
          addNewError("Zahl erwartet");
        }
      } else {
        addNewError("zu viele Argumente");
      }
    }
    MovementEvaluator movement = world.getGameSpecificStuff().getMovementEvaluator();
    int load = movement.getModifiedLoad(currentUnit);
    try {
      capacity = Integer.parseInt(tokens[1]);
    } catch (NumberFormatException e) {
      if (HORSEOrder.equals(tokens[1])) {
        capacity = movement.getPayloadOnHorse(currentUnit);
      } else if (FOOTOrder.equals(tokens[1])) {
        capacity = movement.getPayloadOnFoot(currentUnit);
      } else if (SHIPOrder.equals(tokens[1])) {
        Ship ship = currentUnit.getModifiedShip();
        if (ship == null || ship.getOwnerUnit() != currentUnit) {
          capacity = Integer.MAX_VALUE;
          addNewWarning("Einheit ist nicht Kapitän.");
        } else {
          capacity = ship.getMaxCapacity() - ship.getModifiedLoad() + load;
        }
      } else {
        addNewError("Zahl erwartet");
        return;
      }
      if (isCapacityError(capacity)) {
        addNewWarning("Zu viele Pferde.");
        capacity = load;
      }
    }

    // int load = movement.getModifiedLoad(currentUnit);
    capacities.put(currentUnit, capacity - slack - load);
  }

  private boolean isCapacityError(int capacity) {
    return capacity == MovementEvaluator.CAP_NO_HORSES
        || capacity == MovementEvaluator.CAP_UNSKILLED;
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
      addNewWarning(warning.toString(), false);
    }
    if (delay != 1) {
      StringBuilder newCommand = new StringBuilder(createScriptCommand()).append("+");
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
          SkillType skill = world.getRules().getSkillType(tokens[i++]);
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
    if (!(Warning.W_NEVER.equals(warning) || Warning.W_SKILL.equals(warning) || Warning.W_WEAPON
        .equals(warning)
        || Warning.W_SHIELD.equals(warning) || Warning.W_ARMOR.equals(warning))) {
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
      warning = Warning.W_WEAPON;
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
   * <code>// $cript BerufBotschafter [minimum money] [Talent|command]</code> -- if we have at least
   * minimum money (default 100), learn skill or execute command<br />
   *
   */
  protected Collection<String> commandEmbassador(String[] tokens) {
    Skill skill = null;
    int minimum = 100;
    int actionToken = 1;
    if (tokens.length > 1) {
      try {
        minimum = Integer.parseInt(tokens[1]);
        actionToken = 2;
      } catch (NumberFormatException e) {
        actionToken = 1;
      }
    }
    if (tokens.length > actionToken) {
      skill = getSkill(tokens[actionToken], 10);
    } else {
      skill = getSkill(EresseaConstants.S_WAHRNEHMUNG.toString(), 10);
      if (skill == null) {
        skill = getSkill(EresseaConstants.S_AUSDAUER.toString(), 10);
      }
    }

    commandClear(new String[] { "Loeschen" });

    if (helper.getSilver(currentUnit) < minimum) {
      if (hasEntertain() && currentUnit.getSkill(EresseaConstants.S_UNTERHALTUNG) != null
          && currentUnit.getSkill(EresseaConstants.S_UNTERHALTUNG).getLevel() > 0) {
        addNewOrder(ENTERTAINOrder, true);
      } else if (hasTax() && currentUnit.getSkill(EresseaConstants.S_STEUEREINTREIBEN) != null
          && currentUnit.getSkill(EresseaConstants.S_STEUEREINTREIBEN).getLevel() > 0) {
        addNewOrder(TAXOrder, true);
        for (Unit guard : currentRegion.getGuards()) {
          if (!Units.isAllied(guard.getFaction(), currentUnit.getFaction(),
              EresseaConstants.A_GUARD)) {
            addNewWarning("Region wird bewacht");
            break;
          }
        }
      } else if (hasWork()) {
        addNewOrder(WORKOrder, true);
      } else {
        addNewWarning("Einheit verhungert");
      }
    } else if (skill == null) {
      // other order
      StringBuilder order = new StringBuilder(createScriptCommand()).append("1 ");
      if (tokens.length > actionToken) {
        order.append(tokens[actionToken]);
      }
      for (int i = actionToken + 1; i < tokens.length; ++i) {
        order.append(" ").append(tokens[i]);
      }
      return Collections.singletonList(order.toString());
      // addNewOrder(order.toString(), true);
    } else {
      learn(currentUnit, Collections.singleton(skill));
      if (tokens.length > actionToken + 1) {
        addNewError("zu viele Argumente");
      }
    }
    return Collections.emptyList();
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
            .contains(u.getID()) || allowedUnits.get(u.getFaction()).contains(
                currentRegion.getZeroUnit().getID())))) {
          if (!(requiredUnits.containsKey(u.getFaction()) && requiredUnits.get(u.getFaction())
              .contains(u.getID()))) {
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
        } else {
          sb.append(" ...");
          break;
        }
      }
      entry.getValue().clear();
      addNewWarning(sb.toString());
    }

    // check if required units are present
    for (Faction f : requiredUnits.keySet()) {
      for (UnitID id : requiredUnits.get(f)) {
        Unit u = world.getUnit(id);
        if (u == null || u.getRegion() != currentUnit.getRegion()) {
          addNewWarning("Einheit " + id + " der Partei " + f + " nicht mehr da.");
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
    Map<Faction, Set<UnitID>> map;
    if (tokens[0].equals("Erlaube")) {
      map = allowedUnits;
    } else {
      map = requiredUnits;
    }
    if (faction == null) {
      addNewError("unbekannte Partei");
    } else {
      Set<UnitID> set = map.get(faction);
      if (set == null) {
        set = new HashSet<UnitID>();
        map.put(faction, set);
      }
      if (ALLOrder.equals(tokens[2])) {
        set.add(currentRegion.getZeroUnit().getID());
        if (tokens.length > 3) {
          addNewError("zu viele Argumente");
        }
      } else {
        for (int i = 2; i < tokens.length; ++i) {
          set.add(UnitID.createUnitID(tokens[i], world.base));
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
        currentUnit
            .getModifiedSkill(world.getRules().getSkillType(EresseaConstants.S_UNTERHALTUNG));
    Skill taxing =
        currentUnit.getModifiedSkill(world.getRules().getSkillType(
            EresseaConstants.S_STEUEREINTREIBEN));
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
      if (tax2 * 2 > tax * 3) {
        addNewWarning("Treiber unterbeschäftigt " + tax2 + ">" + tax);
        entertain = currentRegion.maxEntertain();
      }
    } else if (entertain > 0) {
      addNewOrder(ENTERTAINOrder + " " + (amount > 0 ? amount : "") + COMMENTOrder + " "
          + entertain + ">" + tax, true);
      if (amount > currentRegion.maxEntertain() * 1.1) {
        addNewWarning("zu viele Arbeiter");
      } else if (entertain2 * 2 > entertain * 3) {
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
   * <code>// $cript Handel Menge|xM [xR] [ALLES | Verkaufsgut...] Warnung</code>: trade luxuries,
   * Versorge {@value #DEFAULT_EARN_PRIORITY}. Menge M may be a multipler ("x2" of the region
   * maximum). xR: reserve goods for R rounds. Warnung can be "Talent", "Menge", or "nie"<br />
   */
  protected void commandTrade(String[] tokens) {
    if (tokens.length < 2) {
      addNewError("zu wenige Argumente");
      return;
    }

    commandSupply(new String[] { "Versorge", String.valueOf(DEFAULT_EARN_PRIORITY) });

    Warning warning = new Warning(true);
    tokens = warning.parse(tokens);

    int volume = currentRegion.maxLuxuries();

    int buyAmount = -1;
    try {
      if (tokens[1].substring(0, 1).equalsIgnoreCase("x")) {
        buyAmount = Integer.parseInt(tokens[1].substring(1));
        buyAmount = volume * buyAmount;
      } else {
        buyAmount = Integer.parseInt(tokens[1]);
      }
    } catch (NumberFormatException e) {
      addNewError("ungültige Zahl " + tokens[1]);
      return;
    }

    Skill buySkill =
        currentUnit.getModifiedSkill(world.getRules().getSkillType(EresseaConstants.S_HANDELN));
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

    removeOrdersLike(SELLOrder + ".*", true);
    removeOrdersLike(BUYOrder + ".*", true);

    // Gueterzahl intitialisieren
    int totalVolume = 0;

    if (buyAmount > 0 && (volume <= 0 || buyGood == null)) {
      addNewError("Kein Handel möglich");
    }

    LinkedList<String> orders = new LinkedList<String>();

    int maxAmount = buySkill.getLevel() * currentUnit.getPersons() * 10;
    int skillNeeded = 0;

    if (volume > 0 && buyGood != null) {
      int reserveMultiplier = 1, reserveToken = 0;
      try {
        if (tokens.length > 2 && tokens[2].substring(0, 1).equalsIgnoreCase("x")) {
          reserveToken = 1;
          reserveMultiplier = Integer.parseInt(tokens[2].substring(1));
        }
      } catch (NumberFormatException e) {
        addNewError("ungültige Zahl " + tokens[2]);
        return;
      }

      List<String> goods = new LinkedList<String>();
      // Verkaufsbefehl setzen, wenn notwendig
      if (tokens.length > 2 + reserveToken && ALLOrder.equals(tokens[2 + reserveToken])) {
        if (world.getRules().getItemCategory("luxuries") == null) {
          addNewError("Spiel kennt keine Luxusgüter");
        } else {
          for (ItemType luxury : world.getRules().getItemTypes()) {
            if (!luxury.equals(buyGood.getItemType())
                && world.getRules().getItemCategory("luxuries").equals(luxury.getCategory())) {
              goods.add(luxury.getOrderName());
            }
          }
        }
      } else {
        for (int i = 2 + reserveToken; i < tokens.length; ++i) {
          goods.add(tokens[i]);
        }
      }

      for (String luxury : goods) {
        int goodAmount = volume;
        if (goodAmount > getSupply(luxury)) {
          goodAmount = getSupply(luxury);
        }
        skillNeeded += goodAmount;

        if (goodAmount > maxAmount - totalVolume) {
          goodAmount = maxAmount - totalVolume;
        }

        addNeed(luxury, currentUnit, ALLOrder.equals(tokens[2 + reserveToken]) ? goodAmount
            : volume, volume
                * reserveMultiplier,
            TRADE_PRIORITY, warning);
        if (goodAmount > 0) {

          if (goodAmount == volume) {
            orders.add(SELLOrder + " " + ALLOrder + " " + luxury);
          } else {
            orders.add(SELLOrder + " " + goodAmount + " " + luxury);
          }
        }
        totalVolume += goodAmount;
      }

    }

    // Soll eingekauft werden?
    if (volume > 0 && buyAmount > 0 && buyGood != null) {
      int remainingFromSkill = maxAmount - totalVolume + 1 - 1;
      // Berechne noetige Geldmenge fuer Einkauf (einfacher: Modulorechnung, aber wegen
      // Rundungsfehler nicht umsetzbar)
      int remainingToBuy = buyAmount;
      skillNeeded += buyAmount;
      if (remainingToBuy > remainingFromSkill) {
        buyAmount += (remainingFromSkill - remainingToBuy);
        remainingToBuy = remainingFromSkill;
      }
      int priceFactor = 1;
      int money = 0;
      while (remainingToBuy > 0) {
        if (remainingToBuy > volume) {
          remainingToBuy -= volume;
          money -= (volume * priceFactor * buyGood.getPrice()); // price is negative
          priceFactor++;
        } else {
          money -= (remainingToBuy * priceFactor * buyGood.getPrice());
          remainingToBuy = 0;
        }
      }

      addNeed("Silber", currentUnit, money, money, TRADE_PRIORITY);

      // Einkaufsbefehl setzen, wenn notwendig
      if (buyAmount > 0) {
        orders.addFirst(BUYOrder + " " + buyAmount + " " + buyGood.getItemType().getOrderName());
        totalVolume += buyAmount;
      }
    }

    // add orders (buy orders first)
    for (String order : orders) {
      addNewOrder(order, true);
    }

    // Einheit gut genug?
    if (skillNeeded > maxAmount && warning.contains(Warning.C_SKILL)) {
      addNewError("Einheit hat zu wenig Handelstalent (min: " + maxAmount / 10 + " < "
          + (int) Math.ceil(skillNeeded / 10.0) + ")");
    }

    setConfirm(currentUnit, true);
  }

  /**
   * <code>// $cript Quartiermeister [[Menge1 Gut 1]...]</code>: learn perception, allow listed
   * amount of goods. If other goods are detected, do not confirm orders.
   */
  protected void commandQuartermaster(String[] tokens) {
    learn(currentUnit, Collections.singleton(new Skill(world.getRules().getSkillType(
        EresseaConstants.S_WAHRNEHMUNG), 30, 10, 1, true)));
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
        && (world.getDate().getDate() % modulo == 0 || currentRegion.getHerbAmount() == null
            || (!currentRegion
                .getHerbAmount().equals("viele") && !currentRegion.getHerbAmount().equals(
                    "sehr viele")))) {
      addNewOrder(getResearchOrder(), true);
    } else {
      addNewOrder(MAKEOrder + " " + getLocalizedOrder(EresseaConstants.OC_HERBS, "KRÄUTER"), true);
    }
    commandSupply(new String[] { "Versorge", KRAUTOrder, "100" });
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
    newOrder.append(createScriptCommand()).append(tokens[0]);
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
      addNewWarning("Rekrutierung fertig");
      amount = Math.min(max - currentUnit.getPersons(), amount);
    }

    if (amount < min) {
      addNewError("Nicht genug Rekruten");
    }

    if (amount > 0) {
      if (effRace.getRecruitmentCosts() > 0) {
        int costs = amount * effRace.getRecruitmentCosts();
        int maxCosts =
            max == Integer.MAX_VALUE ? costs : (max - currentUnit.getPersons())
                * effRace.getRecruitmentCosts();
        addNeed("Silber", currentUnit, costs, maxCosts, DEFAULT_PRIORITY);
      } else {
        addNewWarning("Rekrutierungskosten unbekannt");
      }
      getRecruitOrder(amount, effRace);
      addNewOrder(getRecruitOrder(amount, effRace != currentUnit.getRace() ? effRace : null), true);
    }
  }

  /**
   * <code>// $cript Kommentar text</code>: add text after a semicolon
   */
  protected void commandComment(String[] tokens) {
    String rest = currentOrder.substring(currentOrder.indexOf(tokens[0]) + tokens[0].length());
    addNewOrder(";" + rest, true);

  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  protected boolean hasEntertain() {
    return world.getGameSpecificStuff().getOrderChanger().isLongOrder(
        getLocalizedOrder(EresseaConstants.OC_ENTERTAIN, ENTERTAINOrder));
  }

  protected boolean hasTax() {
    return world.getGameSpecificStuff().getOrderChanger().isLongOrder(
        getLocalizedOrder(EresseaConstants.OC_TAX, TAXOrder));
  }

  protected boolean hasWork() {
    return world.getGameSpecificStuff().getOrderChanger().isLongOrder(
        getLocalizedOrder(EresseaConstants.OC_WORK, WORKOrder));
  }

  protected void initSupply() {
    if (needQueue == null) {
      needQueue = new ArrayList<Need>();
    } else {
      needQueue.clear();
    }

    if (supplyMapp == null) {
      supplyMapp = new SupplyMap(this);
    } else {
      supplyMapp.clear();
    }
    if (capacities == null) {
      capacities = new HashMap<Unit, Integer>();
    } else {
      capacities.clear();
    }
    if (transfersMap == null) {
      transfersMap = new HashMap<Unit, Map<String, Integer>>();
      transferList = new ArrayList<Transfer>();
    } else {
      transfersMap.clear();
      transferList.clear();
    }
    if (dummyUnits == null) {
      dummyUnits = new HashMap<String, Unit>();
    } else {
      dummyUnits.clear();
    }

    for (Unit u : currentRegion.units()) {
      if (currentFactions.containsKey(u.getFaction())) {
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
    return supplyMapp.put(item, unit, amount, ++supplySerial);
  }

  /**
   * Adds a supply to the supplyMap
   *
   * @param item
   * @param unit
   * @param amount
   */
  protected Supply putDummySupply(String item, Unit unit, int amount) {
    return supplyMapp.put(item, unit, amount, Long.MAX_VALUE);
  }

  /**
   * Tries to satisfy all needs in the current needMap by adding GIVE orders to suppliers.
   */
  protected void satisfyNeeds() {
    supplyMapp.sortByPriority();

    Need[] needs = needQueue.toArray(new Need[] {});
    sort(needs);
    adjustAlreadyTransferred(needs);
    Reserves reserves = new Reserves();

    pack(needs, reserves, false);
    enforceCapacities();
    pack(needs, reserves, true);

    executeReserves(reserves);
    executeTransferOrders();
    warnNeeds();
    warnCapacities();
  }

  private void pack(Need[] needs, Reserves reserves, boolean enforce) {
    for (int n = 0, prioStart = 0, state = 0; n < needs.length; ++n) {
      Need need = needs[n];
      if (need.getPriority() > needs[prioStart].getPriority())
        throw new RuntimeException("needs not sorted");

      {
        if (state == 0) {
          reserveNeed(need, true, reserves);
        } else {
          giveNeed(need, true);
        }

        if (need.getAmount() != Integer.MAX_VALUE) {
          if (state == 0) {
            reserveNeed(need, false, reserves);
          } else {
            giveNeed(need, false);
          }
        } else {
          // now, finally, satisfy infinite needs
          if (state == 0) {
            reserveNeed(need, false, reserves);
          } else {
            giveNeed(need, false);
          }
        }
      }

      if (n == needs.length - 1 || needs[n + 1].getPriority() < needs[prioStart].getPriority()) {
        if (state == 0) {
          n = prioStart - 1;
          ++state;
        } else {
          state = 0;
          prioStart = n + 1;
          if (enforce) {
            enforceCapacities();
          }
        }
      }
    }
  }

  private void warnNeeds() {
    for (Need need : needQueue) {
      if (need.getMinAmount() > 0 && need.getWarning().contains(Warning.C_AMOUNT)) {
        addWarning(need.getUnit(), "braucht " + need.getMinAmount()
            + (need.getMaxAmount() != need.getMinAmount() ? ("/" + need.getMaxAmount()) : "")
            + " mehr " + need.getItem() + ", " + need.getMessage());
      }

      // add messages for unsatisfied needs
      if (need.getMinAmount() <= 0 && need.getMaxAmount() > 0
          && need.getMaxAmount() != Integer.MAX_VALUE) {
        addOrder(need.getUnit(), "; braucht " + need.getMaxAmount() + " mehr " + need.getItem()
            + ", "
            + need.getMessage(),
            false);
      }
    }
  }

  private void warnCapacities() {
    for (Entry<Unit, Integer> cap : capacities.entrySet()) {
      if (cap.getValue() < 0) {
        addWarning(cap.getKey(), "Kapazität überschritten um " + (-cap.getValue()));
      }
    }
  }

  private void sort(Need[] sorted) {
    for (int j = 1; j < sorted.length; ++j) {
      for (int i = 0; i < j; ++i) {
        if (sorted[j].compareTo(sorted[i]) < 0) {
          Need temp = sorted[i];
          sorted[i] = sorted[j];
          sorted[j] = temp;
        }
      }
    }
  }

  private void adjustAlreadyTransferred(Need[] needs) {
    for (Need need : needs) {
      adjustForTransfer(need);
    }
  }

  private void enforceCapacities() {
    for (int i = transferList.size() - 1; i >= 0; --i) {
      Transfer transfer = transferList.get(i);
      Integer cap;
      if ((cap = capacities.get(transfer.getTarget())) != null && cap < 0) {
        if (transfer.getUnit() != transfer.getTarget()) { // && transfer.isMin()
          int weight = getWeight(transfer.getItem());
          if (weight > 0) {
            int delta = cap / weight;
            if (delta * weight > cap) {
              --delta;
            }
            delta = Math.max(-transfer.getAmount(), delta);
            if (delta < 0) {
              undoTransfer(i, delta);
              i = transferList.size();
            }
          }
        }
      }
    }
  }

  private void undoTransfer(int index, int delta) {
    Transfer transfer = transferList.get(index);
    if (-delta < transfer.getAmount()) {
      transfer.reduceAmount(-delta);
    } else {
      transferList.remove(index);
    }
    if (-delta > getMulti(transfersMap, transfer.getTarget(), transfer.getItem())) {
      addNewError("invalid transfer");
    }
    increaseMulti(transfersMap, transfer.getTarget(), transfer.getItem(), delta);
    changeCapacity(transfer.getUnit(), transfer.getTarget(), transfer.getItem(), delta);

    getSupply(transfer.getItem(), transfer.getUnit()).reduceAmount(delta);
    if (transfer.isMin()) {
      transfer.getNeed().reduceMinAmount(delta);
    }
    transfer.getNeed().reduceMaxAmount(delta);

  }

  private void addTransfer(Unit giver, Unit receiver, String item, int amount, boolean min,
      boolean all,
      Need need) {
    transferList.add(new Transfer(giver, receiver, item, amount, min, all, need, ++supplySerial));
    increaseMulti(transfersMap, receiver, item, amount);
  }

  private void executeTransferOrders() {
    for (Transfer t : transferList) {
      if (t.getUnit() != t.getTarget()) {
        addOrder(t.getUnit(),
            getGiveOrder(t.getUnit(), t.getTarget().getID().toString(), t.getItem(),
                (t.isAll() ? Integer.MAX_VALUE : t.getAmount()), false)
                + COMMENTOrder + t.getMessage(), false);
      }
    }
  }

  private void transfer(Unit unit, Need need, int amount) {
    Supply supply = getSupply(need.getItem(), unit);
    if (supply.getAmount() < amount) {
      if (currentUnit == null) {
        addWarning(supply.getUnit(), "not enough " + need.getItem());
      } else {
        addNewWarning("not enough " + need.getItem() + " for " + supply.getUnit());
      }
    }

    need.reduceMaxAmount(amount);
    need.reduceMinAmount(amount);
    supply.reduceAmount(amount);
    if (unit != need.getUnit()) {
      changeCapacity(supply.getUnit(), need.getUnit(), need.getItem(), amount);
    }
  }

  private boolean isHorse(String item) {
    return HORSEItem.equals(item);
  }

  private void giveTransfer(String targetId, String item, int amount, boolean all) {
    Unit targetUnit = findUnit(targetId);
    Need dummyNeed = new Need(targetUnit, item, amount, amount, GIVE_IF_PRIORITY, new Warning(true),
        ++supplySerial);
    Supply supply = getSupply(item, currentUnit);
    all = all || (supply != null && supply.getAmount() == amount);
    addTransfer(currentUnit, targetUnit, item, amount, true, all, dummyNeed);
    transfer(currentUnit, dummyNeed, amount);
  }

  public static class MyReserveVisitor implements ReserveVisitor {

    public void execute(Unit u, String item, int amount) {
      if (amount > 0) {
        if (amount == u.getPersons()) {
          addOrder(u, getReserveOrder(u, item // + COMMENTOrder + need.toString()
              , 1, true), false);
        } else {
          addOrder(u, getReserveOrder(u, item, amount, false), false);
        }
      }
    }

  }

  private void executeReserves(Reserves reserves) {
    reserves.execute(new MyReserveVisitor());
  }

  private void changeCapacity(Unit source, Unit target, String item, int amount) {
    if (!isHorse(item)) {
      int weight = amount * getWeight(item);
      changeCapacity(source, weight);
      changeCapacity(target, -weight);
    }
  }

  private Integer changeCapacity(Unit unit, int delta) {
    Integer c = capacities.get(unit);
    if (c != null) {
      capacities.put(unit, c = c + delta);
    }
    return c;
  }

  /**
   * Tries to satisfy (minimum) need by a RESERVE order
   *
   * @param need
   * @param min
   * @param reserves
   */
  protected void reserveNeed(Need need, boolean min, Reserves reserves) {
    int amount = getNeedAmount(need, min);
    // amount = adjustForTransfer(need, amount);

    Supply supply = getSupply(need.getItem(), need.getUnit());
    if (supply == null)
      return;

    // only suppliers with positive priority serve maximum needs
    amount = Math.min(amount, supply.getAmount() - 0);
    if (amount > 0) {
      if (min) {
        addTransfer(need.getUnit(), need.getUnit(), need.getItem(), amount, min, false, need);
      }
      transfer(need.getUnit(), need, amount);
      if (min) {
        reserves.add(need.getItem(), need.getUnit(), amount);
      }
    }
  }

  /**
   * Tries to satisfy (minimum) need by a give order from suppliers.
   *
   * @param need
   * @param min
   */
  protected void giveNeed(Need need, boolean min) {
    int amount = getNeedAmount(need, min);
    if (amount > 0) {
      // adjustForTransfer(need, amount);
      for (Supply supply : supplyMapp.get(need.getItem())) {
        if (supply.getUnit() != need.getUnit() && (min || supply.getPriority() > 0) && supply
            .hasPriority()) {
          int giveAmount = Math.min(amount, supply.getAmount());
          if (giveAmount > 0) {
            addTransfer(supply.getUnit(), need.getUnit(), need.getItem(), giveAmount, min, false,
                need);
            transfer(supply.getUnit(), need, giveAmount);
            amount -= giveAmount;
          }
        }
        if (amount <= 0) {
          break;
        }
      }
    }
  }

  private int getNeedAmount(Need need, boolean min) {
    int amount = min ? need.getMinAmount() : need.getAmount();
    return amount;
  }

  private int adjustForTransfer(Need need) {
    int amount = need.getMaxAmount();
    Integer transferred = getMulti(transfersMap, need.getUnit(), need.getItem());
    if (transferred != null) {
      if (transferred > amount) {
        increaseMulti(transfersMap, need.getUnit(), need.getItem(), -amount);
        need.setMinAmount(0);
        need.setMaxAmount(0);
        amount = 0;
      } else {
        removeMulti(transfersMap, need.getUnit(), need.getItem());
        need.reduceMinAmount(transferred);
        need.reduceMaxAmount(transferred);
        amount -= transferred;
      }
    }
    return amount;
  }

  protected int getWeight(String item) {
    return Math.round(world.getRules().getItemType(item).getWeight() * 100);
  }

  /**
   * Returns the total supply for an item.
   *
   * @param item Order name of the supplied item
   * @return The supply or 0, if none has been registered.
   */
  protected int getSupply(String item) {
    return supplyMapp.getSupply(item);
  }

  /**
   * Returns a supply of a unit for an item.
   *
   * @param item Order name of the supplied item
   * @param unit
   * @return The supply or null, if none has been registered.
   */
  protected Supply getSupply(String item, Unit unit) {
    return supplyMapp.get(item, unit);
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
    addNeed(item, unit, minAmount, maxAmount, priority, new Warning(true));
  }

  /**
   * Adds the specified amounts to the need of a unit for an item to the needMap.
   *
   * @param item Order name of the required item
   * @param unit
   * @param minAmount
   * @param maxAmount
   */
  protected Need addNeed(String item, Unit unit, int minAmount, int maxAmount, int priority,
      Warning w) {
    Need result;
    if (!currentFactions.containsKey(unit.getFaction())) {
      int count;
      if (getSupply(item, unit) == null && (count = getItemCount(unit, item)) > 0) {
        putDummySupply(item, unit, count);
      }
    }
    if (minAmount > maxAmount) {
      addNewError("min amount " + minAmount + " > max amount" + maxAmount);
      maxAmount = minAmount;
    }
    needQueue.add(result = new Need(unit, item, minAmount, maxAmount, priority, w, ++supplySerial));

    return result;
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
   * @param warning Warnings for missing equipment are only issued if this is <code>true</code>.
   */
  protected void soldier(Unit u, String sWeaponSkill, String sWeapon, String sShield,
      String sArmor, String warning) {

    Rules rules = world.getRules();

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
      if (weaponSkill == null && !Warning.W_NEVER.equals(warning)) {
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
    ArrayList<Item> shields = findItems(shield, u, "shield", true);
    if (shields.isEmpty()) {
      addNewError("keine Schilde bekannt");
    }

    ArrayList<Item> armors = findItems(armor, u, "armour", true);
    if (armors.isEmpty()) {
      addNewError("keine Rüstungen bekannt");
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
        && !reserveEquipment(weapon, weapons, !Warning.W_NEVER.equals(warning) && !Warning.W_SKILL
            .equals(warning))) {
      addNewError("konnte Waffe nicht reservieren");
    }
    if (!NULL.equals(sShield)
        && !reserveEquipment(shield, shields, Warning.W_ARMOR.equals(warning) || Warning.W_SHIELD
            .equals(warning))) {
      addNewError("konnte Schilde nicht reservieren");
    }
    if (!NULL.equals(sArmor) && !reserveEquipment(armor, armors, Warning.W_ARMOR.equals(warning))) {
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
  protected static ArrayList<Item> findItems(ItemType itemType, Unit u, String category,
      boolean returnDummy) {
    ArrayList<Item> items = new ArrayList<Item>(1);
    ItemCategory itemCategory = world.getRules().getItemCategory(StringID.create(category));
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
    if (items.isEmpty() && returnDummy) {
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

  private static Integer putMulti(Map<Unit, Map<String, Integer>> map, Unit key1, String key2,
      Integer value) {
    Map<String, Integer> inner = map.get(key1);
    if (inner == null) {
      map.put(key1, inner = new HashMap<String, Integer>());
    }
    Integer old = inner.put(key2, value);
    return old;
  }

  private static void increaseMulti(Map<Unit, Map<String, Integer>> map, Unit key1, String key2,
      int value) {
    Map<String, Integer> inner = map.get(key1);
    if (inner == null) {
      map.put(key1, inner = new HashMap<String, Integer>());
    }
    Integer old = inner.get(key2);
    if (old == null) {
      inner.put(key2, value);
    } else {
      inner.put(key2, old + value);
    }
  }

  static void increaseMultiInv(Map<String, Map<Unit, Integer>> map, String key1, Unit key2,
      int value) {
    Map<Unit, Integer> inner = map.get(key1);
    if (inner == null) {
      map.put(key1, inner = new HashMap<Unit, Integer>());
    }
    Integer old = inner.get(key2);
    if (old == null) {
      inner.put(key2, value);
    } else {
      inner.put(key2, old + value);
    }
  }

  private static void appendMulti(Map<Object, Map<Object, Collection<Integer>>> map, Object key1,
      Object key2,
      Integer value) {
    Map<Object, Collection<Integer>> inner = map.get(key1);
    if (inner == null) {
      map.put(key1, inner = new HashMap<Object, Collection<Integer>>());
    }
    Collection<Integer> old = inner.get(key2);
    if (old == null) {
      inner.put(key2, old = new ArrayList<Integer>());
    }
    old.add(value);
  }

  private static Integer getMulti(Map<Unit, Map<String, Integer>> map, Unit key1, String key2) {
    Map<String, Integer> inner = map.get(key1);
    if (inner != null)
      return inner.get(key2);
    return null;
  }

  private static Integer removeMulti(Map<Unit, Map<String, Integer>> map, Unit key1, String key2) {
    Map<String, Integer> inner = map.get(key1);
    if (inner != null) {
      Integer val = inner.remove(key2);
      if (inner.isEmpty()) {
        map.remove(key1);
      }
      return val;
    }
    return null;
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
    helper.addOrder(unit, "; TODO: " + text);
    setConfirm(unit, false);
  }

  public static void addOrder(Unit u, String order, boolean refresh) {
    u.addOrder(order, refresh);
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
    return world.getRules().getItemType(name);
  }

  /**
   * Returns the SkillType in the rules matching <code>name</code>.
   *
   * @param name A skill name, like "Ausdauer"
   * @return The SkillType corresponding to name or <code>null</code> if this SkillType does not
   *         exist.
   */
  public static SkillType getSkillType(String name) {
    return world.getRules().getSkillType(name);
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
    return RESEARCHOrder + " " + getLocalizedOrder(EresseaConstants.OC_HERBS, "KRÄUTER");
  }

  /**
   * Returns a <code>RECRUIT amount race</code> order.
   */
  protected String getRecruitOrder(int amount, Race race) {
    if (race != null)
      return getLocalizedOrder(EresseaConstants.OC_RECRUIT, new Object[] { amount, race });
    else
      return getLocalizedOrder(EresseaConstants.OC_RECRUIT, new Object[] { amount });
    // return RECRUITOrder + " " + amount
    // + (race != null ? (" " + getLocalizedOrder("race." + race.getID(), race.getName())) : "");
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
    ItemCategory weapons = world.getRules().getItemCategory(StringID.create("weapons"));
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
   * Converts some Vorlage commands to $cript commands. Call from the script of your faction
   * with<br />
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
    currentFactions = Collections.singletonMap(faction, 1);

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
    final String scriptStart = createScriptCommand();
    for (Unit u : region.units())
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        setCurrentUnit(u);

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
                addNewOrder(scriptStart + "BerufDepotVerwalter " + (number > 0 ? number : ""),
                    true);
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
              addNewOrder("@" + currentOrder.substring(14, currentOrder.lastIndexOf("}") - 1),
                  true);
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

        updateCurrentOrders();
        setCurrentUnit(null);
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
    currentFactions = Collections.singletonMap(faction, 1);

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
    currentFactions = Collections.singletonMap(faction, 1);

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

        setCurrentUnit(u);

        // loop over orders
        line = 0;
        for (Order command : u.getOrders2()) {
          currentOrder = command.getText();
          if (currentOrder.startsWith("@RESERVIERE")) {
            addNewOrder("// $cript Benoetige " + currentOrder.substring(currentOrder.indexOf(" ")),
                true);
            addNewOrder(currentOrder.substring(1), true);
          } else {
            if (command.isLong() || command.isPersistent() || command.getText().startsWith("//")) {
              addNewOrder(command.getText(), false);
            } else {
              // omit
              setChangedOrders(true);
            }
          }
        }
        updateCurrentOrders();
        setCurrentUnit(null);
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
    currentFactions = Collections.singletonMap(faction, 1);

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

        setCurrentUnit(u);

        // loop over orders
        line = 0;
        for (Order command : u.getOrders2()) {
          currentOrder = command.getText();
          if (currentOrder.startsWith("; $$$ LE")) {
            addNewOrder(currentOrder.substring(currentOrder.indexOf("$$$") + 4) + " ; restored",
                true);
            setChangedOrders(true);
          } else if (isChangedOrders()
              && (currentOrder.startsWith("LERNE") || currentOrder.startsWith("LEHRE"))) {
            // skip
          } else {
            addNewOrder(command.getText(), false);
          }
        }
        updateCurrentOrders();
        setCurrentUnit(null);
      }
    }

  }

  /**
   * Private utility script written to handle unwanted orders. Not interesting for the general
   * public.
   *
   * @param faction
   * @param region
   */
  public void fixTwoLongOrders(Faction faction, Region region) {
    if (faction == null)
      throw new NullPointerException();
    currentFactions = Collections.singletonMap(faction, 1);

    initLocales();

    if (region == null) {
      // comment out the following line if you don't have the newest nightly build of Magellan
      helper.getUI().setMaximum(world.getRegions().size());

      // call self for all regions
      for (Region r : world.getRegions()) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(r.toString(), ++progress);

        fixTwoLongOrders(faction, r);
      }
      return;
    }

    // loop for all units of faction
    for (Unit u : region.units()) {
      if (faction.getID().equals(u.getFaction().getID())) {
        // comment out the following line if you don't have the newest nightly build of Magellan
        helper.getUI().setProgress(region.toString() + " - " + u.toString(), progress);

        setCurrentUnit(u);

        int hasLong = 0;
        for (Order command : u.getOrders2()) {
          currentOrder = command.getText();
          if (command.isLong()) {
            if (++hasLong > 1) {
              addNewOrder(COMMENTOrder + " " + currentOrder, true);
              setChangedOrders(true);
            } else {
              addNewOrder(currentOrder, false);
            }
          } else {
            addNewOrder(currentOrder, false);
          }
        }
        updateCurrentOrders();
        setCurrentUnit(null);
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
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new FileReader(file));

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
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static boolean isJavaComment(String line) {
    return line.matches("[ ]*//.*") || line.matches("[ ]*[*].*");
  }
  // ---stop comment for BeanShell

}

class UnitItemPriority {
  public UnitItemPriority(Unit unit, String item, int amount, int priority, long serial) {
    this.unit = unit;
    this.item = item;
    this.amount = amount;
    this.priority = priority;
    this.serial = serial;
  }

  Unit unit;
  /** The order name of the item. */
  String item;
  int amount;
  int priority;
  long serial;

  public static int reduceAmount(int amount, int change) {
    if (amount == Integer.MAX_VALUE || amount == Integer.MIN_VALUE)
      return amount;
    long newValue = (long) amount - change;
    if (newValue > Integer.MAX_VALUE)
      return Integer.MAX_VALUE;
    else if (newValue < Integer.MIN_VALUE)
      return Integer.MIN_VALUE;
    else
      return (int) newValue;

  }

  public int compareTo(UnitItemPriority uip) {
    int diff = uip.priority - priority;
    if (diff != 0)
      return diff;
    return serial > uip.serial ? 1 : serial < uip.serial ? -1 : 0;
  }

  @Override
  public String toString() {
    return unit + " " + amount + " " + item + " (" + priority + ")";
  }
}

class Supply {

  UnitItemPriority uip;

  public Supply(Unit unit, String item, int amount, long serial) {
    super();
    if (unit == null)
      throw new NullPointerException();
    uip = new UnitItemPriority(unit, item, amount, E3CommandParser.DEFAULT_SUPPLY_PRIORITY, serial);
  }

  public boolean hasPriority() {
    return getPriority() != Long.MAX_VALUE;
  }

  public int getPriority() {
    return uip.priority;
  }

  public void setPriority(int priority) {
    uip.priority = priority;
  }

  public Unit getUnit() {
    return uip.unit;
  }

  public int getAmount() {
    return uip.amount;
  }

  public void setAmount(int i) {
    uip.amount = i;
  }

  public String getItem() {
    return uip.item;
  }

  public void reduceAmount(int change) {
    uip.amount = UnitItemPriority.reduceAmount(uip.amount, change);
  }

  @Override
  public String toString() {
    return uip.unit + " has " + uip.amount + " " + uip.item;
  }

  public int compareTo(Supply o) {
    return uip.compareTo(o.uip);
  }
}

interface OrderFilter {

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

class Need {

  private int minAmount;
  private Warning warning;
  UnitItemPriority uip;
  private String message;

  public Need(Unit unit, String item, int minAmount, int maxAmount, int priority,
      Warning warning, long serial) {
    uip = new UnitItemPriority(unit, item, maxAmount, priority, serial);
    this.minAmount = minAmount;
    this.warning = warning;
    message = createMessage();
  }

  public int compareTo(Need need) {
    return uip.compareTo(need.uip);
  }

  public int getPriority() {
    return uip.priority;
  }

  public Unit getUnit() {
    return uip.unit;
  }

  public int getAmount() {
    return uip.amount;
  }

  public void setAmount(int i) {
    uip.amount = i;
    message = createMessage();
  }

  public String getItem() {
    return uip.item;
  }

  public int getMinAmount() {
    return minAmount;
  }

  public void setMinAmount(int amount) {
    minAmount = amount;
    message = createMessage();
  }

  public void reduceMinAmount(int change) {
    minAmount = UnitItemPriority.reduceAmount(minAmount, change);
  }

  public int getMaxAmount() {
    return getAmount();
  }

  public void setMaxAmount(int amount) {
    setAmount(amount);
    message = createMessage();
  }

  public void reduceMaxAmount(int change) {
    uip.amount = UnitItemPriority.reduceAmount(uip.amount, change);
  }

  /**
   * Returns the value of warning.
   *
   * @return Returns warning.
   */
  public Warning getWarning() {
    return warning;
  }

  /**
   * Sets the value of warning.
   *
   * @param warning The value for warning.
   */
  public void setWarning(Warning warning) {
    this.warning = warning;
  }

  @Override
  public String toString() {
    return createMessage();
  }

  private String createMessage() {
    return uip.unit + " needs " + minAmount + "/" + uip.amount + " " + uip.item + " ("
        + uip.priority + ")";
  }

  public String getMessage() {
    return message;
  }
}

class Transfer {
  private Unit target;
  private boolean all;
  private Need need;
  private boolean min;
  private UnitItemPriority uip;

  public Transfer(Unit unit, Unit target, String item, int amount, boolean min, boolean all,
      Need need,
      long serial) {
    uip = new UnitItemPriority(unit, item, amount, 0, serial);
    this.target = target;
    this.all = all;
    this.need = need;
    this.min = min;
  }

  public void reduceAmount(int change) {
    uip.amount = UnitItemPriority.reduceAmount(uip.amount, change);
  }

  public Unit getUnit() {
    return uip.unit;
  }

  public int getAmount() {
    return uip.amount;
  }

  public void setAmount(int i) {
    uip.amount = i;
  }

  public String getItem() {
    return uip.item;
  }

  public boolean isMin() {
    return min;
  }

  public Unit getTarget() {
    return target;
  }

  public boolean isAll() {
    return all;
  }

  public String getMessage() {
    return need.getMessage();
  }

  @Override
  public String toString() {
    return uip.unit + " gives " + target + " " + (all ? "ALL" : uip.amount) + " " + uip.item;
  }

  public Need getNeed() {
    return need;
  }

}
