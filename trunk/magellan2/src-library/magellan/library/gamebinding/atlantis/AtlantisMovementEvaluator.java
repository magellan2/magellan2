// class magellan.library.gamebinding.AtlantisMovementEvaluator
// created on Apr 16, 2013
//
// Copyright 2003-2013 by magellan project team
//
// Author : $Author: $
// $Id: $
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
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.gamebinding.atlantis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.MapMetric;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.MovementRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.Direction;

/**
 * Movement evaluator for Atlantis game.
 */
public class AtlantisMovementEvaluator implements MovementEvaluator {

  private static final int CARRYING_CAPACITY = 5;
  private static final int HORSE_CAPACITY = 50;
  private static final float GE_IN_SILVER = 100;

  private Rules rules;
  private ArrayList<ItemType> horseTypes;
  private MapMetric metric;

  protected AtlantisMovementEvaluator(Rules rules) {
    this.rules = rules;
    horseTypes = new ArrayList<ItemType>(2);
    for (ItemType type : rules.getItemTypes()) {
      if (type.isHorse()) {
        horseTypes.add(type);
      }
    }
    metric = rules.getGameSpecificStuff().getMapMetric();
  }

  public int getPayloadOnHorse(Unit unit) {
    return getPayloadOnFoot(unit);
  }

  public int getPayloadOnFoot(Unit unit) {
    int cap = (int) (CARRYING_CAPACITY * unit.getPersons() * GE_IN_SILVER);
    for (ItemType horseType : horseTypes) {
      Item item = unit.getItem(horseType);
      if (item != null) {
        cap += (int) (HORSE_CAPACITY * item.getAmount() * GE_IN_SILVER);
      }
    }
    return cap;
  }

  public int getLoad(Unit unit) {
    int load = 0;
    for (Item item : unit.getItems()) {
      load += (((int) (item.getItemType().getWeight() * GE_IN_SILVER)) * item.getAmount());
    }
    return load;
  }

  public int getModifiedLoad(Unit unit) {
    int load = 0;
    for (Item item : unit.getModifiedItems()) {
      if (!item.getItemType().isHorse()) {
        load += (((int) (item.getItemType().getWeight() * GE_IN_SILVER)) * item.getAmount());
      }
    }
    return load;
  }

  public int getWeight(Unit unit) {
    int weight = (int) (10 * GE_IN_SILVER * unit.getPersons());
    for (Item item : unit.getItems()) {
      weight += (((int) (item.getItemType().getWeight() * GE_IN_SILVER)) * item.getAmount());
    }
    return weight;
  }

  public int getModifiedWeight(Unit unit) {
    int weight = (int) (10 * GE_IN_SILVER * unit.getModifiedPersons());
    for (Item item : unit.getModifiedItems()) {
      weight += (((int) (item.getItemType().getWeight() * GE_IN_SILVER)) * item.getAmount());
    }
    return weight;
  }

  public int getModifiedRadius(Unit unit) {
    return 1;
  }

  public int getModifiedRadius(Unit unit, boolean onRoad) {
    return 1;
  }

  public int getModifiedRadius(Unit unit, List<Region> path) {
    return 1;
  }

  public int getRadius(Unit u) {
    return 1;
  }

  public int getRadius(Unit u, boolean onRoad) {
    return 1;
  }

  public boolean isPastMovementPassive(Unit unit) {
    if (unit.getShip() != null)
      return !unit.equals(unit.getShip().getOwnerUnit());

    return false;
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

  public MovementRelation getMovement(Unit unit, List<Direction> directions, int maxLength) {
    List<CoordinateID> initialMovement = new ArrayList<CoordinateID>(2);
    CoordinateID c;
    initialMovement.add(c = unit.getRegion().getCoordinate());
    if (directions.size() > 0) {
      initialMovement.add(metric.translate(c, directions.get(0)));
    }
    return new MovementRelation(unit, unit, initialMovement,
        Collections.<CoordinateID> emptyList(), false, (Region) null, 1, -1);
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
        result.add(metric.getDirection(lastRegion, currentRegion));
      }
      lastRegion = currentRegion;
    }
    return result;
  }

}
