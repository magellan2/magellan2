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

/*
 * IfBranchReplacer.java
 *
 * Created on 20. Mai 2002, 17:05
 */
package magellan.library.utils.replacers;

import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0rn */
public class IfBranchReplacer implements BranchReplacer, ParameterReplacer {
	protected Object criterion;
	protected Object branches[] = new Object[2];

	/**
	 * Returns "else" for the first branch, "end" for the next branch.
	 * 
	 * @see magellan.library.utils.replacers.BranchReplacer#getBranchSign(int)
	 */
	public String getBranchSign(int index) {
		if(index == 1) {
			return Replacer.NEXT_BRANCH;
		}

		return Replacer.END;
	}

	/**
	 * @see magellan.library.utils.replacers.BranchReplacer#setBranch(int, java.lang.Object)
	 */
	public void setBranch(int index, Object obj) {
		branches[index] = obj;
	}

	/**
	 * Returns 2.
	 * 
	 * @see magellan.library.utils.replacers.BranchReplacer#getBranchCount()
	 */
	public int getBranchCount() {
		return 2;
	}

	/**
	 * Replaces by the first branch if the predicate is true, else by the second branch.
	 * 
	 * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
	 */
	public Object getReplacement(Object o) {
		if(criterion != null) {
			String ret = null;

			if(criterion instanceof Replacer) {
				try {
					ret = ((Replacer) criterion).getReplacement(o).toString();
				} catch(Exception exc) {
					return null;
				}
			} else {
				ret = criterion.toString();
			}

			int index = 1;

			if(ret.equals(Replacer.TRUE)) {
				index = 0;
			}

			if(branches[index] == null) {
				return Replacer.EMPTY;
			}

			if(branches[index] instanceof Replacer) {
				return ((Replacer) branches[index]).getReplacement(o);
			}

			return branches[index];
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
    return Resources.get("util.replacers.ifbranch.description");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getParameterCount() {
		return 1;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setParameter(int index, Object obj) {
		criterion = obj;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		try {
			return "if " + criterion + " then " + branches[0] + " else " + branches[1];
		} catch(Exception exc) {
		}

		return "IfReplacer";
	}
}
