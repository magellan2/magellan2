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

  /** if something is amiss, this should be true */
  public boolean warning;

  /**
   * Creates a new UnitRelation object.
   * 
   * @param origin The origin unit
   * @param source The source unit
   * @param line The line in the source's orders. The first line is 1.
   * @param warning <code>true</code> iff this relation causes a warning
   */
  public UnitRelation(Unit origin, Unit source, int line, boolean warning) {
    this.origin = origin;
    this.source = source;
    this.line = line;
    this.warning = warning;
  }

  /**
   * Creates a new UnitRelation object.
   * 
   * @param source The source unit
   * @param line The line in the source's orders. The first line is 1.
   * @param warning <code>true</code> iff this relation causes a warning
   */
  public UnitRelation(Unit source, int line, boolean warning) {
    this(source, source, line, warning);
  }

  /**
   * Creates a new UnitRelation object.
   * 
   * @param source The source unit
   * @param line The line in the source's orders
   */
  public UnitRelation(Unit source, int line) {
    this(source, line, false);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getClass().getName() + "@ORIGIN=" + origin + "@SOURCE=" + source + "@line=" + line
        + "@WARNING=" + warning;
  }

  /**
   * Returns true iff this relation is caused by a long order
   * 
   * @return true iff this relation is caused by a long order
   */
  public boolean isLongOrder() {
    return this instanceof LongOrderRelation;
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
    if (origin != source) {
      origin.addRelation(this);
    }
    source.addRelation(this);
  }

}
