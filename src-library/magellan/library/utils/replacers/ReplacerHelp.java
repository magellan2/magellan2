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

package magellan.library.utils.replacers;

import java.util.Iterator;

import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.guiwrapper.EventDispatcherInterface;

/**
 * Sets up and provides the replacers.
 * 
 * @author Andreas
 * @version 1.0
 */
public class ReplacerHelp implements GameDataListener {
  protected static DefaultReplacerFactory defaultFactory;

  /**
   * Creates all replacers.
   */
  public void init(GameData data) {

    DefaultReplacerFactory drf = new DefaultReplacerFactory();

    drf.putReplacer("newline", NewLineReplacer.class);

    Object args[] = new Object[2];
    args[0] = "getName";
    args[1] = Integer.valueOf(0);

    drf.putReplacer("rname", RegionMethodReplacer.class, args);

    args[0] = "getCoordinate";
    args[1] = Integer.valueOf(0);
    drf.putReplacer("coordinate", RegionMethodReplacer.class, args);

    args[0] = "getType";
    drf.putReplacer("rtype", RegionMethodReplacer.class, args);

    args[1] = Integer.valueOf(RegionFieldReplacer.MODE_NON_NEGATIVE);

    Class<?> regionField = RegionFieldReplacer.class;

    args[0] = "peasants";
    drf.putReplacer("peasants", regionField, args);
    args[0] = "stones";
    drf.putReplacer("stones", regionField, args);
    args[0] = "silver";
    drf.putReplacer("silver", regionField, args);
    args[0] = "trees";
    drf.putReplacer("trees", regionField, args);
    args[0] = "horses";
    drf.putReplacer("horses", regionField, args);

    args[0] = "wage";
    drf.putReplacer("wage", regionField, args);
    args[0] = "sprouts";
    drf.putReplacer("sprouts", regionField, args);

    args[0] = "oldPeasants";
    drf.putReplacer("oldPeasants", regionField, args);
    args[0] = "oldStones";
    drf.putReplacer("oldStones", regionField, args);
    args[0] = "oldSilver";
    drf.putReplacer("oldSilver", regionField, args);
    args[0] = "oldTrees";
    drf.putReplacer("oldTrees", regionField, args);
    args[0] = "oldHorses";
    drf.putReplacer("oldHorses", regionField, args);
    args[0] = "oldIron";
    drf.putReplacer("oldIron", regionField, args);
    args[0] = "oldLaen";
    drf.putReplacer("oldLaen", regionField, args);
    args[0] = "oldWage";
    drf.putReplacer("oldWage", regionField, args);
    args[0] = "oldSprouts";
    drf.putReplacer("oldSprouts", regionField, args);

    args[1] = Integer.valueOf(RegionFieldReplacer.MODE_NON_NEGATIVE);

    Class<?> regionMethod = RegionMethodReplacer.class;

    args[0] = "maxRecruit";
    drf.putReplacer("recruit", regionMethod, args);
    args[0] = "maxEntertain";
    drf.putReplacer("entertain", regionMethod, args);
    args[0] = "getPeasantWage";
    drf.putReplacer("peasantWage", regionMethod, args);
    args[0] = "getMorale";
    drf.putReplacer("morale", regionMethod, args);

    // Fiete 20061222 coords
    args[1] = Integer.valueOf(RegionFieldReplacer.MODE_ALL);
    args[0] = "getCoordX";
    drf.putReplacer("posX", regionMethod, args);
    args[0] = "getCoordY";
    drf.putReplacer("posY", regionMethod, args);

    drf.putReplacer("herb", HerbReplacer.class);

    drf.putReplacer("maxWorkers", MaxWorkersReplacer.class);

    // luxury price, sold luxury
    drf.putReplacer("price", LuxuryPriceReplacer.class);

    drf.putReplacer("maxtrade", MaxTradeReplacer.class);
    drf.putReplacer("tradetype", TradeReplacer.class); // == soldname

    Class<?> soldClass = SoldLuxuryReplacer.class;
    drf.putReplacer("soldname", soldClass, Integer.valueOf(0));
    drf.putReplacer("soldchar1", soldClass, Integer.valueOf(1));
    drf.putReplacer("soldchar2", soldClass, Integer.valueOf(2));
    drf.putReplacer("soldprice", soldClass, Integer.valueOf(3));

    // item replacer
    drf.putReplacer("item", ItemTypeReplacer.class);

    // normal count
    drf.putReplacer("count", UnitCountReplacer.class);
    drf.putReplacer("countUnits", UnitCountReplacer.class, Boolean.FALSE);

    // skill count
    Integer iarg = Integer.valueOf(0);
    drf.putReplacer("skill", UnitSkillCountReplacer.class, iarg);
    iarg = Integer.valueOf(1);
    drf.putReplacer("skillmin", UnitSkillCountReplacer.class, iarg);
    iarg = Integer.valueOf(2);
    drf.putReplacer("skillsum", UnitSkillCountReplacer.class, iarg);
    iarg = Integer.valueOf(3);
    drf.putReplacer("skillminsum", UnitSkillCountReplacer.class, iarg);

    // tag replacement
    drf.putReplacer("tag", TagReplacer.class, Boolean.FALSE);
    drf.putReplacer("tagblank", TagReplacer.class, Boolean.TRUE);

    // description
    drf.putReplacer("name", NameReplacer.class);
    drf.putReplacer("description", DescriptionReplacer.class);
    drf.putReplacer("privDesc", PrivDescReplacer.class);

    // faction switch
    drf.putReplacer("faction", FactionSwitch.class);

    // trustlevel switch
    iarg = Integer.valueOf(0);
    drf.putReplacer("priv", TrustlevelSwitch.class, iarg);
    iarg = Integer.valueOf(1);
    drf.putReplacer("privminmax", TrustlevelSwitch.class, iarg);

    // unit filter
    drf.putReplacer("filter", FilterSwitch.class);

    // operators
    drf.putReplacer("+", AdditionOperator.class);
    drf.putReplacer("-", SubtractionOperator.class);
    drf.putReplacer("*", MultiplicationOperator.class);
    drf.putReplacer("/", DivisionOperator.class);

    drf.putReplacer("substr", SubstrOperator.class);

    // op switch
    drf.putReplacer("op", OperationSwitch.class);

    // comparators
    drf.putReplacer("not", NotReplacer.class);
    drf.putReplacer("equals", StringEqualReplacer.class);
    drf.putReplacer("equalsIgnoreCase", StringEqualReplacer.class, Boolean.TRUE);
    drf.putReplacer("contains", StringIndexReplacer.class);
    drf.putReplacer("containsIgnoreCase", StringIndexReplacer.class, Boolean.TRUE);
    drf.putReplacer("<", LessReplacer.class);
    drf.putReplacer("null", NullReplacer.class);

    // branch replacers
    drf.putReplacer("if", IfBranchReplacer.class);

    // special: mallornregion
    drf.putReplacer("mallornregion", MallornRegionSwitch.class);

    ReplacerHelp.defaultFactory = drf;
    ReplacerHelp.reworkRegionSwitches(data);

  }

