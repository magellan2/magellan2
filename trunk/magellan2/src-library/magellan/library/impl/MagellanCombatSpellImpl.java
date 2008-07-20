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

import magellan.library.CombatSpell;
import magellan.library.ID;
import magellan.library.Spell;
import magellan.library.Unit;

/**
 * A class representing a combat spell set for a certain unit. It links a unit with a certain spell
 * and contains information at which level the unit wants to cast the spell.
 */
public class MagellanCombatSpellImpl extends MagellanIdentifiableImpl implements CombatSpell {
	private Spell spell;
	private Unit unit;
	private int castingLevel;

	/**
	 * Creates a new CombatSpell object with the specified id.
	 *
	 * @param id the if of the spell.
	 */
	public MagellanCombatSpellImpl(ID id) {
		super(id);
	}

	/**
	 * Get the actuell spell to be cast in combat.
	 *
	 * @return the spell to be cast.
	 */
	public Spell getSpell() {
		return spell;
	}

	/**
	 * Specify the actual spell of this CombatSpell.
	 *
	 * @param spell the spell that shall be cast in combat.
	 */
	public void setSpell(Spell spell) {
		this.spell = spell;
	}

	/**
	 * Retrieve the unit that has this combat spell set as a combat spell.
	 *
	 * @return the casting unit.
	 */
	public Unit getUnit() {
		return this.unit;
	}

	/**
	 * Sets the unit which has this combat spell set as a combat spell.
	 *
	 * @param unit the casting unit.
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * Gets the level at which the unit wants to cast this spell.
	 *
	 * @return the level of the spell to be casted.
	 */
	public int getCastingLevel() {
		return this.castingLevel;
	}

	/**
	 * Sets a level at which the unit wants to cast this spell.
	 *
	 * @param castingLevel this value must be greater than 0 and  not greater than the unit's magic
	 * 		  skill level.
	 */
	public void setCastingLevel(int castingLevel) {
		this.castingLevel = castingLevel;
	}

	/**
	 * Returns a String representation of this combat spell.
	 *
	 * @return combat spell object as string.
	 */
	@Override
  public String toString() {
		return (getSpell() == null) ? ""
									: (getSpell().getTypeName() + ", " + getCastingLevel() + ": " +
									getSpell().toString());
	}
}
