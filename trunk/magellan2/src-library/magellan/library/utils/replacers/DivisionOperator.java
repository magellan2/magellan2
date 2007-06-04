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
 * An division operator.
 * 
 * <p>
 * A Divide By Zero returns <i>null</i>.
 * </p>
 *
 * @author Andreas
 * @version
 */
public class DivisionOperator extends AbstractOperator {
	/**
	 * Creates a new DivisionOperator object.
	 */
	public DivisionOperator() {
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
		if(((Number) numbers[1]).floatValue() == 0) {
			return null;
		}

		return new Float(((Number) numbers[0]).floatValue() / ((Number) numbers[1]).floatValue());
	}
}
