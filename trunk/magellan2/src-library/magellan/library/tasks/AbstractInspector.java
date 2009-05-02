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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;

/**
 * This is an abstract implementation of an inspector. You can use this as a
 * base for your own implementation.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public abstract class AbstractInspector implements Inspector {
  protected AbstractInspector() {
  }

  /**
   * Calls reviewUnit(u,Problem.INFO), reviewUnit(u,Problem.WARNING)... etc. and
   * returns the joint list of problems.
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit)
   */
  public List<Problem> reviewUnit(Unit u) {
    if (checkIgnoreUnit(u))
      return Collections.emptyList();
    
    List<Problem> problems = new ArrayList<Problem>(10);

    problems.addAll(reviewUnit(u, Problem.INFORMATION));
    problems.addAll(reviewUnit(u, Problem.WARNING));
    problems.addAll(reviewUnit(u, Problem.ERROR));

    return problems.isEmpty() ? new ArrayList<Problem>() : problems;
  }

  /**
   * Returns <code>true</code> iff this unit's orders contain {@link #getSuppressComment()}.
   * Sub-classes should overwrite this to add more sophisticated ignoring of errors.
   * 
   * @param u
   * @return
   */
  protected boolean checkIgnoreUnit(Unit u) {
    for (String order : u.getOrders())
      if (order.equals(getSuppressComment()))
        return true;
    return false;
  }

  /**
   * Returns <code>true</code> iff this container's owner unit's orders contain
   * {@link #getSuppressComment()}. Sub-classes should overwrite this to add
   * more sophisticated ignoring of errors.
   * 
   * @param u
   * @return
   */
  protected boolean checkIgnoreUnitContainer(UnitContainer c) {
    if (c.getOwnerUnit()==null)
      return false;
    return checkIgnoreUnit(c.getOwnerUnit());
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link #reviewRegion(Region, int)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit,
   *      int)
   */
  public List<Problem> reviewUnit(Unit u, int type) {
    return Collections.emptyList();
  }

  /**
   * Calls reviewUnit(r,Problem.INFO), reviewUnit(r,Problem.WARNING)... etc. and
   * returns the joint list of problems.
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region)
   */
  public List<Problem> reviewRegion(Region r) {
    if (checkIgnoreRegion(r))
      return Collections.emptyList();
    List<Problem> problems = new ArrayList<Problem>(2);
    problems.addAll(reviewRegion(r, Problem.INFORMATION));
    problems.addAll(reviewRegion(r, Problem.WARNING));
    problems.addAll(reviewRegion(r, Problem.ERROR));

    return problems.isEmpty() ? new ArrayList<Problem>() : problems;
  }

  /**
   * Returns <code>false</code>.
   * Sub-classes should overwrite this method to add more sophisticated ignoring of errors.
   * 
   * @param u
   * @return <code>true</code> iff this region should be ignored for problems
   */
  protected boolean checkIgnoreRegion(Region r){
    return false;
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link #reviewUnit(Unit, int)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region,
   *      int)
   */
  public List<Problem> reviewRegion(Region r, int type) {
    return Collections.emptyList();
  }

  /**
   * @see magellan.library.tasks.Inspector#suppress(magellan.library.tasks.Problem)
   */
  public Unit suppress(Problem p) {

    // if the source is a unit we should suppress the problem in this unit's orders.
    if (p.getSource() instanceof Unit) {
      Unit u = (Unit) p.getSource();
      u.addOrder(getSuppressComment(), false, 0);
      return u;
    } else {
      // if the source is not a unit, we take the object
      if (p.getObject() instanceof Unit) {
        Unit u = (Unit) p.getObject();
        u.addOrder(getSuppressComment(), false, 0);
        return u;
      }
      // if the object is a container, we take the (old) owner unit
      if (p.getObject() instanceof UnitContainer){
        Unit u = ((UnitContainer) p.getObject()).getOwnerUnit();
        if (u!=null){
          u.addOrder(getSuppressComment(), false, 0);
          return u;
        }
      } 
    }
    return null;
  }

  protected String getSuppressComment() {
    return "; @suppressProblem" + getClass().getName();
  }

  public void unSuppress(Unit u) {
    List<String> newOrders = new ArrayList<String>(u.getOrders().size());
    boolean changed = false;
    for (String o : u.getOrders()){
      if (!o.equals(getSuppressComment())){
        newOrders.add(o);
      } else {
        changed = true;
      }
    }
    if (changed)
      u.setOrders(newOrders);
  }


}
