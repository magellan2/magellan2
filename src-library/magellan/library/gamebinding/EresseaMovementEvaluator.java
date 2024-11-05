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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.relation.MovementRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.Race;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: $
 * @version $Revision: 396 $
 */
public class EresseaMovementEvaluator implements MovementEvaluator {
  private static Logger log = Logger.getInstance(EresseaMovementEvaluator.class);

  /** normal walking speed */
  public static final int FOOT_NORMAL_SPEED = 1;
  /** walking speed on road */
  public static final int FOOT_ROAD_SPEED = 2;
  /** normal riding speed */
  public static final int HORSE_NORMAL_SPEED = 2;
  /** riding speed on road */
  public static final int HORSE_ROAD_SPEED = 3;

  /** The movement cost for normal movement */
  public static final int BF_NORMAL = 3;
  /** The movement cost for road movement */
  public static final int BF_ROAD = 2;

  /** The movement budget for walking units */
  public static final int BF_WALKING = 4;
  /** The movement budget for riding units */
  public static final int BF_RIDING = 6;

  private Rules rules;
  private Collection<ItemType> horseTypes;

  private MessageType transportMessageType = new MessageType(IntegerID.create(891175669));

  private MapMetric mapMetric;

  protected EresseaMovementEvaluator(Rules rules) {
    this.rules = rules;
    horseTypes = new ArrayList<ItemType>(2);
    for (ItemType type : rules.getItemTypes()) {
      if (type.isHorse()) {
        horseTypes.add(type);
      }
    }
    mapMetric = rules.getGameSpecificStuff().getMapMetric();
  }

  /**
   * Returns the maximum payload in GE 100 of this unit when it travels by horse. Horses, carts, catapults and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts and/or catapults for the horses, the weight of the additional carts
   * and/or catapults are also already considered.
   *
   * @return the payload in GE 100, CAP_NO_HORSES if the unit does not possess horses or
   *         CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
   *         horseback.
   */
  public int getPayloadOnHorse(Unit unit) {
    int horses = getHorses(unit);

    if (horses <= 0)
      return MovementEvaluator.CAP_NO_HORSES;

    if (horses > getMaxHorsesRiding(unit))
      return MovementEvaluator.CAP_UNSKILLED;

    int carts = getCarts(unit);
    int towedCarts = Math.min(carts, horses / 2);
    int freeHorses = horses - towedCarts * 2;

    int catapults = getCatapults(unit);
    int towedCatapults = Math.min(catapults, freeHorses / 2);

    int capacity =
        ((towedCarts * 100) + (towedCatapults * 0) // capacity of carts and catapults
            + (horses * 20) // capacity of horses
            - (carts - towedCarts) * 40 - (catapults - towedCatapults) * 100) * 100 // weight of untowed carts and
                                                                                    // catapults
            - (getRaceWeight(unit) * unit.getModifiedPersons()); // weight of unit

    // Fiete 20070421 (Runde 519)
    // GOTS not active when riding! (tested)
    // return respectGOTS(unit, capacity);
    return capacity;
  }

  private int getCarts(Unit unit) {
    Item i = unit.getModifiedItem(rules.getItemType(EresseaConstants.I_CART, true));

    if (i != null)
      return i.getAmount();
    return 0;
  }

  private int getCatapults(Unit unit) {
    Item i = unit.getModifiedItem(rules.getItemType(EresseaConstants.I_CATAPULT, true));

    if (i != null)
      return i.getAmount();
    return 0;
  }

  protected int getMaxHorsesRiding(Unit unit) {
    return getRules().getGameSpecificStuff().getGameSpecificRules().getMaxHorsesRiding(unit);
  }

  protected int getMaxHorsesWalking(Unit unit) {
    return getRules().getGameSpecificStuff().getGameSpecificRules().getMaxHorsesWalking(unit);
  }

