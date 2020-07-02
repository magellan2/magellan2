/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.relation.MovementRelation;
import magellan.library.utils.Direction;

/**
 * Provides game specific methods concerning movement, load and such.
 */
public interface MovementEvaluator {
  /** The unit does not possess horses */
  public static final int CAP_NO_HORSES = Integer.MIN_VALUE;

  /** The unit is not sufficiently skilled in horse riding */
  public static final int CAP_UNSKILLED = MovementEvaluator.CAP_NO_HORSES + 1;

  /**
   * Returns the maximum payload in GE*100 of this unit when it travels by horse. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered.
   *
   * @return the payload in GE*100, CAP_NO_HORSES if the unit does not possess horses or
   *         CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
   *         horseback.
   */
  public int getPayloadOnHorse(Unit unit);

  /**
   * Returns the maximum payload in GE*100 of this unit when it travels on foot. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered. The calculation also takes into account that trolls can tow carts.
   *
   * @return the payload in GE*100, CAP_UNSKILLED if the unit is not sufficiently skilled in horse
   *         riding to travel on foot.
   */
  public int getPayloadOnFoot(Unit unit);

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver
   */
  public int getLoad(Unit unit);

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the modified items.
   */
  public int getModifiedLoad(Unit unit);

  /**
   * The initial weight of the unit as it appears in the report. This should be the game dependent
   * version used to calculate the weight if the information is not available in the report.
   *
   * @return the weight of the unit in silver (GE 100).
   */
  public int getWeight(Unit unit);

  /**
   * The modified weight is calculated from the modified number of persons and the modified items.
   * Due to some game dependencies this is done in this class.
   *
   * @return the modified weight of the unit in silver (GE 100).
   */
  public int getModifiedWeight(Unit unit);

  /**
   * Returns the unit's speed based on payload and horses.
   *
   * @param unit
   * @return the unit's speed based on payload and horses
   */
  public int getModifiedRadius(Unit unit);

  /**
   * Returns the number of regions this unit is able to travel (on roads if <code>onRoad</code> is
   * <code>true</code>) within one turn based on modified riding skill, horses, carts and load of
   * this unit.
   */
  public int getModifiedRadius(Unit unit, boolean onRoad);

  /**
   * Returns the number of regions this unit is able to travel within one turn based on modified
   * riding skill, horses, carts, load of this unit and roads <i>on the given path</i>.
   *
   * @param unit
   * @param path A sequence of regions. The first region must be the current region of the unit. If
   *          two successive elements of the path are the same region, this is interpreted as a
   *          PAUSE, which always ends a turn. See {@link Unit#getModifiedMovement()}.
   * @return The number of regions, the unit may move on this path. The result is always
   *         <code>&le; path.size()-1</code>.
   * @throws IllegalArgumentException if the unit is not in the first path region or the path is not
   *           continuous
   */
  public int getModifiedRadius(Unit unit, List<Region> path);

  /**
   * Returns the destination region where this unit is able to travel within one turn based on
   * modified riding skill, horses, carts, load of this unit and roads <i>on the given path</i>. The
   * result may be inaccurate when some regions corresponding to coordinates in the path are not in
   * the data.
   *
   * @param unit
   * @param path A sequence of coordinates. The first region must be the current region of the unit.
   *          If two successive elements of the path are the same region, this is interpreted as a
   *          PAUSE, which always ends a turn. See {@link Unit#getModifiedMovement()}.
   * @return The number of regions, the unit may move on this path. The result is always
   *         <code>&le; path.size()-1</code>.
   * @throws IllegalArgumentException if the unit is not in the first path region or the path is not
   *           continuous
   */
  public CoordinateID getDestination(Unit unit, List<CoordinateID> path);

  /**
   * Returns the number of turns that the unit needs to travel on the specified path based on
   * modified riding skill, horses, carts, load of this unit and roads <i>on the given path</i>.
   *
   * @param unit
   * @param path A sequence of regions. The first region must be the current region of the unit.
   * @return the number of rounds needed to complete the path
   */
  public int getDistance(Unit unit, List<Region> path);

  /**
   * Returns the number of regions this unit is able to travel within one turn based on the riding
   * skill, horses, carts and load of this unit.
   *
   * @deprecated Use {@link #getModifiedRadius(Unit)}.
   */
  @Deprecated
  public int getRadius(Unit u);

  /**
   * Returns the number of regions this unit is able to travel (on roads if <code>onRoad</code> is
   * <code>true</code>) within one turn based on the riding skill, horses, carts and load of this
   * unit.
   *
   * @deprecated Use {@link #getModifiedRadius(Unit, boolean)}.
   */
  @Deprecated
  public int getRadius(Unit u, boolean onRoad);

  /**
   * Checks if the unit's movement was passive (transported or shipped).
   *
   * @param unit
   * @return <code>true</code> if the unit's past movement was passive
   */
  public boolean isPastMovementPassive(Unit unit);

  /**
   * Returns the list of regions that the unit will move through in the next turn.
   *
   * @param u
   * @return A list of regions, starting with the current region. An empty list if the unit doesn't
   *         move.
   */
  public List<CoordinateID> getModifiedMovement(Unit u);

  /**
   * Returns the list of regions that the unit will move through <em>after</em> the next turn.
   *
   * @param u
   * @return A list of regions, starting with the destination of the current turn. If there is a
   *         PAUSE, the region repeats. An empty list if the unit doesn't move.
   */
  public List<CoordinateID> getAdditionalMovement(Unit u);

  /**
   * Returns the list of regions that the unit will be transported through.
   *
   * @param u
   * @return A list of regions, starting with the current region. If there is a PAUSE, the region
   *         repeats. An empty list if the unit doesn't move.
   */
  public List<CoordinateID> getPassiveMovement(Unit u);

  /**
   * Computes the movement of a unit with the given directions as orders
   *
   * @param unit
   * @param directions
   * @param maxLength
   * @return A MovementRelation expressing the future movement of the unit
   */
  public MovementRelation getMovement(Unit unit, List<Direction> directions, int maxLength);

}
