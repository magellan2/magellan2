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
  private static final EresseaMovementEvaluator singleton = new EresseaMovementEvaluator();

  protected EresseaMovementEvaluator() {
	}


	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaMovementEvaluator getSingleton() {
		return EresseaMovementEvaluator.singleton;
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
			return MovementEvaluator.CAP_NO_HORSES;
		}

		int skillLevel = 0;
		Skill s = unit.getModifiedSkill(new SkillType(StringID.create("Reiten")));

		if(s != null) {
			skillLevel = s.getLevel();
		}

		if(horses > (skillLevel * unit.getModifiedPersons() * 2)) {
			return MovementEvaluator.CAP_UNSKILLED;
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
			return MovementEvaluator.CAP_UNSKILLED;
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

	private int getLoad(Unit unit, Collection<Item> items) {
    int load = 0;
		ItemType horse = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_HORSE);
		ItemType cart = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_CART);
    // darcduck 2007-10-31: take care of bags of negative weight
    ItemType bonw = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_BONW);

		for(Item i : items) {
			if(!i.getItemType().equals(horse) && !i.getItemType().equals(cart)) {
				// pavkovic 2003.09.10: only take care about (possibly) modified items with positive amount
				if(i.getAmount() > 0) {
					load += (((int) (i.getItemType().getWeight() * 100)) * i.getAmount());
				}
			}      
      // darcduck 2007-10-31: take care of bags of negative weight
      if(i.getItemType().equals(bonw)) {
        load -= getBonwLoad(unit, items, i);
      }
		}

		return load;
	}
  
  /**
   * Returns the load in GE 100 of the bag of negatvie weight (bonw).
   * This might be 0 if nothing can be stored in the bag up to 200 per bag.
   * Items are only considered to be stored in the bonw if this is set in the rules.
   * ItemType returns this in method isStoreableInBonw()
   *
   * @return the load of the bonw in GE 100. 
   */
  private int getBonwLoad(Unit unit, Collection<Item> items, Item i_bonw) {
    final int I_BONW_CAP = 20000;
    int bonwload = 0;
    int bonwcap = 0;
    
    if (i_bonw != null) {
      bonwcap = i_bonw.getAmount() * I_BONW_CAP;

      for(Item i : items) {
        if (bonwload>=bonwcap) {
          break;
        }
        if ((i.getAmount() > 0)&&(i.getItemType().isStoreableInBonw())) {
          bonwload += (((int) (i.getItemType().getWeight() * 100)) * i.getAmount());
        }
      }
      bonwload = Math.min(bonwcap, bonwload);
    }
    return bonwload;
  }
  
  
  /**
   * The initial weight of the unit as it appear in 
   * the report. This is the eressea version used to 
   * calculate the weight if the information is not available
   * in the report. 
   *
   * @return the weight of the unit in silver (GE 100).
   */
  public int getWeight(Unit unit) {
    return getWeight(unit, unit.getItems(), unit.getPersons());
  }

  /**
   * The modified weight is calculated from the modified number 
   * of persons and the modified items. Due to some eressea 
   * dependencies this is done in this class.
   *
   * @return the modified weight of the unit in silver (GE 100).
   */
  public int getModifiedWeight(Unit unit) {
    return getWeight(unit, unit.getModifiedItems(), unit.getModifiedPersons());
  }
  
  /**
   * DOCUMENT-ME
   *
   * 
   *
   * 
   */
  private int getWeight(Unit unit, Collection<Item> items, int persons) {
    int weight = 0;
    float personWeight = getRace(unit).getWeight();
    // darcduck 2007-10-31: take care of bags of negative weight
    ItemType bonw = unit.getRegion().getData().rules.getItemType(EresseaConstants.I_BONW);
    
    for(Item item : items) {
      // pavkovic 2003.09.10: only take care about (possibly) modified items with positive amount
      if(item.getAmount() > 0) {
        weight += (item.getAmount() * (int) (item.getItemType().getWeight() * 100));
      }
      // darcduck 2007-10-31: take care of bags of negative weight
      if(item.getItemType().equals(bonw)) {
        weight -= getBonwLoad(unit, items, item);
      }
    }

    weight += (persons * (int) (personWeight * 100));

    return weight;
  }
}
