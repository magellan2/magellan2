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

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 396 $
 */
public class EresseaMovementEvaluator implements MovementEvaluator {
	private EresseaMovementEvaluator() {
	}

	private static final EresseaMovementEvaluator singleton = new EresseaMovementEvaluator();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaMovementEvaluator getSingleton() {
		return singleton;
	}

	/**
	 * Returns the maximum payload in GE  100 of this unit when it travels by horse. Horses, carts
	 * and persons are taken into account for this calculation. If the unit has a sufficient skill
	 * in horse riding but there are too many carts for the horses, the weight of the additional
	 * carts are also already considered.
	 *
	 * 
	 *
	 * @return the payload in GE  100, CAP_NO_HORSES if the unit does not possess horses or
	 * 		   CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
	 * 		   horseback.
	 */
	public int getPayloadOnHorse(Unit unit) {
		int capacity = 0;
		int horses = 0;
		Item i = unit.getModifiedItem(new ItemType(EresseaConstants.I_HORSE));

		if(i != null) {
			horses = i.getAmount();
		}

		if(horses <= 0) {
			return CAP_NO_HORSES;
		}

		int skillLevel = 0;
		Skill s = unit.getModifiedSkill(new SkillType(StringID.create("Reiten")));

		if(s != null) {
			skillLevel = s.getLevel();
		}

		if(horses > (skillLevel * unit.getModifiedPersons() * 2)) {
			return CAP_UNSKILLED;
		}

		int carts = 0;
		i = unit.getModifiedItem(new ItemType(EresseaConstants.I_CART));

		if(i != null) {
			carts = i.getAmount();
		}

		int horsesWithoutCarts = horses - (carts * 2);

		Race race = getRace(unit);

		if(horsesWithoutCarts >= 0) {
			capacity = (((carts * 140) + (horsesWithoutCarts * 20)) * 100) -
					   (((int) ((race.getWeight()) * 100)) * unit.getModifiedPersons());
		} else {
			int cartsWithoutHorses = carts - (horses / 2);
			horsesWithoutCarts = horses % 2;
			capacity = (((((carts - cartsWithoutHorses) * 140) + (horsesWithoutCarts * 20)) -
					   (cartsWithoutHorses * 40)) * 100) -
					   (((int) ((race.getWeight()) * 100)) * unit.getModifiedPersons());
		}
		// Fiete 20070421 (Runde 519)
		// GOTS not active when riding! (tested)
		// return respectGOTS(unit, capacity);
		return capacity;
	}

	/**
	 * Returns the maximum payload in GE  100 of this unit when it travels on foot. Horses, carts
	 * and persons are taken into account for this calculation. If the unit has a sufficient skill
	 * in horse riding but there are too many carts for the horses, the weight of the additional
	 * carts are also already considered. The calculation also takes into account that trolls can
	 * tow carts.
	 *
	 * 
	 *
	 * @return the payload in GE  100, CAP_UNSKILLED if the unit is not sufficiently skilled in
	 * 		   horse riding to travel on horseback.
	 */
	public int getPayloadOnFoot(Unit unit) {
		int capacity = 0;
		int horses = 0;
		Item i = unit.getModifiedItem(new ItemType(EresseaConstants.I_HORSE));

		if(i != null) {
			horses = i.getAmount();
		}

		if(horses < 0) {
			horses = 0;
		}

		int skillLevel = 0;
		Skill s = unit.getModifiedSkill(new SkillType(StringID.create("Reiten")));

		if(s != null) {
			skillLevel = s.getLevel();
		}

		if(horses > ((skillLevel * unit.getModifiedPersons() * 4) + unit.getModifiedPersons())) {
			// too many horses
			return CAP_UNSKILLED;
		}

		int carts = 0;
		i = unit.getModifiedItem(new ItemType(EresseaConstants.I_CART));

		if(i != null) {
			carts = i.getAmount();
		}

		if(carts < 0) {
			carts = 0;
		}

		int horsesWithoutCarts = 0;
		int cartsWithoutHorses = 0;

		if(skillLevel == 0) {
			// can't use carts!!!
			horsesWithoutCarts = horses;
			cartsWithoutHorses = carts;
		} else if(carts > (horses / 2)) {
			// too many carts
			cartsWithoutHorses = carts - (horses / 2);
		} else {
			// too many horses (or exactly right number)
			horsesWithoutCarts = horses - (carts * 2);
		}

		Race race = getRace(unit);

		if((race == null) || (race.getID().equals(EresseaConstants.R_TROLLE) == false)) {
			capacity = (((((carts - cartsWithoutHorses) * 140) + (horsesWithoutCarts * 20)) -
					   (cartsWithoutHorses * 40)) * 100) +
					   (((int) (race.getCapacity() * 100)) * unit.getModifiedPersons());
		} else {
			int horsesMasteredPerPerson = (skillLevel * 4) + 1;
			int trollsMasteringHorses = horses / horsesMasteredPerPerson;

			if((horses % horsesMasteredPerPerson) != 0) {
				trollsMasteringHorses++;
			}

			int cartsTowedByTrolls = Math.min((unit.getModifiedPersons() - trollsMasteringHorses) / 4,
											  cartsWithoutHorses);
			int trollsTowingCarts = cartsTowedByTrolls * 4;
			int untowedCarts = cartsWithoutHorses - cartsTowedByTrolls;
			capacity = (((((carts - untowedCarts) * 140) + (horsesWithoutCarts * 20)) -
					   (untowedCarts * 40)) * 100) +
					   (((int) (race.getCapacity() * 100)) * (unit.getModifiedPersons() -
					   trollsTowingCarts));
		}

		return respectGOTS(unit, capacity);
	}

	private int respectGOTS(Unit unit, int capacity) {
		Item gots = unit.getModifiedItem(new ItemType(EresseaConstants.I_GOTS));

		if(gots == null) {
			return capacity;
		}

		int multiplier = Math.max(0, Math.min(unit.getPersons(), gots.getAmount()));
		Race race = getRace(unit);

		if((multiplier == 0) || (race == null)) {
			return capacity;
		}

		// increase capacity by 49*unit.race.capacity per GOTS
		return capacity + (multiplier * (49 * (int) (race.getCapacity() * 100)));
	}

	private Race getRace(Unit unit) {
		Race race = unit.getRace();

		if(unit.getRealRace() != null) {
			race = unit.getRealRace();
		}

		return race;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public int getLoad(Unit unit) {
		return getLoad(unit, unit.getItems());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public int getModifiedLoad(Unit unit) {
		return getLoad(unit, unit.getModifiedItems());
	}

	private int getLoad(Unit unit, Collection items) {
		int load = 0;
		ItemType horse = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_HORSE);
		ItemType cart = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_CART);

		for(Iterator iter = items.iterator(); iter.hasNext();) {
			Item i = (Item) iter.next();

			if(!i.getItemType().equals(horse) && !i.getItemType().equals(cart)) {
				// pavkovic 2003.09.10: only take care about (possibly) modified items with positive amount
				if(i.getAmount() > 0) {
					load += (((int) (i.getItemType().getWeight() * 100)) * i.getAmount());
				}
			}
		}

		return load;
	}
}
