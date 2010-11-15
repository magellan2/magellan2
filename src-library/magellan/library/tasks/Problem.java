/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.tasks;

import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Unit;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface Problem {

  public enum Severity {
    INFORMATION, WARNING, ERROR
  }

  /**
   * Returns the severity of the problem. Current supported types are INFORMATION, WARNING, and
   * ERROR.
   */
  public Severity getSeverity();

  /**
   * Returns the type of the problem.
   */
  public ProblemType getType();

  /**
   * Returns the inspector that created this problem.
   */
  public Inspector getInspector();

  /**
   * Returns the line in the orders of the unit that caused the problem. The first order is line 1!
   * 
   * @return The line that is the cause of the order or -1 if no single line can be made responsible
   */
  public int getLine();

  /**
   * Returns the object this problem criticizes.
   */
  public Object getObject();

  /**
   * Returns the faction this problem belongs to.
   * 
   * @return The faction this problem belongs to or <code>null</code> if not applicable
   */
  public Faction getFaction();

  /**
   * Returns the value of region.
   * 
   * @return Returns region.
   */
  public Region getRegion();

  /**
   * Returns a unit responsible for this problem.
   * 
   * @return Returns owner.
   */
  public Unit getOwner();

  /**
   * Returns a message for the user.
   * 
   * @return Returns message.
   */
  public String getMessage();

  /**
   * Modifies the orders such that this problem is not listed by the inspector in the future, i.e.
   * by adding a comment to the source unit's orders. Note that it is in the responsibility of the
   * caller to fire OrderChangedEvents.
   * 
   * @return Returns a Unit whose orders were changed to suppress this warning or <code>null</code>
   *         if no orders were changed
   */
  public Unit addSuppressComment();

}
