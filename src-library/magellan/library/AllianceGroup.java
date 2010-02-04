/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import magellan.library.utils.Resources;

/**
 * A class representing an alliance status between two factions. The faction having this alliance is
 * implicit, the target faction is an explicite field of this class.
 */
public class AllianceGroup {

  private EntityID id;
  private String name;
  private EntityID leader;
  private Set<ID> factions;

  /**
   * Create a new Alliance object for an alliance with the specified faction and without any
   * alliance status set.
   * 
   * @param id the faction to establish an alliance with.
   */
  public AllianceGroup(EntityID id) {
    this(id, null);
  }

  /**
   * Create a new Alliance object for an alliance with the specified faction and the specified
   * status.
   * 
   * @param id the faction to establish an alliance with
   * @param name the alliance status, must be one of constants SILVER, FIGHT, GIVE, GUARD, GUISE or
   *          ALL.
   * @throws NullPointerException if the faction parameter is null.
   */
  public AllianceGroup(EntityID id, String name) {
    if (id == null)
      throw new NullPointerException();

    this.id = id;
    this.name = name;
  }

  /**
   * Returns the value of id.
   * 
   * @return Returns id.
   */
  public EntityID getID() {
    return id;
  }

  /**
   * Returns the alliance name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the alliance name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the alliance's leader.
   */
  public EntityID getLeader() {
    return leader;
  }

  /**
   * Sets the leader of the alliance.
   */
  public void setLeader(EntityID leader) {
    this.leader = leader;
  }

  /**
   * Return a string representation of this alliance object.
   * 
   * @return the alliance object as string.
   */
  @Override
  public String toString() {
    if (name == null)
      return Resources.get("alliance.noname", id.toString());
    else
      return Resources.get("alliance.name", name, id.toString());
  }

  /**
   * A method to convert an alliance into a trustlevel. This method should be uses when Magellan
   * calculates trust levels on its own.
   * 
   * @return the trustlevel of this alliance
   */
  public int getTrustLevel() {
    return 100;
  }

  /**
   * Adds a faction to the list of factions beloning to this alliance.
   */
  public void addFaction(Faction faction) {
    if (factions == null) {
      factions = new HashSet<ID>();
    }
    factions.add(faction.getID());
  }

  /**
   * Returns a list of all factions belonging to this alliance.
   */
  public Collection<ID> getFactions() {
    if (factions == null)
      return Collections.emptyList();
    return factions;
  }

}
