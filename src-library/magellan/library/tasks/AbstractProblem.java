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

package magellan.library.tasks;

import magellan.library.Faction;
import magellan.library.HasRegion;
import magellan.library.Unit;
import magellan.library.UnitContainer;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public abstract class AbstractProblem implements Problem {
	protected Object source;
	protected HasRegion object;
	protected Inspector inspector;
	protected String message;
	protected int line;

	/**
	 * Creates a new AbstractProblem object.
	 *
	 * @param s The origin of the problem
   * @param o The object that this problem critisizes
	 * @param i The Inspector that reported this problem
   * @param m The message text of the problem  
	 * 
   * @throws NullPointerException if any of the parameters is null
	 */
	public AbstractProblem(Object s, HasRegion o, Inspector i, String m) {
		this(s, o, i, m, -1);
	}

	/**
	 * Creates a new AbstractProblem object.
	 *
   * @param s The origin of the problem
   * @param o The object that this problem critisizes
   * @param i The Inspector that reported this problem
   * @param m The message text of the problem
   * @param l The line number where the problem occured  
	 *
	 * @throws NullPointerException if any of the parameters is null
	 */
	public AbstractProblem(Object s, HasRegion o, Inspector i, String m, int l) {
		if((s == null) || (o == null) || (i == null) || (m == null)) {
			throw new NullPointerException();
		}

		source = s;
		object = o;
		inspector = i;
		message = m;
		line = l;
	}

	/**
	 * returns the type of the problem
	 *
	 */
	public abstract int getType();

	/**
	 * @see magellan.library.tasks.Problem#getLine()
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @see magellan.library.tasks.Problem#getInspector()
	 */
	public Inspector getInspector() {
		return inspector;
	}

	/**
	 * @see magellan.library.tasks.Problem#getSource()
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @see magellan.library.tasks.Problem#getObject()
	 */
	public HasRegion getObject() {
		return object;
	}

	/**
	 * Returns the message of the problem.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return message;
	}
	
	/**
	 * Returns the faction owning this problem by evaluating the object.
	 * 
	 * @see magellan.library.tasks.Problem#getFaction()
	 */
	public Faction getFaction(){
	  HasRegion hasR = getObject();
	  Faction faction = null;
	  if (hasR instanceof Unit) {
      faction = ((Unit) hasR).getFaction();
    } else if(hasR instanceof UnitContainer){
	    Unit owner = ((UnitContainer) hasR).getOwnerUnit();
	    faction=owner!=null?owner.getFaction():null;
	  }
	  
	  return faction;
	}
	
	/**
	 * Adds a comment to the unit that is responsible for this problem. This comment causes the problem
	 * to be suppressed in subsequent runs of inspectors.
	 * 
	 * @see magellan.library.tasks.Problem#addSuppressComment()
	 */
	public Unit addSuppressComment(){
    return getInspector().suppress(this);
	}
	
}
