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
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.MovementRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.Direction;

/**
 * Movement evaluator for Atlantis game.
 */
public class AtlantisMovementEvaluator implements MovementEvaluator {

  public int getPayloadOnHorse(Unit unit) {
    return 0;
  }

  public int getPayloadOnFoot(Unit unit) {
    return 0;
  }

  public int getLoad(Unit unit) {
    return 0;
  }

  public int getModifiedLoad(Unit unit) {
    return 0;
  }

  public int getWeight(Unit unit) {
    return 0;
  }

  public int getModifiedWeight(Unit unit) {
    return 0;
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

  public CoordinateID getDestination(Unit unit, List<CoordinateID> path) {
    return null;
  }

  public int getDistance(Unit unit, List<Region> path) {
    return 1;
  }

  public int getRadius(Unit u) {
    return 1;
  }

  public int getRadius(Unit u, boolean onRoad) {
    return 1;
  }

  public boolean isPastMovementPassive(Unit unit) {
    return false;
  }

  public List<CoordinateID> getModifiedMovement(Unit u) {
    return Collections.emptyList();
  }

  public List<CoordinateID> getAdditionalMovement(Unit u) {
    return Collections.emptyList();
  }

  public List<CoordinateID> getPassiveMovement(Unit u) {
    return Collections.emptyList();
  }

  public MovementRelation getMovement(Unit unit, List<Direction> directions) {
    List<CoordinateID> initialMovement = new ArrayList<CoordinateID>(2);
    CoordinateID c;
    initialMovement.add(c = unit.getRegion().getCoordinate());
    if (directions.size() > 0) {
      initialMovement.add(c.translate(directions.get(0).toCoordinate()));
    }
    return new MovementRelation(unit, unit, initialMovement,
        Collections.<CoordinateID> emptyList(), false, (Region) null, 1, -1);
  }

}
