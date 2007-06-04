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

import magellan.library.Unit;
import magellan.library.utils.filters.UnitFilter;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class FilterSwitch implements ParameterReplacer, BranchReplacer, EnvironmentDependent {
	protected Object branch;
	protected ReplacerEnvironment env;
	protected UnitFilter myFilter;

	/**
	 * Creates new Template
	 */
	public FilterSwitch() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setParameter(int index, Object obj) {
		createFilter(obj);
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
	 * 
	 */
	public String getBranchSign(int index) {
		return END;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setBranch(int index, Object obj) {
		branch = obj;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getBranchCount() {
		return 1;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		if(branch != null) {
			if(branch instanceof Replacer) {
				Replacer r = (Replacer) branch;

				if((myFilter != null) && (env != null) &&
					   (env.getPart(ReplacerEnvironment.UNITSELECTION_PART) != null)) {
					UnitSelection us = (UnitSelection) env.getPart(ReplacerEnvironment.UNITSELECTION_PART);
					us.addFilter(myFilter);

					Object obj = r.getReplacement(o);
					us.removeFilter(myFilter);

					return obj;
				}

				return r.getReplacement(o);
			}

			return BLANK;
		}

		return BLANK;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setEnvironment(ReplacerEnvironment env) {
		this.env = env;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return null;
	}

	protected void createFilter(Object o) {
		myFilter = new MyFilterClass(o);
	}

	protected class MyFilterClass extends UnitFilter {
		int always = 0;
		protected Replacer rep = null;

		/**
		 * Creates a new MyFilterClass object.
		 *
		 * 
		 */
		public MyFilterClass(Object o) {
			if(o instanceof Replacer) {
				rep = (Replacer) o;
			} else {
				if(o.toString().equals(TRUE)) {
					always = 1;
				} else {
					always = 2;
				}
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public boolean acceptUnit(Unit u) {
			if(always != 0) {
				return always == 1;
			}

			try {
				String s = rep.getReplacement(u).toString();

				return s.equals(TRUE);
			} catch(Exception exc) {
			}

			return false;
		}
	}
}
