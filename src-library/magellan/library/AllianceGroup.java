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
 * A class representing an alliance with several members.
 */
public class AllianceGroup implements Unique {

  private EntityID id;
  private String name;
  private EntityID leader;
  private Set<ID> factions;

  /**
   * Create a new Alliance object.
   * 
   * @param id the ID of this alliance group
   * @throws NullPointerException if the id is null.
   */
  public AllianceGroup(EntityID id) {
    this(id, null);
  }

  /**
   * Create a new Alliance object with name.
   * 
   * @param id the ID of this alliance group
   * @param name The alliance name
   * @throws NullPointerException if the id is null.
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
   * A method to convert an alliance into a trustlevel. This method should be used when Magellan
   * calculates trust levels on its own.
   * 
   * @return the trustlevel of this alliance
   */
  @Deprecated
  public int getTrustLevel() {
    return TrustLevel.TL_PRIVILEGED - 1;
  }

  /**
   * Adds a faction to the list of factions belonging to this alliance.
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
