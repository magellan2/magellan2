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
 * A default implementation serving as template for more specialized problems.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class SimpleProblem implements Problem {
  protected Severity severity;
  protected ProblemType type;
  protected String name;
  protected Region region;
  protected Unit owner;
  protected Faction faction;
  protected Object object;
  protected Inspector inspector;
  protected String message;
  protected int line;

  /**
   * Creates a new AbstractProblem object.
   * 
   * @param type One of {@link Problem.Severity#INFORMATION}, {@link Problem.Severity#WARNING},
   *          {@link Problem.Severity#ERROR}
   * @param region A region where the problem occurs
   * @param owner The unit responsible for this problem or <code>null</code>. If
   *          <code>line &ge; 0 </code>, it refers to an order of this unit.
   * @param faction The faction this problem belongs to or <code>null</code>
   * @param object The object that this problem criticizes
   * @param inspector The Inspector that reported this problem
   * @param message The message text of the problem
   * @param line The line number in the orders of owner where the problem occured or -1 if no such
   *          order can be identified. The first line is line 1!
   * @throws NullPointerException if object, inspector or message is <code>null</code>.
   */
  public SimpleProblem(Severity severity, ProblemType type, Region region, Unit owner,
      Faction faction, Object object, Inspector inspector, String message, int line) {
    // if (object == null || inspector == null || message == null)
    // throw new NullPointerException();
    this.severity = severity;
    this.type = type;
    this.region = region;
    this.owner = owner;
    this.faction = faction;
    this.object = object;
    this.inspector = inspector;
    this.message = message;
    this.line = line;
  }

  /**
   * Returns the value of faction.
   * 
   * @see magellan.library.tasks.Problem#getFaction()
   */
  public Faction getFaction() {
    return faction;
  }

  /**
   * Returns the value of object.
   * 
   * @see magellan.library.tasks.Problem#getObject()
   */
  public Object getObject() {
    return object;
  }

  /**
   * @see magellan.library.tasks.Problem#getType()
   */
  public Severity getSeverity() {
    return severity;
  }

  /**
   * @see magellan.library.tasks.Problem#getType()
   */
  public ProblemType getType() {
    return type;
  }

  /**
   * @see magellan.library.tasks.Problem#getLine()
   */
  public int getLine() {
    return line;
  }

  /**
   * Returns the value of region.
   * 
   * @see magellan.library.tasks.Problem#getRegion()
   */
  public Region getRegion() {
    return region;
  }

  /**
   * Returns the value of owner.
   * 
   * @see magellan.library.tasks.Problem#getOwner()
   */
  public Unit getOwner() {
    return owner;
  }

  /**
   * Returns the value of message.
   * 
   * @see magellan.library.tasks.Problem#getMessage()
   */
  public String getMessage() {
    return message;
  }

  /**
   * @see magellan.library.tasks.Problem#getInspector()
   */
  public Inspector getInspector() {
    return inspector;
  }

  /**
   * Returns the message of the problem.
   */
  @Override
  public String toString() {
    return message;
  }

  /**
   * Adds a comment to the unit that is responsible for this problem. This comment causes the
   * problem to be suppressed in subsequent runs of inspectors.
   * 
   * @see magellan.library.tasks.Problem#addSuppressComment()
   */
  public Unit addSuppressComment() {
    if (getInspector() != null)
      return getInspector().suppress(this);
    return null;
  }

}
