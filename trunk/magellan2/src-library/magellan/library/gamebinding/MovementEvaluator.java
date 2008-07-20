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

package magellan.library.gamebinding;

import magellan.library.Unit;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface MovementEvaluator {
	/** The unit does not possess horses */
	public static final int CAP_NO_HORSES = Integer.MIN_VALUE;

	/* The unit is not sufficiently skilled in horse riding */

	/** DOCUMENT-ME */
	public static final int CAP_UNSKILLED = MovementEvaluator.CAP_NO_HORSES + 1;

	/**
	 * Returns the maximum payload in GE  100 of this unit when it travels by horse. Horses, carts
	 * and persons are taken into account for this calculation. If the unit has a sufficient skill
	 * in horse riding but there are too many carts for the horses, the weight of the additional
	 * carts are also already considered.
	 *
	 * @return the payload in GE  100, CAP_NO_HORSES if the unit does not possess horses or
	 * 		   CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
	 * 		   horseback.
	 */
	public int getPayloadOnHorse(Unit unit);

	/**
	 * Returns the maximum payload in GE  100 of this unit when it travels on foot. Horses, carts
	 * and persons are taken into account for this calculation. If the unit has a sufficient skill
	 * in horse riding but there are too many carts for the horses, the weight of the additional
	 * carts are also already considered. The calculation also takes into account that trolls can
	 * tow carts.
	 *
	 * @return the payload in GE  100, CAP_UNSKILLED if the unit is not sufficiently skilled in
	 * 		   horse riding to travel on horseback.
	 */
	public int getPayloadOnFoot(Unit unit);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public int getLoad(Unit unit);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public int getModifiedLoad(Unit unit);
  
  /**
   * The initial weight of the unit as it appears in 
   * the report. This should be the game dependent version used
   * to calculate the weight if the information is not available
   * in the report. 
   *
   * @return the weight of the unit in silver (GE 100).
   */
  public int getWeight(Unit unit);

  /**
   * The modified weight is calculated from the modified number 
   * of persons and the modified items. Due to some game 
   * dependencies this is done in this class.
   *
   * @return the modified weight of the unit in silver (GE 100).
   */
  public int getModifiedWeight(Unit unit);
}
