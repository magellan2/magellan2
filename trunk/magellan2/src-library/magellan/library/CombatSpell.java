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

/**
 * A class representing a combat spell set for a certain unit. It links a unit with a certain spell
 * and contains information at which level the unit wants to cast the spell.
 */
public interface CombatSpell extends Identifiable {
	/**
	 * Get the actuell spell to be cast in combat.
	 *
	 * @return the spell to be cast.
	 */
	public Spell getSpell();

	/**
	 * Specify the actual spell of this CombatSpell.
	 *
	 * @param spell the spell that shall be cast in combat.
	 */
	public void setSpell(Spell spell);

	/**
	 * Retrieve the unit that has this combat spell set as a combat spell.
	 *
	 * @return the casting unit.
	 */
	public Unit getUnit();

	/**
	 * Sets the unit which has this combat spell set as a combat spell.
	 *
	 * @param unit the casting unit.
	 */
	public void setUnit(Unit unit);

	/**
	 * Gets the level at which the unit wants to cast this spell.
	 *
	 * @return the level of the spell to be casted.
	 */
	public int getCastingLevel();

	/**
	 * Sets a level at which the unit wants to cast this spell.
	 *
	 * @param castingLevel this value must be greater than 0 and  not greater than the unit's magic
	 * 		  skill level.
	 */
	public void setCastingLevel(int castingLevel);

	/**
	 * Returns a String representation of this combat spell.
	 *
	 * @return combat spell object as string.
	 */
	public String toString();

  /**
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();
}