  protected int getHorses(Unit unit) {
    int horses = 0;
    for (ItemType horseType : horseTypes) {
      Item i = unit.getModifiedItem(horseType);

      if (i != null) {
        horses += i.getAmount();
      }
    }
    return horses;
  }

  /**
   * Returns the maximum payload in GE 100 of this unit when it travels on foot. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered. The calculation also takes into account that trolls can tow carts.
   *
   * @return the payload in GE 100, CAP_UNSKILLED if the unit is not sufficiently skilled in horse
   *         riding to travel on horseback.
   */
  public int getPayloadOnFoot(Unit unit) {
    int capacity = 0;
    int horses = Math.max(0, getHorses(unit));

    if (horses > getMaxHorsesWalking(unit))
      // too many horses
      return MovementEvaluator.CAP_UNSKILLED;

    int carts = getCarts(unit);
    int pulledCarts = Math.min(carts, horses / 2);

    Race race = unit.getRace();

    int trollPulledCarts = 0;
    if (race != null && race.getID().equals(EresseaConstants.R_TROLLE)) {
      trollPulledCarts = Math.min(carts - pulledCarts, unit.getModifiedPersons() / 4);
    }

    int freeHorses = trollPulledCarts == 0 ? horses - pulledCarts * 2 : 0;

    int catapults = getCatapults(unit);
    int pulledCatapults = Math.min(catapults, freeHorses / 2);

    int trollPulledCatapults = 0;
    if (race != null && race.getID().equals(EresseaConstants.R_TROLLE)) {
      int freeTrolls = unit.getModifiedPersons() - trollPulledCarts * 4;
      trollPulledCatapults = Math.min(catapults - pulledCatapults, freeTrolls / 4);
    }

    pulledCarts += trollPulledCarts;
    pulledCatapults += trollPulledCatapults;

    capacity = ((pulledCarts * 100) + (pulledCatapults * 0)
        + horses * 20
        - ((carts - pulledCarts) * 40) - ((catapults - pulledCatapults) * 100)) * 100 // weight of not pulled carts and
                                                                                      // catapults
        + (race == null ? 540 : (int) (race.getCapacity() * 100)) * unit.getModifiedPersons(); // capacity of unit

    return respectGOTS(unit, capacity);
  }

  private int respectGOTS(Unit unit, int capacity) {
    Item gots = unit.getModifiedItem(rules.getItemType(EresseaConstants.I_GOTS, true));

    if (gots == null)
      return capacity;

    int multiplier = Math.max(0, Math.min(unit.getPersons(), gots.getAmount()));
    Race race = unit.getRace();

    if ((multiplier == 0) || (race == null))
      return capacity;

    // increase capacity by 49*unit.race.capacity per GOTS
    return capacity + (multiplier * (49 * (int) (race.getCapacity() * 100)));
  }

  /**
   * @deprecated Use {@link #getModifiedRadius(Unit)}
   */
  @Deprecated
  public int getRadius(Unit u) {
    return getRadius(u, false);
  }

  /**
   * Returns the number of regions this unit is able to travel within one turn based on the riding
   * skill, horses, carts and load of this unit.
   *
   * @deprecated Use {@link #getModifiedRadius(Unit, boolean)}.
   */
  @Deprecated
  public int getRadius(Unit u, boolean onRoad) {
    // pavkovic 2003.10.02: use modified load here...
    int load = getModifiedLoad(u);
    int payload = getPayloadOnHorse(u);

    if ((payload >= 0) && ((payload - load) >= 0))
      return onRoad ? 3 : 2;
    else {
      payload = getPayloadOnFoot(u);

      if ((payload >= 0) && ((payload - load) >= 0))
        return onRoad ? 2 : 1;
      else
        return 0;
    }
  }