  protected static void reworkRegionSwitches(GameData data) {
    if (data == null)
      return;

    for (Iterator<RegionType> iter = data.getRules().getRegionTypeIterator(); iter.hasNext();) {
      RegionType type = iter.next();
      Object arg[] = new Object[1];
      String name = "is" + type.getID().toString();
      arg[0] = type;
      ReplacerHelp.defaultFactory.putReplacer(name, RegionTypeSwitch.class, arg);
    }

    ItemType resource;
    if ((resource = data.getRules().getItemType(EresseaConstants.I_RIRON)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("ironlevel", IronLevelReplacer.class, resource);
    }
    if ((resource = data.getRules().getItemType(EresseaConstants.I_RSTONES)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("stoneslevel", StonesLevelReplacer.class, resource);
    }
    if ((resource = data.getRules().getItemType(EresseaConstants.I_RLAEN)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("laenlevel", LaenLevelReplacer.class, resource);
    }

    if ((resource = data.getRules().getItemType(EresseaConstants.I_RIRON)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("iron", IronReplacer.class, resource);
    }
    if ((resource = data.getRules().getItemType(EresseaConstants.I_RLAEN)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("laen", LaenReplacer.class, resource);
    }

    if ((resource = data.getRules().getItemType(EresseaConstants.I_RMALLORN)) != null) {
      ReplacerHelp.defaultFactory.putReplacer("mallorn", MallornReplacer.class, resource);
    }

  }

  /**
   * Creates a new ReplacerHelp object.
   */
  public ReplacerHelp(EventDispatcherInterface dispatcher, GameData data) {
    // we want to be informed early so that the replacer factory is updated at first
    dispatcher.addPriorityGameDataListener(this);
    init(data);
  }

  /**
   * Applies the given replacer to the argument.
   * 
   * @return the replacer result or <code>null</code> on error.
   */
  public static Object getReplacement(Replacer replacer, Object arg) {
    try {
      return replacer.getReplacement(arg);
    } catch (Exception exc) {
      // return null on error
    }

    return null;
  }

  /**
   * Returns the default factory.
   */
  public static ReplacerFactory getDefaultReplacerFactory() {
    return ReplacerHelp.defaultFactory;
  }

  /**
   * Parses defStr and sets up a replacers system accordingly.
   */
  public static ReplacerSystem createReplacer(String def, String cmd, String unknown) {
    if (ReplacerHelp.defaultFactory != null)
      return DefinitionMaker.createDefinition(def, cmd, ReplacerHelp.defaultFactory, unknown);

    return null;
  }

  /**
   * Returns a replacer for the given definition string, using the default separator § and unknown
   * result "-?".
   */
  public static ReplacerSystem createReplacer(String def) {
    return ReplacerHelp.createReplacer(def, "§", "-?-");
  }

  /**
   * Returns a replacer for the given definition string, using the default separator § and given
   * unknown result.
   */
  public static ReplacerSystem createReplacer(String def, String unknown) {
    return ReplacerHelp.createReplacer(def, "§", unknown);
  }

  /**
   * Invoked when the current game data object becomes invalid.
   */
  public void gameDataChanged(GameDataEvent e) {
    ReplacerHelp.reworkRegionSwitches(e.getGameData());
  }
}
