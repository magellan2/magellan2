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
 * UnitSkillCountReplacer.java
 *
 * Created on 30. Dezember 2001, 17:23
 */
package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class UnitSkillCountReplacer extends AbstractParameterReplacer
	implements EnvironmentDependent
{
	/** DOCUMENT-ME */
	public static final int MODE_SKILL = 0;

	/** DOCUMENT-ME */
	public static final int MODE_SKILL_MIN = 1;

	/** DOCUMENT-ME */
	public static final int MODE_SKILL_SUM = 2;

	/** DOCUMENT-ME */
	public static final int MODE_SKILL_SUM_MIN = 3;
	protected int mode;
	private static final int MODE_LENGTHS[] = { 1, 2, 1, 2 };
	protected ReplacerEnvironment environment;

	/**
	 * Creates new UnitSkillCountReplacer
	 *
	 * 
	 */
	public UnitSkillCountReplacer(int mode) {
		super(UnitSkillCountReplacer.MODE_LENGTHS[mode]);
		this.mode = mode;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		if(!(o instanceof Region)) {
			return null;
		}

		boolean minMode = ((mode == UnitSkillCountReplacer.MODE_SKILL_MIN) || (mode == UnitSkillCountReplacer.MODE_SKILL_SUM_MIN));
		boolean sumMode = ((mode == UnitSkillCountReplacer.MODE_SKILL_SUM) || (mode == UnitSkillCountReplacer.MODE_SKILL_SUM_MIN));
		int min = 1;
		String skill = getParameter(0, o).toString();

		if(minMode) {
			Object obj = getParameter(1, o);

			if(obj instanceof Number) {
				min = ((Number) obj).intValue();
			} else {
				try {
					min = (int) Double.parseDouble(obj.toString());
				} catch(NumberFormatException nfe) {
				}
			}
		}

		Collection units = ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).getUnits((Region) o);

		if((units == null) || (units.size() == 0)) {
			return new Integer(0);
		}

		int count = 0;
		Iterator it = units.iterator();

		while(it.hasNext()) {
			Unit u = (Unit) it.next();
			Iterator it2 = u.getSkills().iterator();

			while(it2.hasNext()) {
				Skill sk = (Skill) it2.next();
				SkillType sty = sk.getSkillType();

				if(sty.getName().equals(skill) || sty.getID().toString().equals(skill)) {
					if(!minMode || (sk.getLevel() >= min)) {
						if(sumMode) {
							count += (u.getPersons() * sk.getLevel());
						} else {
							count += u.getPersons();
						}
					}

					break;
				}
			}
		}

		return new Integer(count);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String getDescription() {
		return Resources.get("util.replacers.unitskillcountreplacer.description." + mode);
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