  private int getRaceWeight(Unit unit) {
    Race race = unit.getRace();
    if (race == null)
      return 1000;
    else
      return (int) race.getWeight() * 100;
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver
   *
   * @see magellan.library.gamebinding.MovementEvaluator#getLoad(magellan.library.Unit)
   */
  public int getLoad(Unit unit) {
    return getLoad(unit, unit.getItems());
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the modified items plus all passengers modified weight.
   *
   * @see magellan.library.gamebinding.MovementEvaluator#getModifiedLoad(magellan.library.Unit)
   */
  public int getModifiedLoad(Unit unit) {
    int load = getLoad(unit, unit.getModifiedItems());

    // also take care of passengers
    for (Unit passenger : unit.getPassengers()) {
      if (passenger != null) {
        load += getModifiedWeight(passenger);
      }
    }

    return load;
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the specified items.
   */
  private int getLoad(Unit unit, Collection<Item> items) {
    int load = 0;
    ItemType cart = rules.getItemType(EresseaConstants.I_CART, true);
    // darcduck 2007-10-31: take care of bags of negative weight
    ItemType bonw = rules.getItemType(EresseaConstants.I_BONW, true);

    for (Item i : items) {
      if (!i.getItemType().isHorse() && !i.getItemType().equals(cart)) {
        // pavkovic 2003.09.10: only take care about (possibly) modified items with positive amount
        if (i.getAmount() > 0) {
          load += (((int) (i.getItemType().getWeight() * 100)) * i.getAmount());
        }
      }
      // darcduck 2007-10-31: take care of bags of negative weight
      if (i.getItemType().equals(bonw)) {
        load -= getBonwLoad(unit, items, i);
      }
    }

    return load;
  }

  /**
   * Returns the load in GE 100 of the bag of negative weight (bonw). This might be 0 if nothing can
   * be stored in the bag up to 200 per bag. Items are only considered to be stored in the bonw if
   * this is set in the rules. ItemType returns this in method isStoreableInBonw()
   *
   * @return the load of the bonw in GE 100.
   */
  private int getBonwLoad(Unit unit, Collection<Item> items, Item i_bonw) {
    final int I_BONW_CAP = 20000;
    int bonwload = 0;
    int bonwcap = 0;

    if (i_bonw != null) {
      bonwcap = i_bonw.getAmount() * I_BONW_CAP;

      for (Item i : items) {
        if (bonwload >= bonwcap) {
          break;
        }
        if ((i.getAmount() > 0) && (i.getItemType().isStoreableInBonw())) {
          bonwload += (((int) (i.getItemType().getWeight() * 100)) * i.getAmount());
        }
      }
      bonwload = Math.min(bonwcap, bonwload);
    }
    return bonwload;
  }

  /**
   * The initial weight of the unit as it appear in the report. This is the eressea version used to
   * calculate the weight if the information is not available in the report.
   *
   * @return the weight of the unit in silver (GE 100).
   */
  public int getWeight(Unit unit) {
    if (unit.isWeightWellKnown())
      return unit.getSimpleWeight();
    else
      return getWeight(unit, unit.getItems(), unit.getPersons());
  }

  /**
   * The modified weight is calculated from the modified number of persons and the modified items.
   * Due to some eressea dependencies this is done in this class.
   *
   * @return the modified weight of the unit in silver (GE 100).
   */
  public int getModifiedWeight(Unit unit) {
    int weight = getWeight(unit, unit.getModifiedItems(), unit.getModifiedPersons());

    /*
     * if we have a weight tag in the report we know the current exact weight via getWeight() but we
     * may not know the weight of some items or races which results in a to less calculated
     * (modified) weight. to overcome this, we do a delta calculation here, then we have a higher
     * chance of a correct size, at least when noting is given away or received.
     */
    if (unit.isWeightWellKnown()) {
      weight += unit.getSimpleWeight();
      weight -= getWeight(unit);
    }
    return weight;
  }

  /**
   * Returns the weight of the unit given the given collection of items and number of persons. Bags
   * of negative weight are respected.
   */
  private int getWeight(Unit unit, Collection<Item> items, int persons) {
    int weight = 0;
    int personWeight = getRaceWeight(unit);
    // darcduck 2007-10-31: take care of bags of negative weight
    ItemType bonw = rules.getItemType(EresseaConstants.I_BONW, true);

    for (Item item : items) {
      // pavkovic 2003.09.10: only take care about (possibly) modified items with positive amount
      if (item.getAmount() > 0) {
        weight += (item.getAmount() * (int) (item.getItemType().getWeight() * 100));
      }
      // darcduck 2007-10-31: take care of bags of negative weight
      if (item.getItemType().equals(bonw)) {
        weight -= getBonwLoad(unit, items, item);
      }
    }

    weight += persons * personWeight;

    return weight;
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getModifiedRadius(magellan.library.Unit)
   */
  public int getModifiedRadius(Unit unit) {
    return getModifiedRadius(unit, false);
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getModifiedRadius(magellan.library.Unit,
   *      boolean)
   */
  public int getModifiedRadius(Unit unit, boolean onRoad) {
    if (canRide(unit))
      return onRoad ? HORSE_ROAD_SPEED : HORSE_NORMAL_SPEED;

    if (canWalk(unit))
      return onRoad ? FOOT_ROAD_SPEED : FOOT_NORMAL_SPEED;

    return 0;
  }

  /**
   * @return <code>true</code> if the movement can walk and is not overloaded.
   */
  public boolean canWalk(Unit unit) {
    int load = getModifiedLoad(unit);
    int payload = getPayloadOnFoot(unit);
    return (payload >= 0) && (payload >= load);
  }

  /**
   * @return <code>true</code> if the movement can ride without being overloaded.
   */
  public boolean canRide(Unit unit) {
    int load = getModifiedLoad(unit);
    int payload = getPayloadOnHorse(unit);
    return (payload >= 0) && (payload >= load);
  }

  protected MessageType getTransportMessageType() {
    return transportMessageType;
  }

  /**
   * Returns <code>true</code> if the unit's past movement was passive (transported, shipped...)
   *
   * @param u
   * @return <code>true</code> if there is evidence that the unit's past movement was passive
   *         (transported, shipped...)
   * @see magellan.library.gamebinding.MovementEvaluator#isPastMovementPassive(magellan.library.Unit)
   */
  public boolean isPastMovementPassive(Unit u) {
    if (u.getShip() != null) {
      if (u.equals(u.getShip().getOwnerUnit())) {
        // unit is on ship and the owner
        if (log.isDebugEnabled()) {
          log.debug("PathCellRenderer(" + u + "):false on ship");
        }

        return false;
      }

      // unit is on a ship and not the owner
      if (log.isDebugEnabled()) {
        log.debug("PathCellRenderer(" + u + "):true on ship");
      }

      return true;
    }

    // we assume a transportation to be passive, if
    // there is no message of type 891175669
    if (u.getFaction() == null) {
      if (log.isDebugEnabled()) {
        log.debug("PathCellRenderer(" + u + "):false no faction");
      }

      return false;
    }

    if (u.getFaction().getMessages() == null) {
      // faction has no message at all
      if (log.isDebugEnabled()) {
        log.debug("PathCellRenderer(" + u + "):false no faction");
      }

      return true;
    }

    for (Message m : u.getFaction().getMessages()) {
      if (log.isDebugEnabled()) {
        if (getTransportMessageType().equals(m.getMessageType())) {
          log.debug("PathCellRenderer(" + u + ") Message " + m);

          if (m.getAttribute("unit") != null) {
            log.debug("PathCellRenderer(" + u + ") Unit   " + m.getAttribute("unit"));
            // actually it should be creatUnitID(*, 10, data.base), but it doesn't matter here
            log.debug("PathCellRenderer(" + u + ") UnitID "
                + UnitID.createUnitID(m.getAttribute("unit"), 10));
          }
        }
      }

      if (getTransportMessageType().equals(m.getMessageType())
          && (m.getAttribute("unit") != null)
          && u.getID().equals(
              UnitID.createUnitID(m.getAttribute("unit"), 10, u.getData().base))) {
        // found a transport message; this is only valid in
        // units with active movement
        if (log.isDebugEnabled()) {
          log.debug("PathCellRenderer(" + u + "):false with message " + m);
        }

        return false;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("PathCellRenderer(" + u + "):true with messages");
    }

    return true;
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getModifiedMovement(magellan.library.Unit)
   */
  public List<CoordinateID> getModifiedMovement(Unit u) {
    return getMovement(u, true, false);
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getAdditionalMovement(magellan.library.Unit)
   */
  public List<CoordinateID> getAdditionalMovement(Unit u) {
    return getMovement(u, true, true);
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getPassiveMovement(magellan.library.Unit)
   */
  public List<CoordinateID> getPassiveMovement(Unit u) {
    return getMovement(u, false, false);
  }

  private List<CoordinateID> getMovement(Unit u, boolean active, boolean suffix) {
    List<MovementRelation> rels = u.getRelations(MovementRelation.class);
    if (rels.size() == 0)
      return Collections.emptyList();

    MovementRelation rel = rels.get(0);

    if (active ^ (rel.origin == u))
      return Collections.emptyList();

    if (active)
      if (suffix)
        return rel.getFutureMovement();
      else
        return rel.getInitialMovement();
    else
      return rel.getMovement();
  }

  protected interface Metric {
    public boolean update(CoordinateID currentCoord, Region currentRegion, CoordinateID nextCoord,
        Region nextRegion);

    public int getRounds();
  }

  protected class ShipMetric implements Metric {

    private int etappe;
    private int rounds;
    private int speed;

    public ShipMetric(int speed) {
      this.speed = speed;
    }

    public boolean update(CoordinateID currentCoord, Region currentRegion, CoordinateID nextCoord,
        Region nextRegion) {
      if (nextRegion != null && !nextRegion.getRegionType().isOcean()) {
        etappe = speed;
      } else if (++etappe > speed) {
        etappe -= speed;
        ++rounds;
      }
      return (currentRegion == null || nextRegion == null || (currentRegion.getRegionType()
          .isOcean() || nextRegion.getRegionType().isOcean()));
    }

    public int getRounds() {
      return rounds;
    }

    public int getEtappe() {
      return etappe;
    }
  }

  protected class LandMetric implements Metric {

    private int rounds;
    private int roadEtappe;
    private int normalEtappe;
    private int bfSpeed;

    LandMetric(int speed) {
      bfSpeed = speed;
    }

    public boolean update(CoordinateID currentCoord, Region currentRegion, CoordinateID nextCoord,
        Region nextRegion) {
      if (nextRegion != null && nextRegion.getRegionType().isOcean()) {
        ++rounds;
        normalEtappe = 1;
        roadEtappe = 0;
        return false;
      }
      boolean road = false;
      if (currentRegion != null && nextRegion != null
          && Regions.isCompleteRoadConnection(currentRegion, nextRegion)) {
        road = true;
      }
      int distance = BF_ROAD * roadEtappe + BF_NORMAL * normalEtappe + (road ? BF_ROAD : BF_NORMAL);
      if (distance > bfSpeed) {
        rounds++;
        normalEtappe = road ? 0 : 1;
        roadEtappe = road ? 1 : 0;
      } else {
        if (road) {
          ++roadEtappe;
        } else {
          ++normalEtappe;
        }
      }
      return true;
    }

    public int getRounds() {
      return rounds;
    }

  }

  public MovementRelation getMovement(Unit unit, List<Direction> directions, int maxLength) {
    GameData data = unit.getData();
    List<CoordinateID> initialMovement = new ArrayList<CoordinateID>(2);
    List<CoordinateID> futureMovement = new ArrayList<CoordinateID>(2);

    Metric metric;
    if (unit.getModifiedShip() != null && unit.getModifiedShip().getModifiedOwnerUnit() == unit) {
      metric = new ShipMetric(data.getGameSpecificRules().getShipRange(unit.getModifiedShip()));
    } else {
      metric = new LandMetric(canWalk(unit) ? (canRide(unit) ? BF_RIDING : BF_WALKING) : 0);
    }

    // dissect the order into pieces to detect which way the unit is taking
    Region currentRegion = unit.getRegion();
    CoordinateID currentCoord = unit.getRegion().getCoordinate();

    boolean unknown = false;

    Region invalidRegion = null;

    initialMovement.add(currentCoord);
    int stopped = 0;
    for (Direction movement : directions) {

      // try to get the next region; take "wrap around" regions into account
      CoordinateID nextCoord = currentCoord;
      Region nextRegion = currentRegion;
      if (movement != Direction.INVALID) {
        if (stopped < 2) {
          stopped = 0;
        }
        // try to get next region from the neighbor relation; not possible if the movement goes
        // through an unknown region
        nextRegion = currentRegion != null ? currentRegion.getNeighbors().get(movement) : null;
        // if the nextRegion is unknown for some reason, fall back to coordinate movement
        if (nextRegion == null) {
          nextCoord = mapMetric.translate(currentCoord, movement);
          unknown = stopped < 2;
        } else {
          nextCoord = nextRegion.getCoordinate();
        }
        if (nextRegion == null) {
          nextRegion = unit.getRegion().getData().getRegion(nextCoord);
        }

        if (!metric.update(currentCoord, currentRegion, nextCoord, nextRegion)) {
          if (invalidRegion == null && stopped < 2) { // two PAUSES -> end of route -> do not warn
            invalidRegion = nextRegion;
          }
          if (futureMovement.isEmpty()) {
            futureMovement.add(currentCoord);
          }
        } else if (metric.getRounds() > 0
        // || initialMovement.size() + 1 > maxLength
        ) {
          if (futureMovement.isEmpty()) {
            futureMovement.add(currentCoord);
          }
        }
      } else {
        stopped++;
        if (futureMovement.isEmpty()) {
          futureMovement.add(currentCoord);
        }
      }

      if (!futureMovement.isEmpty()) {
        futureMovement.add(nextCoord);
      } else {
        initialMovement.add(nextCoord);
      }
      currentCoord = nextCoord;
      currentRegion = nextRegion;
    }

    MovementRelation mRel =
        new MovementRelation(unit, unit, initialMovement, futureMovement, unknown, invalidRegion,
            metric.getRounds(), -1);

    return mRel;
  }

  public int getModifiedRadius(Unit unit, List<Region> path) {
    MovementRelation mRel = getMovement(unit, pathToDirections(path), Integer.MAX_VALUE);
    return mRel.getInitialMovement().size() - 1;
  }

  protected List<Direction> pathToDirections(List<?> path) {
    List<Direction> result = new ArrayList<Direction>(path.size());
    CoordinateID lastRegion = null;
    for (Object region : path) {
      CoordinateID currentRegion;
      if (region instanceof Region) {
        currentRegion = ((Region) region).getCoordinate();
      } else {
        currentRegion = (CoordinateID) region;
      }
      if (lastRegion != null) {
        result.add(mapMetric.getDirection(lastRegion, currentRegion));
      }
      lastRegion = currentRegion;
    }
    return result;
  }

  public CoordinateID getDestination(Unit unit, List<CoordinateID> path) {
    MovementRelation mRel = getMovement(unit, pathToDirections(path), Integer.MAX_VALUE);
    return mRel.getDestination();
  }

  /**
   * @see magellan.library.gamebinding.MovementEvaluator#getDistance(magellan.library.Unit,
   *      java.util.List)
   */
  public int getDistance(Unit unit, List<Region> path) {
    MovementRelation mRel = getMovement(unit, pathToDirections(path), Integer.MAX_VALUE);
    return mRel.rounds;
  }

  protected Rules getRules() {
    return rules;
  }

}
