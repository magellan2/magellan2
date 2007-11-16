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

package magellan.library.utils.replacers;



/**
 * An multiplication operator.
 *
 * @author Andreas
 * @version 1.0
 */
public class MultiplicationOperator extends AbstractOperator {
	/**
	 * Creates a new MultiplicationOperator object.
	 */
	public MultiplicationOperator() {
		super(2);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object compute(Object numbers[]) {
		return new Float(((Number) numbers[0]).floatValue() * ((Number) numbers[1]).floatValue());
	}

}
