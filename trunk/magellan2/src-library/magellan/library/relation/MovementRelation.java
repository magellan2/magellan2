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

package magellan.library.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Unit;

/**
 * A relation indicating a movement of a unit.
 */
public class MovementRelation extends UnitRelation implements LongOrderRelation {
  /** This list consists of the reached coordinates (starting with the current region) */
  private List<CoordinateID> movement;

  private List<CoordinateID> initialMovement;
  private List<CoordinateID> futureMovement;

  public boolean unknown;
  public int rounds;
  public Region invalidRegion;

  /**
   * Creates a new MovementRelation object.
   * 
   * @param u The source unit
   * @param transporter
   * @param initialMovement
   * @param futureMovement
   * @param unknown path passes through an unknown region
   * @param invalidRegion path passes through an invalid region for the unit's mode of movement
   * @param rounds number of rounds for the unit to complete the path
   * @param line The line in the transporter's orders
   */
  public MovementRelation(Unit u, Unit transporter, List<CoordinateID> initialMovement,
      List<CoordinateID> futureMovement, boolean unknown, Region invalidRegion, int rounds, int line) {
    super(transporter, u, line);
    if (futureMovement == null) {
      this.futureMovement =
          Collections.singletonList(this.initialMovement.get(initialMovement.size() - 1));
    } else {
      this.futureMovement = futureMovement;
    }
    this.initialMovement = initialMovement;

    this.unknown = unknown;
    this.invalidRegion = invalidRegion;
    this.rounds = rounds;
  }

  /**
   * @see magellan.library.relation.UnitRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@MOVEMENT=" + movement;
  }

  /**
   * Returns the value of movement.
   * 
   * @return Returns movement.
   */
  public List<CoordinateID> getMovement() {
    if (movement == null) {
      movement = new ArrayList<CoordinateID>(initialMovement.size() + futureMovement.size());
      movement.addAll(initialMovement);
      int i = 0;
      for (CoordinateID coord : futureMovement)
        if (i++ > 0) {
          movement.add(coord);
        }
    }
    return movement;
  }

  /**
   * Returns the value of initialMovement.
   * 
   * @return Returns initialMovement.
   */
  public List<CoordinateID> getInitialMovement() {
    return initialMovement;
  }

  /**
   * Returns the value of futureMovement.
   * 
   * @return Returns futureMovement.
   */
  public List<CoordinateID> getFutureMovement() {
    return futureMovement;
  }

  /**
   * Returns the value of destination.
   * 
   * @return Returns destination.
   */
  public CoordinateID getDestination() {
    return initialMovement.get(initialMovement.size() - 1);
  }

  /**
   * Returns the value of transporter.
   * 
   * @return Returns transporter.
   */
  public Unit getTransporter() {
    return origin;
  }

  @Override
  public void add() {
    source.addRelation(this);
    // do not add to origin
  }
}
