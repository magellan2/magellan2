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

import java.util.Iterator;

import magellan.library.Region;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.utils.Cache;


/**
 * A ZeroUnit mimics behaviour of a unit called "0".
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class MagellanZeroUnitImpl extends MagellanUnitImpl implements ZeroUnit {

	/**
	 * Creates a new ZeroUnit object.
	 *
	 * @param r the region this ZeroUnit belongs to.
	 */
	public MagellanZeroUnitImpl(Region r) {
		// pavkovic 2003.09.09: reduce amount of UnitID(0) from n regions to 1
		super(UnitID.createUnitID(0, r.getData().base));
		setRegion(r);
	}

	/**
	 * Sets the region of this unit
	 *
	 * @param r the region of this unit
	 */
	@Override
  public void setRegion(Region r) {
		if(r != getRegion()) {
			if(this.region != null) {
				this.region.removeUnit(this.getID());
			}

			// pavkovic 2002.09.30: dont add to region
			// this unit shall not exist in Region.units()
			// if (r != null)
			//	r.addUnit(this);
			this.region = r;
		}
	}

	/**
	 * Returns the amount of recruitable persons
	 *
	 * @return amount of recruitable persons
	 */
	@Override
  public int getPersons() {
		// 
		return getRegion().maxRecruit();
	}

	/**
	 * Returns the amount of recruitable persons - recruited persons
	 *
	 * @return amount of recruitable persons - recruited persons
	 */
	@Override
  public int getModifiedPersons() {
	  Cache cache = getCache();
		if(cache.modifiedPersons == -1) {
			cache.modifiedPersons = super.getModifiedPersons() - getGivenPersons();
		}

		return cache.modifiedPersons;
	}

	public int getGivenPersons() {
		// delivers the number of persons given to region via command "GIVE 0 x PERSONS"
		int result = 0;

		for(Iterator<PersonTransferRelation> iter = getPersonTransferRelations().iterator(); iter.hasNext();) {
			PersonTransferRelation ptr = iter.next();

			if(!(ptr instanceof RecruitmentRelation)) {
				result += ptr.amount;
			}
		}

		return result;
	}

	/**
	 * Returns a string representation of this temporary unit.
	 *
	 * @return a string representation of this temporary unit
	 */
	@Override
  public String toString() {
		return getRegion().toString();
	}
}
