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

import java.util.List;

import magellan.library.Border;
import magellan.library.ID;
import magellan.library.utils.Direction;


/**
 * Container class for a region border based on its representation in a cr version > 45.
 *
 * @see magellan.library.Region#getBorders()
 */
public class MagellanBorderImpl extends MagellanIdentifiableImpl implements Border {
	/**
	 * The direction in which the border lies. The value must  be one of the DIR_XXX constants in
	 * class Direction.
	 */
	private int direction;

	/** The type of this border. */
	private String type;

	/**
	 * Indicates, to what extend this border type is completed. Values may range from 0 to 100, or
	 * -1 standing for an uninitialized/invalid value.
	 */
	private int buildRatio;

	/** A list containing <tt>String</tt> objects, specifying  effects on this border. */
	private List<String> effects;

	/**
	 * Create a new <tt>Border</tt> object with the specified id.
	 *
	 * @param id the id of the border
	 */
	public MagellanBorderImpl(ID id) {
		this(id, Direction.DIR_INVALID, null, -1);
	}

	/**
	 * Create a new <tt>Border</tt> object initialized to the specified values.
	 *
	 * @param id the id of the border
	 * @param direction the direction of the border
	 * @param type the type of the border
	 * @param buildRatio indicates, to what extend this border type is completed (e.g. street)
	 */
	public MagellanBorderImpl(ID id, int direction, String type, int buildRatio) {
		super(id);
		this.direction = direction;
		this.type = type;
		this.buildRatio = buildRatio;
	}

	/**
	 * Return a string representation of this <tt>Border</tt> object.
	 *
	 * @return Border object as string.
	 */
	public String toString() {
		if(buildRatio == 100 || buildRatio <0) {
			return type + ": " + Direction.toString(direction);
		} else {
			return type + ": " + Direction.toString(direction) + " (" + buildRatio + "%)";
		}
	}

  public int getBuildRatio() {
    return buildRatio;
  }

  public int getDirection() {
    return direction;
  }

  public List<String> getEffects() {
    return effects;
  }

  public String getType() {
    return type;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setBuildRatio(int buildratio) {
    this.buildRatio = buildratio;
  }

  public void setEffects(List<String> effects) {
    this.effects = effects;
  }
}
