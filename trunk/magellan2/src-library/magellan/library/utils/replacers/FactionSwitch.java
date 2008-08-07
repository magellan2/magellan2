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
 * FactionSwitch.java
 *
 * Created on 30. Dezember 2001, 15:55
 */
package magellan.library.utils.replacers;

import java.util.StringTokenizer;

import magellan.library.utils.Resources;
import magellan.library.utils.filters.UnitFactionFilter;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class FactionSwitch extends AbstractParameterReplacer implements EnvironmentDependent,
																		SwitchOnly
{
	protected ReplacerEnvironment environment;

	/**
	 * Creates new FactionSwitch
	 */
	public FactionSwitch() {
		super(1);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String getDescription() {
		return Resources.get("util.replacers.factionswitch.description");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object src) {
		try {
			String fName = getParameter(0, src).toString();

			if(!fName.equals(Replacer.CLEAR)) {
				if(fName.indexOf(',') != -1) {
					StringTokenizer st = new StringTokenizer(fName, ",");

					while(st.hasMoreTokens()) {
						UnitFactionFilter filter = new UnitFactionFilter(st.nextToken());
						((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).addFilter(filter);
					}
				} else {
					UnitFactionFilter filter = new UnitFactionFilter(fName);
					((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).addFilter(filter);
				}
			} else {
				((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).removeFilters(UnitFactionFilter.class);
			}
		} catch(NullPointerException npe) {
		}

		return Replacer.EMPTY;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setEnvironment(ReplacerEnvironment env) {
		environment = env;
	}
}
