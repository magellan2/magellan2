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

package magellan.library.impl;

import java.util.LinkedList;
import java.util.List;

import magellan.library.Battle;
import magellan.library.CoordinateID;
import magellan.library.Message;

/**
 * Container class for a battle.
 */
public class MagellanBattleImpl extends MagellanIdentifiableImpl implements Battle {
  /** defines if this is a default BATTLE or a BATTLESPEC block. */
  private boolean isBattleSpec = false;

  /** List of messages for this Battle object. */
  private List<Message> messages;

  /**
   * Create an empty Battle object with the specified id.
   * 
   * @param id an unique identifier for this battle.
   */
  public MagellanBattleImpl(CoordinateID id) {
    super(id);
    messages = new LinkedList<Message>();
  }

  /**
   * Create an empty Battle object with the specified id.
   * 
   * @param id an unique identifier for this battle.
   * @param spec indicates that the CR representation of this battle is a BATTLESPEC block in the
   *          computer report.
   */
  public MagellanBattleImpl(CoordinateID id, boolean spec) {
    this(id);
    isBattleSpec = spec;
  }

  /**
   * Get the messages of this battle.
   * 
   * @return a reference to the list of messages stored for this battle. This value is never null.
   */
  public List<Message> messages() {
    return messages;
  }

  /**
   * Sets whether the CR representation of this battle is a standard BATTLE block or a BATTLESPEC
   * block.
   * 
   * @param bool set true to mark it as BATTLESPEC block, false to mark as standard BATTLE block.
   */
  public void setBattleSpec(boolean bool) {
    isBattleSpec = bool;
  }

  /**
   * Check if the Battle object is a BATTLESPEC or BATTLE block.
   * 
   * @return true if the CR representation of this battle is a BATTLESPEC block, false if it's a
   *         standard BATTLE block.
   */
  public boolean isBattleSpec() {
    return isBattleSpec;
  }

  /**
   * @see magellan.library.Unique#getID()
   */
  @Override
  public CoordinateID getID() {
    return (CoordinateID) super.getID(); // StringID.create(super.getName());
  }
}
