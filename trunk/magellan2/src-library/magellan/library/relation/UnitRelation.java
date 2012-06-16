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

package magellan.library.relation;

import java.util.HashMap;

import magellan.library.Unit;
import magellan.library.tasks.Problem;
import magellan.library.tasks.ProblemFactory;
import magellan.library.tasks.ProblemType;

/**
 * A (possibly abstract) relation originating from a source unit.
 */
public abstract class UnitRelation {
  /**
   * The origin of this relation, normally the same as source. This is the unit that causes the
   * relation.
   * 
   * @see #source
   */
  public Unit origin;

  /** The source of this relation. This is the semantic origin of this relation. */
  public Unit source;

  /** The line in the source's orders that is the cause for this relation. The first line is 1. */
  public int line;

  public Problem problem;

  /**
   * Creates a new UnitRelation object.
   * 
   * @param origin The origin unit
   * @param source The source unit
   * @param line The line in the source's orders. The first line is 1.
   */
  public UnitRelation(Unit origin, Unit source, int line) {
    if (origin == null)
      throw new NullPointerException("relation without origin");
    this.origin = origin;
    this.source = source;
    this.line = line;
  }

  /**
   * Creates a new UnitRelation object.
   * 
   * @param source The source unit
   * @param line The line in the source's orders
   */
  public UnitRelation(Unit source, int line) {
    this(source, source, line);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getClass().getName() + "@ORIGIN=" + origin + "@SOURCE=" + source + "@line=" + line
        + "@WARNING=" + problem;
  }

  /**
	 * 
	 */
  public static class ID {
    protected static int lastID = -1;
    int id;

    protected ID() {
      id = ++ID.lastID;
    }

    /**
     * Returns true if the
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ID)
        return id == ((ID) obj).id;
      return false;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }

  private static HashMap<Class<? extends UnitRelation>, ID> ids =
      new HashMap<Class<? extends UnitRelation>, ID>();

  /**
   * Returns a unique ID for every subclass of UnitRelation. The ID for a given class will always be
   * the same for one run, but IDs may differ for different instances of this
   * <em>UnitRelation.class</em>.
   * 
   * @param clazz A subclass of UnitRelation.
   * @return An ID that is unique for each class.
   */
  public static ID getClassID(Class<? extends UnitRelation> clazz) {
    ID result = UnitRelation.ids.get(clazz);
    if (result == null) {
      result = new ID();
      UnitRelation.ids.put(clazz, result);
    }
    return result;
  }

  /**
   * Attaches an order to all report objects it is relevant to.
   */
  public void add() {
    if (origin != source && origin != null) {
      origin.addRelation(this);
    }
    if (source != null) {
      source.addRelation(this);
    }
  }

  public void setWarning(String string, ProblemType type) {
    problem =
        ProblemFactory.createProblem(Problem.Severity.WARNING, type, origin, null, string, line);
  }

  public void setError(String string, ProblemType type) {
    problem =
        ProblemFactory.createProblem(Problem.Severity.ERROR, type, origin, null, string, line);
  }

  /**
   * Returns true if this relation somehow relates to the specified object. Subclasses should
   * overwrite this method if they involve relations to other report objects.
   * 
   * @param object
   * @return <code>true</code> if the object is source or origin or otherwise affected by this
   *         relation
   */
  public boolean isRelated(Object object) {
    return origin == object || source == object;
  }

}
