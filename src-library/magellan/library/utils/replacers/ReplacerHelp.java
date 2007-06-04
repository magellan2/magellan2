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
import magellan.library.rules.RegionType;
import magellan.library.utils.guiwrapper.EventDispatcherInterface;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class ReplacerHelp implements GameDataListener {
	protected static DefaultReplacerFactory defaultFactory;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void init(GameData data) {

		DefaultReplacerFactory drf = new DefaultReplacerFactory();

		drf.putReplacer("newline", NewLineReplacer.class);

		Object args[] = new Object[2];
		args[0] = "getName";
		args[1] = new Integer(0);

		drf.putReplacer("rname", RegionMethodReplacer.class, args);

		args[0] = "getType";
		drf.putReplacer("rtype", RegionMethodReplacer.class, args);

		args[1] = new Integer(RegionFieldReplacer.MODE_NON_NEGATIVE);

		Class regionField = RegionFieldReplacer.class;

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
		// Fiete 20070123
		// args[0] = "iron";
		// drf.putReplacer("iron", regionField, args);
		drf.putReplacer("iron", IronReplacer.class);
		
		// args[0] = "laen";
		// drf.putReplacer("laen", regionField, args);
		drf.putReplacer("laen", LaenReplacer.class);
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

		args[1] = new Integer(RegionMethodReplacer.MODE_NON_NEGATIVE);

		Class regionMethod = RegionMethodReplacer.class;

		args[0] = "maxRecruit";
		drf.putReplacer("recruit", regionMethod, args);
		args[0] = "maxEntertain";
		drf.putReplacer("entertain", regionMethod, args);
		args[0] = "getPeasantWage";
		drf.putReplacer("peasantWage", regionMethod, args);
		
		// Fiete 20061222  coords
		args[1] = new Integer(RegionMethodReplacer.MODE_ALL);
		args[0] = "getCoordX";
		drf.putReplacer("posX", regionMethod, args);
		args[0] = "getCoordY";
		drf.putReplacer("posY", regionMethod, args);
		
		drf.putReplacer("maxtrade", MaxTradeReplacer.class);
		drf.putReplacer("herb", HerbReplacer.class);

		drf.putReplacer("maxWorkers", MaxWorkersReplacer.class);
		// FF 20070123
		drf.putReplacer("mallorn", MallornReplacer.class);

		//luxury price, sold luxury
		drf.putReplacer("price", LuxuryPriceReplacer.class);

		Class soldClass = SoldLuxuryReplacer.class;
		drf.putReplacer("soldname", soldClass, new Integer(0));
		drf.putReplacer("soldchar1", soldClass, new Integer(1));
		drf.putReplacer("soldchar2", soldClass, new Integer(2));
		drf.putReplacer("soldprice", soldClass, new Integer(3));

		//item replacer
		drf.putReplacer("item", ItemTypeReplacer.class);

		//normal count
		drf.putReplacer("count", UnitCountReplacer.class);
		drf.putReplacer("countUnits", UnitCountReplacer.class, Boolean.FALSE);

		//skill count
		Integer iarg = new Integer(0);
		drf.putReplacer("skill", UnitSkillCountReplacer.class, iarg);
		iarg = new Integer(1);
		drf.putReplacer("skillmin", UnitSkillCountReplacer.class, iarg);
		iarg = new Integer(2);
		drf.putReplacer("skillsum", UnitSkillCountReplacer.class, iarg);
		iarg = new Integer(3);
		drf.putReplacer("skillminsum", UnitSkillCountReplacer.class, iarg);

		//tag replacement
		drf.putReplacer("tag", TagReplacer.class, Boolean.FALSE);
		drf.putReplacer("tagblank", TagReplacer.class, Boolean.TRUE);

		//description
		drf.putReplacer("description", DescriptionReplacer.class);
		drf.putReplacer("privDesc", PrivDescReplacer.class);

		//faction switch		
		drf.putReplacer("faction", FactionSwitch.class);

		// trustlevel switch
		iarg = new Integer(0);
		drf.putReplacer("priv", TrustlevelSwitch.class, iarg);
		iarg = new Integer(1);
		drf.putReplacer("privminmax", TrustlevelSwitch.class, iarg);

		// unit filter
		drf.putReplacer("filter", FilterSwitch.class);

		// operators
		drf.putReplacer("+", AdditionOperator.class);
		drf.putReplacer("-", SubtractionOperator.class);
		drf.putReplacer("*", MultiplicationOperator.class);
		drf.putReplacer("/", DivisionOperator.class);

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

		reworkRegionSwitches(data);

		defaultFactory = drf;
	}

	protected static void reworkRegionSwitches(GameData data) {
		if(data == null) {
			return;
		}

		for(Iterator iter = data.rules.getRegionTypeIterator(); iter.hasNext();) {
			RegionType type = (RegionType) iter.next();
			Object arg[] = new Object[1];
			String name = "is" + type.getID().toString();
			arg[0] = type;
			defaultFactory.putReplacer(name, RegionTypeSwitch.class, arg);
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
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public static Object getReplacement(Replacer replacer, Object arg) {
		try {
			return replacer.getReplacement(arg);
		} catch(Exception exc) {
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static ReplacerFactory getDefaultReplacerFactory() {
		return defaultFactory;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static ReplacerSystem createReplacer(String def, String cmd, String unknown) {
		if(defaultFactory != null) {
			return DefinitionMaker.createDefinition(def, cmd, defaultFactory, unknown);
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static ReplacerSystem createReplacer(String def) {
		return createReplacer(def, "§", "-?-");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public static ReplacerSystem createReplacer(String def, String unknown) {
		return createReplacer(def, "§", unknown);
	}

	/**
	 * Invoked when the current game data object becomes invalid.
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		reworkRegionSwitches(e.getGameData());
	}
}
