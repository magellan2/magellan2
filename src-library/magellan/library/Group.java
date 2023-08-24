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

package magellan.library;

import java.util.Collection;
import java.util.Map;

import magellan.library.utils.Taggable;

/**
 * A class representing a group of units within a faction.
 */
public interface Group extends Named, Taggable, Addeable {
  /**
   * Set the faction this group belongs to.
   */
  public void setFaction(Faction f);

  /**
   * Get the faction this group belongs to.
   */
  public Faction getFaction();

  /**
   * The alliances specific to this group. The map returned by this function contains <kbd>ID</kbd>
   * objects as keys with the id of the faction that alliance references. The values are instances
   * of class <kbd>Alliance</kbd>. The return value is never null.
   */
  public Map<EntityID, Alliance> allies();

  public void setAllies(Map<EntityID, Alliance> alliances);

  /**
   * Sets the group dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix);

  /**
   * Returns the group dependent prefix for the race name.
   */
  public String getRaceNamePrefix();

  /**
   * Sets an index indicating how instances of class are sorted in the report.
   */
  public void setSortIndex(int index);

  /**
   * Returns an index indicating how instances of class are sorted in the report.
   */
  public int getSortIndex();

  /**
   * Returns an unmodifiable collection of all the units in this container.
   */
  public Collection<Unit> units();

  /**
   * Retrieve a unit in this container by id.
   */
  public Unit getUnit(ID key);

  /**
   * Adds a unit to this container. This method should only be invoked by Unit.setXXX() methods.
   */
  public void addUnit(Unit u);

  /**
   * Removes a unit from this container. This method should only be invoked by Unit.setXXX()
   * methods.
   */
  public Unit removeUnit(ID key);

  /**
   * Returns a String representation of this group object.
   */
  public String toString();

  /**
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();

}
