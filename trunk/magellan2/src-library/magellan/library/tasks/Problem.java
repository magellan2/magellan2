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

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface Problem {
	/**
   * A problem with this type has informational character. There is not
   * necessarily something wrong.
   */
	public static final int INFORMATION = 0;

	/** A problem of this type indicates that something might be wrong depending on context. */
	public static final int WARNING = 1;

	/** A problem of this type indicates some kind of error. */
	public static final int ERROR = 2;

	/**
	 * Returns the inspector that created this problem.
	 * 
	 */
	public Inspector getInspector();

	/**
	 * Returns the type of the problem. Current supported types are INFORMATION, WARNING, and ERROR.
	 * 
	 */
	public int getType();

	/**
	 * Returns the line in the orders of the unit that
	 * 
	 */
	public int getLine();

	/**
	 * Returns the object this problem criticizes.
	 * 
	 */
	public HasRegion getObject();

  /**
   * Returns the originating object, i.e., the object that was checked when this
   * problem was created.
   */
	public Object getSource();

	/**
	 * Returns the message of the problem.
	 * 
	 */
	public String toString();

  /**
   * Returns the faction this problem belongs to.
   * 
   * @return The faction this problem belongs to or <code>null</code> if not applicable
   */
  public Faction getFaction();

  /**
   * Modifies the orders such that this problem is not listed by the inspector
   * in the future, i.e. by adding a comment to the source unit's orders. Note
   * that it is in the responsibility of the caller to fire OrderChangedEvents.
   * 
   * @return Returns a Unit whose orders were changed to suppress this warning
   *         or <code>null</code> if no orders were changed
   */
  public Unit addSuppressComment();

}
