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

import java.util.List;



/**
 * Container class for a battle.
 */
public interface Battle extends Identifiable {
	/**
	 * Get the messages of this battle.
	 *
	 * @return a reference to the list of messages stored for this battle. This value is never
	 * 		   null.
	 */
	public List<Message> messages();

	/**
	 * Sets whether the CR representation of this battle is a standard BATTLE block or a BATTLESPEC
	 * block.
	 *
	 * @param bool set true to mark it as BATTLESPEC block, false to mark as standard BATTLE block.
	 */
	public void setBattleSpec(boolean bool);

	/**
	 * Check if the Battle object is a BATTLESPEC or BATTLE block.
	 *
	 * @return true if the CR representation of this battle is a BATTLESPEC block, false if it's a
	 * 		   standard BATTLE block.
	 */
	public boolean isBattleSpec();
}
