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

package magellan.library.gamebinding;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
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
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
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
	private static final Comparator prioComp = new PrioComp();
	private OrderParser parser = null;
	private List<Completion> completions = null;
	private GameData data = null;
	private Region region = null;
	private Unit unit = null;
	private CompleterSettingsProvider completerSettingsProvider = null;

	/**
	 * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
	 * <tt>GameData</tt> object.
	 *
	 * @param gd The <tt>GameData</tt> this completer uses as context.
	 * 
	 */
	public EresseaOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
		this.completerSettingsProvider = ac;
		this.completions = new LinkedList<Completion>();
		this.data = gd;

		if(data != null) {
			parser = new EresseaOrderParser(this.data, this);
		} else {
			parser = new EresseaOrderParser(null, this);
		}
	}

	/**
	 * Parses the String cmd with Unit u as context and returns possible completions if the cmd is
	 * an incomplete order.
	 *
	 * @param u a <tt>Unit</tt> object taken as context information for the completion decisions.
	 * @param cmd a <tt>String</tt> containing the (possibly incomplete) order to parse.
	 *
	 * @return a <tt>List</tt> with possible completions of the given order. If there are no
	 * 		   proposed completions this list is empty.
	 */
	public List<Completion> getCompletions(Unit u, String cmd) {
		unit = u;
		region = unit.getRegion();
		completions = new LinkedList<Completion>();
		parser.read(new StringReader(cmd));

		List tokens = parser.getTokens();

		if((tokens.size() > 1) &&
			   (((OrderToken) tokens.get(tokens.size() - 2)).ttype == OrderToken.TT_COMMENT)) {
			return new LinkedList<Completion>();
		} else {
			return crop(completions, cmd);
		}
	}

	/**
	 * Filters all Completion objects from list, that do not match the last word in txt, usually
	 * the order entered so far.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public List<Completion> crop(List<Completion> list, String txt) {
		List<Completion> ret = new LinkedList<Completion>();
		int start = 0;
		String stub = getStub(txt);

		if(stub.length() > 0) {
			// filter list
			Collections.sort(list, new IgnrCsComp());
			start = Collections.binarySearch(list, stub, new IgnrCsComp());

			if(start == (-list.size() - 1)) {
				return ret;
			} else {
				if(start < 0) {
					start = Math.abs(start) - 1;
				}

				Iterator it = list.listIterator(start);

				while(it.hasNext()) {
					Completion elem = (Completion) it.next();
					String val = elem.getName();
					int len = Math.min(stub.length(), val.length());

					if(val.substring(0, len).equalsIgnoreCase(stub)) {
						ret.add(elem);
					} else {
						break;
					}
				}
			}

			Collections.sort(ret, prioComp);
		} else {
			// stub.length <= 0
			ret = list;
		}

		return ret;
	}

	// begin of completion methods invoked by OrderParser
	void cmplt() {
		// add completions, that were defined by the user in the option pane
		// and can be accessed by CompleterSettingsProvider.getSelfDefinedCompletions()
		completions.addAll(completerSettingsProvider.getSelfDefinedCompletions());
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_WORK)));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ATTACK),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BANNER),
									   Resources.getOrderTranslation(EresseaConstants.O_BANNER),
									   " \"\"", 9, 1));

		if(!unit.isHero()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION)));
		}

		if(hasSkill(unit, EresseaConstants.S_TARNUNG)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STEAL),
										   " "));
		}

		if(!region.buildings().isEmpty()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SIEGE),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NAME),
									   " "));

		if(unit.getItems().size() > 0) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_USE),
										   Resources.getOrderTranslation(EresseaConstants.O_USE),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE),
									   " "));

		if (unit.getFaction().getItems().size()>0) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CLAIM),
					   Resources.getOrderTranslation(EresseaConstants.O_CLAIM),
					   " "));
		}
		
		
		if(!region.buildings().isEmpty() || !region.ships().isEmpty()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ENTER),
										   " "));
		}

		if(unit.getGuard() == 0) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD)));
		} else {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD) +
										   " " +
										   Resources.getOrderTranslation(EresseaConstants.O_NOT)));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EMAIL),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_END)));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RIDE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW),
									   " "));

		if(hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 7)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH) +
										   " " +
										   Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GIVE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROUP),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HELP),
									   " "));

		if(hasSkill(unit, EresseaConstants.S_MAGIE)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL),
										   " "));
		}

		if(hasSkill(unit, EresseaConstants.S_HANDELN)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BUY),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CONTACT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEACH),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEARN),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MAKE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MOVE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NUMBER),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_OPTION),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD),
									   " "));

		if(hasSkill(unit, EresseaConstants.S_KRAEUTERKUNDE, 6)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PLANT)));
		}

		if(unit.getShip() != null) {
			Unit owner = unit.getShip().getOwnerUnit();

			if(owner != null) {
				if(owner.equals(unit)) {
					completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PIRACY),
												   " "));
				}
			}
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PREFIX),
									   Resources.getOrderTranslation(EresseaConstants.O_PREFIX),
									   " \"\"", 9, 1));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT),
									   " "));

		if(!(unit instanceof TempUnit)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_RESERVE),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROUTE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SORT),
									   " "));

		if(hasSkill(unit, EresseaConstants.S_SPIONAGE)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SPY),
										   " "));
		}

		//completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STIRB), " ")); // don't blame me...
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HIDE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CARRY),
									   " "));

		if(hasSkill(unit, EresseaConstants.S_STEUEREINTREIBEN)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TAX),
										   " "));
		}

		if(hasSkill(unit, EresseaConstants.S_UNTERHALTUNG)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN),
									   " "));

		if((unit.getSkills() != null) && (unit.getSkills().size() > 0)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FORGET),
										   " "));
		}

		if(hasSkill(unit, EresseaConstants.S_HANDELN)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SELL),
										   " "));
		}

		if((unit.getBuilding() != null) || (unit.getShip() != null)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEAVE)));
		}

		if(hasSkill(unit, EresseaConstants.S_MAGIE)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CAST),
										   " "));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHOW),
									   " "));

		if(((unit.getBuilding() != null) && (unit.getBuilding().getOwnerUnit() != null) &&
			   (unit.getBuilding().getOwnerUnit().equals(unit))) ||
			   ((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null) &&
			   (unit.getShip().getOwnerUnit().equals(unit)))) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY)));
		} else {
			if(hasSkill(unit, EresseaConstants.S_STRASSENBAU) && (region != null) &&
				   !region.borders().isEmpty()) {
				completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DESTROY)," "));
			}
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GROW)," "));
	}

	void cmpltAt() {
		cmplt();
	}

	void cmpltAttack() {
		// collects spy-units to create a set of attack-orders against all spies later
		List<Unit> spies = new LinkedList<Unit>();

		// collects enemy units
		// maps faction ids to a List of unit ids
		// to create a set of attack-orders against total factions later
		Map<ID,List<Unit>> unitList = new Hashtable<ID, List<Unit>>();

		for(Iterator iter = unit.getRegion().units().iterator(); iter.hasNext();) {
			Unit curUnit = (Unit) iter.next();

			if(curUnit.isSpy()) {
				spies.add(curUnit);
				addUnit(curUnit, "");
			} else {
				Faction f = (Faction) curUnit.getFaction();

				if((f != null) && (f.getTrustLevel() <= Faction.TL_DEFAULT)) {
					List<Unit> v = unitList.get(f.getID());

					if(v == null) {
						v = new LinkedList<Unit>();
						unitList.put(f.getID(), v);
					}

					v.add(curUnit);
					addUnit(curUnit, "");
				}
			}
		}

		if(spies.size() > 0) {
			Iterator i = spies.iterator();
			Unit curUnit = (Unit) i.next();
			String enemyUnits = curUnit.getID().toString() + " ;" + curUnit.getName();

			while(i.hasNext()) {
				curUnit = (Unit) i.next();
				enemyUnits += ("\n" + Resources.getOrderTranslation(EresseaConstants.O_ATTACK) +
				" " + curUnit.getID().toString() + " ;" + curUnit.getName());
			}

			completions.add(new Completion(Resources.get("magellan.gamebinding.eressea.eresseaordercompleter.spies"), enemyUnits, "", 5, 0));
		}

		for(Iterator iter = unitList.keySet().iterator(); iter.hasNext();) {
			ID fID = (ID) iter.next();
			Iterator i = ((List) unitList.get(fID)).iterator();
			Unit curUnit = (Unit) i.next();
			String enemyUnits = curUnit.getID().toString() + " ;" + curUnit.getName();

			while(i.hasNext()) {
				curUnit = (Unit) i.next();
				enemyUnits += ("\n" + Resources.getOrderTranslation(EresseaConstants.O_ATTACK) +
				" " + curUnit.getID().toString() + " ;" + curUnit.getName());
			}

			completions.add(new Completion(data.getFaction(fID).getName() + " (" + fID.toString() +
										   ")", enemyUnits, "", 6, 0));
			completions.add(new Completion(fID.toString() + " (" + data.getFaction(fID).getName() +
										   ")", enemyUnits, "", 7, 0));
		}
	}

	void cmpltBeklaue() {
		addEnemyUnits("");
	}

	void cmpltBelagere() {
		if((data != null) && (unit != null) && (region != null)) {
			Faction ownerFaction = unit.getFaction();
			Iterator buildings = region.buildings().iterator();

			while(buildings.hasNext()) {
				Building b = (Building) buildings.next();

				if(b.getType().getID().equals(StringID.create("Burg")) &&
					   (b.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
					completions.add(new Completion(b.getName() + " (" + b.getID() + ")",
												   b.getID().toString(), "", 8));
					completions.add(new Completion(b.getID() + " (" + b.getName() + ")"));
				}
			}
		}
	}

	void cmpltBenenne() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   " \"\"", 9, 1));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FOREIGN),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
									   Resources.getOrderTranslation(EresseaConstants.O_FACTION),
									   " \"\"", 9, 1));

		if((unit.getBuilding() != null) && unit.getBuilding().getOwnerUnit().equals(unit)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
										   Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
										   " \"\"", 9, 1));
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
										   Resources.getOrderTranslation(EresseaConstants.O_REGION),
										   " \"\"", 9, 1));
		}

		if((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null) &&
			   unit.getShip().getOwnerUnit().equals(unit)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   " \"\"", 9, 1));
		}
	}

	void cmpltBenenneFremdes() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
									   Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
									   Resources.getOrderTranslation(EresseaConstants.O_FACTION),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
									   Resources.getOrderTranslation(EresseaConstants.O_SHIP),
									   " "));
	}

	void cmpltBenenneFremdeEinheit() {
		if((data != null) && (unit != null) && (region != null)) {
			Faction ownerFaction = unit.getFaction();
			Iterator units = region.units().iterator();

			while(units.hasNext()) {
				Unit u = (Unit) units.next();

				if(u.getFaction().equals(ownerFaction) == false) {
					String id = u.getID().toString();
					String name = u.getName();

					if((name != null) && name.toLowerCase().endsWith(id.toLowerCase())) {
						completions.add(new Completion(name + " (" + id + ")", id, " \"\"", 8, 1));
						completions.add(new Completion(id + " (" + name + ")", id, " \"\"", 9, 1));
					}
				}
			}
		}
	}

	void cmpltBenenneFremdesGebaeude() {
		if((data != null) && (unit != null) && (region != null)) {
			Faction ownerFaction = unit.getFaction();
			Iterator buildings = region.buildings().iterator();

			while(buildings.hasNext()) {
				Building b = (Building) buildings.next();

				if((b.getOwnerUnit() != null) &&
					   (b.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
					String id = b.getID().toString();
					String name = b.getName();

					if((name != null) && name.endsWith(id)) {
						completions.add(new Completion(b.getType().getName() + " " + name + " (" +
													   id + ")", id, " \"\"", 8, 1));
						completions.add(new Completion(id + " (" + b.getType().getName() + " " +
													   name + ")", id, " \"\"", 9, 1));
					}
				}
			}
		}
	}

	void cmpltBenenneFremdePartei() {
		if((data != null) && (data.factions() != null) && (unit != null)) {
			Faction ownerFaction = unit.getFaction();
			Iterator factions = data.factions().values().iterator();

			while(factions.hasNext()) {
				Faction f = (Faction) factions.next();

				if(f.equals(ownerFaction) == false) {
					String id = f.getID().toString();
					String name = f.getName();

					if((name != null) && name.endsWith(id)) {
						completions.add(new Completion(name + " (" + id + ")", id, " \"\"", 8, 1));
						completions.add(new Completion(id + " (" + name + ")", id, " \"\"", 9, 1));
					}
				}
			}
		}
	}

	void cmpltBenenneFremdesSchiff() {
		if((data != null) && (unit != null) && (region != null)) {
			Faction ownerFaction = unit.getFaction();
			Iterator ships = region.ships().iterator();

			while(ships.hasNext()) {
				Ship s = (Ship) ships.next();

				if((s.getOwnerUnit() != null) &&
					   (s.getOwnerUnit().getFaction().equals(ownerFaction) == false)) {
					String id = s.getID().toString();
					String name = s.getName();

					if((name != null) && name.endsWith(id)) {
						completions.add(new Completion(s.getType().getName() + " " + name + " (" +
													   id + ")", id, " \"\"", 8, 1));
						completions.add(new Completion(id + " (" + s.getType().getName() + " " +
													   name + ")", id, " \"\"", 9, 1));
					}
				}
			}
		}
	}

	void cmpltBenenneFremdesTargetID() {
		completions.add(new Completion(" \"\"", " \"\"", "", 9, 1));
	}

	void cmpltBenutze() {
		addUnitItems("");
	}

	void cmpltBeanspruche(){
		for (Iterator iter = unit.getFaction().getItems().iterator();iter.hasNext();){
			Item actItem = (Item)iter.next();
			completions.add(new Completion(actItem.getName()));
		}
	}
	
	void cmpltBeschreibe() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   " \"\"", 9, 1));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PRIVATE),
									   Resources.getOrderTranslation(EresseaConstants.O_PRIVATE),
									   " \"\"", 9, 1));

		if((unit.getBuilding() != null) && unit.getBuilding().getOwnerUnit().equals(unit)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
										   Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
										   " \"\"", 9, 1));
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
										   Resources.getOrderTranslation(EresseaConstants.O_REGION),
										   " \"\"", 9, 1));
		}

		if((unit.getShip() != null) && (unit.getShip().getOwnerUnit() != null) &&
			   unit.getShip().getOwnerUnit().equals(unit)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   " \"\"", 9, 1));
		}
	}

	void cmpltBetrete() {
		Iterator iter = region.buildings().iterator();

		if(iter.hasNext()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
										   " ", 7));
		}

		for(; iter.hasNext();) {
			UnitContainer uc = (UnitContainer) iter.next();

			if(!uc.equals(unit.getBuilding())) {
				completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")",
											   Resources.getOrderTranslation(EresseaConstants.O_CASTLE) +
											   " " + uc.getID() + " ;" + uc.getName(), "", 8));
				completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")",
											   Resources.getOrderTranslation(EresseaConstants.O_CASTLE) +
											   " " + uc.getID() + " ;" + uc.getName(), "", 9));
			}
		}

		iter = region.ships().iterator();

		if(iter.hasNext()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   " ", 7));
		}

		for(; iter.hasNext();) {
			UnitContainer uc = (UnitContainer) iter.next();

			if(!uc.equals(unit.getShip())) {
				completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")",
											   Resources.getOrderTranslation(EresseaConstants.O_SHIP) +
											   " " + uc.getID() + " ;" + uc.getName(), "", 8));
				completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")",
											   Resources.getOrderTranslation(EresseaConstants.O_SHIP) +
											   " " + uc.getID() + " ;" + uc.getName(), "", 9));
			}
		}
	}

	void cmpltBetreteBurg() {
		for(Iterator iter = region.buildings().iterator(); iter.hasNext();) {
			UnitContainer uc = (UnitContainer) iter.next();

			if(!uc.equals(unit.getBuilding())) {
				completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")",
											   uc.getID().toString() + " ;" + uc.getName(), "", 8));
				completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")",
											   uc.getID().toString() + " ;" + uc.getName(), "", 9));
			}
		}
	}

	void cmpltBetreteSchiff() {
		for(Iterator iter = region.ships().iterator(); iter.hasNext();) {
			UnitContainer uc = (UnitContainer) iter.next();

			if(!uc.equals(unit.getShip())) {
				completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")",
											   uc.getID().toString() + " ;" + uc.getName(), "", 8));
				completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")",
											   uc.getID().toString() + " ;" + uc.getName(), "", 9));
			}
		}
	}

	void cmpltBotschaft() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
									   Resources.getOrderTranslation(EresseaConstants.O_REGION),
									   " \"\"", 9, 1));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
									   " "));
	}

	void cmpltBotschaftEinheit() {
		addRegionUnits(" \"\"", 1);
	}

	void cmpltBotschaftPartei() {
		addOtherFactions(" \"\"", 1);
	}

	void cmpltBotschaftGebaeude() {
		Iterator i = region.buildings().iterator();

		while((i != null) && i.hasNext()) {
			UnitContainer uc = (UnitContainer) i.next();
			String id = uc.getID().toString();
			completions.add(new Completion(uc.getName() + " (" + id + ")", id, " \"\"", 9, 1));
		}
	}

	void cmpltBotschaftGebaeudeID() {
		completions.add(new Completion(" \"\"", " \"\"", "", 9, 1));
	}

	void cmpltBotschaftSchiff() {
		Iterator i = region.ships().iterator();

		while((i != null) && i.hasNext()) {
			UnitContainer uc = (UnitContainer) i.next();
			String id = uc.getID().toString();
			completions.add(new Completion(uc.getName() + " (" + id + ")", id, " \"\"", 9, 1));
		}
	}

	void cmpltBotschaftSchiffID() {
		completions.add(new Completion(" \"\"", " \"\"", "", 9, 1));
	}

	void cmpltDefault() {
		cmplt();
	}

	void cmpltFahre() {
		addRegionUnits("");
	}

	void cmpltFolge() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
									   " "));
	}

	void cmpltFolgeEinheit() {
		addRegionUnits("");
	}

	void cmpltFolgeSchiff() {
		if(region != null) {
			Iterator i = region.ships().iterator();

			while(i.hasNext()) {
				Ship s = (Ship) i.next();
				
				int prio = 0;
				// stm 2007-03-11: follow ships, no matter who's the owner
				if((s.getOwnerUnit() != null) &&
					   (unit.getFaction().equals(s.getOwnerUnit().getFaction()))) {
					prio = 16;
				}
				String id = s.getID().toString();
				String name = s.getName();

				if(name != null) {
					completions.add(new Completion(name + " (" + id + ")", id, " ", prio+8));
					completions.add(new Completion(id + " (" + name + ")", id, " ", prio));
				} else {
					completions.add(new Completion(id, " ", prio));
				}
			}
			
			// add ships from DURCHSCHIFFUNG
			for (Iterator<Message> messages =  region.getTravelThruShips().iterator(); messages.hasNext();){
				String text = messages.next().getText();
				
				// try to match a ship id in the text
				// TODO: use message type 
				String number = "\\w+";
				Matcher matcher = Pattern.compile("\\(("+number+")\\)").matcher(text);
				while(matcher.find()){
					if (1<=matcher.groupCount()){
						String id = matcher.group(1);
						completions.add(new Completion(text, id, " ", 8));
						completions.add(new Completion(id + " (" + text + ")", id, " "));
					}
				}
			}
		}
	}

	void cmpltForsche() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
	}

	void cmpltGruppe() {
		if((unit != null) && (unit.getFaction() != null) && (unit.getFaction().getGroups() != null)) {
			for(Iterator iter = unit.getFaction().getGroups().values().iterator(); iter.hasNext();) {
				Group g = (Group) iter.next();
				completions.add(new Completion(g.getName(), "\"" + g.getName() + "\"", ""));
			}
		}
	}

	void cmpltGib() {
		addRegionUnits(" ");
    addRegionShipCommanders(" ");
    addRegionBuildingOwners(" ");
	}

	void cmpltGibUID() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT)));

		//		if (unit.getBuilding() != null && unit.equals(unit.getBuilding().getOwnerUnit()) ||
		//			unit.getShip() != null && unit.equals(unit.getShip().getOwnerUnit()))
		//		{
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CONTROL)));

		//		}
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EACH)
				+ " " + Resources.getOrderTranslation(EresseaConstants.O_AMOUNT), Resources
				.getOrderTranslation(EresseaConstants.O_EACH), " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AMOUNT), "", ""));
	}

	void cmpltGibJe() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AMOUNT), "", ""));
	}

	/**
	 * For multiple-line-completion like the creation of give-orders for the resources of an item
	 * it is necessary to get the unit's id and the amount to be given. They are given as
	 * parameters:
	 *
	 * @param uid the unit's id
	 * @param i the amount
	 * @param persons Whether to add "PERSONEN" or not
	 */
	void cmpltGibUIDAmount(UnitID uid, int i, boolean persons) {
		addUnitItems("");

		// add completions, that create multiple Give-Orders for the resources of an item
		if((i != 0) && (uid != null)) {
			for(Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
				ItemType iType = (ItemType) iter.next();

				if(iType.getResources().hasNext() // necessary resources are known
					    &&checkForMaterials(iType.getResources(), i)) { // necessary resources are available

					boolean suggest = true;
					int loopCount = 0;
					String order = "";

					for(Iterator iterator = iType.getResources(); iterator.hasNext() && suggest;
							loopCount++) {
						Item resource = (Item) iterator.next();

						if((loopCount == 0) && !iterator.hasNext()) {
							// only one resource is necessary for this ItemType
							// don't create a completion to give the resource for this ItemType
							suggest = false;
						} else {
							String resourcename = resource.getName();

							if((resourcename.indexOf(" ") > -1)) {
								resourcename = "\"" + resourcename + "\"";
							}

							if("".equals(order)) {
								order += resourcename;
							} else {
								order += ("\n" +
								Resources.getOrderTranslation(EresseaConstants.O_GIVE) + " " +
								uid.toString() + " " + i + " " + resourcename);
							}
						}
					}

					if(suggest) {
						completions.add(new Completion("R-" + iType.getName(), order, "", 10));
					}
				}
			}
		}

		if (persons)
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MEN)));

	}

	void cmpltGibUIDAmount() {
		cmpltGibUIDAmount(null, 0, true);
	}

	void cmpltGibUIDAlles() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_MEN)));
		addUnitItems("");
	}

	void cmpltHelfe() {
		addOtherFactions(" ");
	}

	void cmpltHelfeFID() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GUARD),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_GIVE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMBAT),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SILVER),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTIONSTEALTH),
									   " "));
	}

	void cmpltHelfeFIDModifier() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
	}

	void cmpltKaempfe() {
		if((unit == null) || ((unit.getCombatStatus() != 0) && (unit.getCombatStatus() != -1))) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AGGRESSIVE)));
		}

		if((unit == null) || (unit.getCombatStatus() != 2)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REAR)));
		}

		if((unit == null) || (unit.getCombatStatus() != 3)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_DEFENSIVE)));
		}

		if((unit == null) || (unit.getCombatStatus() != 4)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
		}

		if((unit == null) || (unit.getCombatStatus() != 5)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FLEE)));
		}

		// ACHTUNG!!!!
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HELP_COMBAT),
									   " "));
	}

	void cmpltKaempfeHelfe() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
	}

	void cmpltKaufe() {
		completions.add(new Completion(region.maxLuxuries() + "", " "));
	}

	void cmpltKaufeAmount() {
		String item = null;

		if(region.getPrices() != null) {
			for(Iterator<LuxuryPrice> iter = region.getPrices().values().iterator(); iter.hasNext();) {
				LuxuryPrice p = iter.next();

				if(p.getPrice() < 0) {
					item = p.getItemType().getName();

					break;
				}
			}
		}

		if(item == null) {
			if((data != null) && (data.rules != null)) {
				ItemCategory luxCat = data.rules.getItemCategory(EresseaConstants.C_LUXURIES);

				if(luxCat != null) {
					for(Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
						ItemType t = (ItemType) iter.next();

						if(t.getCategory().equals(luxCat)) {
							completions.add(new Completion(t.getName()));
						}
					}
				}
			}
		} else {
			completions.add(new Completion(item));
		}
	}

	void cmpltKampfzauber() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
										   " ", 8));

			if((unit.getCombatSpells() != null) && (unit.getCombatSpells().size() > 0)) {
				completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT),
											   "", 8));
			}

			addFilteredSpells(unit.getSpells().values(), false,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  true);
		}
	}

	void cmpltKampfzauberStufe() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			addFilteredSpells(unit.getSpells().values(), false,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  true);
		}
	}

	void cmpltKampfzauberSpell() {
		if((unit.getCombatSpells() != null) && (unit.getCombatSpells().size() > 0)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT),
										   ""));
		}
	}

	void cmpltKontaktiere() {
		Alliance alliance = new Alliance(unit.getFaction());
		alliance.setState(EresseaConstants.A_GIVE);
		alliance.setState(EresseaConstants.A_GUARD);
		addNotAlliedUnits(alliance, "");
	}

	void cmpltLehre() {
		addRegionUnits(" ");
	}

	void cmpltLerne() {
		if((data != null) && (data.rules != null)) {
			for(Iterator iter = data.rules.getSkillTypeIterator(); iter.hasNext();) {
				SkillType t = (SkillType) iter.next();
				int cost = getSkillCost(t, unit);

				if(cost > 0) {
					completions.add(new Completion(t.getName(), " " + cost));
				} else {
					completions.add(new Completion(t.getName()));
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param skillType the skill to be learned
	 * @param unit the Unit
	 *
	 * @return the cost to learn a skill for the given unit. If the unit has no persons the cost
	 * 		   for one person is returned.
	 */
	public int getSkillCost(SkillType skillType, Unit unit) {
		int cost = 0;

		if(skillType.getID().equals(EresseaConstants.S_TAKTIK) ||
			   skillType.getID().equals(EresseaConstants.S_KRAEUTERKUNDE) ||
			   skillType.getID().equals(EresseaConstants.S_ALCHEMIE)) {
			cost = 200;
		} else if(skillType.getID().equals(EresseaConstants.S_SPIONAGE)) {
			cost = 100;
		} else if(skillType.getID().equals(EresseaConstants.S_MAGIE)) {
			// get magiclevel without modifier
			int level = 0;
			Skill skill = (unit != null) ? unit.getSkill(skillType) : null;

			if(skill != null) {
				if(skill.noSkillPoints()) {
					level = skill.getLevel() - skill.getModifier(unit);
				} else {
					int days = unit.getSkill(skillType).getPointsPerPerson();
					level = (int) Math.floor(Math.sqrt((days / 15.0) + 0.25) - 0.5);
				}
			}

			int nextLevel = level + 1;
			cost = (int) (50 + ((50 * (1 + nextLevel) * (nextLevel)) / 2.0));
		}

		if(unit != null) {
			if((unit.getModifiedBuilding() != null) &&
			   unit.getModifiedBuilding().getType().equals(data.rules.getBuildingType(StringID.create("Akademie")))) {
				if(cost == 0) {
					cost = 50;
				} else {
					cost *= 2;
				}
			}

			cost *= Math.max(1, unit.getModifiedPersons());
		}

		return cost;
	}

	void cmpltLiefere() {
		cmpltGib();
	}

	void cmpltLocale() {
		completions.add(new Completion("deutsch", "\"de\"", ""));
		completions.add(new Completion("english", "\"en\"", ""));
	}

	void cmpltMache() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEMP),
									   " "));
		cmpltMacheAmount();
	}

	void cmpltMacheAmount() {
		// buildings
		if(hasSkill(unit, EresseaConstants.S_BURGENBAU)) {
			if((data != null) && (data.rules != null)) {
				for(Iterator iter = data.rules.getBuildingTypeIterator(); iter.hasNext();) {
					BuildingType t = (BuildingType) iter.next();

					if((t instanceof CastleType == false) &&
						   t.containsRegionType(region.getRegionType()) &&
						   hasSkill(unit, EresseaConstants.S_BURGENBAU, t.getMinSkillLevel()) &&
						   (!completerSettingsProvider.getLimitMakeCompletion() ||
						   checkForMaterials(t.getRawMaterials()))) {
						completions.add(new Completion(t.getName(), " "));
					}
				}
			}

			if(!completerSettingsProvider.getLimitMakeCompletion() ||
				   (region.getItem(data.rules.getItemType(StringID.create("Stein"))) != null)) {
				completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE),
											   " "));
			}
		}

		// ships
		if(hasSkill(unit, EresseaConstants.S_SCHIFFBAU) &&
			   (!completerSettingsProvider.getLimitMakeCompletion() ||
			   (region.getItem(data.rules.getItemType(StringID.create("Holz"))) != null))) {
			if((data != null) && (data.rules != null)) {
				for(Iterator iter = data.rules.getShipTypeIterator(); iter.hasNext();) {
					ShipType t = (ShipType) iter.next();

					if(hasSkill(unit, EresseaConstants.S_SCHIFFBAU, t.getBuildLevel())) {
						completions.add(new Completion(t.getName(), " "));
					}
				}
			}

			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP),
										   " "));
		}

		// streets
		// check, if there is the necessary roadsupportbuilding
		BuildingType b = region.getRegionType().getRoadSupportBuilding();
		boolean canMake = false;

		if(b == null) {
			canMake = true;
		} else {
			for(Iterator iter = region.buildings().iterator(); iter.hasNext() && !canMake;) {
				if(((Building) iter.next()).getBuildingType().equals(b)) {
					canMake = true;
				}
			}
		}

		if(hasSkill(unit, EresseaConstants.S_STRASSENBAU) &&
			   (!completerSettingsProvider.getLimitMakeCompletion() ||
			   (region.getItem(data.rules.getItemType(StringID.create("Stein"))) != null)) &&
			   canMake) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROAD),
										   " "));
		}

		// items
		for(Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
			ItemType itemType = (ItemType) iter.next();
			canMake = true;

			if(itemType.getMakeSkill() == null) {
				// some items can not be made like dragonblood or magic artefacts
				canMake = false;
			} else if(!hasSkill(unit, itemType.getMakeSkill().getSkillType().getID(),
									itemType.getMakeSkill().getLevel())) {
				canMake = false;
			} else if(completerSettingsProvider.getLimitMakeCompletion() &&
						  !checkForMaterials(itemType.getResources())) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Eisen"))) &&
						  (region.getIron() <= 0)) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Laen"))) &&
						  (region.getLaen() <= 0)) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Holz"))) &&
						  // bugzilla enhancement 599: also allow completion on sprouts
				// also take care of mallorn flag
				(((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || region.isMallorn())) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Mallorn"))) &&
						  // bugzilla enhancement 599: also allow completion on sprouts
				(((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || !region.isMallorn())) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Pferd"))) &&
						  (region.getHorses() <= 0)) {
				canMake = false;
			} else if(itemType.equals(data.rules.getItemType(StringID.create("Stein"))) &&
						  (region.getStones() <= 0)) {
				canMake = false;
			}

			if(canMake) {
				addItem(itemType, "");
			}
		}
	}

	void cmpltMacheTemp() {
	}

	void cmpltMacheTempID() {
		completions.add(new Completion("\""));
	}

	void cmpltMacheBurg() {
		Iterator i = region.buildings().iterator();

		while((i != null) && i.hasNext()) {
			Building b = (Building) i.next();
			BuildingType type = b.getBuildingType();

			if(type instanceof CastleType || (type.getMaxSize() != b.getSize())) {
				String id = b.getID().toString();
				completions.add(new Completion(b.getName() + " (" + id + ")", id, ""));
			}
		}
	}

	void cmpltMacheBuilding(String typeName) {
		// TODO(pavkovic): korrigieren!!! Hier soll eigentlich das Geb�ude �ber den 
		// �bersetzten Namen gefunden werden!!!
		// BuildingType type = ((Eressea) data.rules).getBuildingType(typeName);
		BuildingType type = data.rules.getBuildingType(StringID.create(typeName));

		if(type != null) {
			Iterator i = region.buildings().iterator();

			while((i != null) && i.hasNext()) {
				UnitContainer uc = (UnitContainer) i.next();

				if(uc.getType().equals(type)) {
					String id = uc.getID().toString();
					completions.add(new Completion(uc.getName() + " (" + id + ")", id, ""));
				}
			}
		}
	}

	void cmpltMacheSchiff() {
		Faction ownerFaction = unit.getFaction();
		Iterator i = region.ships().iterator();

		while((i != null) && i.hasNext()) {
			Ship s = (Ship) i.next();
			String id = s.getID().toString();

			if((s.getOwnerUnit() != null) && ownerFaction.equals(s.getOwnerUnit().getFaction())) {
				completions.add(new Completion(s.getName() + " (" + id + ")", id, "", 8));
				completions.add(new Completion(id + " (" + s.getName() + ")", id, "", 8));
			} else {
				completions.add(new Completion(s.getName() + " (" + id + ")", id, ""));
				completions.add(new Completion(id + " (" + s.getName() + ")", id, ""));
			}
		}
	}

	void cmpltMacheStrasse() {
		addDirections("");
	}

	void cmpltNach() {
		addDirections(" ");
		addSurroundingRegions(unit.getRadius(), " ");
	}

	void cmpltNummer() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_UNIT)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SHIP)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_CASTLE)," "));
	}

	void cmpltOption() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ADDRESSES)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REPORT)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BZIP2)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_COMPUTER)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ITEMPOOL)," "));
    completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SCORE)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SILVERPOOL)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_STATISTICS)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ZIPPED)," "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_TEMPLATE)," "));
	}

	void cmpltOptionOption() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
	}

	void cmpltPiraterie() {
		addOtherFactions(" ");
	}

	void cmpltPiraterieFID() {
		cmpltPiraterie();
	}

	void cmpltPraefix() {
		completions.add(new Completion("\"\"", "\"\"", "", 9, 1));
	}

	void cmpltReserviere() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_EACH),
				   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AMOUNT), "", ""));
	}

	void cmpltReserviereJe() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AMOUNT), "", ""));
	}

	void cmpltReserviereAmount() {
		Faction f = unit.getFaction();
		boolean silverPool = f.getOptions().isActive(StringID.create(EresseaConstants.O_SILVERPOOL));
		boolean materialPool = f.getOptions().isActive(StringID.create(EresseaConstants.O_ITEMPOOL));

		if(!silverPool && !materialPool) {
			addUnitItems("");
		} else if(silverPool && !materialPool) {
			addUnitItems("");

			// if unit doesn't have silver, but poolsilver is available
			if((unit.getItem(data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) == null) &&
				   (region.getItem(data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) != null)) {
				completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SILVER)));
			}
		} else if(!silverPool && materialPool) {
			for(Iterator iter = region.items().iterator(); iter.hasNext();) {
				Item item = (Item) iter.next();

				if(silverPool ||
					   (item.getItemType() != data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) ||
					   (unit.getItem(data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) != null)) {
					String name = item.getName();
					String quotedName = name;

					if((name.indexOf(" ") > -1)) {
						quotedName = "\"" + name + "\"";
					}

					completions.add(new Completion(name, quotedName, ""));
				}
			}
		} else {
			for(Iterator iter = region.items().iterator(); iter.hasNext();) {
				Item item = (Item) iter.next();

				// silver only if silverpool activated or unit has silver
				if(silverPool ||
					   (item.getItemType() != data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) ||
					   (unit.getItem(data.rules.getItemType(StringID.create(EresseaConstants.O_SILVER))) != null)) {
					String name = item.getName();
					String quotedName = name;

					if((name.indexOf(" ") > -1)) {
						quotedName = "\"" + name + "\"";
					}

					completions.add(new Completion(name, quotedName, ""));
				}
			}
		}
	}

	void cmpltRoute() {
		addDirections(" ");
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_PAUSE),
									   " "));
	}

	void cmpltSortiere() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_BEFORE),
									   " "));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_AFTER),
									   " "));
	}

	void cmpltSortiereVor() {
		if(unit.getBuilding() != null) {
			addSortiereUnits(unit, unit.getBuilding(), false);
		} else if(unit.getShip() != null) {
			addSortiereUnits(unit, unit.getShip(), false);
		} else {
			for(Iterator iter = region.units().iterator(); iter.hasNext();) {
				Unit u = (Unit) iter.next();

				if(unit.getFaction().equals(u.getFaction()) && (u.getBuilding() == null) &&
					   (u.getShip() == null)) {
					if(!u.equals(unit)) {
						addUnit(u, "");
					}
				}
			}
		}
	}

	void cmpltSortiereHinter() {
		if(unit.getBuilding() != null) {
			addSortiereUnits(unit, unit.getBuilding(), true);
		} else if(unit.getShip() != null) {
			addSortiereUnits(unit, unit.getShip(), true);
		} else {
			for(Iterator iter = region.units().iterator(); iter.hasNext();) {
				Unit u = (Unit) iter.next();

				if(unit.getFaction().equals(u.getFaction()) && (u.getBuilding() == null) &&
					   (u.getShip() == null)) {
					if(!u.equals(unit)) {
						addUnit(u, "");
					}
				}
			}
		}
	}

	private void addSortiereUnits(Unit u, UnitContainer c, boolean addOwner) {
		for(Iterator iter = c.units().iterator(); iter.hasNext();) {
			Unit currentUnit = (Unit) iter.next();

			if(u.getFaction().equals(currentUnit.getFaction()) &&
				   (c.equals(currentUnit.getBuilding()) || c.equals(currentUnit.getShip()))) {
				if(!u.equals(currentUnit) && (addOwner || !currentUnit.equals(c.getOwnerUnit()))) {
					addUnit(currentUnit, "");
				}
			}
		}
	}

	void cmpltSpioniere() {
		addEnemyUnits("");
	}

	void cmpltStirb() {
		if((unit.getFaction() != null) && (unit.getFaction().getPassword() != null)) {
			completions.add(new Completion('"' + unit.getFaction().getPassword() + '"', ""));
		}
	}

	void cmpltTarne() {
		if(unit.isHideFaction()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION) +
										   " " +
										   Resources.getOrderTranslation(EresseaConstants.O_NOT)));
		} else {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
										   " "));
		}

		if((data != null) && (data.rules != null)) {
			Race demons = data.rules.getRace(EresseaConstants.R_DAEMONEN);

			if((demons == null) || (unit.getRealRace() == null) || unit.getRealRace().equals(demons)) {
				for(Iterator iter = data.rules.getRaceIterator(); iter.hasNext();) {
					Race r = (Race) iter.next();
					completions.add(new Completion(r.getName()));
				}
			}
		}

		completions.add(new Completion(EresseaConstants.O_NOT));
	}

	void cmpltTarnePartei() {
		if(unit.isHideFaction()) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NOT)));
		}

		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_NUMBER),
									   " "));
	}

	void cmpltTarneParteiNummer() {
		log.info("cmplt nummer");
		addFactions("");
	}

	void cmpltTransportiere() {
		addRegionUnits("");
	}

	void cmpltVergesse() {
		for(Iterator i = unit.getSkills().iterator(); i.hasNext();) {
			completions.add(new Completion(((Skill) i.next()).getName(), ""));
		}
	}

	void cmpltVerkaufe() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
	}

	void cmpltVerkaufeAmount() {
		addUnitLuxuries("");
	}

	void cmpltVerkaufeAlles() {
		addUnitLuxuries("");
	}

	void cmpltZaubere() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_REGION),
										   " ", 8));
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
										   " ", 8));
			addFilteredSpells(unit.getSpells().values(), false,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  false);
		}
	}

	void cmpltZaubereStufe() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			addFilteredSpells(unit.getSpells().values(), false,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  false);
		}
	}

	void cmpltZaubereRegion() {
		Map regions1 = Regions.getAllNeighbours(data.regions(), region.getID(), 1, null);
		Map regions2 = Regions.getAllNeighbours(data.regions(), region.getID(), 2, null);

		// first add all regions within a radius of 1 and remove them from Map regions2
		for(Iterator iter = regions1.keySet().iterator(); iter.hasNext();) {
			CoordinateID c = (CoordinateID) iter.next();

			if(!c.equals(region.getCoordinate())) {
				Region r = (Region) regions1.get(c);
				String name = r.getName();
				int prio = 7;

				if(name == null) {
					name = c.toString();
					prio = 8;
				}

				// FIXME(pavkovic, 2004.06.09): this seems to be wrong 
				// Coordinate distance = region.getCoordinate().createDistanceCoordinate(c);
				// completions.add(new Completion(name, distance.toString(" "), " ", prio));
				// We should store the translation while merging different cr files
				completions.add(new Completion(name, c.toString(" "), " ", prio));
			}

			regions2.remove(c);
		}

		for(Iterator iter = regions2.keySet().iterator(); iter.hasNext();) {
			ID c = (ID) iter.next();
			Region r = (Region) regions2.get(c);
			String name = r.getName();
			int prio = 9;

			if(name == null) {
				name = c.toString(" ");
				prio = 10;
			}

			completions.add(new Completion(name, c.toString(" "), " ", prio));
		}
	}

	void cmpltZaubereRegionCoor() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_LEVEL),
										   " ", 8));
			addFilteredSpells(unit.getSpells().values(), true,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  false);
		}
	}

	void cmpltZaubereRegionStufe() {
		if((unit.getSpells() != null) && (unit.getSpells().size() > 0)) {
			addFilteredSpells(unit.getSpells().values(), true,
							  region.getType().equals(data.rules.getRegionType(EresseaConstants.RT_OCEAN)),
							  false);
		}
	}

	/**
	 * adds the given spells if combat, only adds combat-spells and so on
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	private void addFilteredSpells(Collection spells, boolean far, boolean ocean, boolean combat) {
		for(Iterator iter = spells.iterator(); iter.hasNext();) {
			Spell spell = (Spell) iter.next();

			if((spell.getDescription() == null) // indicates that no information is available about this spell
				    ||((spell.getIsFar() || !far) && (spell.getOnOcean() || !ocean) &&
				   (!combat || (spell.getType().toLowerCase().indexOf("combat") > -1)))) {
				String spellName = this.data.getTranslation(spell);
				String quotedSpellName = spellName;

				if(spellName.indexOf(" ") > -1) {
					quotedSpellName = "\"" + spellName + "\"";
				}

				completions.add(new Completion(spellName, quotedSpellName, " "));
			}
		}
	}

	void cmpltZeige() {
		addUnitItems("");
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ALL), " "));
	}

	void cmpltZeigeAlle() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_POTIONS)));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_SPELLS)));
	}

	void cmpltZerstoere() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_ROAD),
									   " "));
	}

	void cmpltZerstoereStrasse() {
		if(region != null) {
			for(Iterator iter = region.borders().iterator(); iter.hasNext();) {
				Border b = (Border) iter.next();

				if(Umlaut.convertUmlauts(b.getType()).equalsIgnoreCase(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
					completions.add(new Completion(Direction.toString(b.getDirection()), ""));
				}
			}
		} else {
			addDirections("");
		}
	}

	void cmpltZuechte() {
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HORSES)));
		completions.add(new Completion(Resources.getOrderTranslation(EresseaConstants.O_HERBS)));
	}

	/**
	 * adds all units in this region whose faction has a trustlevel not greater than zero
	 * (TL_DEFAULT)
	 *
	 * 
	 */
	private void addEnemyUnits(String postfix) {
		if((data != null) && (unit != null) && (region != null)) {
			Iterator units = region.units().iterator();

			while(units.hasNext() == true) {
				Unit u = (Unit) units.next();

				if((u.getFaction().getTrustLevel() <= Faction.TL_DEFAULT) || u.isSpy()) {
					addUnit(u, postfix);
				}
			}
		}
	}

	/**
	 * adds all units in this region, whose faction does not fit all of the alliances in the given
	 * Alliance-Object. Example: Given Alliance contains help and give: units are added if they
	 * are not allied both: help AND give. The reference-object is the faction of the current unit
	 *
	 * 
	 * 
	 */
	private void addNotAlliedUnits(Alliance alliance, String postfix) {
		for(Iterator<Unit> iter = region.units().iterator(); iter.hasNext();) {
			Unit curUnit = iter.next();
			Faction f = curUnit.getFaction();

			// search for alliances
			if(f == null) {
				addUnit(curUnit, postfix);
			} else if(!f.equals(unit.getFaction())) {
				Alliance testAlliance = unit.getFaction().getAllies().get(f.getID());

				if(testAlliance == null) {
					// curUnit is not allied
					addUnit(curUnit, postfix);
				} else {
					if((testAlliance.getState() & alliance.getState()) != alliance.getState()) {
						// curUnit doesn't fit all alliance-states and is therefor added
						addUnit(curUnit, postfix);
					}
				}
			}
		}
	}

	private void addRegionUnits(String postfix) {
		addRegionUnits(postfix, 0);
	}

	private void addRegionUnits(String postfix, int cursorOffset) {
		if(region != null) {
			Iterator<Unit> units = region.units().iterator();

			while(units.hasNext() == true) {
				Unit u = units.next();

				if((unit == null) || !u.equals(unit)) {
					addUnit(u, postfix, cursorOffset);
				}
			}
		}
	}


  private void addRegionShipCommanders(String postfix) {
    addRegionShipCommanders(postfix, 0);
  }
  
  private void addRegionShipCommanders(String postfix, int cursorOffset) {
    if(region != null) {
      Iterator<Ship> ships = region.ships().iterator();
      while(ships.hasNext() == true){
        Ship s = ships.next();
        if (s!=null){
          Unit u = s.getOwnerUnit();
          if (u!=null){
            if((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(s,u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }
  
  private void addUnitContainerOwner(UnitContainer s,Unit u,String postfix, int cursorOffset){
    String id = u.getID().toString();
    
    completions.add(new Completion(s.toString() + " (" + s.getID() + ")", id, postfix, 10, cursorOffset));
    completions.add(new Completion(s.getID() + " (" + s.toString() + ")", id, postfix, 11, cursorOffset));
  }

  private void addRegionBuildingOwners(String postfix) {
    addRegionBuildingOwners(postfix, 0);
  }
  
  private void addRegionBuildingOwners(String postfix, int cursorOffset) {
    if(region != null) {
      Iterator<Building> buildings = region.buildings().iterator();
      while(buildings.hasNext() == true){
        Building b = buildings.next();
        if (b!=null){
          Unit u = b.getOwnerUnit();
          if (u!=null){
            if((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(b,u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }
    
	private void addUnitItems(String postfix) {
		for(Iterator<Item> items = unit.getItems().iterator(); items.hasNext();) {
			Item i = items.next();
			String name = i.getName();
			
			if(name != null) {
				if(name.indexOf(" ") > -1) {
					completions.add(new Completion(name, "\"" + name + "\"", postfix));
				} else {
					completions.add(new Completion(name, postfix));
				}
			}
		}
	}

	private void addFactions(String postfix) {
		if(data != null) {
			for(Iterator<Faction> iter = data.factions().values().iterator(); iter.hasNext(); ) {
				Faction f = iter.next();
				String id = f.getID().toString();
				
				if(f.getName() != null) {
					completions.add(new Completion(f.getName() + " (" + id + ")", id, postfix, 8));
					completions.add(new Completion(id + " (" + f.getName() + ")", id, postfix, 9));
				} else {
					completions.add(new Completion(id, id, postfix, 9));
				}
			}
		}
	}

	private void addOtherFactions(String postfix) {
		addOtherFactions(postfix, 0);
	}

	private void addOtherFactions(String postfix, int cursorOffset) {
		Faction ownerFaction = unit.getFaction();
		Iterator<Faction> factions = data.factions().values().iterator();

		while((factions != null) && factions.hasNext()) {
			Faction f = factions.next();

			if((ownerFaction == null) || (f.equals(ownerFaction) == false)) {
				String id = f.getID().toString();

				if(f.getName() != null) {
					completions.add(new Completion(f.getName() + " (" + id + ")", id, postfix, 8,
												   cursorOffset));
					completions.add(new Completion(id + " (" + f.getName() + ")", id, postfix, 9,
												   cursorOffset));
				} else {
					completions.add(new Completion(id, id, postfix, 9, cursorOffset));
				}
			}
		}
	}

	private void addSurroundingRegions(int radius, String postfix) {
		if(radius < 1) {
			radius = 1;
		}

		RegionType oceanType = data.rules.getRegionType(EresseaConstants.RT_OCEAN);

		if(oceanType == null) {
			log.warn("EresseaOrderCompleter.addSurroundingRegions(): unable to retrieve ocean region type from rules!");

			return;
		}

		Map<ID,RegionType> excludedRegionTypes = new Hashtable<ID, RegionType>();
		excludedRegionTypes.put(oceanType.getID(), oceanType);

		Map<CoordinateID,Region> neighbours = Regions.getAllNeighbours(data.regions(), region.getID(), radius,
												  excludedRegionTypes);

		// do not include the region the unit stays in
		neighbours.remove(region.getID());

		for(Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
			Region r = iter.next();

			if((region != null) && !region.equals(r)) {
				// get a path from the current region to neighbouring
				// translate the path of regions into a string of
				// directions to take
				String directions = Regions.getDirections(data.regions(), region.getID(), r.getID(), excludedRegionTypes);

				if(directions != null) {
					completions.add(new Completion(r.getName(), directions, postfix));
				}
			}
		}
	}

	private void addDirections(String postfix) {
		for(Iterator iter = Direction.getShortNames().iterator(); iter.hasNext();) {
			String dir = (String) iter.next();
			completions.add(new Completion(dir, dir, postfix));
		}

		for(Iterator iter = Direction.getLongNames().iterator(); iter.hasNext();) {
			String dir = (String) iter.next();
			completions.add(new Completion(dir, dir, postfix));
		}
	}

	private void addUnitLuxuries(String postfix) {
		ItemCategory cat = null;

		if((data != null) && (data.rules != null)) {
			cat = data.rules.getItemCategory(EresseaConstants.C_LUXURIES);
		}

		if((cat != null) && (unit != null)) {
			for(Iterator items = unit.getModifiedItems().iterator(); items.hasNext();) {
				Item i = (Item) items.next();
				
				if((i.getItemType().getCategory() != null) &&
				   i.getItemType().getCategory().equals(cat)) {
					completions.add(new Completion(i.getName(), postfix));
				}
			}
		}
	}

	private void addUnit(Unit u, String postfix) {
		addUnit(u, postfix, 0);
	}

	/**
	 * Adds a unit to the completion in a standard manner.
	 *
	 * 
	 * 
	 * 
	 */
	private void addUnit(Unit u, String postfix, int cursorOffset) {
		String id = u.getID().toString();

		if(u instanceof TempUnit) {
			completions.add(new Completion("TEMP " + id, "TEMP " + id, postfix, 8, cursorOffset));
		} else {
			String name = u.getName();

			if(name != null) {
				completions.add(new Completion(name + " (" + id + ")", id, postfix, 8, cursorOffset));
				completions.add(new Completion(id + " (" + name + ")", id, postfix, 9, cursorOffset));
			} else {
				completions.add(new Completion(id, postfix));
			}
		}
	}

	/**
	 * Check for the necessary materials to produce an item considering all privileged factions in
	 * the current region
	 *
	 * @param iter An Iterator over the necessary materials (Items)
	 *
	 * @return true, if the necessary materials are available, false otherwise
	 */
	private boolean checkForMaterials(Iterator iter) {
		return checkForMaterials(iter, 1);
	}

	/**
	 * Check for the necessary materials to produce an item considering all privileged factions in
	 * the current region
	 *
	 * @param iter An Iterator over the necessary materials (Items)
	 * @param amount A multiplicator
	 *
	 * @return true, if the necessary materials are available, false otherwise
	 */
	private boolean checkForMaterials(Iterator iter, int amount) {
		boolean canMake = true;

		while(iter.hasNext() && canMake) {
			Item ingredient = (Item) iter.next();

			// be careful, units cannot own peasants although one is required for the potion "Bauernblut"
			if(ingredient.getItemType() != null) {
				int availableAmount = 0;

				if(ingredient.getItemType().equals(data.rules.getItemType(StringID.create("Bauer")))) {
					availableAmount = region.getPeasants();
				} else {
					Item available = region.getItem(ingredient.getItemType());

					if(available != null) {
						availableAmount = available.getAmount();
					}
				}

				if(availableAmount < (ingredient.getAmount() * amount)) {
					canMake = false;
				}
			}
		}

		return canMake;
	}

	/**
	 * Returns the last word immediately at the end of the String txt.
	 *
	 * 
	 *
	 * 
	 */
	private String getStub(String txt) {
		StringBuffer retVal = new StringBuffer();

		for(int i = txt.length() - 1; i >= 0; i--) {
			char c = txt.charAt(i);

			if((c == '"') || (c == '-') || (c == '_') || (Character.isLetterOrDigit(c) == true)) {
				retVal.append(c);
			} else {
				break;
			}
		}

		return retVal.reverse().toString();
	}

	/**
	 * Determines whether the specified unit has a skill.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	private boolean hasSkill(Unit u, ID id) {
		return hasSkill(u, id, 1);
	}

	/**
	 * Determines whether the specified unit has a skill at a minimum level. Returns also true, if
	 * the specified skill is unknown.
	 * 
	 * FF: changed to reflect modified skill
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	private boolean hasSkill(Unit u, ID id, int level) {
		boolean retVal = false;
		SkillType skillType = data.rules.getSkillType(id);

		if(skillType != null) {
			// Skill e = u.getSkill(skillType);
			Skill e = u.getModifiedSkill(skillType);

			if((e != null) && (e.getLevel() >= level)) {
				retVal = true;
			}
		} else {
			retVal = true;
		}

		return retVal;
	}

	/**
	 * Adds an item by type
	 *
	 * 
	 * 
	 */
	private void addItem(ItemType iType, String postfix) {
		String name = iType.getName();
		String quotedName = name;

		if((name.indexOf(" ") > -1)) {
			quotedName = "\"" + name + "\"";
		}

		completions.add(new Completion(name, quotedName, postfix));
	}

	/**
	 * Case-insensitive comparator for String and/or Completion objects
	 */
	private class IgnrCsComp implements Comparator<Object> {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 *
		 * 
		 */
		public int compare(Object o1, Object o2) {
			int retVal = 0;

			if(o1 instanceof String && o2 instanceof String) {
				retVal = ((String) o1).compareToIgnoreCase((String) o2);
			} else if(o1 instanceof Completion && o2 instanceof Completion) {
				Completion c1 = (Completion) o1;
				Completion c2 = (Completion) o2;

				if(c1.getName() == null) {
					return (c1.getName() == null) ? 0 : 1;
				} else {
					return (c2.getName() == null) ? (-1)
												  : c1.getName().compareToIgnoreCase(c2.getName());
				}
			} else if(o1 instanceof Completion && o2 instanceof String) {
				String s1 = ((Completion) o1).getName();
				String s2 = (String) o2;
				retVal = s1.compareToIgnoreCase(s2);
			} else if(o1 instanceof String && o2 instanceof Completion) {
				String s1 = (String) o1;
				String s2 = ((Completion) o2).getName();
				retVal = s1.compareToIgnoreCase(s2);
			}

			return retVal;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public boolean equals(Object obj) {
			return false;
		}
	}

	/**
	 * Priority comparator for Completion objects
	 */
	private static class PrioComp implements Comparator {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 *
		 * 
		 */
		public int compare(Object o1, Object o2) {
			int retVal = 0;

			if(o1 instanceof Completion && o2 instanceof Completion) {
				Completion c1 = (Completion) o1;
				Completion c2 = (Completion) o2;

				if(c1.getPriority() != c2.getPriority()) {
					retVal = c1.getPriority() - c2.getPriority();
				} else {
					retVal = c1.getName().compareToIgnoreCase(c2.getName());
				}
			} else if(o1 instanceof Completion && o2 instanceof String) {
				retVal = -1;
			} else if(o1 instanceof String && o2 instanceof Completion) {
				retVal = 2;
			}

			return retVal;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public boolean equals(Object obj) {
			return false;
		}
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
	public List<Completion> getCompletions(Unit u, String line, List<Completion> old) {
		if((old == null) || (old.size() == 0)) {
			return this.getCompletions(u, line);
		} else {
			return this.crop(old, line);
		}
	}

	// pavkovic 2003.01.28: this is a Map of the default Resources mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultResources() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("spies", "Traitors/spies");
		}

		return defaultTranslations;
	}
}
